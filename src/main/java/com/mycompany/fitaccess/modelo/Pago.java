package com.mycompany.fitaccess.modelo;

import java.time.LocalDateTime;

public class Pago {

    private int id;
    private int clienteId;
    private int suscripcionId;
    private LocalDateTime fecha;
    private double montoBase;
    private double porcentajeDescuento;
    private double montoFinal;
    private String metodo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getSuscripcionId() { return suscripcionId; }
    public void setSuscripcionId(int suscripcionId) { this.suscripcionId = suscripcionId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    public double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(double montoFinal) { this.montoFinal = montoFinal; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
