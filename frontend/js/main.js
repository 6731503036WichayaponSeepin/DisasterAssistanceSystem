// ============================
// main.js (Secure + JWT version)
// ============================

// ✅ ตรวจสอบ JWT ก่อนทำงาน
const token = localStorage.getItem("jwt_token");
if (!token) {
  alert("⚠️ กรุณาเข้าสู่ระบบก่อน");
  window.location.href = "signin.html";
}

// ✅ ดึง element ต่าง ๆ
const provinceSelect = document.getElementById("province");
const districtSelect = document.getElementById("district");
const subdistrictSelect = document.getElementById("subdistrict");
const postalSelect = document.getElementById("postalCode");
const houseNumberInput = document.getElementById("houseNumber");
const moreDetailsInput = document.getElementById("moreDetails");

// -------------------------------
// 🔹 โหลดรายชื่อจังหวัดทั้งหมด
// -------------------------------
async function loadProvinces() {
  try {
    console.log("📡 โหลดจังหวัด...");
    const res = await fetch("http://localhost:8080/api/location/provinces", {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) throw new Error("HTTP " + res.status);

    const provinces = await res.json();
    if (!Array.isArray(provinces)) {
      console.error("❌ Response ไม่ใช่ array:", provinces);
      return;
    }

    provinces.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      opt.textContent = p.name || p.provinceName;
      provinceSelect.appendChild(opt);
    });
  } catch (err) {
    console.error("❌ โหลดจังหวัดล้มเหลว:", err);
  }
}

// -------------------------------
// 🔹 เมื่อเลือกจังหวัด → โหลดอำเภอ
// -------------------------------
provinceSelect.addEventListener("change", async function () {
  const provinceId = this.value;
  districtSelect.innerHTML = '<option value="">-- เลือกอำเภอ --</option>';
  subdistrictSelect.innerHTML = '<option value="">-- เลือกตำบล --</option>';
  postalSelect.innerHTML = '<option value="">-- เลือกรหัสไปรษณีย์ --</option>';

  if (!provinceId) {
    districtSelect.disabled = true;
    subdistrictSelect.disabled = true;
    postalSelect.disabled = true;
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/location/districts/${provinceId}`, {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) throw new Error("HTTP " + res.status);

    const districts = await res.json();
    if (!Array.isArray(districts)) return;

    districts.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = d.name || d.districtName;
      districtSelect.appendChild(opt);
    });

    districtSelect.disabled = false;
  } catch (err) {
    console.error("❌ โหลดอำเภอล้มเหลว:", err);
  }
});

// -------------------------------
// 🔹 เมื่อเลือกอำเภอ → โหลดตำบล
// -------------------------------
districtSelect.addEventListener("change", async function () {
  const districtId = this.value;
  subdistrictSelect.innerHTML = '<option value="">-- เลือกตำบล --</option>';
  postalSelect.innerHTML = '<option value="">-- เลือกรหัสไปรษณีย์ --</option>';

  if (!districtId) {
    subdistrictSelect.disabled = true;
    postalSelect.disabled = true;
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/location/subdistricts/${districtId}`, {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) throw new Error("HTTP " + res.status);

    const subs = await res.json();
    if (!Array.isArray(subs)) return;

    subs.forEach((s) => {
      const opt = document.createElement("option");
      opt.value = s.id;
      opt.textContent = s.name || s.subdistrictName;
      subdistrictSelect.appendChild(opt);
    });

    subdistrictSelect.disabled = false;
  } catch (err) {
    console.error("❌ โหลดตำบลล้มเหลว:", err);
  }
});

// -------------------------------
// 🔹 เมื่อเลือกตำบล → โหลดรหัสไปรษณีย์
// -------------------------------
subdistrictSelect.addEventListener("change", async function () {
  const subdistrictId = this.value;
  postalSelect.innerHTML = '<option value="">-- เลือกรหัสไปรษณีย์ --</option>';

  if (!subdistrictId) {
    postalSelect.disabled = true;
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/location/postal/${subdistrictId}`, {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) throw new Error("HTTP " + res.status);

    const postals = await res.json();
    if (!Array.isArray(postals)) return;

    postals.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      opt.textContent = p.code || p.postalCode;
      postalSelect.appendChild(opt);
    });

    postalSelect.disabled = false;
  } catch (err) {
    console.error("❌ โหลดรหัสไปรษณีย์ล้มเหลว:", err);
  }
});

// -------------------------------
// 🔹 ฟังก์ชันบันทึกที่อยู่
// -------------------------------
async function saveAddress(event) {
  event.preventDefault();

  const houseNumber = houseNumberInput.value.trim();
  const moreDetails = moreDetailsInput.value.trim();
  const subdistrictId = subdistrictSelect.value;

  if (!houseNumber || !subdistrictId) {
    alert("⚠️ กรุณากรอกบ้านเลขที่และเลือกตำบลให้ครบ");
    return;
  }

  const payload = {
    houseNumber: houseNumber,
    moreDetails: moreDetails,
    subdistrict: { id: subdistrictId },
  };

  try {
    const res = await fetch("http://localhost:8080/api/address", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token,
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) throw new Error("Server Error: " + res.status);
    const saved = await res.json();

    alert("✅ บันทึกที่อยู่เรียบร้อยแล้ว!");
    console.log("📦 Address saved:", saved);

    // ✅ ตรวจ token อีกครั้งก่อนกลับหน้า home
    const checkToken = localStorage.getItem("jwt_token");
    if (!checkToken) {
      alert("⚠️ Session ของคุณหมดอายุ กรุณาเข้าสู่ระบบใหม่");
      window.location.href = "signin.html";
      return;
    }

    // ✅ แสดงข้อความแจ้งเตือนก่อนกลับหน้า home
    alert("✅ บันทึกข้อมูลเรียบร้อยแล้ว!\nระบบจะพาคุณกลับไปยังหน้าแรก");

    // ✅ redirect พร้อม token (กัน cache/refresh ปัญหา)
    window.location.href = `home.html?token=${encodeURIComponent(checkToken)}`;
  } catch (err) {
    console.error("❌ บันทึกที่อยู่ล้มเหลว:", err);
    alert("❌ ไม่สามารถบันทึกที่อยู่ได้");
  }
}

// ✅ โหลดจังหวัดเมื่อเปิดหน้า
loadProvinces();
