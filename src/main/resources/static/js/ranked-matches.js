async function loadMatches() {
  const container = document.getElementById("matches-list");
  const emptyDiv = document.getElementById("empty");
  const matchesCountSpan = document.getElementById("matchesCount");
  const nearestTimeSpan = document.getElementById("nearestTime");
  container.innerHTML = "Загрузка...";
  try {
    const response = await fetch("/court/api/ranked-matches");
    const matches = await response.json();
    container.innerHTML = "";
    if (matches.length === 0) {
      container.style.display = "none";
      emptyDiv.style.display = "block";
      matchesCountSpan.textContent = "0";
      nearestTimeSpan.textContent = "—";
      return;
    }
    container.style.display = "grid";
    emptyDiv.style.display = "none";
    matchesCountSpan.textContent = matches.length;
    const now = new Date();
    const futureMatches = matches.filter((m) => new Date(m.startTime) > now);
    if (futureMatches.length > 0) {
      const nearest = new Date(
        Math.min(...futureMatches.map((m) => new Date(m.startTime)))
      );
      nearestTimeSpan.textContent = nearest.toLocaleString("ru-RU", {
        day: "numeric",
        month: "short",
        hour: "2-digit",
        minute: "2-digit",
      });
    } else {
      nearestTimeSpan.textContent = "—";
    }
    matches.forEach((match) => {
      let participantsHtml =
        '<div class="ranked_participants"><strong>Участники:</strong><br>';
      if (match.participants && match.participants.length > 0) {
        match.participants.forEach((p) => {
          participantsHtml += `
            <span class="ranked_participant">
              ${p.firstName || "Игрок"}
              <span class="ranked_rating-badge">★ ${p.rating || 0}</span>
            </span>`;
        });
      } else {
        participantsHtml += '<span style="color:gray">Пока никого...</span>';
      }
      participantsHtml += "</div>";
      const card = document.createElement("div");
      card.className = "ranked_card";
      card.innerHTML = `
        <div class="ranked_card-title">${match.title}</div>
        <div class="ranked_card-info">
          <span class="ranked_label">📍</span> ${match.locationName} (${match.address})
        </div>
        <div class="ranked_card-info">
          <span class="ranked_label">🕒</span> ${new Date(match.startTime).toLocaleString("ru-RU")}
        </div>
        <div class="ranked_card-desc">${match.description}</div>
        ${participantsHtml}
        <button class="ranked_btn ranked_btn-join" onclick="joinMatch(${match.id})">Принять участие</button>
      `;
      container.appendChild(card);
    });
  } catch (error) {
    container.innerHTML = "Ошибка загрузки матчей";
    console.error(error);
  }
}
async function joinMatch(matchId) {
  if (!confirm("Записаться на этот матч?")) return;
  try {
    const response = await fetch(`/court/api/ranked-matches/join/${matchId}`, {
      method: "POST",
    });

    if (response.ok) {
      alert("Вы успешно записаны!");
      loadMatches();
    } else {
      const error = await response.text();
      alert("Ошибка записи: " + error);
    }
  } catch (error) {
    alert("Ошибка сети");
  }
}
function filterMatches() {
  const searchTerm = document.getElementById("q").value.toLowerCase();
  const sportFilter = document.getElementById("sport").value;
  loadMatches();
}
document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("refresh").addEventListener("click", loadMatches);
  document.getElementById("q").addEventListener("input", filterMatches);
  document.getElementById("sport").addEventListener("change", filterMatches);
  loadMatches();
});