package com.mycompany.fitaccess.reportes;

import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.servicio.ConfiguracionService;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/** Llena los .jrxml con datos de MySQL y los muestra o exporta a PDF. */
public class GeneradorReportes {

    public enum Tipo {
        ASISTENCIA("Asistencia mensual por cliente", "asistencia_mensual"),
        INGRESOS("Ingresos del mes con descuentos", "ingresos_mensuales");

        final String titulo;
        final String archivo;

        Tipo(String titulo, String archivo) {
            this.titulo = titulo;
            this.archivo = archivo;
        }

        @Override
        public String toString() {
            return titulo;
        }
    }

    private JasperPrint llenar(Tipo tipo, int mes, int anio) throws Exception {
        try (InputStream jrxml = getClass().getResourceAsStream("/reportes/" + tipo.archivo + ".jrxml");
             Connection cn = Conexion.getConexion()) {
            if (jrxml == null) {
                throw new IllegalStateException("No se encontro " + tipo.archivo + ".jrxml");
            }
            JasperReport reporte = JasperCompileManager.compileReport(jrxml);
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("MES", mes);
            parametros.put("ANIO", anio);
            parametros.put("NOMBRE_GIMNASIO", ConfiguracionService.obtener().getNombre());
            parametros.put("DIRECCION_GIMNASIO", ConfiguracionService.obtener().getDireccion());
            parametros.put("TELEFONO_GIMNASIO", ConfiguracionService.obtener().getTelefono());
            parametros.put("CORREO_GIMNASIO", ConfiguracionService.obtener().getCorreo());
            parametros.put("LOGO_GIMNASIO", ConfiguracionService.obtener().getLogoRuta());
            return JasperFillManager.fillReport(reporte, parametros, cn);
        }
    }

    /** Abre el visor de JasperReports (permite imprimir y guardar). */
    public void ver(Tipo tipo, int mes, int anio) throws Exception {
        JasperViewer.viewReport(llenar(tipo, mes, anio), false);
    }

    /** Guarda el PDF en la carpeta reportes_generados/ y devuelve el archivo. */
    public File exportarPdf(Tipo tipo, int mes, int anio) throws Exception {
        File carpeta = new File("reportes_generados");
        carpeta.mkdirs();
        File pdf = new File(carpeta, String.format("%s_%d_%02d.pdf", tipo.archivo, anio, mes));
        JasperExportManager.exportReportToPdfFile(llenar(tipo, mes, anio), pdf.getAbsolutePath());
        return pdf;
    }
}
