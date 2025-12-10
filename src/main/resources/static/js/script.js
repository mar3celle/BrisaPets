// Menu toggle - adiciona ao auth.js ou ficheiro JS próprio
document.addEventListener('DOMContentLoaded', function () {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav-menu');

  if (!toggle || !nav) return;

  toggle.addEventListener('click', function (e) {
    const isOpen = nav.classList.toggle('open');
    toggle.classList.toggle('open', isOpen);
    toggle.setAttribute('aria-expanded', String(isOpen));
  });

  // Fecha o menu ao tocar fora (opcional)
  document.addEventListener('click', function (e) {
    if (!nav.classList.contains('open')) return;

    const clickedInside = nav.contains(e.target) || toggle.contains(e.target);
    if (!clickedInside) {
      nav.classList.remove('open');
      toggle.classList.remove('open');
      toggle.setAttribute('aria-expanded', 'false');
    }
  });

  // Fecha com ESC para acessibilidade
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      nav.classList.remove('open');
      toggle.classList.remove('open');
      toggle.setAttribute('aria-expanded', 'false');
    }
  });
});
