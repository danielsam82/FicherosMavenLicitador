package com.licitador.configurator;

import com.licitador.model.LicitacionData;
import com.licitador.model.ArchivoRequerido;
import com.licitador.model.ArticuloAnexo;
import com.licitador.service.TextAreaLogger;
import com.licitador.service.Logger;
import com.licitador.service.ArticuloAnexoService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Clase principal de la aplicación Configurador, que proporciona una interfaz
 * gráfica (GUI) para definir los parámetros de una nueva licitación
 * (expediente, objeto, lotes, documentos requeridos y anexos administrativos).
 */
public class ConfiguradorApp extends JFrame {

    // Componentes UI (Asegúrate que estas variables de campo coincidan con tus inicializaciones)
    private JTextArea txtAreaObjeto;
    private JTextField txtExpediente;
    private JRadioButton rbSiLotes, rbNoLotes;
    private JSpinner spnNumLotes;
    private JSpinner spnNumArchivosComunes;
    private JPanel pnlArchivosComunes;
    private JSpinner spnNumDocumentos;
    private JPanel pnlDocumentos;

    // --- NUEVOS CAMPOS PARA ANEXOS ---
    private JList<ArticuloAnexo> listaAnexosDisponibles;
    private DefaultListModel<ArticuloAnexo> listModelAnexos;
    private List<ArticuloAnexo> anexosSeleccionados = new ArrayList<>();
    private final ArticuloAnexoService anexoService = new ArticuloAnexoService();
    // ----------------------------------

    // Campos añadidos para el sistema de Logging
    private JTextArea logTxt;
    private Logger logger;

    /**
     * Constructor principal de la aplicación Configurador. Inicializa los
     * componentes y configura la interfaz de usuario.
     */
    public ConfiguradorApp() {
        initComponents();

        // Inicialización de Logging y UI (movido desde el método setupUI para asegurar el orden)
        logTxt = new JTextArea();
        logger = new TextAreaLogger(logTxt);

        setupUI();
        cargarAnexosDisponibles(); // Carga la lista inicial de anexos
    }

    // --- NUEVO MÉTODO DE CARGA ---
    /**
     * Carga la lista maestra de anexos en el ListModel.
     */
    private void cargarAnexosDisponibles() {
        // Cargar la lista solo una vez
        List<ArticuloAnexo> articulosCargados = anexoService.cargarArticulos();

        listModelAnexos.clear();
        articulosCargados.forEach(listModelAnexos::addElement);

        logger.logInfo("Base de datos de anexos cargada (" + articulosCargados.size() + " artículos)");
    }
    // -----------------------------

    /**
     * Inicializa todos los componentes de la interfaz de usuario (UI).
     */
    private void initComponents() {
        // Inicialización de los campos de texto y área de texto
        txtAreaObjeto = new JTextArea(3, 20);
        txtExpediente = new JTextField();

        // Inicialización de botones de radio
        rbSiLotes = new JRadioButton("Sí");
        rbNoLotes = new JRadioButton("No", true);

        // Inicialización de Spinners con sus modelos
        spnNumLotes = new JSpinner(new SpinnerNumberModel(1, 1, 200, 1));
        spnNumArchivosComunes = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        spnNumDocumentos = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));

        // Inicialización de paneles contenedores
        pnlArchivosComunes = new JPanel();
        pnlDocumentos = new JPanel();

        // --- NUEVOS COMPONENTES DE ANEXOS ---
        listModelAnexos = new DefaultListModel<>();
        listaAnexosDisponibles = new JList<>(listModelAnexos);
        // ------------------------------------
    }

    /**
     * Configura el diseño general de la ventana principal (JFrame), añadiendo
     * paneles y el botón de generación.
     */
    private void setupUI() {
        setTitle("Generador de Licitaciones - Fraternidad-Muprespa");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizar ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel principal con mejor distribución
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        // 1. Panel Norte (Datos Básicos y Gestión de Anexos)
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.add(createDatosBasicosPanel(), BorderLayout.CENTER);
        northPanel.add(createAnexosAdminButton(), BorderLayout.EAST); // Botón de gestión de anexos
        mainPanel.add(northPanel, BorderLayout.NORTH);

        // 2. Panel Central (Archivos y Anexos Requeridos)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.add(createArchivosPanel()); // Archivos Comunes y Oferta
        centerPanel.add(createAnexosPanel()); // Panel de selección de anexos
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Panel Inferior para el botón y el área de log
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));

        // Área de log
        JScrollPane logScrollPane = new JScrollPane(logTxt);
        logTxt.setEditable(false);
        logTxt.setBorder(BorderFactory.createTitledBorder("Registro de Exportación"));
        logTxt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logScrollPane.setPreferredSize(new Dimension(800, 150));
        bottomPanel.add(logScrollPane, BorderLayout.NORTH);

        // Botón generar
        JButton btnGenerar = createGenerateButton();
        bottomPanel.add(btnGenerar, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
    }

    // --- NUEVO MÉTODO: Botón para el Gestor de Anexos ---
    private JPanel createAnexosAdminButton() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnGestionarAnexos = new JButton("<html><center>Gestionar<br>Plantillas de Anexos</center></html>");
        btnGestionarAnexos.setFont(new Font("Arial", Font.BOLD, 12));
        btnGestionarAnexos.setPreferredSize(new Dimension(150, 60));
        btnGestionarAnexos.addActionListener(e -> abrirGestorAnexos());

        panel.add(btnGestionarAnexos, BorderLayout.NORTH);
        return panel;
    }

    private void abrirGestorAnexos() {
        // Abre el diálogo de gestión de anexos (que carga y guarda la lista maestra)
        ArticuloManagerDialog dialog = new ArticuloManagerDialog(this);
        dialog.setVisible(true);

        // Recargar la lista de anexos después de que el diálogo se cierre
        cargarAnexosDisponibles();
    }
    // ----------------------------------------------------

    // Método createDatosBasicosPanel sin cambios...
    private JPanel createDatosBasicosPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("Datos Básicos de la Licitación", new Color(0, 102, 204)));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Expediente
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Expediente:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtExpediente, gbc);

        // Objeto
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        panel.add(new JLabel("Objeto:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.3;
        txtAreaObjeto.setLineWrap(true);
        txtAreaObjeto.setWrapStyleWord(true);
        panel.add(new JScrollPane(txtAreaObjeto), gbc);

        // Lotes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        panel.add(new JLabel("¿Tiene lotes?"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        JPanel lotesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lotesPanel.setBackground(Color.WHITE);

        ButtonGroup bgLotes = new ButtonGroup();
        bgLotes.add(rbSiLotes);
        bgLotes.add(rbNoLotes);

        lotesPanel.add(rbSiLotes);
        lotesPanel.add(rbNoLotes);
        lotesPanel.add(new JLabel("Número de lotes:"));
        spnNumLotes.setEnabled(false); // Inicialmente deshabilitado

        // Listener para habilitar/deshabilitar spinner
        rbSiLotes.addActionListener(e -> spnNumLotes.setEnabled(true));
        rbNoLotes.addActionListener(e -> spnNumLotes.setEnabled(false));

        lotesPanel.add(spnNumLotes);
        panel.add(lotesPanel, gbc);

        return panel;
    }

    /**
     * Crea y configura el panel que contiene los controles para definir los
     * Archivos Comunes y los Documentos de Oferta requeridos. Es el panel de la
     * izquierda del CENTRO.
     */
    private JPanel createArchivosPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0)); // 2 columnas iguales
        panel.setBorder(createTitledBorder("Archivos y Documentos", new Color(153, 102, 255)));
        panel.setBackground(Color.WHITE);

        // Panel izquierdo: Archivos Comunes
        JPanel archivosComunesPanel = new JPanel(new BorderLayout());
        archivosComunesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel comunesHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comunesHeader.add(new JLabel("Nº Archivos Comunes:"));
        comunesHeader.add(spnNumArchivosComunes);

        pnlArchivosComunes.setLayout(new BoxLayout(pnlArchivosComunes, BoxLayout.Y_AXIS));

        archivosComunesPanel.add(comunesHeader, BorderLayout.NORTH);
        archivosComunesPanel.add(new JScrollPane(pnlArchivosComunes), BorderLayout.CENTER);

        // Panel derecho: Documentos de Oferta
        JPanel documentosPanel = new JPanel(new BorderLayout());
        documentosPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel documentosHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        documentosHeader.add(new JLabel("Nº Documentos:"));
        documentosHeader.add(spnNumDocumentos);

        pnlDocumentos.setLayout(new BoxLayout(pnlDocumentos, BoxLayout.Y_AXIS));

        documentosPanel.add(documentosHeader, BorderLayout.NORTH);
        documentosPanel.add(new JScrollPane(pnlDocumentos), BorderLayout.CENTER);

        // Listeners para actualizar los paneles de configuración de archivos
        spnNumArchivosComunes.addChangeListener(e -> actualizarArchivosComunes());
        spnNumDocumentos.addChangeListener(e -> actualizarDocumentos());

        actualizarArchivosComunes();
        actualizarDocumentos();

        panel.add(archivosComunesPanel);
        panel.add(documentosPanel);

        return panel;
    }

    // --- NUEVO MÉTODO: Panel de Selección de Anexos Requeridos ---
    private JPanel createAnexosPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("Anexos Administrativos Requeridos", new Color(255, 102, 0)));
        panel.setBackground(Color.WHITE);

        // Configuración de la lista de anexos disponibles (con selección múltiple)
        listaAnexosDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaAnexosDisponibles.setCellRenderer(new ArticuloManagerDialog.ArticuloListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(listaAnexosDisponibles);

        // Listener para capturar los elementos seleccionados
        listaAnexosDisponibles.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                anexosSeleccionados.clear();
                for (ArticuloAnexo anexo : listaAnexosDisponibles.getSelectedValuesList()) {
                    anexosSeleccionados.add(anexo);
                }
                logger.logInfo("Anexos seleccionados para esta licitación: " + anexosSeleccionados.size());
            }
        });

        panel.add(new JLabel("Seleccione los anexos administrativos requeridos (Control/Cmd + Clic para selección múltiple):"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    // -------------------------------------------------------------

    // Los métodos createFilePanel, createTitledBorder, actualizarArchivosComunes, 
    // y actualizarDocumentos permanecen sin cambios.
    // ... [Aquí van los métodos que siguen sin cambios: createGenerateButton, createTitledBorder, actualizarArchivosComunes, actualizarDocumentos, createFilePanel] ...
    /**
     * Crea y configura el botón principal de generación del archivo JAR.
     */
    private JButton createGenerateButton() {
        JButton btnGenerar = new JButton("GENERAR ARCHIVO PARA LICITADORES");
        btnGenerar.setFont(new Font("Arial", Font.BOLD, 18));
        btnGenerar.setBackground(new Color(0, 153, 76));
        btnGenerar.setForeground(Color.GREEN);
        btnGenerar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnGenerar.setFocusPainted(false);
        btnGenerar.addActionListener(this::generarJarLicitador);

        // Efecto visual al pasar el ratón
        btnGenerar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnGenerar.setBackground(new Color(0, 180, 90));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnGenerar.setBackground(new Color(0, 153, 76));
            }
        });

        return btnGenerar;
    }

    /**
     * Crea un borde con título y color de fuente personalizado.
     */
    private TitledBorder createTitledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                color
        );
    }

    /**
     * Actualiza dinámicamente el panel de configuración de los archivos
     * comunes.
     */
    private void actualizarArchivosComunes() {
        pnlArchivosComunes.removeAll();
        int num = (Integer) spnNumArchivosComunes.getValue();
        for (int i = 0; i < num; i++) {
            pnlArchivosComunes.add(createFilePanel("Archivo " + (i + 1)));
        }
        pnlArchivosComunes.revalidate();
        pnlArchivosComunes.repaint();
    }

    /**
     * Actualiza dinámicamente el panel de configuración de los documentos de
     * oferta.
     */
    private void actualizarDocumentos() {
        pnlDocumentos.removeAll();
        int num = (Integer) spnNumDocumentos.getValue();
        for (int i = 0; i < num; i++) {
            pnlDocumentos.add(createFilePanel("Documento " + (i + 1)));
        }
        pnlDocumentos.revalidate();
        pnlDocumentos.repaint();
    }

    /**
     * Crea un {@code JPanel} que contiene los campos para definir un archivo
     * requerido.
     */
    private JPanel createFilePanel(String title) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setBorder(BorderFactory.createTitledBorder(title));

        JTextField txt = new JTextField(25);
        rowPanel.add(new JLabel("Nombre:"));
        rowPanel.add(txt);

        JCheckBox chkObligatorio = new JCheckBox("Obligatorio");
        rowPanel.add(chkObligatorio);

        JCheckBox chkConfidencial = new JCheckBox("Confidencial");
        rowPanel.add(chkConfidencial);

        return rowPanel;
    }

// En: com.licitador.configurator.ConfiguradorApp.java
    /**
     * Controlador del evento al hacer clic en el botón "GENERAR". Recolecta los
     * datos de la interfaz, crea un objeto {@code LicitacionData} y llama al
     * {@code JarExporter} para empaquetarlo en un archivo JAR.
     */
    private void generarJarLicitador(ActionEvent e) {
        // Limpiar el log al iniciar
        logTxt.setText("");
        logger.logInfo("Iniciando generación del archivo JAR...");

        if (txtExpediente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "¡El expediente es obligatorio!", "Error", JOptionPane.ERROR_MESSAGE);
            logger.logError("Generación cancelada: El expediente está vacío.");
            return;
        }

        // 1. RECOLECCIÓN DE DATOS (Archivos Comunes)
        List<ArchivoRequerido> archivosComunesList = new ArrayList<>();
        for (Component c : pnlArchivosComunes.getComponents()) {
            if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                JTextField txtNombre = (JTextField) panel.getComponent(1);
                JCheckBox chkObligatorio = (JCheckBox) panel.getComponent(2);
                JCheckBox chkConfidencial = (JCheckBox) panel.getComponent(3);

                if (!txtNombre.getText().trim().isEmpty()) {
                    archivosComunesList.add(new ArchivoRequerido(
                            txtNombre.getText().trim(),
                            chkObligatorio.isSelected(),
                            chkConfidencial.isSelected()
                    ));
                    logger.logInfo("Arch. Común registrado: " + txtNombre.getText().trim());
                }
            }
        }

        // 1. RECOLECCIÓN DE DATOS (Documentos de Oferta)
        List<ArchivoRequerido> documentosOfertaList = new ArrayList<>();
        for (Component c : pnlDocumentos.getComponents()) {
            if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                JTextField txtNombre = (JTextField) panel.getComponent(1);
                JCheckBox chkObligatorio = (JCheckBox) panel.getComponent(2);
                JCheckBox chkConfidencial = (JCheckBox) panel.getComponent(3);

                if (!txtNombre.getText().trim().isEmpty()) {
                    documentosOfertaList.add(new ArchivoRequerido(
                            txtNombre.getText().trim(),
                            chkObligatorio.isSelected(),
                            chkConfidencial.isSelected()
                    ));
                    logger.logInfo("Doc. Oferta registrado: " + txtNombre.getText().trim());
                }
            }
        }

        // 1. RECOLECCIÓN DE DATOS (Artículos de Anexo)
        // (Usamos la variable 'anexosSeleccionados' que definimos en ConfiguradorApp)
        ArticuloAnexo[] anexosArray = anexosSeleccionados.toArray(new ArticuloAnexo[0]);

        // Crear objeto de datos LicitacionData
        int numLotes = rbSiLotes.isSelected() ? (Integer) spnNumLotes.getValue() : 0;

        // CRÍTICO: El constructor de LicitacionData DEBE aceptar la lista de anexos
        LicitacionData datos = new LicitacionData(
                txtExpediente.getText().trim(),
                txtAreaObjeto.getText().trim(),
                rbSiLotes.isSelected(),
                numLotes,
                archivosComunesList.toArray(new ArchivoRequerido[0]),
                documentosOfertaList.toArray(new ArchivoRequerido[0]),
                anexosArray // Se añade la lista de anexos
        );

        logger.logInfo("Datos de licitación recolectados correctamente.");
        logger.logInfo("Expediente: " + datos.getExpediente());
        // CORRECCIÓN: Usamos el getter de LicitacionData, no de Configuracion
        logger.logInfo("Lotes: " + (datos.tieneLotes() ? datos.getNumLotes() : "No"));
        logger.logInfo("Anexos administrativos seleccionados: " + datos.getArticulosAnexos().length); // Corregido para leer desde 'datos'

        // 2. GENERAR JAR
        try {
            String nombreJar = "licitacion-" + datos.getExpediente() + ".jar";

            String jarPath = getJarOutputPath(nombreJar);

            if (jarPath == null) {
                logger.logInfo("Generación cancelada por el usuario.");
                return;
            }

            logger.logInfo("Ruta de destino seleccionada: " + jarPath);

            // CORRECCIÓN: El constructor de JarExporter SÍ acepta LicitacionData
            JarExporter exporter = new JarExporter(datos, jarPath, logTxt);

            // CORRECCIÓN: El método se llama exportJar()
            exporter.exportJar();

            JOptionPane.showMessageDialog(this, "El archivo JAR de licitación se ha creado con éxito en: " + jarPath, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            logger.logInfo("¡Archivo JAR generado con éxito!");

        } catch (Exception ex) {
            logger.logError("Error crítico al generar el .jar: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al generar el .jar:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Muestra un diálogo {@code JFileChooser} para que el usuario seleccione la
     * ubicación y el nombre final del archivo JAR a guardar.
     */
    private String getJarOutputPath(String nombreJar) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo JAR");
        fileChooser.setSelectedFile(new File(nombreJar));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Método principal (main) para iniciar la aplicación Configurador.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new ConfiguradorApp().setVisible(true);
        });
    }
}
