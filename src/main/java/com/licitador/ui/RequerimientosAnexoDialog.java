package com.licitador.ui;

import com.licitador.jar.model.RequerimientoLicitador;
import com.licitador.model.ArticuloAnexo;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import javax.swing.border.EmptyBorder; // Importación necesaria

public class RequerimientosAnexoDialog extends JDialog {

    private final List<RequerimientoLicitador> requerimientos;
    private final String textoDeclarativo; // NUEVO CAMPO
    private boolean aceptado = false;
    
    private JTabbedPane tabbedPane;
    // Mapa para guardar la referencia a los componentes de entrada (JTextField[], JCheckBox)
    private Map<RequerimientoLicitador, Component[]> componentesMap = new HashMap<>(); 

    /**
     * Constructor del diálogo de requisitos.
     * @param owner El Frame padre (MainWindow).
     * @param requerimientos La lista de RequerimientoLicitador a cumplimentar.
     * @param textoDeclarativo El HTML de los artículos de solo lectura.
     */
    public RequerimientosAnexoDialog(Frame owner, List<RequerimientoLicitador> requerimientos, String textoDeclarativo) {
        super(owner, "Cumplimentar Requisitos Interactivos del Anexo", true);
        this.requerimientos = requerimientos;
        this.textoDeclarativo = (textoDeclarativo == null || textoDeclarativo.isEmpty()) ? "<html><p>No hay artículos declarativos requeridos.</p></html>" : textoDeclarativo;
        
        initComponents();
        setSize(750, 600);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        tabbedPane = new JTabbedPane();

        // --- PESTAÑA 1: LECTURA (NUEVA) ---
        JTextPane textPaneLectura = new JTextPane();
        textPaneLectura.setContentType("text/html");
        textPaneLectura.setText(textoDeclarativo);
        textPaneLectura.setEditable(false);
        textPaneLectura.setCaretPosition(0); // Scroll al inicio
        tabbedPane.addTab("Lectura Obligatoria (Artículos Declarativos)", new JScrollPane(textPaneLectura));
        // ------------------------------------

        // Pestañas 2 en adelante: Requerimientos Interactivos
        for (int i = 0; i < requerimientos.size(); i++) {
            RequerimientoLicitador req = requerimientos.get(i);
            JPanel panelRequerimiento = buildRequerimientoPanel(req);
            tabbedPane.addTab("Requerimiento " + (i + 1) + ": " + req.getTituloArticulo(), panelRequerimiento); 
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Panel de Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Confirmar Requisitos y Continuar");
        JButton btnCancelar = new JButton("Cancelar Adhesión");
        
        btnAceptar.addActionListener(e -> intentarAceptar());
        btnCancelar.addActionListener(e -> dispose());

        btnPanel.add(btnAceptar);
        btnPanel.add(btnCancelar);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private JPanel buildRequerimientoPanel(RequerimientoLicitador req) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextPane txtPregunta = new JTextPane();
        txtPregunta.setContentType("text/html");
        txtPregunta.setText("<html><h3>" + req.getTituloArticulo() + "</h3><p><b>Pregunta:</b> " + req.getPregunta() + "</p></html>");
        txtPregunta.setEditable(false);
        panel.add(txtPregunta, BorderLayout.NORTH);

        JCheckBox chkRespuesta = new JCheckBox("Respuesta: Acepto/Confirmo (Sí)");
        JPanel pnlCondicional = new JPanel(new GridBagLayout());
        pnlCondicional.setBorder(BorderFactory.createTitledBorder("Datos Requeridos al Confirmar (Acción: " + req.getAccionSi() + ")"));
        pnlCondicional.setVisible(false);
        
        Component[] componentesAccion = null;

        if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(req.getAccionSi())) {
            componentesAccion = buildCamposPanel(pnlCondicional, req);
        } else if (ArticuloAnexo.ACCION_PEDIR_FICHERO.equals(req.getAccionSi())) {
            componentesAccion = buildFicheroPanel(pnlCondicional, req);
        } else {
             pnlCondicional.add(new JLabel("No se requiere ninguna acción adicional."));
        }
        
        componentesMap.put(req, componentesAccion);
        chkRespuesta.putClientProperty("REQ_OBJECT", req); 

        chkRespuesta.addActionListener(e -> {
            pnlCondicional.setVisible(chkRespuesta.isSelected() && !req.getAccionSi().equals(ArticuloAnexo.ACCION_NINGUNA));
            panel.revalidate();
            panel.repaint();
        });
        
        JPanel pnlCentro = new JPanel(new BorderLayout(5, 10));
        pnlCentro.add(chkRespuesta, BorderLayout.NORTH);
        pnlCentro.add(pnlCondicional, BorderLayout.CENTER);
        
        panel.add(pnlCentro, BorderLayout.CENTER);
        return panel;
    }
    
    // Devuelve los JTextFields creados
    private Component[] buildCamposPanel(JPanel container, RequerimientoLicitador req) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        String[] etiquetas = req.getEtiquetasCampos();
        JTextField[] textFields = new JTextField[etiquetas.length];
        
        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            container.add(new JLabel(etiquetas[i] + ":"), gbc);
            
            gbc.gridx = 1; gbc.weightx = 1;
            JTextField txtCampo = new JTextField(30);
            textFields[i] = txtCampo;
            container.add(txtCampo, gbc);
        }
        return textFields;
    }
    
    // Devuelve el JTextField de la ruta
    private Component[] buildFicheroPanel(JPanel container, RequerimientoLicitador req) {
        JTextField txtRuta = new JTextField(40);
        txtRuta.setEditable(false);
        JButton btnBuscar = new JButton("Seleccionar Fichero...");
        
        btnBuscar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY); 
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtRuta.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        
        container.setLayout(new FlowLayout(FlowLayout.LEFT));
        container.add(new JLabel("Fichero requerido:"));
        container.add(txtRuta);
        container.add(btnBuscar);
        
        return new Component[]{txtRuta};
    }

    private void intentarAceptar() {
        if (validarYTransferirDatos()) {
            this.aceptado = true;
            dispose();
        }
    }
    
    private boolean validarYTransferirDatos() {
        // Itera sobre las pestañas (saltando la pestaña 0, que es la de "Lectura")
        for (int i = 1; i < tabbedPane.getTabCount(); i++) {
            RequerimientoLicitador req = requerimientos.get(i-1); // Ajuste de índice
            
            JPanel panel = (JPanel) tabbedPane.getComponentAt(i);
            JPanel pnlCentro = (JPanel) panel.getComponent(1);
            JCheckBox chkRespuesta = (JCheckBox) pnlCentro.getComponent(0); 

            req.setRespuestaSi(chkRespuesta.isSelected());
            
            if (req.isRespuestaSi()) {
                Component[] componentes = componentesMap.get(req);
                
                if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(req.getAccionSi())) {
                    Map<String, String> valores = new HashMap<>();
                    JTextField[] textFields = (JTextField[]) componentes;
                    
                    for (int j = 0; j < req.getEtiquetasCampos().length; j++) {
                        if (textFields[j].getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(this, "Debe rellenar el campo '" + req.getEtiquetasCampos()[j] + "' en el Artículo '" + req.getTituloArticulo() + "'.", "Validación Requerida", JOptionPane.ERROR_MESSAGE);
                            tabbedPane.setSelectedIndex(i);
                            return false;
                        }
                        valores.put(req.getEtiquetasCampos()[j], textFields[j].getText().trim());
                    }
                    req.setValoresCampos(valores);
                } 
                else if (ArticuloAnexo.ACCION_PEDIR_FICHERO.equals(req.getAccionSi())) {
                    JTextField txtRuta = (JTextField) componentes[0];
                    if (txtRuta.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Debe seleccionar un fichero requerido en el artículo '" + req.getTituloArticulo() + "'.", "Validación Requerida", JOptionPane.ERROR_MESSAGE);
                        tabbedPane.setSelectedIndex(i);
                        return false;
                    }
                    req.setRutaFichero(txtRuta.getText().trim());
                }
            }
        }
        return true;
    }

    // --- Getters para MainWindow ---
    public boolean isAceptado() {
        return aceptado;
    }

    public List<RequerimientoLicitador> getRespuestasFinales() {
        return requerimientos;
    }
}