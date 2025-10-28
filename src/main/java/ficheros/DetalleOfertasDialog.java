package ficheros;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Diálogo modal que muestra los detalles de los archivos de oferta.
 * Puede operar en dos modos:
 * <ul>
 * <li>Vista por lote: Muestra los archivos de oferta esperados y su estado para un lote específico.</li>
 * <li>Vista global: Muestra todos los archivos de oferta que han sido cargados en cualquier lote
 * (o en la oferta única) con sus detalles.</li>
 * </ul>
 * Utiliza un renderizador de celdas personalizado para colorear el estado de carga.
 */
public class DetalleOfertasDialog extends JDialog {

    /**
     * Referencia al {@link FileManager} para acceder a los datos de los archivos cargados.
     */
    private final FileManager fileManager;
    /**
     * Referencia al objeto {@link Configuracion} de la licitación.
     */
    private final Configuracion configuracion;
    /**
     * El número del lote para el que se muestran los detalles.
     * Un valor de {@code -1} indica el modo de vista global (todos los archivos cargados).
     */
    private final int numeroLote;

    /**
     * Renderizador de celda personalizado para la columna "Estado" de la tabla.
     * Asigna colores de fondo a las celdas según el estado del archivo (Cargado, Parcialmente cargado, No cargado).
     */
    private class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String estado = (String) value;
            Color colorFondo;

            // Lógica de color de fondo según el estado
            if (estado.equals("Cargado")) {
                colorFondo = new Color(144, 238, 144); // Verde claro
            } else if (estado.equals("Parcialmente cargado")) {
                colorFondo = new Color(255, 255, 153); // Amarillo claro
            } else { // "No cargado" o cualquier otro estado no reconocido
                colorFondo = new Color(255, 182, 193); // Rojo claro
            }

            if (isSelected) {
                cell.setBackground(table.getSelectionBackground());
            } else {
                cell.setBackground(colorFondo);
            }

            setHorizontalAlignment(JLabel.CENTER);

            return cell;
        }
    }

    /**
     * Constructor para crear e inicializar el diálogo de detalles de ofertas.
     *
     * @param owner La ventana padre {@code JFrame} de este diálogo.
     * @param fileManager El gestor de archivos para obtener los datos de la licitación.
     * @param configuracion El objeto de configuración de la licitación.
     * @param numeroLote El número del lote a mostrar. Usar {@code -1} para una vista global de todos los archivos cargados.
     */
    public DetalleOfertasDialog(JFrame owner, FileManager fileManager, Configuracion configuracion, int numeroLote) {

        // La llamada a super() debe ser la PRIMERA declaración ejecutable del constructor.
        super(owner, calcularTitulo(configuracion, numeroLote), true);

        // Asignación de variables de instancia (DESPUÉS de super())
        this.fileManager = fileManager;
        this.configuracion = configuracion;
        this.numeroLote = numeroLote;

        // Configuración de la ventana
        setLayout(new BorderLayout());
        setSize(800, 500);
        setResizable(true);
        setLocationRelativeTo(owner);

        // Panel principal con margen y color de fondo
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Crear la tabla de detalles con un modelo de tabla por defecto
        String[] columnNames = {"Archivo", "Obligatorio", "Confidencial", "Estado", "Supuestos Confidencialidad"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas no son editables en este diálogo de detalle
            }
        };
        JTable detalleTable = new JTable(tableModel);
        detalleTable.setRowHeight(25); // Altura de fila para mejor legibilidad
        detalleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detalleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        detalleTable.setFillsViewportHeight(true); // Ocupar todo el espacio disponible

        // Aplicar el renderizador de estado a la columna "Estado" (índice 3)
        detalleTable.getColumnModel().getColumn(3).setCellRenderer(new EstadoCellRenderer());
        
        // Ajustar anchos de columna iniciales
        detalleTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Archivo
        detalleTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Obligatorio
        detalleTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Confidencial
        detalleTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado
        detalleTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Supuestos

        // La tabla debe estar en un JScrollPane
        JScrollPane scrollPane = new JScrollPane(detalleTable);

        // Añadir el JScrollPane al panel principal
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Llenar la tabla con los datos correspondientes al lote o la vista global
        llenarTablaDetalles(tableModel);

        // Botón de cerrar y configuración final
        JButton closeButton = new JButton("Cerrar");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setBackground(new Color(200, 220, 240)); // Color azul claro
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.setOpaque(false); // Para que se vea el color de fondo del mainPanel

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        
        // Empaquetar y mostrar
        pack(); // Ajusta el tamaño de la ventana a sus componentes preferidos
        // Aunque pack ajusta el tamaño, se puede volver a establecer un tamaño fijo si es preferible
        // Esto puede ser útil si se quiere asegurar un tamaño mínimo o una apariencia consistente.
        // setSize(800, 500); // Descomentar si se prefiere un tamaño fijo después de pack.
        setVisible(true); // Hacer el diálogo visible.
    }

    /**
     * Calcula y devuelve el título apropiado para el diálogo basándose en si es una
     * vista global o de un lote específico, y si la licitación tiene lotes.
     * Este método es {@code static} para poder ser llamado antes de {@code super()} en el constructor.
     *
     * @param configuracion El objeto de configuración de la licitación.
     * @param numeroLote El número del lote. Usar {@code -1} para vista global.
     * @return El título de la ventana como {@code String}.
     */
    private static String calcularTitulo(Configuracion configuracion, int numeroLote) {
        boolean esVistaGlobal = (numeroLote == -1);

        if (esVistaGlobal) {
            return "Detalles de TODAS las Ofertas Cargadas (Vista Global)";
        } else if (configuracion.isTieneLotes()) {
            return "Detalles del Lote " + numeroLote;
        } else {
            return "Detalles de la Oferta Única";
        }
    }
    
    /**
     * Llena el {@code DefaultTableModel} con los datos de los archivos de oferta.
     * La lógica se adapta para mostrar detalles de un lote específico o una vista global
     * de todos los archivos cargados.
     *
     * @param model El {@code DefaultTableModel} de la tabla a llenar.
     */
    private void llenarTablaDetalles(DefaultTableModel model) {
        model.setRowCount(0); // Limpiar filas existentes

        boolean esVistaGlobal = (this.numeroLote == -1);

        if (esVistaGlobal) {
            // **VISTA GLOBAL: Iterar sobre TODOS los archivos cargados**
            
            // Mapear la configuración por nombre base para buscar las propiedades (obligatorio/confidencial)
            Map<String, Configuracion.ArchivoOferta> configMap = Arrays.stream(configuracion.getArchivosOferta())
                .collect(Collectors.toMap(
                    Configuracion.ArchivoOferta::getNombre,
                    oferta -> oferta
                ));

            // Iterar sobre los archivos cargados en FileManager
            for (Map.Entry<String, FileData> entry : fileManager.getArchivosOferta().entrySet()) {
                String fullKey = entry.getKey(); // Ej: "Lote3_Documento1" o "Documento1"
                FileData archivoCargado = entry.getValue();

                String nombreMostrado;    // Nombre que se muestra en la tabla
                String configKey;         // Clave para buscar en configMap (siempre el nombre del documento sin prefijo de lote)

                // 1. Determinar el nombre a mostrar y la clave de configuración
                if (configuracion.isTieneLotes() && fullKey.contains("_")) {
                    // Caso con Lotes (Ej: Lote3_Documento1)
                    int ultimoGuion = fullKey.lastIndexOf('_');
                    String etiquetaLote = fullKey.substring(0, ultimoGuion); // "Lote3"
                    configKey = fullKey.substring(ultimoGuion + 1);          // "Documento1"
                    
                    nombreMostrado = etiquetaLote + " - " + configKey;
                } else {
                    // Caso Oferta Única (Ej: Documento1)
                    configKey = fullKey;
                    nombreMostrado = fullKey;
                }
                
                // 2. Obtener la configuración (puede ser null si el archivo cargado no está en la config,
                // aunque no debería pasar si la validación es correcta y solo se cargan archivos definidos)
                Configuracion.ArchivoOferta ofertaConfig = configMap.get(configKey);

                // 3. Determinar propiedades de la fila
                String estado = "Cargado"; // En vista global, solo listamos los que ya están cargados
                // Si ofertaConfig es null, asumimos "No" para obligatorio y confidencial por seguridad
                String esObligatorio = (ofertaConfig != null && ofertaConfig.esObligatorio()) ? "Sí" : "No";
                String esConfidencial = archivoCargado.esConfidencial() ? "Sí" : "No";
                String supuestos = archivoCargado.esConfidencial() ?
                                         String.join(", ", archivoCargado.getMotivosSupuestos()) : "N/A";

                // 4. Añadir la fila a la tabla
                model.addRow(new Object[]{
                    nombreMostrado, // USAMOS el nombre con el prefijo de lote (si aplica)
                    esObligatorio,
                    esConfidencial,
                    estado,
                    supuestos
                });
            } // Fin del for (archivos cargados)
            
        } else {
            // **VISTA POR LOTE/OFERTA ÚNICA: Lógica existente y funcional (numeroLote >= 0)**

            // Determina el prefijo del lote (ej: "Lote1_" o "" para oferta única)
            String loteKeyPrefix = configuracion.isTieneLotes() ? "Lote" + numeroLote + "_" : "";

            // Itera sobre la configuración para saber qué archivos esperar en este lote/oferta única
            for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                String fullKey = loteKeyPrefix + ofertaConfig.getNombre();
                FileData archivoCargado = fileManager.getArchivosOferta().get(fullKey);

                String estado = (archivoCargado != null) ? "Cargado" : "No cargado";
                String esObligatorio = ofertaConfig.esObligatorio() ? "Sí" : "No";
                
                String esConfidencial = (archivoCargado != null && archivoCargado.esConfidencial()) ? "Sí" : "No";
                String supuestos = (archivoCargado != null && archivoCargado.esConfidencial()) ?
                                         String.join(", ", archivoCargado.getMotivosSupuestos()) :
                                         "N/A";

                // Añadir la fila a la tabla
                model.addRow(new Object[]{
                    ofertaConfig.getNombre(),
                    esObligatorio,
                    esConfidencial,
                    estado,
                    supuestos
                });
            } // Fin del for (archivos de oferta en la configuración)
        }
    }
}