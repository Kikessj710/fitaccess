package com.mycompany.fitaccess.servicio;

import com.mycompany.fitaccess.modelo.Cliente;

/** ESQUELETO: Matias reemplaza este archivo completo con el codigo real. */
public class AccesoService {

    public enum Tipo { ENTRADA, SALIDA, VENCIDO, NO_RECONOCIDO, ESPERA }

    public static class Resultado {
        public final Tipo tipo;
        public final Cliente cliente;
        public final String mensaje;
        public final int diasRestantes;
        public final int minutos;

        Resultado(Tipo tipo, Cliente cliente, String mensaje, int diasRestantes, int minutos) {
            this.tipo = tipo;
            this.cliente = cliente;
            this.mensaje = mensaje;
            this.diasRestantes = diasRestantes;
            this.minutos = minutos;
        }
    }

    public Resultado procesar(int clienteId) {
        return new Resultado(Tipo.NO_RECONOCIDO, null, "Pendiente (Matias)", 0, 0);
    }

    public Resultado registrarSalida(int clienteId) {
        return procesar(clienteId);
    }

    public int cerrarOlvidados() { return 0; }
    public void iniciarCierreAutomatico() { }
    public int calcularRacha(int clienteId) { return 0; }

    public static String formatoDuracion(int minutos) { return minutos + " min"; }
    public static String formatoCorto(int minutos) { return minutos + "m"; }
}
