package com.mycompany.fitaccess.servicio;


import com.mycompany.fitaccess.dao.AsistenciaDAO;
import com.mycompany.fitaccess.dao.PagoDAO;
import com.mycompany.fitaccess.dao.SuscripcionDAO;
import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.PagoDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.ConfiguracionGym;
import com.mycompany.fitaccess.modelo.Pago;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Config;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Premia la constancia: al renovar se mira la asistencia del periodo anterior.
 *   100% de los dias que abrio el gimnasio  -> 5% de descuento
 *   80% a 99%                               -> 2% de descuento
 *   menos de 80%                            -> sin descuento
 */
public class DescuentoService {

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAOImpl();
    private final SuscripcionDAO suscripcionDAO = new SuscripcionDAOImpl();
    private final PagoDAO pagoDAO = new PagoDAOImpl();

    /** Regla pura (sin base de datos), facil de probar con JUnit. */
    public static double porcentajePorAsistencia(double porcentajeAsistencia) {
        if (porcentajeAsistencia >= 100) {
            return 5;
        }
        if (porcentajeAsistencia >= 80) {
            return 2;
        }
        return 0;
    }

    /** Cuenta los dias que abre el gimnasio entre dos fechas (inclusive). */
    public static int diasHabiles(LocalDate desde, LocalDate hasta) {
        int n = 0;
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            if (Config.abre(d)) {
                n++;
            }
        }
        return n;
    }

    /**
     * % de asistencia en la suscripcion actual (o en la ultima, si ya vencio),
     * contando hasta hoy si aun no termina.
     */
    public double porcentajeAsistencia(int clienteId) {
        Suscripcion s = suscripcionDAO.buscarVigente(clienteId);
        if (s == null) {
            s = suscripcionDAO.buscarUltima(clienteId);
        }
        if (s != null && s.getFechaInicio().isAfter(LocalDate.now())) {
            return 0;   // ya renovo por adelantado: el nuevo periodo aun no empieza
        }
        if (s == null) {
            return 0;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = s.getFechaFin().isAfter(hoy) ? hoy : s.getFechaFin();
        Set<LocalDate> asistidos = asistenciaDAO.fechasAsistidas(clienteId, s.getFechaInicio(), hasta);
        if (hasta.equals(hoy) && !asistidos.contains(hoy)) {
            hasta = hoy.minusDays(1);   // hoy todavia puede venir: no cuenta como falta
        }
        int habiles = diasHabiles(s.getFechaInicio(), hasta);
        if (habiles == 0) {
            return 0;
        }
        long validos = asistidos.stream().filter(Config::abre).count();
        return Math.min(100, validos * 100.0 / habiles);
    }

    public double descuentoPara(int clienteId) {
        return porcentajePorAsistencia(porcentajeAsistencia(clienteId));
    }

    /**
     * Renueva: crea la nueva suscripcion (empieza al terminar la actual, o hoy si ya vencio)
     * y registra el pago con el descuento aplicado.
     */
    public Pago renovar(Cliente cliente, Plan plan, String metodo) {
        double descuento = descuentoPara(cliente.getId());
        Suscripcion ultima = suscripcionDAO.buscarUltima(cliente.getId());
        LocalDate inicio = LocalDate.now();
        if (ultima != null && "ACTIVA".equals(ultima.getEstado()) && !ultima.getFechaFin().isBefore(inicio)) {
            inicio = ultima.getFechaFin().plusDays(1);
        }
        Suscripcion nueva = new Suscripcion();
        nueva.setClienteId(cliente.getId());
        nueva.setPlanId(plan.getId());
        nueva.setFechaInicio(inicio);
        nueva.setFechaFin(inicio.plusDays(plan.getDias() - 1L));
        suscripcionDAO.insertar(nueva);

        Pago p = new Pago();
        p.setClienteId(cliente.getId());
        p.setSuscripcionId(nueva.getId());
        p.setFecha(LocalDateTime.now());
        p.setMontoBase(plan.getPrecio());
        p.setPorcentajeDescuento(descuento);
        p.setMontoFinal(Math.round(plan.getPrecio() * (100 - descuento)) / 100.0);
        p.setMetodo(metodo);
        pagoDAO.insertar(p);
        return p;
    }

    /** Texto del recibo con el descuento visible. */
    public String recibo(Pago p, Cliente c, Plan plan, Suscripcion s) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("              ").append(ConfiguracionService.nombreGimnasio()).append('\n');
        sb.append("           Recibo de pago N. ").append(p.getId()).append('\n');
        sb.append("========================================\n");
        sb.append("Fecha:    ").append(p.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        ConfiguracionGym gym = ConfiguracionService.obtener();
        if (!gym.getDireccion().isBlank()) sb.append("Direccion: ").append(gym.getDireccion()).append('\n');
        if (!gym.getTelefono().isBlank()) sb.append("Telefono:  ").append(gym.getTelefono()).append('\n');
        if (!gym.getCorreo().isBlank()) sb.append("Correo:    ").append(gym.getCorreo()).append('\n');
        sb.append("Cliente:  ").append(c.getNombreCompleto()).append('\n');
        sb.append("Cedula:   ").append(c.getCedula()).append('\n');
        sb.append("Plan:     ").append(plan.getNombre()).append(" (").append(plan.getDias()).append(" dias)\n");
        if (s != null) {
            sb.append("Vigencia: ").append(s.getFechaInicio().format(f)).append(" al ")
                    .append(s.getFechaFin().format(f)).append('\n');
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal:                 $ %8.2f%n", p.getMontoBase()));
        sb.append(String.format("Descuento constancia %3.0f%%: -$ %7.2f%n", p.getPorcentajeDescuento(),
                p.getMontoBase() - p.getMontoFinal()));
        sb.append(String.format("TOTAL:                    $ %8.2f%n", p.getMontoFinal()));
        sb.append("Metodo:   ").append(p.getMetodo()).append('\n');
        sb.append("========================================\n");
        sb.append(p.getPorcentajeDescuento() > 0 ? "  Gracias por tu constancia!\n" : "  Gracias por entrenar con nosotros\n");
        return sb.toString();
    }
}
