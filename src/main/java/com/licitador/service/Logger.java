package com.licitador.service;

public interface Logger {
    void log(String message);
    void logError(String message);
    
    // --- AÑADIR ESTE MÉTODO ---
    void logInfo(String message);
    // --------------------------
}

