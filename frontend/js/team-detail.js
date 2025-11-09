// ตัวอย่างข้อมูลจำลอง
const currentUser = {
  name: "Alice",
  role: "leader", // ถ้าเป็นสมาชิกทั่วไป ให้เปลี่ยนเป็น "member"
};

const teamData = {
  district: "Chiang Rai",
  code: "TEAM-58522",
  members: [
    { name: "Alice", role: "Leader" },
    { name: "Bob", role: "Member" },
  ],
};

// แสดงข้อมูลทีม
document.getElementById("team-district").textContent = teamData.district;
document.getElementById("team-code").textContent = teamData.code;

// แสดงปุ่ม Manage ถ้าเป็นหัวหน้าทีม
const manageBtn = document.getElementById("manage-btn");
if (currentUser.role === "leader") {
  manageBtn.classList.remove("hidden");
}

// สร้างรายการสมาชิก
const memberList = document.getElementById("member-list");
function renderMembers() {
  memberList.innerHTML = "";
  teamData.members.forEach((m, index) => {
    const div = document.createElement("div");
    div.className = "member-card";
    div.innerHTML = `
      <span>${m.name} (${m.role})</span>
      ${
        currentUser.role === "leader"
          ? `<button class="delete-btn" onclick="removeMember(${index})">Remove</button>`
          : ""
      }
    `;
    memberList.appendChild(div);
  });
}
renderMembers();

// ลบสมาชิก
function removeMember(index) {
  if (confirm("ลบสมาชิกคนนี้ออกจากทีม?")) {
    teamData.members.splice(index, 1);
    renderMembers();
  }
}

// จัดการ Modal
const modal = document.getElementById("manage-modal");
document.getElementById("manage-btn").onclick = () => modal.classList.remove("hidden");
document.getElementById("close-modal").onclick = () => modal.classList.add("hidden");

// เพิ่มสมาชิกใหม่
document.getElementById("add-member").onclick = () => {
  const newName = document.getElementById("new-member").value.trim();
  if (newName) {
    teamData.members.push({ name: newName, role: "Member" });
    document.getElementById("new-member").value = "";
    renderMembers();
  }
};
