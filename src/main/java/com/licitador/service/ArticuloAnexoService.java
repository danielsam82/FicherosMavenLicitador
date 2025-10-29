package com.licitador.service;

import com.licitador.model.ArticuloAnexo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar la persistencia (carga y guardado) de la lista maestra
 * de ArticulosAnexo en un archivo binario.
 */
public class ArticuloAnexoService {

    // CAMBIO: Nombre del archivo de persistencia actualizado
    private static final String FILE_NAME = "master_articulos.dat"; 

    /**
     * Carga la lista de artículos desde el archivo de persistencia.
     * @return Una lista de ArticuloAnexo; lista vacía si falla o no existe el archivo.
     */
    @SuppressWarnings("unchecked")
    public List<ArticuloAnexo> cargarArticulos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            // Se asume que el objeto leído es una lista de ArticuloAnexo
            return (List<ArticuloAnexo>) ois.readObject();
        } catch (FileNotFoundException e) {
            // Es normal que el archivo no exista la primera vez.
            System.out.println("Archivo de artículos no encontrado. Creando lista vacía.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            // Otros errores de I/O o deserialización.
            e.printStackTrace();
            // Devolvemos lista vacía para no bloquear la aplicación
            return new ArrayList<>(); 
        }
    }

    /**
     * Guarda la lista de artículos en el archivo de persistencia.
     * @param articulos La lista de ArticuloAnexo a guardar.
     */
    public void guardarArticulos(List<ArticuloAnexo> articulos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(articulos);
            System.out.println("Lista de artículos guardada en: " + FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}