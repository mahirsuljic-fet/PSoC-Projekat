const static byte Rp = 1;
const static byte Yp = 0;
const static byte Gp = 2;

const static int BASE_DELAY = 2000;
const static int RED_DELAY = 3 * BASE_DELAY;
const static int RED_YELLOW_DELAY = 2 * BASE_DELAY;
const static int GREEN_DELAY = 2 * BASE_DELAY;
const static int GREEN_YELLOW_DELAY = 1 * BASE_DELAY;

void setup() {
  pinMode(Rp, OUTPUT);
  pinMode(Yp, OUTPUT);
  pinMode(Gp, OUTPUT);
}

void loop() {
  digitalWrite(Rp, HIGH);
  digitalWrite(Yp, LOW);
  digitalWrite(Gp, LOW);

  delay(RED_DELAY);

  digitalWrite(Rp, LOW);
  digitalWrite(Yp, HIGH);
  digitalWrite(Gp, LOW);

  delay(RED_YELLOW_DELAY);

  digitalWrite(Rp, LOW);
  digitalWrite(Yp, LOW);
  digitalWrite(Gp, HIGH);

  delay(GREEN_DELAY);

  digitalWrite(Rp, LOW);
  digitalWrite(Yp, HIGH);
  digitalWrite(Gp, LOW);

  delay(GREEN_YELLOW_DELAY);
}
