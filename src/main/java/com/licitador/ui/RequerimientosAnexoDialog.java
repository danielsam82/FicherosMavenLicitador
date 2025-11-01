package com.licitador.ui;

import com.licitador.jar.model.RequerimientoLicitador;
import com.licitador.model.ArticuloAnexo;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File; // Necesario para el JFileChooser

/**
 * A dialog for fulfilling the interactive requirements of the annex.
 */
public class RequerimientosAnexoDialog extends JDialog {

    private final List<RequerimientoLicitador> requerimientos;
    private boolean aceptado = false;
    
    private JTabbedPane tabbedPane;

    /**
     * Constructs a new RequerimientosAnexoDialog.
     * @param owner The parent frame (MainWindow).
     * @param requerimientos The list of RequerimientoLicitador to be filled out.
     */
    public RequerimientosAnexoDialog(Frame owner, List<RequerimientoLicitador> requerimientos) {
        super(owner, "Fulfill Interactive Annex Requirements", true);
        this.requerimientos = requerimientos;
        
        initComponents();
        setSize(700, 550);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        tabbedPane = new JTabbedPane();
        
        // Crear una pestaña (panel) para cada requerimiento interactivo
        for (int i = 0; i < requerimientos.size(); i++) {
            RequerimientoLicitador req = requerimientos.get(i);
            JPanel panelRequerimiento = buildRequerimientoPanel(req);
            // Título de la pestaña: Ej. "Art. 1: Declaración Solvencia"
            tabbedPane.addTab("Art. " + (i + 1) + ": " + req.getTituloArticulo(), panelRequerimiento); 
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Panel de Botones inferior
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Confirmar Requisitos y Continuar");
        JButton btnCancelar = new JButton("Cancelar Adhesión");
        
        btnAceptar.addActionListener(e -> intentarAceptar());
        btnCancelar.addActionListener(e -> dispose()); // Solo cierra el diálogo, mantiene la posibilidad de reabrir

        btnPanel.add(btnAceptar);
        btnPanel.add(btnCancelar);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    // --------------------------------------------------------------------------
    // Métodos para construir los Paneles de Interacción
    // --------------------------------------------------------------------------

    private JPanel buildRequerimientoPanel(RequerimientoLicitador req) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Título y Pregunta (Zona NORTE)
        JTextPane txtPregunta = new JTextPane();
        txtPregunta.setContentType("text/html");
        txtPregunta.setText("<html><h3>" + req.getTituloArticulo() + "</h3><p><b>Pregunta:</b> " + req.getPregunta() + "</p></html>");
        txtPregunta.setEditable(false);
        panel.add(txtPregunta, BorderLayout.NORTH);

        // 2. Controles de Respuesta (Sí/No) y Contenido Condicional (Zona CENTRO)
        
        // Checkbox principal para la respuesta SI/NO
        JCheckBox chkRespuesta = new JCheckBox("Respuesta: Acepto/Confirmo las condiciones del artículo");
        
        // Panel que contendrá los campos de texto o el botón de fichero
        JPanel pnlCondicional = new JPanel(new GridBagLayout());
        pnlCondicional.setBorder(BorderFactory.createTitledBorder("Datos Requeridos al Aceptar"));
        pnlCondicional.setVisible(false); // Inicialmente oculto
        
        // Usamos ClientProperty para adjuntar el tipo de acción y el modelo
        chkRespuesta.putClientProperty("REQ_OBJECT", req);
        chkRespuesta.putClientProperty("TIPO_ACCION", req.getAccionSi());

        // Construir los componentes condicionales
        if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(req.getAccionSi())) {
            buildCamposPanel(pnlCondicional, req);
        } else if (ArticuloAnexo.ACCION_PEDIR_FICHERO.equals(req.getAccionSi())) {
            buildFicheroPanel(pnlCondicional, req);
        }
        
        // Listener para habilitar/deshabilitar la sección condicional
        chkRespuesta.addActionListener(e -> {
            pnlCondicional.setVisible(chkRespuesta.isSelected());
            panel.revalidate();
            panel.repaint();
        });
        
        // Ensamblaje final de la pestaña
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(chkRespuesta, BorderLayout.NORTH);
        pnlCentro.add(pnlCondicional, BorderLayout.CENTER);
        
        panel.add(pnlCentro, BorderLayout.CENTER);
        return panel;
    }
    
    private void buildCamposPanel(JPanel container, RequerimientoLicitador req) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        Map<String, JTextField> campos = new HashMap<>();
        String[] etiquetas = req.getEtiquetasCampos();
        
        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            container.add(new JLabel(etiquetas[i] + ":"), gbc);
            
            gbc.gridx = 1; gbc.weightx = 1;
            JTextField txtCampo = new JTextField(30);
            campos.put(etiquetas[i], txtCampo);
            container.add(txtCampo, gbc);
        }
        
        // Adjuntar el Map de componentes al panel para recuperarlo luego en la validación
        container.putClientProperty("CAMPOS_MAP", campos);
    }
    
    private void buildFicheroPanel(JPanel container, RequerimientoLicitador req) {
        JTextField txtRuta = new JTextField(40);
        txtRuta.setEditable(false);
        JButton btnBuscar = new JButton("Seleccionar Fichero...");
        
        btnBuscar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            // Asegurarse de que solo se puedan seleccionar archivos, no directorios
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY); 
            
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                txtRuta.setText(file.getAbsolutePath());
            }
        });
        
        container.setLayout(new FlowLayout(FlowLayout.LEFT));
        container.add(new JLabel("Ruta del Fichero requerido:"));
        container.add(txtRuta);
        container.add(btnBuscar);
        
        // Adjuntar el campo de texto al panel para la validación
        container.putClientProperty("RUTA_FICHERO_FIELD", txtRuta);
    }

    // --------------------------------------------------------------------------
    // Lógica de Aceptación y Validación
    // --------------------------------------------------------------------------

    private void intentarAceptar() {
        if (validarYTransferirDatos()) {
            this.aceptado = true;
            dispose(); // Cierra el diálogo y retorna el control a MainWindow
        }
    }
    
    /**
     * Itera sobre todas las pestañas, valida la información y transfiere
     * los datos de la GUI al modelo RequerimientoLicitador.
     * @return true si la validación es exitosa.
     */
    private boolean validarYTransferirDatos() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            // Navegación: Pestaña -> Panel Centro -> Componente Checkbox/Condicional
            JPanel panel = (JPanel) tabbedPane.getComponentAt(i);
            JPanel pnlCentro = (JPanel) panel.getComponent(1);
            
            JCheckBox chkRespuesta = (JCheckBox) pnlCentro.getComponent(0); 
            RequerimientoLicitador req = (RequerimientoLicitador) chkRespuesta.getClientProperty("REQ_OBJECT");
            JPanel pnlCondicional = (JPanel) pnlCentro.getComponent(1);

            // 1. Transferir Respuesta Sí/No al modelo
            req.setRespuestaSi(chkRespuesta.isSelected());
            
            // 2. Si la respuesta es SÍ, validar y transferir la acción condicional
            if (req.isRespuestaSi()) {
                String tipoAccion = (String) chkRespuesta.getClientProperty("TIPO_ACCION");

                if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(tipoAccion)) {
                    // Acción: PEDIR_CAMPOS
                    Map<String, JTextField> campos = (Map<String, JTextField>) pnlCondicional.getClientProperty("CAMPOS_MAP");
                    Map<String, String> valores = new HashMap<>();
                    
                    // Validación de campos no vacíos
                    for (Map.Entry<String, JTextField> entry : campos.entrySet()) {
                        if (entry.getValue().getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(this, "Debe rellenar el campo '" + entry.getKey() + "' en el Artículo " + (i+1) + ".", "Validación Requerida", JOptionPane.ERROR_MESSAGE);
                            tabbedPane.setSelectedIndex(i); // Vuelve a la pestaña del error
                            return false;
                        }
                        valores.put(entry.getKey(), entry.getValue().getText().trim());
                    }
                    req.setValoresCampos(valores);
                } 
                else if (ArticuloAnexo.ACCION_PEDIR_FICHERO.equals(tipoAccion)) {
                    // Acción: PEDIR_FICHERO
                    JTextField txtRuta = (JTextField) pnlCondicional.getClientProperty("RUTA_FICHERO_FIELD");
                    if (txtRuta.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Debe seleccionar un fichero requerido en el Artículo " + (i+1) + ".", "Validación Requerida", JOptionPane.ERROR_MESSAGE);
                        tabbedPane.setSelectedIndex(i); // Vuelve a la pestaña del error
                        return false;
                    }
                    req.setRutaFichero(txtRuta.getText().trim());
                }
            }
        }
        return true; // Todos los requisitos cumplimentados y validados
    }

    // --------------------------------------------------------------------------
    /**
     * Checks if the user accepted the dialog.
     * @return true if the user clicked "Confirmar Requisitos y Continuar" and the data was validated.
     */
    public boolean isAceptado() {
        return aceptado;
    }

    /**
     * Gets the final answers from the user.
     * @return The list of RequerimientoLicitador objects with the user's answers.
     */
    public List<RequerimientoLicitador> getRespuestasFinales() {
        return requerimientos;
    }
}