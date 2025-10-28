package com.licitador.configurator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry; // Importación necesaria para ZipEntry
import javax.swing.JTextArea;

// Importaciones para el Logger
import com.licitador.service.Logger;
import com.licitador.service.TextAreaLogger;
import com.licitador.model.LicitacionData;

public class JarExporter {

    private final LicitacionData licitacionData;
    private final String outputFilePath;
    private final Logger logger;

    // CONSTRUCTOR: Define que acepta String y JTextArea
    public JarExporter(LicitacionData licitacionData, String outputFilePath, JTextArea logArea) {
        this.licitacionData = licitacionData;
        this.outputFilePath = outputFilePath;
        this.logger = new TextAreaLogger(logArea);
    }

    // Método que ejecuta la exportación
    public void exportJar() {
        try (JarOutputStream target = new JarOutputStream(new FileOutputStream(outputFilePath))) {
            logger.log("Iniciando la exportación del JAR a: " + outputFilePath);

            // --- ESCRIBIR EL MANIFIESTO ---
            writeManifest(target);

            // --- AÑADIR CLASES DEL LICITADOR (RUTAS CORREGIDAS) ---
            // com.licitador.app
            addFile(new File("target/classes/com/licitador/app/Ficheros.class"), "com/licitador/app/Ficheros.class", target);

            // com.licitador.ui
            addFile(new File("target/classes/com/licitador/ui/MainWindow.class"), "com/licitador/ui/MainWindow.class", target);
            addFile(new File("target/classes/com/licitador/ui/CargarArchivoComunDialog.class"), "com/licitador/ui/CargarArchivoComunDialog.class", target);
            addFile(new File("target/classes/com/licitador/ui/CargarOfertaDialog.class"), "com/licitador/ui/CargarOfertaDialog.class", target);
            addFile(new File("target/classes/com/licitador/ui/ConfidencialidadDialog.class"), "com/licitador/ui/ConfidencialidadDialog.class", target);
            addFile(new File("target/classes/com/licitador/ui/ConfiguracionInicialDialog.class"), "com/licitador/ui/ConfiguracionInicialDialog.class", target);
            addFile(new File("target/classes/com/licitador/ui/DetalleOfertasDialog.class"), "com/licitador/ui/DetalleOfertasDialog.class", target);
            addFile(new File("target/classes/com/licitador/ui/LicitadorLotesDialog.class"), "com/licitador/ui/LicitadorLotesDialog.class", target);

            // com.licitador.model
            addFile(new File("target/classes/com/licitador/model/ArchivoRequerido.class"), "com/licitador/model/ArchivoRequerido.class", target);
            addFile(new File("target/classes/com/licitador/model/LicitacionData.class"), "com/licitador/model/LicitacionData.class", target);
            addFile(new File("target/classes/com/licitador/model/LicitadorData.class"), "com/licitador/model/LicitadorData.class", target);
            addFile(new File("target/classes/com/licitador/model/LicitadorLotesData.class"), "com/licitador/model/LicitadorLotesData.class", target);

            // com.licitador.service
            addFile(new File("target/classes/com/licitador/service/Configuracion.class"), "com/licitador/service/Configuracion.class", target);
            addFile(new File("target/classes/com/licitador/service/ConfidencialidadData.class"), "com/licitador/service/ConfidencialidadData.class", target);
            addFile(new File("target/classes/com/licitador/service/FileData.class"), "com/licitador/service/FileData.class", target);
            addFile(new File("target/classes/com/licitador/service/FileManager.class"), "com/licitador/service/FileManager.class", target);
            addFile(new File("target/classes/com/licitador/service/Logger.class"), "com/licitador/service/Logger.class", target);
            addFile(new File("target/classes/com/licitador/service/TextAreaLogger.class"), "com/licitador/service/TextAreaLogger.class", target);

            logger.log("Exportación del JAR finalizada con éxito.");
        } catch (IOException e) {
            logger.log("Error al exportar el JAR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFile(File source, String entryName, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.exists()) {
                in = new BufferedInputStream(new FileInputStream(source));
                target.putNextEntry(new JarEntry(entryName));
                byte[] buffer = new byte[1024];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
                logger.log("Añadido: " + entryName);
            } else {
                logger.log("Advertencia: Archivo no encontrado - " + source.getPath());
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void writeManifest(JarOutputStream target) throws IOException {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        // Clase principal para el JAR de la APLICACIÓN LICITADORA
        mainAttributes.put(Attributes.Name.MAIN_CLASS, "com.licitador.app.Ficheros");

        target.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        manifest.write(target);
        target.closeEntry();
    }
}
