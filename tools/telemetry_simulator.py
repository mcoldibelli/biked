#!/usr/bin/env python3
"""
Simulador de telemetria - imita o ESP32 enviando dados para a API
"""

import requests
import time
import random
import math
import sys
import argparse

# ===== CONFIGURACAO =====
API_BASE = "http://localhost:8080/api/v1"
EMAIL = "marcelo@email.com"
PASSWORD = "marcelo123"
NAME = "Marcelo ABC"

# ===== CORES NO TERMINAL =====
GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
RED = "\033[91m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ===== PERFIS DE TREINO =====
PROFILES = {
    "hiit": {
        "name": "HIIT",
        "description": "Alta intensidade intervalada (30s sprint / 15s descanso)",
        "duration": 60,
    },
    "endurance": {
        "name": "Endurance", 
        "description": "Ritmo constante moderado",
        "duration": 90,
    },
    "climb": {
        "name": "Hill Climb",
        "description": "Subida progressiva com picos",
        "duration": 75,
    },
    "recovery": {
        "name": "Recovery",
        "description": "Baixa intensidade para recuperacao",
        "duration": 45,
    },
    "tabata": {
        "name": "Tabata",
        "description": "20s maximo / 10s descanso (8 rounds)",
        "duration": 60,
    },
    "random": {
        "name": "Random",
        "description": "Variacoes aleatorias imprevisiveis",
        "duration": 60,
    },
}

def log(color, label, msg):
    print(f"{color}[{label}]{RESET} {msg}")

# ===== API CALLS =====
def register_user():
    resp = requests.post(f"{API_BASE}/auth/register", json={
        "email": EMAIL,
        "name": NAME,
        "password": PASSWORD
    })
    if resp.status_code == 201:
        log(GREEN, "AUTH", "Usuario registrado")
    elif resp.status_code == 409:
        log(YELLOW, "AUTH", "Usuario ja existe")
    else:
        log(RED, "AUTH", f"Erro no registro: {resp.status_code}")

def login():
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
        sys.exit(1)

def send_telemetry(workout_id, cadence, speed, heart_rate=None):
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

def finish_workout(token, workout_id, distance, calories):
    resp = requests.put(
        f"{API_BASE}/workouts/{workout_id}/finish",
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        },
        json={
            "distanceMeters": distance,
            "caloriesBurned": calories
        }
    )
    if resp.status_code == 200:
        log(GREEN, "WORKOUT", "Finalizado!")
        return resp.json()
    else:
        log(RED, "WORKOUT", f"Erro ao finalizar: {resp.status_code}")
        return None

# ===== GERADORES DE INTENSIDADE =====
def generate_hiit(elapsed, duration):
    """HIIT: 30s sprint / 15s descanso"""
    cycle = elapsed % 45
    if cycle < 30:
        return 0.8 + random.uniform(0, 0.2)
    else:
        return 0.3 + random.uniform(0, 0.1)

def generate_endurance(elapsed, duration):
    """Endurance: ritmo constante com leve variacao"""
    base = 0.6
    wave = math.sin(elapsed / 10) * 0.1
    fatigue = max(0, (elapsed / duration - 0.7) * 0.3) if elapsed > duration * 0.7 else 0
    return base + wave - fatigue + random.uniform(-0.05, 0.05)

def generate_climb(elapsed, duration):
    """Hill Climb: subida progressiva com platos"""
    progress = elapsed / duration
    if progress < 0.2:
        base = 0.4
    elif progress < 0.4:
        base = 0.55
    elif progress < 0.6:
        base = 0.7
    elif progress < 0.8:
        base = 0.85
    else:
        base = 0.6
    return base + random.uniform(-0.05, 0.05)

def generate_recovery(elapsed, duration):
    """Recovery: baixa intensidade constante"""
    base = 0.35
    wave = math.sin(elapsed / 15) * 0.05
    return base + wave + random.uniform(-0.03, 0.03)

def generate_tabata(elapsed, duration):
    """Tabata: 20s maximo / 10s descanso"""
    cycle = elapsed % 30
    if cycle < 20:
        return 0.9 + random.uniform(0, 0.1)
    else:
        return 0.2 + random.uniform(0, 0.1)

def generate_random(elapsed, duration):
    """Random: mudancas bruscas aleatorias"""
    if random.random() < 0.1:
        return random.uniform(0.3, 1.0)
    return None

GENERATORS = {
    "hiit": generate_hiit,
    "endurance": generate_endurance,
    "climb": generate_climb,
    "recovery": generate_recovery,
    "tabata": generate_tabata,
    "random": generate_random,
}

# ===== SIMULACAO =====
def simulate_workout(profile="endurance", duration_override=None, interval=1):
    profile_info = PROFILES.get(profile, PROFILES["endurance"])
    duration = duration_override or profile_info["duration"]
    generator = GENERATORS.get(profile, generate_endurance)

    print(f"\n{'='*60}")
    print(f"   SIMULADOR DE TELEMETRIA BIKED")
    print(f"{'='*60}")
    print(f"   Perfil: {profile_info['name']}")
    print(f"   {profile_info['description']}")
    print(f"   Duracao: {duration}s")
    print(f"{'='*60}\n")

    # Setup
    register_user()
    token = login()
    workout_id = start_workout(token)
    
    print(f"\n{BLUE}Iniciando treino...{RESET}\n")
    print(f"  {'TEMPO':<12} | {'BARRA':<22} | {'RPM':>6} | {'KM/H':>6} | {'BPM':>4}")
    print(f"  {'-'*12} | {'-'*22} | {'-'*6} | {'-'*6} | {'-'*4}")
    
    total_distance = 0
    total_calories = 0
    data_points = 0
    last_intensity = 0.5
    
    start_time = time.time()
    
    try:
        while (time.time() - start_time) < duration:
            elapsed = time.time() - start_time
            elapsed_int = int(elapsed)
            
            # Warm-up (primeiros 10%)
            if elapsed < duration * 0.1:
                warmup_factor = elapsed / (duration * 0.1)
                intensity = 0.3 + (0.2 * warmup_factor)
            # Cool-down (ultimos 10%)
            elif elapsed > duration * 0.9:
                cooldown_progress = (elapsed - duration * 0.9) / (duration * 0.1)
                intensity = last_intensity * (1 - cooldown_progress * 0.5)
            else:
                new_intensity = generator(elapsed, duration)
                if new_intensity is not None:
                    intensity = new_intensity
                else:
                    intensity = last_intensity
            
            last_intensity = intensity
            
            # Cadencia: 50-110 RPM baseado na intensidade
            cadence = 50 + (60 * intensity) + random.uniform(-3, 3)
            
            # Velocidade: proporcional a cadencia
            resistance_factor = 0.3 + (0.1 * intensity)
            speed = cadence * resistance_factor + random.uniform(-0.5, 0.5)
            
            # Frequencia cardiaca: 90-180 BPM
            target_hr = 90 + (90 * intensity)
            heart_rate = int(target_hr + random.uniform(-3, 3))
            
            # Envia telemetria
            if send_telemetry(workout_id, round(cadence, 1), round(speed, 1), heart_rate):
                data_points += 1
                total_distance += speed * interval
                total_calories += (cadence * 0.05 * intensity)
                
                # Barra visual
                bar_len = int(intensity * 20)
                bar = "#" * bar_len + "." * (20 - bar_len)
                time_str = f"{elapsed_int}s / {duration}s"
                print(f"  {time_str:<12} | [{bar}] | {cadence:6.1f} | {speed:6.1f} | {heart_rate:4d}")
            else:
                log(RED, "ERRO", "Falha ao enviar telemetria")
            
            time.sleep(interval)
    
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Treino interrompido pelo usuario{RESET}")
    
    # Finaliza
    print(f"\n{BLUE}Finalizando treino...{RESET}\n")
    result = finish_workout(
        token, 
        workout_id, 
        round(total_distance, 1), 
        int(total_calories)
    )
    
    # Resumo
    if result:
        print(f"\n{'='*60}")
        print(f"   RESUMO DO TREINO")
        print(f"{'='*60}")
        print(f"   Perfil:           {profile_info['name']}")
        print(f"   DataPoints:       {data_points}")
        print(f"   Duracao:          {result.get('durationSeconds', 0)}s")
        print(f"   Cadencia media:   {result.get('avgCadence', 0):.1f} RPM")
        print(f"   Cadencia maxima:  {result.get('maxCadence', 0):.0f} RPM")
        print(f"   Velocidade media: {result.get('avgSpeed', 0):.1f} km/h")
        print(f"   Velocidade max:   {result.get('maxSpeed', 0):.1f} km/h")
        print(f"   Distancia:        {result.get('distanceMeters', 0):.0f}m")
        print(f"   Calorias:         {result.get('caloriesBurned', 0)}")
        print(f"{'='*60}\n")

def list_profiles():
    print(f"\n{'='*60}")
    print(f"   PERFIS DISPONIVEIS")
    print(f"{'='*60}\n")
    for key, profile in PROFILES.items():
        print(f"   {profile['name']:15} ({key})")
        print(f"   {profile['description']}")
        print(f"   Duracao padrao: {profile['duration']}s\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Simulador de telemetria Biked")
    parser.add_argument("profile", nargs="?", default="endurance",
                        help="Perfil de treino (hiit, endurance, climb, recovery, tabata, random)")
    parser.add_argument("-d", "--duration", type=int, help="Duracao em segundos")
    parser.add_argument("-i", "--interval", type=int, default=1, help="Intervalo entre envios")
    parser.add_argument("-l", "--list", action="store_true", help="Lista perfis disponiveis")
    
    args = parser.parse_args()
    
    if args.list:
        list_profiles()
    else:
        simulate_workout(args.profile, args.duration, args.interval)