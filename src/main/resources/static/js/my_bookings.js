(() => {
  function cancelBooking(bookingId) {
    if (!bookingId) {
      alert("Не удалось определить bookingId");
      return;
    }
    if (!confirm("Отменить запись?")) return;
    fetch("/court/cancel-booking", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "bookingId=" + encodeURIComponent(bookingId),
    })
      .then((response) => {
        if (response.ok) location.reload();
        else alert("Ошибка отмены записи");
      })
      .catch(() => alert("Ошибка сети"));
  }
  document.querySelectorAll(".cancel-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      cancelBooking(btn.getAttribute("data-booking-id"));
    });
  });
  const search = document.getElementById("mbSearch");
  const clear = document.getElementById("mbClear");
  const cards = Array.from(document.querySelectorAll(".bk_card"));
  function applyFilter(value) {
    const q = (value || "").trim().toLowerCase();
    cards.forEach((card) => {
      const hay = (card.getAttribute("data-search") || "").toLowerCase();
      card.style.display = hay.includes(q) ? "" : "none";
    });
  }
  if (search) {
    search.addEventListener("input", (e) => applyFilter(e.target.value));
  }
  if (clear) {
    clear.addEventListener("click", () => {
      if (search) search.value = "";
      applyFilter("");
      search?.focus?.();
    });
  }
})();
