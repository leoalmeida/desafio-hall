# desafio-hall

[Português](README.md) | [English](README.en.md)

## Contexto

A Aurora GeoEnergy centraliza releases em DEV → PRE‐PROD → PROD. Evidências e aprovações estão dispersas e houve falha por falta de evidência e aprovação formal. A diretoria quer um sistema único, auditável, com regras claras e políticas configuráveis.

Para essa implementação será utilizadada uma aplicacao fullstack com frontend Angular, API Gateway Node.js e backend Spring Boot para gestao de aplicacoes, releases, aprovacoes e auditoria.

## Arquitetura

* Frontend Angular SPA
* API Gateway Node.js como única entrada do frontend e implementando responsabilidades transversais (auth/roles,
validação, observabilidade e padronização de erros).
* Backend Service utilizando Java e Spring Boot
* Banco PostgreSQL

## Requisitos base

* Autenticação por token, com roles: admin / approver / viewer.
* Erros padronizados em JSON: { code, message, details }.
* Logs estruturados com requestId/correlationId (propagar do Gateway para o backend).
* Métricas simples (contadores por rota e/ou tempo de request).
* Swagger/OpenAPI no Gateway (obrigatório) e no backend (recomendado).

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
- `security.jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}`
- `security.jwt.algorithm=${JWT_ALGORITHM:HS256}`
- `policy.file.path=${POLICY_FILE_PATH:policy.json}`
- `logging.level.root=${GLOBAL_LOG_LEVEL:INFO}`
- `logging.level.com.example.backend=${BACKEND_LOG_LEVEL:INFO}`

Migrations Flyway iniciais em `service-backend/src/main/resources/db/migration`:

- `V1_1_0__create_application_table.sql`: cria `APPLICATION`
- `V1_1_1__create_release_table.sql`: cria `RELEASE`
- `V1_1_2__create_approval_table.sql`: cria `APPROVAL`
- `V1_1_3__create_audit_log_table.sql`: cria `AUDITLOG`
- `V1_1_4__create_auth_tables.sql`: cria `APP_USER` (user management and roles)
- `V1_1_5__add_release_unique_constraint.sql`: adiciona constraint UNIQUE em RELEASE
- `V1_1_6__create_idempotency_record_table.sql`: cria `IDEMPOTENCY_RECORD` (request deduplication)
- `V1_1_7__insert_app_initial.sql`: insere dados iniciais de aplicacao

As entidades principais do backend (`APPLICATION`, `RELEASE`, `APPROVAL`, `AUDITLOG` e `IDEMPOTENCY_RECORD`) usam `UUID` como identificador primário. Filtros e path params como `applicationId`, `releaseId` e `{id}` seguem esse mesmo formato.

Recursos de observabilidade e documentacao:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`
- Gateway Swagger UI: `http://localhost:3000/swagger-ui`
- Gateway OpenAPI JSON: `http://localhost:3000/api-docs-json`
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
- aplicar autorizacao por roles (`admin`, `approver`, `viewer`) de acordo com as regras da API
- validar payload e `content-type` nas entradas HTTP
- manter rotas publicas de autenticacao
- padronizar tratamento de erros com contrato `{ code, message, details }`
- gerar logs estruturados com `requestId` e `correlationId`
- expor metricas simples por rota em `/metrics`

Configuracao principal em `gateway-node/config/default.json` e variaveis de ambiente:

- `PORT` ou `server.port` para a porta do gateway
- `BACKEND_BASE_URL` para o endereco interno/externo do backend
- `JWT_SECRET` para validar o mesmo token emitido pelo backend

### policy-as-code

O backend carrega a policy JSON em runtime com a seguinte ordem de precedencia:

1. caminho definido em `POLICY_FILE_PATH`
2. arquivo `../policy.json` (raiz do repositorio em execucao local)
3. arquivo `policy.json` no classpath (`service-backend/src/main/resources/policy.json`)

Campos aplicados pela policy:

- `minApprovals`: minimo de aprovacoes com outcome `APPROVED` para permitir aprovacao de release
- `minScore`: score minimo para aprovacao, calculado como percentual de aprovacoes `APPROVED` sobre total de aprovacoes da release
- `freezeWindows`: bloqueio de aprovacao/promocao por ambiente e janela de horario

Diagramas C4 atualizados da arquitetura (em `docs/architecture/`):

- `c1-context.puml` - System context with actors and external systems
- `c2-container.puml` - Container decomposition (Frontend, Gateway, Backend, Database)
- `c3-component-backend.puml` - Component-level details of Spring Boot backend
- `c4-deployment.puml` - Deployment topology (Docker Compose and Kubernetes)
- `saga-release-promotion.puml` - SAGA pattern for multi-stage release promotion
- `cicd-pipeline.puml` - CI/CD pipeline stages and quality gates

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
- Gateway Swagger UI: `http://localhost:3000/swagger-ui`
- Gateway OpenAPI JSON: `http://localhost:3000/api-docs-json`

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
- `GET /api-docs-json`
- `GET /swagger-ui`
- `GET /applications`
- `POST /applications`
- `PUT /applications/{id}`
- `PATCH /applications/{id}`
- `GET /releases`
- `POST /releases`
- `POST /releases/{id}/approve`
- `POST /releases/{id}/disapprove`
- `POST /releases/{id}/promote`
- `PATCH /releases/{id}/evidence-url`
- `GET /releases/{id}/evidence-score`

Observações do endpoint de promoção:

- suporta header `Idempotency-Key` para deduplicação de reenvios
- reenvio com mesma chave já concluída retorna o mesmo efeito sem duplicar auditoria
- chave em processamento ou conflito concorrente retorna `409`
- `GET /approvals`
- `GET /approvals/approver?aprovador=...`
- `GET /approvals/release?releaseId=<uuid>`
- `GET /approvals/outcome?outcome=...`
- `GET /audit`
- `GET /audit?ator=...`
- `GET /audit?acao=...`
- `GET /audit?entidade=...`
- `GET /audit?dataInicio=...&dataFim=...`
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

## Matriz de Acesso (Roles x Endpoints)

Permissões efetivas considerando Gateway + Backend:

| Endpoint | Metodo | admin | approver | viewer |
| --- | --- | --- | --- | --- |
| `/api/auth/login` | `POST` | Sim (publico) | Sim (publico) | Sim (publico) |
| `/healthcheck` | `GET` | Sim (publico) | Sim (publico) | Sim (publico) |
| `/metrics` | `GET` | Sim (publico) | Sim (publico) | Sim (publico) |
| `/api-docs-json` | `GET` | Sim (publico) | Sim (publico) | Sim (publico) |
| `/swagger-ui` | `GET` | Sim (publico) | Sim (publico) | Sim (publico) |
| `/api/applications` | `GET` | Sim | Sim | Sim |
| `/api/applications` | `POST` | Sim | Nao | Nao |
| `/api/applications/{id}` | `PUT/PATCH` | Sim | Nao | Nao |
| `/api/releases` | `GET` | Sim | Sim | Sim |
| `/api/releases` | `POST` | Sim | Nao | Nao |
| `/api/releases/{id}/approve` | `POST` | Sim | Sim | Nao |
| `/api/releases/{id}/disapprove` | `POST` | Sim | Sim | Nao |
| `/api/releases/{id}/promote` | `POST` | Sim | Nao | Nao |
| `/api/releases/{id}/evidence-url` | `PATCH` | Sim | Nao | Nao |
| `/api/releases/{id}/evidence-score` | `GET` | Sim | Sim | Sim |
| `/api/approvals` e filtros | `GET` | Sim | Sim | Nao |
| `/api/audit` e filtros | `GET/POST` | Sim | Nao | Nao |
| `/api/users` | `GET/POST/PUT/DELETE` | Sim | Nao | Nao |

Observações de auditoria:

- ações relevantes auditadas: `CREATE`, `APPROVE`, `DISAPPROVE`, `PROMOTE`, `CHANGE_EVIDENCE_URL`
- `GET /api/audit` aceita filtros opcionais por query string: `ator`, `acao`, `entidade`, `dataInicio`, `dataFim`

## Docker

Existe `docker-compose.yaml` na raiz para subir service-backend, gateway-node, frontend-angular e postgres.

```bash
cd desafio-hall
docker compose up --build
```

O compose usa variaveis de ambiente como (definidas em `.env`):

- `SPRING_LOCAL_PORT=8081`, `SPRING_DOCKER_PORT=8081`
- `DB_HOST=localhost`, `DB_PORT=5432`, `DB_NAME=desafio_hall`, `DB_USERNAME=postgres`, `DB_PASSWORD=postgres`
- `FRONTEND_LOCAL_PORT=80`, `FRONTEND_DOCKER_PORT=4200`
- `API_GATEWAY_LOCAL_PORT=3000`, `API_GATEWAY_DOCKER_PORT=3000`
- `JWT_SECRET=your_jwt_secret_key`, `JWT_ALGORITHM=HS256`, `JWT_EXPIRATION_MS=3600000`
- `TIMEZONE=America/Sao_Paulo`, `LOG_LEVEL=info`

No ambiente Docker:

- o frontend acessa o gateway pela porta publicada em `API_GATEWAY_LOCAL_PORT` (exposta via `API_GATEWAY_DOCKER_PORT`)
- o gateway acessa o backend internamente via `http://service-backend:${SPRING_DOCKER_PORT}`
- o backend acessa o Postgres pelo hostname `postgres`

Nota: Certifique-se de definir as variáveis de ambiente em `.env` ou defina-as diretamente no comando `docker compose up`.

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

### Testes de policy-as-code

Cobertura das regras principais:

- `minApprovals` (bloqueio quando aprovações válidas são menores que o mínimo)
- `minScore` (bloqueio quando score percentual está abaixo do mínimo)
- `freezeWindows` por ambiente (bloqueio em janela ativa)
- aplicação da policy no fluxo de `approveRelease` e `promoteRelease`

Classes de teste:

- `service-backend/src/test/java/com/example/backend/policy/PolicyServiceTest.java`
- `service-backend/src/test/java/com/example/backend/service/ReleaseServiceImplTest.java`

Execução focada:

```bash
cd service-backend
mvn -Dtest=PolicyServiceTest,ReleaseServiceImplTest test
```

## Evidence Scoring

Endpoint de score determinístico de evidência:

- `GET /api/releases/{id}/evidence-score`

Regras de pontuação (0..100):

- URL de evidência válida (`http/https` com host): `+40`
- URL contém padrão de relatório (`report`, `evidence`, `quality`, `test`, `coverage`): `+20`
- URL contém indicador de sucesso (`PASS`, `passed`, `result=pass`, `status=pass`): `+20`
- Peso por status da release:
- `DEPLOYED`, `APPROVED_PROD`, `APPROVED_PREPROD`: `+10`
- `PENDING_PROD`, `PENDING_PREPROD`: `+5`
- demais status: `+0`
- Peso por ambiente:
- `PROD`: `+10`
- `PREPROD`: `+7`
- `DEV`: `+5`

O score final é normalizado com limite entre `0` e `100`.

## Troubleshooting

- Frontend nao conecta no gateway: confirme gateway-node ativo em `http://localhost:3000`.
- Gateway nao conecta no backend: confirme `BACKEND_BASE_URL` apontando para `http://localhost:8081` no ambiente local ou `http://service-backend:${SPRING_DOCKER_PORT}` no Docker.
- Token rejeitado no gateway: valide se `JWT_SECRET` no gateway e no backend possuem o mesmo valor.
- Porta ocupada: ajuste porta do Angular em `frontend-angular/package.json` ou `SPRING_LOCAL_PORT` no backend.
- Erro de variavel no Docker Compose: confira variaveis obrigatorias no shell ou `.env`.
- Falha de build Java: valide `java -version` com Java 17.
