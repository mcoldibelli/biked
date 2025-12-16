# Biked

Sistema de telemetria para bike de spinning que coleta dados em tempo real de sensores, processa
metricas de performance e disponibiliza atraves de APIs REST.

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
- Python 3 (para o simulador)

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
./gradlew bootRun
```

## Endpoints da API

### Autenticacao

| Metodo | Endpoint                | Descricao           |
|--------|-------------------------|---------------------|
| POST   | `/api/v1/auth/register` | Registrar usuario   |
| POST   | `/api/v1/auth/login`    | Login (retorna JWT) |

### Usuarios

| Metodo | Endpoint             | Descricao           |
|--------|----------------------|---------------------|
| GET    | `/api/v1/users/me`   | Usuario autenticado |
| GET    | `/api/v1/users/{id}` | Buscar por ID       |
| PUT    | `/api/v1/users/{id}` | Atualizar usuario   |
| DELETE | `/api/v1/users/{id}` | Remover usuario     |

### Workouts

| Metodo | Endpoint                           | Descricao                 |
|--------|------------------------------------|---------------------------|
| POST   | `/api/v1/workouts`                 | Iniciar treino            |
| PUT    | `/api/v1/workouts/{id}/finish`     | Finalizar treino          |
| GET    | `/api/v1/workouts/{id}`            | Buscar treino             |
| GET    | `/api/v1/workouts`                 | Listar treinos (paginado) |
| GET    | `/api/v1/workouts/{id}/datapoints` | Historico de telemetria   |

### Telemetria

| Metodo | Endpoint            | Descricao              |
|--------|---------------------|------------------------|
| POST   | `/api/v1/telemetry` | Enviar dados do sensor |

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
├── src/main/java/dev/mcoldibelli/biked/
│   ├── config/          # Configuracoes (Security, JWT, RabbitMQ, OpenAPI)
│   ├── controller/      # Endpoints REST
│   ├── dto/             # Request/Response objects
│   ├── exception/       # Tratamento de erros
│   ├── model/           # Entidades JPA
│   ├── repository/      # Acesso a dados
│   └── service/         # Logica de negocio
├── src/test/            # Testes unitarios e integracao
├── tools/               # Scripts auxiliares (simulador)
├── docker-compose.yml   # Orquestracao de containers
├── Dockerfile           # Build da aplicacao
└── build.gradle         # Dependencias e build
```

## Testes

```bash
# Executar testes
./gradlew test

# Relatorio de cobertura
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Hardware (ESP32)

### Componentes

- ESP32 DevKit V1
- Modulo Breakout Jack P2 3.5mm
- Resistor 10k ohms
- Protoboard e jumpers

### Conexoes

| Breakout P2    | ESP32           |
|----------------|-----------------|
| TIP (sinal)    | GPIO 14         |
| SLEEVE (terra) | GND             |
| Resistor 10k   | 3.3V -> GPIO 14 |

TODO

- O firmware esta em `biked-esp32/` (projeto PlatformIO).

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