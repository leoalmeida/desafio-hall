const { createApp } = require("./app");

const { app, config } = createApp();

app.listen(config.port, () => {
  console.log(JSON.stringify({
    timestamp: new Date().toISOString(),
    level: "info",
    message: "Gateway started",
    port: config.port,
    backendBaseUrl: config.backendBaseUrl
  }));
});
