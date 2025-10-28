package com.licitador.ui;

import com.licitador.model.LicitadorData;
import com.licitador.model.LicitadorLotesData;
import com.licitador.service.Configuracion;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class LicitadorLotesDialog extends JDialog {
    
    // El objeto a editar y devolver
    private LicitadorLotesData licitadorLotesData; 
    
    private final Configuracion configuracion;
    private final boolean tieneLotes;
    private boolean datosGuardados = false; // Indica si se hizo clic en Aceptar

    // Campos de LicitadorData (para simplificar, usaremos JTextArea como ejemplo, 
    // pero deben ser JTextFiels, JCheckBox, etc., como tengas en tu LicitadorData Dialog)
    private JTextField razonSocialField;
    private JTextField nifField;
    private JCheckBox esPymeCheck;
    private JCheckBox esExtranjeraCheck;
    // ... otros campos para domicilio, teléfono, email

    // Componentes para la selección de lotes
    private JPanel lotesPanel;
    private Set<JCheckBox> loteCheckboxes; // Checkboxes para la selección

    public LicitadorLotesDialog(Frame owner, Configuracion configuracion, LicitadorLotesData datosIniciales) {
        super(owner, "Editar Datos del Licitador y Lotes", true);
        this.configuracion = configuracion;
        this.licitadorLotesData = datosIniciales;
        this.tieneLotes = configuracion.isTieneLotes();
        this.loteCheckboxes = new HashSet<>();

        // Configuración de la ventana
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Inicializar componentes
        initComponents();
        loadData();

        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // === 1. Panel de Datos del Licitador (Simulado) ===
        JPanel licitadorPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        licitadorPanel.setBorder(BorderFactory.createTitledBorder("Datos del Licitador"));

        razonSocialField = new JTextField(20);
        nifField = new JTextField(20);
        esPymeCheck = new JCheckBox("Es PYME");
        esExtranjeraCheck = new JCheckBox("Empresa Extranjera");

        licitadorPanel.add(new JLabel("Razón Social:"));
        licitadorPanel.add(razonSocialField);
        licitadorPanel.add(new JLabel("NIF:"));
        licitadorPanel.add(nifField);
        licitadorPanel.add(esPymeCheck);
        licitadorPanel.add(esExtranjeraCheck);
        
        // ... Añadir más campos de LicitadorData

        // === 2. Panel de Selección de Lotes ===
        lotesPanel = new JPanel();
        lotesPanel.setBorder(BorderFactory.createTitledBorder("Lotes a Licitar"));
        
        if (tieneLotes) {
            setupLotesCheckboxes();
        } else {
            // Modo Oferta Única
            lotesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            lotesPanel.add(new JLabel("Esta licitación es de Oferta Única."));
        }
        
        // === 3. Panel de Botones ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton aceptarButton = new JButton("Aceptar y Guardar");
        JButton cancelarButton = new JButton("Cancelar");
        
        aceptarButton.addActionListener(e -> onAceptar());
        cancelarButton.addActionListener(e -> dispose());
        
        buttonPanel.add(aceptarButton);
        buttonPanel.add(cancelarButton);

        // === Añadir Paneles al Diálogo ===
        JPanel centralPanel = new JPanel(new BorderLayout(10, 10));
        centralPanel.add(licitadorPanel, BorderLayout.NORTH);
        
        // Usar JScrollPane si hay muchos lotes
        centralPanel.add(new JScrollPane(lotesPanel), BorderLayout.CENTER); 
        
        add(centralPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(aceptarButton);
    }
    
    private void setupLotesCheckboxes() {
            // CORRECCIÓN CLAVE: Usar getNumLotes() en lugar de getLotes()

            // 1. Usamos GridLayout para organizar los lotes en una columna
            lotesPanel.setLayout(new GridLayout(configuracion.getNumLotes(), 1));

            for (int i = 1; i <= configuracion.getNumLotes(); i++) {
                // Asumimos que los lotes se nombran secuencialmente "Lote 1", "Lote 2", etc.
                String nombreLote = "Lote " + i;

                // Usamos el número de lote como ID (String)
                String loteId = String.valueOf(i); 

                JCheckBox cb = new JCheckBox(nombreLote);
                cb.setName(loteId); // Usamos el ID (número) como nombre para identificarlo al cargar/guardar

                // 🔥 CAMBIO CRÍTICO: Deshabilitar el checkbox.
                // La selección de participación se realiza en la tabla principal (MainWindow).
                // Este diálogo solo muestra el estado cargado.
                cb.setEnabled(false); 

                loteCheckboxes.add(cb);
                lotesPanel.add(cb);
            }
        }

    private void loadData() {
        LicitadorData licitador = licitadorLotesData.getLicitador();
        
        // Cargar datos del licitador
        razonSocialField.setText(licitador.getRazonSocial());
        nifField.setText(licitador.getNif());
        esPymeCheck.setSelected(licitador.esPyme());
        esExtranjeraCheck.setSelected(licitador.esExtranjera());
        // ... cargar resto de campos
        
        // Cargar selección de lotes
        if (tieneLotes) {
            Set<String> seleccionActual = licitadorLotesData.getLotesSeleccionadosIds();
            for (JCheckBox cb : loteCheckboxes) {
                if (seleccionActual.contains(cb.getName())) {
                    cb.setSelected(true);
                }
            }
        }
    }

    private void onAceptar() {
        // 1. Guardar LicitadorData
        LicitadorData nuevoLicitador = licitadorLotesData.getLicitador();
        nuevoLicitador.setRazonSocial(razonSocialField.getText());
        nuevoLicitador.setNif(nifField.getText());
        nuevoLicitador.setEsPyme(esPymeCheck.isSelected());
        nuevoLicitador.setEsExtranjera(esExtranjeraCheck.isSelected());
        // ... guardar el resto de campos
        
        // 2. Guardar Selección de Lotes
        if (tieneLotes) {
            Set<String> nuevaSeleccion = new HashSet<>();
            for (JCheckBox cb : loteCheckboxes) {
                if (cb.isSelected()) {
                    nuevaSeleccion.add(cb.getName());
                }
            }
            licitadorLotesData.setLotesSeleccionadosIds(nuevaSeleccion);
        } else {
             // Modo Oferta Única: Asumimos que siempre participa en la única oferta.
             // La lógica para la Oferta Única debe ser revisada en el sistema de archivos
             // para asegurar que siempre se crea el archivo de oferta cuando no hay lotes.
        }

        datosGuardados = true;
        dispose();
    }
    
    /**
     * Devuelve true si el diálogo se cerró haciendo clic en Aceptar.
     */
    public boolean fueGuardado() {
        return datosGuardados;
    }
    
    /**
     * Devuelve el objeto con los datos actualizados.
     */
    public LicitadorLotesData getLicitadorLotesData() {
        return licitadorLotesData;
    }
}