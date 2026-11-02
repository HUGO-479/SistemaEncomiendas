package org.example.agencia.service;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AgenciaService {

    private final AgenciaRepository repository;

    public AgenciaService(AgenciaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Agencia> listarTodas() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Agencia buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Agencia no encontrada"));
    }

    @Transactional
    public Agencia guardar(Agencia agencia) {

        if (agencia.getNombre() == null ||
                agencia.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la agencia es obligatorio");
        }

        agencia.setNombre(agencia.getNombre().trim());

        if (agencia.getId() == null) {

            if (repository.existsByNombreIgnoreCase(agencia.getNombre())) {
                throw new IllegalArgumentException(
                        "Ya existe una agencia con ese nombre");
            }

            agencia.setActiva(true);
            return repository.save(agencia);
        }

        Agencia existente = buscarPorId(agencia.getId());

        if (repository.existsByNombreIgnoreCaseAndIdNot(
                agencia.getNombre(), agencia.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe otra agencia con ese nombre");
        }

        existente.setNombre(agencia.getNombre());
        existente.setDepartamento(agencia.getDepartamento());
        existente.setCiudad(agencia.getCiudad());
        existente.setDireccion(agencia.getDireccion());
        existente.setTelefono(agencia.getTelefono());

        return repository.save(existente);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Agencia agencia = buscarPorId(id);
        agencia.setActiva(!Boolean.TRUE.equals(agencia.getActiva()));
        repository.save(agencia);
    }
}