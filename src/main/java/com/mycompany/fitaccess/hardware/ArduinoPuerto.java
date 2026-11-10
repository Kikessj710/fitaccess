package com.mycompany.fitaccess.hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.fitaccess.util.Config;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Comunicacion con el Arduino por USB (serial 9600).
 * Protocolo: una linea de texto por mensaje.
 *   A;Nombre;dias   acceso permitido
 *   S;Nombre;1h35   salida
 *   N;VENCIDO       negado por suscripcion vencida
 *   N;DESCONOCIDO   rostro no reconocido
 *   P               ping (el Arduino responde OK)
 * Si no hay Arduino, todos los metodos simplemente no hacen nada: la app sigue funcionando.
 */
public final class ArduinoPuerto {

    private static final ArduinoPuerto INSTANCIA = new ArduinoPuerto();
    private SerialPort puerto;

    private ArduinoPuerto() {
    }

    public static ArduinoPuerto get() {
        return INSTANCIA;
    }

    /** Nombres de los puertos disponibles, por ejemplo "COM4". */
    public String[] listarPuertos() {
        SerialPort[] puertos = SerialPort.getCommPorts();
        String[] nombres = new String[puertos.length];
        for (int i = 0; i < puertos.length; i++) {
            nombres[i] = puertos[i].getSystemPortName();
        }
        return nombres;
    }

    /** Descripcion legible: "COM4 - USB-SERIAL CH340". */
    public String describir(String nombre) {
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getSystemPortName().equals(nombre)) {
                return nombre + " - " + p.getPortDescription();
            }
        }
        return nombre;
    }

    public synchronized boolean conectar(String nombre) {
        desconectar();
        try {
            SerialPort p = SerialPort.getCommPort(nombre);
            p.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 300, 0);
            if (!p.openPort()) {
                return false;
            }
            Thread.sleep(2000); // el Arduino se reinicia al abrir el puerto
            puerto = p;
            System.out.println("Arduino conectado en " + nombre);
            return true;
        } catch (Exception e) {
            System.err.println("No se pudo abrir " + nombre + ": " + e.getMessage());
            return false;
        }
    }

    /** Usa el puerto de config.properties; si esta vacio, busca uno que responda al ping. */
    public synchronized boolean conectarAuto() {
        String fijo = Config.get("arduino.puerto", "");
        if (!fijo.isEmpty()) {
            return conectar(fijo);
        }
        for (SerialPort p : SerialPort.getCommPorts()) {
            String desc = (p.getDescriptivePortName() + " " + p.getPortDescription()).toLowerCase();
            boolean pareceArduino = desc.contains("arduino") || desc.contains("ch340")
                    || desc.contains("usb-serial") || desc.contains("usb serial") || desc.contains("cp210");
            if (pareceArduino && conectar(p.getSystemPortName())) {
                if (ping()) {
                    return true;
                }
                desconectar();
            }
        }
        System.out.println("Arduino no encontrado: la app sigue sin hardware");
        return false;
    }

    /** Envia "P" y espera "OK" del Arduino. */
    public synchronized boolean ping() {
        if (!conectado()) {
            return false;
        }
        enviar("P");
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[1];
        long limite = System.currentTimeMillis() + 1500;
        while (System.currentTimeMillis() < limite) {
            if (puerto.readBytes(b, 1) > 0) {
                if (b[0] == '\n') {
                    break;
                }
                sb.append((char) b[0]);
            }
        }
        return sb.toString().trim().equals("OK");
    }

    public synchronized void enviar(String linea) {
        if (!conectado()) {
            return;
        }
        byte[] datos = (limpiar(linea) + "\n").getBytes(StandardCharsets.US_ASCII);
        if (puerto.writeBytes(datos, datos.length) < 0) {
            System.err.println("Se perdio la conexion con el Arduino");
            desconectar();
        }
    }

    public void acceso(String nombre, int diasRestantes) {
        enviar("A;" + nombre + ";" + diasRestantes);
    }

    public void salida(String nombre, String duracion) {
        enviar("S;" + nombre + ";" + duracion);
    }

    public void negado(String motivo) {
        enviar("N;" + motivo);
    }

    public synchronized void desconectar() {
        if (puerto != null) {
            puerto.closePort();
            puerto = null;
        }
    }

    public synchronized boolean conectado() {
        return puerto != null && puerto.isOpen();
    }

    public synchronized String nombrePuerto() {
        return conectado() ? puerto.getSystemPortName() : "-";
    }

    /** La LCD no muestra tildes: "Matías" se envia como "Matias". */
    private static String limpiar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("[^\\x20-\\x7E]", "");
    }
}
