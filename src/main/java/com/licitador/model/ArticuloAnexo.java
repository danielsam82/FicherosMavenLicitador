package com.licitador.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects; 

/**
 * Representa un fragmento modular (Artículo) que se ensamblará
 * en el Anexo Global de Adhesión.
 * * ADAPTACIÓN: Incluye campos para interactividad condicional.
 */
public class ArticuloAnexo implements Serializable {

    private static final long serialVersionUID = 2L; // Se recomienda incrementar el SerialVersionUID al añadir campos

    // --- CAMPOS BASE EXISTENTES ---
    private String idArticulo;                  // Identificador único (ej: "Art_1_Solvencia")
    private int orden;                          // Posición dentro del Anexo Global (1, 2, 3...)
    private String titulo;                      // Título del artículo
    private String contenidoFormato;            // Texto del fragmento (con etiquetas <DATO_LICITADOR ETQ="...">)
    private boolean requiereFirma;              // Si el licitador debe firmar específicamente este fragmento

    // --- NUEVOS CAMPOS DE INTERACTIVIDAD ---
    private String preguntaInteractiva;         // La pregunta Sí/No (vacío si no es interactivo)
    private String accionSi;                    // Acción en caso de responder 'Sí': "NINGUNA", "PEDIR_CAMPOS", "PEDIR_FICHERO"
    private String[] etiquetasCampos;           // Nombres de las etiquetas de los campos a rellenar (máx. 4)
    
    // Constantes para el campo accionSi (hace el código más seguro)
    public static final String ACCION_NINGUNA = "NINGUNA";
    public static final String ACCION_PEDIR_CAMPOS = "PEDIR_CAMPOS";
    public static final String ACCION_PEDIR_FICHERO = "PEDIR_FICHERO";

    // 1. CONSTRUCTOR COMPLETO (Actualizado)
    public ArticuloAnexo(String idArticulo, int orden, String titulo, String contenidoFormato, boolean requiereFirma,
                         String preguntaInteractiva, String accionSi, String[] etiquetasCampos) {
        this.idArticulo = idArticulo;
        this.orden = orden;
        this.titulo = titulo;
        this.contenidoFormato = contenidoFormato;
        this.requiereFirma = requiereFirma;
        this.preguntaInteractiva = preguntaInteractiva;
        this.accionSi = accionSi;
        this.etiquetasCampos = etiquetasCampos;
    }

    // 2. CONSTRUCTOR BÁSICO (Para compatibilidad y creación rápida)
    public ArticuloAnexo() {
        // Valores por defecto para interactividad
        this.preguntaInteractiva = "";
        this.accionSi = ACCION_NINGUNA;
        this.etiquetasCampos = new String[0];
    }
    
    // 3. GETTERS Y SETTERS (Nuevos campos)
    
    /**
     * Devuelve true si el artículo tiene una pregunta definida.
     */
    public boolean esInteractivo() {
        return preguntaInteractiva != null && !preguntaInteractiva.trim().isEmpty();
    }

    public String getPreguntaInteractiva() {
        return preguntaInteractiva;
    }

    public void setPreguntaInteractiva(String preguntaInteractiva) {
        this.preguntaInteractiva = preguntaInteractiva;
    }

    public String getAccionSi() {
        return accionSi;
    }

    public void setAccionSi(String accionSi) {
        this.accionSi = accionSi;
    }

    public String[] getEtiquetasCampos() {
        return etiquetasCampos;
    }

    public void setEtiquetasCampos(String[] etiquetasCampos) {
        this.etiquetasCampos = etiquetasCampos;
    }

    // --- GETTERS Y SETTERS BASE (Sin cambios) ---

    public String getIdArticulo() { return idArticulo; }
    public void setIdArticulo(String idArticulo) { this.idArticulo = idArticulo; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getContenidoFormato() { return contenidoFormato; }
    public void setContenidoFormato(String contenidoFormato) { this.contenidoFormato = contenidoFormato; }
    public boolean isRequiereFirma() { return requiereFirma; }
    public void setRequiereFirma(boolean requiereFirma) { this.requiereFirma = requiereFirma; }

    // 4. MÉTODOS DE OBJETO

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticuloAnexo that = (ArticuloAnexo) o;
        return Objects.equals(idArticulo, that.idArticulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idArticulo);
    }

    @Override
    public String toString() {
        return "(" + orden + ") " + titulo + (esInteractivo() ? " [INTERACTIVO]" : "");
    }
}