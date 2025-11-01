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
 * A modal dialog that allows the user to declare the confidentiality of a document
 * by selecting one or more legal assumptions and providing a mandatory textual
 * motivation for each chosen assumption.
 */
public class ConfidencialidadDialog extends JDialog {

    private final JCheckBox[] checkBoxes;
    private final JTextArea[] textAreas;
    private boolean confirmado = false;
    private final String[] supuestos;

    /**
     * Constructs and initializes the confidentiality declaration dialog.
     *
     * @param owner The parent dialog, which keeps it modal.
     * @param supuestos An array of strings with the names of the available legal assumptions.
     */
    public ConfidencialidadDialog(JDialog owner, String[] supuestos) {
        super(owner, "Declare Confidentiality", true);
        this.supuestos = supuestos;

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
            textAreas[i].setBorder(BorderFactory.createTitledBorder("Motivation"));
            JScrollPane scrollPane = new JScrollPane(textAreas[i]);
            add(scrollPane, gbc);

            final int index = i;
            checkBoxes[i].addActionListener(e -> {
                textAreas[index].setEnabled(checkBoxes[index].isSelected());
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
                        JOptionPane.showMessageDialog(this, "You must provide a motivation for the selected assumption: '" + checkBoxes[i].getText() + "'", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            confirmado = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
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
     * Returns the confirmation status of the dialog.
     *
     * @return {@code true} if the user clicked 'OK' and the validations passed;
     * {@code false} otherwise.
     */
    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Gets the selected confidentiality assumptions along with their associated motivations.
     *
     * @return A {@code Map<String, String>} where the key is the assumption (the
     * checkbox text) and the value is the textual motivation entered by the user.
     * It only includes the options that were selected.
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
     * Gets an array with the selected legal confidentiality assumptions. This is a
     * convenience method that extracts the keys from the map returned by
     * {@link #getConfidencialidadSeleccionada()}.
     *
     * @return An array of {@code String} with the selected assumptions.
     */
    public String[] getSupuestosSeleccionados() {
        return getConfidencialidadSeleccionada().keySet().toArray(new String[0]);
    }

    /**
     * Gets an array with the textual motivations entered by the user, in the same
     * order as the selected assumptions. This is a convenience method that extracts
     * the values from the map returned by {@link #getConfidencialidadSeleccionada()}.
     *
     * @return An array of {@code String} with the motivations for the selected assumptions.
     */
    public String[] getMotivosSupuestos() {
        return getConfidencialidadSeleccionada().values().toArray(new String[0]);
    }

}
