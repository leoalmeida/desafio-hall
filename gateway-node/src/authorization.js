const jwt = require("jsonwebtoken");

const OPEN_ROUTES = [
  { method: "POST", pattern: /^\/api\/auth\/login\/?$/ },
  { method: "POST", pattern: /^\/api\/auth\/signout\/?$/ },
  { method: "GET", pattern: /^\/api-docs-json\/?$/ },
  { method: "GET", pattern: /^\/healthcheck\/?$/ },
  { method: "GET", pattern: /^\/api\/healthcheck\/?$/ },
  { method: "GET", pattern: /^\/metrics\/?$/ }
];

const ROUTE_POLICIES = [
  { methods: ["GET"], pattern: /^\/api\/applications(\/.*)?$/, roles: ["admin", "approver", "viewer"] },
  { methods: ["POST", "PUT", "PATCH", "DELETE"], pattern: /^\/api\/applications(\/.*)?$/, roles: ["admin"] },
  { methods: ["GET"], pattern: /^\/api\/releases(\/.*)?$/, roles: ["admin", "approver", "viewer"] },
  { methods: ["POST"], pattern: /^\/api\/releases\/\d+\/(approve|disapprove)\/?$/, roles: ["admin", "approver"] },
  { methods: ["POST"], pattern: /^\/api\/releases\/\d+\/promote\/?$/, roles: ["admin"] },
  { methods: ["POST", "PUT", "PATCH", "DELETE"], pattern: /^\/api\/releases(\/.*)?$/, roles: ["admin"] },
  { methods: ["GET"], pattern: /^\/api\/approvals(\/.*)?$/, roles: ["admin", "approver"] },
  { methods: ["POST", "PUT", "PATCH", "DELETE"], pattern: /^\/api\/approvals(\/.*)?$/, roles: ["admin"] },
  { methods: ["GET", "POST", "PUT", "PATCH", "DELETE"], pattern: /^\/api\/audit(\/.*)?$/, roles: ["admin"] }
];

function normalizeRole(role) {
  if (!role) {
    return "";
  }

  return String(role).trim().toLowerCase();
}

function isOpenRoute(method, path) {
  const upperMethod = method.toUpperCase();
  return OPEN_ROUTES.some((entry) => entry.method === upperMethod && entry.pattern.test(path));
}

function getPolicyFor(method, path) {
  const upperMethod = method.toUpperCase();
  return ROUTE_POLICIES.find((policy) => policy.methods.includes(upperMethod) && policy.pattern.test(path));
}

function extractBearerToken(headerValue) {
  if (!headerValue) {
    return null;
  }

  const [scheme, token] = headerValue.split(" ");
  if (!scheme || !token || scheme.toLowerCase() !== "bearer") {
    return null;
  }

  return token;
}

function authzMiddleware(jwtSecret) {
  return (req, res, next) => {
    if (req.method.toUpperCase() === "OPTIONS") {
      return next();
    }

    if (!req.path.startsWith("/api") || isOpenRoute(req.method, req.path)) {
      return next();
    }

    const token = extractBearerToken(req.headers.authorization);
    if (!token) {
      return res.status(401).json({
        code: "AUTH_REQUIRED",
        message: "Bearer token is required",
        details: "Provide Authorization: Bearer <token>"
      });
    }

    let decoded;
    try {
      decoded = jwt.verify(token, jwtSecret);
    } catch (error) {
      return res.status(401).json({
        code: "INVALID_TOKEN",
        message: "Token is invalid or expired",
        details: error.message
      });
    }

    const userRole = normalizeRole(decoded.role);
    if (!userRole) {
      return res.status(403).json({
        code: "FORBIDDEN",
        message: "Token does not contain a valid role",
        details: "Missing role claim"
      });
    }

    const policy = getPolicyFor(req.method, req.path);

    if (policy && !policy.roles.includes(userRole)) {
      return res.status(403).json({
        code: "FORBIDDEN",
        message: "Role does not have access to this resource",
        details: `Required roles: ${policy.roles.join(", ")}`
      });
    }

    req.auth = {
      email: decoded.email || decoded.sub,
      name: decoded.name,
      role: userRole,
      subject: decoded.sub
    };

    return next();
  };
}

module.exports = {
  authzMiddleware
};
