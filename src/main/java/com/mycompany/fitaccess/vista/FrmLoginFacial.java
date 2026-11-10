package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.hardware.ArduinoPuerto;
import com.mycompany.fitaccess.hardware.CamaraFacial;
import com.mycompany.fitaccess.hardware.ReconocedorFacial;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.servicio.AccesoService;
import com.mycompany.fitaccess.util.Config;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.bytedeco.opencv.opencv_core.Mat;

/** Login del cliente: vista previa y reconocimiento facial con alternativa por cédula. */
public class FrmLoginFacial extends JFrame {

    private final CamaraFacial camara = new CamaraFacial();
    private final AccesoService acceso = new AccesoService();
    private final ArduinoPuerto arduino = ArduinoPuerto.get();
    private final Map<Integer, String> nombres = new HashMap<>();
    private final AtomicBoolean procesando = new AtomicBoolean();
    private final JLabel lblCamara = new JLabel("Iniciando cámara...", SwingConstants.CENTER);
    private final JLabel lblEstado = new JLabel("Vista previa lista", SwingConstants.CENTER);
    private final JLabel lblDetalle = Estilo.subtitulo("Presione Iniciar reconocimiento para comenzar");
    private final JLabel lblHardware = Estilo.etiqueta("Comprobando Arduino...");
    private final JPanel tarjetaEstado = Estilo.tarjeta();
    private final JButton btnReconocer = Estilo.boton("Iniciar reconocimiento");
    private final JButton btnCedula = Estilo.botonSecundario("Ingresar con cédula (plan B)");
    private final int cuadrosNecesarios = Math.max(1, Config.getInt("reconocimiento.cuadros", 4));
    private final Timer timerHardware = new Timer(2000, e -> actualizarEstadoArduino());

    private int candidato = -1;
    private int conteo;
    private int desconocidos;
    private volatile long pausadoHasta;
    private volatile boolean reconocimientoActivo;
    private volatile boolean camaraLista;

    public FrmLoginFacial() {
        Estilo.ventana(this, "Acceso de cliente", 1150, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(20, 0));
        getRootPane().setBorder(Estilo.margen(20));

        lblCamara.setOpaque(true);
        lblCamara.setBackground(Estilo.PANEL);
        lblCamara.setForeground(Estilo.TEXTO_SUAVE);
        add(lblCamara, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new BorderLayout(0, 16));
        Estilo.transparente(derecha);
        derecha.setPreferredSize(new Dimension(350, 0));

        JPanel cabecera = new JPanel(new GridLayout(3, 1, 0, 5));
        Estilo.transparente(cabecera);
        cabecera.add(Estilo.logo(30));
        cabecera.add(Estilo.subtitulo("Acceso con reconocimiento facial"));
        cabecera.add(lblHardware);
        derecha.add(cabecera, BorderLayout.NORTH);

        tarjetaEstado.setLayout(new BoxLayout(tarjetaEstado, BoxLayout.Y_AXIS));
        lblEstado.setFont(new Font(Estilo.FUENTE, Font.BOLD, 24));
        lblEstado.setForeground(Estilo.TEXTO);
        lblEstado.setAlignmentX(CENTER_ALIGNMENT);
        lblDetalle.setAlignmentX(CENTER_ALIGNMENT);
        tarjetaEstado.add(lblEstado);
        tarjetaEstado.add(lblDetalle);
        derecha.add(tarjetaEstado, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(3, 1, 0, 10));
        Estilo.transparente(botones);
        JButton btnVolver = Estilo.botonSecundario("Volver al inicio");
        botones.add(btnReconocer);
        botones.add(btnCedula);
        botones.add(btnVolver);
        derecha.add(botones, BorderLayout.SOUTH);
        add(derecha, BorderLayout.EAST);

        btnReconocer.setEnabled(false);
        btnReconocer.addActionListener(e -> alternarReconocimiento());
        btnCedula.addActionListener(e -> ingresarConCedula());
        btnVolver.addActionListener(e -> volverAlInicio());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                reconocimientoActivo = false;
                timerHardware.stop();
                camara.detener();
            }
        });

        if (!ReconocedorFacial.get().estaEntrenado()) {
            mostrar("No hay rostros registrados", "Registre un cliente antes de iniciar sesión.", Estilo.AMARILLO);
        }
        actualizarEstadoArduino();
        timerHardware.start();
        iniciarCamara();
    }

    /** La cámara abre en segundo plano para que la interfaz no se congele. */
    private void iniciarCamara() {
        Thread hilo = new Thread(() -> {
            try {
                camara.iniciar(this::alRecibirCuadro);
                camaraLista = true;
                SwingUtilities.invokeLater(() -> {
                    btnReconocer.setEnabled(true);
                    mostrar("Cámara lista", "Presione Iniciar reconocimiento cuando esté frente a la cámara.", Estilo.VERDE);
                });
            } catch (Exception ex) {
                camaraLista = false;
                SwingUtilities.invokeLater(() -> {
                    lblCamara.setText("<html><center>No se pudo abrir la cámara.<br>" + textoHtml(ex.getMessage())
                            + "<br>Puede usar el ingreso por cédula.</center></html>");
                    mostrar("Cámara no disponible", "Revise la cámara o use el plan alternativo por cédula.", Estilo.ROJO);
                });
            }
        }, "inicio-camara-login");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void alternarReconocimiento() {
        if (!camaraLista) {
            mostrar("Cámara no disponible", "Use el ingreso con cédula o revise la conexión de la cámara.", Estilo.ROJO);
            return;
        }
        reconocimientoActivo = !reconocimientoActivo;
        candidato = -1;
        conteo = 0;
        desconocidos = 0;
        btnReconocer.setText(reconocimientoActivo ? "Detener reconocimiento" : "Iniciar reconocimiento");
        if (reconocimientoActivo) {
            mostrar("Reconocimiento activo", "Mire de frente a la cámara.", Estilo.VERDE);
        } else {
            mostrar("Reconocimiento pausado", "La cámara continúa mostrando la vista previa.", Estilo.AMARILLO);
        }
    }

    /** Se invoca desde el hilo de la cámara; las actualizaciones Swing se envían al EDT. */
    private void alRecibirCuadro(BufferedImage imagen, Mat rostro, Rectangle zona) {
        String etiqueta = null;
        if (reconocimientoActivo && System.currentTimeMillis() > pausadoHasta && !procesando.get() && rostro != null) {
            ReconocedorFacial.Prediccion prediccion = ReconocedorFacial.get().predecir(rostro);
            if (prediccion.reconocido()) {
                desconocidos = 0;
                conteo = prediccion.clienteId() == candidato ? conteo + 1 : 1;
                candidato = prediccion.clienteId();
                etiqueta = nombre(candidato);
                if (conteo >= cuadrosNecesarios) {
                    conteo = 0;
                    procesarEnSegundoPlano(candidato);
                }
            } else {
                candidato = -1;
                conteo = 0;
                etiqueta = "?";
                if (++desconocidos >= 25) {
                    desconocidos = 0;
                    arduino.negado("DESCONOCIDO");
                    pausar(3000);
                    SwingUtilities.invokeLater(() -> mostrar("Rostro no reconocido",
                            "Si eres nuevo, regístrate desde la pantalla de inicio.", Estilo.ROJO));
                }
            }
        } else if (rostro == null || !reconocimientoActivo) {
            conteo = 0;
            desconocidos = 0;
        }

        if (zona != null && etiqueta != null) {
            dibujarNombre(imagen, zona, etiqueta);
        }
        SwingUtilities.invokeLater(() -> {
            lblCamara.setText(null);
            lblCamara.setIcon(Estilo.icono(imagen, lblCamara.getWidth(), lblCamara.getHeight()));
        });
    }

    private void procesarEnSegundoPlano(int clienteId) {
        if (!procesando.compareAndSet(false, true)) {
            return;
        }
        Thread hilo = new Thread(() -> {
            try {
                AccesoService.Resultado resultado = acceso.procesar(clienteId);
                mostrarResultado(resultado);
            } catch (RuntimeException ex) {
                pausar(3000);
                SwingUtilities.invokeLater(() -> mostrar("No se pudo procesar el acceso",
                        ex.getMessage() == null ? "Revise la conexión con la base de datos." : ex.getMessage(), Estilo.ROJO));
            } finally {
                procesando.set(false);
            }
        }, "procesar-acceso-cliente");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResultado(AccesoService.Resultado resultado) {
        switch (resultado.tipo) {
            case ENTRADA -> {
                arduino.acceso(resultado.cliente.getPrimerNombre(), resultado.diasRestantes);
                pausar(60_000);
                SwingUtilities.invokeLater(() -> {
                    mostrar("Acceso autorizado", resultado.mensaje, Estilo.VERDE);
                    Timer abrirPantalla = new Timer(1200, e -> {
                        new FrmPrincipalCliente(resultado.cliente).setVisible(true);
                        dispose();
                    });
                    abrirPantalla.setRepeats(false);
                    abrirPantalla.start();
                });
            }
            case SALIDA -> {
                arduino.salida(resultado.cliente.getPrimerNombre(), AccesoService.formatoCorto(resultado.minutos));
                pausar(4000);
                SwingUtilities.invokeLater(() -> mostrar("Salida registrada",
                        "Entrenaste " + AccesoService.formatoDuracion(resultado.minutos), Estilo.ACENTO));
            }
            case VENCIDO -> {
                arduino.negado("VENCIDO");
                pausar(4000);
                SwingUtilities.invokeLater(() -> mostrar("Suscripción vencida",
                        resultado.cliente.getPrimerNombre() + ", renueva en recepción.", Estilo.ROJO));
            }
            case ESPERA -> {
                pausar(3000);
                SwingUtilities.invokeLater(() -> mostrar("Espera un momento", resultado.mensaje, Estilo.AMARILLO));
            }
            default -> {
                arduino.negado("DESCONOCIDO");
                pausar(3000);
                SwingUtilities.invokeLater(() -> mostrar("Rostro no reconocido", resultado.mensaje, Estilo.ROJO));
            }
        }
    }

    /** Plan alternativo para clientes registrados cuando falla la cámara o el reconocimiento. */
    private void ingresarConCedula() {
        String cedula = JOptionPane.showInputDialog(this, "Ingrese su número de cédula:");
        if (cedula == null || cedula.isBlank()) {
            return;
        }
        if (!procesando.compareAndSet(false, true)) {
            mostrar("Espere un momento", "Ya se está procesando un acceso.", Estilo.AMARILLO);
            return;
        }
        Thread hilo = new Thread(() -> {
            try {
                Cliente cliente = new ClienteDAOImpl().buscarPorCedula(cedula.trim());
                if (cliente == null || !cliente.isActivo()) {
                    arduino.negado("DESCONOCIDO");
                    SwingUtilities.invokeLater(() -> mostrar("Cédula no registrada", "Verifique el número ingresado.", Estilo.ROJO));
                } else {
                    mostrarResultado(acceso.procesar(cliente.getId()));
                }
            } catch (RuntimeException ex) {
                SwingUtilities.invokeLater(() -> Estilo.error(this,
                        ex.getMessage() == null ? "No se pudo consultar la base de datos." : ex.getMessage()));
            } finally {
                procesando.set(false);
            }
        }, "acceso-por-cedula");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void actualizarEstadoArduino() {
        if (arduino.conectado()) {
            lblHardware.setText("Arduino conectado: " + arduino.nombrePuerto());
            lblHardware.setForeground(Estilo.VERDE);
        } else {
            lblHardware.setText("Arduino desconectado · modo software activo");
            lblHardware.setForeground(Estilo.AMARILLO);
        }
    }

    private void mostrar(String titulo, String detalle, Color color) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> mostrar(titulo, detalle, color));
            return;
        }
        lblEstado.setText("<html><center>" + textoHtml(titulo) + "</center></html>");
        lblEstado.setForeground(color);
        lblDetalle.setText("<html><center>" + textoHtml(detalle) + "</center></html>");
        tarjetaEstado.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(color, 2), Estilo.margen(16)));
        Timer limpiar = new Timer(5000, e -> {
            if (System.currentTimeMillis() > pausadoHasta && reconocimientoActivo) {
                lblEstado.setText("Mire a la cámara");
                lblEstado.setForeground(Estilo.TEXTO);
                lblDetalle.setText("El sistema buscará un rostro registrado.");
                tarjetaEstado.setBorder(Estilo.margen(16));
            }
        });
        limpiar.setRepeats(false);
        limpiar.start();
    }

    private void pausar(long milisegundos) {
        pausadoHasta = System.currentTimeMillis() + milisegundos;
        candidato = -1;
        conteo = 0;
    }

    private String nombre(int clienteId) {
        return nombres.computeIfAbsent(clienteId, id -> {
            try {
                Cliente cliente = new ClienteDAOImpl().buscarPorId(id);
                return cliente == null ? "?" : cliente.getPrimerNombre();
            } catch (RuntimeException ex) {
                return "?";
            }
        });
    }

    private void dibujarNombre(BufferedImage imagen, Rectangle zona, String texto) {
        Graphics2D g = imagen.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font(Estilo.FUENTE, Font.BOLD, 22));
            int ancho = g.getFontMetrics().stringWidth(texto) + 16;
            int y = Math.max(0, zona.y - 34);
            Color color = "?".equals(texto) ? Estilo.ROJO : Estilo.VERDE;
            g.setColor(color);
            g.fillRoundRect(zona.x, y, ancho, 30, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(texto, zona.x + 8, y + 22);
            if ("?".equals(texto)) {
                g.setStroke(new BasicStroke(3));
                g.setColor(Estilo.ROJO);
                g.drawRect(zona.x, zona.y, zona.width, zona.height);
            }
        } finally {
            g.dispose();
        }
    }

    private void volverAlInicio() {
        new FrmInicio().setVisible(true);
        dispose();
    }

    private static String textoHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
