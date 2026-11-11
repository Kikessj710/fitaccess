const int LED_VERDE = 7;
const int LED_ROJO = 8;

String linea = "";

void setup() {
  Serial.begin(9600);

  pinMode(LED_VERDE, OUTPUT);
  pinMode(LED_ROJO, OUTPUT);

  apagarLuces();
}

void loop() {
  while (Serial.available() > 0) {
    char c = Serial.read();

    if (c == '\n') {
      procesar(linea);
      linea = "";
    } else if (c != '\r') {
      linea += c;
    }
  }
}

void procesar(String mensaje) {
  mensaje.trim();

  if (mensaje.length() == 0) {
    return;
  }

  if (mensaje == "P") {
    Serial.println("OK");
    apagarLuces();
    return;
  }

  if (mensaje.startsWith("A;")) {
    accesoPermitido();
    return;
  }

  if (mensaje.startsWith("S;")) {
    salidaRegistrada();
    return;
  }

  if (mensaje.startsWith("N;")) {
    accesoRechazado();
    return;
  }
}

void accesoPermitido() {
  digitalWrite(LED_ROJO, LOW);
  digitalWrite(LED_VERDE, HIGH);

  delay(3000);

  apagarLuces();
}

void salidaRegistrada() {
  digitalWrite(LED_ROJO, LOW);
  digitalWrite(LED_VERDE, HIGH);

  delay(3000);

  apagarLuces();
}

void accesoRechazado() {
  digitalWrite(LED_VERDE, LOW);
  digitalWrite(LED_ROJO, HIGH);

  delay(3000);

  apagarLuces();
}

void apagarLuces() {
  digitalWrite(LED_VERDE, LOW);
  digitalWrite(LED_ROJO, LOW);
}
