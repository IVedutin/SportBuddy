let currentEmail = "";
let isEmailVerified = false;
function showMessage(text, type) {
  const container = document.getElementById("messageContainer");
  container.textContent = text;
  container.className = `message ${type}-message`;
  container.style.display = "block";
  setTimeout(() => {
    container.style.display = "none";
  }, 5000);
}
async function sendEmailCode() {
  const email = document.getElementById("email").value;
  const sendBtn = document.getElementById("sendCodeBtn");
  if (!email || !email.includes("@") || !email.includes(".")) {
    showMessage("Введите корректный email", "error");
    return;
  }
  try {
    sendBtn.disabled = true;
    sendBtn.textContent = "Отправка...";
    currentEmail = email;
    const response = await fetch("/api/register/send-email-code", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email }),
    });
    const data = await response.json();
    if (data.success) {
      document.getElementById("emailCodeSection").style.display = "block";
      showMessage("Код подтверждения отправлен на ваш email", "success");
      document.getElementById("email").readOnly = true;
      sendBtn.textContent = "Код отправлен";
    } else {
      showMessage(data.message || "Ошибка при отправке кода", "error");
      sendBtn.disabled = false;
      sendBtn.textContent = "Подтвердить email";
    }
  } catch (error) {
    showMessage("Ошибка соединения с сервером", "error");
    console.error("Send code error:", error);
    sendBtn.disabled = false;
    sendBtn.textContent = "Подтвердить email";
  }
}
async function verifyEmailCode() {
  const code = document.getElementById("emailCode").value;
  if (!code || code.length !== 6) {
    showMessage("Введите 6-значный код", "error");
    return;
  }
  try {
    const response = await fetch("/api/register/verify-email-code", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: currentEmail,
        code: code,
      }),
    });
    const data = await response.json();
    if (data.success) {
      isEmailVerified = true;
      document.getElementById("emailVerified").value = "true";
      const emailField = document.getElementById("email");
      const parent = emailField.parentElement;
      const badge = document.createElement("span");
      badge.className = "verified-badge";
      badge.textContent = "Email подтвержден";
      const oldBadge = document.querySelector(".verified-badge");
      if (oldBadge) oldBadge.remove();
      parent.appendChild(badge);
      document.getElementById("sendCodeBtn").disabled = true;
      document.getElementById("sendCodeBtn").textContent = "Email подтвержден";
      document.getElementById("emailCodeSection").style.display = "none";
      showMessage("Email успешно подтвержден!", "success");
    } else {
      showMessage(data.message || "Неверный код", "error");
    }
  } catch (error) {
    showMessage("Ошибка соединения с сервером", "error");
    console.error("Verify code error:", error);
  }
}
document.getElementById("registerForm").addEventListener("submit", async function (e) {
  e.preventDefault();
  if (document.getElementById("emailVerified").value !== "true") {
    showMessage("Пожалуйста, подтвердите email", "error");
    return;
  }
  const firstName = document.getElementById("firstName").value;
  const lastName = document.getElementById("lastName").value;
  const email = document.getElementById("email").value;
  const phone = document.getElementById("phone").value;
  const password = document.getElementById("password").value;
  const confirmPassword = document.getElementById("confirmPassword").value;
  if (password !== confirmPassword) {
    showMessage("Пароли не совпадают!", "error");
    return;
  }
  if (password.length < 6) {
    showMessage("Пароль должен быть не менее 6 символов", "error");
    return;
  }
  const phoneDigits = phone.replace(/\D/g, "");
  if (phoneDigits.length < 10) {
    showMessage("Введите корректный номер телефона", "error");
    return;
  }
  const registerBtn = document.getElementById("registerBtn");
  registerBtn.disabled = true;
  registerBtn.textContent = "Регистрация...";
  const formData = {
    firstName: firstName,
    lastName: lastName,
    email: email,
    phone: phone,
    password: password,
  };
  try {
    const response = await fetch("/api/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(formData),
    });
    const data = await response.json();
    if (data.success) {
      showMessage(
        "Регистрация успешна! Перенаправляем на страницу входа...",
        "success",
      );
      setTimeout(() => {
        window.location.href = "/login";
      }, 2000);
    } else {
      showMessage(data.message || "Ошибка при регистрации", "error");
      registerBtn.disabled = false;
      registerBtn.textContent = "Создать аккаунт";
    }
  } catch (error) {
    console.error("Registration error:", error);
    showMessage("Ошибка соединения с сервером", "error");
    registerBtn.disabled = false;
    registerBtn.textContent = "Создать аккаунт";
  }
});