// ✅ form-handler.js
function goHome(event) {
  // ป้องกันไม่ให้ฟอร์มรีเฟรชหน้า
  event.preventDefault();

  // ดึงค่าจาก input ทั้งสามช่อง
  const fname = document.getElementById("firstname").value;
  const lname = document.getElementById("lastname").value;
  const phone = document.getElementById("number").value;

  // ตรวจสอบว่ากรอกครบหรือยัง
  if (!fname || !phone) {
    alert("กรุณากรอกชื่อและเบอร์โทรให้ครบ");
    return;
  }

  // เก็บข้อมูลไว้ใน localStorage (ไว้ใช้ในหน้า home)
  localStorage.setItem("user_name", fname + " " + lname);
  localStorage.setItem("user_phone", phone);

  // ไปหน้า home.html
  window.location.href = "pages/home.html";
}

