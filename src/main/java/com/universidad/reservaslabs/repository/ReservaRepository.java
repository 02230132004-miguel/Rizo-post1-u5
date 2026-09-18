package com.universidad.reservaslabs.repository;

import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Reserva.
 * Contiene consultas JPQL personalizadas para la detección de solapamientos horarios.
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Consulta JPQL personalizada para buscar reservas activas que se solapen con el intervalo dado
     * para un laboratorio específico. Excluye reservas con estado CANCELADA.
     *
     * Regla matemática de solapamiento de intervalos [A, B) y [C, D):
     * A < D AND B > C  (donde A=r.inicio, B=r.fin, C=inicio, D=fin)
     *
     * @param laboratorioId ID del laboratorio a consultar
     * @param inicio Fecha y hora de inicio del intervalo deseado
     * @param fin Fecha y hora de fin del intervalo deseado
     * @return Lista de reservas que presentan solapamiento
     */
    @Query("SELECT r FROM Reserva r WHERE r.laboratorio.id = :laboratorioId " +
           "AND r.estado <> com.universidad.reservaslabs.model.EstadoReserva.CANCELADA " +
           "AND r.inicio < :fin AND r.fin > :inicio")
    List<Reserva> buscarSolapamientos(@Param("laboratorioId") Long laboratorioId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);

    /**
     * Encuentra todas las reservas asociadas a un laboratorio específico.
     * @param laboratorioId ID del laboratorio
     * @return Lista de reservas
     */
    List<Reserva> findByLaboratorioId(Long laboratorioId);

    /**
     * Encuentra reservas por su estado actual.
     * @param estado Estado de la reserva
     * @return Lista de reservas
     */
    List<Reserva> findByEstado(EstadoReserva estado);
}
