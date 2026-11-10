package com.mycompany.fitaccess.hardware;

import java.io.File;
import java.util.List;
import org.bytedeco.opencv.opencv_core.Mat;

/** ESQUELETO: Miki reemplaza este archivo completo con el codigo real. */
public final class ReconocedorFacial {

    public record Prediccion(int clienteId, double distancia) {
        public boolean reconocido() {
            return clienteId > 0;
        }
    }

    private static final ReconocedorFacial INSTANCIA = new ReconocedorFacial();

    public static ReconocedorFacial get() {
        return INSTANCIA;
    }

    public void cargar() {
    }

    public int entrenar() {
        return 0;
    }

    public Prediccion predecir(Mat rostroGris) {
        return new Prediccion(-1, Double.MAX_VALUE);
    }

    public void guardarRostros(int clienteId, List<Mat> rostros) {
    }

    public void borrarRostros(int clienteId) {
    }

    public File foto(int clienteId) {
        return null;
    }

    public boolean estaEntrenado() {
        return false;
    }
}
