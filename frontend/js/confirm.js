// ✅ ฟังก์ชันสำหรับปุ่ม Confirm
function confirmLocation() {
  const address = document.getElementById("address").value.trim();

  if (address === "") {
    alert("กรุณากรอกที่อยู่ก่อนยืนยัน");
    return;
  }

  // ✅ เก็บที่อยู่ลง localStorage (เผื่อใช้ในหน้าถัดไป)
  localStorage.setItem("userAddress", address);

  // ✅ ไปหน้า Request for Help
  window.location.href = "../pages/request_for_help.html";
}
