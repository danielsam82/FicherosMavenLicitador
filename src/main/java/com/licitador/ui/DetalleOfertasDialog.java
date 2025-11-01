package com.licitador.ui;

import com.licitador.service.Configuracion;
import com.licitador.service.FileData;
import com.licitador.service.FileManager;
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
 * A modal dialog that displays the details of the offer files.
 * It can operate in two modes:
 * <ul>
 * <li>Lot view: Shows the expected offer files and their status for a specific lot.</li>
 * <li>Global view: Shows all the offer files that have been loaded in any lot
 * (or in the single offer) with their details.</li>
 * </ul>
 * It uses a custom cell renderer to color the loading status.
 */
public class DetalleOfertasDialog extends JDialog {

    private final FileManager fileManager;
    private final Configuracion configuracion;
    private final int numeroLote;

    /**
     * A custom cell renderer for the "Status" column of the table.
     * It assigns background colors to the cells according to the file status (Loaded, Partially loaded, Not loaded).
     */
    private class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String estado = (String) value;
            Color colorFondo;

            if (estado.equals("Loaded")) {
                colorFondo = new Color(144, 238, 144);
            } else if (estado.equals("Partially loaded")) {
                colorFondo = new Color(255, 255, 153);
            } else {
                colorFondo = new Color(255, 182, 193);
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
     * Constructor to create and initialize the offer details dialog.
     *
     * @param owner The parent {@code JFrame} of this dialog.
     * @param fileManager The file manager to get the tender data.
     * @param configuracion The tender configuration object.
     * @param numeroLote The lot number to display. Use {@code -1} for a global view of all loaded files.
     */
    public DetalleOfertasDialog(JFrame owner, FileManager fileManager, Configuracion configuracion, int numeroLote) {

        super(owner, calcularTitulo(configuracion, numeroLote), true);

        this.fileManager = fileManager;
        this.configuracion = configuracion;
        this.numeroLote = numeroLote;

        setLayout(new BorderLayout());
        setSize(800, 500);
        setResizable(true);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        String[] columnNames = {"File", "Mandatory", "Confidential", "Status", "Confidentiality Assumptions"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable detalleTable = new JTable(tableModel);
        detalleTable.setRowHeight(25);
        detalleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detalleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        detalleTable.setFillsViewportHeight(true);

        detalleTable.getColumnModel().getColumn(3).setCellRenderer(new EstadoCellRenderer());
        
        detalleTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        detalleTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        detalleTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        detalleTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        detalleTable.getColumnModel().getColumn(4).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(detalleTable);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        llenarTablaDetalles(tableModel);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setBackground(new Color(200, 220, 240));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.setOpaque(false);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        
        pack();
        setVisible(true);
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