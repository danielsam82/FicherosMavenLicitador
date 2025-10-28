package com.licitador.model;

import java.io.Serializable;

/**
 * Modelo de datos mutable (Java Bean) que almacena la información completa
 * del licitador/empresa que presenta la oferta.
 *
 * Implementa {@link Serializable} para permitir que los datos del licitador
 * se guarden y carguen junto con el estado de la sesión.
 */
public class LicitadorData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String razonSocial;
    private String nif;
    private boolean esPyme;

    /**
     * Indica si la empresa licitadora es extranjera (no española).
     */
    private boolean esExtranjera;

    private String domicilio;
    private String telefono;
    private String email;

    /**
     * Constructor por defecto. Inicializa todos los campos con cadenas vacías
     * y los indicadores booleanos a {@code false}.
     */
    public LicitadorData() {
        this.razonSocial = "";
        this.nif = "";
        this.esPyme = false;
        this.esExtranjera = false;
        this.domicilio = "";
        this.telefono = "";
        this.email = "";
    }

    // --- Getters y Setters ---

    /**
     * Obtiene la razón social (nombre) del licitador.
     * @return La razón social.
     */
    public String getRazonSocial() { return razonSocial; }

    /**
     * Establece la razón social (nombre) del licitador.
     * @param razonSocial La nueva razón social.
     */
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    /**
     * Obtiene el Número de Identificación Fiscal (NIF o CIF) del licitador.
     * @return El NIF/CIF.
     */
    public String getNif() { return nif; }

    /**
     * Establece el Número de Identificación Fiscal (NIF o CIF) del licitador.
     * @param nif El nuevo NIF/CIF.
     */
    public void setNif(String nif) { this.nif = nif; }

    /**
     * Indica si el licitador es una Pequeña y Mediana Empresa (PYME).
     * @return {@code true} si es PYME; {@code false} en caso contrario.
     */
    public boolean esPyme() { return esPyme; }

    /**
     * Establece si el licitador es una PYME.
     * @param esPyme {@code true} si es PYME.
     */
    public void setEsPyme(boolean esPyme) { this.esPyme = esPyme; }

    /**
     * Indica si la empresa licitadora tiene domicilio en el extranjero.
     * @return {@code true} si la empresa es extranjera; {@code false} en caso contrario.
     */
    public boolean esExtranjera() { return esExtranjera; }

    /**
     * Establece si la empresa licitadora es extranjera.
     * @param esExtranjera {@code true} si la empresa es extranjera.
     */
    public void setEsExtranjera(boolean esExtranjera) { this.esExtranjera = esExtranjera; }

    /**
     * Obtiene el domicilio completo del licitador.
     * @return El domicilio.
     */
    public String getDomicilio() { return domicilio; }

    /**
     * Establece el domicilio completo del licitador.
     * @param domicilio El nuevo domicilio.
     */
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    /**
     * Obtiene el número de teléfono de contacto.
     * @return El teléfono.
     */
    public String getTelefono() { return telefono; }

    /**
     * Establece el número de teléfono de contacto.
     * @param telefono El nuevo teléfono.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Obtiene la dirección de correo electrónico de contacto.
     * @return El email.
     */
    public String getEmail() { return email; }

    /**
     * Establece la dirección de correo electrónico de contacto.
     * @param email El nuevo email.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Genera una representación en cadena de texto de los datos del licitador,
     * útil para el registro (log).
     *
     * @return Una cadena de texto con la información detallada del licitador.
     */
    @Override
    public String toString() {
        return "Razón Social: " + razonSocial + "\n" +
               "NIF: " + nif + "\n" +
               "PYME: " + (esPyme ? "Sí" : "No") + "\n" +
               "Extranjera: " + (esExtranjera ? "Sí" : "No") + "\n" +
               "Domicilio: " + domicilio + "\n" +
               "Teléfono: " + telefono + "\n" +
               "Email: " + email;
    }
}