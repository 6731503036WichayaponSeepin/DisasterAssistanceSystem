// จำลองฐานข้อมูลทีม
const teams = [
  {
    id: "12345",
    name: "Team A",
    leader: "นายกิติพงษ์ สุวรรณา"
  },
  {
    id: "88888",
    name: "Team B",
    leader: "นางสาวพรทิพย์ อินทรา"
  }
];

// ค้นหาเมื่อกดปุ่ม
document.getElementById("search-btn").addEventListener("click", searchTeam);

function searchTeam() {
  const code = document.getElementById("team-code").value.trim();
  const resultBox = document.getElementById("result-container");
  const noResult = document.getElementById("no-result");

  if (!code) {
    alert("กรุณากรอกรหัสทีม");
    return;
  }

  const found = teams.find(team => team.id === code);

  if (found) {
    document.getElementById("team-name").textContent = found.name;
    document.getElementById("team-id").textContent = found.id;
    document.getElementById("team-leader").textContent = "Rescue Team Leader " + found.leader;

    resultBox.classList.remove("hidden");
    noResult.classList.add("hidden");
  } else {
    resultBox.classList.add("hidden");
    noResult.classList.remove("hidden");
  }
}

// ปุ่ม Join
document.getElementById("join-btn").addEventListener("click", () => {
  const teamId = document.getElementById("team-id").textContent;
  alert(`✅ เข้าร่วมทีมสำเร็จ!\nTeam ID: ${teamId}`);

  // ตัวอย่าง: เก็บค่าใน localStorage
  localStorage.setItem("joinedTeamId", teamId);
});
