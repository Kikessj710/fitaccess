package com.mycompany.fitaccess.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Colores, fuentes y componentes comunes: todas las ventanas se ven iguales. */
public final class Estilo {

    public static final Color FONDO = new Color(0x111318);
    public static final Color PANEL = new Color(0x1B1E25);
    public static final Color BORDE = new Color(0x2A2E37);
    public static final Color ACENTO = new Color(0xFF7A1A);
    public static final Color TEXTO = new Color(0xECEDEF);
    public static final Color TEXTO_SUAVE = new Color(0x9AA0AA);
    public static final Color VERDE = new Color(0x22C55E);
    public static final Color ROJO = new Color(0xEF4444);
    public static final Color AMARILLO = new Color(0xF5B700);

    public static final String FUENTE = "Segoe UI";

    private Estilo() {
    }

    /** Se llama una sola vez al iniciar la app (en App.main). */
    public static void aplicarTema() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#FF7A1A", "@background", "#111318"));
        FlatDarkLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("defaultFont", new Font(FUENTE, Font.PLAIN, 14));
    }

    /** Configura tamano, centro de pantalla y fondo de cualquier ventana. */
    public static void ventana(JFrame f, String titulo, int ancho, int alto) {
        f.setTitle("FitAccess - " + titulo);
        f.setSize(ancho, alto);
        f.setMinimumSize(new Dimension(Math.min(ancho, 800), Math.min(alto, 520)));
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().setBackground(FONDO);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FUENTE, Font.BOLD, 26));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FUENTE, Font.PLAIN, 15));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    /** Logo de texto "FIT ACCESS". */
    public static JLabel logo(int tamano) {
        JLabel l = new JLabel("<html><span style='color:#ECEDEF'>FIT</span>"
                + "<span style='color:#FF7A1A'>ACCESS</span></html>");
        l.setFont(new Font(FUENTE, Font.BOLD, tamano));
        return l;
    }

    /** Boton principal (color de acento). */
    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(ACENTO);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(FUENTE, Font.BOLD, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setPreferredSize(new Dimension(220, 44));
        return b;
    }

    /** Boton secundario (gris). */
    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font(FUENTE, Font.PLAIN, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 38));
        return b;
    }

    /** Panel "tarjeta" con fondo y borde redondeado. */
    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(margen(16));
        p.putClientProperty("FlatLaf.style", "arc: 16");
        return p;
    }

    /** Tarjeta pequena con un numero grande y una descripcion (para tableros). */
    public static JPanel dato(String valor, String descripcion, Color color) {
        JPanel p = tarjeta();
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        JLabel v = new JLabel(valor);
        v.setFont(new Font(FUENTE, Font.BOLD, 28));
        v.setForeground(color);
        JLabel d = etiqueta(descripcion);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(v);
        p.add(d);
        p.setName(descripcion);
        return p;
    }

    /** Escala una imagen para que quepa en ancho x alto sin deformarse. */
    public static ImageIcon icono(BufferedImage img, int ancho, int alto) {
        if (img == null || ancho <= 0 || alto <= 0) {
            return null;
        }
        double escala = Math.min((double) ancho / img.getWidth(), (double) alto / img.getHeight());
        int w = Math.max(1, (int) (img.getWidth() * escala));
        int h = Math.max(1, (int) (img.getHeight() * escala));
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_FAST));
    }

    public static Border margen(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    public static void transparente(JComponent c) {
        c.setOpaque(false);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "FitAccess", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "FitAccess", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String pregunta) {
        return JOptionPane.showConfirmDialog(padre, pregunta, "FitAccess",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
