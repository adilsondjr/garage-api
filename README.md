# Sistema de Gerenciamento de Estacionamento 🚗

[![Java](https://img.shields.io/badge/Java-21+-orange)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)

Um sistema backend robusto e escalável para gerenciar estacionamentos com **preços dinâmicos**, **rastreamento de veículos em tempo real** e **cálculo automático de receita**. Desenvolvido com as melhores práticas de arquitetura, segurança e desempenho.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Recursos Principais](#-recursos-principais)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Início Rápido](#-início-rápido)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Uso](#-exemplos-de-uso)

---

## 🎯 Visão Geral

O **Sistema de Gerenciamento de Estacionamento** é uma solução backend completa que automatiza a gestão de vagas de estacionamento. O sistema processa eventos em tempo real de um simulador, calcula preços dinâmicos baseados na ocupação, rastreia o ciclo de vida dos veículos e gera relatórios de receita automaticamente.

**Caso de uso ideal:** Estacionamentos com múltiplos setores que desejam implementar preços inteligentes e maximizar a receita através da otimização dinâmica de tarifas.

---

## ✨ Recursos Principais

### 💰 Preços Dinâmicos Inteligentes
O sistema ajusta automaticamente os preços baseado na ocupação do setor:
- **Ocupação < 25%**: Desconto de 10% (promoção)
- **Ocupação ≤ 50%**: Sem ajuste (preço base)
- **Ocupação ≤ 75%**: Aumento de 10% (demanda moderada)
- **Ocupação ≤ 100%**: Aumento de 25% (alta demanda)

### 🅿️ Gerenciamento de Setores
- Múltiplos setores de estacionamento independentes
- Configuração individual por setor
- Capacidade máxima configurável
- Preço base customizável

### 📍 Rastreamento de Veículos
- Ciclo de vida completo: ENTRY → PARKED → EXIT
- Timestamps precisos de entrada e saída
- Localização GPS dos veículos
- Histórico de estacionamentos

### 💵 Cálculo Automático de Tarifa
- **Primeiros 30 minutos**: Gratuito
- **Acima de 30 minutos**: Taxa horária com preço dinâmico
- Arredondamento para cima (ceiling) em frações de hora
- Precisão decimal para conversão monetária

### 📊 Gerenciamento de Receita
- Agregação automática diária por setor
- Rastreamento de volume (quantidade de veículos)
- Prevenção de duplicação com constraints únicos
- Otimistic locking para operações concorrentes

### 🔄 Processamento em Tempo Real
- Webhooks para eventos do simulador
- Processamento assíncrono e eficiente
- Transações ACID para integridade dos dados
- Tratamento robusto de erros

### 📡 API REST Completa
- Documentação interativa com Swagger UI
- OpenAPI 3.0 specification
- Endpoints de monitoramento e consulta
- Validação automática de entrada

---

## 🏗️ Arquitetura

### Stack Tecnológico

```
┌─────────────────────────────────────────────────────────────┐
│                   Frontend / Simulador                       │
│          (Gera eventos de entrada/saída)                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Webhooks
                     ↓
┌────────────────────────────────────────────────────────────┐
│              Nginx Proxy (porta 3003)                        │
│         Roteia webhooks → Spring Boot (8080)                │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓
┌────────────────────────────────────────────────────────────┐
│       Spring Boot Application (porta 8080)                   │
├────────────────────────────────────────────────────────────┤
│  Controllers   → Services   → Repositories   → Entities     │
├────────────────────────────────────────────────────────────┤
│  Swagger UI        OpenAPI 3.0      Validação Jakarta       │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓
┌────────────────────────────────────────────────────────────┐
│           MySQL 8.0 Database (porta 3306)                    │
├────────────────────────────────────────────────────────────┤
│  • Sectors          • ParkingSpots                           │
│  • Vehicles         • Revenues                               │
└────────────────────────────────────────────────────────────┘
```

### Padrões de Arquitetura

- **Camadas**: Controller → Service → Repository → Entity
- **Validação**: Jakarta Bean Validation com @Valid
- **Tratamento de Erros**: GlobalExceptionHandler centralizado
- **Cache**: Spring @Cacheable para configurações
- **Concorrência**: Pessimistic locking e Optimistic locking
- **Transações**: @Transactional com controle fino
- **Segurança**: Validação de entrada, mascaramento de erros

---

## 📦 Pré-requisitos

| Componente | Versão | Observação |
|-----------|--------|-----------|
| Java | 21+ | OpenJDK ou Oracle JDK |
| Maven | 3.9+ | Para build do projeto |
| Docker | Latest | Recomendado para dev |
| Docker Compose | Latest | Orquestração de containers |
| MySQL | 8.0+ | Banco de dados (opcional se usar Docker) |

---

## 🚀 Início Rápido

### Opção 1: Docker Compose (Recomendado)

```bash
# Clone o repositório
git clone https://github.com/adilsondjr/garage-api.git
cd garage-api

# Inicie todos os serviços
docker-compose up -d

# Aguarde inicialização (30-40 segundos)
sleep 40

# Verifique se a configuração foi carregada
curl http://localhost:8080/api/v1/garage/sectors
```

**O que inicia automaticamente:**
- ✅ MySQL 8.0 em `localhost:3306` (user: root, senha: password)
- ✅ Simulador em `localhost:3000`
- ✅ Aplicação em `localhost:8080`
- ✅ Nginx proxy em `localhost:3003`

### Opção 2: Desenvolvimento Local

```bash
# Construir o projeto
./mvnw clean package

# Executar testes
./mvnw test

# Iniciar a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

### Opção 3: Build Manual com Maven

```bash
# Criar banco de dados
mysql -u root -p
CREATE DATABASE garage;

# Executar a aplicação
mvn spring-boot:run
```

## 🔌 Endpoints da API

### Eventos de Webhook

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/webhook` | Receber eventos de estacionamento (ENTRY, PARKED, EXIT) |

**Payload do Webhook:**
```json
{
  "event_type": "ENTRY|PARKED|EXIT",
  "license_plate": "ABC1234",
  "sector": "A",
  "latitude": 10.5,
  "longitude": 20.5
}
```

### Endpoints de Garagem

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/garage/sectors` | Listar todos os setores |
| GET | `/api/v1/garage/spots` | Listar todas as vagas |
| GET | `/api/v1/garage/occupancy` | Obter ocupação por setor |

### Endpoints de Receita

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/revenue` | Consultar receita (params: `date`, `sector`) |

### Documentação Interativa

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

---

## 💡 Exemplos de Uso

### 1️⃣ Veículo Entra no Estacionamento

```bash
curl -X POST "http://localhost:8080/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "event_type": "ENTRY",
    "license_plate": "ABC1234",
    "sector": "A",
    "latitude": 10.5,
    "longitude": 20.5,
    "entry_time": "2026-04-22T10:30:00"
  }'
```

**O sistema:**
- ✅ Valida disponibilidade de vagas
- ✅ Calcula preço dinâmico baseado na ocupação
- ✅ Registra entrada do veículo

### 2️⃣ Veículo é Estacionado na Vaga

```bash
curl -X POST "http://localhost:8080/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "event_type": "PARKED",
    "license_plate": "ABC1234",
    "sector": "A",
    "latitude": 10.5,
    "longitude": 20.5
  }'
```

**O sistema:**
- ✅ Associa a vaga ao veículo
- ✅ Marca a vaga como ocupada
- ✅ Atualiza status para PARKED

### 3️⃣ Veículo Sai do Estacionamento

```bash
curl -X POST "http://localhost:8080/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "event_type": "EXIT",
    "license_plate": "ABC1234",
    "sector": "A",
    "latitude": 10.5,
    "longitude": 20.5,
    "exit_time": "2026-04-22T14:45:00"
  }'
```

**O sistema:**
- ✅ Calcula tarifa (30 min grátis + taxa por hora)
- ✅ Libera a vaga
- ✅ Atualiza faturamento

### 4️⃣ Consultar Ocupação

```bash
curl -X GET "http://localhost:8080/api/v1/garage/occupancy" \
  -H "Content-Type: application/json"
```

**Resposta:**
```json
{
  "A": {
    "occupied": 25,
    "available": 75,
    "capacity": 100,
    "occupancyPercent": 25.0
  }
}
```

### 5️⃣ Consultar Receita

```bash
curl -X GET "http://localhost:8080/revenue?date=2025-01-01&sector=A" \
  -H "Content-Type: application/json"
```

**Resposta:**
```json
{
  "amount": 150.50,
  "currency": "BRL",
  "timestamp": "2025-01-01T15:30:45"
}
```

### 6️⃣ Listar Setores

```bash
curl -X GET "http://localhost:8080/api/v1/garage/sectors" \
  -H "Content-Type: application/json"
```

**Resposta:**
```json
[
  {
    "id": 1,
    "name": "A",
    "basePrice": 10.00,
    "maxCapacity": 100,
    "createdAt": "2025-01-01T08:00:00"
  }
]
```

---

## 📁 Estrutura do Projeto

```
.
├── src/
│   ├── main/java/br/com/teste/
│   │   ├── controller/
│   │   │   ├── WebhookController.java          # Manipulador de webhooks
│   │   │   ├── RevenueController.java          # API de receita
│   │   │   └── GarageController.java           # Endpoints de monitoramento
│   │   ├── service/
│   │   │   ├── PricingService.java             # Cálculo de preços dinâmicos
│   │   │   ├── GarageConfigService.java        # Configuração de setores
│   │   │   ├── VehicleEventService.java        # Processamento de eventos
│   │   │   ├── RevenueService.java             # Gerenciamento de receita
│   │   │   └── OccupancyService.java           # Cálculo de ocupação
│   │   ├── repository/
│   │   │   ├── SectorRepository.java
│   │   │   ├── ParkingSpotRepository.java
│   │   │   ├── VehicleRepository.java
│   │   │   └── RevenueRepository.java
│   │   ├── model/
│   │   │   ├── Sector.java                    # Entidade setor
│   │   │   ├── ParkingSpot.java               # Entidade vaga
│   │   │   ├── Vehicle.java                   # Entidade veículo
│   │   │   ├── Revenue.java                   # Entidade receita
│   │   │   └── VehicleStatus.java             # Enum de status
│   │   ├── dto/
│   │   │   ├── VehicleEventRequest.java       # DTO de eventos
│   │   │   ├── RevenueResponse.java           # DTO de resposta
│   │   │   └── ErrorResponse.java             # DTO de erro
│   │   ├── exception/
│   │   │   ├── GarageConfigException.java
│   │   │   ├── SectorFullException.java
│   │   │   ├── VehicleNotFoundException.java
│   │   │   ├── InvalidEventException.java
│   │   │   └── GlobalExceptionHandler.java    # Handler centralizado
│   │   ├── config/
│   │   │   ├── RestClientConfig.java
│   │   │   ├── DataInitializer.java
│   │   │   └── OpenApiConfig.java
│   │   └── GarageManagementApplication.java
│   ├── test/java/br/com/teste/
│   │   ├── service/
│   │   ├── controller/
│   │   └── repository/
│   └── resources/
│       ├── application.yml
│       └── application-test.yml
├── Dockerfile                        # Build multi-estágio
├── docker-compose.yml                # Orquestração completa
├── nginx.conf                        # Configuração do proxy
├── pom.xml                          # Dependências Maven
├── README.md                        # Este arquivo
└── .gitignore
```

---

## 🧪 Testes

### Executar Todos os Testes

```bash
./mvnw clean test
```

### Executar Teste Específico

```bash
./mvnw test -Dtest=PricingServiceTest
```

### Gerar Relatório de Cobertura

```bash
./mvnw test jacoco:report
```

### Testes Inclusos

- **Unit Tests**: PricingService, VehicleEventService
- **Integration Tests**: WebhookController, RevenueController
- **Repository Tests**: VehicleRepository, RevenueRepository

---

## 🔒 Segurança

### Validação de Entrada
- ✅ Jakarta Bean Validation (@NotBlank, @Pattern, @Min/@Max)
- ✅ Validação automática em @Valid
- ✅ Sanitização de entrada

### Tratamento de Erros
- ✅ Mensagens de erro genéricas para o cliente
- ✅ Logs detalhados no servidor
- ✅ Sem exposição de stack traces

### Concorrência
- ✅ Pessimistic locking em operações críticas
- ✅ Optimistic locking em registros de receita
- ✅ Prevenção de race conditions

### Banco de Dados
- ✅ Prepared statements (previne SQL injection)
- ✅ Constraints únicos para integridade
- ✅ Índices otimizados para performance

---

## 📊 Lógica de Negócio

### Cálculo de Preço Dinâmico

Quando um veículo entra:
1. Calcula ocupação atual do setor
2. Aplica multiplicador baseado na ocupação
3. Armazena preço para cobrança posterior

**Tabela de Multiplicadores:**
| Ocupação | Multiplicador | Descrição |
|----------|---------------|-----------|
| < 25% | 0.9x | Desconto (promoção) |
| 25%-50% | 1.0x | Preço base |
| 50%-75% | 1.1x | +10% (demanda) |
| 75%-100% | 1.25x | +25% (alta demanda) |

### Cálculo de Tarifa na Saída

1. Duração = saída - entrada
2. Se duração ≤ 30min: tarifa = 0
3. Se duração > 30min:
   - horas = ceil((duração - 30min) / 60)
   - tarifa = horas × preço_na_entrada

### Agregação de Receita

- Automática ao fim do dia
- Agrupa por setor
- Rastreia volume (quantidade de veículos)
- Usa constraint único (sector_id, date)

---

## 🚨 Tratamento de Erros

### Códigos HTTP

| Código | Significado | Exemplo |
|--------|-----------|---------|
| 200 | ✅ Sucesso | Evento processado |
| 400 | ❌ Entrada inválida | Parâmetros malformados |
| 404 | ❌ Não encontrado | Receita inexistente |
| 409 | ❌ Conflito | Setor cheio |
| 500 | ❌ Erro interno | Erro do servidor |

### Exemplo de Erro

```json
{
  "error": "Sector A is full",
  "timestamp": "2025-01-01T15:30:45",
  "status": 409
}
```

## 🐳 Deploy com Docker

### Build da Imagem

```bash
docker build -t garage-app:1.0.3 .
```

### Executar Container

```bash
docker run -p 8080:8080 \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  garage-app:1.0.3
```

### Docker Compose

```bash
docker-compose up -d      # Iniciar
docker-compose logs -f    # Ver logs
docker-compose down       # Parar
```

---

## ⚙️ Configuração

### Arquivo Principal: `application.yml`

```yaml
spring:
  application:
    name: garage-management
  datasource:
    url: jdbc:mysql://mysql:3306/garage
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

simulator:
  url: http://simulator:3000
  garage-endpoint: /garage

logging:
  level:
    root: INFO
    br.com.teste: DEBUG
```

### Variáveis de Ambiente

```bash
DB_USER=root
DB_PASSWORD=password
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/garage
```
