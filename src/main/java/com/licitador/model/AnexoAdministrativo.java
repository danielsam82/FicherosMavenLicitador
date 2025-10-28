package com.licitador.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa un anexo administrativo que debe ser aceptado por el licitador.
 * Contiene la plantilla (en formato String con posibles tags XML) y metadatos.
 */
public class AnexoAdministrativo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;              // Identificador único (ej: "Anexo_I_Declaracion")
    private String titulo;          // Título descriptivo para la GUI (ej: "Declaración Responsable")
    private String plantillaXML;    // La plantilla del anexo con las etiquetas de cumplimentación.
    private boolean requiereDatosAdicionales; // Indica si el licitador debe rellenar campos extra.

    // 1. CONSTRUCTOR COMPLETO
    public AnexoAdministrativo(String id, String titulo, String plantillaXML, boolean requiereDatosAdicionales) {
        this.id = id;
        this.titulo = titulo;
        this.plantillaXML = plantillaXML;
        this.requiereDatosAdicionales = requiereDatosAdicionales;
    }

    // 2. CONSTRUCTOR VACÍO (Necesario a menudo en frameworks de persistencia/deserialización)
    public AnexoAdministrativo() {
        // Constructor por defecto
    }

    // 3. GETTERS Y SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPlantillaXML() {
        return plantillaXML;
    }

    public void setPlantillaXML(String plantillaXML) {
        this.plantillaXML = plantillaXML;
    }

    public boolean getRequiereDatosAdicionales() {
        return requiereDatosAdicionales;
    }

    public void setRequiereDatosAdicionales(boolean requiereDatosAdicionales) {
        this.requiereDatosAdicionales = requiereDatosAdicionales;
    }

    // Usamos el id para comparar anexos
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnexoAdministrativo that = (AnexoAdministrativo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
