package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manejador de excepciones desacoplado para las vistas Web MVC (@Controller).
 * Captura excepciones de negocio como ReservaConflictException y redirige amigablemente
 * a la interfaz de usuario con mensajes Flash Attributes en lugar de devolver JSON.
 */
@ControllerAdvice(assignableTypes = ReservaWebController.class)
public class ReservaWebExceptionHandler {

    @ExceptionHandler(ReservaConflictException.class)
    public String manejarConflictoReserva(
            ReservaConflictException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        // Si el error ocurrió durante la cancelación, redirigir a la lista principal
        if (request.getRequestURI() != null && request.getRequestURI().contains("cancelar")) {
            return "redirect:/reservas";
        }
        // Para conflictos al crear una nueva reserva, redirigir al formulario
        return "redirect:/reservas/nueva";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/reservas";
    }

    @ExceptionHandler(Exception.class)
    public String manejarExcepcionGenerica(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", "Error procesando la solicitud: " + ex.getMessage());
        if (request.getRequestURI() != null && request.getRequestURI().contains("cancelar")) {
            return "redirect:/reservas";
        }
        return "redirect:/reservas/nueva";
    }
}
