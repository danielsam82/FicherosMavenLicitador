package com.licitador.service;

import java.io.Serializable;

/**
 * A model class that encapsulates data related to the confidentiality declaration of a file.
 * <p>
 * It stores the selected legal assumptions and the motivations entered by the user to justify
 * the confidential nature of a document.
 * Implements {@code Serializable} to allow for its storage or transmission.
 * </p>
 */
public class ConfidencialidadData implements Serializable {
    
    /**
     * An array of names or codes of the selected legal confidentiality assumptions.
     */
    private String[] supuestosSeleccionados;
    
    /**
     * An array of the motivations or justifications entered by the user for each of the selected assumptions.
     */
    private String[] motivosSupuestos;

    /**
     * Constructs an instance of {@code ConfidencialidadData}.
     *
     * @param supuestosSeleccionados An array with the identifiers of the selected legal assumptions.
     * @param motivosSupuestos An array with the textual motivations justifying confidentiality.
     */
    public ConfidencialidadData(String[] supuestosSeleccionados, String[] motivosSupuestos) {
        this.supuestosSeleccionados = supuestosSeleccionados;
        this.motivosSupuestos = motivosSupuestos;
    }
    
    /**
     * Gets the array of identifiers of the selected legal confidentiality assumptions.
     *
     * @return An array of {@code String} with the selected assumptions.
     */
    public String[] getSupuestosSeleccionados() {
        return supuestosSeleccionados;
    }

    /**
     * Gets the array of motivations or justifications entered by the user.
     *
     * @return An array of {@code String} with the motivations.
     */
    public String[] getMotivosSupuestos() {
        return motivosSupuestos;
    }
}