package com.fitaccess;

import com.mycompany.fitaccess.servicio.AccesoService;
import com.mycompany.fitaccess.servicio.DescuentoService;
import com.mycompany.fitaccess.util.Validaciones;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de las reglas de negocio. Clic derecho en el proyecto > Test. */
class ReglasTest {

    @Test
    void descuentoPorAsistencia() {
        assertEquals(5, DescuentoService.porcentajePorAsistencia(100));
        assertEquals(2, DescuentoService.porcentajePorAsistencia(85));
        assertEquals(2, DescuentoService.porcentajePorAsistencia(80));
        assertEquals(0, DescuentoService.porcentajePorAsistencia(79.9));
    }

    @Test
    void cedulaEcuatoriana() {
        assertTrue(Validaciones.cedulaValida("1710034065"));
        assertFalse(Validaciones.cedulaValida("1710034066"));   // digito verificador malo
        assertFalse(Validaciones.cedulaValida("9910034065"));   // provincia 99 no existe
        assertFalse(Validaciones.cedulaValida("12345"));
    }

    @Test
    void correoYTelefono() {
        assertTrue(Validaciones.correoValido("sebas@uta.edu.ec"));
        assertFalse(Validaciones.correoValido("sebas@"));
        assertTrue(Validaciones.telefonoValido("0991234567"));
        assertFalse(Validaciones.telefonoValido("0891234567"));
    }

    @Test
    void formatoDuracion() {
        assertEquals("1 h 35 min", AccesoService.formatoDuracion(95));
        assertEquals("40 min", AccesoService.formatoDuracion(40));
        assertEquals("1h35", AccesoService.formatoCorto(95));
    }
}
