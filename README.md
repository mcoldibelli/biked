# Biked

Sistema de telemetria para bike de spinning que coleta dados em tempo real de sensores, processa
métricas de performance e disponibiliza através de APIs REST.

## Stack Tecnologica

| Camada         | Tecnologia                 |
|----------------|----------------------------|
| Backend        | Java 17, Spring Boot 3.4.1 |
| Banco de Dados | PostgreSQL 17              |
| Mensageria     | RabbitMQ 3                 |
| Autenticacao   | JWT                        |
| Documentacao   | OpenAPI / Swagger          |
| Containers     | Docker, Docker Compose     |
| CI/CD          | GitHub Actions             |
| Hardware       | ESP32, Sensor de cadencia  |

## Arquitetura

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   ESP32     │────>│  Telemetry  │────>│  RabbitMQ   │
│   Sensor    │ HTTP│  Endpoint   │     │   Queue     │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                               v
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Frontend   │────>│   REST API  │<────│  Consumer   │
│   (Web)     │ JWT │             │     │  Service    │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                           v
                    ┌─────────────┐
                    │ PostgreSQL  │
                    └─────────────┘
```

## Requisitos

- Java 17+
- Docker e Docker Compose
- Arduino IDE ou PlatformIO (para o firmware)

## Como Executar

### Ambiente completo (Docker)

```bash
# Clone o repositorio
git clone https://github.com/mcoldibelli/biked.git
cd biked

# Suba os containers
docker-compose up --build
```

Servicos disponiveis:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- RabbitMQ: http://localhost:15672 (guest/guest)

### Desenvolvimento local

```bash
# Suba apenas o banco e mensageria
docker-compose up db rabbitmq -d

# Execute a aplicacao
cd backend
./gradlew bootRun
```

## Endpoints da API

### Autenticação

| Método | Endpoint                | Descrição           |
|--------|-------------------------|---------------------|
| POST   | `/api/v1/auth/register` | Registrar usuário   |
| POST   | `/api/v1/auth/login`    | Login (retorna JWT) |

### Usuários

| Método | Endpoint             | Descrição           |
|--------|----------------------|---------------------|
| GET    | `/api/v1/users/me`   | Usuário autenticado |
| GET    | `/api/v1/users/{id}` | Buscar por ID       |
| PUT    | `/api/v1/users/me`   | Atualizar usuário   |
| PUT    | `/api/v1/users/{id}` | Atualizar por ID    |
| DELETE | `/api/v1/users/{id}` | Remover usuário     |

### Devices

| Método | Endpoint                                      | Descrição                      | Auth    |
|--------|-----------------------------------------------|--------------------------------|---------|
| POST   | `/api/v1/devices`                             | Registrar device (MAC address) | JWT     |
| GET    | `/api/v1/devices`                             | Listar devices do usuário      | JWT     |
| GET    | `/api/v1/devices/{macAddress}`                | Buscar device por MAC          | Público |
| GET    | `/api/v1/devices/{macAddress}/active-workout` | Buscar workout ativo           | Público |

### Workouts

| Método | Endpoint                           | Descrição                 |
|--------|------------------------------------|---------------------------|
| POST   | `/api/v1/workouts`                 | Iniciar treino            |
| PUT    | `/api/v1/workouts/{id}/finish`     | Finalizar treino          |
| GET    | `/api/v1/workouts/{id}`            | Buscar treino             |
| GET    | `/api/v1/workouts`                 | Listar treinos (paginado) |
| GET    | `/api/v1/workouts/{id}/datapoints` | Histórico de telemetria   |

### Telemetria

| Método | Endpoint            | Descrição              |
|--------|---------------------|------------------------|
| POST   | `/api/v1/telemetry` | Enviar dados do sensor |

## Métricas Calculadas

Ao finalizar um workout, o sistema calcula automaticamente:

| Métrica                 | Cálculo                                            |
|-------------------------|----------------------------------------------------|
| Duração                 | `finishedAt - startedAt`                           |
| Cadência Média/Máxima   | Agregação dos datapoints                           |
| Velocidade Média/Máxima | Agregação dos datapoints                           |
| Distância               | `SUM(speed) × 1.39` metros                         |
| Calorias                | `MET × peso × duração` (MET dinâmico por cadência) |

### MET Dinâmico

| Cadência (RPM) | MET |
|----------------|-----|
| < 30           | 2.0 |
| 30-49          | 2.5 |
| 50-69          | 4.0 |
| 70-89          | 5.5 |
| ≥ 90           | 7.0 |

## Simulador de Telemetria

Para testar sem o hardware, use o simulador Python:

```bash
cd tools

# Listar perfis disponiveis
python3 telemetry_simulator.py --list

# Executar simulacao HIIT de 60 segundos
python3 telemetry_simulator.py hiit -d 60

# Executar simulacao Endurance
python3 telemetry_simulator.py endurance
```

### Perfis disponiveis

| Perfil      | Descricao                                                |
|-------------|----------------------------------------------------------|
| `hiit`      | Alta intensidade intervalada (30s sprint / 15s descanso) |
| `endurance` | Ritmo constante moderado                                 |
| `climb`     | Subida progressiva com picos                             |
| `recovery`  | Baixa intensidade para recuperacao                       |
| `tabata`    | 20s maximo / 10s descanso                                |
| `random`    | Variacoes aleatorias imprevisiveis                       |

## Estrutura do Projeto

```
biked/
├── backend/                              # Aplicação Spring Boot
│   ├── src/main/java/dev/mcoldibelli/biked/
│   │   ├── config/                       # Configurações (Security, JWT, RabbitMQ, OpenAPI)
│   │   ├── controller/                   # Endpoints REST
│   │   ├── dto/                          # Request/Response objects
│   │   ├── exception/                    # Tratamento de erros
│   │   ├── model/                        # Entidades JPA
│   │   ├── repository/                   # Acesso a dados
│   │   └── service/                      # Lógica de negócio
│   ├── src/test/                         # Testes unitários e integração
│   ├── Dockerfile                        # Build da aplicação
│   └── build.gradle                      # Dependências e build
├── hardware/                             # Firmware ESP32
│   └── main.ino                          # Código Arduino
├── frontend/                             # Aplicação web (em desenvolvimento)
├── tools/                                # Scripts auxiliares (simulador)
└── docker-compose.yml                    # Orquestração de containers
```

## Testes

```bash
cd backend

# Executar testes
./gradlew test

# Relatorio de cobertura
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Hardware (ESP32)

### Componentes

- ESP32 DevKit V1
- Módulo Breakout Jack P2 3.5mm
- Protoboard e jumpers

### Conexões

| Breakout P2    | ESP32   |
|----------------|---------|
| TIP (sinal)    | GPIO 14 |
| SLEEVE (terra) | GND     |

> O pull-up interno do ESP32 é utilizado (`INPUT_PULLUP`), não sendo necessário resistor externo.

### Firmware

O código do ESP32 está em `hardware/main.ino`. Funcionalidades:

- Conexão WiFi automática
- Leitura de cadência via sensor reed switch
- Busca automática de workout ativo por MAC address
- Envio de telemetria a cada 5 segundos
- Detecção de workout finalizado (limpa estado e volta a aguardar)
- Conversão RPM → velocidade (fator 0.2125)

### Upload do Firmware

1. Instale o Arduino IDE
2. Adicione suporte ao ESP32 (Board Manager)
3. Abra `hardware/main.ino`
4. Configure WiFi e IP do backend no código
5. Selecione a placa "ESP32 Dev Module"
6. Faça upload

### Fluxo de Operação

```
ESP32 liga
    ↓
Conecta WiFi
    ↓
Obtém MAC address
    ↓
Busca workout ativo (GET /devices/{MAC}/active-workout)
    ↓
┌─────────────────┬──────────────────┐
│ Tem workout?    │ Não tem?         │
│ Envia telemetria│ Aguarda (retry   │
│ a cada 5s       │ a cada 10s)      │
└────────┬────────┴──────────────────┘
         │
         ↓ (quando workout finaliza)
Backend retorna 409
         ↓
Limpa workoutId → Volta a aguardar
```

## Roadmap

- [x] Autenticacao JWT
- [x] CRUD de usuarios
- [x] Gerenciamento de workouts
- [x] Mensageria com RabbitMQ
- [x] Persistencia de telemetria
- [x] Calculo automatico de metricas
- [x] Docker + PostgreSQL
- [x] CI/CD com GitHub Actions
- [x] Documentacao Swagger
- [x] Simulador de telemetria
- [ ] Frontend web
- [ ] Integracao ESP32
- [ ] Deploy em cloud (AWS/Kubernetes)