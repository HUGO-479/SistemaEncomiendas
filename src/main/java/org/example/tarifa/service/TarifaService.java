
package org.example.tarifa.service;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.example.tarifa.model.Tarifa;
import org.example.tarifa.repository.TarifaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TarifaService {

    private final TarifaRepository tarifas;
    private final AgenciaRepository agencias;

    public TarifaService(
            TarifaRepository tarifas,
            AgenciaRepository agencias) {
        this.tarifas = tarifas;
        this.agencias = agencias;
    }

    @Transactional(readOnly = true)
    public List<Tarifa> listarTodas() {
        return tarifas.findAll();
    }

    @Transactional(readOnly = true)
    public Tarifa buscarPorId(Long id) {
        return tarifas.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Tarifa no encontrada"));
    }

    @Transactional
    public Tarifa guardar(
            Long id,
            Long origenId,
            Long destinoId,
            BigDecimal precioBase,
            BigDecimal precioPorKg) {

        if (origenId == null || destinoId == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar origen y destino");
        }

        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException(
                    "El origen y el destino deben ser diferentes");
        }

        if (precioBase == null || precioPorKg == null
                || precioBase.signum() < 0
                || precioPorKg.signum() < 0) {
            throw new IllegalArgumentException(
                    "Los precios no pueden ser negativos");
        }

        if (precioBase.scale() > 2 || precioPorKg.scale() > 2
                || precioBase.precision() - precioBase.scale() > 8
                || precioPorKg.precision() - precioPorKg.scale() > 8) {
            throw new IllegalArgumentException(
                    "Los precios deben tener máximo 8 enteros y 2 decimales");
        }

        Agencia origen = agencias.findById(origenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La agencia de origen no existe"));

        Agencia destino = agencias.findById(destinoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La agencia de destino no existe"));

        if (!Boolean.TRUE.equals(origen.getActiva())
                || !Boolean.TRUE.equals(destino.getActiva())) {
            throw new IllegalArgumentException(
                    "Ambas agencias deben estar activas");
        }

        boolean duplicada = id == null
                ? tarifas.existsByAgenciaOrigenIdAndAgenciaDestinoId(
                        origenId, destinoId)
                : tarifas.existsByAgenciaOrigenIdAndAgenciaDestinoIdAndIdNot(
                        origenId, destinoId, id);

        if (duplicada) {
            throw new IllegalArgumentException(
                    "Ya existe una tarifa para esta ruta");
        }

        Tarifa tarifa = id == null ? new Tarifa() : buscarPorId(id);

        tarifa.setAgenciaOrigen(origen);
        tarifa.setAgenciaDestino(destino);
        tarifa.setPrecioBase(precioBase);
        tarifa.setPrecioPorKg(precioPorKg);

        return tarifas.save(tarifa);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Tarifa tarifa = buscarPorId(id);
        tarifa.setActiva(!Boolean.TRUE.equals(tarifa.getActiva()));
        tarifas.save(tarifa);
    }
}
