package com.licitador.service;

import java.io.Serializable;
import java.util.Objects;
import java.util.Arrays;

/**
 * A model class that encapsulates the entire configuration of a tender procedure.
 * <p>
 * It includes identification data, lot structure, and the list of all required files
 * (common and offer), along with their mandatory and confidentiality properties.
 * Implements {@code Serializable} for persistence.
 * </p>
 */
public class Configuracion implements Serializable {

    private final String objetoLicitacion;
    private final String numeroExpediente;
    private final boolean tieneLotes;
    private final int numLotes;
    private final String[] nombresArchivosComunes;
    private final boolean[] archivosComunesObligatorios;
    private final boolean[] archivosComunesConfidenciales;
    private final ArchivoOferta[] archivosOferta;
    private final String[] supuestosConfidencialidad;

    /**
     * Constructor to initialize a new tender configuration.
     *
     * @param objetoLicitacion The object of the tender.
     * @param numeroExpediente The file number.
     * @param tieneLotes {@code true} if it has lots.
     * @param numLotes The number of lots.
     * @param nombresArchivosComunes The names of the common files.
     * @param archivosComunesObligatorios The mandatory status of the common files.
     * @param archivosComunesConfidenciales The confidentiality susceptibility of the common files.
     * @param archivosOferta An array of offer file configurations.
     * @param supuestosConfidencialidad A list of confidentiality assumptions.
     */
    public Configuracion(String objetoLicitacion, String numeroExpediente, boolean tieneLotes, int numLotes,
            String[] nombresArchivosComunes, boolean[] archivosComunesObligatorios,
            boolean[] archivosComunesConfidenciales,
            ArchivoOferta[] archivosOferta, String[] supuestosConfidencialidad) {
        this.objetoLicitacion = Objects.requireNonNull(objetoLicitacion);
        this.numeroExpediente = Objects.requireNonNull(numeroExpediente);
        this.tieneLotes = tieneLotes;
        this.numLotes = numLotes;
        this.nombresArchivosComunes = Objects.requireNonNull(nombresArchivosComunes);
        this.archivosComunesObligatorios = archivosComunesObligatorios;
        this.archivosComunesConfidenciales = Objects.requireNonNull(archivosComunesConfidenciales);
        this.archivosOferta = Objects.requireNonNull(archivosOferta);
        this.supuestosConfidencialidad = Objects.requireNonNull(supuestosConfidencialidad);
    }

    /**
     * Gets the object of the tender.
     *
     * @return The object of the tender.
     */
    public String getObjetoLicitacion() {
        return objetoLicitacion;
    }

    /**
     * Gets the file number of the tender.
     *
     * @return The file number.
     */
    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    /**
     * Checks if the procedure is divided into lots.
     *
     * @return {@code true} if the tender has lots, {@code false} otherwise.
     */
    public boolean isTieneLotes() {
        return tieneLotes;
    }

    /**
     * Gets the total number of lots.
     *
     * @return The number of lots.
     */
    public int getNumLotes() {
        return numLotes;
    }

    /**
     * Gets the names of the required common files.
     *
     * @return An array of {@code String} with the names of the common files.
     */
    public String[] getNombresArchivosComunes() {
        return nombresArchivosComunes;
    }

    /**
     * Gets the array of the mandatory status of the common files.
     *
     * @return An array of {@code boolean} indicating the mandatory status.
     */
    public boolean[] getArchivosComunesObligatorios() {
        return archivosComunesObligatorios;
    }

    /**
     * Gets the array that indicates if the common files are susceptible to being confidential.
     *
     * @return An array of {@code boolean} indicating the confidentiality susceptibility.
     */
    public boolean[] getArchivosComunesConfidenciales() {
        return archivosComunesConfidenciales;
    }

    /**
     * Gets an array with the names of all the offer files.
     *
     * @return An array of {@code String} with the names of the offer files.
     */
    public String[] getNombresArchivosOfertas() {
        return Arrays.stream(archivosOferta)
                .map(ArchivoOferta::getNombre)
                .toArray(String[]::new);
    }

    /**
     * Gets the complete array of offer file configuration objects.
     *
     * @return An array of {@link ArchivoOferta}.
     */
    public ArchivoOferta[] getArchivosOferta() {
        return archivosOferta;
    }

    /**
     * Checks if a specific offer file (by index) is susceptible to being declared confidential.
     *
     * @param index The index of the file within the {@link #archivosOferta} array.
     * @return {@code true} if it can be confidential, {@code false} if not or if the index is invalid.
     */
    public boolean puedeSerConfidencial(int index) {
        if (index >= 0 && index < archivosOferta.length) {
            return archivosOferta[index].esConfidencial();
        }
        return false;
    }

    /**
     * Checks if a specific offer file (by index) is mandatory.
     *
     * @param indice The index of the file within the {@link #archivosOferta} array.
     * @return {@code true} if it is mandatory, {@code false} otherwise.
     */
    public boolean esOfertaObligatoria(int indice) {
        return archivosOferta[indice].esObligatorio;
    }

    /**
     * Gets the list of predefined legal confidentiality assumptions.
     *
     * @return An array of {@code String} with the assumptions.
     */
    public String[] getSupuestosConfidencialidad() {
        return supuestosConfidencialidad;
    }

    /**
     * A static nested class that represents the configuration of an individual offer file.
     * It is similar to {@code interfaz.models.ArchivoRequerido} but specific to this configuration.
     */
    public static class ArchivoOferta implements Serializable {

        private final String nombre;
        private final boolean esConfidencial;
        private final boolean esObligatorio;

        /**
         * Constructor for an offer file.
         *
         * @param nombre The name or description of the file.
         * @param esConfidencial If the file is susceptible to confidentiality.
         * @param esObligatorio If the file upload is mandatory.
         */
        public ArchivoOferta(String nombre, boolean esConfidencial, boolean esObligatorio) {
            this.nombre = Objects.requireNonNull(nombre);
            this.esConfidencial = esConfidencial;
            this.esObligatorio = esObligatorio;
        }

        /**
         * Gets the name of the file.
         *
         * @return The name.
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Checks if the file is susceptible to being declared confidential.
         *
         * @return {@code true} if it is susceptible.
         */
        public boolean esConfidencial() {
            return esConfidencial;
        }

        /**
         * Checks if the file is mandatory.
         *
         * @return {@code true} if it is mandatory.
         */
        public boolean esObligatorio() {
            return esObligatorio;
        }
    }
}
