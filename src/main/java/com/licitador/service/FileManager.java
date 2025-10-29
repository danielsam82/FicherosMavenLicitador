package com.licitador.service;

import com.licitador.model.LicitadorData;
import com.licitador.service.FileData;
import com.licitador.service.Logger;
import com.licitador.service.Configuracion;
import com.licitador.service.PDFGenerator;
import com.lowagie.text.DocumentException; 
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class FileManager implements Serializable {

    // --- NUEVOS CAMPOS PARA EL ANEXO ADMINISTRATIVO ---
    private FileData anexoAdministrativoData;
    private static final String ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE = "Anexo Administrativo";
    // --------------------------------------------------

    // Declaración de variables de instancia
    private transient Logger logger;
    private final Configuracion configuracion;
    private final Map<String, FileData> archivosComunes;
    private final Map<String, FileData> archivosOferta;
    // Mapa: Lote ID (Integer) -> Participación (Boolean)
    private final Map<Integer, Boolean> participacionPorLote;
    private LicitadorData licitadorData; // Se asume inicializado por el constructor

    public FileManager(Configuracion configuracion, Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger no puede ser null");
        }
        this.logger = logger;
        this.configuracion = Objects.requireNonNull(configuracion, "Configuración no puede ser null");
        this.archivosComunes = new HashMap<>();
        this.archivosOferta = new LinkedHashMap<>();
        this.participacionPorLote = new HashMap<>();
        this.licitadorData = new LicitadorData();
    }

    // Custom deserialization to re-initialize transient logger
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // NOTA: El logger debe re-inicializarse externamente si se utiliza el singleton.
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void log(String message) {
        if (logger != null) {
            logger.log(message);
        }
    }

    public void logError(String message) {
        if (logger != null) {
            logger.logError(message);
        }
    }
    
    public void logInfo(String message) {
         if (logger != null) {
            logger.log(message);
        }
    }
    
    // --- LÓGICA DE GESTIÓN DEL ANEXO ADMINISTRATIVO ---
    
    /**
     * Valida si los datos del Licitador y la selección de lotes son suficientes
     * para generar el Anexo Administrativo.
     * @return true si los datos están listos.
     */
    public boolean validarDatosAdministrativosParaAnexo() {
        // 1. Validar participación mínima (si aplica)
        if (!validarMinimoParticipacion()) {
            // El error ya fue logueado en validarMinimoParticipacion()
            return false;
        }
        
        // 2. Validar campos obligatorios del licitador (Se asume que LicitadorData tiene un método para esto)
        // NOTA: Necesitas implementar 'estanDatosObligatoriosCompletos()' en tu clase LicitadorData.
        // if (!licitadorData.estanDatosObligatoriosCompletos()) { 
        //     logError("Validación Administrativa fallida: Faltan datos obligatorios del licitador (NIF/Razón Social).");
        //     return false;
        // }
        
        // Implementación temporal asumiendo NIF y Razón Social son mínimos
        if (licitadorData.getNif() == null || licitadorData.getNif().trim().isEmpty() ||
            licitadorData.getRazonSocial() == null || licitadorData.getRazonSocial().trim().isEmpty()) {
             logError("Validación Administrativa fallida: Faltan datos obligatorios del licitador (NIF/Razón Social).");
             return false;
        }

        return true;
    }

    /**
     * Genera el Anexo Administrativo en formato PDF con los datos del licitador, 
     * la configuración y lo guarda en su campo dedicado. (MODIFICADO)
     *
     * @return true si el anexo se generó y cargó correctamente.
     */
    public boolean generarAnexoAdministrativoYGuardar() {
        
        // 1. Validar primero los datos administrativos
        if (!validarDatosAdministrativosParaAnexo()) {
            JOptionPane.showMessageDialog(null, "No se puede generar el Anexo. Faltan datos obligatorios del licitador o no ha seleccionado lotes.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            // Se asume que PDFGenerator.generarAnexoAdministrativo existe y funciona
            byte[] pdfContent = PDFGenerator.generarAnexoAdministrativo(
                    this.licitadorData,
                    this.participacionPorLote,
                    this.configuracion
            );

            String nombreFinalArchivo = ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE + ".pdf";

            FileData anexoData = new FileData(
                    nombreFinalArchivo,
                    pdfContent,
                    false, // No es confidencial
                    null,
                    null
            );

            // CLAVE: Lo guardamos en su campo dedicado
            this.anexoAdministrativoData = anexoData; 

            logger.log("Anexo Administrativo (" + nombreFinalArchivo + ") generado y listo para compresión.");
            return true;

        } catch (DocumentException e) {
            logError("Error al generar el PDF del Anexo Administrativo (DocumentException): " + e.getMessage());
        } catch (IOException e) {
            logError("Error de I/O al generar el PDF del Anexo Administrativo: " + e.getMessage());
        } catch (Exception e) {
             logError("Error inesperado al generar el Anexo Administrativo: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Valida si el licitador ha cumplimentado todos los requisitos obligatorios
     * para proceder a la compresión final. (CORREGIDO Y UNIFICADO)
     *
     * @return true si la oferta está lista para ser comprimida.
     */
    public boolean validarOfertaCompleta() {

        // 1. Validar Anexo Administrativo (debe haber sido generado previamente)
        if (this.anexoAdministrativoData == null || this.anexoAdministrativoData.getContenido() == null || this.anexoAdministrativoData.getContenido().length == 0) {
            logError("Validación de Compresión fallida: Faltan el Anexo Administrativo. Por favor, generelo primero.");
            return false;
        }

        // 2. Validar Archivos y Ofertas Obligatorias (ya incluye la validación de lotes)
        if (!estanArchivosObligatoriosCompletos()) {
            // El error ya fue logueado en estanArchivosObligatoriosCompletos()
            return false;
        }
        
        logInfo("Validación final exitosa: Todos los requisitos obligatorios están cubiertos.");
        return true;
    }

    // --- MÉTODOS DE ARCHIVOS Y COMPRESIÓN ---

    public boolean guardarSesion() { 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar progreso de la sesión");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo de Sesión (*.dat)", "dat"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".dat")) {
                fileToSave = new File(filePath + ".dat");
            }

            try (FileOutputStream fos = new FileOutputStream(fileToSave); ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                DatosSesionCargada datosSesion = new DatosSesionCargada(
                        archivosComunes,
                        archivosOferta,
                        this.licitadorData,
                        this.participacionPorLote
                );
                oos.writeObject(datosSesion);

                log("Sesión guardada en: " + fileToSave.getPath());
                JOptionPane.showMessageDialog(null, "El progreso se ha guardado correctamente.", "Guardar Sesión", JOptionPane.INFORMATION_MESSAGE);
                return true;

            } catch (IOException e) {
                logError("Error al guardar la sesión: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al guardar el progreso.", "Guardar Sesión", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            log("Operación de guardado de sesión cancelada.");
            return false;
        }
    }

    private static class DatosSesionCargada implements Serializable {

        private static final long serialVersionUID = 2L; 
        final Map<String, FileData> archivosComunes;
        final Map<String, FileData> ofertasPorLote;
        final LicitadorData licitadorData;
        final Map<Integer, Boolean> participacionPorLote; 

        DatosSesionCargada(Map<String, FileData> archivosComunes, Map<String, FileData> ofertasPorLote, LicitadorData licitadorData, Map<Integer, Boolean> participacionPorLote) {
            this.archivosComunes = archivosComunes;
            this.ofertasPorLote = ofertasPorLote;
            this.licitadorData = licitadorData;
            this.participacionPorLote = participacionPorLote;
        }
    }

    public boolean cargarSesion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar progreso de la sesión");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo de Sesión (*.dat)", "dat"));

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            if (!fileToLoad.exists() || !fileToLoad.canRead()) {
                JOptionPane.showMessageDialog(null, "No se puede leer el archivo seleccionado.", "Error al Cargar", JOptionPane.ERROR_MESSAGE);
                logError("No se pudo cargar la sesión: El archivo no existe o no es legible.");
                return false;
            }

            try (FileInputStream fis = new FileInputStream(fileToLoad); ObjectInputStream ois = new ObjectInputStream(fis)) {

                Object objDatosSesion = ois.readObject();
                if (objDatosSesion instanceof DatosSesionCargada) {
                    DatosSesionCargada datosSesion = (DatosSesionCargada) objDatosSesion;

                    this.archivosComunes.clear();
                    this.archivosComunes.putAll(datosSesion.archivosComunes);

                    this.archivosOferta.clear();
                    this.archivosOferta.putAll(datosSesion.ofertasPorLote);

                    this.licitadorData = datosSesion.licitadorData;

                    this.participacionPorLote.clear();
                    if (datosSesion.participacionPorLote != null) {
                        this.participacionPorLote.putAll(datosSesion.participacionPorLote);
                    } else {
                        log("Advertencia: Sesión cargada de versión antigua sin datos de participación por lote.");
                    }
                    
                    this.anexoAdministrativoData = null; // Siempre se regenera tras la carga

                    log("Sesión cargada desde: " + fileToLoad.getPath());
                    JOptionPane.showMessageDialog(null, "Progreso cargado con éxito.", "Cargar Sesión", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    throw new IOException("Formato de archivo de sesión inválido o incompatible. Asegúrese de que el archivo sea de la versión 2 o superior.");
                }

            } catch (IOException | ClassNotFoundException e) {
                logError("Error al cargar la sesión: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al cargar el progreso guardado. El archivo puede estar corrupto o no ser de esta versión.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            log("Operación de carga de sesión cancelada.");
            return false;
        }
    }

    public void resetData() {
        archivosComunes.clear();
        archivosOferta.clear();
        participacionPorLote.clear();
        this.licitadorData = new LicitadorData();
        this.anexoAdministrativoData = null; 
        log("Todos los datos de la sesión han sido eliminados.");
    }
    
    // --- MÉTODOS DE CARGA DE ARCHIVOS ---
    
    public boolean cargarArchivoComun(String nombreConfigurado, File archivoSeleccionado, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        if (archivosComunes.containsKey(nombreConfigurado)) {
             int respuesta = JOptionPane.showConfirmDialog(null, "Ya existe un archivo cargado para '" + nombreConfigurado + "'. ¿Desea sobrescribirlo?", "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
             if (respuesta != JOptionPane.YES_OPTION) {
                 log("Operación de carga de '" + nombreConfigurado + "' cancelada por el usuario.");
                 return false;
             }
        }

        try {
             byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());
             FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);
             archivosComunes.put(nombreConfigurado, nuevoArchivo);

             String logMessage = "Archivo común '" + nombreConfigurado + "' cargado desde: " + archivoSeleccionado.getAbsolutePath();
             if (esConfidencial) {
                 logMessage += " (CONFIDENCIAL)";
             }
             log(logMessage);

             return true;
        } catch (IOException ex) {
             logError("Error al cargar el archivo '" + nombreConfigurado + "': " + ex.getMessage());
             JOptionPane.showMessageDialog(null, "Error al leer el archivo seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
    }

    public boolean cargarArchivoOferta(String nombreOferta, File archivoSeleccionado, String loteKeyPrefix, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        String clave = loteKeyPrefix + nombreOferta;

        if (archivosOferta.containsKey(clave)) {
            int respuesta = JOptionPane.showConfirmDialog(null, "Ya existe un archivo cargado para '" + nombreOferta + "'. ¿Desea sobrescribirlo?", "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                log("Operación de carga de '" + nombreOferta + "' cancelada por el usuario.");
                return false;
            }
        }

        try {
            byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());
            FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);
            archivosOferta.put(clave, nuevoArchivo);

            String logMessage = "Archivo de oferta '" + nombreOferta + "' cargado desde: " + archivoSeleccionado.getAbsolutePath();
            if (!loteKeyPrefix.isEmpty()) {
                String numLoteStr = loteKeyPrefix.replace("Lote", "").replace("_", "");
                logMessage += " para el Lote " + numLoteStr;
            }
            log(logMessage);

            return true;
        } catch (IOException ex) {
            logError("Error al cargar el archivo de oferta: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error al leer el archivo seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean estanArchivosObligatoriosCompletos() {
        // Check common files
        boolean[] obligatoriosComunes = configuracion.getArchivosComunesObligatorios();
        String[] nombresComunes = configuracion.getNombresArchivosComunes();
        for (int i = 0; i < nombresComunes.length; i++) {
            if (obligatoriosComunes[i] && !archivosComunes.containsKey(nombresComunes[i])) {
                logError("Falta archivo común obligatorio: " + nombresComunes[i]);
                return false;
            }
        }

        // Check offer files (per lot or single offer)
        if (configuracion.isTieneLotes()) {
            for (int loteNum = 1; loteNum <= configuracion.getNumLotes(); loteNum++) {
                boolean participa = getParticipacionLote(loteNum);

                if (participa) { 
                    for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                        if (ofertaConfig.esObligatorio()) {
                            String claveEsperada = "Lote" + loteNum + "_" + ofertaConfig.getNombre();
                            if (!archivosOferta.containsKey(claveEsperada)) {
                                logError("Falta oferta obligatoria '" + ofertaConfig.getNombre() + "' para el Lote " + loteNum + " (Lote marcado como Participa).");
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            // For a single offer, check if all required offer files are present
            for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                if (ofertaConfig.esObligatorio() && !archivosOferta.containsKey(ofertaConfig.getNombre())) {
                    logError("Falta oferta obligatoria: " + ofertaConfig.getNombre());
                    return false;
                }
            }
        }
        return true;
    }


    public void comprimirArchivosConProgreso(File carpetaDestino, String zipFileName, String logContent, JProgressBar progressBar, Runnable onFinish) {
        
        // 1. USAMOS LA VALIDACIÓN UNIFICADA DE TODO EL PROCESO
        if (!validarOfertaCompleta()) { 
            JOptionPane.showMessageDialog(null, "No se pueden comprimir los archivos. Faltan documentos obligatorios o el Anexo Administrativo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            logError("Operación de compresión cancelada: Falló la validación completa.");
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        String mensajeConfirmacion = crearMensajeConfirmacion();
        int confirmacion = JOptionPane.showConfirmDialog(null, mensajeConfirmacion, "Confirmar Compresión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            log("Compresión cancelada por el usuario.");
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        progressBar.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                String identificadorParaZip = licitadorData.getNif();

                if (identificadorParaZip == null || identificadorParaZip.isEmpty()) {
                    identificadorParaZip = licitadorData.getRazonSocial();
                }

                if (identificadorParaZip == null || identificadorParaZip.isEmpty()) {
                    identificadorParaZip = "LicitadorDesconocido";
                }

                String sanitizedIdentifier = identificadorParaZip.replaceAll("[^a-zA-Z0-9_.-]", "_");
                String baseFileName = zipFileName + "_" + sanitizedIdentifier + "_" + timeStamp + ".zip";
                File outputFile = new File(carpetaDestino, baseFileName);
                String finalFilePath = outputFile.getAbsolutePath();

                try (FileOutputStream fos = new FileOutputStream(outputFile); ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    log("Iniciando compresión de archivos...");
                    // Se añade +1 por el Log, +1 por el Anexo Administrativo, y los archivos comunes/oferta.
                    int totalFiles = archivosComunes.size() + archivosOferta.size() + 2; 
                    int compressedCount = 0;
                    Set<String> addedEntries = new HashSet<>();
                    Set<String> addedDirs = new HashSet<>();

                    // AÑADIR EL LOG
                    String logFileName = "log_" + configuracion.getNumeroExpediente() + "_" + timeStamp + ".txt";
                    byte[] logBytes = generarContenidoLog(licitadorData, logContent).getBytes();
                    addFileToZip(zipOut, logFileName, logBytes, addedEntries);
                    compressedCount++;

                    // AÑADIR EL ANEXO ADMINISTRATIVO
                    if (anexoAdministrativoData != null) {
                        String anexoNombre = ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE + ".pdf";
                        addFileToZip(zipOut, anexoNombre, anexoAdministrativoData.getContenido(), addedEntries); 
                        log(" - Anexo Administrativo añadido en la raíz del ZIP.");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));
                    }


                    // Añadir archivos comunes
                    String comunesDirName = "Archivos Comunes/";
                    if (addedDirs.add(comunesDirName)) {
                        zipOut.putNextEntry(new ZipEntry(comunesDirName));
                        zipOut.closeEntry();
                    }
                    for (Map.Entry<String, FileData> entry : archivosComunes.entrySet()) {
                        String nombreConfigurado = entry.getKey();
                        FileData fileData = entry.getValue();
                        String extension = fileData.getExtension();
                        String zipEntryPath = comunesDirName + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                        addFileToZip(zipOut, zipEntryPath, fileData.getContenido(), addedEntries);
                        log(" - Archivo común '" + nombreConfigurado + "' añadido");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        // LÓGICA PARA CONFIDENCIALIDAD
                        if (fileData.esConfidencial()) {
                            String justificacionFileName = nombreConfigurado + "_Confidencial.txt";
                            String zipEntryPathConf = comunesDirName + justificacionFileName;
                            String contenidoConfidencial = generarContenidoConfidencialDetallado(fileData);
                            byte[] contenidoConfidencialBytes = contenidoConfidencial.getBytes();
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencialBytes, addedEntries);
                            log(" - Archivo de confidencialidad para '" + nombreConfigurado + "' añadido");
                        }
                    }

                    // Añadir documentos de oferta
                    String ofertaDirName = "Documentos Oferta/";
                    if (addedDirs.add(ofertaDirName)) {
                        zipOut.putNextEntry(new ZipEntry(ofertaDirName));
                        zipOut.closeEntry();
                    }

                    for (Map.Entry<String, FileData> entry : archivosOferta.entrySet()) {
                        String claveOriginal = entry.getKey();
                        FileData fileData = entry.getValue();
                        String extension = fileData.getExtension();
                        String nombreConfigurado = claveOriginal;
                        String carpetaLote = "";
                        String baseEntryName = "";

                        if (configuracion.isTieneLotes()) {
                            int underscoreIndex = claveOriginal.indexOf('_');
                            if (underscoreIndex != -1) {
                                String loteStr = claveOriginal.substring(0, underscoreIndex);

                                try {
                                    int numLote = Integer.parseInt(loteStr.replace("Lote", ""));
                                    if (!getParticipacionLote(numLote)) { 
                                        log(" - Archivo de oferta '" + nombreConfigurado + "' IGNORADO (Lote " + numLote + " NO marcado como Participa).");
                                        continue; 
                                    }
                                } catch (NumberFormatException e) {
                                    logError("Advertencia: Clave de lote mal formateada: " + loteStr);
                                }

                                nombreConfigurado = claveOriginal.substring(underscoreIndex + 1);
                                carpetaLote = loteStr.replace("Lote", "Lote ") + "/";
                                String dirPath = ofertaDirName + carpetaLote;
                                if (addedDirs.add(dirPath)) {
                                    zipOut.putNextEntry(new ZipEntry(dirPath));
                                    zipOut.closeEntry();
                                }
                                baseEntryName = dirPath + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                            } else {
                                logError("Advertencia: Clave de oferta '" + claveOriginal + "' sin formato LoteX_ para una licitación con lotes. Se tratará como oferta general.");
                                baseEntryName = ofertaDirName + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                            }
                        } else {
                            baseEntryName = ofertaDirName + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                        }

                        // Si hemos llegado hasta aquí, el archivo debe ser añadido
                        addFileToZip(zipOut, baseEntryName, fileData.getContenido(), addedEntries);
                        log(" - Archivo de oferta '" + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension) + "'" + (configuracion.isTieneLotes() ? " (" + carpetaLote.replace("/", "") + ")" : "") + " añadido");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        if (fileData.esConfidencial()) {
                            String nombreArchivoConfidencial = nombreConfigurado + "_Confidencial.txt";
                            byte[] contenidoConfidencial = generarContenidoConfidencialDetallado(fileData).getBytes();
                            String zipEntryPathConf = ofertaDirName + carpetaLote + nombreArchivoConfidencial;
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencial, addedEntries);
                            log(" - Archivo de confidencialidad para '" + nombreConfigurado + "' añadido");
                        }
                    }

                    log("Compresión completada correctamente en: " + finalFilePath);
                    JOptionPane.showMessageDialog(null, "Archivos comprimidos correctamente en " + finalFilePath);
                } catch (IOException e) {
                    logError("Error crítico durante la compresión: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error al comprimir los archivos. Detalles: " + e.getMessage(), "Error de Compresión", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setValue(0);
                if (onFinish != null) {
                    onFinish.run();
                }
            }
        };
        worker.execute();
    }

    // --- MÉTODOS AUXILIARES Y DE LOTES ---
    
    private void addFileToZip(ZipOutputStream zipOut, String entryPath, byte[] content, Set<String> addedEntries) throws IOException {
        if (content == null || content.length == 0) {
            logError("Advertencia: No se pudo comprimir la entrada '" + entryPath + "'. Datos nulos o vacíos.");
            return;
        }
        if (addedEntries.contains(entryPath)) {
            logError("Advertencia: La entrada '" + entryPath + "' ya fue añadida. Se ignorará la duplicada.");
            return;
        }

        ZipEntry zipEntry = new ZipEntry(entryPath);
        zipOut.putNextEntry(zipEntry);
        zipOut.write(content);
        zipOut.closeEntry();
        addedEntries.add(entryPath);
    }

    private String generarContenidoConfidencialDetallado(FileData fileData) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Declaración de Confidencialidad ---\n\n");
        sb.append("Archivo Asociado: ").append(fileData.getNombre()).append("\n");
        sb.append("Declarado confidencial por el licitador.\n\n");

        sb.append("Supuestos de confidencialidad seleccionados:\n");
        String[] supuestos = fileData.getSupuestosSeleccionados();
        String[] motivos = fileData.getMotivosSupuestos();

        if (supuestos != null && motivos != null && supuestos.length == motivos.length) {
            for (int i = 0; i < supuestos.length; i++) {
                sb.append("- Supuesto: ").append(supuestos[i]).append("\n");
                sb.append("  Motivo: ").append(motivos[i]).append("\n\n");
            }
        } else {
            sb.append("- No se pudieron recuperar los detalles de los supuestos y motivos o los datos no son consistentes.\n");
        }

        sb.append("---------------------------------------\n");
        return sb.toString();
    }

    private String generarContenidoLog(LicitadorData licitadorData, String logContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Log de ejecución de la aplicación ---\n");
        sb.append("DATOS DEL LICITADOR:\n");
        sb.append(licitadorData.toString()).append("\n");
        sb.append("Expediente: ").append(configuracion.getNumeroExpediente()).append("\n");
        sb.append("Fecha y Hora: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        sb.append("----------------------------------------\n\n");
        sb.append(logContent);
        return sb.toString();
    }

    private String crearMensajeConfirmacion() {
        StringBuilder sb = new StringBuilder("Por favor, revise la documentación cargada antes de comprimir:\n\n");

        sb.append("Archivos comunes cargados:\n");
        if (archivosComunes.isEmpty()) {
            sb.append("- Ninguno\n");
        } else {
            for (FileData fileData : archivosComunes.values()) {
                sb.append("- ").append(fileData.getNombre()).append("\n");
            }
        }
        
        if (this.anexoAdministrativoData != null) {
             sb.append("\nEl Anexo Administrativo está GENERADO y listo para incluir.\n");
        }


        sb.append("\nOfertas cargadas que serán incluidas (solo lotes marcados):\n");
        if (archivosOferta.isEmpty()) {
            sb.append("- Ninguna\n");
        } else {
            if (configuracion.isTieneLotes()) {
                Set<String> lotesConOfertas = new TreeSet<>();

                for (String clave : archivosOferta.keySet()) {
                    try {
                        if (clave.startsWith("Lote") && clave.contains("_")) {
                            String loteStr = clave.substring(0, clave.indexOf('_'));
                            int numLote = Integer.parseInt(loteStr.replace("Lote", ""));

                            if (getParticipacionLote(numLote)) { 
                                lotesConOfertas.add(loteStr);
                            }
                        }
                    } catch (Exception e) {
                        logError("Error al parsear el lote de la clave: " + clave + " - " + e.getMessage());
                    }
                }

                if (lotesConOfertas.isEmpty()) {
                    sb.append("- No hay archivos de oferta cargados en los lotes seleccionados para participación.\n");
                } else {
                    for (String lote : lotesConOfertas) {
                        sb.append("\n--- ").append(lote.replace("Lote", "Lote ")).append(" ---\n");
                        for (Map.Entry<String, FileData> entry : archivosOferta.entrySet()) {
                            String clave = entry.getKey();
                            if (clave.startsWith(lote + "_")) {
                                FileData fileData = entry.getValue();
                                sb.append("- ").append(fileData.getNombre());
                                if (fileData.esConfidencial()) {
                                    sb.append(" (CONFIDENCIAL)");
                                }
                                sb.append("\n");
                            }
                        }
                    }
                }
            } else {
                for (FileData fileData : archivosOferta.values()) {
                    sb.append("- ").append(fileData.getNombre());
                    if (fileData.esConfidencial()) {
                        sb.append(" (CONFIDENCIAL)");
                    }
                    sb.append("\n");
                }
            }
        }

        sb.append("\n¿Desea continuar con la compresión?");
        return sb.toString();
    }
    
    public boolean validarMinimoParticipacion() {
        if (!configuracion.isTieneLotes()) {
            return true;
        }

        boolean alMenosUnLoteSeleccionado = participacionPorLote.values().stream()
                .anyMatch(participa -> participa == true);

        if (!alMenosUnLoteSeleccionado) {
            logError("Validación de participación fallida: Ningún lote marcado para participar.");
            return false;
        }

        return true;
    }

    public void setParticipacionDesdeUI(Set<String> lotesSeleccionadosIds) {
        if (!configuracion.isTieneLotes()) {
            return;
        }

        participacionPorLote.clear();

        for (String loteIdStr : lotesSeleccionadosIds) {
            try {
                int loteNum = Integer.parseInt(loteIdStr);
                if (loteNum > 0 && loteNum <= configuracion.getNumLotes()) {
                    participacionPorLote.put(loteNum, true);
                } else {
                    logError("ID de lote inválido recibido de la UI: " + loteIdStr);
                }
            } catch (NumberFormatException e) {
                logError("Error de formato al parsear ID de lote: " + loteIdStr);
            }
        }

        log("Estado de participación de lotes actualizado desde la interfaz. Lotes seleccionados: " + lotesSeleccionadosIds.toString());
    }

    public boolean getParticipacionLote(int loteNum) {
        if (!configuracion.isTieneLotes()) {
            return loteNum == 1;
        }
        return participacionPorLote.getOrDefault(loteNum, false);
    }

    public boolean eliminarArchivosOfertaPorLote(String idLote) {
        String prefix = idLote.replace(" ", "") + "_";

        List<String> keysToRemove = archivosOferta.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());

        if (keysToRemove.isEmpty()) {
            return false;
        }

        for (String key : keysToRemove) {
            archivosOferta.remove(key);
        }

        return true;
    }

    // --- Getters and Setters ---
    public Map<String, FileData> getArchivosComunes() {
        return Collections.unmodifiableMap(archivosComunes);
    }

    public Map<String, FileData> getArchivosOferta() {
        return Collections.unmodifiableMap(archivosOferta);
    }
    
    public FileData getAnexoAdministrativoData() {
        return anexoAdministrativoData;
    }

    public LicitadorData getLicitadorData() {
        return licitadorData;
    }

    public void setLicitadorData(LicitadorData licitadorData) {
        this.licitadorData = Objects.requireNonNull(licitadorData, "LicitadorData no puede ser null");
    }

    public Configuracion getConfiguracion() {
        return this.configuracion;
    }
}