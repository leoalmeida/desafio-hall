const metrics = {
  startedAt: new Date().toISOString(),
  requestsTotal: 0,
  upstreamErrors: 0,
  byRoute: {}
};

function normalizePath(pathname) {
  if (!pathname) {
    return "/";
  }

  return pathname
    .replace(/\/(\d+)(?=\/|$)/g, "/:id")
    .replace(/\/[0-9a-fA-F-]{8,}(?=\/|$)/g, "/:id");
}

function incrementRouteMetric(method, pathname, statusCode) {
  const routeKey = `${method.toUpperCase()} ${normalizePath(pathname)}`;
  if (!metrics.byRoute[routeKey]) {
    metrics.byRoute[routeKey] = { count: 0, status: {} };
  }

  metrics.byRoute[routeKey].count += 1;
  const statusKey = String(statusCode);
  metrics.byRoute[routeKey].status[statusKey] = (metrics.byRoute[routeKey].status[statusKey] || 0) + 1;
}

function metricsMiddleware(req, res, next) {
  metrics.requestsTotal += 1;

  res.on("finish", () => {
    incrementRouteMetric(req.method, req.path, res.statusCode);
  });

  next();
}

module.exports = {
  metrics,
  metricsMiddleware
};
