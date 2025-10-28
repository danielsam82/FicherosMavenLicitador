package com.licitador.configurator;

import com.licitador.model.LicitacionData;
import com.licitador.service.Logger;
import com.licitador.service.TextAreaLogger;
import javax.swing.JTextArea;
import java.io.*;
import java.util.*;
import java.util.jar.*;

public class JarExporter {
    
    // CRÍTICO: Clase principal REAL de tu proyecto
    private static final String MAIN_CLASS = "com.licitador.app.Ficheros";
    private final LicitacionData licitacionData; 
    private final String outputFilePath;
    private final Logger logger; 
    
    public JarExporter(LicitacionData licitacionData, String outputFilePath, JTextArea logArea) {
        this.licitacionData = licitacionData;
        this.outputFilePath = outputFilePath;
        this.logger = new TextAreaLogger(logArea); 
    }
    
    // =========================================================================
    // LÓGICA DE EXPORTACIÓN (Basada en tu versión original)
    // =========================================================================

    public void exportJar() throws Exception {
        logger.logInfo("Comenzando la creación del archivo JAR en: " + outputFilePath);

        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, MAIN_CLASS); // Clase principal corregida
        
        try (FileOutputStream fos = new FileOutputStream(outputFilePath);
             JarOutputStream jos = new JarOutputStream(fos, manifest)) {
            
            // 1. Añadir configuración serializada
            logger.logInfo("Serializando datos de LicitacionData...");
            addSerializedData(jos);
            
            // 2. Añadir clases necesarias (Copiando todo el classpath, como en tu original)
            logger.logInfo("Copiando clases y dependencias del classpath al JAR...");
            copyClasspathToJar(jos);
            
            logger.logInfo("Archivo JAR creado con éxito.");
            
        } catch (Exception e) {
            logger.logError("Fallo al crear el JAR: " + e.getMessage());
            throw new Exception("Error al generar el JAR: " + e.getMessage(), e);
        }
    }
    
    /** Serializa el objeto LicitacionData y lo añade al JAR con el nombre config.dat. */
    private void addSerializedData(JarOutputStream jos) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Uso de ObjectOutputStream directo para no cerrar el flujo subyacente (jos)
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(licitacionData);
        oos.flush();
        oos.close(); // Cierra el ByteArrayOutputStream (no afecta a jos)
        
        // El nombre de archivo en tu original era 'config.dat'
        addJarEntry(jos, "config.dat", new ByteArrayInputStream(baos.toByteArray()));
        logger.logInfo("Serialización completada en config.dat.");
    }
    
    /** Copia todas las clases y JARs del classpath de la aplicación Configurador al nuevo JAR. */
    private void copyClasspathToJar(JarOutputStream jarOut) throws IOException {
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        
        for (String entry : classpathEntries) {
            File file = new File(entry);
            
            if (file.isDirectory()) {
                // Copia las carpetas de clases (target/classes)
                addDirectoryToJar(file, jarOut, "");
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                // Copia JARs de dependencia (Si el Configurador depende de librerías)
                addJarToJar(file, jarOut);
            }
        }
    }
    
    // =========================================================================
    // MÉTODOS AUXILIARES (De tu versión original, adaptados)
    // =========================================================================

    private void addDirectoryToJar(File directory, JarOutputStream jarOut, String parentPath) throws IOException {
        // Ignorar la carpeta .git y otras no esenciales
        if (directory.getName().startsWith(".")) return;
        
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addDirectoryToJar(file, jarOut, parentPath + file.getName() + "/");
            } else {
                addFileToJar(file, jarOut, parentPath);
            }
        }
    }

    private void addFileToJar(File file, JarOutputStream jarOut, String parentPath) throws IOException {
        String entryName = parentPath + file.getName();
        
        // Evitar duplicar el Manifest si ya existe en un JAR de dependencia
        if (entryName.toUpperCase().contains("META-INF/MANIFEST.MF")) return;
        
        // Las clases están en el classpath, pero queremos asegurarnos de que la
        // clase principal está bien referenciada, aunque tu método lo copia todo.
        // Si el archivo termina en .class y es la clase principal del Config, lo ignoramos.
        // Solo copiamos archivos normales.
        
        try(FileInputStream fis = new FileInputStream(file)) {
            addJarEntry(jarOut, entryName, fis);
        }
    }

    private void addJarToJar(File jarFile, JarOutputStream jarOut) throws IOException {
        try (JarFile sourceJar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = sourceJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                // Ignoramos el manifest del JAR de origen para usar solo el nuestro.
                if (entry.getName().toUpperCase().contains("META-INF/MANIFEST.MF")) {
                    continue; 
                }
                if (!entry.isDirectory()) {
                    addJarEntry(jarOut, entry.getName(), sourceJar.getInputStream(entry));
                }
            }
        }
    }
    
    /** Añade una entrada al JAR desde un InputStream. */
    private void addJarEntry(JarOutputStream jarOut, String entryName, InputStream is) throws IOException {
        try {
             // Asegurarse de que las barras estén en el formato correcto para un JAR
             String cleanEntryName = entryName.replace('\\', '/');
             jarOut.putNextEntry(new JarEntry(cleanEntryName));
             
             byte[] buffer = new byte[1024];
             int len;
             while ((len = is.read(buffer)) > 0) {
                 jarOut.write(buffer, 0, len);
             }
        } finally {
             // NO cerramos el is aquí porque puede ser el stream de un JarFile (addJarToJar)
             // o un FileInputStream (addFileToJar). En tu original lo cerrabas, 
             // pero con try-with-resources es más seguro. Lo dejo como lo tenías, 
             // pero con el try/finally para cerrar la entrada del JAR.
             jarOut.closeEntry();
        }
    }
}