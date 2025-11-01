package com.licitador.service;

import com.licitador.model.LicitadorData;
import com.licitador.service.Configuracion;
import com.licitador.model.ArticuloAnexo;
import com.licitador.jar.model.RequerimientoLicitador;

// Importaciones clave de OpenPDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.awt.Color;
import java.util.HashMap;
import java.io.File; 

/**
 * Generador de PDF usando OpenPDF 1.3.29. Construcción MANUAL.
 * (CORREGIDO: HeaderFooterEvent SÓLO maneja cabecera/pie.
 * Esta clase maneja TODO el contenido, incluyendo títulos y bloque licitador).
 */
public class PDFGenerator {

    // --- Definición de Fuentes ---
    private static final Font FONT_TITULO_PRINCIPAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, new Color(0, 128, 0));
    private static final Font FONT_TITULO_SECUNDARIO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, new Color(0, 128, 0));
    private static final Font FONT_DECLARA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_LICITADOR_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_LICITADOR_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_ARTICULO_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
    private static final Font FONT_ARTICULO_CONTENIDO = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
    private static final Font FONT_ARTICULO_RESPUESTA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(0, 102, 204));
    private static final Font FONT_ARTICULO_DETALLE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    /**
     * Genera el Anexo Administrativo en formato PDF (Construcción Manual).
     */
    public static byte[] generarAnexoManual(
            LicitadorData licitadorData,
            Configuracion configuracion,
            List<RequerimientoLicitador> respuestas
    ) throws DocumentException, IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 1. Crear Documento (Márgenes AJUSTADOS)
            // Dejamos 90pt arriba para el HeaderFooterEvent (Pliego/Logo/Línea)
            // y 50pt abajo para el pie de página.
            Document document = new Document(PageSize.A4, 50, 50, 120, 50); 

            // 2. Crear PdfWriter y asignar Evento de Página
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            // Le pasamos licitadorData al constructor, ya que ahora lo requiere
            HeaderFooterEvent event = new HeaderFooterEvent(configuracion, licitadorData);
            writer.setPageEvent(event); // El evento dibujará Pliego/Logo/Línea

            document.open();

            // 3. TÍTULO 1 (ANEXO REQUISITOS...)
            Paragraph titulo1 = new Paragraph("ANEXO REQUISITOS PREVIOS DE PARTICIPACIÓN", FONT_TITULO_PRINCIPAL);
            titulo1.setAlignment(Element.ALIGN_CENTER);
            // --- CORRECCIÓN DE ESPACIADO ---
            // El margen superior (90) ya nos da espacio.
            // Añadimos solo un poco de espacio (10pt) DESPUÉS de la línea negra.
            titulo1.setSpacingBefore(5);
            titulo1.setSpacingAfter(5);
            document.add(titulo1);

            // 4. TÍTULO 2 (DOCUMENTACIÓN ADMINISTRATIVA)
            Paragraph titulo2 = new Paragraph("DOCUMENTACIÓN ADMINISTRATIVA", FONT_TITULO_SECUNDARIO);
            titulo2.setAlignment(Element.ALIGN_CENTER);
            titulo2.setSpacingBefore(5);
            titulo2.setSpacingAfter(25); // Espacio antes del bloque D./Dª
            document.add(titulo2);

            // 5. BLOQUE DE DATOS DEL LICITADOR
            Phrase bloqueLicitador = new Phrase("", FONT_LICITADOR_NORMAL);
            bloqueLicitador.add(new Chunk("D./Dª "));
            bloqueLicitador.add(new Chunk(licitadorData.getNombreApoderado(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(", con D.N.I. número "));
            bloqueLicitador.add(new Chunk(licitadorData.getNifApoderado(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(", actuando en su propio nombre y derecho/en representación de la empresa "));
            bloqueLicitador.add(new Chunk(licitadorData.getRazonSocial(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(" con NIF de la EMPRESA y con domicilio profesional en "));
            bloqueLicitador.add(new Chunk(licitadorData.getDomicilio(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(" en su calidad de "));
            bloqueLicitador.add(new Chunk(licitadorData.getCalidadApoderado(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(" con número de teléfono "));
            bloqueLicitador.add(new Chunk(licitadorData.getTelefono(), FONT_LICITADOR_BOLD));
            bloqueLicitador.add(new Chunk(" y correo electrónico "));
            bloqueLicitador.add(new Chunk(licitadorData.getEmail(), FONT_LICITADOR_BOLD));

            Paragraph pLicitador = new Paragraph(bloqueLicitador);
            pLicitador.setAlignment(Element.ALIGN_JUSTIFIED);
            pLicitador.setSpacingAfter(20); 
            document.add(pLicitador);

            // 6. DECLARA
            Paragraph declara = new Paragraph("DECLARA", FONT_DECLARA);
            declara.setAlignment(Element.ALIGN_CENTER);
            declara.setSpacingAfter(15); 
            document.add(declara);

            // 7. ARTÍCULOS EN DOS COLUMNAS
            Map<String, RequerimientoLicitador> respuestasMap = new HashMap<>();
            for (RequerimientoLicitador req : respuestas) {
                respuestasMap.put(req.getIdArticulo(), req);
            }

            ArticuloAnexo[] articulos = configuracion.getArticulosAnexos();
            if (articulos == null) {
                articulos = new ArticuloAnexo[0];
            }
            Arrays.sort(articulos, (a1, a2) -> Integer.compare(a1.getOrden(), a2.getOrden()));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 1});
            table.setSpacingBefore(10); 
            table.setSplitLate(false);
            table.setSplitRows(true);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setPadding(5);

            for (int i = 0; i < articulos.length; i++) {
                ArticuloAnexo articulo = articulos[i];

                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER); 
                cell.setPadding(5);
                cell.setPaddingLeft(10); 
                cell.setPaddingRight(10); 

                // --- Línea Separadora a la Derecha ---
                if (i % 2 == 0 && i < articulos.length - 1) { 
                    cell.setBorderWidthRight(0.5f);
                    cell.setBorderColorRight(Color.LIGHT_GRAY);
                }

                Phrase tituloArticuloPhrase = new Phrase("", FONT_ARTICULO_TITULO);
                tituloArticuloPhrase.add(new Chunk(String.format("Apartado nº. %d. ", articulo.getOrden())));
                tituloArticuloPhrase.add(new Chunk(articulo.getTitulo(), FONT_ARTICULO_TITULO));
                tituloArticuloPhrase.add(new Chunk(" — ", FONT_ARTICULO_TITULO));

                Paragraph pTituloArticulo = new Paragraph(tituloArticuloPhrase);
                pTituloArticulo.setSpacingAfter(2);
                cell.addElement(pTituloArticulo);

                Paragraph contenidoParrafo = new Paragraph("", FONT_ARTICULO_CONTENIDO);
                contenidoParrafo.setIndentationLeft(10);
                contenidoParrafo.setAlignment(Element.ALIGN_JUSTIFIED); 

                if (!articulo.esInteractivo()) {
                    String contenidoSustituido = sustituirTagsManualmente(articulo.getContenidoFormato(), licitadorData, configuracion, respuestasMap);
                    for (String linea : contenidoSustituido.split("\n")) {
                        contenidoParrafo.add(new Chunk(linea, FONT_ARTICULO_CONTENIDO));
                        contenidoParrafo.add(Chunk.NEWLINE);
                    }
                } else {
                    RequerimientoLicitador req = respuestasMap.get(articulo.getIdArticulo());
                    if (req != null) {
                        String contenidoRespuesta = adaptarContenidoRespuesta(articulo, req, licitadorData, configuracion, respuestasMap);
                        
                        for (String linea : contenidoRespuesta.split("\n")) {
                            if (linea.startsWith("[Respuesta:")) {
                                contenidoParrafo.add(new Chunk(linea, FONT_ARTICULO_RESPUESTA));
                            } else if (linea.startsWith("— ")) {
                                contenidoParrafo.add(new Chunk(linea, FONT_ARTICULO_DETALLE));
                            }
                            else {
                                contenidoParrafo.add(new Chunk(linea, FONT_ARTICULO_CONTENIDO));
                            }
                            contenidoParrafo.add(Chunk.NEWLINE);
                        }
                    } else {
                        contenidoParrafo.add(new Chunk("(No respondido)", FONT_ARTICULO_DETALLE));
                    }
                }
                cell.addElement(contenidoParrafo);

                if (articulo.isRequiereFirma()) {
                    Paragraph firmaParrafo = new Paragraph("_________________________ (Firma)", FONT_ARTICULO_CONTENIDO);
                    firmaParrafo.setAlignment(Element.ALIGN_RIGHT);
                    firmaParrafo.setSpacingBefore(10);
                    cell.addElement(firmaParrafo);
                }

                cell.setBorderWidthBottom(0.5f); 
                cell.setBorderColorBottom(Color.LIGHT_GRAY);
                cell.setPaddingBottom(10); 

                table.addCell(cell);
            }

            if (articulos.length % 2 != 0) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.NO_BORDER);
                emptyCell.setBorderWidthBottom(0.5f);
                emptyCell.setBorderColorBottom(Color.LIGHT_GRAY);
                emptyCell.setPadding(5);
                emptyCell.setPaddingBottom(10);
                table.addCell(emptyCell);
            }

            document.add(table);

            // 8. CIERRE
            document.close();
            return baos.toByteArray();
        }
    }
    
    /**
     * Adapta el contenido de la respuesta de un artículo interactivo.
     * (Corregido para usar los métodos y constantes correctos)
     */
    private static String adaptarContenidoRespuesta(
            ArticuloAnexo articulo,
            RequerimientoLicitador req,
            LicitadorData licitadorData,
            Configuracion configuracion,
            Map<String, RequerimientoLicitador> respuestasMap) {

        StringBuilder contenidoFinal = new StringBuilder();

        String textoBase = articulo.getContenidoFormato();
        if (textoBase != null && !textoBase.isEmpty()) {
            contenidoFinal.append(sustituirTagsManualmente(textoBase, licitadorData, configuracion, respuestasMap));
            contenidoFinal.append("\n");
        }

        if (req.isRespuestaSi()) {
            contenidoFinal.append("[Respuesta: SÍ");

            if (ArticuloAnexo.ACCION_PEDIR_FICHERO.equals(articulo.getAccionSi())) {
                contenidoFinal.append(". Se adjunta el fichero: ");
                if (req.getRutaFichero() != null && !req.getRutaFichero().isEmpty()) {
                    contenidoFinal.append(new File(req.getRutaFichero()).getName());
                } else {
                    contenidoFinal.append("[Fichero no especificado]");
                }

            } else if (ArticuloAnexo.ACCION_PEDIR_CAMPOS.equals(articulo.getAccionSi())) {
                
                Map<String, String> datosAdicionales = req.getValoresCampos(); 
                
                if (datosAdicionales != null && !datosAdicionales.isEmpty()) {
                    contenidoFinal.append(". Se cumplimentan los siguientes datos adicionales:]\n");
                    for (Map.Entry<String, String> entry : datosAdicionales.entrySet()) {
                        contenidoFinal.append("— ").append(entry.getKey()).append(":: ").append(entry.getValue()).append("\n");
                    }
                    if (contenidoFinal.length() > 0 && contenidoFinal.charAt(contenidoFinal.length() - 1) == '\n') {
                        contenidoFinal.setLength(contenidoFinal.length() - 1);
                    }
                } else {
                    contenidoFinal.append(". (No se proporcionaron datos adicionales)]");
                }

            } else {
                 contenidoFinal.append(". Se acepta la condición.]");
            }
            
            contenidoFinal.append("\n");

        } else {
            if (articulo.getContenidoFormatoRespuestaNo() != null && !articulo.getContenidoFormatoRespuestaNo().isEmpty()) {
                String textoRespuestaNo = sustituirTagsManualmente(articulo.getContenidoFormatoRespuestaNo(), licitadorData, configuracion, respuestasMap);
                contenidoFinal.append("[Respuesta: NO. ").append(textoRespuestaNo).append("]\n");
            } else {
                contenidoFinal.append("[Respuesta: NO]\n");
            }
        }
        
        return contenidoFinal.toString();
    }


    /**
     * Sustituye manualmente los tags por los valores de los modelos.
     */
    private static String sustituirTagsManualmente(String contenido, LicitadorData licitadorData, Configuracion configuracion, Map<String, RequerimientoLicitador> respuestasMap) {
        if (contenido == null) {
            return "";
        }

        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"NIF_CIF\"/>", licitadorData.getNif()); 
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"RAZON_SOCIAL\"/>", licitadorData.getRazonSocial());
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"NOMBRE_APODERADO\"/>", licitadorData.getNombreApoderado());
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"NIF_APODERADO\"/>", licitadorData.getNifApoderado()); 
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"DOMICILIO\"/>", licitadorData.getDomicilio());
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"CARGO_APODERADO\"/>", licitadorData.getCalidadApoderado());
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"TELEFONO\"/>", licitadorData.getTelefono());
        contenido = contenido.replace("<DATO_LICITADOR ETQ=\"EMAIL\"/>", licitadorData.getEmail());

        contenido = contenido.replace("<DATO_CONFIGURACION ETQ=\"NUM_EXPEDIENTE\"/>", configuracion.getNumeroExpediente());
        contenido = contenido.replace("<DATO_CONFIGURACION ETQ=\"OBJETO_LICITACION\"/>", configuracion.getObjetoLicitacion());

        return contenido;
    }
}