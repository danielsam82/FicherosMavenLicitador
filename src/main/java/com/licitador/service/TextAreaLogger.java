package com.licitador.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.Serializable;

/**
 * Implementación de la interfaz {@link Logger} que dirige la salida de los mensajes
 * de registro (logs) a un componente {@link JTextArea} dentro de una aplicación Swing.
 *
 * Esta clase es segura para hilos, ya que utiliza {@link SwingUtilities#invokeLater(Runnable)}
 * para asegurar que todas las modificaciones al JTextArea se realicen en el Event Dispatch Thread (EDT).
 * Los mensajes se formatean con un prefijo indicando el nivel (INFO o ERROR).
 */
public class TextAreaLogger implements Logger {
    private final JTextArea textArea;

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
     * Registra un mensaje de nivel INFO en el {@link JTextArea}.
     *
     * El mensaje se prefija con "[INFO] " y se añade una nueva línea.
     * La operación se ejecuta de forma asíncrona en el Event Dispatch Thread (EDT).
     *
     * @param message El mensaje de información a registrar.
     */
    @Override
    public void log(String message) {
        // La verificación de null ya se hace en el constructor, pero se mantiene para robustez.
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> {
                // Se utiliza append para añadir el mensaje al final del área de texto
                textArea.append("[INFO] " + message + "\n");
            });
        }
    }

    /**
     * Registra un mensaje de nivel ERROR en el {@link JTextArea}.
     *
     * El mensaje se prefija con "[ERROR] " y se añade una nueva línea.
     * La operación se ejecuta de forma asíncrona en el Event Dispatch Thread (EDT).
     *
     * @param message El mensaje de error a registrar.
     */
    @Override
    public void logError(String message) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> {
                // Se utiliza append para añadir el mensaje de error al final del área de texto
                textArea.append("[ERROR] " + message + "\n");
            });
        }
    }
}