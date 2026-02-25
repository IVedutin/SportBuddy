document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(".addcourt_form");
  if (!form) return;
  form.addEventListener("submit", (e) => {
    const maxPlayersInput = document.getElementById("maxPlayers");
    if (!maxPlayersInput) return;
    const maxPlayers = parseInt(maxPlayersInput.value, 10);
    if (maxPlayers < 2) {
      e.preventDefault();
      alert("Максимальное количество игроков должно быть не менее 2");
    }
  });
});