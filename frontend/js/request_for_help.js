/* ============================================================
   🚨 Request for Help (SOS Only)
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ Request for Help (SOS) page loaded.");

  const sosCard = document.getElementById("sos-card");
  const progressCard = document.getElementById("progress-card");
  const progressLine = document.getElementById("progress-line-active");
  const statusText = document.getElementById("status-text");
  const dots = document.querySelectorAll(".dot");

  const STATUS = [
    "Send a request for help",
    "In progress",
    "Accept request",
    "Coming to help",
    "Succeed"
  ];

  let currentStep = 0;

  // อัปเดต Progress Bar
  function updateProgress(step) {
    if (step < 0 || step >= STATUS.length) return;
    currentStep = step;
    statusText.textContent = STATUS[step];

    dots.forEach((dot, index) => {
      dot.classList.remove("active", "success");
      if (index === step) dot.classList.add("active");
      else if (index < step) dot.classList.add("active");
    });

    const percent = (step / (STATUS.length - 1)) * 100;
    progressLine.style.width = `${percent}%`;

    if (STATUS[step] === "Succeed") {
      progressLine.classList.add("success");
      statusText.classList.add("success-text");
    } else {
      progressLine.classList.remove("success");
      statusText.classList.remove("success-text");
    }
  }

  // เริ่ม SOS Request
  sosCard.addEventListener("click", () => {
    sosCard.style.display = "none";
    progressCard.style.display = "block";
    updateProgress(0);
    console.log("🚨 SOS request sent.");
  });

  // จำลองการอัปเดตสถานะ (ใช้ใน Console)
  window.simulateRescueUpdate = () => {
    if (currentStep < STATUS.length - 1) {
      updateProgress(currentStep + 1);
    } else {
      console.log("✅ SOS Request completed!");
    }
  };
});
/* ============================================================
   ✅ ปุ่ม "Send a request for help" — ให้ไปหน้า Status Request
   ============================================================ */
document.addEventListener("DOMContentLoaded", () => {
  const sendButton = document.getElementById("sendRequest");
  if (sendButton) {
sendButton.addEventListener("click", () => {
  window.location.href = "status_request.html?type=sos";
});

  }
});