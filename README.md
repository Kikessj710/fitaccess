# FitAccess

Sistema inteligente de control de acceso y gestion para gimnasios.
Proyecto en Java (Maven + NetBeans) con reconocimiento facial (OpenCV/LBPH),
Arduino por puerto serial, MySQL (XAMPP), JasperReports y control de versiones con Git.

## Equipo

| Integrante | Responsabilidad |
|---|---|
| Enrique (lider) | Base del proyecto, base de datos, modelos, DAO, configuracion de camara y Arduino |
| Miki | Camara y reconocimiento facial, registro de clientes |
| Sebastian | Arduino (circuito, sketch, comunicacion serial) y panel del administrador |
| Matias | Entrada y salida (reglas), login facial y pantalla del cliente |
| Benjamin | Calendario, progreso de peso y asistencia del administrador |
| Gabo | Descuentos, pagos, reportes PDF, ventana de versiones y pruebas |

## Como ejecutar

1. XAMPP: Start en Apache y MySQL.
2. phpMyAdmin > SQL: ejecutar `sql/fitaccess.sql` (y opcional `sql/datos_prueba.sql`).
3. Abrir el proyecto en NetBeans y presionar Run (clase principal `com.fitaccess.App`).
4. Usuario administrador inicial: `admin` / `admin123`.

La configuracion (camara, umbral, puerto del Arduino, hora de cierre) esta en `config.properties`.

## Estructura

```
src/main/java/com/fitaccess
 ├── modelo      Cliente, Plan, Suscripcion, Asistencia, Medida, Pago, Admin
 ├── dao         interfaces DAO
 │   └── impl    implementaciones JDBC
 ├── servicio    AccesoService (entrada/salida), DescuentoService
 ├── hardware    CamaraFacial, ReconocedorFacial, ArduinoPuerto
 ├── vista       ventanas Swing
 ├── reportes    GeneradorReportes (+ .jrxml en resources/reportes)
 └── util        Config, Conexion, Validaciones, Hash, Estilo
arduino/fitaccess/fitaccess.ino   sketch del Arduino
sql/                              scripts de la base de datos
```

## Privacidad

Las fotos de los rostros se guardan solo en la carpeta `rostros/` del computador
(no se suben a GitHub) y se borran al eliminar al cliente.
