// server.js
const express = require("express");
const path = require("path");
const fs = require("fs");
const jwt = require("jsonwebtoken");
const app = express();
const PORT = 5173;

const SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345";

const pagesPath = path.join(__dirname, "pages");
const cssPath   = path.join(__dirname, "css");
const jsPath    = path.join(__dirname, "js");

if (!fs.existsSync(pagesPath)) {
  console.error("❌ Folder 'pages' not found!");
  process.exit(1);
}

// serve css/js ก่อน
app.use("/css", express.static(cssPath));
app.use("/js", express.static(jsPath));

/* -------------------------
   Public pages
-------------------------- */
app.get("/", (req, res) => {
  res.sendFile(path.join(pagesPath, "signin.html"));
});

app.get("/pages/signin.html", (req, res) => {
  res.sendFile(path.join(pagesPath, "signin.html"));
});

app.get("/pages/signupUser.html", (req, res) => {
  res.sendFile(path.join(pagesPath, "signupUser.html"));
});

app.get("/pages/signupRescue.html", (req, res) => {
  res.sendFile(path.join(pagesPath, "signupRescue.html"));
});

/* -------------------------
   JWT Verify
-------------------------- */
function verifyToken(req, res, next) {
  const token =
    req.query.token ||
    (req.headers.authorization && req.headers.authorization.split(" ")[1]);

  if (!token) {
    return res
      .status(403)
      .send("<h2>⛔ Access Denied: Token not provided</h2>");
  }

  try {
    const decoded = jwt.verify(token, SECRET_KEY);
    req.user = decoded;
    next();
  } catch (err) {
    res.status(401).send("<h2>🚫 Invalid or expired token</h2>");
  }
}

//app.use("/pages", express.static(pagesPath));
/* -------------------------
   Protected pages (อยู่เหนือ static!)
-------------------------- */
app.get("/pages/homeUser", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "homeUser.html"));
});

app.get("/pages/location", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "map.html"));
});

app.get("/pages/SOS", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "SOS.html"));
});

app.get("/pages/SUSTENANCE", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "SUSTENANCE.html"));
});


app.get("/pages/account", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "accountUser.html"));
});

app.get("/pages/homeRescue.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "homeRescue.html"));
});
app.get("/pages/accountRescue.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "accountRescue.html"));
});
app.get("/pages/rescueTeam.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "rescueTeam.html"));
});
app.get("/pages/createTeam.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "createTeam.html"));
});
app.get("/pages/selectMember.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "selectMember.html"));
});
app.get("/pages/viewTeam.html",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "viewTeam.html"));
});




/* -------------------------
   Static fallback (อยู่ล่างสุด)
-------------------------- */

app.listen(PORT, () => {
  console.log(`🚀 Frontend running at http://localhost:${PORT}`);
});
