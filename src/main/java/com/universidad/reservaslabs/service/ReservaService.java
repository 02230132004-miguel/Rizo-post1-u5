package com.universidad.reservaslabs.service;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de Negocio para la gestión integral de Reservas de Laboratorios.
 * Implementa la lógica de dominio rica (no anémica), validando reglas temporales en memoria
 * y delegando el filtrado de solapamientos al motor SQL mediante JPA.
 */
@Service
@Transactional
public class ReservaService {

    public static final LocalTime APERTURA = LocalTime.of(7, 0);
    public static final LocalTime CIERRE = LocalTime.of(21, 0);
    public static final Duration DURACION_MINIMA = Duration.ofMinutes(30);
    public static final Duration DURACION_MAXIMA = Duration.ofHours(3);

    private final ReservaRepository reservaRepository;
    private final LaboratorioRepository laboratorioRepository;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository, LaboratorioRepository laboratorioRepository) {
        this.reservaRepository = reservaRepository;
        this.laboratorioRepository = laboratorioRepository;
    }

    /**
     * Valida en memoria y con Java puro las reglas de negocio sobre horarios y duración.
     * 1. Rango cronológico coherente (fin posterior a inicio).
     * 2. Mismo día calendario (no se permiten reservas que crucen medianoche).
     * 3. Duración permitida (entre 30 minutos y 3 horas).
     * 4. Horario hábil institucional (entre 07:00 y 21:00).
     * 5. No se permiten reservas con fecha de inicio en el pasado.
     *
     * @param inicio Fecha y hora de inicio
     * @param fin Fecha y hora de fin
     * @throws ReservaConflictException si alguna de las reglas no se cumple
     */
    public void validarHorarioYDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new ReservaConflictException("Las fechas y horas de inicio y fin son obligatorias.");
        }

        // 1. Rango cronológico
        if (!fin.isAfter(inicio)) {
            throw new ReservaConflictException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        // 2. Mismo día
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReservaConflictException("La reserva debe iniciar y finalizar en el mismo día.");
        }

        // 3. Duración permitida
        Duration duracion = Duration.between(inicio, fin);
        if (duracion.compareTo(DURACION_MINIMA) < 0) {
            throw new ReservaConflictException("La duración mínima de una reserva es de 30 minutos.");
        }
        if (duracion.compareTo(DURACION_MAXIMA) > 0) {
            throw new ReservaConflictException("La duración máxima de una reserva es de 3 horas.");
        }

        // 4. Horario hábil (07:00 - 21:00)
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();

        if (horaInicio.isBefore(APERTURA) || horaFin.isAfter(CIERRE)) {
            throw new ReservaConflictException(
                    String.format("El horario permitido para reservas es entre las %s y las %s.", APERTURA, CIERRE));
        }

        // 5. No permitir reservas en el pasado
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new ReservaConflictException("No es posible programar una reserva en fechas u horas pasadas.");
        }
    }

    /**
     * Crea y confirma una nueva reserva tras validar todas las reglas de negocio y solapamiento.
     *
     * @param reserva Objeto de reserva a persistir
     * @return Reserva confirmada y guardada
     */
    public Reserva crearReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("Los datos de la reserva no pueden ser nulos.");
        }

        // 1. Validar reglas de horario y duración en memoria
        validarHorarioYDuracion(reserva.getInicio(), reserva.getFin());

        // 2. Validar existencia del Laboratorio
        if (reserva.getLaboratorio() == null || reserva.getLaboratorio().getId() == null) {
            throw new RecursoNoEncontradoException("Debe asociar un laboratorio válido a la reserva.");
        }
        Long labId = reserva.getLaboratorio().getId();
        Laboratorio laboratorio = laboratorioRepository.findById(labId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Laboratorio con ID " + labId + " no fue encontrado."));
        reserva.setLaboratorio(laboratorio);

        // 3. Validar solapamiento delegando al motor SQL
        List<Reserva> solapamientos = reservaRepository.buscarSolapamientos(labId, reserva.getInicio(), reserva.getFin());
        if (!solapamientos.isEmpty()) {
            throw new ReservaConflictException(
                    "El laboratorio '" + laboratorio.getNombre() + "' ya cuenta con una reserva activa en el horario seleccionado.");
        }

        // 4. Establecer estado inicial si no está definido
        if (reserva.getEstado() == null) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Cancela una reserva de manera controlada.
     * No se permite cancelar reservas que ya hayan iniciado o transcurrido.
     *
     * @param id Identificador de la reserva
     * @return Reserva cancelada
     */
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = obtenerPorId(id);

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReservaConflictException("La reserva ya se encuentra cancelada.");
        }

        if (reserva.getInicio().isBefore(LocalDateTime.now())) {
            throw new ReservaConflictException("No es posible cancelar una reserva que ya ha iniciado o que pertenece al pasado.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    /**
     * Obtiene una reserva por su ID.
     *
     * @param id Identificador de la reserva
     * @return Reserva encontrada
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva con ID " + id + " no encontrada."));
    }

    /**
     * Lista todas las reservas registradas.
     *
     * @return Lista completa de reservas
     */
    @Transactional(readOnly = true)
    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    /**
     * Lista las reservas de un laboratorio determinado.
     *
     * @param laboratorioId ID del laboratorio
     * @return Lista de reservas del laboratorio
     */
    @Transactional(readOnly = true)
    public List<Reserva> listarPorLaboratorio(Long laboratorioId) {
        return reservaRepository.findByLaboratorioId(laboratorioId);
    }
}
