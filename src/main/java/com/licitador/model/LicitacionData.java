package com.licitador.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Clase inmutable que almacena los datos de configuración esenciales de una
 * licitación, incluyendo el expediente, el objeto, si tiene lotes y la lista de
 * archivos comunes y documentos de oferta requeridos.
 *
 * Implementa {@link Serializable} para permitir el guardado y la carga de la
 * configuración.
 *
 * NOTA: Esta clase depende de {@link ArchivoRequerido}, la cual se asume
 * definida en algún lugar del proyecto.
 */
public class LicitacionData implements Serializable {

    private static final long serialVersionUID = 1L; // Recomendado para Serializable

    private final String expediente;
    private final String objeto;
    private final boolean tieneLotes;
    private final int numLotes;
    private final ArchivoRequerido[] archivosComunes;
    private final ArchivoRequerido[] documentosOferta;
    private final ArticuloAnexo[] anexosAdministrativos;

    /**
     * Constructor que inicializa todos los campos de datos de la licitación.
     *
     * @param expediente El número o código del expediente de la licitación.
     * @param objeto La descripción del objeto del contrato.
     * @param tieneLotes {@code true} si la licitación está dividida en lotes;
     * {@code false} en caso contrario.
     * @param numLotes El número total de lotes, o 1 si no tiene lotes.
     * @param archivosComunes Un array con la configuración de los archivos
     * comunes requeridos.
     * @param documentosOferta Un array con la configuración de los documentos
     * de oferta requeridos (por ejemplo, oferta económica, técnica).
     */
    public LicitacionData(String expediente, String objeto, boolean tieneLotes, int numLotes,
            ArchivoRequerido[] archivosComunes, ArchivoRequerido[] documentosOferta,
            ArticuloAnexo[] anexosAdministrativos) {
        this.expediente = expediente;
        this.objeto = objeto;
        this.tieneLotes = tieneLotes;
        this.numLotes = numLotes;
        this.archivosComunes = archivosComunes;
        this.documentosOferta = documentosOferta;
        this.anexosAdministrativos = anexosAdministrativos; // Campo nuevo
    }

    // Getters
    /**
     * Obtiene el número o código del expediente de la licitación.
     *
     * @return El expediente.
     */
    public String getExpediente() {
        return expediente;
    }

    /**
     * Obtiene la descripción del objeto del contrato.
     *
     * @return El objeto de la licitación.
     */
    public String getObjeto() {
        return objeto;
    }

    /**
     * Indica si la licitación está dividida en lotes.
     *
     * @return {@code true} si tiene lotes; {@code false} en caso contrario.
     */
    public boolean tieneLotes() {
        return tieneLotes;
    }

    /**
     * Obtiene el número total de lotes.
     *
     * @return El número de lotes (mayor o igual a 1).
     */
    public int getNumLotes() {
        return numLotes;
    }

    /**
     * Obtiene el array de archivos comunes requeridos para la licitación.
     *
     * @return Un array de objetos {@code ArchivoRequerido}.
     */
    public ArchivoRequerido[] getArchivosComunes() {
        return archivosComunes;
    }

    /**
     * Obtiene el array de documentos de oferta requeridos para la licitación.
     *
     * @return Un array de objetos {@code ArchivoRequerido}.
     */
    public ArchivoRequerido[] getDocumentosOferta() {
        return documentosOferta;
    }

    /**
     * Devuelve una representación en cadena de texto de esta instancia de
     * {@code LicitacionData}, mostrando todos sus atributos.
     *
     * @return Una cadena de texto formateada con los datos de la licitación.
     */
    @Override
    public String toString() {
        return String.format(
                "LicitacionData[expediente=%s, objeto=%s, lotes=%s/%d, archivos=%s, documentos=%s]",
                expediente, objeto, tieneLotes, numLotes,
                Arrays.toString(archivosComunes),
                Arrays.toString(documentosOferta)
        );
    }

    /**
     * Obtiene el array de artículos del anexo administrativo. Es un alias para
     * getAnexosAdministrativos() usado por el AnexoGenerator.
     *
     * @return Un array de objetos ArticuloAnexo.
     */
    public ArticuloAnexo[] getArticulosAnexos() {
        return anexosAdministrativos;
    }
}
