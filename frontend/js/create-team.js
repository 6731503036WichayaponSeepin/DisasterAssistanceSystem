// ✅ สร้าง Team ID แบบสุ่ม เช่น TEAM-48392
function generateTeamId() {
  const randomNum = Math.floor(10000 + Math.random() * 90000);
  return "TEAM-" + randomNum;
}

// ✅ โหลดอัตโนมัติเมื่อเปิดหน้า
window.addEventListener("DOMContentLoaded", () => {
  document.getElementById("team-id").value = generateTeamId();

  // ดึงรายชื่ออำเภอจาก backend
  fetch("http://localhost:8080/api/districts")
    .then(res => res.json())
    .then(data => {
      const select = document.getElementById("district");
      data.forEach(d => {
        const opt = document.createElement("option");
        opt.value = d.districtName;
        opt.textContent = d.districtName;
        select.appendChild(opt);
      });
    })
    .catch(err => {
      console.error("❌ Error fetching districts:", err);
      alert("ไม่สามารถโหลดรายชื่ออำเภอได้");
    });
});

// ✅ ฟังก์ชันเมื่อกด Create Team
document.getElementById("create-team-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const team = {
    teamName: document.getElementById("team-name").value.trim(),
    teamId: document.getElementById("team-id").value.trim(),
    leader: document.getElementById("team-leader").value.trim(),
    district: document.getElementById("district").value
  };

  if (!team.teamName || !team.leader || !team.district) {
    alert("⚠️ กรุณากรอกข้อมูลให้ครบ");
    return;
  }

  // ส่งข้อมูลไป backend (ตัวอย่างใช้ POST)
  fetch("http://localhost:8080/api/teams", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(team)
  })
    .then(res => res.json())
    .then(data => {
      alert(`✅ สร้างทีมเรียบร้อย!\nชื่อทีม: ${data.teamName}\nรหัส: ${data.teamId}`);
      // ถ้าต้องการกลับหน้าอื่นหลังบันทึก
      // window.location.href = "home.html";
    })
    .catch(err => {
      console.error("❌ Error creating team:", err);
      alert("เกิดข้อผิดพลาดในการสร้างทีม");
    });
});
