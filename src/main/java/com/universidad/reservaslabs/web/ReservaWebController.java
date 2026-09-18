package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador Web MVC para la gestión visual del Sistema de Reservas con Thymeleaf.
 * Inyecta la misma instancia de ReservaService y LaboratorioRepository que la API REST.
 */
@Controller
public class ReservaWebController {

    private final ReservaService reservaService;
    private final LaboratorioRepository laboratorioRepository;

    @Autowired
    public ReservaWebController(ReservaService reservaService, LaboratorioRepository laboratorioRepository) {
        this.reservaService = reservaService;
        this.laboratorioRepository = laboratorioRepository;
    }

    /**
     * Redirige la ruta raíz a /reservas.
     */
    @GetMapping("/")
    public String redireccionarALista() {
        return "redirect:/reservas";
    }

    /**
     * Vista principal con la tabla de reservas y catálogo de laboratorios (/reservas).
     */
    @GetMapping("/reservas")
    public String listarReservas(Model model) {
        model.addAttribute("reservas", reservaService.listarTodas());
        model.addAttribute("laboratorios", laboratorioRepository.findAll());
        return "reservas/lista";
    }

    /**
     * Formulario para registrar una nueva reserva (/reservas/nueva).
     */
    @GetMapping("/reservas/nueva")
    public String formularioNuevaReserva(Model model) {
        if (!model.containsAttribute("reserva")) {
            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(new Laboratorio());
            model.addAttribute("reserva", nuevaReserva);
        }
        model.addAttribute("laboratorios", laboratorioRepository.findAll());
        return "reservas/nueva";
    }

    /**
     * Procesa la creación de una nueva reserva desde el formulario web (POST /reservas).
     */
    @PostMapping("/reservas")
    public String procesarCreacionReserva(
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("laboratorios", laboratorioRepository.findAll());
            return "reservas/nueva";
        }

        reservaService.crearReserva(reserva);
        redirectAttributes.addFlashAttribute("mensaje",
                "¡Reserva registrada con éxito para " + reserva.getNombreSolicitante() + "!");
        return "redirect:/reservas";
    }

    /**
     * Procesa la cancelación de una reserva existente (POST /reservas/{id}/cancelar).
     */
    @PostMapping("/reservas/{id}/cancelar")
    public String procesarCancelacionReserva(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        reservaService.cancelarReserva(id);
        redirectAttributes.addFlashAttribute("mensaje", "La reserva #" + id + " ha sido cancelada exitosamente.");
        return "redirect:/reservas";
    }
}
