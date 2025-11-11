/* ============================================================
   🍞 Request for Sustenance — ส่งคำขอขออาหาร/น้ำ/ยา
   ============================================================ */
document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ Request Sustenance page loaded.");

  const steps = document.querySelectorAll(".step");
  const progressLine = document.getElementById("progress-line");
  const sendButton = document.getElementById("sendRequest");
  let currentStep = 0;

  // ฟังก์ชันอัปเดตสถานะ
  function updateStatus(stepIndex) {
    if (stepIndex < 0 || stepIndex >= steps.length) return;

    steps.forEach((step, index) => {
      step.classList.remove("active", "success");
      if (index < stepIndex) step.classList.add("success");
      else if (index === stepIndex) step.classList.add("active");
    });

    const newHeight = (stepIndex / (steps.length - 1)) * 100;
    progressLine.style.height = `${newHeight}%`;
  }

  // เริ่มต้นขั้นตอนแรก
  updateStatus(0);

  // ปุ่ม "Send a request for help"
  if (sendButton) {
    sendButton.addEventListener("click", () => {
      console.log("🍞 Sustenance request sent!");
      // 👉 ไปหน้า Status Request พร้อมบอกว่าเป็นของ sustenance
      window.location.href = "status_request.html?type=sustenance";
    });
  } else {
    console.error("❌ ไม่เจอปุ่ม id=sendRequest");
  }

  // สำหรับจำลองการอัปเดตสถานะ
  window.simulateRescueUpdate = () => {
    if (currentStep < steps.length - 1) {
      currentStep++;
      updateStatus(currentStep);
    }
  };
});
