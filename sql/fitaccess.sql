-- =========================================================
--  FitAccess - Base de datos (MySQL / MariaDB de XAMPP)
--  Ejecutar completo en phpMyAdmin > pestana SQL
-- =========================================================
DROP DATABASE IF EXISTS fitaccess;
CREATE DATABASE fitaccess CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE fitaccess;

CREATE TABLE plan (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(50)   NOT NULL,
    dias    INT           NOT NULL,
    precio  DECIMAL(8,2)  NOT NULL,
    activo  BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    cedula           CHAR(10)     NOT NULL UNIQUE,
    nombres          VARCHAR(60)  NOT NULL,
    apellidos        VARCHAR(60)  NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    telefono         VARCHAR(15),
    correo           VARCHAR(100),
    altura_cm        DECIMAL(5,1) NOT NULL,
    consentimiento   BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE suscripcion (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id   INT  NOT NULL,
    plan_id      INT  NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin    DATE NOT NULL,
    estado       VARCHAR(12) NOT NULL DEFAULT 'ACTIVA',   -- ACTIVA / CONGELADA / CANCELADA
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    FOREIGN KEY (plan_id)    REFERENCES plan(id)
);

CREATE TABLE asistencia (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id        INT      NOT NULL,
    fecha             DATE     NOT NULL,
    hora_entrada      DATETIME NOT NULL,
    hora_salida       DATETIME NULL,
    duracion_minutos  INT      NULL,
    cierre_automatico BOOLEAN  NOT NULL DEFAULT FALSE,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    INDEX idx_asistencia_fecha (fecha),
    INDEX idx_asistencia_cliente (cliente_id, fecha)
);

CREATE TABLE medida (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT          NOT NULL,
    fecha      DATE         NOT NULL,
    peso_kg    DECIMAL(5,1) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE pago (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id           INT          NOT NULL,
    suscripcion_id       INT          NOT NULL,
    fecha                DATETIME     NOT NULL,
    monto_base           DECIMAL(8,2) NOT NULL,
    porcentaje_descuento DECIMAL(5,2) NOT NULL DEFAULT 0,
    monto_final          DECIMAL(8,2) NOT NULL,
    metodo               VARCHAR(20)  NOT NULL,
    FOREIGN KEY (cliente_id)     REFERENCES cliente(id),
    FOREIGN KEY (suscripcion_id) REFERENCES suscripcion(id)
);

CREATE TABLE administrador (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    usuario    VARCHAR(30)  NOT NULL UNIQUE,
    clave_hash VARCHAR(100) NOT NULL,
    nombre     VARCHAR(60)  NOT NULL
);

-- Planes iniciales
INSERT INTO plan (nombre, dias, precio) VALUES
('Diario', 1, 2.00),
('Mensual', 30, 25.00),
('Trimestral', 90, 65.00);

-- El administrador por defecto (admin / admin123) lo crea la aplicacion
-- automaticamente la primera vez que se abre, con la clave cifrada.
