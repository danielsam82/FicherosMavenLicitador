package ficheros;

import interfaz.models.LicitadorData;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class FileManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Declaración de variables de instancia
    private transient Logger logger;
    private final Configuracion configuracion;
    private final Map<String, FileData> archivosComunes;
    private final Map<String, FileData> archivosOferta;
    // Mapa: Lote ID (Integer) -> Participación (Boolean)
    private final Map<Integer, Boolean> participacionPorLote; 
    private LicitadorData licitadorData;

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

    // Archivo: FileManager.java (Método corregido)

    public boolean guardarSesion() { // <<-- ¡CAMBIO CLAVE: Ahora devuelve boolean!
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

            try (FileOutputStream fos = new FileOutputStream(fileToSave);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                // [MODIFICADO]: Incluir el mapa de participación en el constructor de DatosSesionCargada
                DatosSesionCargada datosSesion = new DatosSesionCargada(
                    archivosComunes, 
                    archivosOferta, 
                    this.licitadorData,
                    // CLAVE: Serializar el mapa de participación
                    this.participacionPorLote 
                );
                oos.writeObject(datosSesion);

                log("Sesión guardada en: " + fileToSave.getPath());
                JOptionPane.showMessageDialog(null, "El progreso se ha guardado correctamente.", "Guardar Sesión", JOptionPane.INFORMATION_MESSAGE);
                return true; // <<-- DEVOLVER TRUE en caso de éxito

            } catch (IOException e) {
                logError("Error al guardar la sesión: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al guardar el progreso.", "Guardar Sesión", JOptionPane.ERROR_MESSAGE);
                return false; // <<-- DEVOLVER FALSE en caso de error
            }
        } else {
            log("Operación de guardado de sesión cancelada.");
            return false; // <<-- DEVOLVER FALSE en caso de cancelación por el usuario
        }
    }

    // Clase interna MODIFICADA para agrupar los datos de sesión para serialización
    private static class DatosSesionCargada implements Serializable {
        private static final long serialVersionUID = 2L; // <<--- [IMPORTANTE]: Se ha subido la versión
        final Map<String, FileData> archivosComunes;
        final Map<String, FileData> ofertasPorLote;
        final LicitadorData licitadorData;
        final Map<Integer, Boolean> participacionPorLote; // <<--- [NUEVO] Campo para serializar

        DatosSesionCargada(Map<String, FileData> archivosComunes, Map<String, FileData> ofertasPorLote, LicitadorData licitadorData, Map<Integer, Boolean> participacionPorLote) {
            this.archivosComunes = archivosComunes;
            this.ofertasPorLote = ofertasPorLote;
            this.licitadorData = licitadorData;
            this.participacionPorLote = participacionPorLote; // <<--- Asignación
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

            try (FileInputStream fis = new FileInputStream(fileToLoad);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {

                Object objDatosSesion = ois.readObject();
                if (objDatosSesion instanceof DatosSesionCargada) {
                    DatosSesionCargada datosSesion = (DatosSesionCargada) objDatosSesion;

                    this.archivosComunes.clear();
                    this.archivosComunes.putAll(datosSesion.archivosComunes);

                    this.archivosOferta.clear();
                    this.archivosOferta.putAll(datosSesion.ofertasPorLote);

                    this.licitadorData = datosSesion.licitadorData;
                    
                    // [MODIFICADO]: Cargar la nueva estructura de participación
                    this.participacionPorLote.clear();
                    if (datosSesion.participacionPorLote != null) {
                        this.participacionPorLote.putAll(datosSesion.participacionPorLote);
                    } else {
                        // Manejo de compatibilidad con versiones antiguas (se asume false por defecto)
                        log("Advertencia: Sesión cargada de versión antigua sin datos de participación por lote.");
                    }

                    log("Sesión cargada desde: " + fileToLoad.getPath());
                    JOptionPane.showMessageDialog(null, "Progreso cargado con éxito.", "Cargar Sesión", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    // Manejo de versiones antiguas sin el campo 'participacionPorLote' (si es que existe)
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
        participacionPorLote.clear(); // <<--- Limpiar el nuevo mapa
        this.licitadorData = new LicitadorData();
        log("Todos los datos de la sesión han sido eliminados.");
    }

    public boolean cargarArchivoComun(String nombreConfigurado, File archivoSeleccionado, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {
        // Verificar si el archivo ya está cargado y pedir confirmación para sobrescribir
        if (archivosComunes.containsKey(nombreConfigurado)) {
            int respuesta = JOptionPane.showConfirmDialog(null, "Ya existe un archivo cargado para '" + nombreConfigurado + "'. ¿Desea sobrescribirlo?", "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                log("Operación de carga de '" + nombreConfigurado + "' cancelada por el usuario.");
                return false;
            }
        }

        try {
            byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());

            // Creamos una nueva instancia de FileData con los datos de confidencialidad
            FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);

            // Almacenamos el nuevo objeto FileData en el mapa
            archivosComunes.put(nombreConfigurado, nuevoArchivo);

            // Registro del evento
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

    // CLAVE: La firma cambia de 'int numLote' a 'String loteKeyPrefix'
    public boolean cargarArchivoOferta(String nombreOferta, File archivoSeleccionado, String loteKeyPrefix, boolean esConfidencial, String[] supuestosSeleccionados, String[] motivosSupuestos) {

        // CLAVE: La clave se construye usando el prefijo (ej: "Lote1_") o cadena vacía ("")
        String clave = loteKeyPrefix + nombreOferta;

        // Check if the file is already loaded and prompt for overwrite
        if (archivosOferta.containsKey(clave)) {
            int respuesta = JOptionPane.showConfirmDialog(null, "Ya existe un archivo cargado para '" + nombreOferta + "'. ¿Desea sobrescribirlo?", "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                log("Operación de carga de '" + nombreOferta + "' cancelada por el usuario.");
                return false;
            }
        }

        try {
            byte[] fileContent = Files.readAllBytes(archivoSeleccionado.toPath());

            // Create FileData, passing the confidentiality details if available
            FileData nuevoArchivo = new FileData(archivoSeleccionado.getName(), fileContent, esConfidencial, supuestosSeleccionados, motivosSupuestos);
            archivosOferta.put(clave, nuevoArchivo);

            // Ajuste en el LOG para mostrar el lote si el prefijo no está vacío
            String logMessage = "Archivo de oferta '" + nombreOferta + "' cargado desde: " + archivoSeleccionado.getAbsolutePath();
            if (!loteKeyPrefix.isEmpty()) {
                // Extraemos el número de lote para el log (ej: "Lote1_" -> "1")
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

    /**
     * [MODIFICADO]: Comprueba archivos obligatorios SOLO para los lotes marcados como PARTICIPACIÓN.
     */
    public boolean estanArchivosObligatoriosCompletos() {
        // Check common files (SIN CAMBIOS)
        boolean[] obligatoriosComunes = configuracion.getArchivosComunesObligatorios();
        String[] nombresComunes = configuracion.getNombresArchivosComunes();
        for (int i = 0; i < nombresComunes.length; i++) {
            if (obligatoriosComunes[i] && !archivosComunes.containsKey(nombresComunes[i])) {
                log("Falta archivo común obligatorio: " + nombresComunes[i]);
                return false;
            }
        }

        // Check offer files (per lot or single offer)
        if (configuracion.isTieneLotes()) {
            // For each lot, check if all required offer files are present
            for (int loteNum = 1; loteNum <= configuracion.getNumLotes(); loteNum++) {
                
                // --- [CLAVE] Comprobamos si el licitador ha marcado la participación ---
                boolean participa = getParticipacionLote(loteNum); 
                
                if (participa) { // <<--- ¡SOLO VALIDAMOS SI PARTICIPA!
                    for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                        if (ofertaConfig.esObligatorio()) {
                            String claveEsperada = "Lote" + loteNum + "_" + ofertaConfig.getNombre();
                            if (!archivosOferta.containsKey(claveEsperada)) {
                                log("Falta oferta obligatoria '" + ofertaConfig.getNombre() + "' para el Lote " + loteNum + " (Lote marcado como Participa).");
                                return false;
                            }
                        }
                    }
                }
                // Si 'participa' es false, saltamos la validación obligatoria para este lote.
            }
        } else {
            // For a single offer, check if all required offer files are present (SIN CAMBIOS)
            for (Configuracion.ArchivoOferta ofertaConfig : configuracion.getArchivosOferta()) {
                if (ofertaConfig.esObligatorio() && !archivosOferta.containsKey(ofertaConfig.getNombre())) {
                    log("Falta oferta obligatoria: " + ofertaConfig.getNombre());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Comprime todos los archivos cargados en un archivo ZIP.
     * @param carpetaDestino La carpeta donde se guardará el archivo ZIP (seleccionada por el usuario).
     * @param zipFileName Nombre base del archivo ZIP (ej: "Oferta").
     * @param logContent Contenido del log de la sesión.
     * @param progressBar Barra de progreso para la actualización de la interfaz de usuario.
     * @param onFinish Acción a ejecutar al finalizar.
     */
    public void comprimirArchivosConProgreso(File carpetaDestino, String zipFileName, String logContent, JProgressBar progressBar, Runnable onFinish) {
        if (!estanArchivosObligatoriosCompletos()) {
            JOptionPane.showMessageDialog(null, "No se pueden comprimir los archivos. Faltan documentos obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            logError("Operación de compresión cancelada: Faltan archivos obligatorios.");
            if (onFinish != null) onFinish.run();
            return;
        }
        
        // 🚀 AÑADIDA VALIDACIÓN DE PARTICIPACIÓN MÍNIMA
        if (!validarMinimoParticipacion()) {
            JOptionPane.showMessageDialog(null, "No se pueden comprimir los archivos. En una licitación con lotes, debe seleccionar al menos un lote para participar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            logError("Operación de compresión cancelada: Ningún lote marcado para participar.");
            if (onFinish != null) onFinish.run();
            return;
        }

        String mensajeConfirmacion = crearMensajeConfirmacion();
        int confirmacion = JOptionPane.showConfirmDialog(null, mensajeConfirmacion, "Confirmar Compresión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            log("Compresión cancelada por el usuario.");
            if (onFinish != null) onFinish.run();
            return;
        }

        progressBar.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                // --- AJUSTE CLAVE: FORZAMOS EL USO DEL NIF PARA EL NOMBRE DEL ZIP ---
                String identificadorParaZip = licitadorData.getNif(); // Obtenemos el NIF

                // 1. Si el NIF está vacío (improbable por la validación), usamos la Razón Social como fallback.
                if (identificadorParaZip == null || identificadorParaZip.isEmpty()) {
                    identificadorParaZip = licitadorData.getRazonSocial();
                }

                // 2. Fallback final si todo está vacío
                if (identificadorParaZip == null || identificadorParaZip.isEmpty()) {
                    identificadorParaZip = "LicitadorDesconocido"; // Valor por defecto
                }
                
                // 3. Sanitizar (limpiar) el identificador elegido (NIF o Razón Social) de caracteres no válidos.
                String sanitizedIdentifier = identificadorParaZip.replaceAll("[^a-zA-Z0-9_.-]", "_");
                // ---------------------------------------------------------------------

                // 4. Construir el nombre completo del archivo ZIP. 
                String baseFileName = zipFileName + "_" + sanitizedIdentifier + "_" + timeStamp + ".zip";
                
                // 5. CLAVE: Crear el objeto File de destino completo (carpetaDestino + baseFileName)
                File outputFile = new File(carpetaDestino, baseFileName);
                String finalFilePath = outputFile.getAbsolutePath();


                try (FileOutputStream fos = new FileOutputStream(outputFile);
                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    log("Iniciando compresión de archivos...");
                    // Solo contamos archivos que se van a añadir (ignorando los que no participan en lotes)
                    int totalFiles = archivosComunes.size() + archivosOferta.size();
                    int compressedCount = 0;
                    Set<String> addedEntries = new HashSet<>();
                    Set<String> addedDirs = new HashSet<>();

                    // Añadir el log
                    String logFileName = "log_" + configuracion.getNumeroExpediente() + "_" + timeStamp + ".txt";
                    byte[] logBytes = generarContenidoLog(licitadorData, logContent).getBytes();
                    addFileToZip(zipOut, logFileName, logBytes, addedEntries);

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
                        log("  - Archivo común '" + nombreConfigurado + "' añadido");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        // LÓGICA PARA CONFIDENCIALIDAD DE ARCHIVOS COMUNES
                        if (fileData.esConfidencial()) {
                            String justificacionFileName = nombreConfigurado + "_Confidencial.txt";
                            String zipEntryPathConf = comunesDirName + justificacionFileName;
                            String contenidoConfidencial = generarContenidoConfidencialDetallado(fileData);
                            byte[] contenidoConfidencialBytes = contenidoConfidencial.getBytes();
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencialBytes, addedEntries);
                            log("  - Archivo de confidencialidad para '" + nombreConfigurado + "' añadido");
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
                                String loteStr = claveOriginal.substring(0, underscoreIndex); // LoteX
                                
                                // CLAVE: Comprobar si este lote está marcado como PARTICIPA antes de añadir.
                                try {
                                    int numLote = Integer.parseInt(loteStr.replace("Lote", ""));
                                    if (!getParticipacionLote(numLote)) { // <<--- [CLAVE] SI NO PARTICIPA, IGNORAR ARCHIVOS
                                        log("  - Archivo de oferta '" + nombreConfigurado + "' IGNORADO (Lote " + numLote + " NO marcado como Participa).");
                                        continue; // Saltar al siguiente archivo
                                    }
                                } catch (NumberFormatException e) {
                                       // Si el parseo falla, lo tratamos como un error pero continuamos para evitar un crash
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
                        log("  - Archivo de oferta '" + nombreConfigurado + (extension.isEmpty() ? "" : "." + extension) + "'" + (configuracion.isTieneLotes() ? " (" + carpetaLote.replace("/", "") + ")" : "") + " añadido");
                        compressedCount++;
                        publish((int) ((double) compressedCount / totalFiles * 100));

                        if (fileData.esConfidencial()) {
                            String nombreArchivoConfidencial = nombreConfigurado + "_Confidencial.txt";
                            byte[] contenidoConfidencial = generarContenidoConfidencialDetallado(fileData).getBytes();
                            String zipEntryPathConf = ofertaDirName + carpetaLote + nombreArchivoConfidencial;
                            addFileToZip(zipOut, zipEntryPathConf, contenidoConfidencial, addedEntries);
                            log("  - Archivo de confidencialidad para '" + nombreConfigurado + "' añadido");
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

    // MÉTODO AUXILIAR RESTAURADO Y ADAPTADO
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

    // MÉTODO AUXILIAR RESTAURADO Y ADAPTADO
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

    // MÉTODO AUXILIAR RESTAURADO Y ADAPTADO
    // Firma modificada para usar LicitadorData
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

    // MÉTODO AUXILIAR RESTAURADO Y ADAPTADO
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

        sb.append("\nOfertas cargadas que serán incluidas (solo lotes marcados):\n");
        if (archivosOferta.isEmpty()) {
            sb.append("- Ninguna\n");
        } else {
            if (configuracion.isTieneLotes()) {
                Set<String> lotesConOfertas = new TreeSet<>();
                
                // Primero, identificar qué lotes tienen archivos cargados Y están marcados para participar
                for (String clave : archivosOferta.keySet()) {
                    try {
                        if (clave.startsWith("Lote") && clave.contains("_")) {
                            String loteStr = clave.substring(0, clave.indexOf('_'));
                            int numLote = Integer.parseInt(loteStr.replace("Lote", ""));
                            
                            // SOLO incluimos el lote si participa
                            if (getParticipacionLote(numLote)) { // <<--- [CLAVE] Filtro por participación
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
                    // Luego, listar los archivos solo de esos lotes
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
    
    // Dentro de la clase FileManager.java

    /**
     * Comprueba si el proceso tiene lotes y, en ese caso, si al menos un lote ha sido marcado para participación.
     * Si no hay lotes, la validación siempre pasa.
     * @return true si la validación es exitosa, false si ningún lote es marcado en un proceso con lotes.
     */
    public boolean validarMinimoParticipacion() {
        // 1. Si NO tiene lotes, la validación siempre pasa.
        if (!configuracion.isTieneLotes()) {
            return true;
        }

        // 2. Si tiene lotes, buscamos al menos una participación marcada.
        // Usamos el Stream API para mayor claridad y eficiencia.
        boolean alMenosUnLoteSeleccionado = participacionPorLote.values().stream()
                                             .anyMatch(participa -> participa == true);

        if (!alMenosUnLoteSeleccionado) {
            logError("Validación de participación fallida: Ningún lote marcado para participar.");
            return false;
        }

        return true;
    }
    
    // --- LÓGICA DE PARTICIPACIÓN DE LOTES (ADAPTADA) ---

    // [MÉTODO CLAVE 1]: Recibe el estado de participación desde la UI (MainWindow)
    /**
     * Sincroniza el estado de participación de lotes desde la UI (MainWindow).
     * @param lotesSeleccionadosIds Set de IDs de lote (como String: "1", "2", etc.)
     * que están marcados como 'Sí' en la tabla.
     */
    public void setParticipacionDesdeUI(Set<String> lotesSeleccionadosIds) {
        if (!configuracion.isTieneLotes()) {
            return; // No hacer nada si no hay lotes
        }
        
        // 1. Limpiamos el mapa actual para reflejar solo la selección activa
        participacionPorLote.clear(); 
        
        // 2. Asignamos 'true' solo a los lotes seleccionados
        for (String loteIdStr : lotesSeleccionadosIds) {
            try {
                int loteNum = Integer.parseInt(loteIdStr);
                // Solo si el lote existe en la configuración, lo marcamos
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

    // [MÉTODO CLAVE 2]: Obtiene el estado de participación (usado por validaciones y UI)
    /**
     * Obtiene el estado de participación para un lote específico.
     * @param loteNum El número de lote (1, 2, etc.).
     * @return true si el lote está marcado para participación, false en caso contrario. 
     * Si es Oferta Única, devuelve true para el lote 1.
     */
    public boolean getParticipacionLote(int loteNum) {
        if (!configuracion.isTieneLotes()) {
            // En modo Oferta Única, asumimos siempre la participación en la única oferta (Lote 1).
            return loteNum == 1; 
        }
        // En modo Lotes, devuelve el valor del mapa, o 'false' si no existe.
        return participacionPorLote.getOrDefault(loteNum, false);
    }
    
    // En FileManager.java, nuevo método

    /**
     * Elimina todos los archivos de oferta asociados a un lote específico.
     * @param idLote El ID del lote (ej: "Lote 1", "Lote 2").
     * @return true si se eliminó al menos un archivo, false en caso contrario.
     */
    public boolean eliminarArchivosOfertaPorLote(String idLote) {
        // Convertimos "Lote 1" a "Lote1_" para hacer coincidir con las claves del Map
        String prefix = idLote.replace(" ", "") + "_"; 

        // Identificamos las claves que deben ser eliminadas.
        List<String> keysToRemove = archivosOferta.keySet().stream()
                                        .filter(key -> key.startsWith(prefix))
                                        .collect(Collectors.toList());

        if (keysToRemove.isEmpty()) {
            return false; // No había nada que eliminar
        }

        // Eliminamos los archivos del Map
        for (String key : keysToRemove) {
            archivosOferta.remove(key);
        }

        // Nota: La participación interna se actualiza cuando el usuario pulsa ACEPTAR en el diálogo.

        return true;
    }   
    
    // --- Getters and Setters ---
    public Map<String, FileData> getArchivosComunes() {
        return Collections.unmodifiableMap(archivosComunes);
    }

    public Map<String, FileData> getArchivosOferta() {
        return Collections.unmodifiableMap(archivosOferta);
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