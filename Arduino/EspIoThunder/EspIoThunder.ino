void setup() {
  pinMode(0, OUTPUT);  // Red led on the ESP, might be used later for debugging
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  pinMode(14, OUTPUT);
  pinMode(15, OUTPUT);
}

void loop() {
  digitalWrite(0, HIGH);
  digitalWrite(12, HIGH);
  delay(50);
  digitalWrite(12, LOW);
  delay(50);
  digitalWrite(13, HIGH);
  delay(50);
  digitalWrite(13, LOW);
  digitalWrite(0, LOW);
  delay(50);
  digitalWrite(14, HIGH);
  delay(50);
  digitalWrite(14, LOW);
  delay(50);
  digitalWrite(15, HIGH);
  delay(50);
  digitalWrite(15, LOW);
  delay(50);
}
