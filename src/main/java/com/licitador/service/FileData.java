package com.licitador.service;

import java.io.Serializable;
import java.util.Objects;
import java.util.Arrays; // Incluido para coherencia, aunque no se usa directamente en esta clase

/**
 * A model class (Data Transfer Object - DTO) that encapsulates the information and
 * binary content of a file attached to the tender.
 * <p>
 * Implements {@code Serializable} for persistence. It stores the file's content
 * as a byte array and handles confidentiality properties, including the selected
 * legal assumptions and their associated reasons.
 * </p>
 */
public class FileData implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final byte[] contenido;
    private boolean esConfidencial;
    private final String[] supuestosSeleccionados;
    private final String[] motivosSupuestos;

    /**
     * Constructor for files that are **not** susceptible to being confidential
     * or have not been marked as such.
     *
     * @param nombre The name of the file.
     * @param contenido The binary content of the file.
     */
    public FileData(String nombre, byte[] contenido) {
        this(nombre, contenido, false, null, null);
    }

    /**
     * Main constructor to initialize a {@code FileData} object, including its
     * confidentiality status and associated details.
     *
     * @param nombre The name of the file.
     * @param contenido The binary content of the file.
     * @param esConfidencial Indicates whether the file should be marked as confidential.
     * @param supuestosSeleccionados An array of selected assumptions. Should be
     * {@code null} if {@code esConfidencial} is {@code false}.
     * @param motivosSupuestos An array of associated reasons. Should be {@code null}
     * if {@code esConfidencial} is {@code false}.
     * @throws NullPointerException If the name or content are {@code null}.
     */
    public FileData(String nombre, byte[] contenido, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        this.nombre = Objects.requireNonNull(nombre, "Name cannot be null");
        this.contenido = Objects.requireNonNull(contenido, "Content cannot be null");
        this.esConfidencial = esConfidencial;

        if (esConfidencial) {
            this.supuestosSeleccionados = (supuestosSeleccionados != null) ? supuestosSeleccionados : new String[0];
            this.motivosSupuestos = (motivosSupuestos != null) ? motivosSupuestos : new String[0];
        } else {
            this.supuestosSeleccionados = null;
            this.motivosSupuestos = null;
        }
    }

    /**
     * Gets the array of keys for the selected confidentiality assumptions.
     * Returns {@code null} if the file is not confidential.
     *
     * @return An array of {@code String} or {@code null}.
     */
    public String[] getSupuestosSeleccionados() {
        return supuestosSeleccionados;
    }

    /**
     * Gets the array of application reasons provided for the selected assumptions.
     * Returns {@code null} if the file is not confidential.
     *
     * @return An array of {@code String} or {@code null}.
     */
    public String[] getMotivosSupuestos() {
        return motivosSupuestos;
    }

    /**
     * Gets the full name of the file (including extension).
     *
     * @return The name of the file.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Gets the binary content of the file.
     *
     * @return A byte array with the content.
     */
    public byte[] getContenido() {
        return contenido;
    }

    /**
     * Checks if the file has been marked as confidential.
     *
     * @return {@code true} if the file is confidential, {@code false} otherwise.
     */
    public boolean esConfidencial() {
        return esConfidencial;
    }

    /**
     * Extracts the file extension from its name.
     *
     * @return The extension as a {@code String} (without the dot), or an empty
     * string if it has no extension.
     */
    public String getExtension() {
        int lastDotIndex = nombre.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < nombre.length() - 1) {
            return nombre.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Extracts the file name without the extension.
     *
     * @return The file name without the extension. If there is no extension,
     * it returns the full name.
     */
    public String getNombreSinExtension() {
        int lastDotIndex = nombre.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return nombre.substring(0, lastDotIndex);
        }
        return nombre;
    }
}
