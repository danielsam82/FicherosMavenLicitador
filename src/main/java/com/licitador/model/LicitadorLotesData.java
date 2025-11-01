package com.licitador.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class encapsulates the complete information of the bidder (personal and company data)
 * along with the selection of the specific lots to which they wish to submit a bid.
 * <p>
 * This class is the central entity for saving and restoring the bidder's participation
 * status in the tender.
 * <p>
 * Implements {@link Serializable} to allow persistence (session saving/loading).
 */
public class LicitadorLotesData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The object containing the bidder's personal and company information.
     */
    private LicitadorData licitador;

    /**
     * A set of lot IDs (usually Strings) that the bidder has selected to participate in.
     * If the set is empty, it is assumed that they are not participating in any lot.
     */
    private Set<String> lotesSeleccionadosIds;

    /**
     * Default constructor. Initializes the bidder's data with a new {@code LicitadorData}
     * object and the set of selected lots as an empty {@code HashSet}.
     */
    public LicitadorLotesData() {
        this.licitador = new LicitadorData();
        this.lotesSeleccionadosIds = new HashSet<>();
    }

    /**
     * Constructor to initialize the class with pre-existing data.
     *
     * @param licitador The {@code LicitadorData} instance to use.
     * @param lotesSeleccionadosIds The set of selected lot IDs.
     */
    public LicitadorLotesData(LicitadorData licitador, Set<String> lotesSeleccionadosIds) {
        this.licitador = licitador;
        this.lotesSeleccionadosIds = lotesSeleccionadosIds;
    }

    /**
     * Gets the bidder's data.
     *
     * @return The {@code LicitadorData} object.
     */
    public LicitadorData getLicitador() {
        return licitador;
    }

    /**
     * Sets the bidder's data.
     *
     * @param licitador The new {@code LicitadorData} object.
     */
    public void setLicitador(LicitadorData licitador) {
        this.licitador = licitador;
    }

    /**
     * Gets the set of IDs of the lots in which the bidder has decided to participate.
     *
     * @return The {@code Set} of selected lot IDs.
     */
    public Set<String> getLotesSeleccionadosIds() {
        return lotesSeleccionadosIds;
    }

    /**
     * Sets the set of selected lot IDs.
     *
     * @param lotesSeleccionadosIds The new {@code Set} of lot IDs.
     */
    public void setLotesSeleccionadosIds(Set<String> lotesSeleccionadosIds) {
        this.lotesSeleccionadosIds = lotesSeleccionadosIds;
    }

    /**
     * Checks if the bidder has selected the lot with the given ID, indicating participation.
     *
     * @param loteId The ID of the lot to check (e.g., "Lot 1", "Lot 2", etc.).
     * @return {@code true} if the {@code loteId} is present in the set of selected lots, {@code false} otherwise.
     */
    public boolean participaEnLote(String loteId) {
        return lotesSeleccionadosIds.contains(loteId);
    }
}