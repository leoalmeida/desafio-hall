const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const swaggerUi = require("swagger-ui-express");
const { createProxyMiddleware, fixRequestBody } = require("http-proxy-middleware");

const { authzMiddleware } = require("./authorization");
const { metrics, metricsMiddleware } = require("./metrics");
const { openApiSpec } = require("./openapi");
const { validationMiddleware } = require("./validation");

function loadConfig() {
  const configPath = path.join(__dirname, "..", "config", "default.json");
  let fileConfig = {};

  if (fs.existsSync(configPath)) {
    fileConfig = JSON.parse(fs.readFileSync(configPath, "utf8"));
  }

  return {
    port: Number(process.env.PORT || fileConfig.server?.port || 3000),
    backendBaseUrl: process.env.BACKEND_BASE_URL || fileConfig.backend?.baseUrl || "http://localhost:8081",
    jwtSecret: process.env.JWT_SECRET || fileConfig.security?.jwtSecret || "changeit-changeit-changeit-changeit",
    rateLimitWindowMs: Number(process.env.RATE_LIMIT_WINDOW_MS || fileConfig.security?.rateLimitWindowMs || 60000),
    rateLimitMax: Number(process.env.RATE_LIMIT_MAX || fileConfig.security?.rateLimitMax || 300)
  };
}

function createApp() {
  const config = loadConfig();
  const app = express();

  app.disable("x-powered-by");
  app.use(helmet());
  app.use(cors({ origin: true, credentials: true,  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH']}));
  app.use(express.json({ limit: "1mb" }));

  app.use((req, res, next) => {
    const requestId = req.headers["x-request-id"] || crypto.randomUUID();
    const correlationId = req.headers["x-correlation-id"] || requestId;
    req.requestId = requestId;
    req.correlationId = correlationId;
    res.setHeader("x-request-id", requestId);
    res.setHeader("x-correlation-id", correlationId);

    const start = process.hrtime.bigint();
    res.on("finish", () => {
      const durationMs = Number(process.hrtime.bigint() - start) / 1_000_000;
      process.stdout.write(`${JSON.stringify({
        timestamp: new Date().toISOString(),
        type: "access",
        requestId,
        correlationId,
        method: req.method,
        path: req.originalUrl,
        statusCode: res.statusCode,
        durationMs: Number(durationMs.toFixed(2))
      })}\n`);
    });

    next();
  });

  app.use(metricsMiddleware);

  app.use(rateLimit({
    windowMs: config.rateLimitWindowMs,
    max: config.rateLimitMax,
    standardHeaders: true,
    legacyHeaders: false,
    message: {
      code: "RATE_LIMITED",
      message: "Too many requests",
      details: "Try again later"
    }
  }));

  app.use(validationMiddleware);

  app.use(authzMiddleware(config.jwtSecret));

  app.get("/healthcheck", (_, res) => {
    res.status(200).json({ status: "ok", service: "gateway-node" });
  });

  app.get("/metrics", (_, res) => {
    res.status(200).json(metrics);
  });

  app.get("/api-docs-json", (_, res) => {
    res.status(200).json(openApiSpec);
  });

  app.use("/swagger-ui", swaggerUi.serve, swaggerUi.setup(openApiSpec));

  app.use("/api", createProxyMiddleware({
    target: config.backendBaseUrl,
    changeOrigin: true,
    secure: false,
    pathRewrite: (path) => `/api${path}`,
    on: {
      proxyReq: (proxyReq, req) => {
        proxyReq.setHeader("x-request-id", req.requestId);
        proxyReq.setHeader("x-correlation-id", req.correlationId);
        fixRequestBody(proxyReq, req);
      },
      proxyRes: (proxyRes, req) => {
        if (proxyRes.statusCode >= 500) {
          metrics.upstreamErrors += 1;
        }
        proxyRes.headers["x-request-id"] = req.requestId;
        proxyRes.headers["x-correlation-id"] = req.correlationId;
      },
      error: (_, req, res) => {
        metrics.upstreamErrors += 1;
        if (!res.headersSent) {
          res.status(502).json({
            code: "UPSTREAM_ERROR",
            message: "Failed to reach backend service",
            details: req.originalUrl
          });
        }
      }
    },
    logLevel: "warn"
  }));

  app.use((req, res) => {
    res.status(404).json({
      code: "NOT_FOUND",
      message: "Route not found",
      details: req.originalUrl
    });
  });

  app.use((error, req, res, _) => {
    process.stdout.write(`${JSON.stringify({
      timestamp: new Date().toISOString(),
      type: "error",
      requestId: req.requestId,
      correlationId: req.correlationId,
      message: error?.message || "Unexpected error"
    })}\n`);

    if (error?.type === "entity.parse.failed") {
      return res.status(400).json({
        code: "INVALID_JSON",
        message: "Malformed JSON request body",
        details: error.message
      });
    }

    res.status(500).json({
      code: "INTERNAL_ERROR",
      message: "Unexpected gateway error",
      details: error.message
    });
  });

  return { app, config };
}

module.exports = {
  createApp
};
