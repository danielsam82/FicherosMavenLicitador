package com.licitador.service;

import com.licitador.model.LicitadorData;
import com.licitador.service.Configuracion;

import com.lowagie.text.*; // Importaciones principales de OpenPDF (lowagie.text)
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.awt.Color; // Para colores de tabla/celdas

/**
 * A PDF generator using the OpenPDF library (lowagie.text).
 * It creates the Administrative Annex with the bidder's data.
 */
public class PDFGenerator {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Color COLOR_HEADER_BG = new Color(230, 230, 230);

    /**
     * Generates the Administrative Annex in PDF format with OpenPDF.
     *
     * @param licitadorData The bidder's data.
     * @param participacionPorLote A map of the selected lots.
     * @param configuracion The configuration object to get the file number.
     * @return A byte array with the binary PDF content.
     * @throws DocumentException If there is an error in the PDF structure.
     * @throws IOException If there is an I/O error when writing the PDF.
     */
    public static byte[] generarAnexoAdministrativo(
            LicitadorData licitadorData,
            Map<Integer, Boolean> participacionPorLote,
            Configuracion configuracion) throws DocumentException, IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Paragraph title = new Paragraph("ANEXO DE CUMPLIMENTACIÓN ADMINISTRATIVA", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(createCell("Expediente N.º:", configuracion.getNumeroExpediente(), FONT_BOLD, FONT_NORMAL));
            infoTable.addCell(createCell("Objeto de la Licitación:", configuracion.getObjetoLicitacion(), FONT_BOLD, FONT_NORMAL));
            infoTable.setSpacingAfter(15);
            document.add(infoTable);

            document.add(new Paragraph("DATOS DE LA EMPRESA LICITADORA", FONT_HEADER));
            document.add(createLicitadorTable(licitadorData));

            document.add(new Paragraph("DECLARACIONES Y PARTICIPACIÓN EN LOTES", FONT_HEADER));
            document.add(createDeclaracionTable(licitadorData));

            if (configuracion.isTieneLotes()) {
                document.add(new Paragraph("LOTES SELECCIONADOS PARA PARTICIPACIÓN", FONT_HEADER));
                document.add(createLoteTable(participacionPorLote));
            } else {
                document.add(new Paragraph("Licitación de Oferta Única: Participación Confirmada.", FONT_NORMAL));
            }

            document.close();
            return baos.toByteArray();
        }
    }

    // --- MÉTODOS AUXILIARES PARA LA ESTRUCTURA DEL PDF ---
    private static PdfPCell createCell(String label, String value, Font labelFont, Font valueFont) {
        // Celda para la etiqueta (Label)
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);

        // Celda para el valor (Value)
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderWidthBottom(0.5f);
        valueCell.setPadding(3);

        // Tabla con 2 columnas: Label y Value
        PdfPTable row = new PdfPTable(new float[]{1.5f, 3.5f});
        row.setWidthPercentage(100);
        row.addCell(labelCell);
        row.addCell(valueCell);

        // Celda que contiene la fila completa
        PdfPCell containerCell = new PdfPCell(row);
        containerCell.setPadding(0);
        containerCell.setBorder(Rectangle.NO_BORDER);
        return containerCell;
    }

    private static Table createLicitadorTable(LicitadorData data) throws DocumentException {
        // Uso de Table antigua para formato simple de 2 columnas
        Table table = new Table(2);
        table.setWidth(100);
        table.setPadding(5);
        table.setSpacing(0);

        table.addCell(createLabeledCell("Razón Social:", data.getRazonSocial()));
        table.addCell(createLabeledCell("NIF/CIF:", data.getNif()));
        table.addCell(createLabeledCell("Domicilio:", data.getDomicilio()));
        table.addCell(createLabeledCell("Email:", data.getEmail()));
        table.addCell(createLabeledCell("Teléfono:", data.getTelefono()));
        table.addCell(new Cell("")); // Celda vacía para rellenar

        return table;
    }

    private static Cell createLabeledCell(String label, String value) {
        // Uso de Cell antigua para retrocompatibilidad/simplicidad en este ejemplo
        Cell cell = new Cell(new Phrase(label + " " + value, FONT_NORMAL));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        // LÍNEA ELIMINADA: cell.setPadding(3); // ¡Esta era la línea problemática!
        return cell;
    }

    private static PdfPTable createDeclaracionTable(LicitadorData data) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        // 1. Cabecera
        PdfPCell header1 = new PdfPCell(new Phrase("Declaración", FONT_BOLD));
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header1.setBackgroundColor(COLOR_HEADER_BG);
        table.addCell(header1);

        PdfPCell header2 = new PdfPCell(new Phrase("Respuesta", FONT_BOLD));
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setBackgroundColor(COLOR_HEADER_BG);
        table.addCell(header2);

        // 2. Fila PYME
        table.addCell(new Phrase("La empresa tiene la condición de PYME.", FONT_NORMAL));
        table.addCell(new Phrase(data.esPyme() ? "SÍ" : "NO", FONT_NORMAL));

        // 3. Fila Extranjera
        table.addCell(new Phrase("La empresa es extranjera (no residente en España).", FONT_NORMAL));
        table.addCell(new Phrase(data.esExtranjera() ? "SÍ" : "NO", FONT_NORMAL));

        return table;
    }

    private static PdfPTable createLoteTable(Map<Integer, Boolean> participacionPorLote) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50); // Tabla más pequeña
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        // 1. Cabecera
        PdfPCell header1 = new PdfPCell(new Phrase("Lote", FONT_BOLD));
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header1.setBackgroundColor(COLOR_HEADER_BG);
        table.addCell(header1);

        PdfPCell header2 = new PdfPCell(new Phrase("Participación", FONT_BOLD));
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setBackgroundColor(COLOR_HEADER_BG);
        table.addCell(header2);

        // 2. Datos
        participacionPorLote.entrySet().stream()
                .filter(Map.Entry::getValue) // Solo los lotes marcados como true
                .sorted(Map.Entry.comparingByKey()) // Ordenar por ID de lote
                .forEach(entry -> {
                    table.addCell(new Phrase("Lote " + entry.getKey(), FONT_NORMAL));
                    table.addCell(new Phrase("SÍ", FONT_NORMAL));
                });

        return table;
    }
}
