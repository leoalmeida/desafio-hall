const openApiSpec = {
  openapi: "3.0.3",
  info: {
    title: "desafio-hall Gateway API",
    version: "1.0.0",
    description: "Node.js API Gateway responsible for auth, authorization, validation, observability and backend proxy."
  },
  servers: [
    {
      url: "http://localhost:3000",
      description: "Local gateway"
    }
  ],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: "http",
        scheme: "bearer",
        bearerFormat: "JWT"
      }
    },
    schemas: {
      ErrorResponse: {
        type: "object",
        required: ["code", "message", "details"],
        properties: {
          code: { type: "string", example: "FORBIDDEN" },
          message: { type: "string", example: "Role does not have access to this resource" },
          details: { type: "string", example: "Required roles: admin" }
        }
      }
    }
  },
  paths: {
    "/healthcheck": {
      get: {
        summary: "Gateway liveness",
        responses: {
          "200": {
            description: "Gateway is healthy"
          }
        }
      }
    },
    "/metrics": {
      get: {
        summary: "Gateway metrics",
        responses: {
          "200": {
            description: "Request counters by route"
          }
        }
      }
    },
    "/api/auth/login": {
      post: {
        summary: "Proxy authentication to backend",
        responses: {
          "200": {
            description: "Authenticated"
          },
          "401": {
            description: "Invalid credentials",
            content: {
              "application/json": {
                schema: {
                  $ref: "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/api/{path}": {
      parameters: [
        {
          in: "path",
          name: "path",
          required: true,
          schema: { type: "string" },
          description: "Backend API path"
        }
      ],
      get: {
        summary: "Proxy GET request",
        security: [{ bearerAuth: [] }],
        responses: {
          "200": { description: "Success" },
          "401": { description: "Unauthorized" },
          "403": { description: "Forbidden" },
          "502": { description: "Upstream error" }
        }
      },
      post: {
        summary: "Proxy POST request",
        security: [{ bearerAuth: [] }],
        responses: {
          "200": { description: "Success" },
          "201": { description: "Created" },
          "400": { description: "Validation error" },
          "401": { description: "Unauthorized" },
          "403": { description: "Forbidden" },
          "415": { description: "Unsupported media type" },
          "502": { description: "Upstream error" }
        }
      }
    }
  }
};

module.exports = {
  openApiSpec
};
