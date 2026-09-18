package com.universidad.reservaslabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para ReservaController y GlobalRestExceptionHandler.
 * Valida los códigos HTTP semánticos (201 Created, 409 Conflict, 404 Not Found, 200 OK).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private Laboratorio labTest;
    private LocalDate fechaPrueba;

    @BeforeEach
    public void setUp() {
        reservaRepository.deleteAll();
        laboratorioRepository.deleteAll();

        labTest = Laboratorio.builder()
                .nombre("Laboratorio de Pruebas Integración")
                .ubicacion("Edificio D - Aula 301")
                .capacidad(20)
                .tipo("Especializado")
                .build();
        labTest = laboratorioRepository.save(labTest);

        fechaPrueba = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("POST /api/reservas - Debe retornar 201 Created al registrar una reserva válida")
    public void crearReserva_DebeRetornar201() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(fechaPrueba, LocalTime.of(10, 0));
        LocalDateTime fin = LocalDateTime.of(fechaPrueba, LocalTime.of(12, 0));

        Reserva reserva = Reserva.builder()
                .laboratorio(labTest)
                .nombreSolicitante("Ing. Miguel Angel Rizo")
                .correoSolicitante("mrizo@universidad.edu")
                .inicio(inicio)
                .fin(fin)
                .motivo("Evaluación práctica de Sistemas Distribuidos")
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombreSolicitante", is("Ing. Miguel Angel Rizo")))
                .andExpect(jsonPath("$.estado", is("CONFIRMADA")))
                .andExpect(jsonPath("$.laboratorio.id", is(labTest.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/reservas - Debe retornar 409 Conflict al registrar una reserva con solapamiento horario")
    public void crearReserva_DebeRetornar409PorSolapamiento() throws Exception {
        LocalDateTime inicio1 = LocalDateTime.of(fechaPrueba, LocalTime.of(14, 0));
        LocalDateTime fin1 = LocalDateTime.of(fechaPrueba, LocalTime.of(16, 0));

        // 1. Guardar primera reserva
        Reserva primera = Reserva.builder()
                .laboratorio(labTest)
                .nombreSolicitante("Profesor A")
                .correoSolicitante("profesor.a@universidad.edu")
                .inicio(inicio1)
                .fin(fin1)
                .motivo("Laboratorio de Física")
                .estado(EstadoReserva.CONFIRMADA)
                .build();
        reservaRepository.save(primera);

        // 2. Intentar registrar segunda reserva que se solapa (15:00 a 17:00)
        LocalDateTime inicioSolapado = LocalDateTime.of(fechaPrueba, LocalTime.of(15, 0));
        LocalDateTime finSolapado = LocalDateTime.of(fechaPrueba, LocalTime.of(17, 0));

        Reserva segunda = Reserva.builder()
                .laboratorio(labTest)
                .nombreSolicitante("Profesor B")
                .correoSolicitante("profesor.b@universidad.edu")
                .inicio(inicioSolapado)
                .fin(finSolapado)
                .motivo("Laboratorio de Química")
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("ya cuenta con una reserva activa")));
    }

    @Test
    @DisplayName("GET /api/reservas/{id} - Debe retornar 404 Not Found para una reserva inexistente")
    public void obtenerReserva_DebeRetornar404() throws Exception {
        mockMvc.perform(get("/api/reservas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("no encontrada")));
    }

    @Test
    @DisplayName("GET /api/reservas - Debe retornar 200 OK con la lista de reservas")
    public void listarReservas_DebeRetornar200() throws Exception {
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/reservas/{id}/cancelar - Debe retornar 200 OK al cancelar una reserva futura")
    public void cancelarReserva_DebeRetornar200() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(fechaPrueba, LocalTime.of(8, 0));
        LocalDateTime fin = LocalDateTime.of(fechaPrueba, LocalTime.of(10, 0));

        Reserva reserva = Reserva.builder()
                .laboratorio(labTest)
                .nombreSolicitante("Investigador")
                .correoSolicitante("inv@universidad.edu")
                .inicio(inicio)
                .fin(fin)
                .motivo("Investigación de Posgrado")
                .estado(EstadoReserva.CONFIRMADA)
                .build();
        Reserva guardada = reservaRepository.save(reserva);

        mockMvc.perform(post("/api/reservas/" + guardada.getId() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(guardada.getId().intValue())))
                .andExpect(jsonPath("$.estado", is("CANCELADA")));
    }
}
