package com.licitador.configurator;

import com.licitador.model.AnexoAdministrativo;
import com.licitador.service.AnexoAdministrativoService;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class AnexoManagerDialog extends JDialog {

    private final AnexoAdministrativoService anexoService;
    private List<AnexoAdministrativo> anexos;
    private JList<AnexoAdministrativo> listaAnexos;
    private DefaultListModel<AnexoAdministrativo> listModel;

    // Componentes del formulario de edición
    private JTextField txtId;
    private JTextField txtTitulo;
    private JTextArea txtPlantillaXML;
    private JCheckBox chkRequiereDatos;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;

    /**
     * Constructor del diálogo de gestión de anexos.
     * @param owner La ventana principal del ConfiguradorApp.
     */
    public AnexoManagerDialog(Frame owner) {
        super(owner, "Gestor de Anexos Administrativos", true); // Diálogo modal
        this.anexoService = new AnexoAdministrativoService();
        this.anexos = anexoService.cargarAnexos();
        
        initComponents();
        loadListModel();
        
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Izquierdo: Lista de Anexos ---
        listModel = new DefaultListModel<>();
        listaAnexos = new JList<>(listModel);
        listaAnexos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAnexos.setCellRenderer(new AnexoListCellRenderer()); // Para mostrar el título
        
        listaAnexos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaAnexos.getSelectedIndex() != -1) {
                displayAnexo(listaAnexos.getSelectedValue());
            }
        });
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Anexos Existentes"));
        leftPanel.add(new JScrollPane(listaAnexos), BorderLayout.CENTER);
        
        // --- Panel Central: Formulario de Edición ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Anexo"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fila 1: ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("ID Único:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtId = new JTextField(20);
        formPanel.add(txtId, gbc);

        // Fila 2: Título
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTitulo = new JTextField(20);
        formPanel.add(txtTitulo, gbc);
        
        // Fila 3: Requiere Datos Adicionales
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Datos Adicionales:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        chkRequiereDatos = new JCheckBox("Marcar si el licitador debe rellenar datos extra.");
        formPanel.add(chkRequiereDatos, gbc);

        // Fila 4: Plantilla XML
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Plantilla XML:"), gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; 
        gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH; // El TextArea ocupa el resto del espacio
        txtPlantillaXML = new JTextArea(10, 50);
        txtPlantillaXML.setFont(new Font("Monospaced", Font.PLAIN, 12));
        formPanel.add(new JScrollPane(txtPlantillaXML), gbc);
        
        // --- Panel Sur: Botones de Acción ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        JButton btnCerrar = new JButton("Cerrar");

        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Espaciador
        buttonPanel.add(btnCerrar);

        // --- Asignar Listeners ---
        btnNuevo.addActionListener(e -> nuevoAnexo());
        btnGuardar.addActionListener(e -> guardarAnexo());
        btnEliminar.addActionListener(e -> eliminarAnexo());
        btnCerrar.addActionListener(e -> dispose());

        // --- Ensamblaje Final ---
        add(leftPanel, BorderLayout.WEST);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        clearForm(); // Limpia el formulario al inicio
    }
    
    /** Carga la lista de anexos en el JList */
    private void loadListModel() {
        listModel.clear();
        anexos.forEach(listModel::addElement);
    }
    
    /** Muestra los datos del anexo seleccionado en el formulario. */
    private void displayAnexo(AnexoAdministrativo anexo) {
        if (anexo == null) {
            clearForm();
            return;
        }
        txtId.setText(anexo.getId());
        txtId.setEnabled(false); // No se puede editar el ID de un anexo existente
        txtTitulo.setText(anexo.getTitulo());
        txtPlantillaXML.setText(anexo.getPlantillaXML());
        chkRequiereDatos.setSelected(anexo.getRequiereDatosAdicionales());
        btnEliminar.setEnabled(true);
        btnGuardar.setEnabled(true);
    }
    
    /** Limpia el formulario para crear un nuevo anexo. */
    private void nuevoAnexo() {
        clearForm();
        txtId.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(true);
        txtId.requestFocus();
    }
    
    /** Reinicia todos los campos del formulario. */
    private void clearForm() {
        txtId.setText("");
        txtTitulo.setText("");
        txtPlantillaXML.setText("");
        chkRequiereDatos.setSelected(false);
        txtId.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(false);
        listaAnexos.clearSelection();
    }

    /** Guarda o actualiza un anexo. */
    private void guardarAnexo() {
        String id = txtId.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String plantilla = txtPlantillaXML.getText();
        boolean requiereDatos = chkRequiereDatos.isSelected();
        
        if (id.isEmpty() || titulo.isEmpty() || plantilla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar ID, Título y Plantilla XML.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Comprueba si ya existe un anexo con este ID (para edición o creación)
        AnexoAdministrativo anexoExistente = anexos.stream()
                .filter(a -> a.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
        
        if (anexoExistente == null) {
            // Creación: Verifica duplicados
            anexos.add(new AnexoAdministrativo(id, titulo, plantilla, requiereDatos));
        } else {
            // Edición
            anexoExistente.setTitulo(titulo);
            anexoExistente.setPlantillaXML(plantilla);
            anexoExistente.setRequiereDatosAdicionales(requiereDatos);
        }
        
        // Persistencia y recarga
        anexoService.guardarAnexos(anexos);
        loadListModel();
        
        JOptionPane.showMessageDialog(this, "Anexo '" + titulo + "' guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }
    
    /** Elimina el anexo actualmente seleccionado. */
    private void eliminarAnexo() {
        AnexoAdministrativo anexoSeleccionado = listaAnexos.getSelectedValue();
        if (anexoSeleccionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar el anexo '" + anexoSeleccionado.getTitulo() + "'?", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            anexos.remove(anexoSeleccionado);
            anexoService.guardarAnexos(anexos);
            loadListModel();
            clearForm();
            JOptionPane.showMessageDialog(this, "Anexo eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Clase interna para renderizar el título del anexo en la JList
    public static class AnexoListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AnexoAdministrativo) {
                AnexoAdministrativo anexo = (AnexoAdministrativo) value;
                setText(anexo.getTitulo() + " (" + anexo.getId() + ")");
            }
            return this;
        }
    }
}