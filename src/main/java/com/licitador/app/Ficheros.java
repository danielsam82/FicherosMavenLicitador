package com.licitador.app;

import com.licitador.ui.MainWindow;
import javax.swing.*;
import java.io.Serializable;

/**
 * Clase principal que contiene el método {@code main} y sirve como punto de
 * entrada para la aplicación de gestión de archivos de licitación.
 * <p>
 * Inicializa la interfaz gráfica de usuario (GUI) en el Event Dispatch Thread
 * (EDT) creando una instancia de {@link MainWindow}.
 * </p>
 *
 * @author Daniel Rubio Vargas
 */
public class Ficheros {

    /**
     * Constructor por defecto de la clase {@code Ficheros}. Aunque no se invoca
     * directamente de forma explícita, se documenta para cumplir con los
     * estándares de Javadoc y evitar advertencias.
     */
    public Ficheros() {
        // No hay lógica de inicialización aquí, ya que el método main se encarga de la GUI.
    }

    /**
     * El método principal de la aplicación.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        // Garantiza que la inicialización de la GUI se realiza en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
