package com.universidad.reservaslabs.service;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ReservaService con JUnit 5 y Mockito.
 * Valida minuciosamente las reglas de negocio de horario, duración, solapamientos y cancelación.
 */
@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private LaboratorioRepository laboratorioRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Laboratorio labPrueba;
    private LocalDate fechaFutura;

    @BeforeEach
    public void setUp() {
        labPrueba = Laboratorio.builder()
                .id(1L)
                .nombre("Lab de Software")
                .ubicacion("Edificio C")
                .capacidad(30)
                .tipo("Informática")
                .build();

        fechaFutura = LocalDate.now().plusDays(2);
    }

    @Test
    @DisplayName("Debe crear una reserva exitosamente cuando los horarios y solapamientos son válidos")
    public void crearReserva_Exitosa() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(10, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(12, 0));

        Reserva reserva = Reserva.builder()
                .laboratorio(labPrueba)
                .nombreSolicitante("Miguel Angel Rizo")
                .correoSolicitante("mrizo@universidad.edu")
                .inicio(inicio)
                .fin(fin)
                .motivo("Clase de Arquitectura de Software")
                .build();

        when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(labPrueba));
        when(reservaRepository.buscarSolapamientos(eq(1L), eq(inicio), eq(fin))).thenReturn(Collections.emptyList());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva creada = reservaService.crearReserva(reserva);

        assertNotNull(creada);
        assertEquals(EstadoReserva.CONFIRMADA, creada.getEstado());
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando el horario solicitado se solapa con una reserva existente")
    public void crearReserva_FallaPorSolapamiento() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(10, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(12, 0));

        Reserva reserva = Reserva.builder()
                .laboratorio(labPrueba)
                .nombreSolicitante("Estudiante")
                .correoSolicitante("test@universidad.edu")
                .inicio(inicio)
                .fin(fin)
                .motivo("Taller")
                .build();

        Reserva reservaExistente = Reserva.builder()
                .id(99L)
                .laboratorio(labPrueba)
                .inicio(inicio.plusMinutes(30))
                .fin(fin.plusMinutes(30))
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(labPrueba));
        when(reservaRepository.buscarSolapamientos(eq(1L), eq(inicio), eq(fin)))
                .thenReturn(List.of(reservaExistente));

        ReservaConflictException ex = assertThrows(ReservaConflictException.class, () ->
                reservaService.crearReserva(reserva));

        assertTrue(ex.getMessage().contains("ya cuenta con una reserva activa"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando la duración es menor a 30 minutos")
    public void validarHorario_DuracionMenor30Minutos() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(10, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(10, 20)); // 20 min

        ReservaConflictException ex = assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicio, fin));

        assertTrue(ex.getMessage().contains("mínima de una reserva es de 30 minutos"));
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando la duración es mayor a 3 horas")
    public void validarHorario_DuracionMayor3Horas() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(10, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(13, 30)); // 3.5 hrs

        ReservaConflictException ex = assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicio, fin));

        assertTrue(ex.getMessage().contains("máxima de una reserva es de 3 horas"));
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando el horario está fuera del rango hábil (07:00 a 21:00)")
    public void validarHorario_FueraDeHorarioHabil() {
        // Antes de las 07:00
        LocalDateTime inicioTemprano = LocalDateTime.of(fechaFutura, LocalTime.of(6, 0));
        LocalDateTime finTemprano = LocalDateTime.of(fechaFutura, LocalTime.of(8, 0));

        assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicioTemprano, finTemprano));

        // Después de las 21:00
        LocalDateTime inicioTarde = LocalDateTime.of(fechaFutura, LocalTime.of(20, 0));
        LocalDateTime finTarde = LocalDateTime.of(fechaFutura, LocalTime.of(22, 0));

        assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicioTarde, finTarde));
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando la fecha de inicio es anterior a la fecha actual (pasado)")
    public void validarHorario_FechaPasada() {
        LocalDateTime inicioPasado = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        LocalDateTime finPasado = inicioPasado.plusHours(1);

        assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicioPasado, finPasado));
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException cuando la hora de fin es anterior o igual a la de inicio")
    public void validarHorario_FinAnteriorAInicio() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(12, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(10, 0));

        assertThrows(ReservaConflictException.class, () ->
                reservaService.validarHorarioYDuracion(inicio, fin));
    }

    @Test
    @DisplayName("Debe cancelar exitosamente una reserva futura")
    public void cancelarReserva_Exitosa() {
        LocalDateTime inicio = LocalDateTime.of(fechaFutura, LocalTime.of(14, 0));
        LocalDateTime fin = LocalDateTime.of(fechaFutura, LocalTime.of(16, 0));

        Reserva reserva = Reserva.builder()
                .id(10L)
                .laboratorio(labPrueba)
                .inicio(inicio)
                .fin(fin)
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva cancelada = reservaService.cancelarReserva(10L);

        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    @DisplayName("Debe lanzar ReservaConflictException al intentar cancelar una reserva de una fecha pasada o ya iniciada")
    public void cancelarReserva_FallaPorFechaPasada() {
        LocalDateTime inicioPasado = LocalDateTime.now().minusHours(2);
        LocalDateTime finPasado = LocalDateTime.now().plusHours(1);

        Reserva reserva = Reserva.builder()
                .id(10L)
                .laboratorio(labPrueba)
                .inicio(inicioPasado)
                .fin(finPasado)
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        ReservaConflictException ex = assertThrows(ReservaConflictException.class, () ->
                reservaService.cancelarReserva(10L));

        assertTrue(ex.getMessage().contains("No es posible cancelar una reserva que ya ha iniciado"));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException cuando se consulta una reserva inexistente")
    public void obtenerPorId_Inexistente() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                reservaService.obtenerPorId(999L));
    }
}
