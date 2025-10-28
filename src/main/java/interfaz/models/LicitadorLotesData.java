package interfaz.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase que encapsula la información completa del licitador (datos personales y de la empresa)
 * junto con la selección de los lotes específicos a los que desea presentar una oferta.
 *
 * Esta clase es la entidad central para guardar y restaurar el estado de participación
 * del licitador en la licitación.
 *
 * Implementa {@link Serializable} para permitir la persistencia (guardado/carga de sesión).
 */
public class LicitadorLotesData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * El objeto que contiene la información personal y de la empresa del licitador.
     */
    private LicitadorData licitador;

    /**
     * Un conjunto de IDs de lotes (generalmente Strings) que el licitador ha seleccionado
     * para participar. Si el conjunto está vacío, se asume que no participa en ningún lote.
     */
    private Set<String> lotesSeleccionadosIds;

    /**
     * Constructor por defecto. Inicializa los datos del licitador con un nuevo objeto
     * {@code LicitadorData} y el conjunto de lotes seleccionados como un {@code HashSet} vacío.
     */
    public LicitadorLotesData() {
        this.licitador = new LicitadorData();
        this.lotesSeleccionadosIds = new HashSet<>();
    }

    /**
     * Constructor para inicializar la clase con datos preexistentes.
     *
     * @param licitador La instancia de {@code LicitadorData} a utilizar.
     * @param lotesSeleccionadosIds El conjunto de IDs de los lotes seleccionados.
     */
    public LicitadorLotesData(LicitadorData licitador, Set<String> lotesSeleccionadosIds) {
        this.licitador = licitador;
        this.lotesSeleccionadosIds = lotesSeleccionadosIds;
    }

    // --- Getters y Setters ---

    /**
     * Obtiene los datos del licitador.
     * @return El objeto {@code LicitadorData}.
     */
    public LicitadorData getLicitador() {
        return licitador;
    }

    /**
     * Establece los datos del licitador.
     * @param licitador El nuevo objeto {@code LicitadorData}.
     */
    public void setLicitador(LicitadorData licitador) {
        this.licitador = licitador;
    }

    /**
     * Obtiene el conjunto de IDs de los lotes a los que el licitador ha decidido concurrir.
     * @return El {@code Set} de IDs de lotes seleccionados.
     */
    public Set<String> getLotesSeleccionadosIds() {
        return lotesSeleccionadosIds;
    }

    /**
     * Establece el conjunto de IDs de los lotes seleccionados.
     * @param lotesSeleccionadosIds El nuevo {@code Set} de IDs de lotes.
     */
    public void setLotesSeleccionadosIds(Set<String> lotesSeleccionadosIds) {
        this.lotesSeleccionadosIds = lotesSeleccionadosIds;
    }

    /**
     * Comprueba si el licitador ha seleccionado el lote con el ID dado, indicando participación.
     *
     * @param loteId El ID del lote a verificar (Ej: "Lote 1", "Lote 2", etc.).
     * @return {@code true} si el {@code loteId} está presente en el conjunto de lotes seleccionados, {@code false} en caso contrario.
     */
    public boolean participaEnLote(String loteId) {
        return lotesSeleccionadosIds.contains(loteId);
    }
}