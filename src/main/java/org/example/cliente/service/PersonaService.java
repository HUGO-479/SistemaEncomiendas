package org.example.cliente.service;

import org.example.cliente.dto.ClienteRegistroDTO;
import org.example.cliente.model.Persona;
import org.example.cliente.model.TipoDocumento;
import org.example.cliente.repository.ClienteRepository;
import org.example.shared.ValidadorDocumento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PersonaService {

    private final ClienteRepository clienteRepository;

    public PersonaService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Optional<Persona> buscarPorDocumento(TipoDocumento tipo, String numero) {
        return clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipo, numero);
    }

    @Transactional
    public Persona registrarCliente(ClienteRegistroDTO dto) {
        // Validamos el formato
        String nombreORazonSocial = dto.getTipoDocumento() == TipoDocumento.RUC
                ? dto.getRazonSocial()
                : dto.getNombres();

        ValidadorDocumento.validar(dto.getTipoDocumento(), dto.getNumeroDocumento(), nombreORazonSocial);

        // Verificamos si ya existe
        Optional<Persona> existente = buscarPorDocumento(dto.getTipoDocumento(), dto.getNumeroDocumento());
        if (existente.isPresent()) {
            return existente.get();
        }

        // Convertimos el DTO a Entidad
        Persona nuevoCliente = new Persona();
        nuevoCliente.setTipoDocumento(dto.getTipoDocumento());
        nuevoCliente.setNumeroDocumento(dto.getNumeroDocumento());
        nuevoCliente.setNombres(dto.getNombres());
        nuevoCliente.setApellidos(dto.getApellidos());
        nuevoCliente.setRazonSocial(dto.getRazonSocial());
        nuevoCliente.setTelefono(dto.getCelular());
        nuevoCliente.setDireccion(dto.getDireccion());

        return clienteRepository.save(nuevoCliente);
    }
}