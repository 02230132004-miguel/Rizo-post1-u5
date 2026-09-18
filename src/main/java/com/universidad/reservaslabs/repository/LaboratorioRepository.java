package com.universidad.reservaslabs.repository;

import com.universidad.reservaslabs.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Laboratorio.
 */
@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    /**
     * Verifica si existe un laboratorio con el nombre dado (ignorando mayúsculas/minúsculas).
     * @param nombre Nombre del laboratorio
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca un laboratorio por su nombre (ignorando mayúsculas/minúsculas).
     * @param nombre Nombre del laboratorio
     * @return Optional con el Laboratorio si existe
     */
    Optional<Laboratorio> findByNombreIgnoreCase(String nombre);
}
