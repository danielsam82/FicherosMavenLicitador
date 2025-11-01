package com.licitador.service;

import com.licitador.model.ArticuloAnexo;
import java.io.Serializable;
import java.util.Objects;
import java.util.Arrays;

/**
 * Clase de modelo que encapsula toda la configuración de un procedimiento de
 * licitación.
 * <p>
 * Incluye datos de identificación, estructura de lotes, y la lista de todos los
 * archivos requeridos (comunes y de oferta), junto con sus propiedades de
 * obligatoriedad y confidencialidad. Implementa {@code Serializable} para su
 * persistencia.
 * </p>
 */
public class Configuracion implements Serializable {

    /**
     * Descripción del objeto de la licitación.
     */
    private final String objetoLicitacion;
    /**
     * Número de expediente administrativo de la licitación.
     */
    private final String numeroExpediente;
    /**
     * Indica si el procedimiento está dividido en lotes.
     */
    private final boolean tieneLotes;
    /**
     * Número total de lotes, si {@link #tieneLotes} es {@code true}.
     */
    private final int numLotes;
    /**
     * Nombres de los archivos comunes o complementarios que se deben adjuntar.
     */
    private final String[] nombresArchivosComunes;
    /**
     * Array booleano que indica la obligatoriedad de cada archivo común (índice
     * correlativo a {@link #nombresArchivosComunes}).
     */
    private final boolean[] archivosComunesObligatorios;
    /**
     * Array booleano que indica si cada archivo común es susceptible de ser
     * declarado confidencial.
     */
    private final boolean[] archivosComunesConfidenciales; // ¡Campo añadido!
    /**
     * Array de objetos que definen las propiedades de los archivos específicos
     * de la oferta.
     *
     * @see ArchivoOferta
     */
    private final ArchivoOferta[] archivosOferta;
    /**
     * Lista de los supuestos legales predefinidos de confidencialidad.
     */
    private final String[] supuestosConfidencialidad;
    // --- CAMPO NUEVO ---

    private final ArticuloAnexo[] articulosAnexos;
    // -------------------

    /**
     * Constructor para inicializar una nueva configuración de licitación.
     *
     * @param objetoLicitacion Objeto de la licitación.
     * @param numeroExpediente Número de expediente.
     * @param tieneLotes {@code true} si tiene lotes.
     * @param numLotes Número de lotes.
     * @param nombresArchivosComunes Nombres de archivos comunes.
     * @param archivosComunesObligatorios Obligatoriedad de los archivos
     * comunes.
     * @param archivosComunesConfidenciales Susceptibilidad de confidencialidad
     * de archivos comunes.
     * @param archivosOferta Array de configuración de archivos de oferta.
     * @param supuestosConfidencialidad Lista de supuestos de confidencialidad.
     */
    public Configuracion(String objetoLicitacion, String numeroExpediente, boolean tieneLotes, int numLotes,
            String[] nombresArchivosComunes, boolean[] archivosComunesObligatorios,
            boolean[] archivosComunesConfidenciales, // ¡Parámetro añadido!
            ArchivoOferta[] archivosOferta, String[] supuestosConfidencialidad, ArticuloAnexo[] articulosAnexos) {
        this.objetoLicitacion = Objects.requireNonNull(objetoLicitacion);
        this.numeroExpediente = Objects.requireNonNull(numeroExpediente);
        this.tieneLotes = tieneLotes;
        this.numLotes = numLotes;
        this.nombresArchivosComunes = Objects.requireNonNull(nombresArchivosComunes);
        this.archivosComunesObligatorios = archivosComunesObligatorios;
        this.archivosComunesConfidenciales = Objects.requireNonNull(archivosComunesConfidenciales); // ¡Inicialización añadida!
        this.archivosOferta = Objects.requireNonNull(archivosOferta);
        this.supuestosConfidencialidad = Objects.requireNonNull(supuestosConfidencialidad);
        this.articulosAnexos = (articulosAnexos != null) ? articulosAnexos : new ArticuloAnexo[0];
    }

    // --- NUEVO GETTER ---
    public ArticuloAnexo[] getArticulosAnexos() {
        return articulosAnexos;
    }

    /**
     * Obtiene el objeto de la licitación.
     *
     * @return El objeto de la licitación.
     */
    public String getObjetoLicitacion() {
        return objetoLicitacion;
    }

    /**
     * Obtiene el número de expediente de la licitación.
     *
     * @return El número de expediente.
     */
    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    /**
     * Consulta si el procedimiento está dividido en lotes.
     *
     * @return {@code true} si la licitación tiene lotes, {@code false} en caso
     * contrario.
     */
    public boolean isTieneLotes() {
        return tieneLotes;
    }

    /**
     * Obtiene el número de lotes total.
     *
     * @return El número de lotes.
     */
    public int getNumLotes() {
        return numLotes;
    }

    /**
     * Obtiene los nombres de los archivos comunes requeridos.
     *
     * @return Array de {@code String} con los nombres de los archivos comunes.
     */
    public String[] getNombresArchivosComunes() {
        return nombresArchivosComunes;
    }

    /**
     * Obtiene el array de la obligatoriedad de los archivos comunes.
     *
     * @return Array de {@code boolean} indicando la obligatoriedad.
     */
    public boolean[] getArchivosComunesObligatorios() {
        return archivosComunesObligatorios;
    }

    /**
     * Obtiene el array que indica si los archivos comunes son susceptibles de
     * ser confidenciales.
     *
     * @return Array de {@code boolean} indicando la susceptibilidad de
     * confidencialidad.
     */
    public boolean[] getArchivosComunesConfidenciales() {
        return archivosComunesConfidenciales;
    }

    /**
     * Obtiene un array con los nombres de todos los archivos de oferta.
     *
     * @return Array de {@code String} con los nombres de los archivos de
     * oferta.
     */
    public String[] getNombresArchivosOfertas() {
        return Arrays.stream(archivosOferta)
                .map(ArchivoOferta::getNombre)
                .toArray(String[]::new);
    }

    /**
     * Obtiene el array completo de objetos de configuración de archivos de
     * oferta.
     *
     * @return Array de {@link ArchivoOferta}.
     */
    public ArchivoOferta[] getArchivosOferta() {
        return archivosOferta;
    }

    /**
     * Consulta si un archivo de oferta específico (por índice) es susceptible
     * de ser declarado confidencial.
     *
     * @param index El índice del archivo dentro del array
     * {@link #archivosOferta}.
     * @return {@code true} si puede ser confidencial, {@code false} si no o si
     * el índice es inválido.
     */
    public boolean puedeSerConfidencial(int index) {
        if (index >= 0 && index < archivosOferta.length) {
            return archivosOferta[index].esConfidencial();
        }
        return false;
    }

    /**
     * Consulta si un archivo de oferta específico (por índice) es obligatorio.
     *
     * @param indice El índice del archivo dentro del array
     * {@link #archivosOferta}.
     * @return {@code true} si es obligatorio, {@code false} en caso contrario.
     */
    public boolean esOfertaObligatoria(int indice) {
        return archivosOferta[indice].esObligatorio;
    }

    /**
     * Obtiene la lista de supuestos legales de confidencialidad predefinidos.
     *
     * @return Array de {@code String} con los supuestos.
     */
    public String[] getSupuestosConfidencialidad() {
        return supuestosConfidencialidad;
    }

    /**
     * Clase estática anidada que representa la configuración de un archivo de
     * oferta individual. Es similar a {@link interfaz.models.ArchivoRequerido}
     * pero específica para esta configuración.
     */
    public static class ArchivoOferta implements Serializable {

        /**
         * Nombre descriptivo del archivo de oferta.
         */
        private final String nombre;
        /**
         * Indica si el archivo puede ser marcado como confidencial.
         */
        private final boolean esConfidencial;
        /**
         * Indica si el archivo es de carga obligatoria.
         */
        private final boolean esObligatorio;

        /**
         * Constructor para un archivo de oferta.
         *
         * @param nombre El nombre o descripción del archivo.
         * @param esConfidencial Si el archivo es susceptible de
         * confidencialidad.
         * @param esObligatorio Si la carga del archivo es obligatoria.
         */
        public ArchivoOferta(String nombre, boolean esConfidencial, boolean esObligatorio) {
            this.nombre = Objects.requireNonNull(nombre);
            this.esConfidencial = esConfidencial;
            this.esObligatorio = esObligatorio;
        }

        /**
         * Obtiene el nombre del archivo.
         *
         * @return El nombre.
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Consulta si el archivo es susceptible de ser declarado confidencial.
         *
         * @return {@code true} si es susceptible.
         */
        public boolean esConfidencial() {
            return esConfidencial;
        }

        /**
         * Consulta si el archivo es obligatorio.
         *
         * @return {@code true} si es obligatorio.
         */
        public boolean esObligatorio() {
            return esObligatorio;
        }
    }
}
