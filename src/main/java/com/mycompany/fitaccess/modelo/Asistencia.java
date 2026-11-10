package com.mycompany.fitaccess.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Asistencia {

    private int id;
    private int clienteId;
    private LocalDate fecha;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private Integer duracionMinutos;
    private boolean cierreAutomatico;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalDateTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalDateTime horaEntrada) { this.horaEntrada = horaEntrada; }
    public LocalDateTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalDateTime horaSalida) { this.horaSalida = horaSalida; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public boolean isCierreAutomatico() { return cierreAutomatico; }
    public void setCierreAutomatico(boolean cierreAutomatico) { this.cierreAutomatico = cierreAutomatico; }

    /** true si entro y todavia no marca la salida. */
    public boolean estaAbierta() {
        return horaSalida == null;
    }
}
