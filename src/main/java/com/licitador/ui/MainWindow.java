package com.licitador.ui;

import com.licitador.model.ArchivoRequerido;
import com.licitador.model.LicitacionData;
import com.licitador.model.LicitadorData;
import com.licitador.service.Configuracion;
import com.licitador.service.FileData;
import com.licitador.service.FileManager;
import com.licitador.service.Logger;
import com.licitador.service.TextAreaLogger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The main window of the application.
 */
public class MainWindow extends JFrame {

    // UI Components
    private JButton cargarArchivosComunesButton;
    private JButton cargarOfertasButton;
    private JButton comprimirButton;
    private JButton instruccionesButton;
    private JButton guardarSesionButton;
    private JButton cargarSesionButton;
    private JButton salirButton;
    private JButton resetButton;
    private JButton editarLicitadorButton;
    private JButton verDetallesOfertaButton; // Nuevo botón
    private JTextArea logArea;
    private JTable archivosComunesTable;
    private JTable lotesTable;
    private JLabel numLotesLabel;
    private JLabel ofertasLabel;
    private JProgressBar progressBar;

    // --- NUEVOS CAMPOS DE LA UI PARA LICITADOR DATA ---
    private JTextField razonSocialField;
    private JTextField nifField;
    private JTextField domicilioField;
    private JTextField emailField;
    private JTextField telefonoField;
    // --------------------------------------------------
    // ¡AQUÍ VAN LAS NUEVAS DECLARACIONES! ✅
    // --- ¡AÑADE ESTO! DECLARACIÓN DE RADIO BUTTONS Y GRUPOS (NUEVOS CAMPOS) ✅ ---
    private JRadioButton pymeSiRadio;
    private JRadioButton pymeNoRadio;
    private JRadioButton extranjeraSiRadio;
    private JRadioButton extranjeraNoRadio;

    private ButtonGroup pymeGroup;
    private ButtonGroup extranjeraGroup;
    // --------------------------------------------------
    // Gestores y datos
    private FileManager fileManager;
    private Configuracion configuracion;
    private Logger logger;

    /**
     * Constructs the main window of the application.
     */
    public MainWindow() {
        inicializarComponentes();

        this.logger = new TextAreaLogger(logArea);

        configurarVentana();

        this.configuracion = cargarConfiguracionDesdeJar();

        if (this.configuracion == null) {
            logger.log("No se encontró configuración en el JAR, usando valores por defecto");
            inicializarDatosConfiguracion();
        } else {
            logger.log("Configuración cargada correctamente desde el JAR");
        }

        this.fileManager = new FileManager(configuracion, logger);

        completarInicializacionComponentes();
        configurarEventos();

        // Cargar los datos del licitador si se cargó una sesión o si son los datos por defecto (vacíos)
        cargarDatosLicitadorUI();

        SwingUtilities.invokeLater(this::iniciarFlujoAplicacion);

        // Llamada para bloquear los campos inmediatamente
        setLicitadorPanelEditable(false);
    }

    // --- MÉTODOS ADAPTADOS O NUEVOS PARA LICITADORDATA ---
    /**
     * Loads the data from LicitadorData into the UI fields.
     * Call this when starting and when loading a session.
     */
    private void cargarDatosLicitadorUI() {
        LicitadorData data = fileManager.getLicitadorData();

        // Text fields
        razonSocialField.setText(data.getRazonSocial() != null ? data.getRazonSocial() : "");
        nifField.setText(data.getNif() != null ? data.getNif() : "");
        domicilioField.setText(data.getDomicilio() != null ? data.getDomicilio() : "");
        emailField.setText(data.getEmail() != null ? data.getEmail() : "");
        telefonoField.setText(data.getTelefono() != null ? data.getTelefono() : "");

        // --- ¡ACTUALIZACIÓN PARA RADIO BUTTONS! (Estado Deseleccionado por defecto) ---
        // 1. PYME: 
        pymeGroup.clearSelection();
        if (data.esPyme()) {
            pymeSiRadio.setSelected(true);
        } else {
            // 🔥 CRÍTICO: Seleccionar "No" si es false
            pymeNoRadio.setSelected(true);
        }

        // 2. Extranjera: 
        extranjeraGroup.clearSelection();
        if (data.esExtranjera()) {
            extranjeraSiRadio.setSelected(true);
        } else {
            // 🔥 SOLUCIÓN: Seleccionar "No" si es false
            extranjeraNoRadio.setSelected(true);
        }

        logger.log("Datos del licitador cargados en la interfaz.");
    }

    /**
     * Saves the bidder's data.
     * @param razonSocial The company name.
     * @param nif The tax ID.
     * @param domicilio The address.
     * @param email The email.
     * @param telefono The phone number.
     * @param esPyme Whether the company is an SME.
     * @param esExtranjera Whether the company is foreign.
     * @param lotesModel The table model for the lots.
     */
    public void guardarDatosLicitador(
            String razonSocial, String nif, String domicilio, String email, String telefono,
            boolean esPyme, boolean esExtranjera, DefaultTableModel lotesModel) {

        fileManager.getLicitadorData().setRazonSocial(razonSocial);
        fileManager.getLicitadorData().setNif(nif);
        fileManager.getLicitadorData().setDomicilio(domicilio);
        fileManager.getLicitadorData().setEmail(email);
        fileManager.getLicitadorData().setTelefono(telefono);

        fileManager.getLicitadorData().setEsPyme(esPyme);
        fileManager.getLicitadorData().setEsExtranjera(esExtranjera);

        if (configuracion.isTieneLotes() && lotesModel != null) {
            Set<String> lotesElegidosIds = new HashSet<>();
            for (int i = 0; i < lotesModel.getRowCount(); i++) {
                String idLote = (String) lotesModel.getValueAt(i, 0);
                String participa = (String) lotesModel.getValueAt(i, 2);

                if ("Sí".equals(participa)) {
                    lotesElegidosIds.add(idLote);
                }
            }
            fileManager.setParticipacionDesdeUI(lotesElegidosIds);
        }
        logger.log("Bidder data and lot selection saved to the internal model.");

        cargarDatosLicitadorUI();
    }

    /**
     * Validates that the mandatory bidder fields (including Email and Phone) are filled and correctly formatted.
     * @return true if the validation is successful.
     */
    public boolean validarDatosLicitador() {

        LicitadorData data = fileManager.getLicitadorData();

        if (data.getRazonSocial().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company Name is required. Please fill in this field.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (data.getNif().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The NIF/CIF is a mandatory identification field.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (data.getDomicilio().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address is mandatory.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (data.getTelefono().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is mandatory.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (data.getEmail().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is mandatory for notifications. Please fill in this field.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!data.getTelefono().matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "The Phone must contain exactly 9 numeric digits.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!data.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "The Email format is not valid.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Displays the dialog that allows the user to configure the initial data of the bidder and the participation in lots.
     */
    private void mostrarConfiguracionInicialDialog() {
        ConfiguracionInicialDialog dialog = new ConfiguracionInicialDialog(this, fileManager, configuracion);
        dialog.setVisible(true);

        cargarDatosLicitadorUI();
    }

    /**
     * Transfers the LicitadorData and the lot model from the temporary dialog to the MainWindow components and updates the internal model (FileManager).
     * @param data The LicitadorData object.
     * @param lotesModel The table model for the lots.
     */
    public void actualizarComponentesYModeloDesdeDialogo(LicitadorData data, DefaultTableModel lotesModel) {
        razonSocialField.setText(data.getRazonSocial());
        nifField.setText(data.getNif());
        domicilioField.setText(data.getDomicilio());
        emailField.setText(data.getEmail());
        telefonoField.setText(data.getTelefono());

        pymeSiRadio.setSelected(data.esPyme());
        pymeNoRadio.setSelected(!data.esPyme());
        extranjeraSiRadio.setSelected(data.esExtranjera());
        extranjeraNoRadio.setSelected(!data.esExtranjera());

        if (configuracion.isTieneLotes() && lotesModel != null) {
            lotesTable.setModel(lotesModel);
            TableColumn participaColumn = lotesTable.getColumnModel().getColumn(2);
            JComboBox<String> comboBox = new JComboBox<>(new String[]{"Sí", "No"});
            participaColumn.setCellEditor(new DefaultCellEditor(comboBox));
        }

    }

    // --- FIN MÉTODOS ADAPTADOS O NUEVOS PARA LICITADORDATA ---
// REEMPLAZAR el método iniciarFlujoAplicacion() en MainWindow.java
    private void iniciarFlujoAplicacion() {
        boolean sesionIniciada = false;

        while (!sesionIniciada) {
            int opcion = JOptionPane.showOptionDialog(this,
                    "¿Desea cargar una sesión existente o iniciar una nueva?",
                    "Inicio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Cargar Sesión", "Nueva Sesión"},
                    "Nueva Sesión");

            if (opcion == JOptionPane.YES_OPTION) { // Usuario quiere cargar sesión
                if (fileManager.cargarSesion()) {
                    cargarDatosLicitadorUI();
                    logger.log("Nombre del licitador recuperado: " + fileManager.getLicitadorData().getRazonSocial());
                    sesionIniciada = true;
                    actualizarTablas();
                    actualizarTablaArchivosComunes();
                } else {
                    logger.log("No se pudo cargar la sesión. Por favor, seleccione otra opción.");
                }
            } else if (opcion == JOptionPane.NO_OPTION) { // Usuario quiere una nueva sesión

                iniciarNuevaSesion(); // Llama al método que lanza el diálogo

                // Comprobar si los datos principales del licitador están ahora cargados.
                if (!fileManager.getLicitadorData().getRazonSocial().isEmpty()) {
                    // La sesión se inició y los datos se guardaron.
                    sesionIniciada = true;
                    // Solo actualizamos tablas si la sesión es válida.
                    actualizarTablas();
                    logger.log("Nueva sesión iniciada y datos de configuración inicial guardados.");
                } else {
                    // El diálogo se canceló. sesionIniciada sigue siendo 'false'.
                    logger.log("Configuración inicial cancelada. Volviendo al menú de inicio.");
                }
            } else {
                // Esto se ejecuta si el usuario pulsa la 'X' o cancela el cuadro de diálogo inicial.
                logger.log("Aplicación cerrada por el usuario al inicio.");

                // 1. Limpiar recursos
                eliminarCarpetaTemp();

                // 2. Forzar el cierre de la aplicación
                System.exit(0);
                return; // Termina la ejecución del método
            }
        }
    }

    private void iniciarNuevaSesion() {
        // En lugar de un showInputDialog simple, mostramos el diálogo completo
        mostrarConfiguracionInicialDialog();

    }

    private Configuracion cargarConfiguracionDesdeJar() {
        try (InputStream is = getClass().getResourceAsStream("/config.dat"); ObjectInputStream ois = new ObjectInputStream(is)) {

            LicitacionData datos = (LicitacionData) ois.readObject();
            logger.log("Datos de licitación cargados: " + datos.getExpediente());

            Boolean[] obligatorios = Arrays.stream(datos.getArchivosComunes())
                    .map(ArchivoRequerido::esObligatorio)
                    .toArray(Boolean[]::new);

            boolean[] archivosComunesObligatorios = new boolean[obligatorios.length];
            for (int i = 0; i < obligatorios.length; i++) {
                archivosComunesObligatorios[i] = obligatorios[i];
            }

            // --- EXTRACCIÓN DE CONFIDENCIALIDAD ---
            Boolean[] confidenciales = Arrays.stream(datos.getArchivosComunes())
                    .map(ArchivoRequerido::esConfidencial)
                    .toArray(Boolean[]::new);

            boolean[] archivosComunesConfidenciales = new boolean[confidenciales.length];
            for (int i = 0; i < confidenciales.length; i++) {
                archivosComunesConfidenciales[i] = confidenciales[i];
            }
            // --- FIN EXTRACCIÓN DE CONFIDENCIALIDAD ---

            return new Configuracion(
                    datos.getObjeto(),
                    datos.getExpediente(),
                    datos.tieneLotes(),
                    datos.getNumLotes(),
                    convertirArchivosComunes(datos.getArchivosComunes()),
                    archivosComunesObligatorios,
                    archivosComunesConfidenciales,
                    convertirDocumentosOferta(datos.getDocumentosOferta()),
                    new String[]{
                        "Contiene información comercial sensible",
                        "Incluye secretos comerciales",
                        "Contiene secretos industriales"
                    }
            );
        } catch (Exception e) {
            logger.logError("Error al cargar configuración: " + e.getMessage());
            return null;
        }
    }

    private Configuracion.ArchivoOferta[] convertirDocumentosOferta(ArchivoRequerido[] documentos) {
        return Arrays.stream(documentos)
                .map(doc -> new Configuracion.ArchivoOferta(doc.getNombre(), doc.esConfidencial(), doc.esObligatorio()))
                .toArray(Configuracion.ArchivoOferta[]::new);
    }

    private String[] convertirArchivosComunes(ArchivoRequerido[] archivos) {
        return Arrays.stream(archivos)
                .map(ArchivoRequerido::getNombre)
                .toArray(String[]::new);
    }

    private void configurarVentana() {
        setTitle("Fichero Estructura - Fraternidad-Muprespa");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icono.png"))).getImage());
        } catch (Exception e) {
            logger.logError("Error al cargar el icono de la ventana: " + e.getMessage());
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
    }

    private void inicializarComponentes() {
        logArea = new JTextArea();
        logArea.setEditable(false);

        cargarArchivosComunesButton = new JButton("Cargar Archivos Comunes");
        cargarOfertasButton = new JButton("Cargar Ofertas");
        comprimirButton = new JButton("Comprimir Archivos");
        instruccionesButton = new JButton("Instrucciones");
        guardarSesionButton = new JButton("Guardar Sesión");
        cargarSesionButton = new JButton("Cargar Sesión");
        salirButton = new JButton("Salir");
        resetButton = new JButton("Restablecer");

        // --- INICIALIZACIÓN DE LOS NUEVOS CAMPOS ---
        razonSocialField = new JTextField(30);
        nifField = new JTextField(20);
        domicilioField = new JTextField(30);
        emailField = new JTextField(20);
        telefonoField = new JTextField(15);
        // --------------------------------------------
        // Inicialización del grupo PYME
        pymeSiRadio = new JRadioButton("Sí");
        pymeNoRadio = new JRadioButton("No");
        pymeGroup = new ButtonGroup();
        pymeGroup.add(pymeSiRadio);
        pymeGroup.add(pymeNoRadio);

        // Inicialización del grupo Extranjera
        extranjeraSiRadio = new JRadioButton("Sí");
        extranjeraNoRadio = new JRadioButton("No");
        extranjeraGroup = new ButtonGroup();
        extranjeraGroup.add(extranjeraSiRadio);
        extranjeraGroup.add(extranjeraNoRadio);
        // --------------------------------------------
        //Botón edición licitador
        editarLicitadorButton = new JButton("Datos básicos participación");

        archivosComunesTable = new JTable(new DefaultTableModel(new Object[]{"Nombre", "Obligatorio", "Estado", "Confidencial"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            // Recomendación: asegurar que el renderizado de texto funcione.
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        });

        // 🔥 INICIALIZACIÓN DE LOTESTABLE CON CORRECCIÓN CLAVE 🔥
        lotesTable = new JTable(new DefaultTableModel(new Object[]{"Lote", "Archivos Cargados", "Estado", "Participa"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo la columna "Participa" (índice 3) debe ser editable
                return column == 3;
            }

            // Sobrescribimos getColumnClass para decirle a Swing el tipo de cada columna
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // Índice 3 es la columna "Participa"
                    return Boolean.class;  // Para que use JCheckBox
                }
                // ¡CORRECCIÓN! Usamos String.class para el resto.
                return String.class;
            }
        });

        verDetallesOfertaButton = new JButton("Mostrar detalles");
        verDetallesOfertaButton.setEnabled(false); // Deshabilitado por defecto

        numLotesLabel = new JLabel();
        ofertasLabel = new JLabel();

        // El método de configuración de tablas se llama al final de completarInicializacionComponentes
    }

    private void inicializarDatosConfiguracion() {
        String[] supuestosConfidencialidad = {
            "Contiene información comercial sensible",
            "Incluye secretos comerciales",
            "Contiene secretos industriales"
        };

        String[] archivosComunes = {
            "Anexo \"Declaración de confidencialidad\".",
            "DR-Aportación Ofertas Técnicas.",
            "DR-Ofertas (PDF) Económicas.",
            "Excel - Resto Tarifas"
        };

        boolean[] archivosComunesObligatorios = {
            true,
            true,
            false,
            false
        };

        // Este es el array que faltaba para la confidencialidad de los archivos comunes.
        // Asumimos que por defecto no son confidenciales.
        boolean[] archivosComunesConfidenciales = {
            false,
            false,
            false,
            false
        };

        Configuracion.ArchivoOferta[] archivosOferta = {
            new Configuracion.ArchivoOferta("PIC2025_32130 Oferta económica y Proyecto de servicio y calidad", false, true),};

        configuracion = new Configuracion(
                "Contratación del servicio de fisioterapia en régimen ambulatorio para pacientes derivados desde un centro "
                + "propio de FRATERNIDAD-MUPRESPA, en diversas localidades de la Comunidad Autónoma de Castilla - La Mancha.",
                "PIC2025_32130",
                true,
                20,
                archivosComunes,
                archivosComunesObligatorios,
                archivosComunesConfidenciales, // <-- ¡Este es el argumento que faltaba!
                archivosOferta,
                supuestosConfidencialidad
        );
    }

    private void completarInicializacionComponentes() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // CÓDIGO NUEVO EN 'completarInicializacionComponentes'
        JPanel topPanelLateral = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanelLateral.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        topPanelLateral.add(crearPanelInformacion());
        topPanelLateral.add(crearPanelDatosLicitador());

        mainPanel.add(topPanelLateral, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        JPanel panelArchivos = new JPanel(new GridLayout(1, 2, 15, 5));
        panelArchivos.add(crearPanelArchivosComunesConBoton());
        panelArchivos.add(crearPanelOfertasConBoton());
        centro.add(panelArchivos, BorderLayout.NORTH);
        centro.add(crearPanelTablas(), BorderLayout.CENTER);
        mainPanel.add(centro, BorderLayout.CENTER);

        mainPanel.add(crearPanelLog(), BorderLayout.EAST);
        mainPanel.add(crearPanelBotonesInferiores(), BorderLayout.SOUTH);

        add(mainPanel);

        configurarRenderizadorTablas();

        // -----------------------------------------------------------------------------
        // INICIO: LÓGICA DE ESCUCHA DE PARTICIPACIÓN DE LOTES (GUARDADO) 🔥
        // -----------------------------------------------------------------------------
        // El índice 3 es la columna "Participa" en el modelo de la tabla de lotes.
        final int COLUMNA_PARTICIPACION = 3;

        // 1. Obtener el modelo de la tabla de lotes
        DefaultTableModel modeloLotes = (DefaultTableModel) lotesTable.getModel();

        // 2. Adjuntar el Listener para escuchar los clics en el checkbox
        modeloLotes.addTableModelListener(e -> {
            // Solo actuamos si es un evento de actualización (UPDATE) y es en la columna del checkbox
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == COLUMNA_PARTICIPACION) {
                int row = e.getFirstRow();
                int numLote = row + 1; // Fila 0 = Lote 1

                // El valor de la celda es un Boolean (true/false)
                Boolean participa = (Boolean) modeloLotes.getValueAt(row, COLUMNA_PARTICIPACION);

            }
        });
        // -----------------------------------------------------------------------------
        // FIN: LÓGICA DE ESCUCHA DE PARTICIPACIÓN DE LOTES
        // -----------------------------------------------------------------------------
    }

// --- MÉTODO PARA DATOS DEL LICITADOR FINALIZADO CON BOTÓN DE EDICIÓN ---
    private JPanel crearPanelDatosLicitador() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Licitador y Clasificación"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Razón Social y NIF
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Razón Social*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(razonSocialField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("NIF/CIF*:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        panel.add(nifField, gbc);

        // Fila 1: Domicilio
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Domicilio Social*:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(domicilioField, gbc);

        // Fila 2: Email y Teléfono
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Email*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(emailField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Teléfono*:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        panel.add(telefonoField, gbc);

        // ----------------------------------------------------------------------------------
        // Fila 3: LEYENDA (Izquierda) y CLASIFICACIÓN (Derecha con Separador)
        // ----------------------------------------------------------------------------------
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;

        // Zona Izquierda (Leyenda)
        JLabel leyendaObligatorio = new JLabel("Los campos marcados con * son obligatorios.");
        leyendaObligatorio.setForeground(Color.BLUE);
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(leyendaObligatorio, gbc);

        // Zona Derecha (Clasificación)
        JPanel pymePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pymePanel.add(new JLabel("¿Es PYME?*:"));
        pymePanel.add(pymeSiRadio);
        pymePanel.add(pymeNoRadio);

        JPanel extranjeraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        extranjeraPanel.add(new JLabel("¿Es Empresa Extranjera?*:"));
        extranjeraPanel.add(extranjeraSiRadio);
        extranjeraPanel.add(extranjeraNoRadio);

        JPanel clasificacionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        clasificacionPanel.add(pymePanel);
        clasificacionPanel.add(new JLabel("|"));
        clasificacionPanel.add(extranjeraPanel);

        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(clasificacionPanel, gbc);

        // ----------------------------------------------------------------------------------
        // 🔥 NUEVA FILA 4: BOTÓN DE EDICIÓN
        // ----------------------------------------------------------------------------------
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4; // Ocupa las 4 columnas
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; // No estirar horizontalmente

        JPanel botonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botonPanel.add(editarLicitadorButton);

        panel.add(botonPanel, gbc);

        // Restaurar GBC para futuras filas si las hubiera (aunque parece ser la última)
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        return panel;
    }
    // ---------------------------------------------

    private TableCellRenderer crearHeaderRendererConTooltips(JTable table, Map<String, String> tooltips) {
        TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();

        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel headerLabel = (JLabel) c;
                    String columnName = headerLabel.getText();
                    if (tooltips.containsKey(columnName)) {
                        headerLabel.setToolTipText(tooltips.get(columnName));
                        // Establecer el color de fondo del tooltip
                        UIManager.put("ToolTip.background", new Color(255, 255, 204)); // Amarillo pastel
                    } else {
                        headerLabel.setToolTipText(null);
                    }
                }
                return c;
            }
        };
    }

// En MainWindow.java
    private void configurarRenderizadorTablas() {

        // 1. Configuración de la selección de filas
        archivosComunesTable.setRowSelectionAllowed(false);
        archivosComunesTable.setColumnSelectionAllowed(false);

        // 🔥 CORRECCIÓN CRÍTICA: Desactivar la selección de filas y columnas en lotes/oferta única
        lotesTable.setRowSelectionAllowed(false); // <--- CORREGIDO: Vuelve a ser FALSE
        lotesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lotesTable.setColumnSelectionAllowed(false);

        // CRÍTICO: Bloqueo de Navegación/Foco de la Tabla (Mantenido)
        archivosComunesTable.setFocusable(false);
        lotesTable.setFocusable(false);

        // ----------------------------------------------------------------------
        // Interceptor de Foco/Selección ELIMINADO (Ya no aplica, la tabla no permite selección)
        // Se elimina el bloque 'if (configuracion.isTieneLotes()) { ... }' completo.
        // ----------------------------------------------------------------------
        // 2. y 3. Definición de tooltips (Mantenido)
        Map<String, String> tooltipsComunes = new HashMap<>();
        tooltipsComunes.put("Documento", "Nombre de los archivos comunes requeridos.");
        tooltipsComunes.put("Obligatorio", "Indica si el archivo debe ser subido obligatoriamente.");
        tooltipsComunes.put("Estado", "Estado de carga del archivo.");
        tooltipsComunes.put("Confidencial", "Declarado como confidencial por el licitador.");

        Map<String, String> tooltipsLotes = new HashMap<>();
        tooltipsLotes.put("Lote", "Número de lote o nombre de la oferta.");
        tooltipsLotes.put("Archivos", "Número de archivos de oferta cargados para este lote.");
        tooltipsLotes.put("Estado", "Estado de carga de los archivos de oferta del lote.");
        tooltipsLotes.put("Participa", "Marque si presenta oferta para este lote. ¡CRÍTICO para la compresión!");

        // 4. Aplicar los renderizadores de la cabecera con tooltips (Mantenido)
        archivosComunesTable.getTableHeader().setDefaultRenderer(crearHeaderRendererConTooltips(archivosComunesTable, tooltipsComunes));
        lotesTable.getTableHeader().setDefaultRenderer(crearHeaderRendererConTooltips(lotesTable, tooltipsLotes));

        // 5. Renderizador de celdas BASE para ALINEACIÓN y FOCO (Mantenido)
        DefaultTableCellRenderer baseCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Bloqueo de Pintura de Foco: Eliminar el borde azul de foco visual
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                }

                // Alineación por defecto a la izquierda
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                }

                // Si está seleccionado, dejamos el color por defecto (azul de selección)
                if (!isSelected) {
                    // Si NO está seleccionado, restablecemos los colores a neutros
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }

                return c;
            }
        };

        // 6. Asignación de renderizadores eliminada (se hace en los métodos actualizarTabla)
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Información de la Licitación"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Objeto:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea objetoArea = new JTextArea(configuracion.getObjetoLicitacion(), 3, 40);
        objetoArea.setEditable(false);
        objetoArea.setLineWrap(true);
        objetoArea.setWrapStyleWord(true);
        objetoArea.setBackground(panel.getBackground());
        panel.add(new JScrollPane(objetoArea), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("Expediente:"), gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(configuracion.getNumeroExpediente()), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("¿Tiene lotes?"), gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(configuracion.isTieneLotes() ? "Sí" : "No"), gbc);

        if (configuracion.isTieneLotes()) {
            gbc.gridx = 0;
            gbc.gridy++;
            panel.add(new JLabel("Nº de lotes:"), gbc);

            gbc.gridx = 1;
            numLotesLabel.setText(String.valueOf(configuracion.getNumLotes()));
            panel.add(numLotesLabel, gbc);
        }

        return panel;
    }

    private JPanel crearPanelArchivosComunesConBoton() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JTextArea area = new JTextArea("Archivos comunes:\n", 3, 25);
        for (String nombre : configuracion.getNombresArchivosComunes()) {
            area.append("- " + nombre + "\n");
        }
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(350, 100));
        panel.add(scroll, BorderLayout.CENTER);

        // --- INICIO DE LA MODIFICACIÓN ---
        // Se crea un nuevo panel con FlowLayout para que el botón no se estire
        JPanel buttonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cargarArchivosComunesButton = new JButton("Cargar Archivos Comunes"); // Asegurar que se inicializa aquí para que no sea null
        buttonWrapperPanel.add(cargarArchivosComunesButton);
        panel.add(buttonWrapperPanel, BorderLayout.SOUTH);
        // --- FIN DE LA MODIFICACIÓN ---

        return panel;
    }

    private JPanel crearPanelOfertasConBoton() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        if (configuracion.isTieneLotes()) {
            ofertasLabel.setText("Archivos de oferta por lote:");
        } else {
            ofertasLabel.setText("Archivos de oferta:");
        }

        JTextArea area = new JTextArea("", 3, 25);
        area.append(ofertasLabel.getText() + "\n");
        for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
            area.append("- " + ofertaConfig.getNombre() + "\n");
        }
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(350, 100));
        panel.add(scroll, BorderLayout.CENTER);

        // Nuevo panel para los botones de abajo
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.add(cargarOfertasButton);
        // El botón ya está creado con el texto "Ver información cargada"
        buttonPanel.add(verDetallesOfertaButton); // El botón siempre se añade
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelTablas() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 5));
        DefaultTableModel lotesModel = (DefaultTableModel) lotesTable.getModel();

        // ------------------ TABLA DE ARCHIVOS COMUNES ------------------
        JScrollPane scrollComunes = new JScrollPane(archivosComunesTable);
        scrollComunes.setBorder(BorderFactory.createTitledBorder("Archivos Comunes Cargados"));
        scrollComunes.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollComunes);

        // Bloqueo de reordenación de columnas
        archivosComunesTable.getTableHeader().setReorderingAllowed(false);

        // ------------------ TABLA DE LOTES/OFERTAS ------------------
        JScrollPane scrollLotes = new JScrollPane(lotesTable);

        // Bloqueo de reordenación de columnas
        lotesTable.getTableHeader().setReorderingAllowed(false);

        if (configuracion.isTieneLotes()) {
            scrollLotes.setBorder(BorderFactory.createTitledBorder("Lotes y Ofertas"));
            // 4 columnas, incluyendo 'Participa'
            lotesModel.setColumnIdentifiers(new Object[]{"Lote", "Archivos Cargados", "Estado", "Participa"});

            // Índice fijo en el modelo: 3. Índice en la vista tras mover: 0.
            final int PARTICIPA_VIEW_INDEX = 0;
            final int PARTICIPA_MODEL_INDEX = 3;

            // 🚀 Reordenación: Mover la columna "Participa" (índice 3) a la posición 0 (Vista).
            lotesTable.getColumnModel().moveColumn(PARTICIPA_MODEL_INDEX, PARTICIPA_VIEW_INDEX);

            // 🛠️ Configuración de la Columna "Participa" (Ahora de texto "Sí"/"No")
            TableColumn participaColumn = lotesTable.getColumnModel().getColumn(PARTICIPA_VIEW_INDEX);

            // Renderizador de cabecera (para centrar)
            DefaultTableCellRenderer defaultHeaderRenderer = (DefaultTableCellRenderer) lotesTable.getTableHeader().getDefaultRenderer();
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                    }
                    return c;
                }
            };
            participaColumn.setHeaderRenderer(headerRenderer);

            // Alineamos la columna 'Participa' al centro.
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            participaColumn.setCellRenderer(centerRenderer);

            // Ajuste de ancho de la columna Participa
            participaColumn.setPreferredWidth(80);
            participaColumn.setMaxWidth(100);

        } else {
            // MODO SIN LOTES
            scrollLotes.setBorder(BorderFactory.createTitledBorder("Oferta Única"));
            // Solo 3 columnas: NO se incluye "Participa" en el modelo.
            lotesModel.setColumnIdentifiers(new Object[]{"Oferta", "Archivos Cargados", "Estado"});
        }

        scrollLotes.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollLotes);

        return panel;
    }

    private JScrollPane crearPanelLog() {
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Acciones"));
        scrollLog.setPreferredSize(new Dimension(250, 0));
        logArea.setEditable(false);
        return scrollLog;
    }

    private JPanel crearPanelBotonesInferiores() {
        JPanel panel = new JPanel(new BorderLayout(20, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.add(instruccionesButton);
        panel.add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.add(cargarSesionButton);
        centerPanel.add(guardarSesionButton);
        centerPanel.add(resetButton);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        rightPanel.add(progressBar);
        rightPanel.add(comprimirButton);
        rightPanel.add(salirButton);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private void confirmarSalida() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea salir? Si no ha guardado la sesión, los datos se perderán.",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            eliminarCarpetaTemp();
            dispose();
        }
    }

    /**
     * Recorre la lotesTable y devuelve un SET con los IDs (String) de los lotes
     * seleccionados para participar. Lee el estado de participación (String
     * "Sí" o "No") de la tabla.
     *
     * @return Set<String> donde el valor es el ID del lote (ej: "1", "2",
     * etc.).
     */
    private Set<String> obtenerLotesSeleccionadosIds() {
        Set<String> lotesSeleccionados = new HashSet<>();

        // Si la configuración no requiere lotes, no hay que leer la tabla.
        if (!configuracion.isTieneLotes()) {
            return lotesSeleccionados;
        }

        DefaultTableModel model = (DefaultTableModel) lotesTable.getModel();

        // Asumimos el orden del MODELO (antes de mover la columna en la vista)
        final int LOTE_NAME_MODEL_INDEX = 0;
        final int PARTICIPA_MODEL_INDEX = 3;

        for (int i = 0; i < model.getRowCount(); i++) {
            // 1. Obtener el estado de participación (se lee el String "Sí" o "No")
            Object participaValue = model.getValueAt(i, PARTICIPA_MODEL_INDEX);
            boolean participa = "Sí".equals(participaValue);

            if (participa) {
                // 2. Obtener el nombre del lote (ej: "Lote 1")
                String nombreLote = (String) model.getValueAt(i, LOTE_NAME_MODEL_INDEX);

                // 3. Extraer el ID del lote (ej: "Lote 1" -> "1")
                try {
                    // Quitamos el prefijo "Lote " y el resultado es el ID String
                    String loteId = nombreLote.substring("Lote ".length()).trim();
                    lotesSeleccionados.add(loteId);
                } catch (Exception e) {
                    System.err.println("Error al parsear el ID de lote de la tabla: " + nombreLote);
                }
            }
        }
        return lotesSeleccionados;
    }

    // MÉTODO AUXILIAR REQUERIDO DENTRO DE MAINWINDOW
    /**
     * [TEMPORAL]: Convierte el Set<String> de IDs de lote (ej: {"1", "3"}) en
     * un Map<Integer, String> (ej: {1: "Lote 1", 3: "Lote 3"}) para mantener la
     * compatibilidad con el constructor de CargarOfertaDialog.
     *
     * * @param lotesIdSet El conjunto de IDs de lote seleccionados (ej: {"1",
     * "2"}).
     * @return El mapa en el formato antiguo {LoteNum: NombreLote}.
     */
    private Map<Integer, String> convertirSetALotesElegidos(Set<String> lotesIdSet) {
        Map<Integer, String> lotesElegidos = new HashMap<>();
        for (String loteIdStr : lotesIdSet) {
            try {
                int loteNum = Integer.parseInt(loteIdStr);
                String nombreLote = "Lote " + loteIdStr; // Generamos el nombre de nuevo
                lotesElegidos.put(loteNum, nombreLote);
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir ID de lote: " + loteIdStr);
            }
        }
        return lotesElegidos;
    }

    private void configurarEventos() {

        editarLicitadorButton.addActionListener(e -> editarDatosLicitador());

        cargarArchivosComunesButton.addActionListener(e -> {
            CargarArchivoComunDialog dialog = new CargarArchivoComunDialog(this, fileManager, this::actualizarTablas);
            dialog.setVisible(true);
            actualizarTablas();
        });

        cargarOfertasButton.addActionListener(e -> {
            try {
                // 1. Verificación básica de componentes
                if (fileManager == null || configuracion == null) {
                    JOptionPane.showMessageDialog(this, "ERROR FATAL: Componentes internos (fileManager/configuracion) no inicializados.", "ERROR DE OBJETO NULO", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!configuracion.isTieneLotes()) {
                    // ➡️ CASO SIN LOTES: Usa el constructor simple
                    CargarOfertaDialog dialog = new CargarOfertaDialog(this, fileManager, configuracion);
                    dialog.setVisible(true);

                } else {
                    // ➡️ CASO CON LOTES: Lee la selección del usuario

                    // --- ADAPTACIÓN CLAVE ---
                    // 1. Usamos el método correcto que devuelve Set<String>
                    Set<String> lotesElegidosIdSet = obtenerLotesSeleccionadosIds();

                    if (lotesElegidosIdSet.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Debe seleccionar al menos un lote marcando la casilla 'Participa' para cargar las ofertas.",
                                "Lotes Requeridos",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 2. Convertimos el Set<String> al formato antiguo Map<Integer, String> 
                    //    para mantener la compatibilidad con el constructor del diálogo.
                    Map<Integer, String> lotesElegidos = convertirSetALotesElegidos(lotesElegidosIdSet);
                    // --- FIN ADAPTACIÓN CLAVE ---

                    CargarOfertaDialog dialog = new CargarOfertaDialog(this, fileManager, configuracion, lotesElegidos);
                    dialog.setVisible(true);
                }

                // 2. Actualizar el estado de las tablas después de la carga
                actualizarTablas();

            } catch (Exception ex) {
                // GESTIÓN CRÍTICA DE ERRORES: Muestra la excepción por pantalla.
                String errorMsg = "Fallo en la creación/carga del diálogo:\n" + ex.getMessage();

                if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                    errorMsg += "\nCausa: " + ex.getCause().getMessage();
                }

                JOptionPane.showMessageDialog(this,
                        "¡Error crítico! El programa falló al intentar abrir la ventana de carga.\n" + errorMsg,
                        "ERROR DE EJECUCIÓN FATAL",
                        JOptionPane.ERROR_MESSAGE);

                // Opcional para depuración local
                // ex.printStackTrace(); 
            }
        });

        comprimirButton.addActionListener(e -> {
            // --- INICIO MANEJADOR DE COMPRIMIR ---

            // 0. Validar datos del licitador
            if (!validarDatosLicitador()) {
                logger.logError("Operación de compresión cancelada: Faltan datos obligatorios del licitador.");
                return;
            }

            // 1. Verificación 1: Archivos cargados (al menos uno debe existir)
            if (fileManager.getArchivosComunes().isEmpty() && fileManager.getArchivosOferta().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay archivos cargados para comprimir.", "Error", JOptionPane.ERROR_MESSAGE);
                logger.logError("No hay archivos cargados para comprimir.");
                return;
            }

            // ------------------- 🔥 NUEVA VALIDACIÓN DE PARTICIPACIÓN 🔥 --------------------
            // 1.5. Verificación 2: Mínimo de participación (SOLO si el proceso tiene lotes)
            if (!fileManager.validarMinimoParticipacion()) {
                // Si falla, significa que configuracion.isTieneLotes() es TRUE y no hay casillas marcadas.
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar al menos un lote o una oferta marcando la casilla 'Participa' para poder comprimir.",
                        "Advertencia de Participación",
                        JOptionPane.WARNING_MESSAGE);
                logger.logError("Operación de compresión cancelada: Ningún lote marcado para participar.");
                return;
            }
            // ------------------------------------------------------------------------

            // 2. Verificación 3: Archivos obligatorios
            // Solo se ejecuta si el mínimo de participación ha sido validado (si aplica).
            if (!fileManager.estanArchivosObligatoriosCompletos()) {
                JOptionPane.showMessageDialog(this, "No se pueden comprimir los archivos. Faltan documentos obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                logger.logError("Operación de compresión cancelada: Faltan documentos obligatorios.");
                return;
            }

            // --- PASO 3 y 4: USAR JFILECHOOSER PARA SUGERIR NOMBRE BASADO EN NIF Y ELEGIR UBICACIÓN ---
            LicitadorData data = fileManager.getLicitadorData();
            String nifLimpio = data.getNif().replaceAll("[^a-zA-Z0-9-]", "");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String identificadorBase = nifLimpio.isEmpty() ? "Licitador" : nifLimpio;
            String defaultFileName = String.format("Oferta_%s_%s.zip", identificadorBase, timeStamp);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Oferta Comprimida como:");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            // 2. Establecer el nombre por defecto del archivo (¡Aquí se sugiere el nombre!)
            fileChooser.setSelectedFile(new File(defaultFileName));

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivoDestinoCompleto = fileChooser.getSelectedFile();
                File carpetaDestino = archivoDestinoCompleto.getParentFile();
                String zipFileNameBase = "Oferta";

                // Si la extensión .zip falta (porque el usuario no la escribió), la añadimos.
                if (!archivoDestinoCompleto.getName().toLowerCase().endsWith(".zip")) {
                    archivoDestinoCompleto = new File(archivoDestinoCompleto.getAbsolutePath() + ".zip");
                }

                // Verificación de seguridad para permisos de escritura
                if (carpetaDestino == null || !carpetaDestino.isDirectory() || !carpetaDestino.canWrite()) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede escribir en la carpeta seleccionada. Por favor, elija otra ubicación.",
                            "Error de Permiso",
                            JOptionPane.ERROR_MESSAGE);
                    logger.logError("El usuario seleccionó una carpeta no escribible o inválida.");
                    return;
                }

                // Iniciar la compresión
                setBotonesEnabled(false);

                // 5. Llamada al método del FileManager 
                fileManager.comprimirArchivosConProgreso(
                        carpetaDestino,
                        zipFileNameBase, // "Oferta"
                        logArea.getText(),
                        progressBar,
                        () -> {
                            setBotonesEnabled(true);
                        }
                );

            } else {
                logger.log("Operación de selección de destino de compresión cancelada por el usuario.");
            }
            // --- FIN MANEJADOR DE COMPRIMIR ---
        });

        // 💾 --- INICIO CORRECCIÓN: MANEJADOR DE GUARDAR SESIÓN AÑADIDO --- 💾
        guardarSesionButton.addActionListener(e -> {
            if (!validarDatosLicitador()) {
                logger.logError("Guardado de sesión cancelado: Faltan datos obligatorios del licitador.");
                JOptionPane.showMessageDialog(this, "Debe completar los datos obligatorios del licitador antes de guardar la sesión.", "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 1. Sincronizar UI -> Modelo antes de guardar
            // 🔥 ELIMINAR ESTA LÍNEA: guardarDatosLicitador();

            // 2. Ejecutar el guardado
            if (fileManager.guardarSesion()) {
                logger.log("Sesión guardada con éxito.");
            } else {
                logger.logError("Error al guardar la sesión.");
            }
        });
        // 💾 --- FIN CORRECCIÓN: MANEJADOR DE GUARDAR SESIÓN AÑADIDO --- 💾

        // --- INICIO ADAPTACIÓN DEL MANEJADOR DE CARGAR SESIÓN ---
        cargarSesionButton.addActionListener(e -> {
            if (fileManager.cargarSesion()) {
                fileManager.setLogger(this.logger);
                cargarDatosLicitadorUI(); // Cargar Data -> UI
                actualizarTablas();
                logger.log("Nombre del licitador recuperado: " + fileManager.getLicitadorData().getRazonSocial());

            }
        });
        // --- FIN ADAPTACIÓN DEL MANEJADOR DE CARGAR SESIÓN ---

        salirButton.addActionListener(e -> confirmarSalida());

        instruccionesButton.addActionListener(e -> mostrarInstrucciones());

        resetButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea restablecer los datos cargados? Los cambios no guardados se perderán.", "Confirmar Restablecer", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                fileManager.resetData();
                cargarDatosLicitadorUI(); // Limpiar campos de la UI
                actualizarTablasIniciales();
                logger.log("Se ha restablecido la sesión. Datos eliminados de la memoria.");
            }
        });

        // 1. Evento de selección para la tabla de lotes
        // 🛑 ELIMINAMOS el ListSelectionListener antiguo que dependía de la selección de filas, 
        //    porque lotesTable.setRowSelectionAllowed(false) lo ha anulado.
        /*
            lotesTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    boolean tieneSeleccion = lotesTable.getSelectedRow() != -1;
                    if (verDetallesOfertaButton != null) {
                        verDetallesOfertaButton.setEnabled(tieneSeleccion);
                    }
                }
            });
         */
        // 2. Evento de clic para el botón de detalles
        if (verDetallesOfertaButton != null) {
            verDetallesOfertaButton.addActionListener(e -> {
                try {
                    // El usuario quiere ver TODAS las ofertas cargadas.
                    // Usamos -1 como nuestra constante de vista GLOBAL.
                    final int VISTA_GLOBAL = -1;

                    // Si el botón está habilitado, hay archivos cargados. Llamamos en modo GLOBAL.
                    new DetalleOfertasDialog(this, fileManager, configuracion, VISTA_GLOBAL);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error al intentar mostrar los detalles de ofertas globales: " + ex.getMessage(),
                            "Error de Visualización",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void setBotonesEnabled(boolean enabled) {
        cargarArchivosComunesButton.setEnabled(enabled);
        cargarOfertasButton.setEnabled(enabled);
        comprimirButton.setEnabled(enabled);
        instruccionesButton.setEnabled(enabled);
        guardarSesionButton.setEnabled(enabled);
        cargarSesionButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        if (salirButton != null) {
            salirButton.setEnabled(enabled);
        }
    }

    /**
     * Updates the tables in the main window.
     */
    public void actualizarTablas() {
        actualizarTablaArchivosComunes();
        actualizarTablaLotes();
    }

    /**
     * Updates the tables to their initial state.
     */
    public void actualizarTablasIniciales() {
        actualizarTablaArchivosComunes();
        actualizarTablaLotes();
    }

// En MainWindow.java
    private void actualizarTablaArchivosComunes() {
        // 1. Obtener los datos de los arrays paralelos
        Map<String, FileData> archivosComunesCargados = fileManager.getArchivosComunes();
        String[] nombresRequeridos = configuracion.getNombresArchivosComunes();
        boolean[] obligatorios = configuracion.getArchivosComunesObligatorios();

        // 2. CRÍTICO: Crear una lista temporal de objetos para poder ordenar
        // El objeto temporal será: {Nombre, EsObligatorio}
        List<Object[]> archivosConInfo = new ArrayList<>();
        for (int i = 0; i < nombresRequeridos.length; i++) {
            archivosConInfo.add(new Object[]{nombresRequeridos[i], obligatorios[i]});
        }

        // Ordenar la lista: Obligatorio (true) arriba, luego por nombre alfabéticamente
        archivosConInfo.sort((a1, a2) -> {
            boolean esObligatorio1 = (boolean) a1[1];
            boolean esObligatorio2 = (boolean) a2[1];
            String nombre1 = (String) a1[0];
            String nombre2 = (String) a2[0];

            // 1. Orden por Obligatoriedad (Obligatorio arriba)
            int obligatoriedad = Boolean.compare(esObligatorio2, esObligatorio1);
            if (obligatoriedad != 0) {
                return obligatoriedad;
            }
            // 2. Orden Alfabético por Nombre
            return nombre1.compareTo(nombre2);
        });

        // 3. Crear el nuevo modelo de la tabla
        // Columnas: Documento, Obligatorio, Estado, Confidencial
        String[] columnNames = {"Documento", "Obligatorio", "Estado", "Confidencial"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 4. Llenar el modelo con la lista ordenada
        for (Object[] archivoInfo : archivosConInfo) {
            String nombreRequerido = (String) archivoInfo[0];
            boolean esObligatorio = (boolean) archivoInfo[1];

            FileData archivoCargado = archivosComunesCargados.get(nombreRequerido);

            String estado = (archivoCargado != null) ? "Cargado" : "Falta";
            String esConfidencial = (archivoCargado != null && archivoCargado.esConfidencial()) ? "Sí" : "No";

            model.addRow(new Object[]{
                nombreRequerido,
                esObligatorio ? "Sí" : "No",
                estado,
                esConfidencial
            });
        }

        // 5. Asignar el modelo y el Renderizador
        archivosComunesTable.setModel(model);

        // Asignar el ComunesRenderer Personalizado a TODAS las columnas (para pintar la fila entera)
        ComunesRenderer renderer = new ComunesRenderer(archivosComunesCargados);
        for (int i = 0; i < archivosComunesTable.getColumnCount(); i++) {
            archivosComunesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void actualizarTablaLotes() {

        // 1. Obtener datos de referencia
        Map<String, FileData> ofertasCargadas = fileManager.getArchivosOferta();
        Configuracion.ArchivoOferta[] ofertasConfig = configuracion.getArchivosOferta();
        int totalArchivosOferta = ofertasConfig.length;

        // Calcular el total de archivos de oferta obligatorios
        long totalObligatorias = Arrays.stream(ofertasConfig)
                .filter(Configuracion.ArchivoOferta::esObligatorio)
                .count();

        if (configuracion.isTieneLotes()) {
            // CASO 1: LICITACIÓN CON LOTES

            // Redefinir el modelo para la vista CON LOTES
            // Se mantiene el orden de columnas crucial para el diálogo externo
            String[] columnNames = {"Lote", "Archivos", "Estado", "Participa"};
            DefaultTableModel lotesFiltradosModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Ninguna celda es editable (solo visualización)
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    // Todas las columnas son Object/String
                    return Object.class;
                }
            };

            // El bucle garantiza el orden Lote 1, Lote 2, etc.
            for (int loteNum = 1; loteNum <= configuracion.getNumLotes(); loteNum++) {

                // Estado de participación
                boolean participa = fileManager.getParticipacionLote(loteNum);

                // 🔥 CORRECCIÓN CRÍTICA: SOLO añadir la fila si participa
                if (!participa) {
                    continue; // Saltar al siguiente lote si no participa
                }

                final String loteKeyPrefix = "Lote" + loteNum + "_";

                Set<String> ofertasDelLote = ofertasCargadas.keySet().stream()
                        .filter(key -> key.startsWith(loteKeyPrefix))
                        .collect(Collectors.toSet());

                long obligatoriasCargadasLote = ofertasDelLote.stream()
                        .map(key -> key.substring(loteKeyPrefix.length()))
                        .filter(ofertaNombre -> Arrays.stream(ofertasConfig)
                        .anyMatch(ao -> ao.getNombre().equals(ofertaNombre) && ao.esObligatorio()))
                        .count();

                String estado;
                if (ofertasDelLote.isEmpty()) {
                    estado = "No cargado";
                } else if (obligatoriasCargadasLote == totalObligatorias && ofertasDelLote.size() == totalArchivosOferta) {
                    estado = "Cargado";
                } else {
                    estado = "Parcialmente cargado";
                }

                String participaStr = "Sí"; // Ya sabemos que es 'Sí' porque filtramos arriba

                // Se añade SOLAMENTE el lote participante
                lotesFiltradosModel.addRow(new Object[]{
                    "Lote " + loteNum,
                    ofertasDelLote.size() + " archivos",
                    estado,
                    participaStr // Columna 3 (índice 3) contiene "Sí"
                });
            }

            lotesTable.setModel(lotesFiltradosModel);

            // Asignar el OfertaRenderer a TODAS las columnas 
            OfertaRenderer renderer = new OfertaRenderer();
            for (int i = 0; i < lotesTable.getColumnCount(); i++) {
                lotesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }

        } else {
            // CASO 2: LICITACIÓN SIN LOTES (Oferta Única) (Se mantiene igual)

            // Redefinir el modelo para la vista SIN LOTES (solo 3 columnas)
            String[] columnNames = {"Documentación", "Archivos", "Estado"};
            DefaultTableModel modelOfertaUnica = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Lógica de cálculo del estado para oferta única
            int archivosCargados = ofertasCargadas.size();

            long obligatoriasCargadas = ofertasCargadas.keySet().stream()
                    .map(key -> {
                        return Arrays.stream(ofertasConfig)
                                .filter(ao -> ao.getNombre().equals(key) && ao.esObligatorio())
                                .findFirst().orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .count();

            String estado;
            if (archivosCargados == 0) {
                estado = "No cargado";
            } else if (obligatoriasCargadas == totalObligatorias && archivosCargados == totalArchivosOferta) {
                estado = "Cargado";
            } else {
                estado = "Parcialmente cargado";
            }

            // Añadir la fila de Oferta Única
            modelOfertaUnica.addRow(new Object[]{"Oferta Única", archivosCargados + " archivos", estado});

            lotesTable.setModel(modelOfertaUnica);

            // Asignar el OfertaRenderer a TODAS las columnas
            OfertaRenderer renderer = new OfertaRenderer();
            for (int i = 0; i < lotesTable.getColumnCount(); i++) {
                lotesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }

        actualizarEstadoBotonDetalles();
        logger.log("Tabla de lotes/oferta única actualizada.");
    }

    /**
     * Bloquea/desbloquea todos los campos de datos del licitador en la
     * MainWindow.
     */
    private void setLicitadorPanelEditable(boolean editable) {
        // Componentes de texto
        razonSocialField.setEditable(editable);
        nifField.setEditable(editable);
        domicilioField.setEditable(editable);
        emailField.setEditable(editable);
        telefonoField.setEditable(editable);

        // Radio buttons
        pymeSiRadio.setEnabled(editable);
        pymeNoRadio.setEnabled(editable);
        extranjeraSiRadio.setEnabled(editable);
        extranjeraNoRadio.setEnabled(editable);
    }

    /**
     * Muestra el diálogo de configuración inicial, permitiendo la edición de
     * datos/lotes.
     */
    private void editarDatosLicitador() {
        logger.log("Iniciando edición de datos de licitador y selección de lotes...");

        // La clave es reusar el método que ya creamos, que se encarga de mostrar 
        // el diálogo y sincronizar los datos de vuelta.
        mostrarConfiguracionInicialDialog();

        // La actualización de tablas se produce automáticamente tras el diálogo 
        // gracias a que el flujo original llama a actualizarTablas() / actualizarTablasIniciales().
        // Aquí solo nos aseguramos de que el panel se bloquea de nuevo.
        setLicitadorPanelEditable(false);
        logger.log("Datos de licitador y selección de lotes actualizados.");
    }

    private void mostrarInstrucciones() {
        JDialog dialog = new JDialog(this, "Instrucciones", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea instruccionesArea = new JTextArea();
        instruccionesArea.setEditable(false);
        instruccionesArea.setLineWrap(true);
        instruccionesArea.setWrapStyleWord(true);
        instruccionesArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        instruccionesArea.setText(generarInstrucciones());

        dialog.add(new JScrollPane(instruccionesArea), BorderLayout.CENTER);
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Habilita o deshabilita el botón de detalles generales de ofertas. Solo se
     * habilita si hay AL MENOS UN archivo de oferta cargado en el sistema (es
     * decir, el mapa de archivos de oferta no está vacío).
     */
    private void actualizarEstadoBotonDetalles() {
        // Es true si el mapa de archivos de oferta cargados NO está vacío.
        boolean hayArchivosCargados = !fileManager.getArchivosOferta().isEmpty();

        if (verDetallesOfertaButton != null) {
            verDetallesOfertaButton.setEnabled(hayArchivosCargados);
        }
    }

// Dentro de la clase MainWindow { ... }
    /**
     * Renderer personalizado para la tabla de archivos comunes
     * ({@code archivosComunesTable}).
     *
     * Aplica colores de fondo y alineación para indicar el estado del documento
     * (Cargado, Falta Obligatorio, Opcional no cargado).
     *
     * Los colores utilizados son: - Verde claro: Documento cargado. - Rojo
     * suave: Documento obligatorio y no cargado (alerta).
     */
    class ComunesRenderer extends DefaultTableCellRenderer {

        private final Map<String, FileData> comunesCargados;
        private final Color COLOR_ALERTA_OBLIGATORIO = new Color(255, 220, 220); // Rojo más suave para el fondo
        private final Color COLOR_CARGADO_EXITO = new Color(200, 255, 200);   // Verde claro

        /**
         * Constructor del Renderer de Archivos Comunes.
         *
         * @param comunesCargados Mapa de archivos comunes cargados para
         * determinar el estado.
         */
        public ComunesRenderer(Map<String, FileData> comunesCargados) {
            this.comunesCargados = comunesCargados;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String obligatorio = (String) table.getValueAt(row, 1);
            String estado = (String) table.getValueAt(row, 2);

            boolean esObligatorio = "Sí".equals(obligatorio);
            boolean estaCargado = "Cargado".equals(estado);

            // 1. Lógica de Estilos (Foco y Borde)
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }

            // 2. Aplicar lógica de color
            if (!isSelected) {
                if (estaCargado) {
                    c.setBackground(COLOR_CARGADO_EXITO);
                    c.setForeground(Color.BLACK);
                } else if (esObligatorio) {
                    c.setBackground(COLOR_ALERTA_OBLIGATORIO);
                    c.setForeground(Color.RED.darker().darker());
                } else {
                    // Opcional no cargado
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            } else {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }

            // 3. Alineación y Modificación de Texto
            if (c instanceof JLabel) {
                if (column == 0) {
                    // Documento (Columna 0): A la izquierda
                    ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                } else {
                    // Obligatorio, Estado, Confidencial: Centrado
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }

                // Modificar el texto de la columna "Estado" (índice 2) para incluir emojis
                if (column == 2 && !isSelected) {
                    if (estaCargado) {
                        ((JLabel) c).setText("Cargado ✅");
                    } else {
                        ((JLabel) c).setText("Falta" + (esObligatorio ? " ❌" : ""));
                    }
                }
            }

            return c;
        }
    }

    /**
     * Renderer personalizado para la tabla de lotes u oferta única
     * ({@code lotesTable}).
     *
     * Aplica colores de fondo a la fila completa según el estado de la
     * oferta/lote: - Verde claro: Totalmente Cargado. - Amarillo claro:
     * Parcialmente Cargado (falta documentación). - Rojo claro: No Cargado
     * (alerta).
     */
    class OfertaRenderer extends DefaultTableCellRenderer {

        private final Color COLOR_CARGADO = new Color(200, 255, 200);   // Verde claro
        private final Color COLOR_PARCIAL = new Color(255, 255, 204);   // Amarillo claro
        private final Color COLOR_ALERTA = new Color(255, 200, 200);  // Rojo claro

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 1. Lógica de Estilos (Foco y Borde)
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }

            // 2. Obtener el estado (siempre en la columna 2) y determinar la alerta
            String estado = (String) table.getValueAt(row, 2);
            boolean participa = table.getColumnCount() == 4 && "Sí".equals(table.getValueAt(row, 3));
            boolean esOfertaUnica = table.getColumnCount() == 3;
            boolean debeAplicarAlerta = participa || esOfertaUnica;

            // 3. Aplicar lógica de color a TODA LA FILA
            if (!isSelected) {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());

                if ("Cargado".equals(estado)) {
                    c.setBackground(COLOR_CARGADO);
                    c.setForeground(Color.BLACK);
                } else if ("Parcialmente cargado".equals(estado)) {
                    c.setBackground(COLOR_PARCIAL);
                    c.setForeground(Color.BLACK);
                } else if ("No cargado".equals(estado) && debeAplicarAlerta) {
                    // Solo aplica color de alerta si el lote participa o es la oferta única
                    c.setBackground(COLOR_ALERTA);
                    c.setForeground(Color.RED.darker().darker());
                }
            } else {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }

            // 4. Alineación y Modificación de Texto
            if (c instanceof JLabel) {
                if (column == 0) {
                    // Lote / Documentación (Columna 0): A la izquierda
                    ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                } else {
                    // Archivos, Estado, Participa: Centrado
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }

                // Modificar el texto de la columna "Estado" (índice 2) para incluir emojis
                if (column == 2 && !isSelected) {
                    if ("Cargado".equals(estado)) {
                        ((JLabel) c).setText("Cargado ✅");
                    } else if ("Parcialmente cargado".equals(estado)) {
                        ((JLabel) c).setText("Parcialmente cargado 🟡");
                    } else if ("No cargado".equals(estado)) {
                        ((JLabel) c).setText("No cargado ❌");
                    }
                }
            }

            return c;
        }
    }

    /**
     * Genera la cadena de texto con las instrucciones detalladas de uso de la
     * aplicación. Esta información se muestra en el diálogo de instrucciones.
     *
     * @return String que contiene las instrucciones formateadas.
     */
    private String generarInstrucciones() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instrucciones para la Preparación de la Documentación para Licitación\n");
        sb.append("----------------------------------------------------------------------\n\n");
        sb.append("Bienvenido a la herramienta de preparación de documentación para licitaciones. Siga estos pasos para completar su oferta de manera correcta y rápida.\n\n");

        sb.append("1. Datos del Licitador:\n");
        sb.append("    - Rellene los campos obligatorios (Razón Social y NIF/CIF) y el resto de datos de contacto.\n");
        sb.append("    - Estos datos son cruciales para el guardado de la sesión y el nombramiento del archivo ZIP final.\n");
        sb.append("    - **Clave de Lotes**: Si la licitación tiene lotes, en la tabla inferior derecha, marque la casilla 'Participa' para aquellos lotes a los que desea presentar oferta.\n\n");

        sb.append("2. Carga de Archivos Comunes:\n");
        sb.append("    - Haga clic en el botón 'Cargar Archivos Comunes' para adjuntar documentos que son comunes a toda la oferta, como declaraciones o anexos.\n");
        sb.append("    - Se abrirá una ventana donde podrá seleccionar el archivo correspondiente a cada documento común listado. Si un documento es obligatorio y no se carga, no podrá comprimir la oferta final.\n\n");

        sb.append("3. Carga de Ofertas:\n");
        sb.append("    - Haga clic en el botón 'Cargar Ofertas' para adjuntar la documentación específica de su propuesta.\n");
        sb.append("    - Si la licitación tiene lotes, deberá seleccionar el lote y el documento de oferta que desea cargar. Asegúrese de cargar la oferta correcta para cada lote.\n");
        sb.append("    - Si la licitación no tiene lotes, simplemente seleccione el documento de oferta correspondiente.\n\n");

        sb.append("4. Archivos Confidenciales:\n");
        sb.append("    - Si un documento de oferta debe ser marcado como confidencial, marque la casilla 'Declarar confidencialidad'.\n");
        sb.append("    - Deberá seleccionar uno o más supuestos de confidencialidad y proporcionar una justificación detallada para cada uno. Esta información se incluirá en un archivo de texto independiente que se generará junto con su documento.\n\n");

        sb.append("5. Guardado y Restauración de la Sesión:\n");
        sb.append("    - Utilice el botón 'Guardar Sesión' para guardar el progreso de su trabajo en un archivo '.dat'. **Asegúrese de que los datos del licitador están completos y correctos antes de guardar.**\n");
        sb.append("    - El botón 'Cargar Sesión' le permite restaurar el trabajo desde un archivo '.dat' previamente guardado, incluyendo los datos del licitador.\n\n");

        sb.append("6. Compresión de la Oferta Final:\n");
        sb.append("    - **Debe rellenar la Razón Social y el NIF/CIF antes de comprimir.**\n");
        sb.append("    - Una vez que haya cargado todos los archivos obligatorios y opcionales que desee, haga clic en el botón 'Comprimir Archivos'.\n");
        sb.append("    - La aplicación verificará que todos los archivos obligatorios estén cargados. Si falta alguno, se lo notificará.\n");
        sb.append("    - Se generará un archivo ZIP que contendrá toda la documentación en una estructura de carpetas organizada: una carpeta para los archivos comunes y, si aplica, una carpeta para cada lote.\n");
        sb.append("    - Dentro del ZIP, también se incluirán un archivo de log con las acciones realizadas y los archivos de justificación de confidencialidad para los documentos que lo requieran.\n\n");

        sb.append("7. Salir y Restablecer:\n");
        sb.append("    - Use el botón 'Restablecer' para eliminar todos los archivos cargados en la sesión actual y empezar de nuevo. Esta acción no se puede deshacer.\n");
        sb.append("    - Use el botón 'Salir' para cerrar la aplicación de forma segura. Se le pedirá una confirmación para evitar la pérdida accidental de datos.\n\n");

        return sb.toString();
    }

    /**
     * Attempts to delete the temporary "temp" folder and all its contents.
     * This is typically called when closing the application to clean up temporary files.
     * If an error occurs during deletion, it is logged.
     */
    public void eliminarCarpetaTemp() {
        try {
            Path tempDir = Paths.get("temp");
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                logger.log("Temporary folder 'temp' deleted.");
            }
        } catch (IOException e) {
            logger.logError("Error deleting temporary folder: " + e.getMessage());
        }
    }

    /**
     * Exposes the FileManager instance (file and tender data manager) so that it can be used by external classes such as dialogs.
     * @return The FileManager instance.
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * Exposes the Logger instance (event log manager).
     * @return The Logger instance.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * The main entry point for the application. Initializes the user interface on the Event Dispatch Thread (EDT) to ensure thread safety in Swing.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
