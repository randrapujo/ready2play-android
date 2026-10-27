'use strict';
/* Ready2Play Launcher — Android Edition (Renderer & Gesture Engine) */

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

/* ---------- Mock Android Bridge (Untuk Browser Preview & Standalone) ---------- */
const MockAndroidBridge = {
  isAndroid: () => false,
  getInstalledApps: async () => {
    const saved = localStorage.getItem('r2p_installed_apps');
    if (saved) return JSON.parse(saved);
    const mock = [
      {
        id: 'app:com.mobile.legends',
        name: 'Mobile Legends: Bang Bang',
        packageName: 'com.mobile.legends',
        genre: 'MOBA / Arkade',
        source: 'android',
        enabled: true,
        cover: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1600&q=80'
      },
      {
        id: 'app:com.miHoYo.GenshinImpact',
        name: 'Genshin Impact',
        packageName: 'com.miHoYo.GenshinImpact',
        genre: 'Action RPG / Open World',
        source: 'android',
        enabled: true,
        cover: 'https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=1600&q=80'
      },
      {
        id: 'app:org.ppsspp.ppsspp',
        name: 'PPSSPP (PSP Emulator)',
        packageName: 'org.ppsspp.ppsspp',
        genre: 'Emulator Konsol',
        source: 'emu',
        enabled: true,
        cover: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1600&q=80'
      },
      {
        id: 'app:com.mojang.minecraftpe',
        name: 'Minecraft',
        packageName: 'com.mojang.minecraftpe',
        genre: 'Sandbox / Survival',
        source: 'android',
        enabled: true,
        cover: 'https://images.unsplash.com/photo-1627856013091-fed6e4e30025?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1627856013091-fed6e4e30025?w=1600&q=80'
      }
    ];
    localStorage.setItem('r2p_installed_apps', JSON.stringify(mock));
    return mock;
  },
  getCustomPorts: async () => {
    const saved = localStorage.getItem('r2p_custom_ports');
    if (saved) return JSON.parse(saved);
    const mock = [
      {
        id: 'port:hk-port',
        name: 'Hollow Knight (Fan Port)',
        packageName: 'com.jack.hk',
        genre: 'Metroidvania',
        source: 'port',
        cover: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1600&q=80'
      },
      {
        id: 'port:defjam-port',
        name: 'Def Jam Fight for NY (AetherSX2)',
        packageName: 'xyz.aethersx2.android',
        genre: 'Fighting / Fighting Arkade',
        source: 'port',
        cover: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&q=80',
        backdrop: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1600&q=80'
      }
    ];
    localStorage.setItem('r2p_custom_ports', JSON.stringify(mock));
    return mock;
  },
  saveCustomPorts: async (ports) => {
    localStorage.setItem('r2p_custom_ports', JSON.stringify(ports));
    return { ok: true };
  },
  saveInstalledApps: async (apps) => {
    localStorage.setItem('r2p_installed_apps', JSON.stringify(apps));
    return { ok: true };
  },
  getLastPlayed: async () => {
    const saved = localStorage.getItem('r2p_last_played');
    return saved ? JSON.parse(saved) : {};
  },
  setLastPlayed: async (id, ts) => {
    const saved = localStorage.getItem('r2p_last_played');
    const lp = saved ? JSON.parse(saved) : {};
    lp[id] = ts;
    localStorage.setItem('r2p_last_played', JSON.stringify(lp));
  },
  launchApp: async (pkg) => {
    return { ok: true, message: 'Simulasi peluncuran paket ' + pkg };
  },
  quit: () => {
    alert('Keluar dari launcher (di Android ini akan meminimalkan aplikasi).');
  }
};

/* Deteksi Bridge Android Asli vs Mock */
const bridge = window.AndroidBridge || {
  isAndroid: () => typeof window.Android !== 'undefined',
  getInstalledApps: async () => {
    if (window.Android && window.Android.getInstalledApps) {
      try { return JSON.parse(window.Android.getInstalledApps()); } catch (e) { console.error(e); }
    }
    return MockAndroidBridge.getInstalledApps();
  },
  getCustomPorts: async () => {
    if (window.Android && window.Android.getCustomPorts) {
      try { return JSON.parse(window.Android.getCustomPorts()); } catch (e) { console.error(e); }
    }
    return MockAndroidBridge.getCustomPorts();
  },
  saveCustomPorts: async (ports) => {
    if (window.Android && window.Android.saveCustomPorts) {
      window.Android.saveCustomPorts(JSON.stringify(ports));
      return { ok: true };
    }
    return MockAndroidBridge.saveCustomPorts(ports);
  },
  saveInstalledApps: async (apps) => {
    if (window.Android && window.Android.saveInstalledApps) {
      window.Android.saveInstalledApps(JSON.stringify(apps));
      return { ok: true };
    }
    return MockAndroidBridge.saveInstalledApps(apps);
  },
  getLastPlayed: async () => {
    if (window.Android && window.Android.getLastPlayed) {
      try { return JSON.parse(window.Android.getLastPlayed()); } catch (e) { console.error(e); }
    }
    return MockAndroidBridge.getLastPlayed();
  },
  setLastPlayed: async (id, ts) => {
    if (window.Android && window.Android.setLastPlayed) {
      window.Android.setLastPlayed(id, ts);
      return;
    }
    return MockAndroidBridge.setLastPlayed(id, ts);
  },
  launchApp: async (item) => {
    if (window.Android && window.Android.launchApp) {
      const res = window.Android.launchApp(item.packageName || item.id);
      return { ok: res };
    }
    return MockAndroidBridge.launchApp(item.packageName || item.id);
  },
  quit: () => {
    if (window.Android && window.Android.quit) {
      window.Android.quit();
      return;
    }
    MockAndroidBridge.quit();
  }
};

/* ---------- State Aplikasi ---------- */
let library = { ports: [], apps: [], lastPlayed: {} };
let games = [];
let selected = 0;
let filterText = '';
let overlayOpen = null;

const row = $('#tile-row');
const backdrop = $('#backdrop');

/* ---------- Format & Helpers ---------- */
function fmtLastPlayed(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  const now = new Date();
  const sameDay = d.toDateString() === now.toDateString();
  const yest = new Date(now - 864e5).toDateString() === d.toDateString();
  const jam = 'pukul ' + String(d.getHours()).padStart(2, '0') + '.' + String(d.getMinutes()).padStart(2, '0');
  if (sameDay) return 'Terakhir dimainkan: hari ini ' + jam;
  if (yest) return 'Terakhir dimainkan: kemarin ' + jam;
  return 'Terakhir dimainkan: ' + d.toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });
}

function status(msg) { 
  $('#status-line').textContent = msg || ''; 
}

/* ---------- Muat Perpustakaan Game ---------- */
async function loadLibrary() {
  const [ports, apps, lastPlayed] = await Promise.all([
    bridge.getCustomPorts(),
    bridge.getInstalledApps(),
    bridge.getLastPlayed()
  ]);

  library.ports = Array.isArray(ports) ? ports : [];
  library.apps = Array.isArray(apps) ? apps : [];
  library.lastPlayed = lastPlayed || {};

  applyFilter();
}

function applyFilter() {
  const activeApps = library.apps.filter(a => a.enabled !== false);
  const all = [...library.ports, ...activeApps];
  
  games = filterText
    ? all.filter(g => g.name.toLowerCase().includes(filterText.toLowerCase()))
    : all;

  if (selected >= games.length) {
    selected = Math.max(0, games.length - 1);
  }
  render();
}

/* ---------- Render Antarmuka ---------- */
function render() {
  row.innerHTML = '';
  if (games.length === 0) {
    $('#game-title').textContent = filterText ? 'Tidak ada game cocok' : 'Perpustakaan Kosong';
    $('#game-meta').textContent = filterText
      ? 'Coba kata kunci lain atau bersihkan kotak pencarian.'
      : 'Belum ada game. Buka Pengaturan (⚙) untuk menambahkan game fan port atau aktifkan aplikasi.';
    $('#game-badges').innerHTML = '';
    $('#game-last').textContent = '';
    $('#btn-play').disabled = true;
    backdrop.style.opacity = 0;
    return;
  }

  $('#btn-play').disabled = false;

  games.forEach((g, i) => {
    const t = document.createElement('div');
    t.className = 'tile' + (i === selected ? ' selected' : '');
    
    if (g.cover) {
      t.style.backgroundImage = 'url("' + g.cover + '")';
    } else {
      t.classList.add('noart');
    }

    // Tag Kategori
    const tag = document.createElement('span');
    const isPort = g.source === 'port';
    const isEmu = g.source === 'emu';
    tag.className = 'tile-tag ' + (isPort ? 'port' : (isEmu ? 'emu' : ''));
    tag.textContent = isPort ? 'FAN PORT' : (isEmu ? 'EMULATOR' : 'ANDROID');
    t.appendChild(tag);

    const nm = document.createElement('div');
    nm.className = 'tile-name';
    nm.textContent = g.name;
    t.appendChild(nm);

    t.title = g.name;

    // Klik / Tap untuk memilih
    t.addEventListener('click', () => {
      if (selected === i) {
        launchSelected();
      } else {
        selected = i;
        SFX.move();
        render();
      }
    });

    row.appendChild(t);
  });

  const g = games[selected];
  if (!g) return;

  // Background Key Art
  const bg = g.backdrop || g.cover;
  if (bg) {
    backdrop.style.backgroundImage = 'url("' + bg + '")';
    backdrop.style.opacity = 1;
  } else {
    backdrop.style.opacity = 0;
  }

  // Panel Informasi
  $('#game-title').textContent = g.name;
  const metaParts = [g.genre, g.source === 'port' ? 'Fan Port Community' : (g.source === 'emu' ? 'Emulator Konsol' : 'Aplikasi Android')];
  $('#game-meta').textContent = metaParts.filter(Boolean).join('  •  ');

  const badgeType = g.source === 'port' ? 'port">FAN PORT' : (g.source === 'emu' ? 'emu">EMULATOR' : '">ANDROID');
  $('#game-badges').innerHTML = '<span class="badge ' + badgeType + '</span>';
  $('#game-last').textContent = fmtLastPlayed(library.lastPlayed[g.id]);

  const selElem = row.children[selected];
  if (selElem) {
    selElem.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
  }
}

/* ---------- Console Sound Effects (SFX Engine) ---------- */
const SFX = {
  ctx: null,
  enabled: true,

  init() {
    if (!this.ctx && (window.AudioContext || window.webkitAudioContext)) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioCtx();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  },

  // Suara geser navigasi konsol (soft, pleasant futuristic tick)
  move() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    try {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      const now = this.ctx.currentTime;

      osc.type = 'sine';
      osc.frequency.setValueAtTime(420, now);
      osc.frequency.exponentialRampToValueAtTime(620, now + 0.04);

      gain.gain.setValueAtTime(0.08, now);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.05);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now);
      osc.stop(now + 0.05);
    } catch (e) {}
  },

  // Suara konfirmasi / tombol Mainkan (rich ascending power chord)
  launch() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    try {
      const now = this.ctx.currentTime;
      [523.25, 659.25, 783.99, 1046.50].forEach((freq, i) => {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        const delay = now + (i * 0.045);

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, delay);

        gain.gain.setValueAtTime(0.12, delay);
        gain.gain.exponentialRampToValueAtTime(0.0001, delay + 0.25);

        osc.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start(delay);
        osc.stop(delay + 0.26);
      });
    } catch (e) {}
  },

  // Suara buka dialog / overlay
  open() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    try {
      const now = this.ctx.currentTime;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, now);
      osc.frequency.exponentialRampToValueAtTime(880, now + 0.08);

      gain.gain.setValueAtTime(0.09, now);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.12);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now);
      osc.stop(now + 0.12);
    } catch (e) {}
  },

  // Suara kembali / tutup dialog
  back() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;

    try {
      const now = this.ctx.currentTime;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(700, now);
      osc.frequency.exponentialRampToValueAtTime(420, now + 0.07);

      gain.gain.setValueAtTime(0.07, now);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.09);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now);
      osc.stop(now + 0.09);
    } catch (e) {}
  }
};

/* Unlock audio otomatis saat user berinteraksi */
['click', 'touchstart', 'keydown'].forEach(evt => {
  window.addEventListener(evt, () => SFX.init(), { once: true });
});

/* ---------- Navigasi ---------- */
function move(dir) {
  if (games.length === 0) return;
  const prev = selected;
  selected = Math.min(games.length - 1, Math.max(0, selected + dir));
  if (prev !== selected) {
    SFX.move();
    render();
  }
}

async function launchSelected() {
  const g = games[selected];
  if (!g) return;

  SFX.launch();
  status('Meluncurkan ' + g.name + '...');
  const res = await bridge.launchApp(g);
  
  if (res && res.ok) {
    status('');
    const now = Date.now();
    await bridge.setLastPlayed(g.id, now);
    library.lastPlayed[g.id] = now;
    $('#game-last').textContent = fmtLastPlayed(now);
  } else {
    status('Gagal meluncurkan: ' + (res.message || 'Aplikasi tidak ditemukan'));
  }
}

/* ---------- Touch Gestures (Swipe Navigasi Layar HP) ---------- */
let touchStartX = 0;
let touchStartY = 0;
let touchStartTime = 0;

window.addEventListener('touchstart', (e) => {
  if (overlayOpen) return;
  const t = e.touches[0];
  touchStartX = t.clientX;
  touchStartY = t.clientY;
  touchStartTime = performance.now();
}, { passive: true });

window.addEventListener('touchend', (e) => {
  if (overlayOpen) return;
  const t = e.changedTouches[0];
  const deltaX = t.clientX - touchStartX;
  const deltaY = t.clientY - touchStartY;
  const deltaTime = performance.now() - touchStartTime;

  // Swipe horizontal terdeteksi jika pergeseran X cukup signifikan
  if (Math.abs(deltaX) > 45 && Math.abs(deltaX) > Math.abs(deltaY) * 1.5 && deltaTime < 400) {
    if (deltaX < 0) {
      move(1);  // Swipe ke kiri -> Game berikutnya
    } else {
      move(-1); // Swipe ke kanan -> Game sebelumnya
    }
  }
}, { passive: true });

/* ---------- Overlay Pencarian & Pengaturan ---------- */
function openOverlay(which) {
  overlayOpen = which;
  SFX.open();
  $('#search-overlay').classList.toggle('hidden', which !== 'search');
  $('#settings-overlay').classList.toggle('hidden', which !== 'settings');

  if (which === 'search') {
    const input = $('#search-input');
    input.value = filterText;
    setTimeout(() => input.focus(), 100);
  } else if (which === 'settings') {
    renderPortList();
    renderInstalledAppsList();
  }
}

function closeOverlay() {
  if (overlayOpen) SFX.back();
  overlayOpen = null;
  $('#search-overlay').classList.add('hidden');
  $('#settings-overlay').classList.add('hidden');
  row.focus();
}

/* ---------- Tab Switcher di Settings ---------- */
$$('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    $$('.tab-btn').forEach(b => b.classList.remove('active'));
    $$('.tab-content').forEach(c => c.classList.remove('active'));
    btn.classList.add('active');
    const target = $('#' + btn.dataset.tab);
    if (target) target.classList.add('active');
  });
});

/* ---------- Render List Settings ---------- */
function renderPortList() {
  const list = $('#port-list');
  list.innerHTML = '';
  if (library.ports.length === 0) {
    list.innerHTML = '<p class="sheet-note">Belum ada game fan port / ROM kustom terdaftar.</p>';
    return;
  }
  library.ports.forEach((g) => {
    const item = document.createElement('div');
    item.className = 'port-item';

    const grow = document.createElement('div');
    grow.className = 'grow';
    grow.innerHTML = '<b></b><small></small>';
    grow.querySelector('b').textContent = g.name;
    grow.querySelector('small').textContent = g.packageName || 'Path kustom';

    const del = document.createElement('button');
    del.type = 'button';
    del.className = 'danger';
    del.textContent = 'Hapus';
    del.addEventListener('click', async () => {
      library.ports = library.ports.filter(p => p.id !== g.id);
      await bridge.saveCustomPorts(library.ports);
      applyFilter();
      renderPortList();
    });

    item.appendChild(grow);
    item.appendChild(del);
    list.appendChild(item);
  });
}

function renderInstalledAppsList() {
  const container = $('#installed-apps-list');
  container.innerHTML = '';

  if (library.apps.length === 0) {
    container.innerHTML = '<p class="sheet-note">Tidak ada aplikasi Android ditemukan.</p>';
    return;
  }

  library.apps.forEach((app) => {
    const item = document.createElement('div');
    item.className = 'app-item';

    const grow = document.createElement('div');
    grow.className = 'grow';
    grow.innerHTML = '<b></b><small></small>';
    grow.querySelector('b').textContent = app.name;
    grow.querySelector('small').textContent = app.packageName;

    const toggle = document.createElement('button');
    toggle.type = 'button';
    const isEnabled = app.enabled !== false;
    toggle.textContent = isEnabled ? 'Tampilkan' : 'Disembunyikan';
    if (isEnabled) toggle.classList.add('primary');

    toggle.addEventListener('click', async () => {
      app.enabled = !isEnabled;
      await bridge.saveInstalledApps(library.apps);
      applyFilter();
      renderInstalledAppsList();
    });

    item.appendChild(grow);
    item.appendChild(toggle);
    container.appendChild(item);
  });
}

/* Form Tambah Game Fan Port */
$('#add-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const entry = {
    id: 'port:' + Date.now(),
    name: $('#f-name').value.trim(),
    genre: $('#f-genre').value.trim() || 'Fan Port',
    packageName: $('#f-pkg').value.trim(),
    source: 'port',
    cover: $('#f-cover').value.trim(),
    backdrop: $('#f-backdrop').value.trim()
  };

  if (!entry.name || !entry.packageName) return;

  library.ports.push(entry);
  await bridge.saveCustomPorts(library.ports);
  e.target.reset();
  applyFilter();
  renderPortList();
  status('Game ditambahkan: ' + entry.name);
});

/* ---------- Event Listeners UI ---------- */
$('#btn-sound').addEventListener('click', () => {
  SFX.enabled = !SFX.enabled;
  $('#btn-sound').textContent = SFX.enabled ? '🔊' : '🔇';
  $('#btn-sound').title = SFX.enabled ? 'Efek Suara Konsol (Aktif)' : 'Efek Suara Konsol (Bisu)';
  if (SFX.enabled) SFX.open();
});
$('#btn-play').addEventListener('click', launchSelected);
$('#btn-search').addEventListener('click', () => openOverlay('search'));
$('#btn-settings').addEventListener('click', () => openOverlay('settings'));
$('#btn-close-settings').addEventListener('click', closeOverlay);
$('#btn-close-search').addEventListener('click', closeOverlay);
$('#btn-quit').addEventListener('click', () => bridge.quit());

$('#search-input').addEventListener('input', (e) => {
  filterText = e.target.value;
  selected = 0;
  applyFilter();
});
$('#search-input').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') closeOverlay();
});

/* ---------- Keyboard & Remote Navigation ---------- */
document.addEventListener('keydown', (e) => {
  if (overlayOpen === 'search') {
    if (e.key === 'Escape') { filterText = ''; selected = 0; applyFilter(); closeOverlay(); }
    return;
  }
  if (overlayOpen === 'settings') {
    if (e.key === 'Escape') closeOverlay();
    return;
  }
  switch (e.key) {
    case 'ArrowRight': case 'd': case 'D': move(1); e.preventDefault(); break;
    case 'ArrowLeft': case 'a': case 'A': move(-1); e.preventDefault(); break;
    case 'Enter': launchSelected(); break;
    case 'F2': openOverlay('settings'); break;
    case 'F3': openOverlay('search'); break;
    case 'Escape': bridge.quit(); break;
  }
});

/* ---------- Gamepad Controller Support (PS / Xbox / Android Gamepad) ---------- */
const padState = { lastMove: 0, buttons: {} };
function pollGamepad() {
  const pads = navigator.getGamepads ? navigator.getGamepads() : [];
  const gp = pads && [...pads].find(p => p && p.connected);

  if (gp && !overlayOpen) {
    const now = performance.now();
    const axis = gp.axes[0] || 0;
    let dir = 0;

    // D-Pad or Left Stick
    if (gp.buttons[15] && gp.buttons[15].pressed) dir = 1;
    if (gp.buttons[14] && gp.buttons[14].pressed) dir = -1;
    if (axis > 0.5) dir = 1;
    if (axis < -0.5) dir = -1;

    if (dir !== 0 && now - padState.lastMove > 200) {
      move(dir);
      padState.lastMove = now;
    }

    const press = (idx) => {
      const b = gp.buttons[idx];
      const down = !!(b && b.pressed);
      const was = !!padState.buttons[idx];
      padState.buttons[idx] = down;
      return down && !was;
    };

    if (press(0)) launchSelected();       // X / A (Main)
    if (press(3)) openOverlay('search');   // Triangle / Y (Cari)
    if (press(9)) openOverlay('settings'); // Start / Options
    if (press(1)) bridge.quit();           // Circle / B (Keluar)
  }

  requestAnimationFrame(pollGamepad);
}

/* ---------- Jam Realtime ---------- */
function tickClock() {
  const d = new Date();
  $('#clock').textContent = String(d.getHours()).padStart(2, '0') + '.' + String(d.getMinutes()).padStart(2, '0');
}
setInterval(tickClock, 5000);
tickClock();

/* ---------- Inisialisasi ---------- */
loadLibrary().then(() => {
  row.focus();
});
requestAnimationFrame(pollGamepad);
