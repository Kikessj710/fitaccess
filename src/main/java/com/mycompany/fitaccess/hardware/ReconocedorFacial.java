package com.mycompany.fitaccess.hardware;

import com.mycompany.fitaccess.util.Config;
import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * Reconocimiento facial con LBPH. Las fotos se guardan en rostros/&lt;idCliente&gt;/1.png, 2.png...
 * El numero de carpeta es la "etiqueta" que devuelve la prediccion.
 * Una sola instancia para toda la app: ReconocedorFacial.get()
 */
public final class ReconocedorFacial {

    /** Resultado de una prediccion. clienteId = -1 si no se reconoce. */
    public record Prediccion(int clienteId, double distancia) {

        public boolean reconocido() {
            return clienteId > 0;
        }
    }

    private static final ReconocedorFacial INSTANCIA = new ReconocedorFacial();

    private LBPHFaceRecognizer modelo;
    private boolean entrenado;
    private final File carpeta = new File(Config.get("rostros.carpeta", "rostros"));
    private final File archivoModelo = new File(carpeta, "modelo_lbph.xml");

    private ReconocedorFacial() {
    }

    public static ReconocedorFacial get() {
        return INSTANCIA;
    }

    /** Carga el modelo guardado; si no existe, lo entrena. Se llama al abrir la app. */
    public synchronized void cargar() {
        try {
            if (archivoModelo.exists()) {
                modelo = LBPHFaceRecognizer.create();
                modelo.read(archivoModelo.getAbsolutePath());
                entrenado = true;
                System.out.println("Modelo facial cargado");
            } else {
                entrenar();
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el modelo: " + e.getMessage());
            entrenar();
        }
    }

    /** Entrena con todas las carpetas de rostros/. Devuelve cuantas personas aprendio. */
    public synchronized int entrenar() {
        List<Mat> imagenes = new ArrayList<>();
        List<Integer> etiquetas = new ArrayList<>();
        File[] personas = carpeta.listFiles(File::isDirectory);
        if (personas != null) {
            for (File persona : personas) {
                int id;
                try {
                    id = Integer.parseInt(persona.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                File[] fotos = persona.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
                if (fotos == null) {
                    continue;
                }
                for (File foto : fotos) {
                    Mat m = imread(foto.getAbsolutePath(), IMREAD_GRAYSCALE);
                    if (!m.empty()) {
                        imagenes.add(m);
                        etiquetas.add(id);
                    }
                }
            }
        }
        if (imagenes.isEmpty()) {
            entrenado = false;
            System.out.println("No hay rostros registrados todavia");
            return 0;
        }
        MatVector vector = new MatVector(imagenes.size());
        Mat labels = new Mat(imagenes.size(), 1, CV_32SC1);
        IntBuffer buffer = labels.createBuffer();
        for (int i = 0; i < imagenes.size(); i++) {
            vector.put(i, imagenes.get(i));
            buffer.put(i, etiquetas.get(i));
        }
        modelo = LBPHFaceRecognizer.create(1, 8, 8, 8, Double.MAX_VALUE);
        modelo.train(vector, labels);
        carpeta.mkdirs();
        modelo.save(archivoModelo.getAbsolutePath());
        entrenado = true;
        int personasUnicas = (int) etiquetas.stream().distinct().count();
        System.out.println("Modelo entrenado con " + personasUnicas + " personas");
        return personasUnicas;
    }

    /**
     * Distancia LBPH: mientras MAS BAJA, mas parecido. Si supera el umbral de
     * config.properties, se considera "no reconocido" (mejor negar que dejar entrar a otro).
     */
    public synchronized Prediccion predecir(Mat rostroGris) {
        if (!entrenado || rostroGris == null || rostroGris.empty()) {
            return new Prediccion(-1, Double.MAX_VALUE);
        }
        IntPointer etiqueta = new IntPointer(1);
        DoublePointer distancia = new DoublePointer(1);
        modelo.predict(rostroGris, etiqueta, distancia);
        double d = distancia.get(0);
        int id = etiqueta.get(0);
        double umbral = Config.getDouble("reconocimiento.umbral", 70);
        return new Prediccion(d <= umbral ? id : -1, d);
    }

    /** Guarda las fotos capturadas en rostros/&lt;clienteId&gt;/ */
    public void guardarRostros(int clienteId, List<Mat> rostros) {
        File dir = new File(carpeta, String.valueOf(clienteId));
        dir.mkdirs();
        for (int i = 0; i < rostros.size(); i++) {
            imwrite(new File(dir, (i + 1) + ".png").getAbsolutePath(), rostros.get(i));
        }
    }

    /** Borra las fotos del cliente (derecho a eliminar sus datos biometricos). */
    public void borrarRostros(int clienteId) {
        File dir = new File(carpeta, String.valueOf(clienteId));
        File[] fotos = dir.listFiles();
        if (fotos != null) {
            for (File f : fotos) {
                f.delete();
            }
        }
        dir.delete();
    }

    /** Primera foto del cliente (para mostrarla en su pantalla), o null. */
    public File foto(int clienteId) {
        File f = new File(new File(carpeta, String.valueOf(clienteId)), "1.png");
        return f.exists() ? f : null;
    }

    public boolean estaEntrenado() {
        return entrenado;
    }
}
