(function () {
  const marquee = document.getElementById("sportsMarquee");
  const track = document.getElementById("sportsTrack");
  if (!marquee || !track) return;

  function buildMarquee() {
    // 1) собрать исходные карточки 
    const originals = Array.from(track.querySelectorAll(".sport-card[data-original='true'], .sport-card:not([data-clone])"));

    // если это первый запуск — пометим оригиналы
    originals.forEach(card => {
      card.dataset.original = "true";
      delete card.dataset.clone;
    });

    // удалить старые клоны
    track.querySelectorAll("[data-clone='true']").forEach(el => el.remove());
    track.querySelectorAll(".marquee-group").forEach(el => el.remove());

    // 2) упаковать оригиналы в группу
    const group = document.createElement("div");
    group.className = "marquee-group";
    group.style.display = "flex";
    group.style.gap = "16px";

    originals.forEach(card => group.appendChild(card));
    track.appendChild(group);

    // 3) измерить ширину одного круга
    const loopWidth = group.getBoundingClientRect().width;
    track.style.setProperty("--loop-width", loopWidth + "px");

    // 4) клонировать группу
    let totalWidth = loopWidth;
    let safety = 0;
    while (totalWidth < marquee.clientWidth + loopWidth && safety < 20) {
      const clone = group.cloneNode(true);
      clone.dataset.clone = "true";
      clone.querySelectorAll(".sport-card").forEach(c => c.dataset.clone = "true");
      track.appendChild(clone);
      totalWidth += loopWidth;
      safety++;
    }

    // 5) скорость
    const pxPerSec = 90; // скорость движения (меняй)
    const duration = Math.max(10, loopWidth / pxPerSec);
    track.style.setProperty("--marquee-duration", duration + "s");
  }

  let t = null;
  function rebuild() {
    clearTimeout(t);
    t = setTimeout(buildMarquee, 80);
  }

  buildMarquee();
  window.addEventListener("resize", rebuild);
})();
