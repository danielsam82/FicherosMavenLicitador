package com.licitador.ui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Diálogo modal que permite al usuario declarar la confidencialidad de un
 * documento seleccionando uno o varios supuestos legales y proporcionando una
 * motivación textual obligatoria para cada supuesto elegido.
 */
public class ConfidencialidadDialog extends JDialog {

    /**
     * Array de {@code JCheckBox} correspondientes a los supuestos de
     * confidencialidad disponibles.
     */
    private final JCheckBox[] checkBoxes;
    /**
     * Array de {@code JTextArea} para introducir la motivación de
     * confidencialidad para cada supuesto (asociados uno a uno con
     * {@code checkBoxes}).
     */
    private final JTextArea[] textAreas;
    /**
     * Indicador de estado que es {@code true} si el usuario pulsó 'OK' y pasó
     * las validaciones, y {@code false} si pulsó 'Cancelar' o cerró el diálogo.
     */
    private boolean confirmado = false;
    /**
     * Array inmutable de los nombres de los supuestos legales de
     * confidencialidad.
     */
    private final String[] supuestos;

    /**
     * Constructor para inicializar el diálogo de declaración de
     * confidencialidad.
     *
     * @param owner El diálogo padre, que lo mantiene modal.
     * @param supuestos Array de cadenas con los nombres de los supuestos
     * legales disponibles.
     */
    public ConfidencialidadDialog(JDialog owner, String[] supuestos) {
        super(owner, "Declarar confidencialidad", true);
        this.supuestos = supuestos; // Guardamos los supuestos para poder usarlos

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        checkBoxes = new JCheckBox[supuestos.length];
        textAreas = new JTextArea[supuestos.length];

        for (int i = 0; i < supuestos.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            checkBoxes[i] = new JCheckBox(supuestos[i]);
            add(checkBoxes[i], gbc);

            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            textAreas[i] = new JTextArea(3, 30);
            textAreas[i].setEnabled(false);
            textAreas[i].setBorder(BorderFactory.createTitledBorder("Motivación"));
            JScrollPane scrollPane = new JScrollPane(textAreas[i]);
            add(scrollPane, gbc);

            final int index = i;
            checkBoxes[i].addActionListener(e -> {
                textAreas[index].setEnabled(checkBoxes[index].isSelected());
                // Borramos el texto si se deselecciona el checkbox
                if (!checkBoxes[index].isSelected()) {
                    textAreas[index].setText("");
                }
            });
        }

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    if (textAreas[i].getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Debe proporcionar una motivación para el supuesto seleccionado: '" + checkBoxes[i].getText() + "'", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            confirmado = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = supuestos.length;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Devuelve el estado de confirmación del diálogo.
     *
     * @return {@code true} si el usuario pulsó 'OK' y las validaciones pasaron;
     * {@code false} en caso contrario.
     */
    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Obtiene los supuestos de confidencialidad seleccionados junto con sus
     * motivaciones asociadas.
     *
     * @return Un {@code Map<String, String>} donde la clave es el supuesto (el
     * texto del checkbox) y el valor es la motivación textual introducida por
     * el usuario. Solo incluye las opciones que fueron seleccionadas.
     */
    public Map<String, String> getConfidencialidadSeleccionada() {
        Map<String, String> seleccion = new LinkedHashMap<>();
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                seleccion.put(checkBoxes[i].getText(), textAreas[i].getText());
            }
        }
        return seleccion;
    }

    /**
     * Obtiene un array con los supuestos legales de confidencialidad
     * seleccionados. Es un método de conveniencia que extrae las claves del
     * mapa devuelto por {@link #getConfidencialidadSeleccionada()}.
     *
     * @return Array de {@code String} con los supuestos seleccionados.
     */
    public String[] getSupuestosSeleccionados() {
        return getConfidencialidadSeleccionada().keySet().toArray(new String[0]);
    }

    /**
     * Obtiene un array con las motivaciones textuales introducidas por el
     * usuario, en el mismo orden que los supuestos seleccionados. Es un método
     * de conveniencia que extrae los valores del mapa devuelto por
     * {@link #getConfidencialidadSeleccionada()}.
     *
     * @return Array de {@code String} con las motivaciones de los supuestos
     * seleccionados.
     */
    public String[] getMotivosSupuestos() {
        return getConfidencialidadSeleccionada().values().toArray(new String[0]);
    }

}
