const {
  timeSlotId,
  chatUrl,
  chatCreationLockTime,
  chatCreatorFullName,
  currentUserId,
  chatCreatorId,
} = window.chatData || {};
const chatSection = document.getElementById("chat-section");
function renderChatSection() {
  if (chatUrl) {
    chatSection.innerHTML = `
      <a href="${chatUrl}" target="_blank" class="pt_btn" style="background: #0077FF; color: white; text-align: center; width: 100%;">Перейти в чат Max</a>
    `;
    return;
  }
  if (chatCreationLockTime && new Date(chatCreationLockTime) > new Date()) {
    if (currentUserId === chatCreatorId) {
      chatSection.innerHTML = `
        <p class="pt_chat-message">Вы создаете чат. Вставьте ссылку-приглашение ниже:</p>
        <div class="pt_chat-form" style="width: 100%;">
          <input type="text" id="chat-url-input" class="pt_input" style="width: 100%; padding: 0.75rem; margin-bottom: 0.5rem;" placeholder="https://max.ru/join/...">
          <button id="save-chat-url-btn" class="pt_btn" style="width: 100%;">Сохранить ссылку</button>
        </div>
      `;
      document
        .getElementById("save-chat-url-btn")
        .addEventListener("click", saveChatUrl);
    }
    else {
      chatSection.innerHTML = `
        <p class="pt_chat-message">${chatCreatorFullName} создает чат... Пожалуйста, подождите.</p>
        <button class="pt_btn" disabled style="background: #b0b0b0; width: 100%; cursor: not-allowed;">Создать чат</button>
      `;
    }
    return;
  }
  chatSection.innerHTML = `
    <button id="lock-chat-btn" class="pt_btn" style="width: 100%;">Создать чат события в Max</button>
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
    if (response.ok) {
      window.location.reload();
    }
  } catch (error) {
    alert("Произошла сетевая ошибка.");
  }
}
async function saveChatUrl() {
  const chatUrlInput = document.getElementById("chat-url-input");
  const url = chatUrlInput.value;
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
    if (response.ok) {
      window.location.reload();
    }
  } catch (error) {
    alert("Произошла сетевая ошибка.");
  }
}
renderChatSection();
