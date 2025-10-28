package com.licitador.service;

import com.licitador.model.AnexoAdministrativo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AnexoAdministrativoService {

    // Archivo donde se guardarán los anexos en la carpeta de la aplicación
    private static final String FILE_NAME = "master_anexos.dat";

    /**
     * Carga la lista maestra de anexos desde el archivo de persistencia.
     */
    public List<AnexoAdministrativo> cargarAnexos() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Se asume que guardamos la lista completa
            return (List<AnexoAdministrativo>) ois.readObject();
        } catch (Exception e) {
            // Si hay error, lo registramos (si tuviéramos acceso a un logger estático) 
            // y devolvemos una lista vacía para no bloquear la aplicación.
            System.err.println("Error cargando anexos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista maestra de anexos en el archivo de persistencia.
     */
    public void guardarAnexos(List<AnexoAdministrativo> anexos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(anexos);
        } catch (IOException e) {
            System.err.println("Error guardando anexos: " + e.getMessage());
        }
    }
}
