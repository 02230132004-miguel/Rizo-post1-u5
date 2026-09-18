package com.universidad.reservaslabs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Entidad de dominio que representa un Laboratorio universitario.
 */
@Entity
@Table(name = "laboratorios")
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del laboratorio es obligatorio")
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @NotBlank(message = "La ubicación del laboratorio es obligatoria")
    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima debe ser de al menos 1 persona")
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @NotBlank(message = "El tipo de laboratorio es obligatorio")
    @Column(name = "tipo", nullable = false, length = 80)
    private String tipo;

    public Laboratorio() {
    }

    public Laboratorio(Long id, String nombre, String ubicacion, Integer capacidad, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    public Laboratorio(String nombre, String ubicacion, Integer capacidad, String tipo) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    public static LaboratorioBuilder builder() {
        return new LaboratorioBuilder();
    }

    public static class LaboratorioBuilder {
        private Long id;
        private String nombre;
        private String ubicacion;
        private Integer capacidad;
        private String tipo;

        public LaboratorioBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LaboratorioBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public LaboratorioBuilder ubicacion(String ubicacion) {
            this.ubicacion = ubicacion;
            return this;
        }

        public LaboratorioBuilder capacidad(Integer capacidad) {
            this.capacidad = capacidad;
            return this;
        }

        public LaboratorioBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public Laboratorio build() {
            return new Laboratorio(id, nombre, ubicacion, capacidad, tipo);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Laboratorio that = (Laboratorio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Laboratorio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", capacidad=" + capacidad +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
