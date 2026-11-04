package org.example.ticket.controller;

import org.example.ticket.service.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/generar/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws Exception {
        InputStream pdfStream = ticketService.generarPdf(id);
        byte[] pdfBytes = pdfStream.readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}