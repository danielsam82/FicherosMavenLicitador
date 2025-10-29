// Archivo: src/main/java/com/licitador/jar/AnexoGenerator.java
package com.licitador.jar;

import com.licitador.model.LicitacionData;
import com.licitador.model.ArticuloAnexo;
import com.licitador.jar.model.RequerimientoLicitador;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase responsable de dos fases en el JAR del Licitador: 1. Obtener los
 * requisitos de datos del Licitador (para la GUI). 2. Ensamblar el Anexo final,
 * sustituyendo tags y adaptando el contenido en base a las respuestas recogidas
 * en los RequerimientoLicitador.
 */
public class AnexoGenerator {

    private final LicitacionData licitacionData;
    private final Map<String, String> datosFijosLicitador; // Datos del Licitador (NIF, Razón Social, etc.)

    // Para la sustitución: Mapeamos el ID del Artículo a su Requerimiento Final
    private final Map<String, RequerimientoLicitador> respuestasMap;

    public AnexoGenerator(LicitacionData licitacionData, Map<String, String> datosFijosLicitador) {
        this.licitacionData = licitacionData;
        this.datosFijosLicitador = datosFijosLicitador;
        this.respuestasMap = new HashMap<>(); // Se llenará al recibir las respuestas
    }

    // ----------------------------------------------------------------------
    // FASE 1: OBTENER REQUERIMIENTOS (Para mostrar preguntas en la GUI)
    // ----------------------------------------------------------------------
    /**
     * Examina los artículos y extrae todos los artículos interactivos que la
     * GUI del licitador debe gestionar.
     *
     * @return Lista de objetos RequerimientoLicitador.
     */
    public List<RequerimientoLicitador> obtenerRequerimientosInteractivos() {
        List<RequerimientoLicitador> requerimientos = new ArrayList<>();

        ArticuloAnexo[] articulos = licitacionData.getArticulosAnexos();
        if (articulos == null) {
            return requerimientos;
        }

        // Ordenamos los artículos para que la GUI los muestre en el orden correcto
        Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));

        for (ArticuloAnexo articulo : articulos) {
            if (articulo.esInteractivo()) {
                RequerimientoLicitador req = new RequerimientoLicitador(
                        articulo.getIdArticulo(),
                        articulo.getTitulo(),
                        articulo.getPreguntaInteractiva(),
                        articulo.getAccionSi(),
                        articulo.getEtiquetasCampos()
                );
                requerimientos.add(req);
            }
        }
        return requerimientos;
    }

    // ----------------------------------------------------------------------
    // FASE 2: GENERAR CONTENIDO FINAL (Usando las respuestas recogidas)
    // ----------------------------------------------------------------------
    /**
     * Almacena las respuestas recogidas por la GUI para la fase de generación.
     *
     * @param requerimientosFinales La lista de Requerimientos COMPLETADA por el
     * Licitador.
     */
    public void setRespuestasFinales(List<RequerimientoLicitador> requerimientosFinales) {
        requerimientosFinales.forEach(req -> respuestasMap.put(req.getIdArticulo(), req));
    }

    /**
     * Ensambla todos los artículos, adaptando el texto según las respuestas
     * interactivas, y sustituyendo todos los tags por los datos reales.
     *
     * @return El contenido final del Anexo Global (String pre-PDF, ej: HTML).
     */
    public String generarContenidoFinal() {
        ArticuloAnexo[] articulos = licitacionData.getArticulosAnexos();

        if (articulos == null || articulos.length == 0) {
            return "No se encontraron artículos definidos para la licitación.";
        }

        // 1. Ordenar artículos
        Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));

        StringBuilder anexoGlobal = new StringBuilder();

        // --- INICIO DE CABECERA Y PREÁMBULO FIJO ---
        anexoGlobal.append("<h1>ANEXO ADMINISTRATIVO GLOBAL DE ADHESIÓN</h1>\n");
        anexoGlobal.append("<p>Expediente: <b>").append(licitacionData.getExpediente()).append("</b> (Objeto: ")
                .append(licitacionData.getObjeto()).append(").</p>\n<hr>\n");
        // --- FIN DE CABECERA ---

        // 2. Iterar, adaptar el contenido y sustituir tags
        for (ArticuloAnexo articulo : articulos) {

            String contenidoOriginal = articulo.getContenidoFormato();
            String contenidoAdaptado = "";

            // A. Verificar si el artículo es interactivo
            if (articulo.esInteractivo()) {
                RequerimientoLicitador req = respuestasMap.get(articulo.getIdArticulo());

                if (req != null) {
                    if (req.isRespuestaSi()) {
                        // El licitador respondió SÍ: Adaptar el contenido con sus datos
                        contenidoAdaptado = adaptarContenidoRespuestaSi(contenidoOriginal, req);
                    } else {
                        // El licitador respondió NO: Omitir el contenido del artículo o insertar una declaración de exclusión
                        // Por simplicidad, si la respuesta es NO, asumiremos que se omite el contenido.
                        continue; // Saltar a la siguiente iteración (el artículo no se incluye)
                    }
                } else {
                    // Si el requerimiento no fue contestado, por seguridad se omite.
                    // En la GUI del Licitador se debe forzar que todos se contesten.
                    continue;
                }
            } else {
                // El artículo es declarativo: Usar el contenido original
                contenidoAdaptado = contenidoOriginal;
            }

            // B. Sustituir tags y añadir al Anexo Global
            String contenidoFinal = sustituirTags(contenidoAdaptado);

            anexoGlobal.append("<h2>").append(articulo.getTitulo()).append("</h2>\n");
            anexoGlobal.append("<p>").append(contenidoFinal).append("</p>\n");

            // C. Añadir línea de firma si aplica
            if (articulo.isRequiereFirma()) {
                anexoGlobal.append("<p style=\"text-align: right; margin-top: 50px;\">_________________________ (Firma)</p>\n");
            }
        }

        return anexoGlobal.toString();
    }

    /**
     * Adapta el texto del artículo para incluir los datos que el licitador
     * rellenó en la GUI o la referencia al archivo subido.
     */
    private String adaptarContenidoRespuestaSi(String contenido, RequerimientoLicitador req) {
        StringBuilder sb = new StringBuilder(contenido);

        sb.append("<br><br><b>[Declaración Interactiva - Respuesta SÍ]</b><br>");

        switch (req.getAccionSi()) {
            case ArticuloAnexo.ACCION_PEDIR_FICHERO:
                sb.append("Se adjunta el fichero requerido: **").append(req.getRutaFichero()).append("**.");
                break;
            case ArticuloAnexo.ACCION_PEDIR_CAMPOS:
                sb.append("Se cumplimentan los siguientes datos adicionales:<br>");
                for (Map.Entry<String, String> entry : req.getValoresCampos().entrySet()) {
                    // Aquí se puede elegir cómo integrar el dato. Por simplicidad, lo agregamos al final del artículo.
                    // EJ: <DATO_LICITADOR ETQ="AVAL_BANCARIO"/> podría usarse para que el admin lo inserte.
                    sb.append("— ").append(entry.getKey()).append(": <b>").append(entry.getValue()).append("</b><br>");
                }
                break;
            case ArticuloAnexo.ACCION_NINGUNA:
            default:
                sb.append("No se requirió ninguna acción adicional.");
                break;
        }
        return sb.toString();
    }

// Dentro de AnexoGenerator.java (com.licitador.jar)
    /**
     * Reemplaza todas las etiquetas <DATO_LICITADOR ETQ="KEY"/> por sus
     * valores. Implementado usando Pattern y Matcher para compatibilidad con
     * Java 8.
     */
    private String sustituirTags(String contenido) {
        // Patrón para encontrar <DATO_LICITADOR ETQ="..."/>
        // Grupo 1: El valor dentro de las comillas (la KEY)
        Pattern pattern = Pattern.compile("<DATO_LICITADOR\\s+ETQ=\"([A-Z_]+)\"\\s*/>");
        Matcher matcher = pattern.matcher(contenido);

        StringBuffer sb = new StringBuffer(); // Usar StringBuffer para el reemplazo secuencial

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = "[DATO_FALTANTE: " + key + "]"; // Valor por defecto si no se encuentra

            // 1. Buscar en los datos del Licitador (NIF, Razón Social, etc.)
            if (datosFijosLicitador.containsKey(key)) {
                replacement = datosFijosLicitador.get(key);
            } // 2. Buscar en los datos fijos de la Licitación (Expediente, Objeto)
            else {
                switch (key) {
                    case "EXPEDIENTE":
                        replacement = licitacionData.getExpediente();
                        break;
                    case "OBJETO":
                        replacement = licitacionData.getObjeto();
                        break;
                    default:
                        // Ya tiene el valor por defecto "[DATO_FALTANTE: KEY]"
                        break;
                }
            }

            // Escapar caracteres especiales en el reemplazo antes de usar appendReplacement
            replacement = Matcher.quoteReplacement(replacement);

            matcher.appendReplacement(sb, replacement);
        }

        matcher.appendTail(sb); // Añadir el resto del contenido no capturado

        return sb.toString();
    }
}
