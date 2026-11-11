package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.modelo.ConfiguracionGym;
import com.mycompany.fitaccess.servicio.ConfiguracionService;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Configuración no técnica del negocio: identidad, horarios y cierres especiales. */
public class FrmConfiguracion extends JFrame {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final ConfiguracionGym configuracion = ConfiguracionService.obtener();
    private final JTextField txtNombre = new JTextField(configuracion.getNombre());
    private final JTextField txtDireccion = new JTextField(configuracion.getDireccion());
    private final JTextField txtTelefono = new JTextField(configuracion.getTelefono());
    private final JTextField txtCorreo = new JTextField(configuracion.getCorreo());
    private final JTextField txtLogo = new JTextField(configuracion.getLogoRuta());
    private final JTextArea txtMensaje = new JTextArea(configuracion.getMensajeBienvenida(), 4, 30);
    private final JTextField txtApertura = new JTextField(configuracion.getHoraApertura().format(HORA));
    private final JTextField txtCierre = new JTextField(configuracion.getHoraCierre().format(HORA));
    private final JTextArea txtFeriados = new JTextArea(4, 30);
    private final JCheckBox[] checks = new JCheckBox[7];

    public FrmConfiguracion() {
        Estilo.ventana(this, "Configuración del gimnasio", 760, 650);
        setLayout(new BorderLayout(0, 14));
        getRootPane().setBorder(Estilo.margen(22));
        add(Estilo.subtitulo("Personaliza la información que verán tus clientes y visitantes."), BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Datos del gimnasio", datos());
        pestañas.addTab("Horarios", horarios());
        add(pestañas, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        Estilo.transparente(abajo);
        JLabel ayuda = Estilo.subtitulo("Los cambios se guardan para todo el sistema.");
        JButton guardar = Estilo.boton("Guardar cambios");
        JButton cancelar = Estilo.botonSecundario("Cancelar");
        JPanel acciones = new JPanel(new GridLayout(1, 2, 10, 0));
        Estilo.transparente(acciones);
        acciones.add(cancelar);
        acciones.add(guardar);
        abajo.add(ayuda, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
        guardar.addActionListener(e -> guardar());
        cancelar.addActionListener(e -> dispose());
    }

    private JPanel datos() {
        JPanel p = tarjeta();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();
        int y = 0;
        y = fila(p, g, y, "Nombre del gimnasio", txtNombre);
        y = fila(p, g, y, "Dirección", txtDireccion);
        y = fila(p, g, y, "Teléfono", txtTelefono);
        y = fila(p, g, y, "Correo", txtCorreo);
        JPanel logo = new JPanel(new BorderLayout(8, 0));
        Estilo.transparente(logo);
        txtLogo.setEditable(false);
        JButton seleccionar = Estilo.botonSecundario("Elegir logo...");
        logo.add(txtLogo, BorderLayout.CENTER);
        logo.add(seleccionar, BorderLayout.EAST);
        seleccionar.addActionListener(e -> elegirLogo());
        y = fila(p, g, y, "Logo", logo);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        y = fila(p, g, y, "Mensaje de bienvenida", txtMensaje);
        g.gridy = y; g.weighty = 1; p.add(new JLabel(), g);
        return p;
    }

    private JPanel horarios() {
        JPanel p = tarjeta();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();
        int y = 0;
        y = fila(p, g, y, "Hora de apertura (HH:mm)", txtApertura);
        y = fila(p, g, y, "Hora de cierre (HH:mm)", txtCierre);
        g.gridy = y++;
        p.add(Estilo.etiqueta("Días de atención"), g);
        JPanel dias = new JPanel(new GridLayout(2, 4, 8, 6));
        Estilo.transparente(dias);
        String[] nombres = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < 7; i++) {
            checks[i] = new JCheckBox(nombres[i], configuracion.getDiasAtencion().contains(DayOfWeek.of(i + 1)));
            dias.add(checks[i]);
        }
        g.gridy = y++;
        p.add(dias, g);
        g.gridy = y++;
        p.add(Estilo.etiqueta("Feriados o cierres especiales"), g);
        txtFeriados.setLineWrap(true);
        txtFeriados.setWrapStyleWord(true);
        txtFeriados.setText(configuracion.getFeriados().stream().sorted().map(LocalDate::toString).reduce((a, b) -> a + ", " + b).orElse(""));
        txtFeriados.setToolTipText("Fechas separadas por coma. Ejemplo: 2026-12-25, 2026-01-01");
        txtFeriados.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        g.gridy = y++;
        p.add(txtFeriados, g);
        g.gridy = y++;
        p.add(Estilo.subtitulo("Las fechas deben usar el formato AAAA-MM-DD y se excluirán de asistencia y rachas."), g);
        g.gridy = y; g.weighty = 1; p.add(new JLabel(), g);
        return p;
    }

    private void elegirLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar logo del gimnasio");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes PNG, JPG o JPEG", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path carpeta = Path.of("recursos", "gimnasio");
            Files.createDirectories(carpeta);
            String extension = extension(chooser.getSelectedFile().getName());
            Path destino = carpeta.resolve("logo" + extension);
            Files.copy(chooser.getSelectedFile().toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            txtLogo.setText(destino.toString());
        } catch (Exception ex) {
            Estilo.error(this, "No se pudo copiar el logo: " + ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String nombre = txtNombre.getText().trim();
            if (nombre.isBlank()) throw new IllegalArgumentException("Escribe el nombre del gimnasio");
            LocalTime apertura = LocalTime.parse(txtApertura.getText().trim(), HORA);
            LocalTime cierre = LocalTime.parse(txtCierre.getText().trim(), HORA);
            if (!cierre.isAfter(apertura)) throw new IllegalArgumentException("La hora de cierre debe ser posterior a la apertura");
            Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
            for (int i = 0; i < checks.length; i++) if (checks[i].isSelected()) dias.add(DayOfWeek.of(i + 1));
            if (dias.isEmpty()) throw new IllegalArgumentException("Selecciona al menos un día de atención");
            Set<LocalDate> feriados = new HashSet<>();
            String texto = txtFeriados.getText().trim();
            if (!texto.isBlank()) for (String valor : texto.split(",")) feriados.add(LocalDate.parse(valor.trim()));
            configuracion.setNombre(nombre);
            configuracion.setDireccion(txtDireccion.getText().trim());
            configuracion.setTelefono(txtTelefono.getText().trim());
            configuracion.setCorreo(txtCorreo.getText().trim());
            configuracion.setLogoRuta(txtLogo.getText().trim());
            configuracion.setMensajeBienvenida(txtMensaje.getText().trim());
            configuracion.setHoraApertura(apertura);
            configuracion.setHoraCierre(cierre);
            configuracion.setDiasAtencion(dias);
            configuracion.setFeriados(feriados);
            ConfiguracionService.guardar(configuracion);
            Estilo.info(this, "Configuración guardada correctamente.");
            dispose();
        } catch (Exception ex) {
            Estilo.error(this, "Revisa los datos: " + ex.getMessage());
        }
    }

    private static JPanel tarjeta() {
        JPanel p = Estilo.tarjeta();
        p.setBorder(Estilo.margen(18));
        return p;
    }

    private static GridBagConstraints base() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.insets = new Insets(7, 7, 7, 7);
        return g;
    }

    private static int fila(JPanel p, GridBagConstraints g, int y, String etiqueta, java.awt.Component campo) {
        g.gridy = y++;
        p.add(Estilo.etiqueta(etiqueta), g);
        g.gridy = y++;
        p.add(campo, g);
        return y;
    }

    private static String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? ".png" : nombre.substring(punto).toLowerCase();
    }
}
