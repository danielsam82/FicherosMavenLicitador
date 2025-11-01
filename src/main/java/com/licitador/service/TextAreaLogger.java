package com.licitador.service;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An implementation of the {@link Logger} interface that directs log messages to a
 * {@link JTextArea} component within a Swing application.
 * <p>
 * This class is thread-safe, as it uses {@link SwingUtilities#invokeLater(Runnable)}
 * to ensure that all modifications to the JTextArea are made on the Event Dispatch Thread (EDT).
 * Messages are formatted with a prefix indicating the level (INFO, LOG, or ERROR) and a timestamp.
 * </p>
 */
public class TextAreaLogger implements Logger {
    
    private final JTextArea textArea;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Constructs a {@code TextAreaLogger}.
     *
     * @param textArea The {@link JTextArea} where the log messages will be displayed.
     * @throws IllegalArgumentException if the provided {@link JTextArea} is null.
     */
    public TextAreaLogger(JTextArea textArea) {
        if (textArea == null) {
            throw new IllegalArgumentException("JTextArea cannot be null");
        }
        this.textArea = textArea;
    }
    
    /**
     * A helper method to format and append messages to the JTextArea, ensuring
     * that it runs on the Event Dispatch Thread (EDT).
     * @param prefix The level prefix (e.g., [INFO], [ERROR]).
     * @param message The body of the message.
     */
    private void appendFormattedMessage(String prefix, String message) {
         if (textArea != null) {
             String timestamp = LocalDateTime.now().format(formatter);
             SwingUtilities.invokeLater(() -> {
                 textArea.append("[" + timestamp + "] " + prefix + message + "\n");
                 textArea.setCaretPosition(textArea.getDocument().getLength());
             });
         }
    }


    /**
     * Logs a general-level message to the {@link JTextArea}.
     *
     * @param message The general message to log.
     */
    @Override
    public void log(String message) {
        appendFormattedMessage("LOG: ", message);
    }

    /**
     * Logs an error-level message to the {@link JTextArea}.
     *
     * @param message The error message to log.
     */
    @Override
    public void logError(String message) {
        appendFormattedMessage("ERROR: ", message);
    }
    
    /**
     * Logs an info-level message to the {@link JTextArea}.
     *
     * @param message The informational message to log.
     */
    @Override
    public void logInfo(String message) {
        appendFormattedMessage("INFO: ", message);
    }
}