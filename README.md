# desafio-hall

[Português](README.md) | [English](README.en.md)

Aplicacao fullstack com frontend Angular, API Gateway Node.js e backend Spring Boot para gestao de aplicacoes, releases, aprovacoes e auditoria.

## Resumo Executivo

Este repositorio concentra a solucao com separacao clara entre interface, API e servicos de negocio.

Objetivos do projeto:

- oferecer APIs REST para operacoes de aplicacoes e releases
- integrar frontend, gateway e backend com contratos estaveis
- garantir qualidade por testes e padroes de build

Escopo principal:

- `frontend-angular`: SPA Angular
- `gateway-node`: Camada de entrada e roteamento
- `service-backend`: API REST Spring Boot
- `postgres`: Banco de dados PostgreSQL

Arquitetura resumida:

Frontend
    |
Api Gateway
    |
Backend REST
    |
Persistencia

## Stack

- Java 17
- Spring Boot 3.5.14
- Node.js
- Angular 21
- Docker Compose

## Sumario

- [Resumo Executivo](#resumo-executivo)
- [Stack](#stack)
- [Requisitos](#requisitos)
- [Estrutura](#estrutura)
- [Configuracao](#configuracao)
- [Como Rodar Rapido](#como-rodar-rapido)
- [API](#api)
- [Docker](#docker)
- [Testes e Qualidade](#testes-e-qualidade)
- [Troubleshooting](#troubleshooting)

## Requisitos

- Java 17
- Maven 3.8+
- Node.js 20+ e npm
- Docker e Docker Compose (opcional)

## Estrutura

```text
desafio-hall/
    service-backend/
    gateway-node/
    frontend-angular/
    deployment/
    docker-compose.yaml
```

## Configuracao

### service-backend

Configuracao padrao em `service-backend/src/main/resources/application.properties`:

- `server.port=${SPRING_LOCAL_PORT:8081}`
- `spring.datasource.url=jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:mydb}`
- `spring.datasource.username=${DB_USERNAME:postgres}`
- `spring.datasource.password=${DB_PASSWORD:postgres}`
- `security.jwt.secret=${JWT_SECRET:changeit-changeit-changeit-changeit}`

Recursos de observabilidade e documentacao:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`
- Actuator: `http://localhost:8081/actuator`

### frontend-angular

Ambiente de desenvolvimento aponta para:

- `/api/applications`
- `/api/releases`
- `/api/approvals`
- `/api/audit`
- `/api/auth`

Script local sobe em:

- `http://localhost:4200`

Scripts principais em `frontend-angular/package.json`:

- `npm start` (ng serve na porta 4200)
- `npm run build`
- `npm run test`
- `npm run lint`

### gateway-node

O `gateway-node` atua como ponto unico de entrada para o frontend e expõe ` /api/* ` para o navegador.

Responsabilidades principais:

- encaminhar chamadas do frontend para o `service-backend`
- validar token Bearer nas rotas protegidas
- aplicar autorizacao por roles (`admin`, `approver`, `viewer`)
- validar payload e `content-type` nas entradas HTTP
- manter rotas publicas de autenticacao
- padronizar tratamento de erros com contrato `{ code, message, details }`
- gerar logs estruturados com `requestId` e `correlationId`
- expor metricas simples por rota em `/metrics`

Configuracao principal em `gateway-node/config/default.json` e variaveis de ambiente:

- `PORT` ou `server.port` para a porta do gateway
- `BACKEND_BASE_URL` para o endereco interno/externo do backend
- `JWT_SECRET` para validar o mesmo token emitido pelo backend

Diagramas C4 atualizados da arquitetura:

- `docs/architecture/context-diagram.puml`
- `docs/architecture/container-diagram.puml`
- `docs/architecture/deployment-diagram.puml`
- `docs/architecture/saga-pattern.puml`
- `docs/architecture/component-diagram-gateway.puml`

## Como Rodar Rapido

### Subida local essencial

1. build Java na raiz
2. subir banco de dados
3. subir service-backend
4. subir gateway-node
5. subir frontend-angular
6. validar endpoints e fluxo principal

### 1. Build do service-backend

```bash
cd service-backend
mvn clean install
```

### 2. Subir PostgreSQL (opcional via Docker)

```bash
cd ..
docker compose up -d postgres
```

### 3. Subir service-backend

```bash
cd service-backend
mvn spring-boot:run
```

### 4. Subir gateway-node

```bash
cd ../gateway-node
npm install
JWT_SECRET=changeit-changeit-changeit-changeit BACKEND_BASE_URL=http://localhost:8081 npm run dev
```

### 5. Subir frontend-angular

```bash
cd ../frontend-angular
npm install
npm start
```

Recursos do service-backend:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`

### Resultado esperado

- service-backend disponivel em `http://localhost:8081`
- gateway-node disponivel em `http://localhost:3000`
- frontend-angular disponivel em `http://localhost:4200`
- frontend consumindo `/api/*` (proxy para o gateway)

## API

Base URL publica: `http://localhost:3000/api`

O gateway encaminha as chamadas para o `service-backend` em `http://localhost:8081/api`.

- `POST /auth/login`
- `GET /healthcheck/`
- `GET /metrics`
- `GET /applications`
- `POST /applications`
- `PUT /applications/{id}`
- `PATCH /applications/{id}`
- `GET /releases`
- `POST /releases`
- `POST /releases/{id}/approve`
- `POST /releases/{id}/disapprove`
- `POST /releases/{id}/promote`
- `GET /approvals`
- `GET /approvals/approver?aprovador=...`
- `GET /approvals/release?releaseId=...`
- `GET /approvals/outcome?outcome=...`
- `GET /audit`
- `GET /audit/actor?ator=...`
- `GET /audit/action?acao=...`
- `GET /audit/entity?entidade=...`
- `GET /audit/interval?dataInicio=...&dataFim=...`
- `POST /audit`

Exemplo de login:

```json
{
    "email": "admin@example.com",
    "password": "senha123"
}
```

## Docker

Existe `docker-compose.yaml` na raiz para subir service-backend, gateway-node, frontend-angular e postgres.

```bash
cd desafio-hall
docker compose up --build
```

O compose usa variaveis de ambiente como:

- `SPRING_LOCAL_PORT`, `SPRING_DOCKER_PORT`
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `FRONTEND_LOCAL_PORT`, `FRONTEND_DOCKER_PORT`
- `API_GATEWAY_LOCAL_PORT`, `API_GATEWAY_DOCKER_PORT`
- `JWT_SECRET`

No ambiente Docker:

- o frontend acessa o gateway pela porta publicada em `API_GATEWAY_LOCAL_PORT`
- o gateway acessa o backend internamente via `BACKEND_BASE_URL=http://service-backend:${SPRING_DOCKER_PORT}`
- o backend acessa o Postgres pelo hostname `postgres`

## Testes e Qualidade

service-backend:

```bash
cd service-backend
mvn clean test
```

Para rodar verificacoes de qualidade no backend:

```bash
cd service-backend
mvn verify
```

frontend-angular:

```bash
cd frontend-angular
npm run test
npm run lint
npm run test:ci
```

No service-backend estao configurados plugins como Jacoco, Checkstyle, PMD, SpotBugs e Spotless.

gateway-node:

```bash
cd gateway-node
npm run lint
npm test
```

## Troubleshooting

- Frontend nao conecta no gateway: confirme gateway-node ativo em `http://localhost:3000`.
- Gateway nao conecta no backend: confirme `BACKEND_BASE_URL` apontando para `http://localhost:8081` no ambiente local ou `http://service-backend:${SPRING_DOCKER_PORT}` no Docker.
- Token rejeitado no gateway: valide se `JWT_SECRET` no gateway e no backend possuem o mesmo valor.
- Porta ocupada: ajuste porta do Angular em `frontend-angular/package.json` ou `SPRING_LOCAL_PORT` no backend.
- Erro de variavel no Docker Compose: confira variaveis obrigatorias no shell ou `.env`.
- Falha de build Java: valide `java -version` com Java 17.
