const form = document.getElementById("loginForm");
if (form) {
  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const errorDiv = document.getElementById("errorMessage");
    errorDiv.style.display = "none";
    const formData = new FormData();
    formData.append("username", email);
    formData.append("password", password);
    try {
      const response = await fetch("/login", {
        method: "POST",
        body: formData,
      });
      if (response.ok) {
        window.location.href = "/dashboard";
      } else {
        const errorText = await response.text();
        errorDiv.textContent =
          errorText || "Неверный email или пароль";
        errorDiv.style.display = "block";
      }
    } catch (error) {
      errorDiv.textContent = "Ошибка соединения с сервером";
      errorDiv.style.display = "block";
      console.error("Login error:", error);
    }
  });
}