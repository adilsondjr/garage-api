# Contribuindo para o Projeto 🤝

Obrigado por seu interesse em contribuir! Este guia ajudará você a entender como contribuir.

---

## Como começar

### 1. Fork o repositório
Clique em "Fork" no GitHub para criar sua própria cópia.

### 2. Clone seu fork
```bash
git clone https://github.com/seu-usuario/garage-api.git
cd garage-api
```

### 3. Crie uma branch de feature
```bash
git checkout -b feature/sua-feature-aqui
```

### 4. Faça suas mudanças
Edite os arquivos e implemente sua feature.

### 5. Commit suas mudanças
```bash
git commit -m "feat: descrição breve da mudança"
```

### 6. Push para seu fork
```bash
git push origin feature/sua-feature-aqui
```

### 7. Crie um Pull Request
Vá para o GitHub e clique em "New Pull Request".

---

## Diretrizes de código

### Estilo de código
- Seguir [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Nomes descritivos para variáveis e métodos
- Métodos pequenos e focados
- Máximo 120 caracteres por linha

### Exemplo bom:
```java
@Service
public class PricingService {
    public BigDecimal calculateDynamicPrice(int occupancyPercent, BigDecimal basePrice) {
        if (occupancyPercent < 25) {
            return basePrice.multiply(BigDecimal.valueOf(0.9));
        }
        // ...
        return basePrice;
    }
}
```

---

## Commits

### Convenção de commit
Usar o formato Conventional Commits:

```
type(scope): subject

body

footer
```

### Tipos de commit:
- **feat**: Nova feature
- **fix**: Correção de bug
- **docs**: Mudança em documentação
- **style**: Formatação, sem mudanças de lógica
- **refactor**: Refatoração
- **test**: Adição ou modificação de testes
- **chore**: Build, dependencies, etc

### Exemplos:
```bash
git commit -m "feat(pricing): adicionar cálculo de preço dinâmico"
git commit -m "fix(webhook): corrigir validação de license_plate"
git commit -m "docs: atualizar README com novos endpoints"
git commit -m "test(revenue): adicionar testes unitários para RevenueService"
```

---

## Testes

### Executar todos os testes
```bash
./mvnw test
```

### Executar teste específico
```bash
./mvnw test -Dtest=PricingServiceTest
```

### Gerar relatório de cobertura
```bash
./mvnw test jacoco:report
```

### Requisitos para PR:
- ✅ Todos os testes passando
- ✅ Mínimo 80% de cobertura
- ✅ Testes para cenários positivos e negativos

---

## Pull Request

### Checklist antes de enviar:
- [ ] Código segue o estilo do projeto
- [ ] Testes executam sem erros
- [ ] Documentação atualizada
- [ ] Sem arquivos desnecessários adicionados
- [ ] Commits têm mensagens claras

### Template de PR:
```markdown
## Descrição
Descreva as mudanças que fez.

## Tipo de mudança
- [ ] Nova feature
- [ ] Correção de bug
- [ ] Refatoração
- [ ] Documentação

## Como testar
Explique como testar a mudança.

## Screenshots (se aplicável)
Adicione imagens ou logs relevantes.

## Checklist
- [ ] Meu código segue o estilo do projeto
- [ ] Revisei meu próprio código
- [ ] Adicionei testes adequados
- [ ] Adicionei documentação necessária
```

---

## Reportar Issues

### Bug Report
Inclua:
- Descrição clara do problema
- Passos para reproduzir
- Comportamento esperado vs observado
- Ambiente (OS, Java version, etc)
- Logs relevantes

### Feature Request
Inclua:
- Descrição do caso de uso
- Abordagem proposta
- Exemplos de como seria usado

---

## Estrutura do Projeto

```
src/
├── main/java/br/com/teste/
│   ├── controller/      # REST endpoints
│   ├── service/         # Lógica de negócio
│   ├── repository/      # Acesso a dados
│   ├── model/           # Entidades JPA
│   ├── dto/             # Transfer objects
│   ├── exception/       # Exceções custom
│   └── config/          # Configurações
└── test/java/br/com/teste/
    ├── service/         # Testes de serviço
    ├── controller/      # Testes de controller
    └── repository/      # Testes de repository
```

---

## Tarefas comuns

### Adicionar novo endpoint
1. Criar Controller em `controller/`
2. Criar Service em `service/`
3. Criar DTOs em `dto/`
4. Criar testes
5. Documentar em README.md

### Modificar entidade do banco
1. Atualizar class em `model/`
2. Hibernate cuidará das migrações
3. Atualizar testes

### Adicionar dependência
1. Editar `pom.xml`
2. Executar `./mvnw dependency:resolve`

---

## Boas práticas

✅ **Faça:**
- Commits pequenos e focados
- Testes para cada mudança
- Documentação clara
- Código legível e bem formatado
- Seguir padrões do projeto

❌ **Não faça:**
- Commits gigantes misturando várias features
- Pull requests sem testes
- Código com warnings
- Commits com mensagens genéricas ("fix", "update")
- Mudar estilo de código existente sem razão

---

## Código de Conduta

- Seja respeitoso com outros contribuidores
- Aceite críticas construtivas
- Foque em resolver problemas, não em pessoas
- Ajude outros a aprender e crescer

---

## Dúvidas?

Sinta-se à vontade para:
- Abrir uma issue com `[QUESTION]` no título
- Comentar em PRs abertos
- Fazer uma discussão no GitHub

---

**Obrigado por contribuir! Sua ajuda é valiosa! 🙏**
