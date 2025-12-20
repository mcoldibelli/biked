#include <WiFi.h>
#include <HTTPClient.h>

const char* SSID = "Chewbacca";
const char* PASSWORD = "VkDn%XJGC4";
const char* BACKEND_URL = "http://192.168.0.217:8080";

#define SENSOR_PIN 14

volatile unsigned long ultimoPulso = 0;
volatile unsigned long intervalo = 0;
volatile int contadorPulsos = 0;

String workoutId = "";
String macAddress = "";

void IRAM_ATTR contarPulso() {
    unsigned long agora = millis();
    if (agora - ultimoPulso > 100) {
        intervalo = agora - ultimoPulso;
        ultimoPulso = agora;
        contadorPulsos++;
    }
}

void conectarWiFi() {
    Serial.println("Conectando ao WiFi...");
    WiFi.begin(SSID, PASSWORD);
    
    int tentativas = 0;
    while (WiFi.status() != WL_CONNECTED && tentativas < 30) {
        delay(500);
        Serial.print(".");
        tentativas++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWiFi conectado!");
        Serial.print("IP: ");
        Serial.println(WiFi.localIP());
        macAddress = WiFi.macAddress();
        Serial.print("MAC: ");
        Serial.println(macAddress);
    } else {
        Serial.println("\nFalha ao conectar WiFi!");
    }
}

String buscarWorkoutAtivo() {
    if (WiFi.status() != WL_CONNECTED) return "";
    
    HTTPClient http;
    String url = String(BACKEND_URL) + "/api/v1/devices/" + macAddress + "/active-workout";
    
    Serial.print("Buscando workout: ");
    Serial.println(url);
    
    http.begin(url);
    int httpCode = http.GET();
    
    String id = "";
    if (httpCode == 200) {
        String response = http.getString();
        Serial.println("Workout encontrado!");
        int start = response.indexOf("\"id\":\"") + 6;
        int end = response.indexOf("\"", start);
        id = response.substring(start, end);
        Serial.print("Workout ID: ");
        Serial.println(id);
    } else if (httpCode == 404) {
        Serial.println("Nenhum workout ativo");
    } else {
        Serial.print("Erro: ");
        Serial.println(httpCode);
    }
    
    http.end();
    return id;
}

void enviarTelemetria(int rpm) {
    if (workoutId == "" || WiFi.status() != WL_CONNECTED) return;
    
    HTTPClient http;
    String url = String(BACKEND_URL) + "/api/v1/telemetry";
    
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    float speed = rpm * 0.2125;
    
    String json = "{\"workoutId\":\"" + workoutId + "\","
                  "\"cadence\":" + String(rpm) + ","
                  "\"speed\":" + String(speed, 2) + ","
                  "\"timestamp\":" + String(millis()) + "}";
    
    Serial.print("Enviando: ");
    Serial.println(json);
    
    int httpCode = http.POST(json);
    
     if (httpCode == 200 || httpCode == 201 || httpCode == 202) {
        Serial.println("Telemetria enviada!");
    } else if (httpCode == 400 || httpCode == 409) {
        // Workout não está mais ativo
        Serial.println("Workout finalizado, limpando...");
        workoutId = "";
    } else {
        Serial.print("Erro ao enviar: ");
        Serial.println(httpCode);
    }
    
    http.end();
}

void setup() {
    Serial.begin(115200);
    delay(1000);
    
    Serial.println("=== BOOT ===");
    
    pinMode(SENSOR_PIN, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(SENSOR_PIN), contarPulso, FALLING);
    Serial.println("Sensor configurado");
    
    conectarWiFi();
    
    Serial.println("Sistema iniciado!");
    Serial.println("Buscando workout inicial...");
    workoutId = buscarWorkoutAtivo();
    Serial.println("Setup completo!");
}

void loop() {
    static unsigned long ultimaLeitura = 0;
    static unsigned long ultimoEnvio = 0;
    static unsigned long ultimaBuscaWorkout = 0;
    static int rpmFiltrado = 0;
    unsigned long agora = millis();
    
    if (workoutId == "" && agora - ultimaBuscaWorkout >= 10000) {
        ultimaBuscaWorkout = agora;
        workoutId = buscarWorkoutAtivo();
    }

    if (agora - ultimaLeitura >= 1000) {
        ultimaLeitura = agora;
        
        int rpmAtual = 0;
        if (agora - ultimoPulso < 2000 && intervalo > 0) {
            rpmAtual = 60000 / intervalo;
            if (rpmAtual > 200) rpmAtual = 0;
            if (rpmAtual < 10 && rpmAtual > 0) rpmAtual = 0;
        }
        
        rpmFiltrado = (rpmFiltrado + rpmAtual) / 2;
        
        Serial.print("RPM: ");
        Serial.print(rpmFiltrado);
        Serial.print(" | Workout: ");
        Serial.println(workoutId != "" ? workoutId : "aguardando...");
    }
    
    if (workoutId != "" && agora - ultimoEnvio >= 5000) {
        ultimoEnvio = agora;
        enviarTelemetria(rpmFiltrado);
    }
}