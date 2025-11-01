package com.licitador.service;

/**
 * An interface for logging messages.
 */
public interface Logger {
    /**
     * Logs a general message.
     * @param message The message to log.
     */
    void log(String message);

    /**
     * Logs an error message.
     * @param message The error message to log.
     */
    void logError(String message);
    
    /**
     * Logs an informational message.
     * @param message The info message to log.
     */
    void logInfo(String message);
}

