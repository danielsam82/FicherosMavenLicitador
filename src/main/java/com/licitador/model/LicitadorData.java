package com.licitador.model;

import java.io.Serializable;

/**
 * A mutable data model (Java Bean) that stores the complete information of the
 * bidder/company submitting the offer.
 * <p>
 * Implements {@link Serializable} to allow the bidder's data to be saved and
 * loaded along with the session state.
 */
public class LicitadorData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String razonSocial;
    private String nif;
    private boolean esPyme;
    private boolean esExtranjera;
    private String domicilio;
    private String telefono;
    private String email;

    /**
     * Default constructor. Initializes all fields with empty strings and
     * boolean flags to {@code false}.
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

    /**
     * Gets the corporate name of the bidder.
     *
     * @return The corporate name.
     */
    public String getRazonSocial() { return razonSocial; }

    /**
     * Sets the corporate name of the bidder.
     *
     * @param razonSocial The new corporate name.
     */
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    /**
     * Gets the Tax Identification Number (NIF or CIF) of the bidder.
     *
     * @return The NIF/CIF.
     */
    public String getNif() { return nif; }

    /**
     * Sets the Tax Identification Number (NIF or CIF) of the bidder.
     *
     * @param nif The new NIF/CIF.
     */
    public void setNif(String nif) { this.nif = nif; }

    /**
     * Indicates whether the bidder is a Small and Medium-sized Enterprise (SME).
     *
     * @return {@code true} if it is an SME; {@code false} otherwise.
     */
    public boolean esPyme() { return esPyme; }

    /**
     * Sets whether the bidder is an SME.
     *
     * @param esPyme {@code true} if it is an SME.
     */
    public void setEsPyme(boolean esPyme) { this.esPyme = esPyme; }

    /**
     * Indicates whether the bidding company is based abroad.
     *
     * @return {@code true} if the company is foreign; {@code false} otherwise.
     */
    public boolean esExtranjera() { return esExtranjera; }

    /**
     * Sets whether the bidding company is foreign.
     *
     * @param esExtranjera {@code true} if the company is foreign.
     */
    public void setEsExtranjera(boolean esExtranjera) { this.esExtranjera = esExtranjera; }

    /**
     * Gets the full address of the bidder.
     *
     * @return The address.
     */
    public String getDomicilio() { return domicilio; }

    /**
     * Sets the full address of the bidder.
     *
     * @param domicilio The new address.
     */
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    /**
     * Gets the contact phone number.
     *
     * @return The phone number.
     */
    public String getTelefono() { return telefono; }

    /**
     * Sets the contact phone number.
     *
     * @param telefono The new phone number.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Gets the contact email address.
     *
     * @return The email address.
     */
    public String getEmail() { return email; }

    /**
     * Sets the contact email address.
     *
     * @param email The new email address.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Generates a string representation of the bidder's data, useful for logging.
     *
     * @return A string with the detailed information of the bidder.
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