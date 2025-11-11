document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ Status Request page loaded.");

  // อ่านค่าพารามิเตอร์จาก URL
  const params = new URLSearchParams(window.location.search);
  const requestType = params.get("type");

  const cardTitle = document.getElementById("request-title");
  const cardSubtitle = document.getElementById("request-subtitle");
  const cardIcon = document.getElementById("request-icon");

  if (requestType === "sustenance") {
    console.log("🍞 Showing Sustenance status card");
    cardTitle.textContent = "Request Sustenance";
    cardSubtitle.textContent = "Water/food/medicine";
    cardIcon.src = "../img/icon_sustenance.png"; // 🔧 เปลี่ยนเป็น path icon ของคุณ
    cardTitle.parentElement.style.backgroundColor = "#FFD43B"; // สีเหลือง
  } else {
    console.log("🚨 Showing SOS status card");
    cardTitle.textContent = "Request SOS";
    cardSubtitle.textContent = "Emergency assistance";
    cardIcon.src = "../img/icon_sos.png";
    cardTitle.parentElement.style.backgroundColor = "#FF3B30"; // สีแดง
  }
});
