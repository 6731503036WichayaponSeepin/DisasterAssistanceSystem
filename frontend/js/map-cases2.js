// ===== toggle bottom sheet (ของเดิมคุณใช้ได้อยู่) =====
const sheet = document.getElementById('sheet');
const header = document.getElementById('sheetHeader');
const selectBtn = document.getElementById('selectBtn');
const nextBtn  = document.getElementById('nextBtn'); // << ต้องมีปุ่มนี้ใน HTML

function openSheet(){ sheet.classList.remove('peek'); }
function collapseSheet(){ sheet.classList.add('peek'); }
function toggleSheet(){ sheet.classList.contains('peek') ? openSheet() : collapseSheet(); }
collapseSheet();
header.addEventListener('click', toggleSheet);

// ===== โหมด Select =====
let selecting = false;
const cards = document.querySelectorAll('[data-card]');

// ปุ่ม Select = เข้า/ออกโหมดเลือก (ยังไม่เปลี่ยนหน้า)
selectBtn.addEventListener('click', () => {
  selecting = !selecting;
  selectBtn.textContent = selecting ? 'Done' : 'Select';
});

// คลิกการ์ดเพื่อเลือก (แสดงวงกลมน้ำเงิน)
cards.forEach(card => {
  card.addEventListener('click', () => {
    if (!selecting) return;
    card.classList.toggle('selected');
    const chk = card.querySelector('.check');
    if (chk) chk.style.display = card.classList.contains('selected') ? 'flex' : 'none';
  });
});

// กด Next → เก็บเฉพาะการ์ดที่เลือก แล้วไปหน้า selected-cases.html
nextBtn.addEventListener('click', () => {
  const payload = [...document.querySelectorAll('.card.selected')].map(card => {
    const name    = card.querySelector('input[name="name"]')?.value || '';
    const number  = card.querySelector('input[name="number"]')?.value || '';
    const address = card.querySelector('input[name="address"]')?.value || '';
    // priority จากสีจุด
    const dot = card.querySelector('.dot');
    let priority = 'red';
    if (dot?.classList.contains('orange')) priority = 'orange';
    else if (dot?.classList.contains('yellow')) priority = 'yellow';
    return { name, number, address, priority };
  });

  // เซฟใส่ sessionStorage ด้วย "กุญแจเดียวกัน"
  sessionStorage.setItem('selectedCases', JSON.stringify(payload));

  // ไปหน้า selected-cases.html (ไม่ใช่ selected-cases2)
  window.location.href = './selected-cases.html';
});
