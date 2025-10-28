package ficheros;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;

/**
 * Diálogo modal para cargar archivos "comunes" o complementarios al
 * procedimiento de licitación. Estos archivos no están ligados a un lote
 * específico.
 * <p>
 * Permite al usuario seleccionar el tipo de documento común a cargar (según la
 * configuración) y manejar la lógica de declaración de confidencialidad antes
 * de llamar al {@link FileManager}.
 * </p>
 *
 * @see FileManager
 * @see ConfidencialidadDialog
 */
public class CargarArchivoComunDialog extends JDialog {

    /**
     * ComboBox para que el usuario seleccione el nombre del archivo común a
     * cargar.
     */
    private final JComboBox<String> archivoComboBox;
    /**
     * Botón para abrir el selector de archivos del sistema operativo.
     */
    private final JButton seleccionarArchivoButton;
    /**
     * Botón para cerrar el diálogo.
     */
    private final JButton cerrarButton;
    /**
     * Referencia al gestor de archivos para realizar las operaciones de carga.
     */
    private final FileManager fileManager;
    /**
     * Tarea {@code Runnable} que se ejecuta después de una carga exitosa para
     * actualizar la interfaz de usuario principal (ej: una tabla de archivos
     * cargados).
     */
    private final Runnable callback;

    /**
     * Constructor para crear e inicializar el diálogo de carga de archivos
     * comunes.
     *
     * @param parent La ventana padre del diálogo (normalmente el {@code JFrame}
     * principal).
     * @param fileManager El gestor de archivos para manejar la carga.
     * @param callback La acción a ejecutar tras la carga exitosa (ej:
     * actualizar tabla).
     */
    public CargarArchivoComunDialog(JFrame parent, FileManager fileManager, Runnable callback) {
        super(parent, "Cargar Archivo Común", true);
        this.fileManager = fileManager;
        this.callback = callback;
        setLayout(new BorderLayout(10, 10));

        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 5, 5));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instruccionLabel = new JLabel("<html>Seleccione un archivo de la lista para cargarlo o sobrescribirlo.</html>");
        String[] nombresArchivos = fileManager.getConfiguracion().getNombresArchivosComunes();
        archivoComboBox = new JComboBox<>(nombresArchivos);
        seleccionarArchivoButton = new JButton("Seleccionar Archivo");

        panelCentral.add(instruccionLabel);
        panelCentral.add(archivoComboBox);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(seleccionarArchivoButton);

        cerrarButton = new JButton("Finalizar");
        panelBotones.add(cerrarButton);

        add(panelCentral, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // --- Inicio de la lógica corregida ---
        seleccionarArchivoButton.addActionListener(e -> {
            String nombreSeleccionado = (String) archivoComboBox.getSelectedItem();
            if (nombreSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un tipo de archivo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // Buscar el índice del archivo seleccionado en la configuración
                String[] nombresComunes = fileManager.getConfiguracion().getNombresArchivosComunes();
                int index = Arrays.asList(nombresComunes).indexOf(nombreSeleccionado);

                // 1. Verificar si el archivo es susceptible de ser confidencial
                boolean esSusceptibleConfidencial = (index != -1 && fileManager.getConfiguracion().getArchivosComunesConfidenciales()[index]);
                boolean esConfidencial = false;
                String[] supuestosSeleccionados = null;
                String[] motivosSupuestos = null;

                // 2. Si es susceptible, preguntar al usuario
                if (esSusceptibleConfidencial) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Este archivo puede ser declarado como confidencial. ¿Desea marcarlo como tal?",
                            "Declarar Confidencialidad",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Si el usuario confirma, mostrar el diálogo de supuestos
                        // Se asume que ConfidencialidadDialog es accesible
                        ConfidencialidadDialog dialog = new ConfidencialidadDialog(this, fileManager.getConfiguracion().getSupuestosConfidencialidad());
                        dialog.setVisible(true);

                        if (dialog.isConfirmado()) {
                            Map<String, String> seleccion = dialog.getConfidencialidadSeleccionada();
                            esConfidencial = true;
                            supuestosSeleccionados = seleccion.keySet().toArray(new String[0]);
                            motivosSupuestos = seleccion.values().toArray(new String[0]);
                        } else {
                            // Si el usuario cancela, no se carga nada y se sale
                            return;
                        }
                    }
                }

                // 3. Cargar el archivo, pasando la información de confidencialidad
                if (fileManager.cargarArchivoComun(nombreSeleccionado, selectedFile, esConfidencial, supuestosSeleccionados, motivosSupuestos)) {
                    JOptionPane.showMessageDialog(this, "Archivo cargado correctamente.", "Carga exitosa", JOptionPane.INFORMATION_MESSAGE);
                    if (callback != null) {
                        callback.run();
                    }
                    //dispose(); // Mantenido comentado, ya que el original no lo hacía.
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar el archivo. Por favor, intente de nuevo.", "Error de carga", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // --- Fin de la lógica corregida ---

        cerrarButton.addActionListener(e -> {
            this.dispose();
        });

        pack();
        setLocationRelativeTo(parent);
    }
}
