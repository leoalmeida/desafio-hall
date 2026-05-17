const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");
const express = require("express");
const jwt = require("jsonwebtoken");
const request = require("supertest");

const JWT_SECRET = "test-secret-key";

function createToken(role) {
  return jwt.sign(
    {
      sub: "user@email.com",
      email: "user@email.com",
      name: "User",
      role
    },
    JWT_SECRET,
    { expiresIn: "1h" }
  );
}

async function startBackendStub() {
  const backendApp = express();
  backendApp.use(express.json());

  backendApp.get("/api/releases", (req, res) => {
    res.status(200).json({
      ok: true,
      route: "releases-get",
      query: req.query,
      headers: {
        requestId: req.headers["x-request-id"],
        correlationId: req.headers["x-correlation-id"]
      }
    });
  });

  backendApp.post("/api/releases", (req, res) => {
    res.status(201).json({ ok: true, route: "releases-post", body: req.body });
  });

  backendApp.post("/api/releases/:id/approve", (_, res) => {
    res.status(204).send();
  });

  backendApp.post("/api/applications", (req, res) => {
    res.status(201).json({ ok: true, route: "applications-post", body: req.body });
  });

  backendApp.get("/api/audit", (_, res) => {
    res.status(200).json({ ok: true, route: "audit-get" });
  });

  const server = http.createServer(backendApp);

  await new Promise((resolve) => {
    server.listen(0, "127.0.0.1", resolve);
  });

  const address = server.address();
  const port = typeof address === "object" && address ? address.port : 0;

  return {
    server,
    baseUrl: `http://127.0.0.1:${port}`
  };
}

test("GET /healthcheck is public", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const response = await request(app).get("/healthcheck");

  assert.equal(response.status, 200);
  assert.equal(response.body.status, "ok");
});

test("protected route requires bearer token", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const response = await request(app).get("/api/releases?applicationId=1&version=1.0&environment=DEV&status=PENDING");

  assert.equal(response.status, 401);
  assert.equal(response.body.code, "AUTH_REQUIRED");
});

test("releases GET allows VIEWER and proxies upstream", async () => {
  const stub = await startBackendStub();

  try {
    const { createApp } = require("./app");
    process.env.JWT_SECRET = JWT_SECRET;
    process.env.BACKEND_BASE_URL = stub.baseUrl;

    const { app } = createApp();
    const token = createToken("VIEWER");

    const response = await request(app)
      .get("/api/releases?applicationId=1&version=1.0&environment=DEV&status=PENDING")
      .set("x-request-id", "req-123")
      .set("x-correlation-id", "corr-456")
      .set("Authorization", `Bearer ${token}`);

    assert.equal(response.status, 200);
    assert.equal(response.body.route, "releases-get");
    assert.equal(response.body.headers.requestId, "req-123");
    assert.equal(response.body.headers.correlationId, "corr-456");
    assert.equal(response.headers["x-request-id"], "req-123");
    assert.equal(response.headers["x-correlation-id"], "corr-456");
  } finally {
    await new Promise((resolve, reject) => {
      stub.server.close((error) => (error ? reject(error) : resolve()));
    });
  }
});

test("releases GET validates required query params", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const token = createToken("VIEWER");

  const response = await request(app)
    .get("/api/releases?applicationId=1")
    .set("Authorization", `Bearer ${token}`);

  assert.equal(response.status, 400);
  assert.equal(response.body.code, "VALIDATION_ERROR");
});

test("applications POST requires JSON object body", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const token = createToken("VIEWER");

  const response = await request(app)
    .post("/api/applications")
    .set("Authorization", `Bearer ${token}`)
    .set("Content-Type", "application/json")
    .send(["invalid"]);

  assert.equal(response.status, 400);
  assert.equal(response.body.code, "VALIDATION_ERROR");
});

test("releases POST forbids VIEWER role", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const token = createToken("VIEWER");

  const response = await request(app)
    .post("/api/releases")
    .set("Authorization", `Bearer ${token}`)
    .set("Content-Type", "application/json")
    .send({ test: true });

  assert.equal(response.status, 403);
  assert.equal(response.body.code, "FORBIDDEN");
});

test("audit endpoints require ADMIN role", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const token = createToken("APPROVER");

  const response = await request(app)
    .get("/api/audit")
    .set("Authorization", `Bearer ${token}`);

  assert.equal(response.status, 403);
  assert.equal(response.body.code, "FORBIDDEN");
});

test("applications POST allows authenticated ADMIN", async () => {
  const stub = await startBackendStub();

  try {
    const { createApp } = require("./app");
    process.env.JWT_SECRET = JWT_SECRET;
    process.env.BACKEND_BASE_URL = stub.baseUrl;

    const { app } = createApp();
    const token = createToken("ADMIN");

    const response = await request(app)
      .post("/api/applications")
      .set("Authorization", `Bearer ${token}`)
      .set("Content-Type", "application/json")
      .send({ name: "App Gateway Test" });

    assert.equal(response.status, 201);
    assert.equal(response.body.route, "applications-post");
    assert.equal(response.body.body.name, "App Gateway Test");
  } finally {
    await new Promise((resolve, reject) => {
      stub.server.close((error) => (error ? reject(error) : resolve()));
    });
  }
});

test("release approval allows APPROVER role", async () => {
  const stub = await startBackendStub();

  try {
    const { createApp } = require("./app");
    process.env.JWT_SECRET = JWT_SECRET;
    process.env.BACKEND_BASE_URL = stub.baseUrl;

    const { app } = createApp();
    const token = createToken("APPROVER");

    const response = await request(app)
      .post("/api/releases/10/approve")
      .set("Authorization", `Bearer ${token}`)
      .set("Content-Type", "application/json")
      .send({});

    assert.equal(response.status, 204);
  } finally {
    await new Promise((resolve, reject) => {
      stub.server.close((error) => (error ? reject(error) : resolve()));
    });
  }
});

test("gateway exposes OpenAPI JSON", async () => {
  const { createApp } = require("./app");
  process.env.JWT_SECRET = JWT_SECRET;
  process.env.BACKEND_BASE_URL = "http://127.0.0.1:65535";

  const { app } = createApp();
  const response = await request(app).get("/api-docs-json");

  assert.equal(response.status, 200);
  assert.equal(response.body.openapi, "3.0.3");
});
