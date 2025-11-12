// ✅ js/home.js
// ดึงค่าจาก localStorage ที่บันทึกไว้ตอน Sign Up
const name = localStorage.getItem("user_name");
const phone = localStorage.getItem("user_phone");

// แสดงข้อมูลใน Dashboard
document.querySelector(".name").textContent = name || "ชื่อผู้ใช้";
document.querySelector(".phone").textContent = phone || "เบอร์โทรศัพท์";

// (เพิ่มเติม: ปุ่มออกจากระบบ)
const logoutBtn = document.createElement("button");
logoutBtn.textContent = "ออกจากระบบ";
logoutBtn.style.marginTop = "15px";
logoutBtn.onclick = () => {
  localStorage.clear();
  window.location.href = "index.html"; // กลับไปหน้า Sign Up
};
document.querySelector(".profile").appendChild(logoutBtn);
