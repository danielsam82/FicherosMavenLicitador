package com.licitador.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.IOException;
import java.net.URL;
import java.awt.Color;
import com.licitador.model.LicitadorData; // Importar LicitadorData

/**
 * Clase ayudante para insertar la cabecera (Logo y Texto) Y el Pie de Página
 * (Numeración). (Versión CORREGIDA: Usa posicionamiento Y absoluto para la
 * cabecera)
 */
public class HeaderFooterEvent extends PdfPageEventHelper {

    private Image logo;
    private final Configuracion configuracion;
    private final LicitadorData licitadorData; // Necesario para el bloque del apoderado

    // --- Definición de Fuentes ---
    private static final Font FONT_TITULO_PRINCIPAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, new Color(0, 128, 0));
    private static final Font FONT_TITULO_SECUNDARIO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, new Color(0, 128, 0));
    private static final Font FONT_DECLARA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_LICITADOR_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_LICITADOR_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    private final Font FONT_HEADER_TEXT = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.black);
    private final Font FONT_HEADER_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.black);
    private final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    /**
     * Constructor MODIFICADO: Ahora también recibe LicitadorData
     */
    public HeaderFooterEvent(Configuracion configuracion, LicitadorData licitadorData) {
        this.configuracion = configuracion;
        this.licitadorData = licitadorData; // Guardar
        try {
            URL logoUrl = getClass().getClassLoader().getResource("logo_fraternidad.png");
            if (logoUrl != null) {
                this.logo = Image.getInstance(logoUrl);
                this.logo.scaleToFit(100, 50);
            } else {
                System.err.println("¡ADVERTENCIA! No se encontró el recurso 'logo_fraternidad.png' en src/main/resources/");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.logo = null;
        }
    }

    /**
     * Dibuja la cabecera completa (Pliego, Logo, Títulos, Licitador) en la
     * parte superior de la PRIMERA PÁGINA ÚNICAMENTE.
     */
    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        if (configuracion == null || licitadorData == null) {
            return;
        }

        // --- DIBUJAR SOLO EN LA PÁGINA 1 ---
        if (writer.getPageNumber() > 1) {
            return;
        }

        try {
            // --- 1. CABECERA SUPERIOR (Pliego y Logo) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setTotalWidth(document.right() - document.left());
            headerTable.setWidths(new float[]{3.0f, 1.0f});

            // Celda de Texto (Izquierda)
            Phrase phrase = new Phrase();
            phrase.add(new Chunk("Pliego de Condiciones Particulares para la ", FONT_HEADER_TEXT));
            String objeto = (configuracion.getObjetoLicitacion() != null && !configuracion.getObjetoLicitacion().isEmpty())
                    ? configuracion.getObjetoLicitacion() : "OBJETO DE LA LICITACIÓN";
            String exp = (configuracion.getNumeroExpediente() != null && !configuracion.getNumeroExpediente().isEmpty())
                    ? configuracion.getNumeroExpediente() : "EXPEDIENTE";
            phrase.add(new Chunk("\nREF: PIC " + exp, FONT_HEADER_BOLD));

            PdfPCell textCell = new PdfPCell(phrase);
            textCell.setBorder(Rectangle.BOTTOM);
            textCell.setBorderColor(Color.BLACK); // <-- La línea negra
            textCell.setPadding(5);
            headerTable.addCell(textCell);

            // Celda de Logo (Derecha)
            PdfPCell logoCell = new PdfPCell();
            if (logo != null) {
                logoCell.addElement(logo);
            }
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setBorder(Rectangle.BOTTOM);
            logoCell.setBorderColor(Color.BLACK); // <-- La línea negra
            logoCell.setPadding(1);
            headerTable.addCell(logoCell);

            // --- 🔥 CORRECCIÓN DE POSICIÓN ABSOLUTA ---
            // Posición Y absoluta: 800 puntos desde la parte inferior de la página (A4 tiene 842 puntos de alto)
            // Esto coloca la cabecera siempre a 800 puntos de la parte inferior, fija.
            // Los márgenes del documento no afectan esta posición, solo dónde empieza el contenido.
            headerTable.writeSelectedRows(0, -1, document.leftMargin(), 790, writer.getDirectContent());
            // ------------------------------------------

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dibuja el pie de página (Numeración) en la parte inferior de CADA PÁGINA.
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            footerTable.setTotalWidth(document.right() - document.left());

            String text = String.format("Página %d | Fraternidad-Muprespa, Mutua Colaboradora con la Seguridad Social nº 275", writer.getPageNumber());
            PdfPCell cell = new PdfPCell(new Phrase(text, FONT_FOOTER));
            cell.setBorder(Rectangle.TOP);
            cell.setBorderColor(Color.BLACK);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);

            footerTable.addCell(cell);

            // Escribir la tabla en la posición absoluta del pie
            footerTable.writeSelectedRows(0, -1, document.leftMargin(), document.bottom() - 10, writer.getDirectContent());

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
