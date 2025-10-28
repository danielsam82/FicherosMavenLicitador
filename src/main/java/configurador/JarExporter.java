package configurador;

import interfaz.models.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.JOptionPane;

/**
 * Clase encargada de generar el archivo JAR ejecutable final que se entrega al
 * licitador. Este JAR encapsula la aplicación cliente completa junto con los
 * datos de configuración de la licitación serializados
 * ({@code LicitacionData}).
 *
 * El proceso se realiza tomando como base un JAR precompilado (shaded/gordo) de
 * Maven y añadiéndole el archivo {@code config.dat} con los datos específicos.
 */
/**
 * Constructor por defecto.
 */
public class JarExporter {

    /**
     * Define la clase principal que se ejecutará al iniciar el JAR final.
     */
    private static final String MAIN_CLASS = "ficheros.Ficheros";

    /**
     * Ruta relativa al JAR fuente (shaded JAR de Maven) que contiene todas las
     * clases y dependencias de la aplicación cliente.
     */
    private static final String SHADED_JAR_PATH = "Ficheros2-1.0-SNAPSHOT.jar";

    /**
     * Genera el JAR ejecutable final, incrustando los datos de configuración de
     * la licitación y copiando todas las clases del JAR fuente.
     *
     * @param datos Los datos de configuración (expediente, objeto, documentos,
     * etc.) a serializar e incrustar en el JAR.
     * @param outputJarPath La ruta completa, incluyendo el nombre de archivo,
     * para el JAR de salida.
     * @return {@code true} si la exportación fue exitosa; {@code false} si la
     * operación fue cancelada (por ejemplo, por el usuario en el diálogo de
     * guardar) o si la ruta es inválida.
     * @throws IOException Si ocurre un error de entrada/salida durante la
     * serialización, la creación del JAR, o la copia de archivos.
     */
    public boolean exportToJar(LicitacionData datos, String outputJarPath) throws IOException {

        // 1. MANEJO DE CANCELACIÓN: Si la ruta es nula o vacía (ej. se canceló el JFileChooser), salimos.
        if (outputJarPath == null || outputJarPath.trim().isEmpty()) {
            return false;
        }

        boolean success = false;

        // 2. CREAR MANIFIESTO DEL JAR FINAL
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);

        try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputJarPath), manifest)) {

            // 3. AÑADIR CONFIGURACIÓN SERIALIZADA (config.dat)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(datos);
            }
            // Agrega el archivo config.dat al JAR
            addJarEntry(jarOut, "config.dat", new ByteArrayInputStream(baos.toByteArray()));

            // 4. COPIAR TODO EL CONTENIDO DEL JAR GORDO
            copyShadedJarContents(jarOut);

            // Si llegamos hasta aquí, el proceso fue exitoso
            success = true;

        } catch (FileNotFoundException e) {
            // Manejo del error si el JAR fuente (shaded JAR) no se encuentra
            JOptionPane.showMessageDialog(null,
                    "Error: No se encontró el JAR fuente de Maven. Ejecuta 'Clean and Build' en tu IDE.\n"
                    + "Buscado en: " + new File(SHADED_JAR_PATH).getAbsolutePath(),
                    "Error de Archivo",
                    JOptionPane.ERROR_MESSAGE);
            // Relanzamos la excepción para el manejo superior
            throw e;
        } catch (Exception e) {
            // Manejo de cualquier otra excepción de E/S
            throw new IOException("Error al generar el JAR: " + e.getMessage(), e);
        }

        // 5. MOSTRAR EL MENSAJE DE ÉXITO (SOLO si success es true)
        if (success) {
            JOptionPane.showMessageDialog(null,
                    "¡Archivo .jar generado con éxito!\n"
                    + "Ruta: " + outputJarPath,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        return success;
    }

    // ========================================================================
    // LÓGICA DE COPIA (SOLO EXTRACCIÓN DEL JAR FUENTE)
    // ========================================================================
    /**
     * Copia todos los contenidos (clases, recursos, etc.) del JAR fuente
     * ({@code SHADED_JAR_PATH}) al JAR de salida, excluyendo el manifiesto y
     * las entradas de directorio.
     *
     * @param jarOut El {@code JarOutputStream} abierto del JAR de salida.
     * @throws IOException Si el archivo JAR fuente no se encuentra o hay un
     * error de lectura.
     */
    private void copyShadedJarContents(JarOutputStream jarOut) throws IOException {
        File shadedJarFile = new File(SHADED_JAR_PATH);

        if (!shadedJarFile.exists()) {
            throw new FileNotFoundException("El JAR Sombreado (" + SHADED_JAR_PATH + ") no fue encontrado. Asegúrate de compilar el proyecto correctamente.");
        }

        try (JarFile sourceJar = new JarFile(shadedJarFile)) {
            Enumeration<JarEntry> entries = sourceJar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Excluimos el Manifiesto y directorios para evitar duplicados y conflictos
                if (entryName.equalsIgnoreCase("META-INF/MANIFEST.MF") || entry.isDirectory()) {
                    continue;
                }

                // Copiamos el archivo al nuevo JAR
                addJarEntry(jarOut, entryName, sourceJar.getInputStream(entry));
            }
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================
    /**
     * Añade una entrada (archivo o recurso) al JAR de salida.
     *
     * @param jarOut El {@code JarOutputStream} al que se añadirá la entrada.
     * @param entryName El nombre de la entrada dentro del JAR.
     * @param is El {@code InputStream} de los datos a copiar.
     * @throws IOException Si ocurre un error de lectura o escritura.
     */
    private void addJarEntry(JarOutputStream jarOut, String entryName, InputStream is) throws IOException {
        jarOut.putNextEntry(new JarEntry(entryName));

        // Copia de datos
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            jarOut.write(buffer, 0, len);
        }

        jarOut.closeEntry();
        is.close(); // Cierra el InputStream de la entrada
    }
}
