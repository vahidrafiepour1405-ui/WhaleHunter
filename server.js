const http = require("node:http");
const fs = require("node:fs");
const path = require("node:path");

const PORT = Number(process.env.PORT) || 3000;
const WEB_DIR = path.join(__dirname, "web");
const INDEX_FILE = path.join(WEB_DIR, "index.html");

const server = http.createServer((req, res) => {
  const pathname = new URL(req.url || "/", "http://localhost").pathname;

  if (pathname === "/health" || pathname === "/healthz") {
    res.writeHead(200, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
    res.end(JSON.stringify({ ok: true, service: "WAL", status: "online" }));
    return;
  }

  if (pathname === "/" || pathname === "/index.html") {
    fs.readFile(INDEX_FILE, (err, html) => {
      if (err) {
        res.writeHead(500, { "content-type": "text/plain; charset=utf-8" });
        res.end("WAL web page could not be loaded. Check that web/index.html exists.");
        return;
      }
      res.writeHead(200, {
        "content-type": "text/html; charset=utf-8",
        "cache-control": "no-cache, no-store, must-revalidate",
        "x-content-type-options": "nosniff"
      });
      res.end(html);
    });
    return;
  }

  res.writeHead(404, { "content-type": "application/json; charset=utf-8" });
  res.end(JSON.stringify({ error: "Not found" }));
});

server.listen(PORT, "0.0.0.0", () => {
  console.log("WAL web server listening on port " + PORT);
});
