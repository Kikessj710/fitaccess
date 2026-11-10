package com.mycompany.fitaccess.modelo;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Suscripcion {

    private int id;
    private int clienteId;
    private int planId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado = "ACTIVA";

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /** Vigente = activa y hoy esta entre la fecha de inicio y la de fin. */
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return "ACTIVA".equals(estado) && !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    /** Dias que le quedan contando hoy (0 si ya vencio). */
    public int diasRestantes() {
        long d = ChronoUnit.DAYS.between(LocalDate.now(), fechaFin) + 1;
        return (int) Math.max(0, d);
    }
}
