-- Migración no destructiva para FitAccess existente.
-- Ejecutar una sola vez sobre la base de datos fitaccess.

CREATE TABLE IF NOT EXISTS configuracion_gimnasio (
    id                 TINYINT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    direccion          VARCHAR(180) NOT NULL DEFAULT '',
    telefono           VARCHAR(30)  NOT NULL DEFAULT '',
    correo             VARCHAR(100) NOT NULL DEFAULT '',
    logo_ruta          VARCHAR(255) NOT NULL DEFAULT '',
    mensaje_bienvenida VARCHAR(180) NOT NULL DEFAULT 'Bienvenido a FitAccess',
    hora_apertura      CHAR(5) NOT NULL DEFAULT '06:00',
    hora_cierre        CHAR(5) NOT NULL DEFAULT '22:00',
    dias_atencion      VARCHAR(20) NOT NULL DEFAULT '1,2,3,4,5,6',
    feriados           TEXT NOT NULL,
    actualizado_en     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO configuracion_gimnasio
    (id, nombre, direccion, telefono, correo, logo_ruta, mensaje_bienvenida,
     hora_apertura, hora_cierre, dias_atencion, feriados)
SELECT 1, 'FitAccess Gym', '', '', '', '', 'Bienvenido a FitAccess',
       '06:00', '22:00', '1,2,3,4,5,6', ''
WHERE NOT EXISTS (SELECT 1 FROM configuracion_gimnasio WHERE id = 1);
