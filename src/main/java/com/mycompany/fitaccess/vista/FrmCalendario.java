package com.mycompany.fitaccess.vista;


import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.util.Config;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Calendario del mes: verde = asistio, rojo = no asistio, gris = cerrado o futuro. */
public class FrmCalendario extends JFrame {

    private final Cliente cliente;
    private YearMonth mes = YearMonth.now();
    private final JPanel cuadricula = new JPanel(new GridLayout(0, 7, 6, 6));
    private final JLabel lblMes = Estilo.titulo("");
    private final JLabel lblResumen = Estilo.subtitulo(" ");

    public FrmCalendario(Cliente cliente) {
        this.cliente = cliente;
        Estilo.ventana(this, "Calendario de " + cliente.getPrimerNombre(), 760, 620);
        setLayout(new BorderLayout(0, 14));
        getRootPane().setBorder(Estilo.margen(20));

        JPanel arriba = new JPanel(new BorderLayout());
        Estilo.transparente(arriba);
        JButton btnAnterior = Estilo.botonSecundario("<  Anterior");
        JButton btnSiguiente = Estilo.botonSecundario("Siguiente  >");
        lblMes.setHorizontalAlignment(SwingConstants.CENTER);
        arriba.add(btnAnterior, BorderLayout.WEST);
        arriba.add(lblMes, BorderLayout.CENTER);
        arriba.add(btnSiguiente, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        Estilo.transparente(cuadricula);
        add(cuadricula, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        Estilo.transparente(abajo);
        abajo.add(lblResumen, BorderLayout.WEST);
        JLabel leyenda = Estilo.etiqueta("<html><font color='#22C55E'>&#9632;</font> Asistio &nbsp; "
                + "<font color='#EF4444'>&#9632;</font> No asistio &nbsp; "
                + "<font color='#555B66'>&#9632;</font> Cerrado / futuro</html>");
        abajo.add(leyenda, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        btnAnterior.addActionListener(e -> { mes = mes.minusMonths(1); dibujar(); });
        btnSiguiente.addActionListener(e -> { mes = mes.plusMonths(1); dibujar(); });
        dibujar();
    }

    private void dibujar() {
        Locale es = Locale.forLanguageTag("es-EC");
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, es);
        lblMes.setText(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + mes.getYear());

        cuadricula.removeAll();
        for (String d : new String[]{"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"}) {
            JLabel l = Estilo.etiqueta(d);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            cuadricula.add(l);
        }
        LocalDate primero = mes.atDay(1);
        for (int i = 1; i < primero.getDayOfWeek().getValue(); i++) {
            cuadricula.add(new JLabel());   // espacios antes del dia 1
        }

        Set<LocalDate> asistidos;
        try {
            asistidos = new AsistenciaDAOImpl().fechasAsistidas(cliente.getId(), primero, mes.atEndOfMonth());
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
            return;
        }
          LocalDate hoy = LocalDate.now();

LocalDate fechaRegistro = cliente.getFechaRegistro() == null
        ? hoy
        : cliente.getFechaRegistro().toLocalDate();

int habiles = 0;
int vino = 0;

for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
    LocalDate f = mes.atDay(dia);
    Color color;

    boolean antesDelRegistro = f.isBefore(fechaRegistro);
    boolean futuro = f.isAfter(hoy);
    boolean cerrado = !Config.abre(f);

    if (antesDelRegistro || futuro || cerrado) {
        // La persona todavía no pertenecía al gimnasio,
        // el día aún no llega o el gimnasio estaba cerrado.
        color = new Color(0x2A2E37);
    } else if (asistidos.contains(f)) {
        color = Estilo.VERDE;
    } else if (f.equals(hoy)) {
        // Hoy todavía puede asistir.
        color = Estilo.PANEL;
    } else {
        // Solo se marca como falta después del registro.
        color = Estilo.ROJO;
    }

    if (!antesDelRegistro
            && Config.abre(f)
            && !f.isAfter(hoy)) {
        habiles++;

        if (asistidos.contains(f)) {
            vino++;
        }
    }

      
            JLabel celda = new JLabel(String.valueOf(dia), SwingConstants.CENTER);
            celda.setOpaque(true);
            celda.setBackground(color);
            celda.setForeground(Color.WHITE);
            celda.setFont(new Font(Estilo.FUENTE, f.equals(hoy) ? Font.BOLD : Font.PLAIN, 16));
            if (f.equals(hoy)) {
                celda.setBorder(javax.swing.BorderFactory.createLineBorder(Estilo.ACENTO, 2));
            }
            cuadricula.add(celda);
        }
        int pct = habiles == 0 ? 0 : vino * 100 / habiles;
        lblResumen.setText("Asistio " + vino + " de " + habiles + " dias (" + pct + "%)");
        cuadricula.revalidate();
        cuadricula.repaint();
    }
}

