package com.licitador.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Arrays;

/**
 * Representa un fragmento modular (Artículo) que se ensamblará
 * en el Anexo Global de Adhesión.
 * ADAPTACIÓN: Soporta contenido condicional (Sí/No) para artículos interactivos.
 */
public class ArticuloAnexo implements Serializable {

    private static final long serialVersionUID = 3L; // Incrementamos versión

    // --- CAMPOS BASE ---
    private String idArticulo;
    private int orden;
    private String titulo;
    
    // --- CAMPOS DE INTERACTIVIDAD ---
    private boolean esInteractivo; // True si requiere pregunta Sí/No
    private String preguntaInteractiva; // La pregunta (ej: "¿Pertenece a grupo empresarial?")
    
    // --- CONTENIDO CONDICIONAL ---
    // Texto si la respuesta es SÍ (o si no es interactivo)
    private String contenidoFormato; 
    // NUEVO: Texto si la respuesta es NO
    private String contenidoFormatoRespuestaNo; 
    
    // --- ACCIONES (Solo si la respuesta es SÍ) ---
    private String accionSi; // "NINGUNA", "PEDIR_CAMPOS", "PEDIR_FICHERO"
    private String[] etiquetasCampos; // Máx 4

    private boolean requiereFirma;
    
    // Constantes de Acción
    public static final String ACCION_NINGUNA = "NINGUNA";
    public static final String ACCION_PEDIR_CAMPOS = "PEDIR_CAMPOS";
    public static final String ACCION_PEDIR_FICHERO = "PEDIR_FICHERO";

    // Constructor completo
    public ArticuloAnexo(String idArticulo, int orden, String titulo, 
                         boolean esInteractivo, String preguntaInteractiva, 
                         String contenidoFormato, String contenidoFormatoRespuestaNo,
                         String accionSi, String[] etiquetasCampos, boolean requiereFirma) {
        this.idArticulo = idArticulo;
        this.orden = orden;
        this.titulo = titulo;
        this.esInteractivo = esInteractivo;
        this.preguntaInteractiva = preguntaInteractiva;
        this.contenidoFormato = contenidoFormato;
        this.contenidoFormatoRespuestaNo = contenidoFormatoRespuestaNo;
        this.accionSi = accionSi;
        this.etiquetasCampos = etiquetasCampos;
        this.requiereFirma = requiereFirma;
    }

    // Constructor vacío
    public ArticuloAnexo() {
        this.accionSi = ACCION_NINGUNA;
        this.etiquetasCampos = new String[0];
    }
    
    // --- Getters y Setters ---

    public String getIdArticulo() { return idArticulo; }
    public void setIdArticulo(String idArticulo) { this.idArticulo = idArticulo; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getContenidoFormato() { return contenidoFormato; } // Contenido para "SÍ"
    public void setContenidoFormato(String contenidoFormato) { this.contenidoFormato = contenidoFormato; }
    public String getContenidoFormatoRespuestaNo() { return contenidoFormatoRespuestaNo; } // Contenido para "NO"
    public void setContenidoFormatoRespuestaNo(String c) { this.contenidoFormatoRespuestaNo = c; }
    public boolean isRequiereFirma() { return requiereFirma; }
    public void setRequiereFirma(boolean requiereFirma) { this.requiereFirma = requiereFirma; }
    public boolean esInteractivo() { return esInteractivo; }
    public void setEsInteractivo(boolean esInteractivo) { this.esInteractivo = esInteractivo; }
    public String getPreguntaInteractiva() { return preguntaInteractiva; }
    public void setPreguntaInteractiva(String preguntaInteractiva) { this.preguntaInteractiva = preguntaInteractiva; }
    public String getAccionSi() { return accionSi; }
    public void setAccionSi(String accionSi) { this.accionSi = accionSi; }
    public String[] getEtiquetasCampos() { return etiquetasCampos; }
    public void setEtiquetasCampos(String[] etiquetasCampos) { this.etiquetasCampos = etiquetasCampos; }

    // Métodos de Objeto
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticuloAnexo that = (ArticuloAnexo) o;
        return Objects.equals(idArticulo, that.idArticulo);
    }
    @Override
    public int hashCode() { return Objects.hash(idArticulo); }
    @Override
    public String toString() {
        return "(" + orden + ") " + titulo + (esInteractivo ? " [INTERACTIVO]" : "");
    }
}