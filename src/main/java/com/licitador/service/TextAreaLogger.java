package com.licitador.service;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación de la interfaz {@link Logger} que dirige la salida de los mensajes
 * de registro (logs) a un componente {@link JTextArea} dentro de una aplicación Swing.
 *
 * Esta clase es segura para hilos, ya que utiliza {@link SwingUtilities#invokeLater(Runnable)}
 * para asegurar que todas las modificaciones al JTextArea se realicen en el Event Dispatch Thread (EDT).
 * Los mensajes se formatean con un prefijo indicando el nivel (INFO, LOG o ERROR) y una marca de tiempo.
 */
public class TextAreaLogger implements Logger {
    
    private final JTextArea textArea;
    // Uso de java.time.format para manejo moderno de fechas
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Constructor para {@code TextAreaLogger}.
     *
     * @param textArea El {@link JTextArea} donde se mostrarán los mensajes de registro.
     * @throws IllegalArgumentException si el {@link JTextArea} proporcionado es nulo.
     */
    public TextAreaLogger(JTextArea textArea) {
        if (textArea == null) {
            throw new IllegalArgumentException("JTextArea no puede ser null");
        }
        this.textArea = textArea;
    }
    
    /**
     * Método auxiliar para formatear y añadir mensajes al JTextArea, asegurando
     * que se ejecuta en el Event Dispatch Thread (EDT).
     * * @param prefix El prefijo del nivel (ej: [INFO], [ERROR]).
     * @param message El cuerpo del mensaje.
     */
    private void appendFormattedMessage(String prefix, String message) {
         if (textArea != null) {
             String timestamp = LocalDateTime.now().format(formatter);
             SwingUtilities.invokeLater(() -> {
                 // Formato: [HH:mm:ss] [PREFIJO] Mensaje
                 textArea.append("[" + timestamp + "] " + prefix + message + "\n");
                 // Desplazar al final para ver los últimos mensajes
                 textArea.setCaretPosition(textArea.getDocument().getLength());
             });
         }
    }


    /**
     * Registra un mensaje de nivel LOG (general) en el {@link JTextArea}.
     *
     * @param message El mensaje general a registrar.
     */
    @Override
    public void log(String message) {
        appendFormattedMessage("LOG: ", message);
    }

    /**
     * Registra un mensaje de nivel ERROR en el {@link JTextArea}.
     *
     * @param message El mensaje de error a registrar.
     */
    @Override
    public void logError(String message) {
        // Tu versión original usaba "[ERROR] ", pero lo adapto al formato con timestamp.
        appendFormattedMessage("ERROR: ", message);
    }
    
    /**
     * Registra un mensaje de nivel INFO en el {@link JTextArea}.
     *
     * Este método es crucial para corregir el error de compilación en ConfiguradorApp.
     *
     * @param message El mensaje de información a registrar.
     */
    @Override
    public void logInfo(String message) {
        // Implementación del método que faltaba
        appendFormattedMessage("INFO: ", message);
    }
}