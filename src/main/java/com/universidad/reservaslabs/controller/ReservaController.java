package com.universidad.reservaslabs.controller;

import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Reservas de Laboratorios.
 * Inyecta ReservaService para delegar la lógica y reglas de negocio.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    @Autowired
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listarReservas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @GetMapping("/laboratorio/{laboratorioId}")
    public ResponseEntity<List<Reserva>> listarPorLaboratorio(@PathVariable Long laboratorioId) {
        return ResponseEntity.ok(reservaService.listarPorLaboratorio(laboratorioId));
    }

    @PostMapping
    public ResponseEntity<Reserva> crearReserva(@Valid @RequestBody Reserva reserva) {
        Reserva nueva = reservaService.crearReserva(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelarReservaPost(@PathVariable Long id) {
        Reserva cancelada = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(cancelada);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelarReservaPut(@PathVariable Long id) {
        Reserva cancelada = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(cancelada);
    }
}
