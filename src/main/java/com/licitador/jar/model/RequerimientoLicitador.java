// Archivo: src/main/java/com/licitador/jar/model/RequerimientoLicitador.java
package com.licitador.jar.model;

import com.licitador.model.ArticuloAnexo; // Importa desde tu modelo compartido
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Representa una solicitud de datos o acción dirigida al Licitador,
 * extraída de un ArticuloAnexo interactivo, junto con sus respuestas.
 */
public class RequerimientoLicitador implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- INSTRUCCIONES DEL CONFIGURADOR (NO CAMBIAN) ---
    private final String idArticulo;
    private final String tituloArticulo;
    private final String pregunta;
    private final String accionSi;
    private final String[] etiquetasCampos; // Solo si accionSi == PEDIR_CAMPOS

    // --- RESPUESTAS DEL LICITADOR (se rellenarán más tarde en la GUI) ---
    private boolean respuestaSi; // true si el licitador responde 'Sí' a la pregunta
    private Map<String, String> valoresCampos; // Map<Etiqueta, Valor> si accionSi == PEDIR_CAMPOS
    private String rutaFichero; // Ruta completa del fichero subido si accionSi == PEDIR_FICHERO

    // Constructor para inicializar las instrucciones del requerimiento
    public RequerimientoLicitador(String idArticulo, String tituloArticulo, String pregunta,
                                  String accionSi, String[] etiquetasCampos) {
        this.idArticulo = idArticulo;
        this.tituloArticulo = tituloArticulo;
        this.pregunta = pregunta;
        this.accionSi = accionSi;
        this.etiquetasCampos = (etiquetasCampos != null) ? Arrays.copyOf(etiquetasCampos, etiquetasCampos.length) : new String[0];
        // Inicializar respuestas por defecto
        this.respuestaSi = false;
        this.valoresCampos = new HashMap<>();
        this.rutaFichero = "";
    }

    // --- Getters para las instrucciones ---
    public String getIdArticulo() { return idArticulo; }
    public String getTituloArticulo() { return tituloArticulo; }
    public String getPregunta() { return pregunta; }
    public String getAccionSi() { return accionSi; }
    public String[] getEtiquetasCampos() { return Arrays.copyOf(etiquetasCampos, etiquetasCampos.length); }

    // --- Getters y Setters para las respuestas del Licitador ---
    public boolean isRespuestaSi() { return respuestaSi; }
    public void setRespuestaSi(boolean respuestaSi) { this.respuestaSi = respuestaSi; }

    public Map<String, String> getValoresCampos() { return valoresCampos; }
    public void setValoresCampos(Map<String, String> valoresCampos) { this.valoresCampos = valoresCampos; }

    public String getRutaFichero() { return rutaFichero; }
    public void setRutaFichero(String rutaFichero) { this.rutaFichero = rutaFichero; }

    @Override
    public String toString() {
        return "Requerimiento para '" + tituloArticulo + "': " + pregunta;
    }
}