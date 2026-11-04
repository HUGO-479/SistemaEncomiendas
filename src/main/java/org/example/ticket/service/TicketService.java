package org.example.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.example.envio.model.Envio;
import org.example.envio.repository.EnvioRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
public class TicketService {

    private final EnvioRepository envioRepository;

    public TicketService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    public InputStream generarPdf(Long envioId) throws Exception {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6); // Tamaño ticket
        PdfWriter.getInstance(document, out);
        document.open();

        // Título
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        document.add(new Paragraph("TICKET DE ENVÍO", fontTitulo));
        document.add(new Paragraph("Código: " + envio.getNumeroGuia()));

        // Generar QR
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(envio.getNumeroGuia(), BarcodeFormat.QR_CODE, 150, 150);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        Image qrImage = Image.getInstance(pngData);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        // Datos
        document.add(new Paragraph("Remitente: " + envio.getRemitente().getNombres() + " " + envio.getRemitente().getApellidos()));
        document.add(new Paragraph("Destinatario: " + envio.getDestinatario().getNombres() + " " + envio.getDestinatario().getApellidos()));
        document.add(new Paragraph("Estado: " + envio.getEstado()));
        document.add(new Paragraph("Fecha: " + envio.getRegistradoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}