package com.licitador.model;

import java.io.Serializable;

/**
 * Represents a file or document that is required to be attached by the bidder in a tender process.
 * Implements {@code Serializable} to be saved within the configuration JAR file.
 */
public class ArchivoRequerido implements Serializable {

    /**
     * The name or description of the file/document (e.g., 'Economic Offer Document').
     */
    private final String nombre;

    /**
     * Indicates whether the file is mandatory for the bidder to upload.
     */
    private final boolean obligatorio;

    /**
     * Indicates whether the file is confidential and requires confidentiality assumptions.
     */
    private final boolean esConfidencial;

    /**
     * Constructs a new instance of a required file.
     *
     * @param nombre The name of the file/document.
     * @param obligatorio Indicates whether the file is mandatory.
     * @param esConfidencial Indicates whether the file is confidential.
     */
    public ArchivoRequerido(String nombre, boolean obligatorio, boolean esConfidencial) {
        this.nombre = nombre;
        this.obligatorio = obligatorio;
        this.esConfidencial = esConfidencial;
    }

    /**
     * Gets the name of the required file.
     *
     * @return The name of the file.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Determines whether the upload of this file is mandatory.
     *
     * @return {@code true} if it is mandatory, {@code false} otherwise.
     */
    public boolean esObligatorio() {
        return obligatorio;
    }

    /**
     * Determines whether this file has been marked as confidential.
     *
     * @return {@code true} if it is confidential, {@code false} otherwise.
     */
    public boolean esConfidencial() {
        return esConfidencial;
    }
}
