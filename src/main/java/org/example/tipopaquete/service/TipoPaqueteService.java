package org.example.tipopaquete.service;

import org.example.tipopaquete.model.TipoPaquete;
import org.example.tipopaquete.repository.TipoPaqueteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TipoPaqueteService {

    private final TipoPaqueteRepository repository;

    public TipoPaqueteService(TipoPaqueteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoPaquete> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public TipoPaquete buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Tipo de paquete no encontrado"
                        ));
    }

    @Transactional
    public TipoPaquete guardar(TipoPaquete datos) {

        String nombre = datos.getNombre();

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        nombre = nombre.trim();

        if (nombre.length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar 50 caracteres"
            );
        }

        if (datos.getId() == null) {

            if (repository.existsByNombreIgnoreCase(nombre)) {
                throw new IllegalArgumentException(
                        "Ya existe un tipo de paquete con ese nombre"
                );
            }

            TipoPaquete nuevo = new TipoPaquete();
            nuevo.setNombre(nombre);
            nuevo.setDescripcion(datos.getDescripcion());
            nuevo.setActivo(true);

            return repository.save(nuevo);
        }

        TipoPaquete existente = buscarPorId(datos.getId());

        if (repository.existsByNombreIgnoreCaseAndIdNot(
                nombre, existente.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe otro tipo de paquete con ese nombre"
            );
        }

        existente.setNombre(nombre);
        existente.setDescripcion(datos.getDescripcion());

        return repository.save(existente);
    }

    @Transactional
    public void cambiarEstado(Long id) {

        TipoPaquete tipo = buscarPorId(id);

        tipo.setActivo(
                !Boolean.TRUE.equals(tipo.getActivo())
        );

        repository.save(tipo);
    }
}