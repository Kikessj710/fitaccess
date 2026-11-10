package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.MedidaDAOImpl;
import com.mycompany.fitaccess.dao.impl.PagoDAOImpl;
import com.mycompany.fitaccess.dao.impl.PlanDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.hardware.CamaraFacial;
import com.mycompany.fitaccess.hardware.ReconocedorFacial;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Medida;
import com.mycompany.fitaccess.modelo.Pago;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Config;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Validaciones;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.bytedeco.opencv.opencv_core.Mat;

/** Registro de un cliente nuevo: datos, plan y captura de 20 fotos del rostro. */
public class FrmRegistro extends JFrame {

    private final JTextField txtCedula = new JTextField();
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JSpinner spnNacimiento;
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtPeso = new JTextField();
    private final JTextField txtAltura = new JTextField();
    private final JComboBox<Plan> cmbPlan = new JComboBox<>();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"Efectivo", "Transferencia", "Tarjeta"});
    private final JCheckBox chkConsentimiento = new JCheckBox(
            "<html>Acepto que mi rostro se use solo para el control de acceso<br>"
            + "(Ley Organica de Proteccion de Datos Personales)</html>");

    private final JLabel lblCamara = new JLabel("Iniciando camara...", SwingConstants.CENTER);
    private final JLabel lblAyuda = Estilo.etiqueta("Mire a la camara con buena luz frontal");
    private final JProgressBar barra = new JProgressBar();
    private final JButton btnCapturar = Estilo.boton("Capturar rostro");
    private final JButton btnGuardar = Estilo.boton("Guardar registro");

    private final CamaraFacial camara = new CamaraFacial();
    private final List<Mat> rostros = Collections.synchronizedList(new ArrayList<>());
    private final int totalFotos = Config.getInt("rostros.cantidad", 20);
    private volatile boolean capturando;
    private long ultimaFoto;

    public FrmRegistro() {
        Estilo.ventana(this, "Registro de cliente", 1150, 720);
        setLayout(new BorderLayout(20, 0));
        getRootPane().setBorder(Estilo.margen(20));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -20);
        spnNacimiento = new JSpinner(new SpinnerDateModel(c.getTime(), null, new Date(), Calendar.DAY_OF_MONTH));
        spnNacimiento.setEditor(new JSpinner.DateEditor(spnNacimiento, "dd/MM/yyyy"));

        add(formulario(), BorderLayout.WEST);
        add(panelCamara(), BorderLayout.CENTER);

        try {
            new PlanDAOImpl().listarActivos().forEach(cmbPlan::addItem);
            if (cmbPlan.getItemCount() > 1) {
                cmbPlan.setSelectedIndex(1); // Mensual por defecto
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }

        barra.setMaximum(totalFotos);
        barra.setStringPainted(true);
        barra.setString("0 / " + totalFotos + " fotos");
        btnGuardar.setEnabled(false);

        btnCapturar.addActionListener(e -> iniciarCaptura());
        btnGuardar.addActionListener(e -> guardar());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                camara.detener();
            }
        });
        encenderCamara();
    }

    private JPanel formulario() {
        JPanel p = Estilo.tarjeta();
        p.setLayout(new GridBagLayout());
        p.setPreferredSize(new Dimension(430, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 4, 4, 4);
        g.weightx = 1;
        g.gridx = 0;
        g.gridwidth = 2;
        int y = 0;
        g.gridy = y++;
        p.add(Estilo.titulo("Nuevo cliente"), g);
        y = fila(p, g, y, "Cedula", txtCedula);
        g.gridwidth = 1;
        g.gridy = y++;
        p.add(Estilo.etiqueta("Nombres"), g);
        g.gridx = 1;
        p.add(Estilo.etiqueta("Apellidos"), g);
        g.gridx = 0;
        g.gridy = y++;
        p.add(txtNombres, g);
        g.gridx = 1;
        p.add(txtApellidos, g);
        g.gridx = 0;
        g.gridwidth = 2;
        y = fila(p, g, y, "Fecha de nacimiento", spnNacimiento);
        y = fila(p, g, y, "Celular (09XXXXXXXX)", txtTelefono);
        y = fila(p, g, y, "Correo", txtCorreo);
        g.gridwidth = 1;
        g.gridy = y++;
        p.add(Estilo.etiqueta("Peso (kg)"), g);
        g.gridx = 1;
        p.add(Estilo.etiqueta("Altura (cm)"), g);
        g.gridx = 0;
        g.gridy = y++;
        p.add(txtPeso, g);
        g.gridx = 1;
        p.add(txtAltura, g);
        g.gridx = 0;
        g.gridwidth = 2;
        y = fila(p, g, y, "Plan", cmbPlan);
        y = fila(p, g, y, "Metodo de pago", cmbMetodo);
        g.gridy = y++;
        p.add(chkConsentimiento, g);
        g.gridy = y;
        g.weighty = 1;
        p.add(new JLabel(), g); // empuja todo hacia arriba
        return p;
    }

    private int fila(JPanel p, GridBagConstraints g, int y, String texto, JComponent campo) {
        g.gridy = y++;
        p.add(Estilo.etiqueta(texto), g);
        g.gridy = y++;
        p.add(campo, g);
        return y;
    }

    private JPanel panelCamara() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        Estilo.transparente(p);
        lblCamara.setOpaque(true);
        lblCamara.setBackground(Estilo.PANEL);
        lblCamara.setForeground(Estilo.TEXTO_SUAVE);
        p.add(lblCamara, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new GridLayout(0, 1, 0, 8));
        Estilo.transparente(abajo);
        abajo.add(lblAyuda);
        abajo.add(barra);
        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        Estilo.transparente(botones);
        botones.add(btnCapturar);
        botones.add(btnGuardar);
        abajo.add(botones);
        p.add(abajo, BorderLayout.SOUTH);
        return p;
    }

    /** La camara tarda unos segundos en abrir: se hace fuera del hilo de la ventana. */
    private void encenderCamara() {
        new Thread(() -> {
            try {
                camara.iniciar(this::alRecibirCuadro);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblCamara.setText(
                        "<html><center>No se pudo abrir la camara<br>" + ex.getMessage()
                        + "<br>Revise DroidCam y camara.indice en config.properties</center></html>"));
            }
        }).start();
    }

    /** Se ejecuta en el hilo de la camara por cada cuadro. */
    private void alRecibirCuadro(java.awt.image.BufferedImage imagen, Mat rostro, java.awt.Rectangle zona) {
        if (capturando && rostro != null && System.currentTimeMillis() - ultimaFoto > 150) {
            rostros.add(rostro);
            ultimaFoto = System.currentTimeMillis();
            if (rostros.size() >= totalFotos) {
                capturando = false;
            }
        }
        int n = rostros.size();
        SwingUtilities.invokeLater(() -> {
            lblCamara.setText(null);
            lblCamara.setIcon(Estilo.icono(imagen, lblCamara.getWidth(), lblCamara.getHeight()));
            barra.setValue(n);
            barra.setString(n + " / " + totalFotos + " fotos");
            if (n >= totalFotos) {
                lblAyuda.setText("Listo. Revise los datos y presione Guardar registro");
                lblAyuda.setForeground(Estilo.VERDE);
                btnGuardar.setEnabled(true);
                btnCapturar.setText("Repetir captura");
                btnCapturar.setEnabled(true);
            } else if (capturando) {
                lblAyuda.setText(rostro == null ? "No se ve el rostro: acerquese y mire a la camara"
                        : "Gire un poco la cabeza a los lados y sonria a veces...");
            }
        });
    }

    private void iniciarCaptura() {
        if (!chkConsentimiento.isSelected()) {
            Estilo.error(this, "El cliente debe aceptar el uso de su rostro para continuar");
            return;
        }
        if (!camara.estaActiva()) {
            Estilo.error(this, "La camara no esta encendida");
            return;
        }
        rostros.clear();
        btnGuardar.setEnabled(false);
        btnCapturar.setEnabled(false);
        lblAyuda.setForeground(Estilo.TEXTO_SUAVE);
        capturando = true;
    }

    /** Valida el formulario. Devuelve el mensaje de error o null si todo esta bien. */
    private String validar() {
        if (!Validaciones.cedulaValida(txtCedula.getText().trim())) return "Cedula ecuatoriana invalida";
        if (!Validaciones.soloLetras(txtNombres.getText())) return "Nombres: solo letras";
        if (!Validaciones.soloLetras(txtApellidos.getText())) return "Apellidos: solo letras";
        if (fechaNacimiento().isAfter(LocalDate.now().minusYears(12))) return "Edad minima: 12 anos";
        if (!Validaciones.telefonoValido(txtTelefono.getText())) return "Celular invalido (09XXXXXXXX)";
        if (!txtCorreo.getText().isBlank() && !Validaciones.correoValido(txtCorreo.getText())) return "Correo invalido";
        if (!Validaciones.enRango(Validaciones.decimal(txtPeso.getText()), 30, 250)) return "Peso entre 30 y 250 kg";
        if (!Validaciones.enRango(Validaciones.decimal(txtAltura.getText()), 100, 230)) return "Altura entre 100 y 230 cm";
        if (cmbPlan.getSelectedItem() == null) return "Elija un plan";
        if (!chkConsentimiento.isSelected()) return "Falta el consentimiento de uso del rostro";
        if (rostros.size() < totalFotos) return "Primero capture el rostro";
        return null;
    }

    private LocalDate fechaNacimiento() {
        return ((Date) spnNacimiento.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void guardar() {
        String error = validar();
        if (error != null) {
            Estilo.error(this, error);
            return;
        }
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        if (clienteDAO.buscarPorCedula(txtCedula.getText().trim()) != null) {
            Estilo.error(this, "Ya existe un cliente con esa cedula");
            return;
        }
        Cliente c = new Cliente();
        c.setCedula(txtCedula.getText().trim());
        c.setNombres(txtNombres.getText().trim());
        c.setApellidos(txtApellidos.getText().trim());
        c.setFechaNacimiento(fechaNacimiento());
        c.setTelefono(txtTelefono.getText().trim());
        c.setCorreo(txtCorreo.getText().trim());
        c.setAlturaCm(Validaciones.decimal(txtAltura.getText()));
        c.setConsentimiento(true);
        double peso = Validaciones.decimal(txtPeso.getText());
        Plan plan = (Plan) cmbPlan.getSelectedItem();
        String metodo = (String) cmbMetodo.getSelectedItem();
        List<Mat> fotos = new ArrayList<>(rostros);

        btnGuardar.setEnabled(false);
        btnCapturar.setEnabled(false);
        lblAyuda.setText("Guardando y entrenando el reconocimiento...");
        camara.detener();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                int id = clienteDAO.insertar(c);
                new MedidaDAOImpl().insertar(new Medida(id, LocalDate.now(), peso));

                Suscripcion s = new Suscripcion();
                s.setClienteId(id);
                s.setPlanId(plan.getId());
                s.setFechaInicio(LocalDate.now());
                s.setFechaFin(LocalDate.now().plusDays(plan.getDias() - 1L));
                new SuscripcionDAOImpl().insertar(s);

                Pago p = new Pago();
                p.setClienteId(id);
                p.setSuscripcionId(s.getId());
                p.setFecha(LocalDateTime.now());
                p.setMontoBase(plan.getPrecio());
                p.setPorcentajeDescuento(0);
                p.setMontoFinal(plan.getPrecio());
                p.setMetodo(metodo);
                new PagoDAOImpl().insertar(p);

                ReconocedorFacial.get().guardarRostros(id, fotos);
                ReconocedorFacial.get().entrenar();   // reentrenar con la persona nueva
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Estilo.info(FrmRegistro.this, "Bienvenido " + c.getPrimerNombre()
                            + ". Ya puedes entrar con tu rostro.");
                    dispose();
                } catch (Exception ex) {
                    Estilo.error(FrmRegistro.this, "Error al guardar: " + ex.getMessage());
                    btnGuardar.setEnabled(true);
                }
            }
        }.execute();
    }
}
