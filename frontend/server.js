// server.js
const express = require("express");
const path = require("path");
const fs = require("fs");
const jwt = require("jsonwebtoken"); // ✅ ต้องติดตั้งก่อน: npm install jsonwebtoken
const app = express();
const PORT = 5173;

// ⚙️ คีย์เดียวกับที่ Spring Boot ใช้สร้าง token (ต้องให้ตรงกัน)
const SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345";

// ✅ Path หลัก
const pagesPath = path.join(__dirname, "pages");
const cssPath = path.join(__dirname, "css");
const jsPath = path.join(__dirname, "js");

// ✅ ตรวจสอบว่ามีโฟลเดอร์ pages
if (!fs.existsSync(pagesPath)) {
    console.error("❌ Folder 'pages' not found!");
    process.exit(1);
}

// ✅ เสิร์ฟไฟล์ static (css, js)
app.use("/css", express.static(cssPath));
app.use("/js", express.static(jsPath));

/* =====================================================
   🟢 1️⃣ หน้าทั่วไป (Public pages) — เข้าได้โดยไม่ต้อง login
   ===================================================== */
app.get("/", (req, res) => {
    res.sendFile(path.join(pagesPath, "signupUser.html"));
});

app.get("/signup.html", (req, res) => {
    res.sendFile(path.join(pagesPath, "signup.html"));
});

app.get("/signin.html", (req, res) => {
    res.sendFile(path.join(pagesPath, "signin.html"));
});

/* =====================================================
   🔒 2️⃣ Middleware ตรวจสอบ JWT token
   ===================================================== */
function verifyToken(req, res, next) {
    // ดึง token จาก query หรือ header
    const token =
        req.query.token ||
        (req.headers.authorization && req.headers.authorization.split(" ")[1]);

    if (!token) {
        return res
            .status(403)
            .send("<h2>⛔ Access Denied: Token not provided</h2><a href='/signin.html'>กลับไปหน้าเข้าสู่ระบบ</a>");
    }

    try {
        const decoded = jwt.verify(token, SECRET_KEY);
        req.user = decoded;
        next();
    } catch (err) {
        console.error("❌ Invalid Token:", err.message);
        res
            .status(401)
            .send("<h2>🚫 Invalid or expired token</h2><a href='/signin.html'>เข้าสู่ระบบอีกครั้ง</a>");
    }
}

/* =====================================================
   🔐 3️⃣ หน้าที่ต้อง login ก่อนเข้า (Protected pages)
   ===================================================== */
app.get("/pages/home.html", verifyToken, (req, res) => {
    res.sendFile(path.join(pagesPath, "home.html"));
});

app.get("/pages/edit-address.html", verifyToken, (req, res) => {
    res.sendFile(path.join(pagesPath, "edit-address.html"));
});

app.get("/pages/account1.html", verifyToken, (req, res) => {
    res.sendFile(path.join(pagesPath, "account1.html"));
});

app.get("/pages/add-address.html", verifyToken, (req, res) => {
    res.sendFile(path.join(pagesPath, "add-address.html"));
});

/* =====================================================
   🚀 4️⃣ เริ่มรันเซิร์ฟเวอร์
   ===================================================== */
app.listen(PORT, () => {
    console.log(`🚀 Frontend running at http://localhost:${PORT}`);
    console.log("🌐 Open http://localhost:" + PORT + " to view the site");
});
