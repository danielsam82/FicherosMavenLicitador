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

/**
 * Manages the files and data for the tender process, including session management,
 * file loading, validation, and compression.
 */
public class FileManager implements Serializable {

    private FileData anexoAdministrativoData;
    private static final String ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE = "Anexo Administrativo";

    private transient Logger logger;
    private final Configuracion configuracion;
    private final Map<String, FileData> archivosComunes;
    private final Map<String, FileData> archivosOferta;
    private final Map<Integer, Boolean> participacionPorLote;
    private LicitadorData licitadorData;

    /**
     * Constructs a new FileManager.
     *
     * @param configuracion The tender configuration.
     * @param logger The logger to use for logging messages.
     */
    public FileManager(Configuracion configuracion, Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null");
        }
        this.logger = logger;
        this.configuracion = Objects.requireNonNull(configuracion, "Configuración cannot be null");
        this.archivosComunes = new HashMap<>();
        this.archivosOferta = new LinkedHashMap<>();
        this.participacionPorLote = new HashMap<>();
        this.licitadorData = new LicitadorData();
    }

    // Custom deserialization to re-initialize transient logger
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // NOTE: The logger must be re-initialized externally if a singleton is used.
    }

    /**
     * Sets the logger for this FileManager.
     * @param logger The logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Logs a message using the configured logger.
     * @param message The message to log.
     */
    public void log(String message) {
        if (logger != null) {
            logger.log(message);
        }
    }

    /**
     * Logs an error message using the configured logger.
     * @param message The error message to log.
     */
    public void logError(String message) {
        if (logger != null) {
            logger.logError(message);
        }
    }
    
    /**
     * Logs an info message using the configured logger.
     * @param message The info message to log.
     */
    public void logInfo(String message) {
         if (logger != null) {
            logger.log(message);
        }
    }
    
    // --- LÓGICA DE GESTIÓN DEL ANEXO ADMINISTRATIVO ---
    
    /**
     * Validates if the bidder's data and lot selection are sufficient to generate the Administrative Annex.
     * @return true if the data is ready.
     */
    public boolean validarDatosAdministrativosParaAnexo() {
        if (!validarMinimoParticipacion()) {
            return false;
        }
        
        if (licitadorData.getNif() == null || licitadorData.getNif().trim().isEmpty() ||
            licitadorData.getRazonSocial() == null || licitadorData.getRazonSocial().trim().isEmpty()) {
             logError("Administrative validation failed: Missing mandatory bidder data (NIF/Company Name).");
             return false;
        }

        return true;
    }

    /**
     * Generates the Administrative Annex in PDF format with the bidder's data and the configuration,
     * and saves it in its dedicated field.
     *
     * @return true if the annex was generated and loaded correctly.
     */
    public boolean generarAnexoAdministrativoYGuardar() {
        
        if (!validarDatosAdministrativosParaAnexo()) {
            JOptionPane.showMessageDialog(null, "Cannot generate the Annex. Missing mandatory bidder data or no lots selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            byte[] pdfContent = PDFGenerator.generarAnexoAdministrativo(
                    this.licitadorData,
                    this.participacionPorLote,
                    this.configuracion
            );

            String nombreFinalArchivo = ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE + ".pdf";

            FileData anexoData = new FileData(
                    nombreFinalArchivo,
                    pdfContent,
                    false,
                    null,
                    null
            );

            this.anexoAdministrativoData = anexoData; 

            logger.log("Administrative Annex (" + nombreFinalArchivo + ") generated and ready for compression.");
            return true;

        } catch (DocumentException e) {
            logError("Error generating the Administrative Annex PDF (DocumentException): " + e.getMessage());
        } catch (IOException e) {
            logError("I/O error generating the Administrative Annex PDF: " + e.getMessage());
        } catch (Exception e) {
             logError("Unexpected error generating the Administrative Annex: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Validates if the bidder has completed all the mandatory requirements to proceed with the final compression.
     *
     * @return true if the offer is ready to be compressed.
     */
    public boolean validarOfertaCompleta() {

        if (this.anexoAdministrativoData == null || this.anexoAdministrativoData.getContenido() == null || this.anexoAdministrativoData.getContenido().length == 0) {
            logError("Compression validation failed: Administrative Annex is missing. Please generate it first.");
            return false;
        }

        if (!estanArchivosObligatoriosCompletos()) {
            return false;
        }
        
        logInfo("Final validation successful: All mandatory requirements are covered.");
        return true;
    }

    /**
     * Saves the current session to a file.
     * @return true if the session was saved successfully.
     */
    public boolean guardarSesion() { 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Session Progress");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Session File (*.dat)", "dat"));

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

                log("Session saved to: " + fileToSave.getPath());
                JOptionPane.showMessageDialog(null, "Progress has been saved successfully.", "Save Session", JOptionPane.INFORMATION_MESSAGE);
                return true;

            } catch (IOException e) {
                logError("Error saving session: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error saving progress.", "Save Session", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            log("Save session operation canceled.");
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

    /**
     * Loads a session from a file.
     * @return true if the session was loaded successfully.
     */
    public boolean cargarSesion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Session Progress");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Session File (*.dat)", "dat"));

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            if (!fileToLoad.exists() || !fileToLoad.canRead()) {
                JOptionPane.showMessageDialog(null, "Cannot read the selected file.", "Load Error", JOptionPane.ERROR_MESSAGE);
                logError("Could not load session: File does not exist or is not readable.");
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
                        log("Warning: Session loaded from an older version without lot participation data.");
                    }
                    
                    this.anexoAdministrativoData = null;

                    log("Session loaded from: " + fileToLoad.getPath());
                    JOptionPane.showMessageDialog(null, "Progress loaded successfully.", "Load Session", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    throw new IOException("Invalid or incompatible session file format. Ensure the file is from version 2 or higher.");
                }

            } catch (IOException | ClassNotFoundException e) {
                logError("Error loading session: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error loading saved progress. The file may be corrupt or from a different version.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            log("Load session operation canceled.");
            return false;
        }
    }

    /**
     * Resets all session data.
     */
    public void resetData() {
        archivosComunes.clear();
        archivosOferta.clear();
        participacionPorLote.clear();
        this.licitadorData = new LicitadorData();
        this.anexoAdministrativoData = null; 
        log("All session data has been deleted.");
    }
    
    // --- MÉTODOS DE CARGA DE ARCHIVOS ---
    
    /**
     * Loads a common file.
     * @param nombreConfigurado The configured name of the file.
     * @param archivoSeleccionado The selected file.
     * @param esConfidencial Whether the file is confidential.
     * @param supuestosSeleccionados The selected confidentiality assumptions.
     * @param motivosSupuestos The reasons for the confidentiality assumptions.
     * @return true if the file was loaded successfully.
     */
    public boolean cargarArchivoComun(String nombreConfigurado, File archivoSeleccionado, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        if (archivosComunes.containsKey(nombreConfigurado)) {
             int respuesta = JOptionPane.showConfirmDialog(null, "A file already exists for '" + nombreConfigurado + "'. Do you want to overwrite it?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
             if (respuesta != JOptionPane.YES_OPTION) {
                 log("Load operation for '" + nombreConfigurado + "' canceled by user.");
                 return false;
             }
        }

        try {
             byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());
             FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);
             archivosComunes.put(nombreConfigurado, nuevoArchivo);

             String logMessage = "Common file '" + nombreConfigurado + "' loaded from: " + archivoSeleccionado.getAbsolutePath();
             if (esConfidencial) {
                 logMessage += " (CONFIDENTIAL)";
             }
             log(logMessage);

             return true;
        } catch (IOException ex) {
             logError("Error loading file '" + nombreConfigurado + "': " + ex.getMessage());
             JOptionPane.showMessageDialog(null, "Error reading the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
    }

    /**
     * Loads an offer file.
     * @param nombreOferta The name of the offer.
     * @param archivoSeleccionado The selected file.
     * @param loteKeyPrefix The lot key prefix.
     * @param esConfidencial Whether the file is confidential.
     * @param supuestosSeleccionados The selected confidentiality assumptions.
     * @param motivosSupuestos The reasons for the confidentiality assumptions.
     * @return true if the file was loaded successfully.
     */
    public boolean cargarArchivoOferta(String nombreOferta, File archivoSeleccionado, String loteKeyPrefix, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        String clave = loteKeyPrefix + nombreOferta;

        if (archivosOferta.containsKey(clave)) {
            int respuesta = JOptionPane.showConfirmDialog(null, "A file already exists for '" + nombreOferta + "'. Do you want to overwrite it?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                log("Load operation for '" + nombreOferta + "' canceled by user.");
                return false;
            }
        }

        try {
            byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());
            FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);
            archivosOferta.put(clave, nuevoArchivo);

            String logMessage = "Offer file '" + nombreOferta + "' loaded from: " + archivoSeleccionado.getAbsolutePath();
            if (!loteKeyPrefix.isEmpty()) {
                String numLoteStr = loteKeyPrefix.replace("Lote", "").replace("_", "");
                logMessage += " for Lot " + numLoteStr;
            }
            log(logMessage);

            return true;
        } catch (IOException ex) {
            logError("Error loading offer file: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error reading the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Checks if all mandatory files are complete.
     * @return true if all mandatory files are complete.
     */
    public boolean estanArchivosObligatoriosCompletos() {
        boolean[] obligatoriosComunes = configuracion.getArchivosComunesObligatorios();
        String[] nombresComunes = configuracion.getNombresArchivosComunes();
        for (int i = 0; i < nombresComunes.length; i++) {
            if (obligatoriosComunes[i] && !archivosComunes.containsKey(nombresComunes[i])) {
                logError("Missing mandatory common file: " + nombresComunes[i]);
                return false;
            }
        }

        if (configuracion.isTieneLotes()) {
            for (int loteNum = 1; loteNum <= configuracion.getNumLotes(); loteNum++) {
                boolean participa = getParticipacionLote(loteNum);

                if (participa) { 
                    for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                        if (ofertaConfig.esObligatorio()) {
                            String claveEsperada = "Lote" + loteNum + "_" + ofertaConfig.getNombre();
                            if (!archivosOferta.containsKey(claveEsperada)) {
                                logError("Missing mandatory offer '" + ofertaConfig.getNombre() + "' for Lot " + loteNum + " (Lot marked as participating).");
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                if (ofertaConfig.esObligatorio() && !archivosOferta.containsKey(ofertaConfig.getNombre())) {
                    logError("Missing mandatory offer: " + ofertaConfig.getNombre());
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Compresses the files with a progress bar.
     * @param carpetaDestino The destination folder.
     * @param zipFileName The name of the zip file.
     * @param logContent The content of the log file.
     * @param progressBar The progress bar to update.
     * @param onFinish A runnable to execute when the compression is finished.
     */
    public void comprimirArchivosConProgreso(File carpetaDestino, String zipFileName, String logContent, JProgressBar progressBar, Runnable onFinish) {
        
        if (!validarOfertaCompleta()) { 
            JOptionPane.showMessageDialog(null, "Cannot compress files. Missing mandatory documents or the Administrative Annex.", "Warning", JOptionPane.WARNING_MESSAGE);
            logError("Compression operation canceled: Full validation failed.");
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        String mensajeConfirmacion = crearMensajeConfirmacion();
        int confirmacion = JOptionPane.showConfirmDialog(null, mensajeConfirmacion, "Confirm Compression", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            log("Compression canceled by user.");
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
                    identificadorParaZip = "UnknownBidder";
                }

                String sanitizedIdentifier = identificadorParaZip.replaceAll("[^a-zA-Z0-9_.-]", "_");
                String baseFileName = zipFileName + "_" + sanitizedIdentifier + "_" + timeStamp + ".zip";
                File outputFile = new File(carpetaDestino, baseFileName);
                String finalFilePath = outputFile.getAbsolutePath();

                try (FileOutputStream fos = new FileOutputStream(outputFile); ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    log("Starting file compression...");
                    int totalFiles = archivosComunes.size() + archivosOferta.size() + 2; 
                    int compressedCount = 0;
                    Set<String> addedEntries = new HashSet<>();
                    Set<String> addedDirs = new HashSet<>();

                    String logFileName = "log_" + configuracion.getNumeroExpediente() + "_" + timeStamp + ".txt";
                    byte[] logBytes = generarContenidoLog(licitadorData, logContent).getBytes();
                    addFileToZip(zipOut, logFileName, logBytes, addedEntries);
                    compressedCount++;

                    if (anexoAdministrativoData != null) {
                        String anexoNombre = ANEXO_ADMINISTRATIVO_NOMBRE_CLAVE + ".pdf";
                        addFileToZip(zipOut, anexoNombre, anexoAdministrativoData.getContenido(), addedEntries); 
                        log(" - Administrative Annex added to the root of the ZIP.");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));
                    }

                    String comunesDirName = "Common Files/";
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
                        log(" - Common file '" + nombreConfigurado + "' added");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        if (fileData.esConfidencial()) {
                            String justificacionFileName = nombreConfigurado + "_Confidential.txt";
                            String zipEntryPathConf = comunesDirName + justificacionFileName;
                            String contenidoConfidencial = generarContenidoConfidencialDetallado(fileData);
                            byte[] contenidoConfidencialBytes = contenidoConfidencial.getBytes();
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencialBytes, addedEntries);
                            log(" - Confidentiality file for '" + nombreConfigurado + "' added");
                        }
                    }

                    String ofertaDirName = "Offer Documents/";
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
                                        log(" - Offer file '" + nombreConfigurado + "' IGNORED (Lot " + numLote + " NOT marked as participating).");
                                        continue; 
                                    }
                                } catch (NumberFormatException e) {
                                    logError("Warning: Malformed lot key: " + loteStr);
                                }

                                nombreConfigurado = claveOriginal.substring(underscoreIndex + 1);
                                carpetaLote = loteStr.replace("Lote", "Lot ") + "/";
                                String dirPath = ofertaDirName + carpetaLote;
                                if (addedDirs.add(dirPath)) {
                                    zipOut.putNextEntry(new ZipEntry(dirPath));
                                    zipOut.closeEntry();
                                }
                                baseEntryName = dirPath + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                            } else {
                                logError("Warning: Offer key '" + claveOriginal + "' without LoteX_ format for a tender with lots. It will be treated as a general offer.");
                                baseEntryName = ofertaDirName + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                            }
                        } else {
                            baseEntryName = ofertaDirName + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension);
                        }

                        addFileToZip(zipOut, baseEntryName, fileData.getContenido(), addedEntries);
                        log(" - Offer file '" + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension) + "'" + (configuracion.isTieneLotes() ? " (" + carpetaLote.replace("/", "") + ")" : "") + " added");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        if (fileData.esConfidencial()) {
                            String nombreArchivoConfidencial = nombreConfigurado + "_Confidential.txt";
                            byte[] contenidoConfidencial = generarContenidoConfidencialDetallado(fileData).getBytes();
                            String zipEntryPathConf = ofertaDirName + carpetaLote + nombreArchivoConfidencial;
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencial, addedEntries);
                            log(" - Confidentiality file for '" + nombreConfigurado + "' added");
                        }
                    }

                    log("Compression completed successfully at: " + finalFilePath);
                    JOptionPane.showMessageDialog(null, "Files compressed successfully at " + finalFilePath);
                } catch (IOException e) {
                    logError("Critical error during compression: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error compressing files. Details: " + e.getMessage(), "Compression Error", JOptionPane.ERROR_MESSAGE);
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
    
    /**
     * Validates that at least one lot has been selected for participation.
     * @return true if the minimum participation is met.
     */
    public boolean validarMinimoParticipacion() {
        if (!configuracion.isTieneLotes()) {
            return true;
        }

        boolean alMenosUnLoteSeleccionado = participacionPorLote.values().stream()
                .anyMatch(participa -> participa == true);

        if (!alMenosUnLoteSeleccionado) {
            logError("Participation validation failed: No lot marked for participation.");
            return false;
        }

        return true;
    }

    /**
     * Sets the participation from the UI.
     * @param lotesSeleccionadosIds The IDs of the selected lots.
     */
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
                    logError("Invalid lot ID received from UI: " + loteIdStr);
                }
            } catch (NumberFormatException e) {
                logError("Error parsing lot ID format: " + loteIdStr);
            }
        }

        log("Lot participation status updated from the interface. Selected lots: " + lotesSeleccionadosIds.toString());
    }

    /**
     * Gets the participation status for a specific lot.
     * @param loteNum The lot number.
     * @return true if participating in the lot.
     */
    public boolean getParticipacionLote(int loteNum) {
        if (!configuracion.isTieneLotes()) {
            return loteNum == 1;
        }
        return participacionPorLote.getOrDefault(loteNum, false);
    }

    /**
     * Deletes the offer files for a specific lot.
     * @param idLote The lot ID.
     * @return true if files were deleted.
     */
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

    /** @return An unmodifiable map of the common files. */
    public Map<String, FileData> getArchivosComunes() {
        return Collections.unmodifiableMap(archivosComunes);
    }

    /** @return An unmodifiable map of the offer files. */
    public Map<String, FileData> getArchivosOferta() {
        return Collections.unmodifiableMap(archivosOferta);
    }
    
    /** @return The administrative annex data. */
    public FileData getAnexoAdministrativoData() {
        return anexoAdministrativoData;
    }

    /** @return The bidder data. */
    public LicitadorData getLicitadorData() {
        return licitadorData;
    }

    /** @param licitadorData The bidder data to set. */
    public void setLicitadorData(LicitadorData licitadorData) {
        this.licitadorData = Objects.requireNonNull(licitadorData, "LicitadorData cannot be null");
    }

    /** @return The configuration. */
    public Configuracion getConfiguracion() {
        return this.configuracion;
    }
}