# Arquitetura C4 — desafio-hall

Diagramas de arquitetura seguindo o [modelo C4](https://c4model.com/) e usando [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML) para design e exportação das imagens.

## Níveis

| Arquivo | Nível | Descrição |
|---|---|---|
| [c1-context.puml](c1-context.puml) | Nível 1 — Contexto | Sistema e atores externos |
| [c2-container.puml](c2-container.puml) | Nível 2 — Container | Blocos de execução do sistema |
| [c3-component-backend.puml](c3-component-backend.puml) | Nível 3 — Componente | Internos do service-backend |
| [c4-deployment.puml](c4-deployment.puml) | Nível 4 — Implantação | Docker Compose / Kubernetes |
| [saga-release-promotion.puml](saga-release-promotion.puml) | SAGA (Orquestração) | Fluxo completo de promoção DEV → PRE-PROD → PROD com compensações |

## Como visualizar

### Imagem
Abra o arquivo `.png` com a imagem exportada pelo plantuml.

### VS Code (PUML) 
Instale a extensão **PlantUML** (`jebbs.plantuml`) e pressione `Alt+D` em qualquer arquivo `.puml`.

### Online (PUML)
Cole o conteúdo do arquivo em [Plantuml Online](https://www.plantuml.com/plantuml/uml/).

## Visão geral do sistema

```
Usuário (Browser)
      |
 [Frontend Angular SPA]  :4200
      |
 [API Gateway Node.js]   :3000
      |    |
      |    +-- /healthcheck, /metrics, /swagger-ui
      |
 [service-backend (Spring Boot)]  :8081
      |
 [PostgreSQL 13]  :5432
```
