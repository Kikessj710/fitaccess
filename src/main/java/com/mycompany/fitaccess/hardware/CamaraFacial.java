package com.mycompany.fitaccess.hardware;

import com.mycompany.fitaccess.util.Config;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.LINE_8;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * Abre la camara (DroidCam aparece como una webcam normal), detecta el rostro con
 * Haar y entrega cada cuadro a la ventana. Corre en su propio hilo: la ventana NO se congela.
 */
public class CamaraFacial {

    /** Tamano estandar de los rostros (registro y reconocimiento usan el mismo). */
    public static final int TAMANO_ROSTRO = 200;

    /** La ventana implementa esto para recibir cada cuadro. Se llama desde el hilo de la camara. */
    public interface Oyente {

        /**
         * @param imagen imagen a color con el recuadro dibujado (lista para mostrar)
         * @param rostro rostro en gris de 200x200, o null si no hay nadie
         * @param zona   donde esta el rostro en la imagen, o null
         */
        void cuadro(BufferedImage imagen, Mat rostro, Rectangle zona);
    }

    private OpenCVFrameGrabber camara;
    private CascadeClassifier detector;
    private final OpenCVFrameConverter.ToMat aMat = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter aImagen = new Java2DFrameConverter();
    private volatile boolean activa;
    private Thread hilo;

    /** Ruta del clasificador Haar (carpeta recursos del proyecto). */
    public static String rutaCascade() {
        return new File("recursos/haarcascade_frontalface_default.xml").getAbsolutePath();
    }

    public void iniciar(Oyente oyente) throws Exception {
        detector = new CascadeClassifier(rutaCascade());
        if (detector.empty()) {
            throw new IllegalStateException("No se encontro " + rutaCascade());
        }
        camara = new OpenCVFrameGrabber(Config.getInt("camara.indice", 0));
        camara.setImageWidth(640);
        camara.setImageHeight(480);
        camara.start();
        activa = true;
        hilo = new Thread(() -> bucle(oyente), "camara-fitaccess");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void bucle(Oyente oyente) {
        Mat gris = new Mat();
        RectVector caras = new RectVector();
        Size minimo = new Size(110, 110);
        Size maximo = new Size();
        Size tamano = new Size(TAMANO_ROSTRO, TAMANO_ROSTRO);
        Scalar verde = new Scalar(94, 197, 34, 0);   // BGR
        while (activa) {
            try {
                Frame frame = camara.grab();
                if (frame == null) {
                    continue;
                }
                Mat color = aMat.convert(frame);
                cvtColor(color, gris, COLOR_BGR2GRAY);
                equalizeHist(gris, gris);
                detector.detectMultiScale(gris, caras, 1.1, 5, 0, minimo, maximo);

                // Se queda con la cara mas grande (la persona mas cercana)
                Rect mayor = null;
                for (long i = 0; i < caras.size(); i++) {
                    Rect r = caras.get(i);
                    if (mayor == null || r.area() > mayor.area()) {
                        mayor = r;
                    }
                }
                Mat rostro = null;
                Rectangle zona = null;
                if (mayor != null) {
                    zona = new Rectangle(mayor.x(), mayor.y(), mayor.width(), mayor.height());
                    rostro = new Mat();
                    resize(new Mat(gris, mayor), rostro, tamano);
                    rectangle(color, mayor, verde, 3, LINE_8, 0);
                }
                BufferedImage imagen = Java2DFrameConverter.cloneBufferedImage(aImagen.convert(aMat.convert(color)));
                oyente.cuadro(imagen, rostro, zona);
            } catch (Exception e) {
                if (activa) {
                    System.err.println("Error de camara: " + e.getMessage());
                }
            }
        }
    }

    /** Siempre llamar al cerrar la ventana, o la camara queda ocupada. */
    public void detener() {
        activa = false;
        try {
            if (hilo != null) {
                hilo.join(1500);
            }
            if (camara != null) {
                camara.stop();
                camara.release();
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar la camara: " + e.getMessage());
        }
    }

    public boolean estaActiva() {
        return activa;
    }
}
