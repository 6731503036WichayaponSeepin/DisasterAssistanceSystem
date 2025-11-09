// 🔹 ตั้งค่าเริ่มต้น
const start = [19.9097, 99.8264];
const map = L.map("map").setView(start, 13);

// 🔹 โหลดแผนที่จาก OpenStreetMap
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
  maxZoom: 19,
  attribution: "&copy; OpenStreetMap contributors"
}).addTo(map);

// 🔹 ฟังก์ชันอัปเดตที่อยู่เมื่อหมุดถูกลาก
function updateAddressOnDrag(marker) {
  marker.on("dragend", function (e) {
    const latlng = e.target.getLatLng();
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latlng.lat}&lon=${latlng.lng}&format=json`)
      .then(res => res.json())
      .then(data => {
        if (data && data.display_name) {
          document.getElementById("address").value = data.display_name;
          marker.bindPopup(`📍 ${data.display_name}`).openPopup();
        } else {
          document.getElementById("address").value = "ไม่พบข้อมูลที่อยู่";
        }
      })
      .catch(() => {
        document.getElementById("address").value = "เกิดข้อผิดพลาดในการดึงข้อมูลที่อยู่";
      });
  });
}

// 🔹 Marker ที่ลากได้
let marker = L.marker(start, { draggable: true }).addTo(map);
marker.bindPopup("📍 ลากเพื่อเลือกตำแหน่ง").openPopup();
updateAddressOnDrag(marker); // ✅ ผูก event ตอนสร้างครั้งแรก

// 🔹 ปุ่ม Confirm
function confirmLocation() {
  const latlng = marker.getLatLng();
  const address = document.getElementById("address").value;

  localStorage.setItem("selectedLat", latlng.lat);
  localStorage.setItem("selectedLng", latlng.lng);
  localStorage.setItem("selectedAddress", address);

  alert(`✅ บันทึกตำแหน่งเรียบร้อย!\nLat: ${latlng.lat}\nLng: ${latlng.lng}`);
}

// 🔹 เมื่อพิมพ์ที่อยู่แล้วกด Enter ให้ปักหมุดอัตโนมัติ
const addressInput = document.getElementById("address");
addressInput.addEventListener("change", () => {
  const query = addressInput.value.trim();
  if (!query) return;

  fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`)
    .then(res => res.json())
    .then(data => {
      if (data.length > 0) {
        const { lat, lon, display_name } = data[0];

        map.setView([lat, lon], 16);
        if (marker) map.removeLayer(marker);
        marker = L.marker([lat, lon], { draggable: true }).addTo(map);
        marker.bindPopup(`📍 ${display_name}`).openPopup();

        updateAddressOnDrag(marker); // ✅ ผูก event ใหม่หลังปักหมุดใหม่
      } else {
        alert("❌ ไม่พบตำแหน่งตามที่อยู่ที่ระบุ");
      }
    })
    .catch(err => {
      console.error("Geocoding error:", err);
      alert("เกิดข้อผิดพลาดในการค้นหาที่อยู่");
    });
});

// 🔹 ปุ่มระบุตำแหน่งปัจจุบัน
document.getElementById("locate-btn").addEventListener("click", () => {
  if (!navigator.geolocation) {
    alert("เบราว์เซอร์นี้ไม่รองรับการระบุตำแหน่ง");
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const lat = position.coords.latitude;
      const lon = position.coords.longitude;

      map.setView([lat, lon], 16);

      if (marker) map.removeLayer(marker);
      marker = L.marker([lat, lon], { draggable: true }).addTo(map);
      marker.bindPopup("📍 ตำแหน่งปัจจุบันของคุณ").openPopup();

      updateAddressOnDrag(marker); // ✅ ผูก event หลังจากสร้าง marker ใหม่

      // ดึงข้อมูลที่อยู่จากพิกัด
      fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`)
        .then(res => res.json())
        .then(data => {
          if (data && data.display_name) {
            document.getElementById("address").value = data.display_name;
          }
        });
    },
    (error) => {
      console.error(error);
      alert("❌ ไม่สามารถเข้าถึงตำแหน่งของคุณได้ กรุณาเปิด GPS หรืออนุญาตการระบุตำแหน่ง");
    }
  );
});
