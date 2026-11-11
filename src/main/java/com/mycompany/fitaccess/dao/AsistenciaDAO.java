package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Asistencia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AsistenciaDAO {

    /** Registra una entrada y devuelve el id generado. */
    int insertar(Asistencia a);

    void registrarSalida(int id, LocalDateTime salida, int minutos, boolean automatico);

    /** Entrada sin salida del cliente, o null. */
    Asistencia buscarAbierta(int clienteId);

    /** Ultimo registro del cliente, o null. */
    Asistencia buscarUltima(int clienteId);

    /** Todas las entradas sin salida (quien esta dentro ahora). */
    List<Asistencia> listarAbiertas();

    List<Asistencia> listarPorFecha(LocalDate fecha);

    List<Asistencia> listarPorCliente(int clienteId, LocalDate desde, LocalDate hasta);

    /** Dias distintos en los que el cliente asistio entre dos fechas. */
    Set<LocalDate> fechasAsistidas(int clienteId, LocalDate desde, LocalDate hasta);

    /** Minutos entrenados por el cliente en un dia (cuenta la entrada abierta hasta ahora). */
    int minutosDelDia(int clienteId, LocalDate fecha);

    /** Cantidad de entradas por hora del dia (0..23) en un mes, para la grafica de horas pico. */
    int[] entradasPorHora(int mes, int anio);
}
