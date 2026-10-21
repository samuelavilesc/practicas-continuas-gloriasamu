// Genera docs/site/: un .html por cada .md de docs/ y un index.html que los enlaza.
// Uso: cd docs && npm ci && node build-site.js
const fs = require("fs");
const path = require("path");
const { marked } = require("marked");

const DOCS = __dirname;
const OUT = path.join(DOCS, "site");

const escapeHtml = (s) =>
  s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

const CSS = `
  :root { color-scheme: light dark; }
  body { font: 16px/1.6 system-ui, sans-serif; max-width: 880px; margin: 2rem auto; padding: 0 1rem; }
  img { max-width: 100%; height: auto; border: 1px solid #8884; border-radius: 6px; }
  table { border-collapse: collapse; display: block; overflow-x: auto; }
  th, td { border: 1px solid #8886; padding: .35rem .6rem; vertical-align: top; }
  pre { background: #8881; padding: .8rem; overflow-x: auto; border-radius: 6px; }
  code { background: #8881; padding: .1rem .3rem; border-radius: 4px; }
  pre code { background: none; padding: 0; }
  blockquote { border-left: 4px solid #8886; margin-left: 0; padding-left: 1rem; color: #888; }
  nav a { margin-right: 1rem; }
`;

const page = (title, body, nav = true) => `<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapeHtml(title)}</title>
<style>${CSS}</style>
</head>
<body>
${nav ? '<nav><a href="index.html">← Índice</a></nav>' : ""}
${body}
</body>
</html>
`;

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true });
  for (const e of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, e.name);
    const d = path.join(dst, e.name);
    e.isDirectory() ? copyDir(s, d) : fs.copyFileSync(s, d);
  }
}

fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(OUT, { recursive: true });

const files = fs
  .readdirSync(DOCS)
  .filter((f) => f.endsWith(".md"))
  .sort((a, b) => a.localeCompare(b, "es", { numeric: true }));

const entries = [];
for (const file of files) {
  const md = fs.readFileSync(path.join(DOCS, file), "utf8");
  const title = (md.match(/^#\s+(.+)$/m) || [null, file])[1].trim();
  let html = marked.parse(md, { gfm: true });
  // los enlaces entre documentos apuntan a .md: en la web deben apuntar a .html
  html = html.replace(/href="([^"#:]+)\.md(#[^"]*)?"/g, 'href="$1.html$2"');
  const outName = file.replace(/\.md$/, ".html");
  fs.writeFileSync(path.join(OUT, outName), page(title, html));
  entries.push({ outName, title });
  console.log(`  ${file} -> site/${outName}`);
}

const list = entries
  .map((e) => `<li><a href="${e.outName}">${escapeHtml(e.title)}</a></li>`)
  .join("\n");
fs.writeFileSync(
  path.join(OUT, "index.html"),
  page(
    "Prácticas Continuas · Boletines",
    `<h1>Prácticas Continuas · Boletines</h1>
<p>Memorias de la primera parte de la asignatura, de Gloria Sánchez Alonso y Samuel Avilés Conesa.</p>
<ul>
${list}
</ul>`,
    false
  )
);

// capturas de pantalla
const caps = path.join(DOCS, "capturas");
if (fs.existsSync(caps)) copyDir(caps, path.join(OUT, "capturas"));

console.log(`Web generada en ${OUT} (${entries.length} documentos)`);
