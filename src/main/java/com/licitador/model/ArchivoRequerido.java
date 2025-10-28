package com.licitador.model;

import java.io.Serializable;

/**
 * Representa un archivo o documento que es requerido para ser adjuntado por el
 * licitador en un procedimiento de licitación. Implementa {@code Serializable}
 * para poder ser guardado dentro del archivo JAR de configuración.
 */
public class ArchivoRequerido implements Serializable {

    /**
     * El nombre o descripción del archivo/documento (ej: 'Documento Oferta
     * Económica').
     */
    private final String nombre;

    /**
     * Indica si el archivo es de carga obligatoria para el licitador.
     */
    private final boolean obligatorio;

    /**
     * Indica si el archivo tiene carácter confidencial y requiere supuestos de
     * confidencialidad.
     */
    private final boolean esConfidencial;

    /**
     * Constructor para crear una instancia de un archivo requerido.
     *
     * @param nombre El nombre del archivo/documento.
     * @param obligatorio Indica si el archivo es obligatorio.
     * @param esConfidencial Indica si el archivo es confidencial.
     */
    public ArchivoRequerido(String nombre, boolean obligatorio, boolean esConfidencial) {
        this.nombre = nombre;
        this.obligatorio = obligatorio;
        this.esConfidencial = esConfidencial;
    }

    // Getters...
    /**
     * Obtiene el nombre del archivo requerido.
     *
     * @return El nombre del archivo.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Determina si la carga de este archivo es obligatoria.
     *
     * @return {@code true} si es obligatorio, {@code false} en caso contrario.
     */
    public boolean esObligatorio() {
        return obligatorio;
    }

    /**
     * Determina si este archivo ha sido marcado como confidencial.
     *
     * @return {@code true} si es confidencial, {@code false} en caso contrario.
     */
    public boolean esConfidencial() {
        return esConfidencial;
    }
}
