// --- HUD preview: live values and keystrokes -------------------------
const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const $ = (s) => document.querySelector(s);

if (!reduced) {
  const fps = $('#fps'), cpsL = $('#cpsL'), cpsR = $('#cpsR');
  setInterval(() => {
    fps.textContent = 228 + Math.floor(Math.random() * 30);
  }, 700);

  const keys = Object.fromEntries([...document.querySelectorAll('.k')].map((k) => [k.dataset.key, k]));
  const press = (name, ms) => {
    const k = keys[name];
    if (!k) return;
    k.classList.add('on');
    setTimeout(() => k.classList.remove('on'), ms);
  };

  // Strafe pattern: W held, A/D alternating, plus clicks
  keys.w.classList.add('on');
  let left = true;
  setInterval(() => {
    press(left ? 'a' : 'd', 380);
    left = !left;
  }, 450);
  let clicks = [];
  setInterval(() => {
    press('l', 60);
    clicks.push(Date.now());
  }, 105);
  setInterval(() => {
    if (Math.random() < 0.15) press('space', 250);
    if (Math.random() < 0.2) press('r', 90);
    const now = Date.now();
    clicks = clicks.filter((t) => now - t < 1000);
    cpsL.textContent = clicks.length;
    cpsR.textContent = Math.random() < 0.3 ? 1 : 0;
  }, 300);
}

// --- Mod menu mock ----------------------------------------------------------
const modules = [
  { name: 'FPS', cat: 'hud', desc: 'Current frames per second.', on: true },
  { name: 'CPS', cat: 'hud', desc: 'Clicks per second.', on: true },
  { name: 'Coordinates', cat: 'hud', desc: 'Position and facing.', on: true },
  { name: 'Keystrokes', cat: 'hud', desc: 'WASD, mouse and space.', on: true },
  { name: 'Armor Status', cat: 'hud', desc: 'Armor with durability.', on: true },
  { name: 'Zoom', cat: 'visual', desc: 'Hold C, scroll to adjust.', on: true },
  { name: 'Fullbright', cat: 'visual', desc: 'Caves without torches.', on: false },
  { name: 'Custom Main Menu', cat: 'utility', desc: 'Voxira title screen.', on: true },
];
const catName = { hud: 'HUD', visual: 'Visual', utility: 'Utility' };
const grid = $('#mm-grid');
let filter = 'all';

function renderMenu() {
  grid.innerHTML = '';
  modules
    .filter((m) => filter === 'all' || m.cat === filter)
    .forEach((m) => {
      const card = document.createElement('button');
      card.className = 'mm-card' + (m.on ? ' on' : '');
      card.setAttribute('aria-pressed', m.on);
      card.innerHTML = `<b></b><small></small><span></span><i class="mm-toggle"></i>`;
      card.querySelector('b').textContent = m.name;
      card.querySelector('small').textContent = catName[m.cat];
      card.querySelector('span').textContent = m.desc;
      card.addEventListener('click', () => {
        m.on = !m.on;
        renderMenu();
      });
      grid.appendChild(card);
    });
}
document.querySelectorAll('.mm-tabs button').forEach((b) =>
  b.addEventListener('click', () => {
    filter = b.dataset.filter;
    document.querySelectorAll('.mm-tabs button').forEach((x) => {
      x.classList.toggle('active', x === b);
      x.setAttribute('aria-selected', x === b);
    });
    renderMenu();
  })
);
renderMenu();

// --- Install tabs -----------------------------------------------------
document.querySelectorAll('.tab').forEach((t) =>
  t.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach((x) => {
      x.classList.toggle('active', x === t);
      x.setAttribute('aria-selected', x === t);
    });
    document.querySelectorAll('.steps').forEach((p) => (p.hidden = p.dataset.panel !== t.dataset.tab));
  })
);
