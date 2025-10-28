package com.licitador.ui;

import com.licitador.model.LicitadorData;
import com.licitador.service.Configuracion;
import com.licitador.service.FileManager;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.stream.IntStream;

/**
 * Diálogo modal para recoger los datos iniciales del licitador y la
 * participación en lotes al iniciar una Nueva Sesión o al editar la
 * configuración existente.
 * <p>
 * Permite la entrada de datos personales del licitador (razón social, NIF, etc.)
 * y, si la licitación tiene lotes, la selección de los lotes en los que participará.
 * Almacena los datos introducidos en el {@link FileManager} y notifica a la
 * {@link MainWindow} para actualizar su estado.
 * </p>
 */
public class ConfiguracionInicialDialog extends JDialog {

    /**
     * Referencia a la ventana principal de la aplicación.
     */
    private final MainWindow parent;
    /**
     * Gestor de archivos para acceder y persistir los datos del licitador y la configuración.
     */
    private final FileManager fileManager;
    /**
     * Objeto de configuración de la licitación actual.
     */
    private final Configuracion configuracion;

    // Componentes del Licitador
    /** Campo de texto para la razón social del licitador. */
    private JTextField razonSocialField;
    /** Campo de texto para el NIF/CIF del licitador. */
    private JTextField nifField;
    /** Campo de texto para el domicilio del licitador. */
    private JTextField domicilioField;
    /** Campo de texto para el correo electrónico del licitador. */
    private JTextField emailField;
    /** Campo de texto para el número de teléfono del licitador. */
    private JTextField telefonoField;

    /** RadioButton para indicar si el licitador es una PYME. */
    private JRadioButton pymeSiRadio;
    /** RadioButton para indicar si el licitador NO es una PYME. */
    private JRadioButton pymeNoRadio;
    /** RadioButton para indicar si el licitador es una empresa extranjera. */
    private JRadioButton extranjeraSiRadio;
    /** RadioButton para indicar si el licitador NO es una empresa extranjera. */
    private JRadioButton extranjeraNoRadio;

    /** Tabla que muestra los lotes disponibles y permite seleccionar la participación. */
    private JTable lotesTable;

    /**
     * Constructor para el diálogo de configuración inicial.
     *
     * @param parent La ventana principal de la aplicación, utilizada para la interacción y actualización.
     * @param fm El {@link FileManager} que gestiona los datos del licitador y de la licitación.
     * @param config El objeto {@link Configuracion} de la licitación actual.
     */
    public ConfiguracionInicialDialog(MainWindow parent, FileManager fm, Configuracion config) {
        super(parent, "Configuración Inicial del Licitador", true); // Modal
        this.parent = parent;
        this.fileManager = fm;
        this.configuracion = config;

        // 1. Inicializar la UI del diálogo
        inicializarComponentesUI();

        // 2. Cargar datos existentes (del FileManager a los campos del diálogo)
        cargarDatosLicitadorAlDialogo();

        // 3. Configurar la tabla de lotes (incluyendo el listener de limpieza)
        configurarTablaLotes();

        // 4. Configurar la disposición de la ventana
        setLayout(new BorderLayout());
        add(crearPanelPrincipal(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ---------------------- MÉTODOS DE INICIALIZACIÓN ----------------------
    /**
     * Inicializa todos los componentes de la interfaz de usuario del diálogo,
     * como los campos de texto, botones de radio y la tabla de lotes.
     */
    private void inicializarComponentesUI() {
        // Inicializar campos de texto
        razonSocialField = new JTextField(20);
        nifField = new JTextField(20);
        domicilioField = new JTextField(20);
        emailField = new JTextField(20);
        telefonoField = new JTextField(20);

        // Inicializar Radio Buttons
        pymeSiRadio = new JRadioButton("Sí");
        pymeNoRadio = new JRadioButton("No");
        ButtonGroup pymeGroup = new ButtonGroup();
        pymeGroup.add(pymeSiRadio);
        pymeGroup.add(pymeNoRadio);

        extranjeraSiRadio = new JRadioButton("Sí");
        extranjeraNoRadio = new JRadioButton("No");
        ButtonGroup extranjeraGroup = new ButtonGroup();
        extranjeraGroup.add(extranjeraSiRadio);
        extranjeraGroup.add(extranjeraNoRadio);

        // Inicializar Tabla de Lotes
        lotesTable = new JTable();
        // cargarTablaLotesInicial() se llamará dentro de configurarTablaLotes()
    }

    /**
     * Carga la estructura inicial de la tabla de lotes y rellena con la información
     * de los lotes y el estado de participación actual, obtenida del {@link FileManager}.
     * Si la licitación no tiene lotes, muestra un mensaje informativo.
     */
    private void cargarTablaLotesInicial() {
        if (!configuracion.isTieneLotes()) {
            lotesTable.setModel(new DefaultTableModel(
                    new Object[][]{{"La licitación no está dividida en lotes."}},
                    new Object[]{"Información"}
            ));
            return;
        }

        // Columnas: ID Lote, Nombre, Participa (Sí/No)
        String[] columnNames = {"ID Lote", "Descripción", "Participa"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Permitir editar solo la columna 'Participa'
                return column == 2;
            }
        };

        // Crear filas con el estado de participación ACTUAL (leído del FileManager)
        IntStream.rangeClosed(1, configuracion.getNumLotes()).forEach(i -> {
            boolean participa = fileManager.getParticipacionLote(i);
            String participaStr = participa ? "Sí" : "No";

            model.addRow(new Object[]{String.valueOf(i), "Lote " + i, participaStr});
        });

        lotesTable.setModel(model);

        // Configurar el editor para la columna 'Participa' (índice 2)
        TableColumn participaColumn = lotesTable.getColumnModel().getColumn(2);
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Sí", "No"});
        participaColumn.setCellEditor(new DefaultCellEditor(comboBox));

        // OCULTAR la columna "ID Lote" (Columna 0)
        TableColumn idColumn = lotesTable.getColumnModel().getColumn(0);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setWidth(0);
        idColumn.setPreferredWidth(0);
        idColumn.setResizable(false);

        // Ajustar ancho de las otras columnas
        lotesTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Descripción
        lotesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Participa
    }

    /**
     * Configura la tabla de lotes, incluyendo su modelo y un {@code TableModelListener}
     * para gestionar la deselección de lotes. Si un lote se deselecciona y ya tiene
     * archivos cargados, se solicita confirmación al usuario para eliminarlos.
     */
    private void configurarTablaLotes() {
        cargarTablaLotesInicial();

        if (configuracion.isTieneLotes()) {
            lotesTable.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    // Solo nos interesan las actualizaciones de celdas
                    if (e.getType() == TableModelEvent.UPDATE) {
                        final int row = e.getFirstRow();
                        final int column = e.getColumn();

                        // Asumiendo que la columna 'Participa' es la 2 (índice 2)
                        if (column == 2) {
                            DefaultTableModel model = (DefaultTableModel) lotesTable.getModel();
                            // El ID del Lote está en la columna 0.
                            String idLote = (String) model.getValueAt(row, 0);
                            String participa = (String) model.getValueAt(row, 2);

                            // Si el usuario cambia a "No"
                            if ("No".equals(participa)) {
                                confirmarYEliminarArchivosLote("Lote " + idLote, row);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Pide confirmación al usuario para eliminar archivos de oferta asociados a un lote
     * que ha sido deseleccionado. Si el usuario cancela la eliminación, la participación
     * del lote se revierte a 'Sí'.
     *
     * @param idLote El identificador del lote (ej., "Lote 3").
     * @param row El índice de la fila en la tabla de lotes para poder revertir el valor.
     */
    private void confirmarYEliminarArchivosLote(String idLote, int row) {
        // 1. Verificar si hay archivos cargados para el lote.
        String prefix = idLote.replace(" ", "") + "_"; // Convertir "Lote 3" a "Lote3_"
        boolean hayArchivosCargados = parent.getFileManager().getArchivosOferta().keySet().stream()
                .anyMatch(key -> key.startsWith(prefix));

        // Si no hay archivos, simplemente aceptamos el cambio a "No" sin advertir y salimos.
        if (!hayArchivosCargados) {
            parent.getLogger().log("El lote " + idLote + " fue deseleccionado, pero no tenía archivos cargados. No se requiere limpieza.");
            return;
        }

        // 2. Advertir y pedir confirmación estricta
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "ADVERTENCIA: Ha deseleccionado el lote " + idLote + ". Se han detectado archivos de oferta cargados para este lote.\n\n"
                + "Si pulsa 'Sí', **LOS ARCHIVOS DETECTADOS SERÁN ELIMINADOS PERMANENTEMENTE**.\n\n"
                + "Si pulsa 'No', la participación en el lote " + idLote + " se mantendrá en 'Sí' y sus archivos no se borrarán.",
                "Confirmar Eliminación Obligatoria de Archivos",
                JOptionPane.YES_NO_OPTION, // Usamos YES_NO para forzar la elección
                JOptionPane.WARNING_MESSAGE
        );

        DefaultTableModel model = (DefaultTableModel) lotesTable.getModel();

        if (confirm == JOptionPane.YES_OPTION) {
            // Opción 1: SÍ, ELIMINAR (Se mantiene el "No" en la tabla y se borran archivos)
            boolean eliminado = parent.getFileManager().eliminarArchivosOfertaPorLote(idLote);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Archivos del lote " + idLote + " eliminados con éxito.",
                        "Limpieza Completa",
                        JOptionPane.INFORMATION_MESSAGE);
                parent.actualizarTablas(); // Actualiza las tablas de la MainWindow
                parent.getLogger().log("Archivos del lote " + idLote + " eliminados tras deselección (Sí a la advertencia).");
            }
            // El estado "No" ya está en la tabla, se mantiene.

        } else {
            // Opción 2: NO (Cancelar la eliminación/deselección). Revertimos la selección a "Sí".
            // Revertir el valor de la tabla a "Sí".
            // Esto desencadenará otro TableModelEvent, pero como el valor es "Sí",
            // el listener volverá a ejecutarse, verá que participa es "Sí" y no hará nada, evitando un bucle.
            model.setValueAt("Sí", row, 2);

            JOptionPane.showMessageDialog(this,
                    "La participación en el lote " + idLote + " se ha mantenido en 'Sí'. Los archivos cargados no han sido modificados.",
                    "Acción Cancelada",
                    JOptionPane.INFORMATION_MESSAGE);
            parent.getLogger().log("Operación de deselección de lote " + idLote + " cancelada por el usuario.");
        }
    }

    // ---------------------- MANEJO DE DATOS ----------------------
    /**
     * Carga los datos del {@link LicitadorData} actual (obtenido del {@link FileManager})
     * en los campos de texto y botones de radio del diálogo. Si no existen datos previos,
     * los campos se inicializan vacíos o con la opción "No" por defecto.
     */
    private void cargarDatosLicitadorAlDialogo() {
        LicitadorData data = fileManager.getLicitadorData();

        razonSocialField.setText(data.getRazonSocial() != null ? data.getRazonSocial() : "");
        nifField.setText(data.getNif() != null ? data.getNif() : "");
        domicilioField.setText(data.getDomicilio() != null ? data.getDomicilio() : "");
        emailField.setText(data.getEmail() != null ? data.getEmail() : "");
        telefonoField.setText(data.getTelefono() != null ? data.getTelefono() : "");

        // Radio Buttons (seleccionar 'No' por defecto si no hay datos)
        if (data.esPyme()) {
            pymeSiRadio.setSelected(true);
        } else {
            pymeNoRadio.setSelected(true);
        }

        if (data.esExtranjera()) {
            extranjeraSiRadio.setSelected(true);
        } else {
            extranjeraNoRadio.setSelected(true);
        }
    }

    // ---------------------- CREACIÓN DE PANELES UI ----------------------
    /**
     * Crea y devuelve el panel de interfaz de usuario para la entrada de datos del licitador.
     * @return Un {@code JPanel} que contiene los campos de texto y botones de radio del licitador.
     */
    private JPanel crearPanelLicitador() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Licitador"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Razón Social (obligatorio)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Razón Social*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(razonSocialField, gbc);

        // Fila 2: NIF (obligatorio)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("NIF/CIF*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nifField, gbc);

        // Fila 3: Domicilio (obligatorio)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Domicilio*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(domicilioField, gbc);

        // Fila 4: Email (obligatorio)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Email*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(emailField, gbc);

        // Fila 5: Teléfono (obligatorio)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Teléfono*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(telefonoField, gbc);

        // Fila 6: PYME
        JPanel pymePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pymePanel.add(pymeSiRadio);
        pymePanel.add(pymeNoRadio);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        panel.add(new JLabel("¿Es PYME?*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(pymePanel, gbc);

        // Fila 7: Extranjera
        JPanel extranjeraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        extranjeraPanel.add(extranjeraSiRadio);
        extranjeraPanel.add(extranjeraNoRadio);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        panel.add(new JLabel("¿Es Extranjera?*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(extranjeraPanel, gbc);

        return panel;
    }

    /**
     * Crea y devuelve el panel para la selección de lotes.
     * Muestra la tabla de lotes si la licitación los tiene, o un mensaje informativo en caso contrario.
     * @return Un {@code JPanel} que contiene la tabla de lotes o un mensaje.
     */
    private JPanel crearPanelLotes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Participación en Lotes"));

        if (configuracion.isTieneLotes()) {
            panel.add(new JScrollPane(lotesTable), BorderLayout.CENTER);
        } else {
            panel.add(new JLabel("Esta licitación no está dividida en lotes.", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        // Ajuste de tamaño preferido
        panel.setPreferredSize(new Dimension(500, configuracion.isTieneLotes() ? 200 : 80));
        return panel;
    }

    /**
     * Crea y devuelve el panel principal que organiza los paneles del licitador y los lotes.
     * @return Un {@code JPanel} que es el contenedor principal de la UI.
     */
    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(crearPanelLicitador());
        panel.add(Box.createVerticalStrut(15)); // Espacio entre paneles
        panel.add(crearPanelLotes());

        return panel;
    }

    /**
     * Crea y devuelve el panel que contiene los botones "Aceptar" y "Cancelar".
     * Incluye la lógica de los ActionListeners para guardar datos y validar.
     * @return Un {@code JPanel} con los botones de acción del diálogo.
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton aceptarButton = new JButton("Aceptar y Continuar");
        JButton cancelarButton = new JButton("Cancelar");

        // ActionListener para Aceptar: Sincroniza, Valida y Cierra
        aceptarButton.addActionListener(e -> {
            // 1. Llama al nuevo método público de MainWindow para que guarde los datos en el FileManager
            parent.guardarDatosLicitador(
                    razonSocialField.getText(),
                    nifField.getText(),
                    domicilioField.getText(),
                    emailField.getText(),
                    telefonoField.getText(),
                    pymeSiRadio.isSelected(),
                    extranjeraSiRadio.isSelected(),
                    (DefaultTableModel) lotesTable.getModel() // Pasa el modelo de la tabla
            );

            // 2. Ahora, valida los datos que ACABAMOS de guardar en el FileManager
            if (parent.validarDatosLicitador()) {
                // 3. Si la validación es OK, cierra el diálogo
                dispose();

                // 3.5. CRÍTICO: ACTUALIZAR las tablas de la MainWindow
                // Esto fuerza la MainWindow a redibujar la tabla de lotes solo con los seleccionados.
                parent.actualizarTablas();
            }
        });

        // ActionListener para Cancelar: Solo cierra el diálogo
        cancelarButton.addActionListener(e -> {
            dispose();
        });

        panel.add(aceptarButton);
        panel.add(cancelarButton);
        return panel;
    }
}