package com.mycompany.fitaccess.modelo;

public class Plan {

    private int id;
    private String nombre;
    private int dias;
    private double precio;
    private boolean activo = true;

    public Plan() {
    }

    public Plan(String nombre, int dias, double precio) {
        this.nombre = nombre;
        this.dias = dias;
        this.precio = precio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Asi se ve en los JComboBox. */
    @Override
    public String toString() {
        return String.format("%s - %d dias - $%.2f", nombre, dias, precio);
    }
}
