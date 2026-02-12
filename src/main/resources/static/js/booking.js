const sportSelect = document.getElementById('sport-type-select');
const courtSelect = document.getElementById('court-select');
const courtSection = document.getElementById('court-section');
const timeSection = document.getElementById('timeSection');
const dayTabs = document.getElementById('dayTabs');
const timePills = document.getElementById('timePills');
const detailsLinkContainer = document.getElementById('details-link-container');
const MAX_PLAYERS = 20;
function resetTimeUI() {
  timeSection.style.display = 'none';
  dayTabs.innerHTML = '';
  timePills.innerHTML = '';
}
function resetCourtUI() {
  courtSection.style.display = 'none';
  courtSelect.innerHTML = '<option value="" selected disabled hidden>Выберите площадку</option>';
  detailsLinkContainer.innerHTML = '';
}
sportSelect.addEventListener('change', function () {
  const sportTypeId = this.value;
  resetTimeUI();
  resetCourtUI();
  if (!sportTypeId) return;
  fetch('/court/courts/' + sportTypeId)
    .then(r => r.json())
    .then(courts => {
      courtSelect.innerHTML = '<option value="" selected disabled hidden>Выберите площадку</option>';
      if (!courts || courts.length === 0) {
        courtSelect.innerHTML = '<option value="" selected disabled hidden>Нет площадок для этого спорта</option>';
        courtSection.style.display = 'block';
        return;
      }
      courts.forEach(court => {
        const opt = document.createElement('option');
        opt.value = court.id;
        opt.textContent = court.name;
        courtSelect.appendChild(opt);
      });
      courtSection.style.display = 'block';
    })
    .catch(() => {
      courtSelect.innerHTML = '<option value="" selected disabled hidden>Ошибка загрузки площадок</option>';
      courtSection.style.display = 'block';
    });
});
courtSelect.addEventListener('change', function () {
  const courtId = this.value;
  resetTimeUI();
  detailsLinkContainer.innerHTML = '';
  if (!courtId) return;
  const detailsLink = document.createElement('a');
  detailsLink.href = `/court/details/${courtId}`;
  detailsLink.textContent = 'Подробнее и отзывы →';
  detailsLinkContainer.appendChild(detailsLink);
  fetch('/court/timeslots/' + courtId)
    .then(r => r.json())
    .then(timeSlots => {
      const slotsByDayAll = groupTimeSlotsByDay(timeSlots || []);
      const days = Object.keys(slotsByDayAll).slice(0, 3);
      dayTabs.innerHTML = '';
      timePills.innerHTML = '';
      if (days.length === 0) {
        timePills.innerHTML = '<div style="padding:10px; font-weight:800;">Нет доступного времени</div>';
        timeSection.style.display = 'block';
        return;
      }
      days.forEach((day, idx) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'day-tab' + (idx === 0 ? ' active' : '');
        btn.textContent = day;
        btn.onclick = () => {
          document.querySelectorAll('.day-tab').forEach(x => x.classList.remove('active'));
          btn.classList.add('active');
          renderDay(day, slotsByDayAll);
        };
        dayTabs.appendChild(btn);
      });
      renderDay(days[0], slotsByDayAll);
      timeSection.style.display = 'block';
    })
    .catch(() => {
      dayTabs.innerHTML = '';
      timePills.innerHTML = '<div style="padding:10px; font-weight:800;">Ошибка загрузки времени</div>';
      timeSection.style.display = 'block';
    });
});
function renderDay(day, slotsByDayAll) {
  timePills.innerHTML = '';
  const slots = slotsByDayAll[day] || [];
  slots.forEach(slot => {
    const startTime = formatTime(slot.startTime);
    const endTime = formatTime(slot.endTime);
    const pill = document.createElement('button');
    pill.type = 'button';
    pill.className = 'time-pill';
    pill.innerHTML = `
      <div class="t">${startTime} – ${endTime}</div>
      <div class="s">Бесплатно • 0/${MAX_PLAYERS}</div>
    `;
    pill.onclick = () => bookTimeSlot(slot.id);
    timePills.appendChild(pill);
    fetch('/court/booking-count/' + slot.id)
      .then(r => r.json())
      .then(count => {
        pill.innerHTML = `
          <div class="t">${startTime} – ${endTime}</div>
          <div class="s">Бесплатно • ${count}/${MAX_PLAYERS}</div>
        `;
        if (count >= MAX_PLAYERS) {
          pill.classList.add('full');
          pill.disabled = true;
        }
      })
      .catch(() => {
      });
  });
}
function groupTimeSlotsByDay(timeSlots) {
  const groups = {};
  timeSlots.forEach(slot => {
    const date = new Date(slot.startTime);
    const day = date.toLocaleDateString('ru-RU', { weekday: 'short' });
    const dayFixed = day.charAt(0).toUpperCase() + day.slice(1, 2);
    const dayNum = date.getDate();
    const month = date.toLocaleDateString('ru-RU', { month: 'short' });
    const label = `${dayFixed}, ${dayNum} ${month}`;
    if (!groups[label]) groups[label] = [];
    groups[label].push(slot);
  });
  return groups;
}
function formatTime(dateTimeString) {
  const date = new Date(dateTimeString);
  return date.toLocaleTimeString('ru-RU', {
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/Saratov'
  });
}
function bookTimeSlot(timeSlotId) {
  if (confirm('Записаться на это время?')) {
    fetch('/court/book', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'timeSlotId=' + encodeURIComponent(timeSlotId)
    })
    .then(response => {
      if (response.ok) {
        window.location.href = '/court/participants/' + timeSlotId;
      } else {
        alert('Ошибка записи');
      }
    })
    .catch(() => alert('Ошибка сети'));
  }
}
