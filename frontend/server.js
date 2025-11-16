// server.js
const express = require("express");
const path = require("path");
const jwt = require("jsonwebtoken");
const app = express();
const PORT = 5173;

const SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345";

const pagesPath = path.join(__dirname, "pages");
const cssPath   = path.join(__dirname, "css");
const jsPath    = path.join(__dirname, "js");

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
    return res.status(403).send("<h2>⛔ Access Denied: Token not provided</h2>");
  }

  try {
    const decoded = jwt.verify(token, SECRET_KEY);
    req.user = decoded;
    next();
  } catch (err) {
    res.status(401).send("<h2>🚫 Invalid or expired token</h2>");
  }
}

/* -------------------------
   Protected pages (ต้องอยู่เหนือ static)
-------------------------- */
app.get("/pages/homeUser.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "homeUser.html"));
});

app.get("/pages/account.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "accountUser.html"));
});

app.get("/pages/location", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "map.html"));
});

app.get("/pages/SOS.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "SOS.html"));
});

app.get("/pages/SUSTENANCE.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "SUSTENANCE.html"));
});

// Rescue pages
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
app.get("/pages/caseSelection.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "caseSelection.html"));
});
app.get("/pages/selectedCases.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "selectedCases.html"));
});
app.get("/pages/comingHelp.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "comingHelp.html"));
});
app.get("/pages/doneCases.html", verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "doneCases.html"));
});

/* -------------------------
   Static fallback (ต้องล่างสุด)
-------------------------- */
//app.use("/pages", express.static(pagesPath));

app.listen(PORT, () => {
  console.log(`🚀 Frontend running at http://localhost:${PORT}`);
});
