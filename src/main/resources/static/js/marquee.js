(function () {
  const marquee = document.getElementById("sportsMarquee");
  const track = document.getElementById("sportsTrack");
  if (!marquee || !track) return;
  const cards = Array.from(track.children);
  cards.forEach(card => {
    const clone = card.cloneNode(true);
    track.appendChild(clone);
  });
  const loopWidth = track.scrollWidth / 2;
  const pxPerSec = 70;
  const duration = loopWidth / pxPerSec;
  track.style.setProperty("--loop-width", loopWidth + "px");
  track.style.setProperty("--marquee-duration", duration + "s");
})();