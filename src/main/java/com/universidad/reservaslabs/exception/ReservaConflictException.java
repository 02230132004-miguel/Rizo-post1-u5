package com.universidad.reservaslabs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando una operación entra en conflicto con el estado o las reglas del negocio,
 * como el solapamiento horario de una reserva o la cancelación inválida.
 * Mapeada a código HTTP 409 CONFLICT.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ReservaConflictException extends RuntimeException {

    public ReservaConflictException(String mensaje) {
        super(mensaje);
    }
}
