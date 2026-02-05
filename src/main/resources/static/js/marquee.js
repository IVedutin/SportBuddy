(() => {
  const marquee = document.getElementById("sportsMarquee");
  const track = document.getElementById("sportsTrack");
  if (!marquee || !track) return;
  // сохраняем оригинальные карточки один раз
  const originals = Array.from(track.querySelectorAll(".sport-card"));
  const pxPerSec = 70; // скорость (px/сек)
  const build = () => {
    // очистить дорожку и собрать группу заново
    track.innerHTML = "";
    const group = document.createElement("div");
    group.className = "marquee-group";
    group.style.display = "flex";
    group.style.gap = "16px";
    originals.forEach(card => group.appendChild(card.cloneNode(true)));
    track.appendChild(group);
    // измерить ширину одного круга
    const loopWidth = group.getBoundingClientRect().width;
    track.style.setProperty("--loop-width", `${loopWidth}px`);
    // сколько групп нужно, чтобы закрыть экран + один запасной круг
    const need = Math.ceil((marquee.clientWidth + loopWidth) / loopWidth);
    for (let i = 1; i < need; i++) {
      track.appendChild(group.cloneNode(true));
    }
    // длительность анимации
    const duration = Math.max(10, loopWidth / pxPerSec);
    track.style.setProperty("--marquee-duration", `${duration}s`);
  };
  let timer;
  const onResize = () => {
    clearTimeout(timer);
    timer = setTimeout(build, 80);
  };
  build();
  window.addEventListener("resize", onResize);
})();
