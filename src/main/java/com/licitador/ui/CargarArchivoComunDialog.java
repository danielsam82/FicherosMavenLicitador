package com.licitador.ui;

import com.licitador.service.FileManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;

/**
 * A modal dialog for loading "common" or supplementary files for the tender procedure.
 * These files are not linked to a specific lot.
 * <p>
 * It allows the user to select the type of common document to load (according to the
 * configuration) and handle the confidentiality declaration logic before calling the
 * {@link FileManager}.
 * </p>
 *
 * @see FileManager
 * @see ConfidencialidadDialog
 */
public class CargarArchivoComunDialog extends JDialog {

    private final JComboBox<String> archivoComboBox;
    private final JButton seleccionarArchivoButton;
    private final JButton cerrarButton;
    private final FileManager fileManager;
    private final Runnable callback;

    /**
     * Constructs and initializes the dialog for loading common files.
     *
     * @param parent The parent window of the dialog (usually the main {@code JFrame}).
     * @param fileManager The file manager to handle the loading.
     * @param callback The action to execute after a successful load (e.g., update a table).
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

                String[] nombresComunes = fileManager.getConfiguracion().getNombresArchivosComunes();
                int index = Arrays.asList(nombresComunes).indexOf(nombreSeleccionado);

                boolean esSusceptibleConfidencial = (index != -1 && fileManager.getConfiguracion().getArchivosComunesConfidenciales()[index]);
                boolean esConfidencial = false;
                String[] supuestosSeleccionados = null;
                String[] motivosSupuestos = null;

                if (esSusceptibleConfidencial) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Este archivo puede ser declarado como confidencial. ¿Desea marcarlo como tal?",
                            "Declarar Confidencialidad",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        ConfidencialidadDialog dialog = new ConfidencialidadDialog(this, fileManager.getConfiguracion().getSupuestosConfidencialidad());
                        dialog.setVisible(true);

                        if (dialog.isConfirmado()) {
                            Map<String, String> seleccion = dialog.getConfidencialidadSeleccionada();
                            esConfidencial = true;
                            supuestosSeleccionados = seleccion.keySet().toArray(new String[0]);
                            motivosSupuestos = seleccion.values().toArray(new String[0]);
                        } else {
                            return;
                        }
                    }
                }

                if (fileManager.cargarArchivoComun(nombreSeleccionado, selectedFile, esConfidencial, supuestosSeleccionados, motivosSupuestos)) {
                    JOptionPane.showMessageDialog(this, "Archivo cargado correctamente.", "Carga exitosa", JOptionPane.INFORMATION_MESSAGE);
                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar el archivo. Por favor, intente de nuevo.", "Error de carga", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cerrarButton.addActionListener(e -> {
            this.dispose();
        });

        pack();
        setLocationRelativeTo(parent);
    }
}
