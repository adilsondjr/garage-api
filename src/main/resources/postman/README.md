# Postman Collection - Garage Management API

Coleção simples do Postman para testes e documentação da API Garage Management System.

## 📁 Arquivos

- **Garage-Management-API.postman_collection.json** - Collection com todos os endpoints
- **README.md** - Este arquivo

## 🚀 Como Importar

### 1. Abrir Postman
```
https://www.postman.com/
```

### 2. Importar Collection
- Clique em "Import" (canto superior esquerdo)
- Selecione o arquivo `Garage-Management-API.postman_collection.json`
- Clique em "Import"

## 📊 Estrutura da Collection

### 1. 📊 Garage Monitoring
Endpoints de monitoramento do estacionamento:
- **Get All Sectors** - Lista todos os setores
- **Get All Parking Spots** - Lista todas as vagas
- **Get Real-time Occupancy** - Ocupação em tempo real

### 2. 💳 Revenue Management
Endpoints de consulta de receita:
- **Get Revenue by Sector and Date** - Consultar receita de um setor em uma data
- **Get Revenue - Invalid Date** - Teste de erro com data inválida

### 3. 🚗 Webhook Events
Endpoints de eventos de estacionamento:
- **Vehicle ENTRY** - Registrar chegada do veículo
- **Vehicle PARKED** - Registrar estacionamento
- **Vehicle EXIT** - Registrar saída do veículo

### 4. 🧪 Error Scenarios
Testes de cenários de erro:
- **Invalid Event Type** - Tipo de evento inválido (erro 400)
- **Missing Required Fields** - Campos obrigatórios faltando (erro 400)

## 🧪 Executando Requests

### Opção 1: Executar Requests Individuais
1. Selecione a requisição desejada na collection
2. Clique em "Send"
3. Verifique a resposta na aba "Body"

### Opção 2: Executar Toda a Collection
1. Clique em "..." próximo ao nome da collection
2. Selecione "Run collection"
3. Clique em "Run Collection"
4. Acompanhe os resultados em tempo real

## 🔄 Ciclo Completo de Teste

Para testar um fluxo completo de um veículo:

### 1. Verificar Estado Inicial
```
GET /api/v1/garage/occupancy
```

### 2. Registrar Entrada
```
POST /webhook
{
  "event_type": "ENTRY",
  "license_plate": "ABC1234",
  "sector": "A",
  "lat": -23.5505,
  "lng": -46.6333
}
```

### 3. Registrar Estacionamento
```
POST /webhook
{
  "event_type": "PARKED",
  "license_plate": "ABC1234",
  "sector": "A",
  "lat": -23.5505,
  "lng": -46.6333
}
```

### 4. Verificar Ocupação Atualizada
```
GET /api/v1/garage/occupancy
```

### 5. Registrar Saída
```
POST /webhook
{
  "event_type": "EXIT",
  "license_plate": "ABC1234",
  "sector": "A",
  "lat": -23.5505,
  "lng": -46.6333
}
```

### 6. Consultar Receita
```
GET /revenue?date=2026-04-20&sector=A
```

## 🐛 Troubleshooting

### Erro: "Could not get any response"
- Verifique se a aplicação está rodando em `http://localhost:8080`
- Teste: `curl http://localhost:8080/api/v1/garage/sectors`

### Erro: "Status 400 - Bad Request"
- Verifique a estrutura do JSON
- Certifique-se que todos os campos obrigatórios estão preenchidos
- Verifique o formato dos valores (datas, coordenadas, etc)

### Erro: "Status 500 - Internal Server Error"
- Verifique os logs da aplicação
- Confirme que os dados existem no banco de dados
- A placa do veículo pode já estar registrada - use uma placa diferente

## 📝 Exemplos de Respostas

### Get Sectors (200 OK)
```json
[
  {
    "id": 1,
    "name": "A",
    "basePrice": 40.50,
    "maxCapacity": 10,
    "createdAt": "2026-04-20T12:07:19"
  }
]
```

### Get Occupancy (200 OK)
```json
{
  "A": {
    "total": 10,
    "percentage": 10.0,
    "available": 9,
    "occupied": 1
  }
}
```

### Get Revenue (200 OK)
```json
{
  "amount": 0.00,
  "currency": "BRL",
  "timestamp": "2026-04-20T12:10:52"
}
```

### Error Response (400 Bad Request)
```json
{
  "error": "Parâmetros de evento inválidos",
  "timestamp": "2026-04-20T12:10:52",
  "status": 400
}
```

## 🔗 Links Úteis

- [Postman](https://www.postman.com/)
- [API Documentation](http://localhost:8080/swagger-ui.html)
- [OpenAPI Spec](http://localhost:8080/v3/api-docs)

## 📝 Notas

- Todas as datas devem estar em formato `YYYY-MM-DD`
- Coordenadas GPS estão em formato decimal
- Não há validação de formato de placa (use qualquer string)
- Latitude e longitude aceitam qualquer valor numérico

---

**Versão:** 2.0.0
**Última Atualização:** 2026-04-20
