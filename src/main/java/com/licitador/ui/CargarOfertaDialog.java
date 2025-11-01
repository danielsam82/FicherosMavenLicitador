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
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A modal dialog for loading offer files. It allows the user to select the document
 * and, optionally, the lot to which it belongs, and upload it through the {@link FileManager}.
 * <p>
 * It manages the file selection, the logic of lot assignment, and the handling
 * of document confidentiality.
 * </p>
 *
 * @see FileManager
 * @see Configuracion
 * @see ConfidencialidadDialog
 */
public class CargarOfertaDialog extends JDialog {

    private final JComboBox<String> ofertaComboBox;
    private JComboBox<String> loteComboBox;
    private final FileManager fileManager;
    private final Configuracion configuracion;
    private final DefaultTableModel tableModel;
    private final JTable detalleTable;
    private final JLabel detallesLabel;

    private int numeroLote;
    private Map<Integer, String> lotesElegidos;
    private final boolean mostrarLoteComboBox;

    /**
     * Constructor for the **NO LOTS MODE** (Single Offer). It calls the main
     * constructor passing a map that represents Lot 0.
     *
     * @param parent The parent window of the dialog.
     * @param fileManager The file manager.
     * @param configuracion The procedure's configuration.
     */
    public CargarOfertaDialog(Frame parent, FileManager fileManager, Configuracion configuracion) {
        this(parent, fileManager, configuracion, Collections.singletonMap(0, "Single Offer"));
    }

    /**
     * Main constructor for the **WITH/WITHOUT LOTS MODE**.
     *
     * @param parent The parent window of the dialog.
     * @param fileManager The file manager.
     * @param configuracion The procedure's configuration.
     * @param lotesElegidos A map with the selected lots, where the key is the
     * lot ID and the value is the name. In no-lot mode, it only contains {0: "Single Offer"}.
     */
    public CargarOfertaDialog(Frame parent, FileManager fileManager, Configuracion configuracion, Map<Integer, String> lotesElegidos) {
        super(parent, "Cargar Archivo de Oferta", true);

        this.fileManager = fileManager;
        this.configuracion = configuracion;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);

        this.mostrarLoteComboBox = configuracion.isTieneLotes() && (lotesElegidos.size() > 1 || !lotesElegidos.containsKey(0));

        if (this.mostrarLoteComboBox) {
            this.lotesElegidos = lotesElegidos;
            this.numeroLote = lotesElegidos.keySet().iterator().next();
        } else {
            this.lotesElegidos = Collections.singletonMap(0, "Single Offer");
            this.numeroLote = 0;
        }

        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        String instruccion;
        if (this.mostrarLoteComboBox) {
            instruccion = "<html><b>Seleccione lote, documento y posteriormente cargue el Archivo</b></html>";
        } else {
            instruccion = "<html><b>Seleccione documento y posteriormente cargue el Archivo</b></html>";
        }

        JLabel instruccionLabel = new JLabel(instruccion);
        instruccionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(instruccionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        panelPrincipal.add(Box.createVerticalStrut(10), gbc);

        if (this.mostrarLoteComboBox) {
            JLabel loteLabel = new JLabel("Lote:");

            String[] lotesNombres = lotesElegidos.values().toArray(new String[0]);
            loteComboBox = new JComboBox<>(lotesNombres);
            loteComboBox.setPreferredSize(new Dimension(80, loteComboBox.getPreferredSize().height));

            loteComboBox.addActionListener(e -> {
                String nombreLoteSeleccionado = (String) loteComboBox.getSelectedItem();

                this.numeroLote = lotesElegidos.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(nombreLoteSeleccionado))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(0);

                llenarTablaDetalles();
                actualizarTituloTabla();
                if (getParent() instanceof MainWindow) {
                    ((MainWindow) getParent()).actualizarTablas();
                }
            });

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            panelPrincipal.add(loteLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            panelPrincipal.add(loteComboBox, gbc);
        } else {
            loteComboBox = null;
        }

        JLabel ofertaLabel = new JLabel("Documento a cargar:");

        String[] nombresOfertas;
        try {
            nombresOfertas = configuracion.getNombresArchivosOfertas();
            if (nombresOfertas == null || nombresOfertas.length == 0) {
                nombresOfertas = new String[]{"ERROR: No hay documentos configurados"};
            }
        } catch (Exception e) {
            System.err.println("Error al obtener nombres de ofertas de la configuración: " + e.getMessage());
            nombresOfertas = new String[]{"ERROR CRÍTICO: Configuración Inaccesible"};
        }

        ofertaComboBox = new JComboBox<>(nombresOfertas);
        ofertaComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXX");

        gbc.gridx = this.mostrarLoteComboBox ? 2 : 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panelPrincipal.add(ofertaLabel, gbc);

        gbc.gridx = this.mostrarLoteComboBox ? 3 : 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(ofertaComboBox, gbc);

        row++;

        JButton seleccionarArchivoButton = new JButton("Seleccionar Archivo y Cargar Documento");
        seleccionarArchivoButton.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        seleccionarArchivoButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        seleccionarArchivoButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton cerrarButton = new JButton("Finalizar y Cerrar");
        cerrarButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.add(seleccionarArchivoButton);
        buttonPanel.add(cerrarButton);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(buttonPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        panelPrincipal.add(Box.createVerticalStrut(15), gbc);

        detallesLabel = new JLabel("Archivos cargados:");
        detallesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(detallesLabel, gbc);

        String[] columnNames = {"File", "Mandatory", "Confidential", "Status", "Confidentiality Assumptions"};
        tableModel = new DefaultTableModel(columnNames, 0);
        detalleTable = new JTable(tableModel);

        JTableHeader header = detalleTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 240, 240));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        detalleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detalleTable.setRowHeight(25);
        detalleTable.setGridColor(new Color(220, 220, 220));
        detalleTable.setShowGrid(true);
        detalleTable.setIntercellSpacing(new Dimension(5, 5));
        detalleTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        detalleTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        detalleTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        detalleTable.getColumnModel().getColumn(3).setCellRenderer(new EstadoCellRenderer());

        JScrollPane scrollPane = new JScrollPane(detalleTable);
        scrollPane.setPreferredSize(new Dimension(750, 200));

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panelPrincipal.add(scrollPane, gbc);

        seleccionarArchivoButton.addActionListener(e -> {
            String nombreOfertaSeleccionada = (String) ofertaComboBox.getSelectedItem();

            if (nombreOfertaSeleccionada == null || nombreOfertaSeleccionada.startsWith("ERROR:")) {
                JOptionPane.showMessageDialog(this, "You must select a valid document from the list. Check your offer configuration.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String loteKeyPrefix = getLoteKeyPrefix();

            if (this.mostrarLoteComboBox && loteKeyPrefix.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must select a lot.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            String title = "Select file for: " + nombreOfertaSeleccionada;

            if (this.mostrarLoteComboBox && loteComboBox != null && loteComboBox.getSelectedItem() != null) {
                title += " (" + loteComboBox.getSelectedItem() + ")";
            } else if (!this.mostrarLoteComboBox) {
                title += " (Single Offer)";
            }
            fileChooser.setDialogTitle(title);

            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivoSeleccionado = fileChooser.getSelectedFile();

                boolean esConfidencial = false;
                String[] supuestosSeleccionados = null;
                String[] motivosSupuestos = null;

                Optional<Configuracion.ArchivoOferta> ofertaConfOptional = Arrays.stream(configuracion.getArchivosOferta())
                        .filter(oferta -> oferta.getNombre().equals(nombreOfertaSeleccionada))
                        .findFirst();

                if (ofertaConfOptional.isPresent() && ofertaConfOptional.get().esConfidencial()) {
                    int respuestaConfidencial = JOptionPane.showConfirmDialog(
                            this, "The file '" + nombreOfertaSeleccionada + "' can be confidential. Do you want to mark it as such?",
                            "File Confidentiality", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                    );

                    if (respuestaConfidencial == JOptionPane.YES_OPTION) {
                        ConfidencialidadDialog confidencialidadDialog = new ConfidencialidadDialog(CargarOfertaDialog.this, configuracion.getSupuestosConfidencialidad());
                        confidencialidadDialog.setVisible(true);

                        if (confidencialidadDialog.isConfirmado()) {
                            Map<String, String> seleccionConfidencialidad = confidencialidadDialog.getConfidencialidadSeleccionada();
                            esConfidencial = true;
                            supuestosSeleccionados = seleccionConfidencialidad.keySet().toArray(new String[0]);
                            motivosSupuestos = seleccionConfidencialidad.values().toArray(new String[0]);
                            if (supuestosSeleccionados.length == 0) {
                                JOptionPane.showMessageDialog(CargarOfertaDialog.this, "You must select at least one confidentiality assumption and a reason.", "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                }

                if (fileManager.cargarArchivoOferta(nombreOfertaSeleccionada, archivoSeleccionado, loteKeyPrefix, esConfidencial, supuestosSeleccionados, motivosSupuestos)) {
                    JOptionPane.showMessageDialog(CargarOfertaDialog.this, "Offer file loaded successfully.", "Load successful", JOptionPane.INFORMATION_MESSAGE);
                    llenarTablaDetalles();
                    if (getParent() instanceof MainWindow) {
                        ((MainWindow) getParent()).actualizarTablas();
                    }
                } else {
                }
            }
        });

        cerrarButton.addActionListener(e -> dispose());

        add(panelPrincipal, BorderLayout.CENTER);

        llenarTablaDetalles();
        actualizarTituloTabla();

        pack();
        setLocationRelativeTo(parent);
    }

    //---------------------------------------------------------
    // MÉTODOS AUXILIARES
    //---------------------------------------------------------
    /**
     * Genera el prefijo de clave para almacenar el archivo, basado en el lote
     * seleccionado.
     * <p>
     * Devuelve una cadena vacía ("") si es Oferta Única (Lote 0), o "LoteN_" si
     * es un lote N.
     * </p>
     *
     * @return El prefijo de clave del lote, o cadena vacía si no hay lote.
     */
    private String getLoteKeyPrefix() {
        if (this.numeroLote == 0) {
            return ""; // Caso Oferta Única / Lote 0 devuelve cadena vacía
        }

        // Usa el numeroLote para construir el prefijo
        return "Lote" + this.numeroLote + "_";
    }

    /**
     * Llena la tabla {@link #detalleTable} con el estado actual de los archivos
     * de oferta para el lote {@link #numeroLote} seleccionado.
     * <p>
     * Recupera el estado de cada archivo de
     * {@link FileManager#getArchivosOferta()}.
     * </p>
     */
    private void llenarTablaDetalles() {
        tableModel.setRowCount(0);

        String loteKeyPrefix = getLoteKeyPrefix();

        for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
            String fullKey = loteKeyPrefix + ofertaConfig.getNombre();
            FileData archivoCargado = fileManager.getArchivosOferta().get(fullKey);

            String estado = (archivoCargado != null) ? "Cargado" : "No cargado";
            String esObligatorio = ofertaConfig.esObligatorio() ? "Sí" : "No";
            String esConfidencial = (archivoCargado != null && archivoCargado.esConfidencial()) ? "Sí" : "No";

            String supuestos = (archivoCargado != null && archivoCargado.esConfidencial() && archivoCargado.getMotivosSupuestos() != null)
                    ? String.join(", ", archivoCargado.getMotivosSupuestos()) : "N/A";

            tableModel.addRow(new Object[]{
                ofertaConfig.getNombre(), esObligatorio, esConfidencial, estado, supuestos
            });
        }
    }

    /**
     * Actualiza el texto de la etiqueta {@link #detallesLabel} para reflejar el
     * nombre del lote actual.
     */
    private void actualizarTituloTabla() {
        if (this.numeroLote > 0 && this.mostrarLoteComboBox && loteComboBox != null && loteComboBox.getSelectedItem() != null) {
            String loteSeleccionado = (String) loteComboBox.getSelectedItem();
            detallesLabel.setText("Archivos cargados en el lote " + loteSeleccionado + ":");
        } else {
            detallesLabel.setText("Archivos cargados (Oferta Única):");
        }
    }

    /**
     * Clase interna para renderizar la columna "Estado" de la tabla, asignando
     * un color de fondo diferente (verde/rosa) según si el archivo está
     * "Cargado" o no.
     */
    private class EstadoCellRenderer extends DefaultTableCellRenderer {

        /**
         * Sobrescribe el método para aplicar color de fondo a la celda según el
         * estado.
         *
         * @param table La tabla.
         * @param value El valor de la celda (el estado).
         * @param isSelected Si la celda está seleccionada.
         * @param hasFocus Si la celda tiene el foco.
         * @param row La fila de la celda.
         * @param column La columna de la celda.
         * @return El componente de renderizado con el color aplicado.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = (String) value;
            Color colorFondo;

            if (estado.equals("Cargado")) {
                colorFondo = new Color(144, 238, 144); // Verde claro
            } else {
                colorFondo = new Color(255, 182, 193); // Rosa claro
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
}
