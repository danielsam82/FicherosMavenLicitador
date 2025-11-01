// Archivo: src/main/java/com/licitador/ui/ApoderadoDialog.java
package com.licitador.ui;

import com.licitador.model.LicitadorData;
import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para solicitar los datos del Apoderado (representante legal)
 * antes de generar el Anexo Administrativo.
 */
public class ApoderadoDialog extends JDialog {

    private final LicitadorData licitadorData;
    private boolean aceptado = false;

    private JTextField txtNombreApoderado;
    private JTextField txtNifApoderado;
    private JTextField txtCalidadApoderado;

    public ApoderadoDialog(Frame owner, LicitadorData licitadorData) {
        super(owner, "Datos del Representante Legal (Apoderado)", true);
        this.licitadorData = licitadorData;
        
        initComponents();
        
        // Precargar datos si ya existían (ej. de una sesión cargada)
        txtNombreApoderado.setText(licitadorData.getNombreApoderado());
        txtNifApoderado.setText(licitadorData.getNifApoderado());
        txtCalidadApoderado.setText(licitadorData.getCalidadApoderado());
        
        setSize(500, 250);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Nombre Apoderado
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelForm.add(new JLabel("Nombre Completo (Apoderado)*:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNombreApoderado = new JTextField(30);
        panelForm.add(txtNombreApoderado, gbc);

        // Fila 2: NIF Apoderado
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelForm.add(new JLabel("NIF (Apoderado)*:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNifApoderado = new JTextField(15);
        panelForm.add(txtNifApoderado, gbc);

        // Fila 3: Calidad
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelForm.add(new JLabel("Actúa en calidad de*:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCalidadApoderado = new JTextField(30);
        panelForm.add(txtCalidadApoderado, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAceptar.addActionListener(e -> onAceptar());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);

        add(panelForm, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private void onAceptar() {
        // Validación
        if (txtNombreApoderado.getText().trim().isEmpty() || 
            txtNifApoderado.getText().trim().isEmpty() || 
            txtCalidadApoderado.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, "Debe rellenar todos los campos del representante.", "Campos Obligatorios", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Guardar datos en el objeto
        licitadorData.setNombreApoderado(txtNombreApoderado.getText().trim());
        licitadorData.setNifApoderado(txtNifApoderado.getText().trim());
        licitadorData.setCalidadApoderado(txtCalidadApoderado.getText().trim());
        
        this.aceptado = true;
        dispose();
    }
    
    public boolean isAceptado() {
        return aceptado;
    }
}