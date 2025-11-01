package com.licitador.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects; 

/**
 * Represents a modular fragment (Article) to be assembled into the Global Adhesion Annex.
 * Includes fields for conditional interactivity.
 */
public class ArticuloAnexo implements Serializable {

    private static final long serialVersionUID = 2L;

    /** Unique identifier (e.g., "Art_1_Solvencia"). */
    private String idArticulo;
    /** Position within the Global Annex (1, 2, 3...). */
    private int orden;
    /** Title of the article. */
    private String titulo;
    /** Text of the fragment (with tags like <DATO_LICITADOR ETQ="...">). */
    private String contenidoFormato;
    /** Whether the bidder must specifically sign this fragment. */
    private boolean requiereFirma;

    // --- INTERACTIVITY FIELDS ---
    /** The Yes/No question (empty if not interactive). */
    private String preguntaInteractiva;
    /** Action to take if the answer is 'Yes': "NINGUNA", "PEDIR_CAMPOS", "PEDIR_FICHERO". */
    private String accionSi;
    /** Names of the labels for the fields to be filled (max 4). */
    private String[] etiquetasCampos;
    
    /** Constant for no action. */
    public static final String ACCION_NINGUNA = "NINGUNA";
    /** Constant for requesting fields to be filled. */
    public static final String ACCION_PEDIR_CAMPOS = "PEDIR_CAMPOS";
    /** Constant for requesting a file to be uploaded. */
    public static final String ACCION_PEDIR_FICHERO = "PEDIR_FICHERO";

    /**
     * Full constructor to create an instance of an annex article.
     *
     * @param idArticulo Unique identifier.
     * @param orden Position within the annex.
     * @param titulo Title of the article.
     * @param contenidoFormato Text content with placeholders.
     * @param requiereFirma Whether a specific signature is required.
     * @param preguntaInteractiva The Yes/No question for interactivity.
     * @param accionSi The action to take if the answer is 'Yes'.
     * @param etiquetasCampos The labels for the fields to be filled.
     */
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

    /**
     * Basic constructor for compatibility and quick creation.
     * Initializes interactivity fields to default values.
     */
    public ArticuloAnexo() {
        this.preguntaInteractiva = "";
        this.accionSi = ACCION_NINGUNA;
        this.etiquetasCampos = new String[0];
    }
    
    /**
     * Returns true if the article has a defined question, making it interactive.
     *
     * @return true if the article is interactive, false otherwise.
     */
    public boolean esInteractivo() {
        return preguntaInteractiva != null && !preguntaInteractiva.trim().isEmpty();
    }

    /**
     * Gets the interactive question.
     * @return The question string.
     */
    public String getPreguntaInteractiva() {
        return preguntaInteractiva;
    }

    /**
     * Sets the interactive question.
     * @param preguntaInteractiva The question string.
     */
    public void setPreguntaInteractiva(String preguntaInteractiva) {
        this.preguntaInteractiva = preguntaInteractiva;
    }

    /**
     * Gets the action to be performed if the answer is 'Yes'.
     * @return The action string.
     */
    public String getAccionSi() {
        return accionSi;
    }

    /**
     * Sets the action to be performed if the answer is 'Yes'.
     * @param accionSi The action string.
     */
    public void setAccionSi(String accionSi) {
        this.accionSi = accionSi;
    }

    /**
     * Gets the labels of the fields to be filled.
     * @return An array of strings with the labels.
     */
    public String[] getEtiquetasCampos() {
        return etiquetasCampos;
    }

    /**
     * Sets the labels of the fields to be filled.
     * @param etiquetasCampos An array of strings with the labels.
     */
    public void setEtiquetasCampos(String[] etiquetasCampos) {
        this.etiquetasCampos = etiquetasCampos;
    }

    // --- BASE GETTERS AND SETTERS ---

    /** @return The unique ID of the article. */
    public String getIdArticulo() { return idArticulo; }
    /** @param idArticulo The unique ID of the article. */
    public void setIdArticulo(String idArticulo) { this.idArticulo = idArticulo; }
    /** @return The order of the article. */
    public int getOrden() { return orden; }
    /** @param orden The order of the article. */
    public void setOrden(int orden) { this.orden = orden; }
    /** @return The title of the article. */
    public String getTitulo() { return titulo; }
    /** @param titulo The title of the article. */
    public void setTitulo(String titulo) { this.titulo = titulo; }
    /** @return The formatted content of the article. */
    public String getContenidoFormato() { return contenidoFormato; }
    /** @param contenidoFormato The formatted content of the article. */
    public void setContenidoFormato(String contenidoFormato) { this.contenidoFormato = contenidoFormato; }
    /** @return true if a signature is required, false otherwise. */
    public boolean isRequiereFirma() { return requiereFirma; }
    /** @param requiereFirma true if a signature is required, false otherwise. */
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