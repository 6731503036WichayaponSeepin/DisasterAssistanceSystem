// ===== เปิด/ย่อ bottom sheet =====
const sheet  = document.getElementById('sheet');
const header = document.getElementById('sheetHeader');
const selectBtn = document.getElementById('selectBtn');
const nextBtn   = document.getElementById('nextBtn');
const cards     = document.querySelectorAll('[data-card]');

function openSheet(){
  sheet.classList.remove('peek');
  sheet.setAttribute('aria-expanded','true');
}
function collapseSheet(){
  sheet.classList.add('peek');
  sheet.setAttribute('aria-expanded','false');
}
function toggleSheet(){
  sheet.classList.contains('peek') ? openSheet() : collapseSheet();
}

// เริ่มต้นแบบ peek
collapseSheet();
header.addEventListener('click', toggleSheet);
header.addEventListener('keydown', (e)=>{
  if(e.key === 'Enter' || e.key === ' '){
    e.preventDefault();
    toggleSheet();
  }
});

// ===== โหมดเลือก =====
let selecting = false;

// เมื่อกดปุ่ม Select → เข้าสู่โหมดเลือก / Done → ออกจากโหมดเลือก
selectBtn.addEventListener('click', () => {
  selecting = !selecting;
  selectBtn.textContent = selecting ? 'Done' : 'Select';
});

// คลิกการ์ดเพื่อเลือก / ยกเลิกเลือก (ติ๊กถูกสีน้ำเงินขึ้นเมื่อเลือก)
cards.forEach(card => {
  card.addEventListener('click', () => {
    if (!selecting) return; // ต้องอยู่ในโหมด Select เท่านั้น
    card.classList.toggle('selected');
  });
});

// ===== ปุ่ม Next → เก็บข้อมูลและไป selected-cases2.html =====
nextBtn.addEventListener('click', () => {
  const selectedCards = [...document.querySelectorAll('.card.selected')].map(card => {
    const name = card.querySelector('input[name="name"]').value;
    const number = card.querySelector('input[name="number"]').value;
    const address = card.querySelector('input[name="address"]').value;
    const dot = card.querySelector('.dot');
    let priority = 'red';
    if (dot.classList.contains('orange')) priority = 'orange';
    else if (dot.classList.contains('yellow')) priority = 'yellow';
    return { name, number, address, priority };
  });

  // เก็บข้อมูลไว้ใน sessionStorage
  sessionStorage.setItem('selectedCases', JSON.stringify(selectedCards));

  // ไปหน้า selected-cases2.html
  window.location.href = './selected-cases2.html';
});
