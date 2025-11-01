package com.licitador.ui;

import com.licitador.model.LicitadorData;
import com.licitador.service.FileManager;
import com.licitador.service.Logger;
import com.licitador.service.Configuracion; // La definiste aquí

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A dialog for filling in the administrative data of the bidder,
 * including the selection of participation in lots.
 */
public class AdministrativoCumplimentacionDialog extends JDialog {

    private final FileManager fileManager;
    private final LicitadorData licitadorData;
    private final Configuracion configuracion;
    private final Logger logger;

    private JTextField txtRazonSocial;
    private JTextField txtNif;
    private JTextField txtDomicilio;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JCheckBox chkEsPyme;
    private JCheckBox chkEsExtranjera;
    private Map<Integer, JCheckBox> loteCheckboxes;

    /**
     * Constructs a new AdministrativoCumplimentacionDialog.
     *
     * @param owner The parent frame.
     * @param fileManager The file manager.
     * @param logger The logger.
     */
    public AdministrativoCumplimentacionDialog(JFrame owner, FileManager fileManager, Logger logger) {
        super(owner, "Administrative Data and Lots", true);
        this.fileManager = fileManager;
        this.licitadorData = fileManager.getLicitadorData();
        this.configuracion = fileManager.getConfiguracion();
        this.logger = logger;
        
        initComponents();
        loadLicitadorData();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel Norte: Datos del Licitador
        JPanel licitadorPanel = createLicitadorPanel();
        add(licitadorPanel, BorderLayout.NORTH);

        // Panel Centro: Lotes (si aplica)
        if (configuracion.isTieneLotes()) {
            JPanel lotePanel = createLotesPanel();
            add(lotePanel, BorderLayout.CENTER);
        } else {
             // Si no hay lotes, se pone un mensaje simple en el centro
            JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
            center.add(new JLabel("Licitación de Oferta Única. No requiere selección de lotes."));
            add(center, BorderLayout.CENTER);
        }

        // Panel Sur: Botones de Acción
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createLicitadorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Licitador"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Ayudante para añadir campos al panel
        int row = 0;
        row = addField(panel, gbc, row, "Razón Social:", txtRazonSocial = new JTextField(30));
        row = addField(panel, gbc, row, "NIF/CIF:", txtNif = new JTextField(15));
        row = addField(panel, gbc, row, "Domicilio:", txtDomicilio = new JTextField(30));
        row = addField(panel, gbc, row, "Teléfono:", txtTelefono = new JTextField(15));
        row = addField(panel, gbc, row, "Email:", txtEmail = new JTextField(30));

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
        panel.setBorder(BorderFactory.createTitledBorder("Selección de Lotes"));

        int numLotes = configuracion.getNumLotes();
        int cols = (numLotes <= 4) ? 1 : (numLotes <= 8) ? 2 : 3;
        
        JPanel gridPanel = new JPanel(new GridLayout(0, cols, 10, 10));
        loteCheckboxes = new HashMap<>();

        for (int i = 1; i <= numLotes; i++) {
            JCheckBox chk = new JCheckBox("Lote " + i);
            gridPanel.add(chk);
            loteCheckboxes.put(i, chk);
        }

        panel.add(gridPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerar = new JButton("Generar Anexo y Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGenerar.addActionListener(e -> {
            if (saveLicitadorData()) {
                if (updateLoteParticipation()) { // 1. Actualiza el mapa de lotes en el FileManager
                    if (callGeneratePDF()) { // 2. Genera el PDF (Anexo) y lo carga
                        JOptionPane.showMessageDialog(this, "Datos guardados y Anexo Administrativo generado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                }
            }
        });
        
        btnCancelar.addActionListener(e -> dispose());
        
        panel.add(btnCancelar);
        panel.add(btnGenerar);
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
        
        // Cargar estado de lotes
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
        
        // El FileManager recibirá el objeto LicitadorData actualizado por referencia,
        // pero se llama al setter explícitamente para asegurar
        fileManager.setLicitadorData(licitadorData);
        logger.logInfo("Datos del licitador actualizados.");
        return true;
    }
    
    /**
     * Sincroniza el estado de los checkboxes de lotes con el mapa interno del FileManager.
     * @return true siempre (se asume que la sincronización es exitosa).
     */
    private boolean updateLoteParticipation() {
        if (!configuracion.isTieneLotes()) {
            return true;
        }
        
        // Obtener los IDs de lote seleccionados de la UI
        Set<String> lotesSeleccionadosIds = loteCheckboxes.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(entry -> String.valueOf(entry.getKey()))
                .collect(Collectors.toSet());
        
        // 1. Sincronizar el estado de participación en el FileManager
        fileManager.setParticipacionDesdeUI(lotesSeleccionadosIds);
        
        // 2. Eliminar archivos de oferta cargados para lotes que ya no participan
        // Esto se debe hacer *después* de setParticipacionDesdeUI, que ya limpió participacionPorLote.
        // Ahora necesitamos comparar la participación anterior con la nueva.
        
        // NOTA: Implementación simple: si un lote cargado previamente ahora no está seleccionado,
        // eliminamos los archivos de oferta.
        for (int i = 1; i <= configuracion.getNumLotes(); i++) {
            if (!lotesSeleccionadosIds.contains(String.valueOf(i))) {
                String idLote = "Lote " + i;
                if (fileManager.eliminarArchivosOfertaPorLote(idLote)) {
                    logger.logInfo("Archivos de oferta eliminados para " + idLote + " (ya no participa).");
                }
            }
        }
        
        // Validar participación mínima (si aplica)
        if (!fileManager.validarMinimoParticipacion()) {
             // NO debe salir de aquí si es inválido, sino que debe dar un aviso
             // y permitir al usuario corregir. Dejamos que el usuario cierre y el 
             // FileManager valide de nuevo antes de comprimir.
             JOptionPane.showMessageDialog(this, "Advertencia: En licitaciones con lotes, debe seleccionar al menos un lote para participar.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
        
        return true;
    }
    
    /**
     * Llama al método del FileManager para generar el PDF y cargarlo en el sistema.
     * @return true si la generación fue exitosa.
     */
    private boolean callGeneratePDF() {
        // En este punto, los datos del licitador y la participación de lotes ya están en el FileManager.
        return fileManager.generarAnexoAdministrativoYGuardar();
    }
}