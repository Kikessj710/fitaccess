/*
 * FitAccess - Arduino del torniquete
 * Recibe por USB (9600 baudios) una linea de texto desde Java:
 *   A;Enrique;12     acceso: LED verde, pitido corto, servo abre, LCD "Hola Enrique"
 *   S;Enrique;1h35   salida: LED verde, dos pitidos, LCD "Hasta pronto"
 *   N;VENCIDO        negado: LED rojo, alarma, LCD "Suscrip. vencida"
 *   N;DESCONOCIDO    negado: LED rojo, alarma, LCD "No reconocido"
 *   P                ping: responde OK (Java lo usa para encontrar el puerto)
 *
 * Libreria necesaria: "LiquidCrystal I2C" de Frank de Brabander
 * (Arduino IDE > Herramientas > Administrar bibliotecas).
 */
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Servo.h>

const int LED_VERDE = 7;
const int LED_ROJO  = 8;
const int BUZZER    = 9;
const int SERVO_PIN = 6;

// Si la pantalla no muestra nada, cambie 0x27 por 0x3F
LiquidCrystal_I2C lcd(0x27, 16, 2);
Servo barrera;

String linea = "";
unsigned long volverReposoEn = 0;

void setup() {
  Serial.begin(9600);
  pinMode(LED_VERDE, OUTPUT);
  pinMode(LED_ROJO, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  lcd.init();
  lcd.backlight();
  barrera.attach(SERVO_PIN);
  reposo();
}

void loop() {
  // Lee caracter por caracter hasta el salto de linea
  while (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '\n') {
      procesar(linea);
      linea = "";
    } else if (c != '\r') {
      linea += c;
    }
  }
  // Despues de 4 segundos vuelve al mensaje de espera (sin usar delay largo)
  if (volverReposoEn > 0 && millis() > volverReposoEn) {
    reposo();
  }
}

void procesar(String l) {
  l.trim();
  if (l.length() == 0) return;

  int p1 = l.indexOf(';');
  int p2 = l.indexOf(';', p1 + 1);
  String tipo  = (p1 == -1) ? l : l.substring(0, p1);
  String dato1 = (p1 == -1) ? "" : (p2 == -1 ? l.substring(p1 + 1) : l.substring(p1 + 1, p2));
  String dato2 = (p2 == -1) ? "" : l.substring(p2 + 1);

  if (tipo == "P") {
    Serial.println("OK");
  } else if (tipo == "A") {
    acceso(dato1, dato2);
  } else if (tipo == "S") {
    salida(dato1, dato2);
  } else if (tipo == "N") {
    negado(dato1);
  }
}

void acceso(String nombre, String dias) {
  digitalWrite(LED_ROJO, LOW);
  digitalWrite(LED_VERDE, HIGH);
  mostrar("Hola " + nombre, "Quedan " + dias + " dias");
  tone(BUZZER, 1800, 150);
  barrera.write(90);              // abre
  volverReposoEn = millis() + 4000;
}

void salida(String nombre, String duracion) {
  digitalWrite(LED_ROJO, LOW);
  digitalWrite(LED_VERDE, HIGH);
  mostrar("Hasta pronto", nombre + " " + duracion);
  tone(BUZZER, 1500, 100);
  delay(180);
  tone(BUZZER, 1500, 100);
  barrera.write(90);
  volverReposoEn = millis() + 4000;
}

void negado(String motivo) {
  digitalWrite(LED_VERDE, LOW);
  digitalWrite(LED_ROJO, HIGH);
  barrera.write(0);               // se mantiene cerrada
  if (motivo == "VENCIDO") {
    mostrar("Acceso negado", "Suscrip. vencida");
  } else {
    mostrar("Acceso negado", "No reconocido");
  }
  for (int i = 0; i < 3; i++) {   // alarma: tres pitidos graves
    tone(BUZZER, 400, 200);
    delay(300);
  }
  volverReposoEn = millis() + 4000;
}

void reposo() {
  digitalWrite(LED_VERDE, LOW);
  digitalWrite(LED_ROJO, LOW);
  barrera.write(0);               // cierra
  mostrar("   FITACCESS", "Mire la camara");
  volverReposoEn = 0;
}

// Escribe dos lineas de maximo 16 caracteres
void mostrar(String arriba, String abajo) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(arriba.substring(0, min((int) arriba.length(), 16)));
  lcd.setCursor(0, 1);
  lcd.print(abajo.substring(0, min((int) abajo.length(), 16)));
}
