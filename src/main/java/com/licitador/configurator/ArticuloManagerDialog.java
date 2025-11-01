package com.licitador.configurator;

import com.licitador.model.ArticuloAnexo;
import com.licitador.service.ArticuloAnexoService;
import java.awt.*;
import java.awt.event.ActionListener; // Asegúrate de que esta importación esté
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class ArticuloManagerDialog extends JDialog {

    // CAMBIO: Usar el servicio renombrado
    private final ArticuloAnexoService articuloService;
    private List<ArticuloAnexo> articulos;
    private JList<ArticuloAnexo> listaArticulos;
    private DefaultListModel<ArticuloAnexo> listModel;

    // Componentes del formulario de edición BASE
    private JTextField txtId;
    private JTextField txtTitulo;
    private JTextField txtOrden;
    private JTextArea txtContenidoFormato;
    private JCheckBox chkRequiereFirma;
    private JComboBox<String> cmbTags;

    // --- NUEVOS CAMPOS DE INTERACTIVIDAD ---
    private JCheckBox chkEsInteractivo;
    private JTextArea txtPreguntaInteractiva;
    private JComboBox<String> cmbAccionSi;
    private JTextField[] txtEtiquetasCampos = new JTextField[4];
    private JPanel pnlCamposEtiquetas; // Panel que contiene los 4 campos
    // Nuevo campo para el contenido de la respuesta "NO"
    private JTextArea txtContenidoFormatoRespuestaNo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnInsertTag;

    /**
     * Constructor del diálogo de gestión de Artículos de Anexos.
     */
    public ArticuloManagerDialog(Frame owner) {
        super(owner, "Gestor de Artículos de Anexo Global", true);
        this.articuloService = new ArticuloAnexoService();
        this.articulos = articuloService.cargarArticulos();

        initComponents();
        loadListModel();

        setSize(900, 750);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Izquierdo: Lista de Artículos ---
        listModel = new DefaultListModel<>();
        listaArticulos = new JList<>(listModel);
        listaArticulos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaArticulos.setCellRenderer(new ArticuloListCellRenderer());

        listaArticulos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaArticulos.getSelectedIndex() != -1) {
                displayArticulo(listaArticulos.getSelectedValue());
            } else if (listaArticulos.getSelectedIndex() == -1) {
                // Si la selección se limpia, limpiar y bloquear el formulario
                clearForm();
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

        // --- 🔥 CORRECCIÓN DE ORDEN DE INICIALIZACIÓN ---
        // Inicializamos los JTextArea aquí para que no sean nulos 
        // cuando 'createInteractividadPanel' (que llama a 'toggleInteractividad') los necesite.
        txtContenidoFormato = new JTextArea(10, 60);
        txtContenidoFormatoRespuestaNo = new JTextArea(5, 60);
        // -----------------------------------------------------------

        // Fila 1: ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("ID Único:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtId = new JTextField(10);
        formPanel.add(txtId, gbc);

        // Fila 2: Orden
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Orden (1, 2, 3...):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtOrden = new JTextField(5);
        formPanel.add(txtOrden, gbc);

        // Fila 3: Título
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtTitulo = new JTextField(20);
        formPanel.add(txtTitulo, gbc);

        // Fila 4: Requiere Firma
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        chkRequiereFirma = new JCheckBox("Este artículo requiere una línea de firma específica.");
        formPanel.add(chkRequiereFirma, gbc);

        // Fila 5: INTERACTIVIDAD (Ahora es seguro llamarlo)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(createInteractividadPanel(), gbc);

        // Fila 6: Controles de Inserción de Etiquetas
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel xmlControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xmlControlsPanel.add(new JLabel("Insertar Dato Licitador/Expediente:"));
        String[] tags = {"NIF_CIF", "RAZON_SOCIAL", "NOMBRE_EMPRESA", "CARGO_REPRESENTANTE", "EXPEDIENTE", "OBJETO"};
        cmbTags = new JComboBox<>(tags);
        xmlControlsPanel.add(cmbTags);
        btnInsertTag = new JButton("Insertar Etiqueta");
        btnInsertTag.addActionListener(e -> insertTag());
        xmlControlsPanel.add(btnInsertTag);
        formPanel.add(xmlControlsPanel, gbc);

        // Fila 7: Área de Contenido Formato (para "SÍ" o contenido normal)
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JLabel("Contenido (Texto si 'SÍ' o si no es interactivo):"), gbc);

        gbc.gridy = 7;
        gbc.weightx = 1;
        gbc.weighty = 5;
        txtContenidoFormato.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtContenidoFormato.setLineWrap(true);
        txtContenidoFormato.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtContenidoFormato), gbc);

        // Fila 8: Área de Contenido para Respuesta "NO"
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JLabel("Contenido (Texto si la respuesta es 'NO'):"), gbc);

        gbc.gridy = 9;
        gbc.weightx = 1;
        gbc.weighty = 3;
        txtContenidoFormatoRespuestaNo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtContenidoFormatoRespuestaNo.setLineWrap(true);
        txtContenidoFormatoRespuestaNo.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtContenidoFormatoRespuestaNo), gbc);

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
        btnEliminar.addActionListener(e -> eliminarArticulo());
        btnCerrar.addActionListener(e -> dispose());

        // --- Ensamblaje Final ---
        add(leftPanel, BorderLayout.WEST);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        clearForm(); // Limpia y deshabilita Guardar/Eliminar

        // --- 🔥 ADAPTACIÓN (Bloqueo Inicial) ---
        setFormularioEnabled(false); // Deshabilita el formulario al inicio
    }

    /**
     * Crea y configura el panel de lógica de interactividad (Pregunta/Acción).
     */
    private JPanel createInteractividadPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lógica de Interacción (Si/No)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Checkbox para habilitar la interactividad
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        chkEsInteractivo = new JCheckBox("Este artículo requiere una pregunta de adhesión (Sí/No)");
        panel.add(chkEsInteractivo, gbc);

        // Fila 2: Pregunta
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Pregunta:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtPreguntaInteractiva = new JTextArea(2, 40);
        txtPreguntaInteractiva.setLineWrap(true);
        txtPreguntaInteractiva.setWrapStyleWord(true);
        panel.add(new JScrollPane(txtPreguntaInteractiva), gbc);

        // Fila 3: Acción si "Sí"
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Acción si [Sí] es la respuesta:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        String[] acciones = {ArticuloAnexo.ACCION_NINGUNA, ArticuloAnexo.ACCION_PEDIR_CAMPOS, ArticuloAnexo.ACCION_PEDIR_FICHERO};
        cmbAccionSi = new JComboBox<>(acciones);
        panel.add(cmbAccionSi, gbc);

        // Fila 4: Panel para las 4 Etiquetas (visible solo si se elige PEDIR_CAMPOS)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        pnlCamposEtiquetas = new JPanel(new GridLayout(2, 4, 5, 5));
        pnlCamposEtiquetas.setBorder(BorderFactory.createTitledBorder("Etiquetas de Campos a Cumplimentar (Máx 4)"));

        for (int i = 0; i < 4; i++) {
            txtEtiquetasCampos[i] = new JTextField(10);
            pnlCamposEtiquetas.add(new JLabel("Campo " + (i + 1) + " Etiqueta:"));
            pnlCamposEtiquetas.add(txtEtiquetasCampos[i]);
        }
        panel.add(pnlCamposEtiquetas, gbc);

        // Configurar los Listeners de visibilidad
        chkEsInteractivo.addActionListener(e -> toggleInteractividad());
        cmbAccionSi.addActionListener(e -> toggleEtiquetasCampos());

        // Inicializar estado (AHORA es seguro llamarlo)
        toggleInteractividad();

        return panel;
    }

    // En: com.licitador.configurator.ArticuloManagerDialog.java
    /**
     * Controla la visibilidad de los controles de pregunta y acción. (MÉTODO
     * RESTAURADO)
     */
    private void toggleInteractividad() {
        boolean enabled = chkEsInteractivo.isSelected();
        txtPreguntaInteractiva.setEnabled(enabled);
        cmbAccionSi.setEnabled(enabled);

        // Habilitar/Deshabilitar el JTextArea para la respuesta "NO"
        txtContenidoFormatoRespuestaNo.setEnabled(enabled);
        if (!enabled) {
            txtContenidoFormatoRespuestaNo.setText("");
        }

        toggleEtiquetasCampos(); // Llamar para actualizar el panel de campos
        if (!enabled) {
            // Limpiar si se deshabilita
            txtPreguntaInteractiva.setText("");
            cmbAccionSi.setSelectedItem(ArticuloAnexo.ACCION_NINGUNA);
        }
    }

    /**
     * Controla la visibilidad de los campos de etiqueta según la acción.
     * (MÉTODO RESTAURADO)
     */
    private void toggleEtiquetasCampos() {
        // Solo visible si "Interactivo" está marcado Y la acción es "PEDIR_CAMPOS"
        boolean visible = chkEsInteractivo.isSelected()
                && ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(cmbAccionSi.getSelectedItem());

        pnlCamposEtiquetas.setVisible(visible);

        if (!visible) {
            // Limpiar los campos si no son visibles
            for (JTextField txt : txtEtiquetasCampos) {
                txt.setText("");
            }
        }

        // Repintar el panel contenedor para asegurar que se actualice la GUI
        if (pnlCamposEtiquetas.getParent() != null) {
            pnlCamposEtiquetas.getParent().revalidate();
            pnlCamposEtiquetas.getParent().repaint();
        }
    }

    // --------------------------------------------------------------------------
    // --- NUEVO MÉTODO DE BLOQUEO DE FORMULARIO ---
    // --------------------------------------------------------------------------
    /**
     * Habilita o deshabilita todos los campos de entrada del formulario.
     *
     * @param enabled true para habilitar, false para deshabilitar.
     */
    private void setFormularioEnabled(boolean enabled) {
        // Campos Base
        txtId.setEnabled(enabled);
        txtOrden.setEnabled(enabled);
        txtTitulo.setEnabled(enabled);
        txtContenidoFormato.setEnabled(enabled);
        chkRequiereFirma.setEnabled(enabled);

        // Controles de Tags
        cmbTags.setEnabled(enabled);
        btnInsertTag.setEnabled(enabled);

        // Campos Interactivos
        chkEsInteractivo.setEnabled(enabled);
        txtPreguntaInteractiva.setEnabled(enabled);
        cmbAccionSi.setEnabled(enabled);
        txtContenidoFormatoRespuestaNo.setEnabled(enabled);

        // Habilitar/Deshabilitar los campos de etiqueta
        for (JTextField txt : txtEtiquetasCampos) {
            txt.setEnabled(enabled);
        }

        // Si estamos deshabilitando, también ocultamos el panel de etiquetas
        // (toggleInteractividad se encargará de mostrarlo si 'enabled' es true)
        if (!enabled) {
            pnlCamposEtiquetas.setVisible(false);
        }
    }

    // --------------------------------------------------------------------------
    // --- LÓGICA DE GESTIÓN (ADAPTADA) ---
    // --------------------------------------------------------------------------
    /**
     * Muestra los datos del artículo seleccionado en el formulario.
     */
    private void displayArticulo(ArticuloAnexo articulo) {
        if (articulo == null) {
            clearForm();
            return;
        }

        // --- 🔥 ADAPTACIÓN (Habilitar Formulario) ---
        setFormularioEnabled(true);

        // --- CAMPOS BASE ---
        txtId.setText(articulo.getIdArticulo());
        txtId.setEnabled(false); // ID no se puede cambiar al editar
        txtOrden.setText(String.valueOf(articulo.getOrden()));
        txtTitulo.setText(articulo.getTitulo());
        txtContenidoFormato.setText(articulo.getContenidoFormato());
        chkRequiereFirma.setSelected(articulo.isRequiereFirma());

        // --- CAMPOS INTERACTIVOS ---
        boolean esInteractivo = articulo.esInteractivo();
        chkEsInteractivo.setSelected(esInteractivo);
        txtPreguntaInteractiva.setText(articulo.getPreguntaInteractiva());
        cmbAccionSi.setSelectedItem(articulo.getAccionSi());
        txtContenidoFormatoRespuestaNo.setText(articulo.getContenidoFormatoRespuestaNo());

        // Campos de etiqueta
        String[] etiquetas = articulo.getEtiquetasCampos();
        if (etiquetas == null) {
            etiquetas = new String[0];
        }

        for (int i = 0; i < 4; i++) {
            if (i < etiquetas.length) {
                txtEtiquetasCampos[i].setText(etiquetas[i]);
            } else {
                txtEtiquetasCampos[i].setText("");
            }
        }

        // Actualizar la visibilidad de la GUI
        toggleInteractividad();

        btnEliminar.setEnabled(true);
        btnGuardar.setEnabled(true);
    }

    /**
     * Limpia el formulario para crear un nuevo artículo.
     */
    private void nuevoArticulo() {
        clearForm(); // Limpia y deshabilita

        // --- 🔥 ADAPTACIÓN (Habilitar Formulario) ---
        setFormularioEnabled(true); // Habilita el formulario para la nueva entrada

        txtId.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(true);
        txtId.requestFocus();
    }

    /**
     * Reinicia todos los campos del formulario.
     */
    private void clearForm() {
        // --- 🔥 ADAPTACIÓN (Bloquear Formulario) ---
        setFormularioEnabled(false);

        txtId.setText("");
        txtOrden.setText("");
        txtTitulo.setText("");
        txtContenidoFormato.setText("");
        chkRequiereFirma.setSelected(false);

        chkEsInteractivo.setSelected(false);
        txtPreguntaInteractiva.setText("");
        cmbAccionSi.setSelectedItem(ArticuloAnexo.ACCION_NINGUNA);
        txtContenidoFormatoRespuestaNo.setText("");
        for (JTextField txt : txtEtiquetasCampos) {
            txt.setText("");
        }

        // 'toggleInteractividad' es llamado por 'setFormularioEnabled(false)'
        // (a través de chkEsInteractivo.setSelected(false) si es necesario)
        txtId.setEnabled(false); // Específicamente deshabilitar ID
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(false);
        listaArticulos.clearSelection();
    }

    /**
     * Inserta la etiqueta XML de sustitución en la posición del cursor.
     */
    private void insertTag() {
        String selectedTag = (String) cmbTags.getSelectedItem();
        if (selectedTag != null) {
            String tagToInsert = "<DATO_LICITADOR ETQ=\"" + selectedTag + "\"/>";

            // Decidir en qué área de texto insertar (la que tenga el foco)
            JTextArea targetArea = txtContenidoFormato;
            if (txtContenidoFormatoRespuestaNo.hasFocus()) {
                targetArea = txtContenidoFormatoRespuestaNo;
            }

            int caretPosition = targetArea.getCaretPosition();
            targetArea.insert(tagToInsert, caretPosition);
            targetArea.setCaretPosition(caretPosition + tagToInsert.length());
            targetArea.requestFocusInWindow();
        }
    }

    /**
     * Guarda o actualiza un artículo.
     */
    private void guardarArticulo() {
        String id = txtId.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String contenidoSi = txtContenidoFormato.getText().trim();
        boolean requiereFirma = chkRequiereFirma.isSelected();
        int orden;

        try {
            // Asignar 0 si está vacío, para evitar error al guardar sin orden
            orden = txtOrden.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtOrden.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El campo 'Orden' debe ser un número entero válido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (id.isEmpty() || titulo.isEmpty() || contenidoSi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar ID, Título y Contenido (para 'Sí' o no interactivo).", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- RECOLECCIÓN DE DATOS INTERACTIVOS ---
        boolean esInteractivo = chkEsInteractivo.isSelected();
        String pregunta = "";
        String accion = ArticuloAnexo.ACCION_NINGUNA;
        String[] etiquetas;
        String contenidoNo = "";

        if (esInteractivo) {
            pregunta = txtPreguntaInteractiva.getText().trim();
            accion = (String) cmbAccionSi.getSelectedItem();
            contenidoNo = txtContenidoFormatoRespuestaNo.getText().trim();

            if (pregunta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar el texto de la Pregunta Interactiva.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (contenidoNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar el texto de respuesta para 'NO'.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(accion)) {
                etiquetas = Arrays.stream(txtEtiquetasCampos)
                        .map(txt -> txt.getText().trim())
                        .filter(tag -> !tag.isEmpty())
                        .toArray(String[]::new);

                if (etiquetas.length == 0) {
                    JOptionPane.showMessageDialog(this, "Ha seleccionado 'Pedir Campos' pero no ha definido ninguna etiqueta.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                etiquetas = new String[0];
            }
        } else {
            etiquetas = new String[0];
        }

        ArticuloAnexo articuloExistente = articulos.stream()
                .filter(a -> a.getIdArticulo().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);

        if (articuloExistente == null) {
            // Creación
            ArticuloAnexo nuevo = new ArticuloAnexo(id, orden, titulo,
                    esInteractivo, pregunta,
                    contenidoSi, contenidoNo,
                    accion, etiquetas,
                    requiereFirma);
            articulos.add(nuevo);
        } else {
            // Edición
            articuloExistente.setOrden(orden);
            articuloExistente.setTitulo(titulo);
            articuloExistente.setContenidoFormato(contenidoSi);
            articuloExistente.setContenidoFormatoRespuestaNo(contenidoNo);
            articuloExistente.setRequiereFirma(requiereFirma);
            articuloExistente.setEsInteractivo(esInteractivo);
            articuloExistente.setPreguntaInteractiva(pregunta);
            articuloExistente.setAccionSi(accion);
            articuloExistente.setEtiquetasCampos(etiquetas);
        }

        articuloService.guardarArticulos(articulos);
        loadListModel();

        JOptionPane.showMessageDialog(this, "Artículo '" + titulo + "' guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }

    /**
     * Elimina el artículo actualmente seleccionado.
     */
    private void eliminarArticulo() {
        ArticuloAnexo articuloSeleccionado = listaArticulos.getSelectedValue();
        if (articuloSeleccionado == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el artículo '" + articuloSeleccionado.getTitulo() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            articulos.remove(articuloSeleccionado);
            articuloService.guardarArticulos(articulos);
            loadListModel();
            clearForm();
            JOptionPane.showMessageDialog(this, "Artículo eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Carga la lista de artículos, la ordena y la muestra en el JList
     */
    private void loadListModel() {
        articulos.sort((a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));
        listModel.clear();
        articulos.forEach(listModel::addElement);
    }

    // --------------------------------------------------------------------------
    // --- CLASE INTERNA: RENDERER ---
    // --------------------------------------------------------------------------
    public static class ArticuloListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ArticuloAnexo) {
                ArticuloAnexo articulo = (ArticuloAnexo) value;
                setText(articulo.toString());

                if (articulo.esInteractivo() && !isSelected) {
                    setBackground(new Color(255, 255, 204)); // Amarillo claro
                } else if (!isSelected) {
                    setBackground(list.getBackground());
                }
            }
            return this;
        }
    }
}
