package com.licitador.configurator;

import com.licitador.model.ArticuloAnexo;
// Es necesario renombrar ArticuloAnexoService a ArticuloAnexoService
import com.licitador.service.ArticuloAnexoService; 
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors; // Necesario para ordenar la lista
import javax.swing.*;

public class ArticuloManagerDialog extends JDialog {

    // CAMBIO: Usar el servicio renombrado
    private final ArticuloAnexoService articuloService; 
    private List<ArticuloAnexo> articulos; // Lista ahora de ArticuloAnexo
    private JList<ArticuloAnexo> listaArticulos; // Componente JList renombrado
    private DefaultListModel<ArticuloAnexo> listModel;

    // Componentes del formulario de edición
    private JTextField txtId;
    private JTextField txtTitulo;
    private JTextField txtOrden; // NUEVO: Campo para el orden
    private JTextArea txtContenidoFormato; // CAMBIO: De XML a Contenido Formato
    private JCheckBox chkRequiereFirma; // CAMBIO: De Datos Adicionales a Requiere Firma
    private JComboBox<String> cmbTags; // NUEVO: Para inserción de etiquetas

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnInsertTag; // NUEVO: Botón para insertar etiquetas

    /**
     * Constructor del diálogo de gestión de Artículos de Anexos.
     */
    public ArticuloManagerDialog(Frame owner) {
        // CAMBIO: Título del diálogo
        super(owner, "Gestor de Artículos de Anexo Global", true); 
        // CAMBIO: Instanciar el servicio renombrado
        this.articuloService = new ArticuloAnexoService();
        this.articulos = articuloService.cargarArticulos(); // Cargar Artículos
        
        initComponents();
        loadListModel();
        
        setSize(900, 700);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Izquierdo: Lista de Artículos ---
        listModel = new DefaultListModel<>();
        listaArticulos = new JList<>(listModel);
        listaArticulos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // CAMBIO: Usar el CellRenderer renombrado
        listaArticulos.setCellRenderer(new ArticuloListCellRenderer()); 
        
        listaArticulos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaArticulos.getSelectedIndex() != -1) {
                displayArticulo(listaArticulos.getSelectedValue());
            }
        });
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Artículos Existentes (Maestro)"));
        leftPanel.add(new JScrollPane(listaArticulos), BorderLayout.CENTER);
        
        // --- Panel Central: Formulario de Edición ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Artículo"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fila 1: ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("ID Único:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtId = new JTextField(10);
        formPanel.add(txtId, gbc);

        // Fila 2: Orden
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Orden (1, 2, 3...):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtOrden = new JTextField(5);
        formPanel.add(txtOrden, gbc);
        
        // Fila 3: Título
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTitulo = new JTextField(20);
        formPanel.add(txtTitulo, gbc);
        
        // Fila 4: Requiere Firma
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1;
        chkRequiereFirma = new JCheckBox("Este artículo requiere una línea de firma específica.");
        formPanel.add(chkRequiereFirma, gbc);
        
        // Fila 5: Controles de Inserción de Etiquetas
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; 
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel xmlControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xmlControlsPanel.add(new JLabel("Insertar Dato Licitador/Expediente:"));
        
        String[] tags = {"NIF_CIF", "RAZON_SOCIAL", "NOMBRE_EMPRESA", "CARGO_REPRESENTANTE", "EXPEDIENTE", "OBJETO"};
        cmbTags = new JComboBox<>(tags);
        xmlControlsPanel.add(cmbTags);
        
        btnInsertTag = new JButton("Insertar Etiqueta");
        btnInsertTag.addActionListener(e -> insertTag());
        xmlControlsPanel.add(btnInsertTag);
        
        formPanel.add(xmlControlsPanel, gbc); 

        // Fila 6: Área de Contenido Formato
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH; 
        formPanel.add(new JLabel("Contenido (Texto con posible formato/tags):"), gbc);
        
        gbc.gridy = 6; gbc.weightx = 1; gbc.weighty = 5;
        txtContenidoFormato = new JTextArea(15, 60); // CAMBIO de nombre de variable
        txtContenidoFormato.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtContenidoFormato.setLineWrap(true);
        txtContenidoFormato.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtContenidoFormato), gbc);
        
        // --- Panel Sur: Botones de Acción ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        JButton btnCerrar = new JButton("Cerrar");

        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnCerrar);

        // --- Asignar Listeners ---
        btnNuevo.addActionListener(e -> nuevoArticulo());
        btnGuardar.addActionListener(e -> guardarArticulo());
        btnEliminar.addActionListener(e -> eliminarArticulo()); // CAMBIO de nombre de método
        btnCerrar.addActionListener(e -> dispose());

        // --- Ensamblaje Final ---
        add(leftPanel, BorderLayout.WEST);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        clearForm();
    }
    
    // --------------------------------------------------------------------------
    // --- LÓGICA DE GESTIÓN ---
    // --------------------------------------------------------------------------
    
    /** Carga la lista de artículos, la ordena y la muestra en el JList */
    private void loadListModel() {
        // Ordenamos la lista de artículos por el campo 'orden' para mejor visualización
        articulos.sort((a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));
        
        listModel.clear();
        articulos.forEach(listModel::addElement);
    }
    
    /** Muestra los datos del artículo seleccionado en el formulario. */
    private void displayArticulo(ArticuloAnexo articulo) {
        if (articulo == null) {
            clearForm();
            return;
        }
        // CAMBIOS DE GETTERS
        txtId.setText(articulo.getIdArticulo());
        txtId.setEnabled(false);
        txtOrden.setText(String.valueOf(articulo.getOrden())); // Nuevo campo
        txtTitulo.setText(articulo.getTitulo());
        txtContenidoFormato.setText(articulo.getContenidoFormato()); // Nuevo getter
        chkRequiereFirma.setSelected(articulo.isRequiereFirma()); // Nuevo getter
        
        btnEliminar.setEnabled(true);
        btnGuardar.setEnabled(true);
    }
    
    /** Limpia el formulario para crear un nuevo artículo. */
    private void nuevoArticulo() {
        clearForm();
        txtId.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(true);
        txtId.requestFocus();
    }
    
    /** Reinicia todos los campos del formulario. */
    private void clearForm() {
        txtId.setText("");
        txtOrden.setText("");
        txtTitulo.setText("");
        txtContenidoFormato.setText("");
        chkRequiereFirma.setSelected(false);
        txtId.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(false);
        listaArticulos.clearSelection();
    }

    /** Inserta la etiqueta XML de sustitución en la posición del cursor. */
    private void insertTag() {
        String selectedTag = (String) cmbTags.getSelectedItem();
        if (selectedTag != null) {
            String tagToInsert = "<DATO_LICITADOR ETQ=\"" + selectedTag + "\"/>";
            int caretPosition = txtContenidoFormato.getCaretPosition();
            txtContenidoFormato.insert(tagToInsert, caretPosition);
            txtContenidoFormato.setCaretPosition(caretPosition + tagToInsert.length());
            txtContenidoFormato.requestFocusInWindow();
        }
    }

    /** Guarda o actualiza un artículo. */
    private void guardarArticulo() {
        String id = txtId.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String contenido = txtContenidoFormato.getText();
        boolean requiereFirma = chkRequiereFirma.isSelected();
        int orden;
        
        try {
            orden = Integer.parseInt(txtOrden.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El campo 'Orden' debe ser un número entero válido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (id.isEmpty() || titulo.isEmpty() || contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar ID, Título y Contenido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Búsqueda de artículo por ID
        ArticuloAnexo articuloExistente = articulos.stream()
                .filter(a -> a.getIdArticulo().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
        
        if (articuloExistente == null) {
            // Creación
            // Uso del constructor actualizado
            ArticuloAnexo nuevo = new ArticuloAnexo(id, orden, titulo, contenido, requiereFirma); 
            articulos.add(nuevo);
        } else {
            // Edición
            articuloExistente.setOrden(orden);
            articuloExistente.setTitulo(titulo);
            articuloExistente.setContenidoFormato(contenido);
            articuloExistente.setRequiereFirma(requiereFirma);
        }
        
        // Persistencia y recarga
        articuloService.guardarArticulos(articulos); // CAMBIO de nombre de método en el servicio
        loadListModel();
        
        JOptionPane.showMessageDialog(this, "Artículo '" + titulo + "' guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }
    
    /** Elimina el artículo actualmente seleccionado. */
    private void eliminarArticulo() {
        ArticuloAnexo articuloSeleccionado = listaArticulos.getSelectedValue();
        if (articuloSeleccionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar el artículo '" + articuloSeleccionado.getTitulo() + "'?", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            articulos.remove(articuloSeleccionado);
            articuloService.guardarArticulos(articulos); // CAMBIO de nombre de método en el servicio
            loadListModel();
            clearForm();
            JOptionPane.showMessageDialog(this, "Artículo eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --------------------------------------------------------------------------
    // --- CLASE INTERNA: RENDERER (Renombrada) ---
    // --------------------------------------------------------------------------
    
    // CAMBIO: Renombrada a ArticuloListCellRenderer
    public static class ArticuloListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ArticuloAnexo) {
                ArticuloAnexo articulo = (ArticuloAnexo) value;
                // CAMBIO: Usa el toString() de ArticuloAnexo para mostrar (Orden) Título [ID]
                setText(articulo.toString());
            }
            return this;
        }
    }
}