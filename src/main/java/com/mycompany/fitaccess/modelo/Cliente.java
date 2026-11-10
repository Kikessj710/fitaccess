package com.mycompany.fitaccess.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Cliente {

    private int id;
    private String cedula;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private double alturaCm;
    private boolean consentimiento;
    private LocalDateTime fechaRegistro;
    private boolean activo = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public double getAlturaCm() { return alturaCm; }
    public void setAlturaCm(double alturaCm) { this.alturaCm = alturaCm; }
    public boolean isConsentimiento() { return consentimiento; }
    public void setConsentimiento(boolean consentimiento) { this.consentimiento = consentimiento; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /** Primer nombre, para saludar: "Hola Enrique". */
    public String getPrimerNombre() {
        return nombres == null ? "" : nombres.trim().split("\\s+")[0];
    }

    public int getEdad() {
        return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cedula + ")";
    }
}
