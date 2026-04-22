# Primeiros Passos 🚀

Guia passo a passo para começar com o Sistema de Gerenciamento de Estacionamento.

---

## Pré-requisitos

Certifique-se de ter instalado:

- **Java 21+** - Download: [java.oracle.com](https://www.oracle.com/java/)
- **Maven 3.9+** - Download: [maven.apache.org](https://maven.apache.org/)
- **Docker & Docker Compose** - Download: [docker.com](https://www.docker.com/)
- **Git** - Download: [git-scm.com](https://git-scm.com/)

### Verificar instalação:
```bash
java -version
mvn -version
docker -version
docker-compose -version
```

---

## Opção 1: Docker Compose (Recomendado) ✅

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/garage-api.git
cd garage-api
```

### 2. Inicie os serviços
```bash
docker-compose up -d
```

### 3. Aguarde a inicialização
```bash
sleep 40  # Espere 40 segundos
```

### 4. Verifique o status
```bash
docker ps
```

Você deve ver 4 containers rodando:
- `garage-app-spring-boot` (status: healthy)
- `garage-mysql` (status: healthy)
- `garage-simulator`
- `garage-proxy`

### 5. Teste um endpoint
```bash
curl http://localhost:8080/api/v1/garage/sectors
```

---

## Opção 2: Desenvolvimento Local

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/garage-api.git
cd garage-api
```

### 2. Crie o banco de dados
```bash
mysql -u root -p
CREATE DATABASE garage;
EXIT;
```

### 3. Inicie a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

---

## Opção 3: Build com Maven

### 1. Construa o projeto
```bash
./mvnw clean package
```

### 2. Execute o JAR
```bash
java -jar target/garage-app-1.0.3.jar
```

---

## Verificar a instalação

### Acessar Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Listar setores
```bash
curl http://localhost:8080/api/v1/garage/sectors
```

Resposta esperada:
```json
[
  {
    "id": 1,
    "name": "A",
    "basePrice": 40.50,
    "maxCapacity": 10
  }
]
```

---

## Próximos passos

1. **Leia a documentação**: Veja [README.md](README.md)
2. **Teste os endpoints**: Use [Swagger UI](http://localhost:8080/swagger-ui.html)
3. **Explore o código**: Abra em sua IDE favorita
4. **Execute os testes**: `./mvnw test`

---

## Troubleshooting

### Porta 8080 já está em uso?
```bash
# Altere a porta em application.yml ou:
docker-compose down
docker-compose up -d
```

### Erro de conexão com MySQL?
```bash
# Verifique se MySQL está saudável:
docker logs garage-mysql
```

### Spring Boot não inicia?
```bash
# Verifique os logs:
docker logs -f garage-app-spring-boot
```

---

## Comandos úteis

```bash
# Ver logs da aplicação:
docker logs -f garage-app-spring-boot

# Entrar no container:
docker exec -it garage-app-spring-boot bash

# Parar tudo:
docker-compose down

# Remover volumes (dados):
docker-compose down -v

# Reconstruir:
docker-compose build --no-cache
```

---

**Pronto! Você tem tudo configurado! 🎉**

Para maiores informações, veja [README.md](README.md) ou [CONTRIBUINDO.md](CONTRIBUINDO.md).
