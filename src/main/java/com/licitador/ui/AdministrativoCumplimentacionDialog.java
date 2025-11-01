package com.licitador.ui;

// Importaciones necesarias (asegúrate de que los paths son correctos)
import com.licitador.model.LicitadorData;
import com.licitador.service.FileManager;
import com.licitador.service.Logger;
import com.licitador.service.Configuracion; 

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importación añadida
import java.awt.*;
import java.awt.event.WindowAdapter; // Importación añadida
import java.awt.event.WindowEvent; // Importación añadida
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diálogo para la cumplimentación de los datos administrativos del licitador,
 * incluyendo la selección de participación en lotes.
 * (Esta clase NO genera el PDF, solo guarda los datos en el FileManager).
 */
public class AdministrativoCumplimentacionDialog extends JDialog {

    private final MainWindow parent; // Referencia a MainWindow
    private final FileManager fileManager;
    private final LicitadorData licitadorData;
    private final Configuracion configuracion;
    private final Logger logger;

    // Campos de entrada de datos del licitador
    private JTextField txtRazonSocial;
    private JTextField txtNif;
    private JTextField txtDomicilio;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JCheckBox chkEsPyme;
    private JCheckBox chkEsExtranjera;
    private Map<Integer, JCheckBox> loteCheckboxes; // Checkboxes para la participación en lotes
    
    private boolean configuracionAceptada = false; // Flag para MainWindow

    /**
     * Constructor del diálogo.
     * @param owner La ventana principal (MainWindow).
     * @param fileManager El gestor de archivos.
     * @param logger El logger.
     */
    public AdministrativoCumplimentacionDialog(MainWindow owner, FileManager fileManager, Logger logger) {
        super(owner, "Cumplimentación Administrativa y Lotes", true);
        this.parent = owner; 
        this.fileManager = fileManager;
        this.licitadorData = fileManager.getLicitadorData();
        this.configuracion = fileManager.getConfiguracion();
        this.logger = logger;

        initComponents();
        loadLicitadorData(); // Cargar datos existentes si los hay

        pack();
        setLocationRelativeTo(owner);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Panel de Datos del Licitador
        mainPanel.add(createLicitadorPanel(), BorderLayout.NORTH);

        // 2. Panel de Selección de Lotes (Solo si aplica)
        if (configuracion.isTieneLotes()) {
            JPanel lotePanel = createLotesPanel();
            mainPanel.add(lotePanel, BorderLayout.CENTER);
        } else {
             JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
             center.add(new JLabel("Licitación de Oferta Única. No requiere selección de lotes."));
             mainPanel.add(center, BorderLayout.CENTER);
        }

        // 3. Panel de Botones de Control
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createLicitadorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Licitador (* Obligatorio)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addField(panel, gbc, row, "Razón Social*:", txtRazonSocial = new JTextField(30));
        row = addField(panel, gbc, row, "NIF/CIF*:", txtNif = new JTextField(15));
        row = addField(panel, gbc, row, "Domicilio*:", txtDomicilio = new JTextField(30));
        row = addField(panel, gbc, row, "Teléfono*:", txtTelefono = new JTextField(15));
        row = addField(panel, gbc, row, "Email*:", txtEmail = new JTextField(30));

        // Checkboxes en una sola fila (Fila 5)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("PYME:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        chkEsPyme = new JCheckBox();
        panel.add(chkEsPyme, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Extranjera:"), gbc);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        chkEsExtranjera = new JCheckBox();
        panel.add(chkEsExtranjera, gbc);

        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(textField, gbc);
        return row + 1;
    }

    private JPanel createLotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selección de Lotes (Marque a cuáles desea ofertar)"));

        int numLotes = configuracion.getNumLotes();
        int cols = (numLotes <= 4) ? 1 : (numLotes <= 8) ? 2 : 3;

        JPanel gridPanel = new JPanel(new GridLayout(0, cols, 10, 10));
        loteCheckboxes = new HashMap<>();

        for (int i = 1; i <= numLotes; i++) {
            JCheckBox chk = new JCheckBox("Lote " + i);
            gridPanel.add(chk);
            loteCheckboxes.put(i, chk);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // CORRECCIÓN: El botón ahora solo Acepta y Guarda los datos del licitador/lotes.
        JButton btnAceptar = new JButton("Aceptar y Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> {
            // 1. Validar y Guardar los datos del licitador
            if (saveLicitadorData()) {
                // 2. Sincronizar la selección de lotes
                if (updateLoteParticipation()) {
                    
                    // 3. Marcar como aceptado y cerrar
                    configuracionAceptada = true; 
                    dispose();
                }
            }
        });

        btnCancelar.addActionListener(e -> dispose());

        panel.add(btnCancelar);
        panel.add(btnAceptar);
        return panel;
    }

    // --- LÓGICA DE DATOS ---
    private void loadLicitadorData() {
        txtRazonSocial.setText(licitadorData.getRazonSocial());
        txtNif.setText(licitadorData.getNif());
        txtDomicilio.setText(licitadorData.getDomicilio());
        txtTelefono.setText(licitadorData.getTelefono());
        txtEmail.setText(licitadorData.getEmail());
        chkEsPyme.setSelected(licitadorData.esPyme());
        chkEsExtranjera.setSelected(licitadorData.esExtranjera());

        if (configuracion.isTieneLotes()) {
            for (Map.Entry<Integer, JCheckBox> entry : loteCheckboxes.entrySet()) {
                boolean participa = fileManager.getParticipacionLote(entry.getKey());
                entry.getValue().setSelected(participa);
            }
        }
    }

    private boolean saveLicitadorData() {
        if (txtRazonSocial.getText().trim().isEmpty() || txtNif.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Los campos Razón Social y NIF/CIF son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        licitadorData.setRazonSocial(txtRazonSocial.getText().trim());
        licitadorData.setNif(txtNif.getText().trim());
        licitadorData.setDomicilio(txtDomicilio.getText().trim());
        licitadorData.setTelefono(txtTelefono.getText().trim());
        licitadorData.setEmail(txtEmail.getText().trim());
        licitadorData.setEsPyme(chkEsPyme.isSelected());
        licitadorData.setEsExtranjera(chkEsExtranjera.isSelected());

        fileManager.setLicitadorData(licitadorData);
        logger.logInfo("Datos del licitador actualizados.");
        return true;
    }

    private boolean updateLoteParticipation() {
        if (!configuracion.isTieneLotes()) {
            return true;
        }

        Set<String> lotesSeleccionadosIds = loteCheckboxes.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(entry -> String.valueOf(entry.getKey()))
                .collect(Collectors.toSet());

        fileManager.setParticipacionDesdeUI(lotesSeleccionadosIds);

        for (int i = 1; i <= configuracion.getNumLotes(); i++) {
            if (!lotesSeleccionadosIds.contains(String.valueOf(i))) {
                String idLote = "Lote " + i;
                if (fileManager.eliminarArchivosOfertaPorLote(idLote)) {
                    logger.logInfo("Archivos de oferta eliminados para " + idLote + " (ya no participa).");
                }
            }
        }

        if (!fileManager.validarMinimoParticipacion()) {
             JOptionPane.showMessageDialog(this, "Advertencia: En licitaciones con lotes, debe seleccionar al menos un lote para participar.", "Validación", JOptionPane.WARNING_MESSAGE);
             // No retornamos false, permitimos que el usuario acepte,
             // pero el FileManager fallará en la compresión final.
        }

        return true;
    }

    /**
     * Getter para que MainWindow sepa si el diálogo se cerró con Aceptar.
     * @return true si el usuario guardó los datos.
     */
    public boolean isConfiguracionAceptada() {
        return configuracionAceptada;
    }
}