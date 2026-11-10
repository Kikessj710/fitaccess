package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.MedidaDAOImpl;
import com.mycompany.fitaccess.dao.impl.PlanDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.hardware.ArduinoPuerto;
import com.mycompany.fitaccess.hardware.ReconocedorFacial;
import com.mycompany.fitaccess.modelo.Asistencia;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Medida;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.servicio.AccesoService;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/** Resumen personal del cliente que acaba de ingresar al gimnasio. */
public class FrmPrincipalCliente extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Cliente cliente;
    private final AccesoService acceso = new AccesoService();

    public FrmPrincipalCliente(Cliente cliente) {
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
        Estilo.ventana(this, cliente.getNombreCompleto(), 1050, 660);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(0, 20));
        getRootPane().setBorder(Estilo.margen(24));
        add(cabecera(), BorderLayout.NORTH);
        add(datos(), BorderLayout.CENTER);
        add(botones(), BorderLayout.SOUTH);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                volverAlAcceso();
            }
        });
    }

    private JPanel cabecera() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        Estilo.transparente(panel);
        JLabel foto = new JLabel("", SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(120, 120));
        foto.setOpaque(true);
        foto.setBackground(Estilo.PANEL);
        File archivo = ReconocedorFacial.get().foto(cliente.getId());
        try {
            if (archivo != null) {
                BufferedImage imagen = ImageIO.read(archivo);
                if (imagen != null) {
                    foto.setIcon(Estilo.icono(imagen, 120, 120));
                }
            }
        } catch (Exception ignorado) {
            // La pantalla sigue funcionando aunque no haya una foto disponible.
        }
        panel.add(foto, BorderLayout.WEST);

        JPanel textos = new JPanel(new GridLayout(4, 1, 0, 4));
        Estilo.transparente(textos);
        textos.add(Estilo.titulo("Hola, " + cliente.getNombreCompleto()));

        Suscripcion suscripcion = new SuscripcionDAOImpl().buscarUltima(cliente.getId());
        Plan plan = suscripcion == null ? null : new PlanDAOImpl().buscarPorId(suscripcion.getPlanId());
        String nombrePlan = plan == null ? "Sin plan registrado" : "Plan " + plan.getNombre();
        textos.add(Estilo.subtitulo(nombrePlan + " · " + estadoSuscripcion(suscripcion)));
        textos.add(Estilo.subtitulo("Inicio: " + fecha(suscripcion == null ? null : suscripcion.getFechaInicio())
                + "     |     Vencimiento: " + fecha(suscripcion == null ? null : suscripcion.getFechaFin())));

        Asistencia abierta = new AsistenciaDAOImpl().buscarAbierta(cliente.getId());
        JLabel estadoDentro = Estilo.subtitulo(abierta != null
                ? "Dentro del gimnasio desde las " + abierta.getHoraEntrada().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "No hay una entrada activa registrada");
        estadoDentro.setForeground(abierta != null ? Estilo.VERDE : Estilo.TEXTO_SUAVE);
        textos.add(estadoDentro);
        panel.add(textos, BorderLayout.CENTER);
        return panel;
    }

    private JPanel datos() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 16, 16));
        Estilo.transparente(panel);

        Suscripcion suscripcion = new SuscripcionDAOImpl().buscarVigente(cliente.getId());
        int dias = suscripcion == null ? 0 : suscripcion.diasRestantes();
        int racha = acceso.calcularRacha(cliente.getId());
        int minutosHoy = new AsistenciaDAOImpl().minutosDelDia(cliente.getId(), LocalDate.now());
        Medida medida = new MedidaDAOImpl().ultima(cliente.getId());
        double peso = medida == null ? 0 : medida.getPesoKg();
        double imc = peso > 0 && cliente.getAlturaCm() > 0
                ? Medida.imc(peso, cliente.getAlturaCm()) : 0;

        panel.add(Estilo.dato(String.valueOf(dias), "Días restantes", dias <= 5 ? Estilo.AMARILLO : Estilo.VERDE));
        panel.add(Estilo.dato(racha + (racha == 1 ? " día" : " días"), "Racha de asistencia", Estilo.ACENTO));
        panel.add(Estilo.dato(AccesoService.formatoDuracion(minutosHoy), "Entrenado hoy", Estilo.TEXTO));
        panel.add(Estilo.dato(cliente.getEdad() + " años", "Edad", Estilo.TEXTO));
        panel.add(Estilo.dato(peso > 0 ? String.format("%.1f kg", peso) : "Sin registro", "Peso actual", Estilo.TEXTO));
        panel.add(Estilo.dato(imc > 0 ? String.format("%.1f", imc) : "—",
                imc > 0 ? "IMC · " + Medida.categoriaImc(imc) : "IMC no disponible", Estilo.TEXTO));
        return panel;
    }

    private JPanel botones() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        Estilo.transparente(panel);
        JLabel estadoArduino = Estilo.etiqueta(ArduinoPuerto.get().conectado()
                ? "Arduino conectado" : "Arduino desconectado · modo software activo");
        estadoArduino.setForeground(ArduinoPuerto.get().conectado() ? Estilo.VERDE : Estilo.AMARILLO);
        panel.add(estadoArduino, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        Estilo.transparente(acciones);
        JButton btnVolver = Estilo.botonSecundario("Volver al acceso");
        JButton btnCalendario = Estilo.botonSecundario("Mi calendario");
        JButton btnProgreso = Estilo.botonSecundario("Mi progreso");
        JButton btnSalida = Estilo.boton("Registrar salida");
        acciones.add(btnVolver);
        acciones.add(btnCalendario);
        acciones.add(btnProgreso);
        acciones.add(btnSalida);
        panel.add(acciones, BorderLayout.EAST);

        btnVolver.addActionListener(e -> volverAlAcceso());
        btnCalendario.addActionListener(e -> new FrmCalendario(cliente).setVisible(true));
        btnProgreso.addActionListener(e -> new FrmProgreso(cliente).setVisible(true));
        btnSalida.addActionListener(e -> registrarSalida());
        return panel;
    }

    private void registrarSalida() {
        try {
            AccesoService.Resultado resultado = acceso.registrarSalida(cliente.getId());
            if (resultado.tipo == AccesoService.Tipo.SALIDA) {
                ArduinoPuerto.get().salida(cliente.getPrimerNombre(), AccesoService.formatoCorto(resultado.minutos));
                Estilo.info(this, "Hasta pronto, " + cliente.getPrimerNombre() + ".\nEntrenaste "
                        + AccesoService.formatoDuracion(resultado.minutos));
                volverAlAcceso();
            } else {
                Estilo.info(this, resultado.mensaje);
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage() == null
                    ? "No se pudo registrar la salida. Revise la conexión con la base de datos." : ex.getMessage());
        }
    }

    private String estadoSuscripcion(Suscripcion suscripcion) {
        if (suscripcion == null) {
            return "Sin suscripción";
        }
        if (suscripcion.estaVigente()) {
            return "Suscripción vigente";
        }
        return "Suscripción vencida o inactiva";
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "No registrada" : fecha.format(FORMATO_FECHA);
    }

    private void volverAlAcceso() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::volverAlAcceso);
            return;
        }
        if (isDisplayable()) {
            new FrmLoginFacial().setVisible(true);
            dispose();
        }
    }
}
