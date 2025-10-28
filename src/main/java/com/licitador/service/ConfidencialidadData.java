package com.licitador.service;

import java.io.Serializable;

/**
 * Clase de modelo que encapsula los datos relacionados con la declaración de
 * confidencialidad de un archivo.
 * <p>
 * Almacena los supuestos legales seleccionados y las motivaciones introducidas
 * por el usuario para justificar el carácter confidencial de un documento.
 * Implementa {@code Serializable} para permitir su almacenamiento o transmisión.
 * </p>
 */
public class ConfidencialidadData implements Serializable {
    
    /**
     * Array de nombres o códigos de los supuestos legales de confidencialidad seleccionados.
     */
    private String[] supuestosSeleccionados;
    
    /**
     * Array de las motivaciones o justificaciones introducidas por el usuario
     * para cada uno de los supuestos seleccionados.
     */
    private String[] motivosSupuestos;

    /**
     * Constructor para crear una instancia de {@code ConfidencialidadData}.
     *
     * @param supuestosSeleccionados Array con los identificadores de los supuestos legales seleccionados.
     * @param motivosSupuestos Array con las motivaciones textuales que justifican la confidencialidad.
     */
    public ConfidencialidadData(String[] supuestosSeleccionados, String[] motivosSupuestos) {
        this.supuestosSeleccionados = supuestosSeleccionados;
        this.motivosSupuestos = motivosSupuestos;
    }

    // Getters
    
    /**
     * Obtiene el array de identificadores de los supuestos legales de confidencialidad seleccionados.
     *
     * @return Array de {@code String} con los supuestos seleccionados.
     */
    public String[] getSupuestosSeleccionados() {
        return supuestosSeleccionados;
    }

    /**
     * Obtiene el array de motivaciones o justificaciones introducidas por el usuario.
     *
     * @return Array de {@code String} con las motivaciones.
     */
    public String[] getMotivosSupuestos() {
        return motivosSupuestos;
    }
}