package com.universidad.reservaslabs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa la Reserva de un Laboratorio.
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El laboratorio es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Column(name = "nombre_solicitante", nullable = false, length = 120)
    private String nombreSolicitante;

    @NotBlank(message = "El correo del solicitante es obligatorio")
    @Email(message = "El formato de correo no es válido")
    @Column(name = "correo_solicitante", nullable = false, length = 120)
    private String correoSolicitante;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    @NotBlank(message = "El motivo de la reserva es obligatorio")
    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @NotNull(message = "El estado de la reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    public Reserva() {
        this.laboratorio = new Laboratorio();
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public Reserva(Long id, Laboratorio laboratorio, String nombreSolicitante, String correoSolicitante,
                   LocalDateTime inicio, LocalDateTime fin, String motivo, EstadoReserva estado) {
        this.id = id;
        this.laboratorio = (laboratorio != null) ? laboratorio : new Laboratorio();
        this.nombreSolicitante = nombreSolicitante;
        this.correoSolicitante = correoSolicitante;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = (estado != null) ? estado : EstadoReserva.CONFIRMADA;
    }

    public Reserva(Laboratorio laboratorio, String nombreSolicitante, String correoSolicitante,
                   LocalDateTime inicio, LocalDateTime fin, String motivo, EstadoReserva estado) {
        this.laboratorio = (laboratorio != null) ? laboratorio : new Laboratorio();
        this.nombreSolicitante = nombreSolicitante;
        this.correoSolicitante = correoSolicitante;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = (estado != null) ? estado : EstadoReserva.CONFIRMADA;
    }

    public static ReservaBuilder builder() {
        return new ReservaBuilder();
    }

    public static class ReservaBuilder {
        private Long id;
        private Laboratorio laboratorio;
        private String nombreSolicitante;
        private String correoSolicitante;
        private LocalDateTime inicio;
        private LocalDateTime fin;
        private String motivo;
        private EstadoReserva estado = EstadoReserva.CONFIRMADA;

        public ReservaBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ReservaBuilder laboratorio(Laboratorio laboratorio) {
            this.laboratorio = laboratorio;
            return this;
        }

        public ReservaBuilder nombreSolicitante(String nombreSolicitante) {
            this.nombreSolicitante = nombreSolicitante;
            return this;
        }

        public ReservaBuilder correoSolicitante(String correoSolicitante) {
            this.correoSolicitante = correoSolicitante;
            return this;
        }

        public ReservaBuilder inicio(LocalDateTime inicio) {
            this.inicio = inicio;
            return this;
        }

        public ReservaBuilder fin(LocalDateTime fin) {
            this.fin = fin;
            return this;
        }

        public ReservaBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public ReservaBuilder estado(EstadoReserva estado) {
            this.estado = estado;
            return this;
        }

        public Reserva build() {
            return new Reserva(id, laboratorio, nombreSolicitante, correoSolicitante, inicio, fin, motivo, estado);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Laboratorio getLaboratorio() {
        if (this.laboratorio == null) {
            this.laboratorio = new Laboratorio();
        }
        return this.laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", laboratorio=" + (laboratorio != null ? laboratorio.getNombre() : "null") +
                ", nombreSolicitante='" + nombreSolicitante + '\'' +
                ", correoSolicitante='" + correoSolicitante + '\'' +
                ", inicio=" + inicio +
                ", fin=" + fin +
                ", motivo='" + motivo + '\'' +
                ", estado=" + estado +
                '}';
    }
}
