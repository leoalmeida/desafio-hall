# desafio-hall

[Português](README.md) | [English](README.en.md)

Full-stack application with Angular frontend, Node.js API Gateway, and Spring Boot backend for application, release, approval, and audit management.

## Executive Summary

This repository contains the solution with clear separation between UI, API gateway, and business services.

Main goals:

- provide REST APIs for applications and releases
- integrate frontend, gateway, and backend with stable contracts
- ensure quality through tests and build standards

Main modules:

- `frontend-angular`: Angular SPA
- `gateway-node`: API Gateway / proxy (`/api/*`)
- `service-backend`: Spring Boot REST API
- `postgres`: PostgreSQL database

## Stack

- Java 17
- Spring Boot 3.5.14
- Node.js
- Angular 21
- Docker Compose

## Quick Start

```bash
cd service-backend
mvn clean install
```

```bash
cd ..
docker compose up -d postgres
```

```bash
cd service-backend
mvn spring-boot:run
```

```bash
cd ../gateway-node
npm install
JWT_SECRET=changeit-changeit-changeit-changeit BACKEND_BASE_URL=http://localhost:8081 npm run dev
```

```bash
cd ../frontend-angular
npm install
npm start
```

Expected endpoints:

- backend: `http://localhost:8081`
- gateway: `http://localhost:3000`
- frontend: `http://localhost:4200`

Public API base URL:

- `http://localhost:3000/api`

## Docker

```bash
docker compose up --build
```

Important variables are documented in `.env.example`.

## Quality

Backend:

```bash
cd service-backend
mvn clean test
mvn verify
```

Gateway:

```bash
cd gateway-node
npm run lint
npm test
```

Frontend:

```bash
cd frontend-angular
npm run lint
npm run build
npm run test
```