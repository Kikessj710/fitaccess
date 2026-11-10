package com.mycompany.fitaccess.modelo;

import java.time.LocalDate;

public class Medida {

    private int id;
    private int clienteId;
    private LocalDate fecha;
    private double pesoKg;

    public Medida() {
    }

    public Medida(int clienteId, LocalDate fecha, double pesoKg) {
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }

    /** IMC = peso / altura(m)^2 */
    public static double imc(double pesoKg, double alturaCm) {
        double m = alturaCm / 100.0;
        return m <= 0 ? 0 : pesoKg / (m * m);
    }

    public static String categoriaImc(double imc) {
        if (imc <= 0) return "-";
        if (imc < 18.5) return "Bajo peso";
        if (imc < 25) return "Normal";
        if (imc < 30) return "Sobrepeso";
        return "Obesidad";
    }
}
