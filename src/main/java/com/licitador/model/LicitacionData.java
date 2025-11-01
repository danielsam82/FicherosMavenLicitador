package com.licitador.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An immutable class that stores the essential configuration data of a tender,
 * including the file number, the object, whether it has lots, and the list of
 * required common files and offer documents.
 * <p>
 * Implements {@link Serializable} to allow saving and loading of the configuration.
 * <p>
 * NOTE: This class depends on {@link ArchivoRequerido}, which is assumed to be
 * defined elsewhere in the project.
 */
public class LicitacionData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String expediente;
    private final String objeto;
    private final boolean tieneLotes;
    private final int numLotes;
    private final ArchivoRequerido[] archivosComunes;
    private final ArchivoRequerido[] documentosOferta;
    private final ArticuloAnexo[] anexosAdministrativos;

    /**
     * Constructor that initializes all the data fields of the tender.
     *
     * @param expediente The file number or code of the tender.
     * @param objeto The description of the contract's object.
     * @param tieneLotes {@code true} if the tender is divided into lots;
     * {@code false} otherwise.
     * @param numLotes The total number of lots, or 1 if it has no lots.
     * @param archivosComunes An array with the configuration of the required
     * common files.
     * @param documentosOferta An array with the configuration of the required
     * offer documents (e.g., economic, technical offer).
     * @param anexosAdministrativos An array with the administrative annex articles.
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
        this.anexosAdministrativos = anexosAdministrativos;
    }

    /**
     * Gets the file number or code of the tender.
     *
     * @return The file number.
     */
    public String getExpediente() {
        return expediente;
    }

    /**
     * Gets the description of the contract's object.
     *
     * @return The object of the tender.
     */
    public String getObjeto() {
        return objeto;
    }

    /**
     * Indicates whether the tender is divided into lots.
     *
     * @return {@code true} if it has lots; {@code false} otherwise.
     */
    public boolean tieneLotes() {
        return tieneLotes;
    }

    /**
     * Gets the total number of lots.
     *
     * @return The number of lots (greater than or equal to 1).
     */
    public int getNumLotes() {
        return numLotes;
    }

    /**
     * Gets the array of common files required for the tender.
     *
     * @return An array of {@code ArchivoRequerido} objects.
     */
    public ArchivoRequerido[] getArchivosComunes() {
        return archivosComunes;
    }

    /**
     * Gets the array of offer documents required for the tender.
     *
     * @return An array of {@code ArchivoRequerido} objects.
     */
    public ArchivoRequerido[] getDocumentosOferta() {
        return documentosOferta;
    }

    /**
     * Returns a string representation of this {@code LicitacionData} instance,
     * showing all its attributes.
     *
     * @return A formatted string with the tender's data.
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
     * Gets the array of administrative annex articles. It's an alias for
     * getAnexosAdministrativos() used by AnexoGenerator.
     *
     * @return An array of ArticuloAnexo objects.
     */
    public ArticuloAnexo[] getArticulosAnexos() {
        return anexosAdministrativos;
    }
}
