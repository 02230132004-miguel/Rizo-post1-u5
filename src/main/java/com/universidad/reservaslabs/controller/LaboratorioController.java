package com.universidad.reservaslabs.controller;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del catálogo de Laboratorios.
 * Inyecta directamente LaboratorioRepository para operaciones CRUD sencillas.
 */
@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioRepository laboratorioRepository;

    @Autowired
    public LaboratorioController(LaboratorioRepository laboratorioRepository) {
        this.laboratorioRepository = laboratorioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Laboratorio>> listarLaboratorios() {
        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        return ResponseEntity.ok(laboratorios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laboratorio> obtenerLaboratorio(@PathVariable Long id) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Laboratorio con ID " + id + " no encontrado."));
        return ResponseEntity.ok(laboratorio);
    }

    @PostMapping
    public ResponseEntity<Laboratorio> crearLaboratorio(@Valid @RequestBody Laboratorio laboratorio) {
        if (laboratorioRepository.existsByNombreIgnoreCase(laboratorio.getNombre())) {
            throw new ReservaConflictException("Ya existe un laboratorio registrado con el nombre: " + laboratorio.getNombre());
        }
        Laboratorio guardado = laboratorioRepository.save(laboratorio);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLaboratorio(@PathVariable Long id) {
        if (!laboratorioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Laboratorio con ID " + id + " no encontrado.");
        }
        laboratorioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
