function cancelBooking(btn) {
  const bookingId = btn.getAttribute("data-booking-id");
  if (!bookingId) {
    alert("Не удалось определить bookingId");
    return;
  }
  if (confirm("Отменить запись?")) {
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
}
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".cancel-btn").forEach((button) => {
    button.addEventListener("click", function () {
      cancelBooking(this);
    });
  });
  const reviewModal = document.getElementById("review-modal");
  const courtIdInput = document.getElementById("courtIdInput");
  const modalTitle = document.getElementById("review-modal-title");
  document.querySelectorAll(".review-btn").forEach((button) => {
    button.addEventListener("click", (e) => {
      e.preventDefault();
      const courtId = button.getAttribute("data-court-id");
      const courtName = button.getAttribute("data-court-name");
      courtIdInput.value = courtId;
      modalTitle.innerText = `Отзыв о площадке: "${courtName}"`;
      reviewModal.style.display = "flex";
    });
  });
  const closeBtn = document.getElementById("close-modal-btn");
  if (closeBtn) {
    closeBtn.addEventListener("click", () => {
      reviewModal.style.display = "none";
    });
  }
  if (reviewModal) {
    reviewModal.addEventListener("click", function (event) {
      if (event.target === reviewModal) {
        reviewModal.style.display = "none";
      }
    });
  }
  const searchInput = document.getElementById("mbSearch");
  const clearBtn = document.getElementById("mbClear");
  const bookingCards = document.querySelectorAll(".bk_card");
  function filterBookings() {
    const searchTerm = (searchInput?.value || "").toLowerCase().trim();

    bookingCards.forEach((card) => {
      const searchData = card.getAttribute("data-search") || "";
      if (searchTerm === "" || searchData.toLowerCase().includes(searchTerm)) {
        card.style.display = "flex";
      } else {
        card.style.display = "none";
      }
    });
  }
  function clearSearch() {
    if (searchInput) searchInput.value = "";
    filterBookings();
  }
  if (searchInput) {
    searchInput.addEventListener("input", filterBookings);
  }
  if (clearBtn) {
    clearBtn.addEventListener("click", clearSearch);
  }
  filterBookings();
});