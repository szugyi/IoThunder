#include <ESP8266WiFi.h>

//how many clients should be able to telnet to this ESP8266
#define MAX_SRV_CLIENTS 1
#define PORT 8081
#define MAX_PWM_VAL 5

const char* ssid     = "***************";
const char* password = "***************";
// WIFI
const unsigned int MAX_INPUT = 1024; // how much serial data we expect before a newline

WiFiServer server(PORT);
WiFiClient serverClients[MAX_SRV_CLIENTS];

// State-machine
typedef enum {  NONE, LED, DELAY } states; // the possible states of the state-machine
states state = NONE;                                // current state-machine state
unsigned int currentValue = 0;                          // current partial number
unsigned int currentLed = 0;
const unsigned int ledCount = 4;
const unsigned int leds[] = {12, 13, 14, 15};

void setup() {
  // Initializing the used pins
  int i;
  for (i = 0; i < ledCount; i++) {
    pinMode(leds[i], OUTPUT);
  }
  analogWriteRange(MAX_PWM_VAL);

  Serial.begin(115200);
  delay(100);

  // We start by connecting to a WiFi network
  Serial.println("===========================");
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  // Start the server
  server.begin();
  server.setNoDelay(true);

  Serial.print("Ready! Use 'telnet ");
  Serial.print(WiFi.localIP()); Serial.print(" "); Serial.print(PORT);
  Serial.println("' to connect");
}

void loop() {
  uint8_t i;
  //check if there are any new clients
  if (server.hasClient()) {
    for (i = 0; i < MAX_SRV_CLIENTS; i++) {
      //find free/disconnected spot
      if (!serverClients[i] || !serverClients[i].connected()) {
        if (serverClients[i]) serverClients[i].stop();
        serverClients[i] = server.available();
        Serial.print("New client: "); Serial.print(i);
        continue;
      }
    }
    //no free/disconnected spot so reject
    WiFiClient serverClient = server.available();
    serverClient.stop();
  }

  //check clients for data
  for (i = 0; i < MAX_SRV_CLIENTS; i++) {
    if (serverClients[i] && serverClients[i].connected()) {
      if (serverClients[i].available()) {
        //get data from the telnet client and push it to the UART
        while (serverClients[i].available()) {
          processIncomingByte (serverClients[i].read ());
        }
      }
    }
  }
}

void processIncomingByte (const byte inByte) {
  static char input_line [MAX_INPUT];
  static unsigned int input_pos = 0;

  switch (inByte) {
    case '\n':   // end of text
      input_line [input_pos] = 0;  // terminating null byte
      process_data (input_line);   // terminator reached! process input_line here ...
      input_pos = 0;               // reset buffer for next time
      break;
    case '\r':   // discard carriage return
      break;
    default:
      // keep adding if not full ... allow for terminating null byte
      if (input_pos < (MAX_INPUT - 1))
        input_line [input_pos++] = inByte;
      break;
  }
}

// here to process incoming serial data after a terminator received
void process_data (const char* data) {
  Serial.println (data);
  
  while (*data) {
    if (isdigit (*data)) {
      currentValue *= 10;
      currentValue += *data - '0';

      if (state == LED) {
        if (currentLed >= ledCount) currentLed = 0;
        analogWrite(leds[currentLed], currentValue);
        currentLed++;
        currentValue = 0;
      }
    }
    else {
      // The end of the number signals a state change
      if (state == DELAY) {
        delay(currentValue * 100);
      }
      
      currentLed = 0;
      currentValue = 0;

      // set the new state, if we recognize it
      switch (*data) {
        case 'L':
          currentLed = 0;
          state = LED;
          break;
        case 'D':
          state = DELAY;
          break;
        default:
          state = NONE;
          break;
      }
    }

    data++;
  }
  
  dimLeds();
  Serial.println ("Data processing finished");
}

void dimLeds(){
  int i;
  for (i = 0; i < ledCount; i++) {
    analogWrite(leds[i], 0);
  }
}

