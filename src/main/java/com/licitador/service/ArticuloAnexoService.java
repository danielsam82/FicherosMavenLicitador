package com.licitador.service;

import com.licitador.model.ArticuloAnexo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A service to manage the persistence (loading and saving) of the master list
 * of ArticulosAnexo in a binary file.
 */
public class ArticuloAnexoService {

    private static final String FILE_NAME = "master_articulos.dat"; 

    /**
     * Loads the list of articles from the persistence file.
     * @return A list of ArticuloAnexo; an empty list if it fails or the file does not exist.
     */
    @SuppressWarnings("unchecked")
    public List<ArticuloAnexo> cargarArticulos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<ArticuloAnexo>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Article file not found. Creating empty list.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>(); 
        }
    }

    /**
     * Saves the list of articles to the persistence file.
     * @param articulos The list of ArticuloAnexo to save.
     */
    public void guardarArticulos(List<ArticuloAnexo> articulos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(articulos);
            System.out.println("List of articles saved in: " + FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}