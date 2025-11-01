package com.licitador.jar;

import com.licitador.model.LicitacionData;
import com.licitador.model.ArticuloAnexo;
import com.licitador.jar.model.RequerimientoLicitador;
import com.licitador.service.Configuracion;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase responsable de dos fases en el JAR del Licitador:
 * 1. Obtener los requisitos de datos del Licitador (para la GUI).
 * 2. Ensamblar el Anexo final, sustituyendo tags y adaptando el contenido
 * en base a las respuestas recogidas en los RequerimientoLicitador.
 */
public class AnexoGenerator {

    private final Configuracion configuracion; 
    private final Map<String, String> datosFijosLicitador; 
    private final Map<String, RequerimientoLicitador> respuestasMap;

    public AnexoGenerator(Configuracion configuracion, Map<String, String> datosFijosLicitador) {
        this.configuracion = configuracion;
        this.datosFijosLicitador = datosFijosLicitador;
        this.respuestasMap = new HashMap<>();
    }

    // --- FASE 1: OBTENER REQUERIMIENTOS ---

    /**
     * Examina los artículos y extrae todos los artículos interactivos que la
     * GUI del licitador debe gestionar.
     */
    public List<RequerimientoLicitador> obtenerRequerimientosInteractivos() {
        List<RequerimientoLicitador> requerimientos = new ArrayList<>();
        ArticuloAnexo[] articulos = configuracion.getArticulosAnexos(); 
        if (articulos == null) return requerimientos;

        Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));

        for (ArticuloAnexo articulo : articulos) {
            if (articulo.esInteractivo()) { // Solo los interactivos
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

    /**
     * Obtiene solo el texto de los artículos declarativos (no interactivos)
     * ordenados, listos para ser mostrados en la pestaña de "Lectura" del diálogo.
     */
    public String obtenerTextoDeclarativo() {
        StringBuilder sb = new StringBuilder();
        ArticuloAnexo[] articulos = configuracion.getArticulosAnexos(); 
        if (articulos == null) {
            return "<html><body><p>No hay artículos declarativos definidos.</p></body></html>";
        }
        Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));
        boolean foundDeclarative = false;

        for (ArticuloAnexo articulo : articulos) {
            if (!articulo.esInteractivo()) { // Solo los NO interactivos
                foundDeclarative = true;
                String contenidoSustituido = sustituirTags(articulo.getContenidoFormato());
                sb.append(contenidoSustituido.replaceAll("\n", "<br>")); 
                sb.append("<br>"); 
                if (articulo.isRequiereFirma()) {
                    sb.append("<p style=\"text-align: right; margin-top: 30px;\">_________________________ (Firma)</p>\n");
                }
                sb.append("<hr>\n");
            }
        }
        if (!foundDeclarative) {
            return "<html><body><p>No hay artículos declarativos requeridos.</p></body></html>";
        }
        return "<html><body>" + sb.toString() + "</body></html>";
    }

    // --- FASE 2: GENERAR CONTENIDO FINAL ---

    public void setRespuestasFinales(List<RequerimientoLicitador> requerimientosFinales) {
        requerimientosFinales.forEach(req -> respuestasMap.put(req.getIdArticulo(), req));
    }

    /**
     * Ensambla todos los artículos (declarativos e interactivos)
     * (Adaptado para usar contenidoRespuestaNo)
     */
    public String generarContenidoFinal() {
        ArticuloAnexo[] articulos = configuracion.getArticulosAnexos(); 
        if (articulos == null || articulos.length == 0) {
            return "No se encontraron artículos definidos para la licitación.";
        }
        Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));

        StringBuilder anexoGlobal = new StringBuilder();
        anexoGlobal.append("<html><body>"); 
        anexoGlobal.append("<h1>ANEXO ADMINISTRATIVO GLOBAL DE ADHESIÓN</h1>\n");
        anexoGlobal.append("<p>Expediente: <b>").append(configuracion.getNumeroExpediente()).append("</b> (Objeto: ")
                   .append(configuracion.getObjetoLicitacion()).append(").</p>\n<hr>\n");

        for (ArticuloAnexo articulo : articulos) {
            String contenidoAAnadir = "";

            if (articulo.esInteractivo()) {
                RequerimientoLicitador req = respuestasMap.get(articulo.getIdArticulo());
                
                if (req == null) continue; // No se respondió, se omite

                if (req.isRespuestaSi()) {
                    // El licitador respondió SÍ: Usar el contenidoFormato (para "Sí")
                    contenidoAAnadir = adaptarContenidoRespuestaSi(articulo.getContenidoFormato(), req);
                } else {
                    // El licitador respondió NO: Usar el contenidoFormatoRespuestaNo
                    contenidoAAnadir = articulo.getContenidoFormatoRespuestaNo();
                }
            } else {
                // Artículo declarativo
                contenidoAAnadir = articulo.getContenidoFormato();
            }

            // Sustituir tags (NIF, EXPEDIENTE, etc.) y añadir al Anexo Global
            String contenidoFinal = sustituirTags(contenidoAAnadir);
            
            anexoGlobal.append(contenidoFinal.replaceAll("\n", "<br>"));
            anexoGlobal.append("<br>"); 

            if (articulo.isRequiereFirma()) {
                anexoGlobal.append("<p style=\"text-align: right; margin-top: 50px;\">_________________________ (Firma)</p>\n");
            }
        }

        anexoGlobal.append("</body></html>");
        return anexoGlobal.toString();
    }

    /**
     * Adapta el texto del artículo (versión SÍ) para incluir los datos que el licitador
     * rellenó en la GUI o la referencia al archivo subido.
     */
    private String adaptarContenidoRespuestaSi(String contenidoSi, RequerimientoLicitador req) {
        StringBuilder sb = new StringBuilder(contenidoSi);

        // Añade la información extra recopilada
        switch (req.getAccionSi()) {
            case ArticuloAnexo.ACCION_PEDIR_FICHERO:
                sb.append("<br><b>[Respuesta: SÍ. Se adjunta el fichero: ")
                  .append(new File(req.getRutaFichero()).getName()) // Solo el nombre del fichero
                  .append("]</b>");
                break;
            case ArticuloAnexo.ACCION_PEDIR_CAMPOS:
                sb.append("<br><b>[Respuesta: SÍ. Se cumplimentan los siguientes datos adicionales:]</b><br>");
                for (Map.Entry<String, String> entry : req.getValoresCampos().entrySet()) {
                    sb.append("— ").append(entry.getKey()).append(": <b>").append(entry.getValue()).append("</b><br>");
                }
                break;
            case ArticuloAnexo.ACCION_NINGUNA:
            default:
                 sb.append("<br><b>[Respuesta: SÍ. Se acepta la condición.]</b>");
                break;
        }
        return sb.toString();
    }

    /**
     * Reemplaza todas las etiquetas <DATO_LICITADOR ETQ="KEY"/> por sus
     * valores.
     */
    private String sustituirTags(String contenido) {
        if (contenido == null) return ""; // Seguridad por si contenidoFormatoRespuestaNo es nulo
        
        Pattern pattern = Pattern.compile("<DATO_LICITADOR\\s+ETQ=\"([A-Z_]+)\"\\s*/>");
        Matcher matcher = pattern.matcher(contenido);
        StringBuffer sb = new StringBuffer(); 

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = "[DATO_FALTANTE: " + key + "]"; 

            if (datosFijosLicitador.containsKey(key)) {
                replacement = datosFijosLicitador.get(key);
            } 
            else {
                switch (key) {
                    case "EXPEDIENTE":
                        replacement = configuracion.getNumeroExpediente(); 
                        break;
                    case "OBJETO":
                        replacement = configuracion.getObjetoLicitacion(); 
                        break;
                    default:
                        break;
                }
            }
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}