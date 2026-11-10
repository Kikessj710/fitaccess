package com.mycompany.fitaccess.hardware;

/** ESQUELETO: Sebastian reemplaza este archivo completo con el codigo real. */
public final class ArduinoPuerto {

    private static final ArduinoPuerto INSTANCIA = new ArduinoPuerto();

    public static ArduinoPuerto get() {
        return INSTANCIA;
    }

    public String[] listarPuertos() { return new String[0]; }
    public String describir(String nombre) { return nombre; }
    public boolean conectar(String nombre) { return false; }
    public boolean conectarAuto() { return false; }
    public boolean ping() { return false; }
    public void enviar(String linea) { }
    public void acceso(String nombre, int diasRestantes) { }
    public void salida(String nombre, String duracion) { }
    public void negado(String motivo) { }
    public void desconectar() { }
    public boolean conectado() { return false; }
    public String nombrePuerto() { return "-"; }
}
