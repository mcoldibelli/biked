"""
Simulador de telemetria - imita o ESP32 enviando dados para a API
"""

import requests
import time
import random
import math
import sys

API_BASE = "http://localhost:8080/api/v1"
EMAIL = "marcelo.coldibelli@gmail.com"
PASSWORD = "Marcelo31!"
NAME = "Marcelo Augusto Bersan Coldibelli"

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
RED = "\033[91m"
RESET = "\033[0m"


def log(color, label, msg):
    print(f"{color}[{label}]{RESET} {msg}")

def register_user():
    """Registra usuário se não existir"""
    resp = requests.post(f"{API_BASE}/auth/register", json={
        "email": EMAIL,
        "name": NAME,
        "password": PASSWORD
    })
    if resp.status_code == 201:
        log(GREEN, "AUTH", "Usuário registrado")
    elif resp.status_code == 409:
        log(YELLOW, "AUTH", "Usuário já existe")
    else:
        log(RED, "AUTH", f"Erro no registro: {resp.status_code}")

def login():
    """Faz login e retorna token"""
    resp = requests.post(f"{API_BASE}/auth/login", json={
        "email": EMAIL,
        "password": PASSWORD
    })

    if resp.status_code == 200:
        token = resp.json()["token"]
        log(GREEN, "AUTH", "Login realizado")
        return token
    else:
        log(RED, "AUTH", f"Erro no login: {resp.status_code}")
        sys.exit(1)

def start_workout(token):
    """Inicia um novo workout"""
    resp = requests.post(
        f"{API_BASE}/workouts",
        headers={"Authorization": f"Bearer {token}"}
    )

    if resp.status_code == 201:
        workout_id = resp.json()["id"]
        log(GREEN, "WORKOUT", f"Iniciado: {workout_id}")
        return workout_id
    else:
        log(RED, "WORKOUT", f"Erro ao iniciar: {resp.status_code}")

def send_telemetry(workout_id, cadence, speed, heart_rate=None):
    """Envia ponto de telemetria"""
    data = {
        "workoutId": workout_id,
        "cadence": cadence,
        "speed": speed,
        "timestamp": int(time.time() * 1000)
    }

    if heart_rate:
        data["heartRate"] = heart_rate

    resp = requests.post(f"{API_BASE}/telemetry", json=data)
    return resp.status_code == 202

def finish_workout(token, workout_id, metrics):
    """Finaliza o workout"""
    resp = requests.put(
        f"{API_BASE}/workouts/{workout_id}/finish",
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        },
        json={
            "avgCadence": metrics["avg_cadence"],
            "maxCadence": metrics["max_cadence"],
            "avgSpeed": metrics["avg_speed"],
            "maxSpeed": metrics["max_speed"],
            "distanceMeters": metrics["distance"],
            "caloriesBurned": metrics["calories"]
        }
    )
    if resp.status_code == 200:
        result = resp.json()
        log(GREEN, "WORKOUT", "Finalizado!")
        return result
    else:
        log(RED, "WORKOUT", f"Erro ao finalizar: {resp.status_code}")
        return None
    

def simulate_workout(duration_seconds=30, interval=1):
    """
    Simula um treino completo

    Args:
        duration_second: duração do treino em segundos
        interval: intervalo entre envios em segundos
    """
    print(f"\n{'='*50}")
    print(f"    SIMULADOR DE TELEMETRIA BIKED")
    print(f"{'='*50}\n")

    register_user()
    token = login()
    workout_id = start_workout(token)

    print(f"\n{BLUE}Simulando treino de {duration_seconds}s...{RESET}\n")

    total_distance = 0
    total_calories = 0
    data_points = 0
    cadence_values = []
    speed_values = []

    # Fase do treino (aquecimento -> pico -> desaquecimento)
    start_time = time.time()

    try:
        while (time.time() - start_time) < duration_seconds:
            elapsed = time.time() - start_time
            progress = elapsed / duration_seconds

            # Simula curva de intensidade (seno)
            intensity = math.sin(progress * math.pi)

            # Cadência: 60-100 RPM baseado na intensidade
            base_cadence = 60 + (40 * intensity)
            cadence = base_cadence + random.uniform(-5, 5)

            # Velocidade: proporcional à cadência
            speed = cadence * 0.35 + random.uniform(-1, 1)

            # Frequência cardíaca: 100-160 BPM
            heart_rate = int(100 + (60 * intensity) + random.uniform(-3, 3))

            # Envia telemetria
            if send_telemetry(workout_id, round(cadence, 1), round(speed, 1), heart_rate):
                data_points += 1
                # Acumula métricas
                cadence_values.append(cadence)
                speed_values.append(speed)
                total_distance += speed * interval  # metros por segundo
                total_calories += (cadence * 0.05)  # estimativa simples

                # Log formatado
                bar_len = int(intensity * 20)
                bar = "█" * bar_len + "░" * (20 - bar_len)
                print(f"  {bar} | RPM: {cadence:5.1f} | km/h: {speed:5.1f} | HR:  {heart_rate}")
            else:
                log(RED, "ERRO", "Falha ao enviar telemetria")

            time.sleep(interval)

    except KeyboardInterrupt:
        print(f"\n{YELLOW}Treino interrompido pelo usuário{RESET}")

    # Calcula métricas
    metrics = {
        "avg_cadence": round(sum(cadence_values) / len(cadence_values), 1) if cadence_values else 0,
        "max_cadence": round(max(cadence_values), 1) if cadence_values else 0,
        "avg_speed": round(sum(speed_values) / len(speed_values), 1) if speed_values else 0,
        "max_speed": round(max(speed_values), 1) if speed_values else 0,
        "distance": round(total_distance, 1),
        "calories": int(total_calories)
    }

    # Finaliza
    print(f"\n{BLUE}Finalizando treino...{RESET}\n")
    result = finish_workout(token, workout_id, metrics)
    
    # Resumo
    if result:
        print(f"\n{'='*50}")
        print(f"   RESUMO DO TREINO")
        print(f"{'='*50}")
        print(f"   DataPoints enviados: {data_points}")
        print(f"   Duração: {result.get('durationSeconds', 0)}s")
        print(f"   Cadência média: {result.get('avgCadence', 0):.1f} RPM")
        print(f"   Cadência máxima: {result.get('maxCadence', 0)} RPM")
        print(f"   Velocidade média: {result.get('avgSpeed', 0):.1f} km/h")
        print(f"   Velocidade máxima: {result.get('maxSpeed', 0):.1f} km/h")
        print(f"   Distância: {result.get('distanceMeters', 0):.0f}m")
        print(f"   Calorias: {result.get('caloriesBurned', 0)}")
        print(f"{'='*50}\n")

if __name__ == "__main__":
    # Argumentos opcionais: duração e intervalo
    duration = int(sys.argv[1]) if len(sys.argv) > 1 else 30
    interval = int(sys.argv[2]) if len(sys.argv) > 2 else 1
    
    simulate_workout(duration, interval)