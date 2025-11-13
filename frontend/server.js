// server.js
const express = require("express");
const path = require("path");
const fs = require("fs");
const jwt = require("jsonwebtoken");
const app = express();
const PORT = 5173;

// ⚙️ คีย์เดียวกับที่ Spring Boot ใช้สร้าง token (ต้องให้ตรงกัน)
const SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345";

// ✅ Path หลัก
const pagesPath = path.join(__dirname, "pages");
const cssPath   = path.join(__dirname, "css");
const jsPath    = path.join(__dirname, "js");

// ✅ ตรวจสอบว่ามีโฟลเดอร์ pages
if (!fs.existsSync(pagesPath)) {
  console.error("❌ Folder 'pages' not found!");
  process.exit(1);
}

// ✅ เสิร์ฟไฟล์ static (css, js, pages)
app.use("/css", express.static(cssPath));
app.use("/js", express.static(jsPath));
app.use("/pages", express.static(pagesPath));   // 👈 เพิ่มบรรทัดนี้กันเหนียว

/* =====================================================
   🟢 1️⃣ หน้าทั่วไป (Public pages) — เข้าได้โดยไม่ต้อง login
   ===================================================== */

// root → signin
app.get("/", (req, res) => {
  res.sendFile(path.join(pagesPath, "signin.html"));
});

// เข้าตรง /pages/signin.html ก็ได้
app.get("/pages/signin.html", (req, res) => {
  res.sendFile(path.join(pagesPath, "signin.html"));
});

// 🟦 User Sign up → /pages/signupUser.html
app.get("/pages/signupUser.html", (req, res) => {
  res.sendFile(path.join(pagesPath, "signupUser.html"));
});

// 🔴 Rescue Sign up → /pages/signupRescue.html
app.get("/pages/signupRescue.html", (req, res) => {
  console.log("✅ HIT /pages/signupRescue.html");   // debug ดูใน terminal
  res.sendFile(path.join(pagesPath, "signupRescue.html"));
});

/* =====================================================
   🔒 2️⃣ Middleware ตรวจสอบ JWT token
   ===================================================== */
function verifyToken(req, res, next) {
  const token =
    req.query.token ||
    (req.headers.authorization && req.headers.authorization.split(" ")[1]);

  if (!token) {
    return res
      .status(403)
      .send("<h2>⛔ Access Denied: Token not provided</h2><a href='/pages/signin.html'>กลับไปหน้าเข้าสู่ระบบ</a>");
  }

  try {
    const decoded = jwt.verify(token, SECRET_KEY);
    req.user = decoded;
    next();
  } catch (err) {
    console.error("❌ Invalid Token:", err.message);
    res
      .status(401)
      .send("<h2>🚫 Invalid or expired token</h2><a href='/pages/signin.html'>เข้าสู่ระบบอีกครั้ง</a>");
  }
}

/* =====================================================
   🔐 3️⃣ หน้าที่ต้อง login ก่อนเข้า (Protected pages)
   ===================================================== */
app.get("/pages/homeUser",verifyToken, (req, res) => {
  res.sendFile(path.join(pagesPath, "homeUser.html"));
});
app.get("/pages/location", verifyToken,(req, res) => {
  res.sendFile(path.join(pagesPath, "map.html"));
});


// (อันอื่นๆ ค่อยเติมทีหลังได้)
app.get("/pages/homeRescue.html",  (req, res) => {
  res.sendFile(path.join(pagesPath, "homeRescue.html"));
});

app.listen(PORT, () => {
  console.log(`🚀 Frontend running at http://localhost:${PORT}`);
});
