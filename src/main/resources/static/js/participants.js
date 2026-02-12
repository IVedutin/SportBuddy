(() => {
  const data = window.__PT_DATA__ || {};
  const timeSlotId = data.timeSlotId;
  const chatUrl = data.chatUrl;
  const chatCreationLockTime = data.chatCreationLockTime;
  const chatCreatorFullName = data.chatCreatorFullName;
  const currentUserId = data.currentUserId;
  const chatCreatorId = data.chatCreatorId;
  const chatSection = document.getElementById("chat-section");
  if (!chatSection) return;
  function renderChatSection() {
    if (chatUrl) {
      chatSection.innerHTML = `
        <a href="${chatUrl}" target="_blank" class="pt_chat-link">
          Перейти в чат Max
        </a>
      `;
      return;
    }
    if (chatCreationLockTime && new Date(chatCreationLockTime) > new Date()) {
      if (currentUserId === chatCreatorId) {
        chatSection.innerHTML = `
          <div class="pt_chat-muted">Вы создаете чат. Вставьте ссылку-приглашение ниже:</div>
          <input type="text" id="chat-url-input" class="pt_chat-input" placeholder="https://max.ru/join/...">
          <button id="save-chat-url-btn" class="pt_chat-btn">Сохранить ссылку</button>
        `;
        document
          .getElementById("save-chat-url-btn")
          .addEventListener("click", saveChatUrl);
      } else {
        chatSection.innerHTML = `
          <div class="pt_chat-muted">${chatCreatorFullName} создает чат... Пожалуйста, подождите.</div>
          <button class="pt_chat-btn" disabled>Создать чат</button>
        `;
      }
      return;
    }
    chatSection.innerHTML = `
      <button id="lock-chat-btn" class="pt_chat-btn">
        Создать чат события в Max
      </button>
    `;
    document
      .getElementById("lock-chat-btn")
      .addEventListener("click", lockChatCreation);
  }
  async function lockChatCreation() {
    const formData = new FormData();
    formData.append("timeSlotId", timeSlotId);
    try {
      const response = await fetch("/court/lock-chat-creation", {
        method: "POST",
        body: formData,
      });
      const message = await response.text();
      alert(message);
      if (response.ok) window.location.reload();
    } catch (error) {
      alert("Произошла сетевая ошибка.");
    }
  }
  async function saveChatUrl() {
    const chatUrlInput = document.getElementById("chat-url-input");
    const url = chatUrlInput?.value || "";
    if (!url || !url.startsWith("https://max.ru/join/")) {
      alert(
        "Пожалуйста, введите корректную ссылку-приглашение (должна начинаться с https://max.ru/join/).",
      );
      return;
    }
    const formData = new FormData();
    formData.append("timeSlotId", timeSlotId);
    formData.append("chatUrl", url);
    try {
      const response = await fetch("/court/save-chat-url", {
        method: "POST",
        body: formData,
      });
      const message = await response.text();
      alert(message);
      if (response.ok) window.location.reload();
    } catch (error) {
      alert("Произошла сетевая ошибка.");
    }
  }
  renderChatSection();
})();
