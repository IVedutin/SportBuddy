document.addEventListener("DOMContentLoaded", () => {
  let allMatches = [];
  const el = (id) => document.getElementById(id);
  function safeText(v) {
    return v === null || v === undefined || v === "" ? "—" : v;
  }
  function formatDate(dt) {
    try {
      return new Date(dt).toLocaleString();
    } catch (e) {
      return "—";
    }
  }
  function renderStats(matches) {
    const countEl = el("matchesCount");
    const nearestEl = el("nearestTime");
    if (!countEl || !nearestEl) return;

    countEl.textContent = matches.length;
    if (!matches.length) {
      nearestEl.textContent = "—";
      return;
    }
    const sorted = [...matches].sort(
      (a, b) => new Date(a.startTime) - new Date(b.startTime),
    );
    nearestEl.textContent = formatDate(sorted[0].startTime);
  }
  function buildParticipants(match) {
    let html = `<div class="rm_participants">
        <div class="rm_participants-title">Участники</div>`;
    if (match.participants && match.participants.length > 0) {
      html += `<div class="rm_participants-list">`;
      match.participants.forEach((p) => {
        html += `
          <span class="rm_participant">
            <span class="rm_participant-name">${safeText(p.firstName)}</span>
            <span class="rm_rating">★ ${safeText(p.rating)}</span>
          </span>
        `;
      });
      html += `</div>`;
    } else {
      html += `<div class="rm_participants-empty">Пока никого…</div>`;
    }
    html += `</div>`;
    return html;
  }
  function renderMatches(matches) {
    const container = el("matches-list");
    const empty = el("empty");
    if (!container) return;
    container.innerHTML = "";
    if (!matches || matches.length === 0) {
      if (empty) empty.style.display = "block";
      return;
    }
    if (empty) empty.style.display = "none";
    matches.forEach((match) => {
      const div = document.createElement("div");
      div.className = "rm_card";
      div.innerHTML = `
        <div class="rm_top">
          <div class="rm_title">${safeText(match.title)}</div>
          <span class="rm_badge">Рейтинг</span>
        </div>
        <div class="rm_meta">
          <div class="rm_meta-row">
            <img src="../static/images/pin.png" class="rm_icon" alt="Локация">
            ${safeText(match.locationName)}
            <span class="rm_meta-muted">(${safeText(match.address)})</span>
          </div>
          <div class="rm_meta-row">
            <img src="../static/images/clock.png" class="rm_icon" alt="Время">
            ${formatDate(match.startTime)}
          </div>
        </div>
        <div class="rm_desc">${safeText(match.description)}</div>
        ${buildParticipants(match)}
        <div class="rm_actions">
          <button class="rm_btn" type="button" data-id="${match.id}">Принять участие</button>
        </div>
      `;
      div
        .querySelector(".rm_btn")
        ?.addEventListener("click", () => joinMatch(match.id));
      container.appendChild(div);
    });
  }
  function applyFilters() {
    const qVal = (el("q")?.value || "").toLowerCase().trim();
    const sportVal = el("sport")?.value || "all";
    let filtered = allMatches;
    if (sportVal !== "all") {
      filtered = filtered.filter(
        (m) => (m.sport || m.discipline || "").toLowerCase() === sportVal,
      );
    }
    if (qVal) {
      filtered = filtered.filter((m) => {
        const hay =
          `${m.title || ""} ${m.locationName || ""} ${m.address || ""}`.toLowerCase();
        return hay.includes(qVal);
      });
    }
    renderMatches(filtered);
    renderStats(filtered);
  }
  async function loadMatches() {
    try {
      const response = await fetch("/court/api/ranked-matches");
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const matches = await response.json();
      allMatches = Array.isArray(matches) ? matches : [];
    } catch (e) {
      console.error("Ошибка загрузки матчей:", e);
      allMatches = [];
    }
    applyFilters();
  }
  async function joinMatch(matchId) {
    if (!confirm("Записаться на этот матч?")) return;
    try {
      const response = await fetch(
        `/court/api/ranked-matches/join/${matchId}`,
        {
          method: "POST",
        },
      );
      if (response.ok) {
        alert("Вы успешно записаны!");
        loadMatches();
      } else {
        alert("Ошибка записи");
      }
    } catch (e) {
      console.error("Ошибка записи:", e);
      alert("Ошибка записи");
    }
  }
  el("refresh")?.addEventListener("click", loadMatches);
  el("q")?.addEventListener("input", applyFilters);
  el("sport")?.addEventListener("change", applyFilters);
  loadMatches();
});
