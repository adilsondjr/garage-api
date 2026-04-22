# Leia Primeiro! 📖

## Bem-vindo ao Sistema de Gerenciamento de Estacionamento

Bem-vindo! Este documento ajudará você a entender rapidamente o que é este projeto e como começar.

---

## ❓ O que é este projeto?

Este é um **sistema backend profissional** para gerenciar estacionamentos com:

- 💰 **Preços dinâmicos** que ajustam automaticamente baseado na ocupação
- 📍 **Rastreamento de veículos** em tempo real (entrada, estacionamento, saída)
- 📊 **Cálculo automático de receita** por dia e setor
- 🔌 **API REST completa** com documentação interativa (Swagger)

---

## 🚀 Comece em 3 passos

### 1. Inicie os serviços
```bash
docker-compose up -d
sleep 40  # Aguarde inicialização
```

### 2. Acesse a documentação interativa
```
http://localhost:8080/swagger-ui.html
```

### 3. Teste um endpoint
```bash
curl http://localhost:8080/api/v1/garage/sectors
```

---

## 📋 O que você encontrará

| Arquivo | Descrição |
|---------|-----------|
| **README.md** | 📘 Documentação completa do projeto |
| **PRIMEIROS_PASSOS.md** | 🎯 Guia de setup e instalação |
| **CONTRIBUINDO.md** | 🤝 Como contribuir com o projeto |
| **nginx.conf** | ⚙️ Configuração do proxy |
| **docker-compose.yml** | 🐳 Orquestração dos containers |

---

## 🔍 Endpoints principais

### Consultar setores
```bash
curl http://localhost:8080/api/v1/garage/sectors
```

### Verificar ocupação
```bash
curl http://localhost:8080/api/v1/garage/occupancy
```

### Registrar evento (webhooks)
```bash
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -d '{"event_type":"ENTRY","license_plate":"ABC1234","sector":"A","lat":10.5,"lng":20.5}'
```

### Consultar receita
```bash
curl "http://localhost:8080/revenue?date=2026-04-20&sector=A"
```

---

## 💡 Conceitos importantes

### Preço Dinâmico
O sistema ajusta os preços automaticamente:
- **< 25% ocupação**: -10% (promoção)
- **25-50% ocupação**: preço base
- **50-75% ocupação**: +10%
- **> 75% ocupação**: +25%

### Cálculo de Tarifa
- **Primeiros 30 minutos**: GRATUITO
- **Após 30 minutos**: preço_por_hora × horas_arredondadas

### Estados de um Veículo
1. **ENTRY**: Veículo chega no estacionamento
2. **PARKED**: Veículo é alocado em uma vaga
3. **EXIT**: Veículo sai do estacionamento

---

## 🆘 Próximas etapas

1. **Aprenda** sobre a arquitetura em [README.md](README.md)
2. **Configure** o ambiente com [PRIMEIROS_PASSOS.md](PRIMEIROS_PASSOS.md)
3. **Contribua** seguindo [CONTRIBUINDO.md](CONTRIBUINDO.md)

---

## 📞 Precisa de ajuda?

- 📖 Veja a documentação: `README.md`
- 🔧 Configure tudo: `PRIMEIROS_PASSOS.md`
- 💬 Participe: `CONTRIBUINDO.md`

---

**Aproveite! 🎉**
