package com.universidad.reservaslabs.config;

import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Inicializador de datos de prueba para el entorno H2 en memoria.
 * Inserta 3 laboratorios universitarios y 1 reserva inicial confirmada para pruebas.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final LaboratorioRepository laboratorioRepository;
    private final ReservaRepository reservaRepository;

    @Autowired
    public DataInitializer(LaboratorioRepository laboratorioRepository, ReservaRepository reservaRepository) {
        this.laboratorioRepository = laboratorioRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Inicializando datos semilla en H2 Database...");

        // 1. Inserción de los 3 Laboratorios iniciales
        Laboratorio lab1 = Laboratorio.builder()
                .nombre("Laboratorio de Redes y Telecomunicaciones")
                .ubicacion("Edificio A - Aula 101 (Planta Baja)")
                .capacidad(30)
                .tipo("Especializado en Redes")
                .build();

        Laboratorio lab2 = Laboratorio.builder()
                .nombre("Laboratorio de Desarrollo de Software y Cloud")
                .ubicacion("Edificio C - Aula 205 (Segundo Piso)")
                .capacidad(40)
                .tipo("Informática y Computación")
                .build();

        Laboratorio lab3 = Laboratorio.builder()
                .nombre("Laboratorio de Inteligencia Artificial y Robótica")
                .ubicacion("Edificio B - Aula 104 (Primer Piso)")
                .capacidad(25)
                .tipo("Investigación y Robótica")
                .build();

        laboratorioRepository.save(lab1);
        laboratorioRepository.save(lab2);
        laboratorioRepository.save(lab3);
        log.info("Laboratorios registrados exitosamente: 3");

        // 2. Inserción de 1 Reserva inicial confirmada para el día de mañana (09:00 a 11:00)
        LocalDate fechaManana = LocalDate.now().plusDays(1);
        LocalDateTime inicioReserva = LocalDateTime.of(fechaManana, LocalTime.of(9, 0));
        LocalDateTime finReserva = LocalDateTime.of(fechaManana, LocalTime.of(11, 0));

        Reserva reservaInicial = Reserva.builder()
                .laboratorio(lab1)
                .nombreSolicitante("Miguel Angel Rizo Arias")
                .correoSolicitante("mrizo@universidad.edu")
                .inicio(inicioReserva)
                .fin(finReserva)
                .motivo("Práctica de Enrutamiento y Conmutación de Redes CISCO")
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        reservaRepository.save(reservaInicial);
        log.info("Reserva inicial de prueba registrada exitosamente para el ID de lab: {}", lab1.getId());
    }
}
