package com.mycompany.fitaccess.modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.time.DayOfWeek;

/** Datos comerciales y horario configurable del gimnasio. */
public class ConfiguracionGym {
    private int id = 1;
    private String nombre = "FitAccess Gym";
    private String direccion = "";
    private String telefono = "";
    private String correo = "";
    private String logoRuta = "";
    private String mensajeBienvenida = "Bienvenido a FitAccess";
    private LocalTime horaApertura = LocalTime.of(6, 0);
    private LocalTime horaCierre = LocalTime.of(22, 0);
    private Set<DayOfWeek> diasAtencion = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
    private Set<LocalDate> feriados = Collections.emptySet();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getLogoRuta() { return logoRuta; }
    public void setLogoRuta(String logoRuta) { this.logoRuta = logoRuta; }
    public String getMensajeBienvenida() { return mensajeBienvenida; }
    public void setMensajeBienvenida(String mensajeBienvenida) { this.mensajeBienvenida = mensajeBienvenida; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }
    public Set<DayOfWeek> getDiasAtencion() { return diasAtencion; }
    public void setDiasAtencion(Set<DayOfWeek> diasAtencion) { this.diasAtencion = diasAtencion; }
    public Set<LocalDate> getFeriados() { return feriados; }
    public void setFeriados(Set<LocalDate> feriados) { this.feriados = feriados; }

    public boolean atiende(LocalDate fecha) {
        return diasAtencion.contains(fecha.getDayOfWeek()) && !feriados.contains(fecha);
    }
}
