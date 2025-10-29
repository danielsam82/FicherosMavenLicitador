package com.licitador.model;

import java.io.Serializable;
import java.util.Objects; // Necesario para equals y hashCode

/**
 * Representa un fragmento modular (Artículo) que se ensamblará
 * en el Anexo Global de Adhesión que el licitador debe aceptar.
 */
public class ArticuloAnexo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String idArticulo;              // Identificador único (ej: "Art_1_Solvencia")
    private int orden;                      // Posición dentro del Anexo Global (1, 2, 3...)
    private String titulo;                  // Título del artículo (ej: "Artículo Primero: Solvencia Económica")
    private String contenidoFormato;        // Texto del fragmento (puede contener etiquetas <DATO_LICITADOR ETQ="...">)
    private boolean requiereFirma;          // Si el licitador debe firmar específicamente este fragmento
    
    // 1. CONSTRUCTOR COMPLETO
    public ArticuloAnexo(String idArticulo, int orden, String titulo, String contenidoFormato, boolean requiereFirma) {
        this.idArticulo = idArticulo;
        this.orden = orden;
        this.titulo = titulo;
        this.contenidoFormato = contenidoFormato;
        this.requiereFirma = requiereFirma;
    }
    
    // 2. CONSTRUCTOR VACÍO 
    public ArticuloAnexo() {
        // Constructor por defecto
    }
    
    // 3. GETTERS Y SETTERS
    
    public String getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(String idArticulo) {
        this.idArticulo = idArticulo;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenidoFormato() {
        return contenidoFormato;
    }

    public void setContenidoFormato(String contenidoFormato) {
        this.contenidoFormato = contenidoFormato;
    }

    public boolean isRequiereFirma() {
        return requiereFirma;
    }
    
    // Nota: El setter para booleanos a menudo usa 'set' en lugar de 'is'
    public void setRequiereFirma(boolean requiereFirma) {
        this.requiereFirma = requiereFirma;
    }
    
    // 4. MÉTODOS DE OBJETO (Para correcta gestión en listas y mapas)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticuloAnexo that = (ArticuloAnexo) o;
        return Objects.equals(idArticulo, that.idArticulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idArticulo);
    }

    @Override
    public String toString() {
        return "(" + orden + ") " + titulo + " [" + idArticulo + "]";
    }
}