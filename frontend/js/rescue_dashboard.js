import { initializeApp } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-app.js";
import { getDatabase, ref, set } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-database.js";

/* -----------------------------------
   🔹 ตั้งค่า Firebase
----------------------------------- */
const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_APP.firebaseapp.com",
  databaseURL: "https://YOUR_APP.firebaseio.com",
  projectId: "YOUR_APP",
  storageBucket: "YOUR_APP.appspot.com",
  messagingSenderId: "YOUR_ID",
  appId: "YOUR_APP_ID"
};
const app = initializeApp(firebaseConfig);
const database = getDatabase(app);

/* -----------------------------------
   🔹 ฟังก์ชันอัปเดตสถานะผู้ร้องขอ
----------------------------------- */
const updateBtn = document.getElementById("update-status");
const userIdInput = document.getElementById("user-id");
const statusSelect = document.getElementById("rescue-status");
const log = document.getElementById("log");

updateBtn.addEventListener("click", () => {
  const userId = userIdInput.value.trim();
  const status = statusSelect.value;

  if (!userId) {
    log.textContent = "⚠️ กรุณากรอก User ID ก่อนอัปเดต";
    return;
  }

  set(ref(database, "requests/" + userId + "/status"), status)
    .then(() => {
      log.textContent = `✅ Updated status "${status}" for user ${userId}`;
    })
    .catch((err) => {
      log.textContent = `❌ Error: ${err.message}`;
    });
});
