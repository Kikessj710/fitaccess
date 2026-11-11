# FitAccess

Sistema inteligente de control de acceso y gestión para gimnasios. FitAccess permite registrar clientes, reconocerlos mediante el rostro, controlar entradas y salidas, administrar suscripciones y pagos, consultar asistencias y progreso físico, generar reportes y comunicarse con un torniquete basado en Arduino.

## Equipo

| Integrante | Responsabilidades |
|---|---|
| Enrique | Líder del proyecto, base de datos, modelos, DAO, configuración, integración Git, validaciones, cámara y Arduino |
| Miki | Cámara, reconocimiento facial y registro de clientes |
| Matías | Login facial, entrada, salida y pantalla del cliente |
| Benjamín | Calendario, progreso físico, asistencia, descuentos, pagos, reportes, pruebas y documentación |

## Tecnologías utilizadas

- Java 17.
- Apache NetBeans.
- Maven.
- MySQL/MariaDB mediante XAMPP.
- Swing para la interfaz gráfica.
- OpenCV y JavaCV para el reconocimiento facial.
- DroidCam para utilizar el celular como cámara.
- OBS Studio y OBS Virtual Camera para exponer la cámara del celular a FitAccess.
- Arduino IDE, comunicación serial y servo motor.
- JFreeChart para gráficas.
- JasperReports para reportes PDF.
- JUnit 5 para pruebas unitarias.
- Git y GitHub para control de versiones.

## Requisitos

Antes de ejecutar el proyecto se necesita instalar:

1. JDK 17.
2. Apache NetBeans.
3. Maven, incluido normalmente con NetBeans.
4. XAMPP con MySQL o MariaDB.
5. Git para Windows.
6. OBS Studio.
7. DroidCam en el celular y en Windows.
8. Arduino IDE, si se utilizará el torniquete.

## Estructura del proyecto

```text
fitaccess/
├── pom.xml
├── README.md
├── config.properties
├── sql/
│   ├── fitaccess.sql
│   └── datos_prueba.sql
├── recursos/
│   ├── haarcascade_frontalface_default.xml
│   └── reportes/
├── arduino/
│   └── fitaccess/
│       └── fitaccess.ino
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/fitaccess/
    │   │       ├── App.java
    │   │       ├── modelo/
    │   │       ├── dao/
    │   │       ├── dao/impl/
    │   │       ├── servicio/
    │   │       ├── hardware/
    │   │       ├── vista/
    │   │       ├── reportes/
    │   │       └── util/
    │   └── resources/
    └── test/
        └── java/
```

> En algunas copias creadas desde NetBeans el paquete puede aparecer como `com.mycompany.fitaccess`. Debe utilizarse el paquete que tenga realmente el código fuente del repositorio. La clase principal configurada en Maven debe coincidir con la ruta real de `App.java`.

## Configuración de la base de datos

### 1. Encender XAMPP

Abrir XAMPP y presionar **Start** en:

```text
MySQL
```

Apache solo es necesario si se utilizará phpMyAdmin.

### 2. Crear la estructura

Abrir phpMyAdmin en:

```text
http://localhost/phpmyadmin
```

Ejecutar primero:

```text
sql/fitaccess.sql
```

Este archivo crea la base de datos, las tablas, índices y relaciones del sistema.

### 3. Cargar datos de demostración

Después de crear las tablas se puede ejecutar:

```text
sql/datos_prueba.sql
```

Este archivo agrega clientes, planes, suscripciones, pagos, asistencias y medidas para probar el calendario, el progreso, los reportes, los descuentos y las horas pico.

Los clientes insertados por `datos_prueba.sql` no tienen fotografías faciales. Para probar el reconocimiento se debe registrar a una persona real desde FitAccess.

### 4. Configurar la conexión

El archivo `config.properties` debe estar en la raíz del proyecto. Ejemplo:

```properties
db.url=jdbc:mysql://localhost:3306/fitaccess?useSSL=false&serverTimezone=America/Guayaquil&allowPublicKeyRetrieval=true
db.user=root
db.password=

# Índice de la cámara que funciona en el computador de exposición
camara.indice=2

# Puerto del Arduino. Cambiar COM4 por el puerto real.
arduino.puerto=COM4

# Parámetros del reconocimiento facial
reconocimiento.umbral=70
reconocimiento.cuadros=3

# Hora de cierre automático, si está configurada
cierre.hora=22:00
```

El usuario de XAMPP normalmente es `root` sin contraseña. Si la instalación local tiene una contraseña, debe colocarse en `db.password`.

No se deben publicar contraseñas reales, fotografías ni archivos generados localmente en GitHub.

## Ejecutar el proyecto

1. Encender MySQL desde XAMPP.
2. Confirmar que la base `fitaccess` exista.
3. Abrir el proyecto en NetBeans.
4. Esperar a que Maven descargue las dependencias.
5. Verificar que la clase principal sea `App`.
6. Ejecutar **Clean and Build**.
7. Confirmar que aparezca:

```text
BUILD SUCCESS
```

8. Ejecutar el proyecto con **Run** o `F6`.

La clase principal se configura en:

```text
Project Properties → Run → Main Class
```

Debe corresponder a la ruta real, por ejemplo:

```text
com.fitaccess.App
```

o, si esa es la ruta del código local:

```text
com.mycompany.fitaccess.App
```

## Usuario administrador inicial

La primera ejecución crea el administrador predeterminado si la tabla no tiene usuarios:

```text
Usuario: admin
Contraseña: admin123
```

Se recomienda cambiar la contraseña para un entorno real. La aplicación almacena las contraseñas usando BCrypt.

## Configuración de la cámara del celular

En el computador de exposición se utiliza este flujo:

```text
Celular
  ↓
DroidCam
  ↓
DroidCam Source 3 en OBS
  ↓
OBS Virtual Camera
  ↓
FitAccess
```

### Preparar DroidCam

1. Abrir DroidCam en el celular.
2. Conectar el celular a la computadora.
3. Abrir el cliente DroidCam.
4. Verificar que el video del celular aparezca correctamente.
5. Mantener el celular desbloqueado durante la conexión si el sistema lo solicita.

El método de conexión puede ser USB o Wi-Fi, según la configuración disponible. Para la exposición se recomienda USB por su mayor estabilidad.

### Preparar OBS

1. Abrir OBS Studio.
2. Agregar una fuente de captura de video.
3. Seleccionar:

```text
DroidCam Source 3
```

4. Verificar que el video aparezca en el lienzo de OBS.
5. Presionar:

```text
Iniciar cámara virtual
```

El botón debe cambiar a **Detener cámara virtual**.

6. Mantener OBS abierto mientras se ejecuta FitAccess.

En el computador utilizado para la presentación, el valor probado es:

```properties
camara.indice=2
```

El índice puede cambiar en otra computadora. Si FitAccess abre una cámara incorrecta, cerrar la aplicación, probar otro índice y volver a ejecutarla.

## Configuración de Arduino

### Conexión

1. Conectar el Arduino por USB.
2. Abrir el Administrador de dispositivos de Windows.
3. Revisar la sección **Puertos (COM y LPT)**.
4. Identificar el puerto, por ejemplo:

```text
USB-SERIAL CH340 (COM4)
```

5. Configurar el mismo puerto en `config.properties`:

```properties
arduino.puerto=COM4
```

### Subir el sketch

1. Abrir Arduino IDE.
2. Abrir:

```text
arduino/fitaccess/fitaccess.ino
```

3. Seleccionar la placa correcta, normalmente Arduino Uno.
4. Seleccionar el puerto COM correcto.
5. Instalar la biblioteca `LiquidCrystal I2C` si el sketch la requiere.
6. Compilar y subir el sketch.
7. Cerrar el Monitor Serie antes de ejecutar FitAccess.

El puerto serial solo puede ser utilizado por un programa a la vez. Si el Monitor Serie está abierto, FitAccess puede mostrar que Arduino está desconectado.

Si Arduino no está conectado, la aplicación debe continuar funcionando con la cámara y la interfaz gráfica.

## Funcionalidades

### Registro de clientes

- Registro de datos personales.
- Validación de cédula ecuatoriana.
- Validación de nombres y apellidos.
- Validación de teléfono y correo.
- Validación de peso y altura.
- Consentimiento para el tratamiento de datos biométricos.
- Captura y almacenamiento del rostro.

### Acceso facial

- Reconocimiento de clientes registrados.
- Comprobación de suscripción vigente.
- Registro automático de entrada.
- Registro automático de salida.
- Pausa para evitar registros duplicados.
- Mensaje para clientes no reconocidos.
- Plan alternativo de ingreso mediante cédula.

### Administrador

- Inicio de sesión protegido.
- Gestión de clientes.
- Gestión de planes y suscripciones.
- Consulta de asistencia.
- Clientes actualmente dentro del gimnasio.
- Historial por fecha.
- Clientes con suscripción próxima a vencer.
- Gráfica de horas pico.
- Calendario de asistencia.
- Progreso de peso e IMC.
- Registro de pagos.
- Cálculo de descuentos por constancia.
- Generación de reportes PDF.
- Consulta de información de versión y Git.

### Arduino

Cuando el hardware está conectado, el sistema puede controlar:

- Luz verde para acceso autorizado.
- Luz roja para acceso rechazado.
- Buzzer.
- Pantalla LCD.
- Servo del torniquete.

## Reglas de descuento

El porcentaje de descuento por asistencia es:

| Porcentaje de asistencia | Descuento |
|---:|---:|
| 100 % | 5 % |
| 80 % a 99 % | 2 % |
| Menos de 80 % | 0 % |

Los días anteriores al registro del cliente no deben contabilizarse como faltas. En el calendario se muestran en gris y no afectan el porcentaje de asistencia.

## Validaciones principales

- La cédula debe tener 10 dígitos y un dígito verificador correcto.
- Los nombres y apellidos aceptan letras y espacios.
- El teléfono ecuatoriano debe tener el formato `09XXXXXXXX`.
- El correo debe tener un formato válido.
- El peso debe estar dentro del rango definido por la aplicación.
- La altura debe estar dentro del rango definido por la aplicación.
- Los campos obligatorios no pueden quedar vacíos.
- Los usuarios deben tener un formato permitido.
- Los precios deben ser mayores que cero.
- Las fechas deben ser coherentes.
- No se deben guardar pagos sin cliente, plan o método de pago.

## Pruebas

Para ejecutar las pruebas desde NetBeans:

```text
Clic derecho sobre el proyecto → Test
```

También se puede ejecutar desde PowerShell:

```powershell
mvn test
```

El resultado esperado es:

```text
BUILD SUCCESS
```

Las pruebas cubren reglas como:

- Cédula.
- Correo.
- Teléfono.
- Duración de entrenamientos.
- Descuentos por asistencia.
- Rangos numéricos.
- IMC y otras reglas de negocio.

## Control de versiones

Las ramas principales son:

```text
main      versión final para exposición
 develop  integración del equipo
```

Cada integrante trabajó en una rama de funcionalidad. Después de terminar una tarea se creó un Pull Request hacia `develop`.

La versión final se publica en `main` y se identifica con el tag:

```text
v1.0
```

Para consultar el historial desde Git:

```powershell
git log --oneline --decorate --graph --all
```

## Flujo recomendado de trabajo con Git

```powershell
git switch develop
git pull origin develop
git switch -c feature/nombre-de-la-tarea
```

Después de modificar los archivos:

```powershell
git status
git add archivo-modificado
git commit -m "descripcion clara del cambio"
git push -u origin feature/nombre-de-la-tarea
```

Luego se crea el Pull Request hacia `develop`.

No se recomienda usar `git push --force` en `develop` o `main`.

## Solución de problemas

### No hay conexión con MySQL

- Verificar que MySQL esté iniciado en XAMPP.
- Revisar que exista la base `fitaccess`.
- Ejecutar `sql/fitaccess.sql`.
- Revisar `db.url`, `db.user` y `db.password`.

### La aplicación no encuentra la clase principal

Verificar en NetBeans:

```text
Project Properties → Run → Main Class
```

La clase debe coincidir con el paquete real de `App.java`.

### La cámara abre la webcam equivocada

Cambiar `camara.indice` en `config.properties` y volver a ejecutar la aplicación. En el computador de exposición se utiliza:

```properties
camara.indice=2
```

### La cámara aparece verde o vacía

- Comprobar que DroidCam muestre video.
- Comprobar que `DroidCam Source 3` muestre video en OBS.
- Verificar que OBS indique **Detener cámara virtual**.
- Cerrar otras aplicaciones que utilicen la cámara.
- Cerrar y abrir nuevamente la cámara virtual de OBS.
- Reiniciar OBS si es necesario.
- Mantener OBS abierto antes de iniciar FitAccess.

### No se reconoce un rostro

- Mejorar la iluminación frontal.
- Evitar contraluz.
- Colocar el celular a la altura del rostro.
- Registrar nuevamente el rostro.
- Revisar el umbral de reconocimiento en `config.properties`.
- Verificar que el archivo Haar cascade exista en `recursos/`.

### Arduino aparece desconectado

- Cerrar el Monitor Serie del Arduino IDE.
- Revisar el puerto COM.
- Revisar el cable USB.
- Confirmar que el sketch haya sido cargado.
- Comprobar `arduino.puerto` en `config.properties`.

### JasperReports falla al generar un PDF

- Revisar que las plantillas `.jrxml` estén en `recursos/reportes/`.
- Verificar que existan datos para el periodo seleccionado.
- Ejecutar nuevamente **Clean and Build**.
- Revisar el mensaje de error mostrado por la aplicación.

### Maven tarda en la primera compilación

Es normal. Maven descarga JavaCV, OpenCV, JasperReports y otras dependencias la primera vez. La compilación posterior debe ser más rápida.

## Privacidad y datos biométricos

Las fotografías faciales son datos biométricos sensibles. El sistema solicita consentimiento antes del registro y conserva las imágenes en el equipo local dentro de la carpeta utilizada por el reconocimiento facial.

No se deben subir fotografías, perfiles faciales, contraseñas reales, archivos de configuración privados ni datos personales de clientes al repositorio público.

Para una instalación real se deben aplicar las políticas de protección de datos correspondientes y obtener la autorización de los usuarios.

## Preparación para la exposición

El día anterior se debe comprobar:

- XAMPP inicia MySQL.
- La base tiene las tablas y datos de demostración.
- Existen tres o cuatro personas reales registradas con rostro.
- DroidCam muestra el video del celular.
- OBS muestra `DroidCam Source 3`.
- OBS Virtual Camera está activa.
- `camara.indice=2` funciona en el computador de exposición.
- Arduino tiene el sketch cargado.
- El Monitor Serie está cerrado.
- El cable USB del celular funciona.
- Existe un cable de repuesto.
- El ingreso mediante cédula funciona como plan B.
- Se puede generar al menos un reporte PDF.
- La aplicación compila desde `main`.
- La etiqueta `v1.0` está publicada en GitHub.

## Orden sugerido de demostración

1. Mostrar la aplicación iniciada y la conexión con la base de datos.
2. Mostrar el registro de un cliente.
3. Capturar su rostro.
4. Mostrar el ingreso mediante reconocimiento facial.
5. Mostrar el mensaje de Arduino y la apertura del torniquete.
6. Mostrar la pantalla del cliente.
7. Registrar la salida y mostrar la duración del entrenamiento.
8. Mostrar un cliente no reconocido o vencido.
9. Abrir el panel administrativo.
10. Mostrar quién está dentro, asistencia, calendario y progreso.
11. Registrar una renovación con descuento.
12. Generar un reporte PDF.
13. Mostrar la ventana de versiones y el historial de Git.

## Versión

```text
FitAccess v1.0
Versión final para exposición
```
