const ROUTE_VALIDATORS = [
  {
    methods: ["GET"],
    pattern: /^\/api\/releases\/?$/,
    validate: (req) => {
      const required = ["applicationId", "version", "environment", "status"];
      const missing = required.filter((name) => !req.query[name]);
      if (missing.length > 0) {
        return `Missing query params: ${missing.join(", ")}`;
      }
      return null;
    }
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/approvals\/approver\/?$/,
    validate: (req) => (!req.query.aprovador ? "Missing query param: aprovador" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/approvals\/release\/?$/,
    validate: (req) => (!req.query.releaseId ? "Missing query param: releaseId" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/approvals\/outcome\/?$/,
    validate: (req) => (!req.query.outcome ? "Missing query param: outcome" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/audit\/actor\/?$/,
    validate: (req) => (!req.query.ator ? "Missing query param: ator" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/audit\/action\/?$/,
    validate: (req) => (!req.query.acao ? "Missing query param: acao" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/audit\/entity\/?$/,
    validate: (req) => (!req.query.entidade ? "Missing query param: entidade" : null)
  },
  {
    methods: ["GET"],
    pattern: /^\/api\/audit\/interval\/?$/,
    validate: (req) => {
      const missing = ["dataInicio", "dataFim"].filter((name) => !req.query[name]);
      if (missing.length > 0) {
        return `Missing query params: ${missing.join(", ")}`;
      }
      return null;
    }
  }
];

function validationMiddleware(req, res, next) {
  const method = req.method.toUpperCase();
  const path = req.path;

  if (path.startsWith("/api")) {
    const jsonMethods = ["POST", "PUT", "PATCH"];
    if (jsonMethods.includes(method)) {
      const contentType = req.headers["content-type"] || "";
      if (!contentType.toLowerCase().includes("application/json")) {
        return res.status(415).json({
          code: "UNSUPPORTED_MEDIA_TYPE",
          message: "Content-Type must be application/json",
          details: `Received: ${contentType || "none"}`
        });
      }

      if (req.body === null || typeof req.body !== "object" || Array.isArray(req.body)) {
        return res.status(400).json({
          code: "VALIDATION_ERROR",
          message: "Request body must be a JSON object",
          details: `Path: ${req.path}`
        });
      }
    }
  }

  const validator = ROUTE_VALIDATORS.find((entry) => entry.methods.includes(method) && entry.pattern.test(path));
  if (!validator) {
    return next();
  }

  const errorDetails = validator.validate(req);
  if (errorDetails) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      message: "Request validation failed",
      details: errorDetails
    });
  }

  return next();
}

module.exports = {
  validationMiddleware
};
