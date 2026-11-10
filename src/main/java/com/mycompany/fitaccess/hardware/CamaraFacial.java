package com.mycompany.fitaccess.hardware;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.opencv.opencv_core.Mat;

/** ESQUELETO: Miki reemplaza este archivo completo con el codigo real. */
public class CamaraFacial {

    public static final int TAMANO_ROSTRO = 200;

    public interface Oyente {
        void cuadro(BufferedImage imagen, Mat rostro, Rectangle zona);
    }

    public static String rutaCascade() {
        return new File("recursos/haarcascade_frontalface_default.xml").getAbsolutePath();
    }

    public void iniciar(Oyente oyente) throws Exception {
        throw new UnsupportedOperationException("Camara pendiente (Miki)");
    }

    public void detener() {
    }

    public boolean estaActiva() {
        return false;
    }
}
