package ficheros;

import java.io.Serializable;
import java.util.Objects;
import java.util.Arrays; // Incluido para coherencia, aunque no se usa directamente en esta clase

/**
 * Clase de modelo (Data Transfer Object - DTO) que encapsula la información y
 * el contenido binario de un archivo adjunto a la licitación.
 * <p>
 * Implementa {@code Serializable} para su persistencia. Almacena el contenido
 * del archivo como un array de bytes y maneja propiedades de confidencialidad,
 * incluyendo los supuestos legales seleccionados y los motivos asociados.
 * </p>
 */
public class FileData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Nombre completo del archivo (incluyendo extensión).
     */
    private final String nombre;
    /**
     * Contenido binario del archivo.
     */
    private final byte[] contenido;
    /**
     * Indicador de si el archivo ha sido marcado como confidencial por el
     * licitador.
     */
    private boolean esConfidencial;

    /**
     * Array de claves de los supuestos legales de confidencialidad
     * seleccionados. Es {@code null} si el archivo no es confidencial.
     */
    private final String[] supuestosSeleccionados;
    /**
     * Array de los motivos de aplicación (descripciones) para cada supuesto. Es
     * {@code null} si el archivo no es confidencial.
     */
    private final String[] motivosSupuestos;

    /**
     * Constructor para archivos que **no** son susceptibles de ser
     * confidenciales o que no se han marcado como tales.
     *
     * @param nombre El nombre del archivo.
     * @param contenido El contenido binario del archivo.
     */
    public FileData(String nombre, byte[] contenido) {
        // Llama al constructor principal, marcando como NO confidencial y con nulls
        this(nombre, contenido, false, null, null);
    }

    /**
     * Constructor principal para inicializar un objeto {@code FileData},
     * incluyendo su estado de confidencialidad y los detalles asociados.
     *
     * @param nombre El nombre del archivo.
     * @param contenido El contenido binario del archivo.
     * @param esConfidencial Indica si el archivo debe marcarse como
     * confidencial.
     * @param supuestosSeleccionados Array de supuestos seleccionados. Debe ser
     * {@code null} si {@code esConfidencial} es {@code false}.
     * @param motivosSupuestos Array de motivos asociados. Debe ser {@code null}
     * si {@code esConfidencial} es {@code false}.
     * @throws NullPointerException Si el nombre o el contenido son
     * {@code null}.
     */
    public FileData(String nombre, byte[] contenido, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        this.nombre = Objects.requireNonNull(nombre, "Nombre no puede ser null");
        this.contenido = Objects.requireNonNull(contenido, "Contenido no puede ser null");
        this.esConfidencial = esConfidencial;

        // SOLO si es confidencial, guarda los supuestos y motivos.
        if (esConfidencial) {
            // Aseguramos que no sean null, usando un array vacío si la entrada es null
            this.supuestosSeleccionados = (supuestosSeleccionados != null) ? supuestosSeleccionados : new String[0];
            this.motivosSupuestos = (motivosSupuestos != null) ? motivosSupuestos : new String[0];
        } else {
            // Si no es confidencial, estos campos se establecen a null para ahorrar espacio y claridad.
            this.supuestosSeleccionados = null;
            this.motivosSupuestos = null;
        }
    }

    /**
     * Obtiene el array de claves de supuestos de confidencialidad
     * seleccionados. Retorna {@code null} si el archivo no es confidencial.
     *
     * @return Array de {@code String} o {@code null}.
     */
    public String[] getSupuestosSeleccionados() {
        return supuestosSeleccionados;
    }

    /**
     * Obtiene el array de motivos de aplicación proporcionados para los
     * supuestos seleccionados. Retorna {@code null} si el archivo no es
     * confidencial.
     *
     * @return Array de {@code String} o {@code null}.
     */
    public String[] getMotivosSupuestos() {
        return motivosSupuestos;
    }

    /**
     * Obtiene el nombre completo del archivo (incluyendo extensión).
     *
     * @return El nombre del archivo.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el contenido binario del archivo.
     *
     * @return Array de bytes con el contenido.
     */
    public byte[] getContenido() {
        return contenido;
    }

    /**
     * Consulta si el archivo ha sido marcado como confidencial.
     *
     * @return {@code true} si el archivo es confidencial, {@code false} en caso
     * contrario.
     */
    public boolean esConfidencial() {
        return esConfidencial;
    }

    /**
     * Extrae la extensión del archivo a partir de su nombre.
     *
     * @return La extensión como {@code String} (sin el punto), o una cadena
     * vacía si no tiene extensión.
     */
    public String getExtension() {
        int lastDotIndex = nombre.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < nombre.length() - 1) {
            return nombre.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Extrae el nombre del archivo sin la extensión.
     *
     * @return El nombre del archivo sin la extensión. Si no hay extensión,
     * devuelve el nombre completo.
     */
    public String getNombreSinExtension() {
        int lastDotIndex = nombre.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return nombre.substring(0, lastDotIndex);
        }
        return nombre;
    }
}
