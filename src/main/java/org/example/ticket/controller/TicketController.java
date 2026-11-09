package org.example.ticket.controller;

import org.example.ticket.service.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ==========================================
    // RUTA 1: GENERAR TICKET (A6)
    // ==========================================
    @GetMapping("/generar/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws Exception {
        InputStream pdfStream = ticketService.generarPdf(id);
        byte[] pdfBytes = pdfStream.readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ==========================================
    // RUTA 2: GENERAR GUÍA DE REMISIÓN (A5)
    // ==========================================
    @GetMapping("/guia/{id}")
    public ResponseEntity<byte[]> generarGuia(@PathVariable Long id) throws Exception {
        InputStream pdfStream = ticketService.generarGuiaPdf(id);
        byte[] pdfBytes = pdfStream.readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "guia_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}