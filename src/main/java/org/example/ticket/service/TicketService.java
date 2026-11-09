package org.example.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.envio.model.Envio;
import org.example.envio.repository.EnvioRepository;
import org.springframework.stereotype.Service;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TicketService {

    private final EnvioRepository envioRepository;

    public TicketService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    // ==========================================
    // MÉTODO 1: GENERAR TICKET (Comprobante de Pago)
    // ==========================================
    public InputStream generarPdf(Long envioId) throws Exception {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6, 20, 20, 20, 20);
        PdfWriter.getInstance(document, out);
        document.open();

        // Estilos
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // Encabezado
        Paragraph titulo = new Paragraph("ATYRA EXPRESS S.A.C.", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph ruc = new Paragraph("RUC: 20601234567", fontNormal);
        ruc.setAlignment(Element.ALIGN_CENTER);
        document.add(ruc);

        Paragraph dir = new Paragraph("Av. Principal 123 - Lima", fontNormal);
        dir.setAlignment(Element.ALIGN_CENTER);
        document.add(dir);

        document.add(new Paragraph("--------------------------------------------------", fontNormal));

        // Datos de la Guía
        document.add(new Paragraph("TICKET DE ENVÍO", fontSubtitulo));
        document.add(new Paragraph("N° Guía: " + envio.getNumeroGuia(), fontNormal));
        document.add(new Paragraph("Fecha: " + envio.getRegistradoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
        document.add(new Paragraph(" ", fontNormal));

        // QR
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(envio.getNumeroGuia(), BarcodeFormat.QR_CODE, 100, 100);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        Image qrImage = Image.getInstance(pngOutputStream.toByteArray());
        qrImage.setAlignment(Element.ALIGN_CENTER);
        qrImage.scaleToFit(80, 80);
        document.add(qrImage);
        document.add(new Paragraph(" ", fontNormal));

        // Remitente
        document.add(new Paragraph("REMITENTE", fontSubtitulo));
        document.add(new Paragraph(envio.getRemitente().getNombres() + " " + envio.getRemitente().getApellidos(), fontNormal));
        document.add(new Paragraph("DNI/RUC: " + envio.getRemitente().getNumeroDocumento(), fontNormal));
        document.add(new Paragraph(" ", fontNormal));

        // Destinatario
        document.add(new Paragraph("DESTINATARIO", fontSubtitulo));
        document.add(new Paragraph(envio.getDestinatario().getNombres() + " " + envio.getDestinatario().getApellidos(), fontNormal));
        document.add(new Paragraph("DNI/RUC: " + envio.getDestinatario().getNumeroDocumento(), fontNormal));
        document.add(new Paragraph(" ", fontNormal));

        // Datos del Envío
        document.add(new Paragraph("DETALLE", fontSubtitulo));
        document.add(new Paragraph("Origen: " + envio.getAgenciaOrigen().getNombre(), fontNormal));
        document.add(new Paragraph("Destino: " + envio.getAgenciaDestino().getNombre(), fontNormal));
        document.add(new Paragraph("Peso Real: " + envio.getAdmision().getPesoKg() + " kg", fontNormal));
        document.add(new Paragraph("Descripción: " + envio.getAdmision().getContenidoDeclarado(), fontNormal));
        document.add(new Paragraph("--------------------------------------------------", fontNormal));

        // Totales
        Paragraph total = new Paragraph("TOTAL A PAGAR: S/ " + envio.getImporteCotizado(), fontTitulo);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        Paragraph estado = new Paragraph("Estado: " + envio.getEstado(), fontSubtitulo);
        estado.setAlignment(Element.ALIGN_CENTER);
        document.add(estado);

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // MÉTODO 2: GENERAR GUÍA DE REMISIÓN (Documento Legal)
    // ==========================================
    public InputStream generarGuiaPdf(Long envioId) throws Exception {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5, 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();

        // Estilos
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Encabezado
        Paragraph titulo = new Paragraph("GUÍA DE REMISIÓN ELECTRÓNICA", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph empresa = new Paragraph("ATYRA EXPRESS S.A.C. | RUC: 20601234567", fontSubtitulo);
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);

        document.add(new Paragraph(" ", fontNormal));

        // Datos de la Guía
        PdfPTable tableDatos = new PdfPTable(2);
        tableDatos.setWidthPercentage(100);
        tableDatos.addCell(new PdfPCell(new Paragraph("N° Guía: " + envio.getNumeroGuia(), fontSubtitulo)));
        tableDatos.addCell(new PdfPCell(new Paragraph("Fecha Emisión: " + LocalDate.now(), fontNormal)));
        document.add(tableDatos);

        document.add(new Paragraph(" ", fontNormal));

        // Datos Remitente y Destinatario (Tabla)
        PdfPTable tablePersonas = new PdfPTable(2);
        tablePersonas.setWidthPercentage(100);

        // Remitente
        PdfPCell cellRemitente = new PdfPCell();
        cellRemitente.addElement(new Paragraph("REMITENTE", fontSubtitulo));
        cellRemitente.addElement(new Paragraph(envio.getRemitente().getNombres() + " " + envio.getRemitente().getApellidos(), fontNormal));
        cellRemitente.addElement(new Paragraph("DNI/RUC: " + envio.getRemitente().getNumeroDocumento(), fontNormal));
        tablePersonas.addCell(cellRemitente);

        // Destinatario
        PdfPCell cellDestinatario = new PdfPCell();
        cellDestinatario.addElement(new Paragraph("DESTINATARIO", fontSubtitulo));
        cellDestinatario.addElement(new Paragraph(envio.getDestinatario().getNombres() + " " + envio.getDestinatario().getApellidos(), fontNormal));
        cellDestinatario.addElement(new Paragraph("DNI/RUC: " + envio.getDestinatario().getNumeroDocumento(), fontNormal));
        tablePersonas.addCell(cellDestinatario);

        document.add(tablePersonas);
        document.add(new Paragraph(" ", fontNormal));

        // Datos del Transporte (Tabla)
        PdfPTable tableTransporte = new PdfPTable(3);
        tableTransporte.setWidthPercentage(100);
        tableTransporte.addCell(new PdfPCell(new Paragraph("ORIGEN", fontSubtitulo)));
        tableTransporte.addCell(new PdfPCell(new Paragraph("DESTINO", fontSubtitulo)));
        tableTransporte.addCell(new PdfPCell(new Paragraph("PESO BRUTO", fontSubtitulo)));

        tableTransporte.addCell(new PdfPCell(new Paragraph(envio.getAgenciaOrigen().getNombre(), fontNormal)));
        tableTransporte.addCell(new PdfPCell(new Paragraph(envio.getAgenciaDestino().getNombre(), fontNormal)));
        tableTransporte.addCell(new PdfPCell(new Paragraph(envio.getAdmision().getPesoKg() + " kg", fontNormal)));

        document.add(tableTransporte);
        document.add(new Paragraph(" ", fontNormal));

        // Descripción
        document.add(new Paragraph("DESCRIPCIÓN DE LA CARGA", fontSubtitulo));
        document.add(new Paragraph(envio.getAdmision().getContenidoDeclarado(), fontNormal));

        document.add(new Paragraph(" ", fontNormal));
        document.add(new Paragraph("--------------------------------------------------", fontNormal));

        // Pie de Firma
        document.add(new Paragraph("Recibido conforme (Firma y DNI):", fontNormal));
        document.add(new Paragraph(" ", fontNormal));
        document.add(new Paragraph(" ", fontNormal));
        document.add(new Paragraph("_______________________________", fontNormal));

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}