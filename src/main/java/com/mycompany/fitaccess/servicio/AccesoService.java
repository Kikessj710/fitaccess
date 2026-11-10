package com.mycompany.fitaccess.servicio;

import com.mycompany.fitaccess.dao.AsistenciaDAO;
import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.dao.SuscripcionDAO;
import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Asistencia;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Config;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Reglas centrales de entrada, salida, suscripción y asistencia. */
public class AccesoService {

    public enum Tipo { ENTRADA, SALIDA, VENCIDO, NO_RECONOCIDO, ESPERA }

    /** Resultado que consume la interfaz para mostrar mensajes y accionar el Arduino. */
    public static final class Resultado {
        public final Tipo tipo;
        public final Cliente cliente;
        public final String mensaje;
        public final int diasRestantes;
        public final int minutos;

        public Resultado(Tipo tipo, Cliente cliente, String mensaje, int diasRestantes, int minutos) {
            this.tipo = tipo;
            this.cliente = cliente;
            this.mensaje = mensaje;
            this.diasRestantes = diasRestantes;
            this.minutos = minutos;
        }
    }

    /* Evita entradas dobles incluso cuando dos ventanas usan instancias distintas del servicio. */
    private static final Object CERROJO_ACCESO = new Object();
    private static final AtomicBoolean CIERRE_INICIADO = new AtomicBoolean();
    private static final ScheduledExecutorService PROGRAMADOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "fitaccess-cierre-automatico");
        t.setDaemon(true);
        return t;
    });

    private final ClienteDAO clienteDAO;
    private final SuscripcionDAO suscripcionDAO;
    private final AsistenciaDAO asistenciaDAO;

    public AccesoService() {
        this(new ClienteDAOImpl(), new SuscripcionDAOImpl(), new AsistenciaDAOImpl());
    }

    /** Constructor inyectable para pruebas y para reutilizar implementaciones alternativas de DAO. */
    public AccesoService(ClienteDAO clienteDAO, SuscripcionDAO suscripcionDAO, AsistenciaDAO asistenciaDAO) {
        this.clienteDAO = clienteDAO;
        this.suscripcionDAO = suscripcionDAO;
        this.asistenciaDAO = asistenciaDAO;
    }

    /**
     * Alterna entrada/salida según exista una asistencia abierta. El ID debe provenir del
     * reconocedor facial o del plan alternativo de cédula; el servicio vuelve a cargar al cliente.
     */
    public Resultado procesar(int clienteId) {
        synchronized (CERROJO_ACCESO) {
            Cliente cliente = clienteDAO.buscarPorId(clienteId);
            if (cliente == null || !cliente.isActivo()) {
                return resultado(Tipo.NO_RECONOCIDO, null, "No encontramos un cliente activo con ese rostro", 0, 0);
            }

            LocalDateTime ahora = LocalDateTime.now();
            int pausaMinutos = Math.max(0, Config.getInt("acceso.pausa.minutos", 2));

            // Si ya está dentro, no convertir una lectura repetida inmediata en una salida.
            Asistencia abierta = asistenciaDAO.buscarAbierta(cliente.getId());
            if (abierta != null) {
                long minutosDentro = Duration.between(abierta.getHoraEntrada(), ahora).toMinutes();
                if (minutosDentro < pausaMinutos) {
                    return resultado(Tipo.ESPERA, cliente,
                            "La entrada ya fue registrada. Espera antes de volver a presentar tu rostro.", 0, 0);
                }
                return cerrar(cliente, abierta, ahora, false);
            }

            // También evita registrar otra entrada inmediatamente después de marcar la salida.
            Asistencia ultima = asistenciaDAO.buscarUltima(cliente.getId());
            if (ultima != null && ultima.getHoraSalida() != null
                    && Duration.between(ultima.getHoraSalida(), ahora).toMinutes() < pausaMinutos) {
                return resultado(Tipo.ESPERA, cliente,
                        "La salida ya fue registrada. Espera un momento antes de ingresar de nuevo.", 0, 0);
            }

            Suscripcion suscripcion = suscripcionDAO.buscarVigente(cliente.getId());
            if (suscripcion == null) {
                return resultado(Tipo.VENCIDO, cliente, "No tienes una suscripción vigente. Renueva en recepción.", 0, 0);
            }

            Asistencia asistencia = new Asistencia();
            asistencia.setClienteId(cliente.getId());
            asistencia.setFecha(ahora.toLocalDate());
            asistencia.setHoraEntrada(ahora);
            asistenciaDAO.insertar(asistencia);

            int dias = suscripcion.diasRestantes();
            return resultado(Tipo.ENTRADA, cliente,
                    "Entrada registrada. Te quedan " + dias + (dias == 1 ? " día de suscripción." : " días de suscripción."),
                    dias, 0);
        }
    }

    /** Registra la salida solicitada desde la pantalla principal del cliente. */
    public Resultado registrarSalida(int clienteId) {
        synchronized (CERROJO_ACCESO) {
            Cliente cliente = clienteDAO.buscarPorId(clienteId);
            if (cliente == null || !cliente.isActivo()) {
                return resultado(Tipo.NO_RECONOCIDO, null, "No encontramos un cliente activo.", 0, 0);
            }
            Asistencia abierta = asistenciaDAO.buscarAbierta(clienteId);
            if (abierta == null) {
                return resultado(Tipo.ESPERA, cliente, "No tienes una entrada pendiente de salida.", 0, 0);
            }
            return cerrar(cliente, abierta, LocalDateTime.now(), false);
        }
    }

    private Resultado cerrar(Cliente cliente, Asistencia asistencia, LocalDateTime salida, boolean automatico) {
        int minutos = (int) Math.max(0, Duration.between(asistencia.getHoraEntrada(), salida).toMinutes());
        asistenciaDAO.registrarSalida(asistencia.getId(), salida, minutos, automatico);
        return resultado(Tipo.SALIDA, cliente,
                "Salida registrada. Tiempo de entrenamiento: " + formatoDuracion(minutos) + ".", 0, minutos);
    }

    /** Cierra asistencias olvidadas cuando ya pasó la hora de cierre del gimnasio. */
    public int cerrarOlvidados() {
        synchronized (CERROJO_ACCESO) {
            int cerradas = 0;
            LocalDateTime ahora = LocalDateTime.now();
            for (Asistencia asistencia : asistenciaDAO.listarAbiertas()) {
                LocalDateTime cierreDelDia = asistencia.getFecha().atTime(Config.horaCierre());
                if (ahora.isAfter(cierreDelDia)) {
                    LocalDateTime salida = cierreDelDia.isBefore(asistencia.getHoraEntrada())
                            ? asistencia.getHoraEntrada() : cierreDelDia;
                    int minutos = (int) Math.max(0,
                            Duration.between(asistencia.getHoraEntrada(), salida).toMinutes());
                    asistenciaDAO.registrarSalida(asistencia.getId(), salida, minutos, true);
                    cerradas++;
                }
            }
            if (cerradas > 0) {
                System.out.println("Cierre automático: " + cerradas + " salidas registradas");
            }
            return cerradas;
        }
    }

    /** Inicia una única tarea en segundo plano que revisa el cierre cada cinco minutos. */
    public void iniciarCierreAutomatico() {
        if (CIERRE_INICIADO.compareAndSet(false, true)) {
            PROGRAMADOR.scheduleAtFixedRate(() -> {
                try {
                    cerrarOlvidados();
                } catch (RuntimeException e) {
                    System.err.println("No se pudieron cerrar asistencias olvidadas: " + e.getMessage());
                }
            }, 0, 5, TimeUnit.MINUTES);
        }
    }

    /** Calcula los días consecutivos de asistencia; los días cerrados no rompen la racha. */
    public int calcularRacha(int clienteId) {
        LocalDate hoy = LocalDate.now();
        Set<LocalDate> asistidos = asistenciaDAO.fechasAsistidas(clienteId, hoy.minusDays(366), hoy);
        LocalDate dia = asistidos.contains(hoy) ? hoy : hoy.minusDays(1);
        int racha = 0;
        for (int i = 0; i < 366; i++, dia = dia.minusDays(1)) {
            if (!Config.abre(dia)) {
                continue;
            }
            if (asistidos.contains(dia)) {
                racha++;
            } else {
                break;
            }
        }
        return racha;
    }

    /** 95 minutos -> "1 h 35 min". */
    public static String formatoDuracion(int minutos) {
        int valor = Math.max(0, minutos);
        return valor >= 60 ? (valor / 60) + " h " + (valor % 60) + " min" : valor + " min";
    }

    /** Formato compacto para la pantalla LCD del Arduino: 95 -> "1h35". */
    public static String formatoCorto(int minutos) {
        int valor = Math.max(0, minutos);
        return (valor / 60) + "h" + String.format("%02d", valor % 60);
    }

    private static Resultado resultado(Tipo tipo, Cliente cliente, String mensaje, int dias, int minutos) {
        return new Resultado(tipo, cliente, mensaje, dias, minutos);
    }
}
