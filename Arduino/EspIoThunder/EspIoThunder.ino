#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include "ESPAsyncWebServer.h"
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <FastLED.h>

// Replace with your network credentials
const char* ssid = "TODO: SSID";
const char* password = "TODO: PASSWORD";

#define LED_PIN     13
#define NUM_LEDS    60

int red = 0;
int green = 0;
int blue = 0;
int alpha = 0;

CRGB leds[NUM_LEDS];

AsyncWebServer server(80);

void setup() {
  setupWifi();
  setupOTA();
  setupLeds();
  setupServer();  
}

void loop() {
  ArduinoOTA.handle();
}

void setupWifi() {
  Serial.begin(115200);
  Serial.println("Booting");
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  
  while (WiFi.waitForConnectResult() != WL_CONNECTED) {
    Serial.println("Connection Failed! Rebooting...");
    delay(5000);
    ESP.restart();
  }
}

void setupOTA() {
  ArduinoOTA.onStart([]() {
    Serial.println("Start");
  });
  ArduinoOTA.onEnd([]() {
    Serial.println("\nEnd");
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
    else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
    else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
    else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
    else if (error == OTA_END_ERROR) Serial.println("End Failed");
  });
  ArduinoOTA.begin();
  Serial.println("Ready");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void setupLeds() {
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setBrightness(5);

  setColor(CRGB(255, 193, 7));  
}

void setupServer() {
  server.on("/color", HTTP_POST, [](AsyncWebServerRequest *request) {
    handleColorRequest(request);
    
    request->send(200, "text/plain", "OK");
  });
  
  server.begin();
}

void handleColorRequest(AsyncWebServerRequest *request) {
  int params = request->params();

  for(int i = 0; i < params; i++) {
    AsyncWebParameter* p = request->getParam(i);

    if(p->name() == "red") {
      red = p->value().toInt();
    } else if(p->name() == "green") {
      green = p->value().toInt();
    } else if(p->name() == "blue") {
      blue = p->value().toInt();
    } else if(p->name() == "alpha") {
      alpha = p->value().toInt();
    }
  }

  FastLED.setBrightness(alpha);
  setColor(CRGB(red, green, blue));
}

void setColor(CRGB color) {
  for (int i = 0; i < NUM_LEDS; i++) {
    leds[i] = color;
    FastLED.show();
  }
}
