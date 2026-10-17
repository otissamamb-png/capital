const styles = `
:root {
  --navy: #0b2545;
  --royal: #1d4ed8;
  --royal-dark: #1e40af;
  --blue-light: #eff6ff;
  --blue-surface: #f0f7ff;
  --emerald: #10b981;
  --emerald-light: #ecfdf5;
  --amber: #f59e0b;
  --amber-light: #fffbeb;
  --rose: #ef4444;
  --rose-light: #fef2f2;
  --slate-900: #0f172a;
  --slate-800: #1e293b;
  --slate-700: #334155;
  --slate-600: #475569;
  --slate-500: #64748b;
  --slate-400: #94a3b8;
  --slate-300: #cbd5e1;
  --slate-200: #e2e8f0;
  --slate-100: #f1f5f9;
  --slate-50: #f8fafc;
  --white: #ffffff;
}

* { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Manrope', system-ui, -apple-system, sans-serif;
  background: var(--slate-50);
  color: var(--slate-900);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

button { font-family: inherit; cursor: pointer; border: none; background: none; font-size: 14px; }
input, textarea, select { font-family: inherit; font-size: 14px; }
a { color: var(--royal); text-decoration: none; }

.app-shell { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  background: var(--white);
  border-bottom: 1px solid var(--slate-200);
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}
.topbar-brand { display: flex; align-items: center; gap: 12px; }
.brand-mark {
  width: 40px; height: 40px; border-radius: 12px;
  background: var(--navy);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 15px; letter-spacing: 0.5px;
}
.brand-name { font-weight: 800; font-size: 16px; color: var(--navy); }
.brand-sub { font-size: 11px; font-weight: 700; color: var(--royal); letter-spacing: 1.5px; }

.nav-tabs { display: flex; gap: 4px; }
.nav-tab {
  padding: 8px 16px; border-radius: 10px; font-weight: 600; font-size: 14px;
  color: var(--slate-600); transition: all 0.15s ease;
}
.nav-tab:hover { background: var(--slate-100); color: var(--slate-900); }
.nav-tab.active { background: var(--blue-light); color: var(--royal); }

.topbar-right { display: flex; align-items: center; gap: 12px; }
.icon-btn {
  width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: var(--slate-700); font-size: 18px; transition: background 0.15s ease; position: relative;
}
.icon-btn:hover { background: var(--slate-100); }
.badge-dot {
  position: absolute; top: 4px; right: 4px; min-width: 18px; height: 18px;
  background: var(--rose); color: #fff; border-radius: 9px; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
}
.avatar {
  width: 36px; height: 36px; border-radius: 50%; background: var(--royal); color: #fff;
  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px;
}

.page { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 24px; }

.card {
  background: var(--white); border: 1px solid var(--slate-200); border-radius: 16px;
  padding: 20px; transition: box-shadow 0.15s ease;
}
.card.hover:hover { box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08); }

.btn {
  padding: 10px 20px; border-radius: 12px; font-weight: 700; font-size: 14px;
  transition: all 0.15s ease; display: inline-flex; align-items: center; gap: 8px; justify-content: center;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: var(--royal); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--royal-dark); }
.btn-outline { background: transparent; border: 1.5px solid var(--slate-300); color: var(--slate-700); }
.btn-outline:hover:not(:disabled) { border-color: var(--slate-400); background: var(--slate-50); }
.btn-danger { background: var(--rose-light); color: var(--rose); }
.btn-danger:hover:not(:disabled) { background: #fee2e2; }
.btn-sm { padding: 6px 14px; font-size: 13px; border-radius: 10px; }

.pill {
  display: inline-flex; align-items: center; gap: 6px; padding: 3px 10px;
  border-radius: 999px; font-size: 12px; font-weight: 700;
}
.pill-available { background: var(--emerald-light); color: var(--emerald); }
.pill-occupied { background: var(--rose-light); color: var(--rose); }
.pill-pending { background: var(--amber-light); color: var(--amber); }
.pill-info { background: var(--blue-light); color: var(--royal); }

input.field, textarea.field, select.field {
  width: 100%; padding: 11px 14px; border: 1.5px solid var(--slate-200);
  border-radius: 12px; background: var(--white); color: var(--slate-900);
  transition: border-color 0.15s ease; outline: none;
}
input.field:focus, textarea.field:focus, select.field:focus { border-color: var(--royal); }
input.field::placeholder, textarea.field::placeholder { color: var(--slate-400); }

.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-label { font-size: 13px; font-weight: 700; color: var(--slate-700); }

.alert {
  padding: 12px 16px; border-radius: 12px; font-size: 13px; font-weight: 600; margin-bottom: 16px;
}
.alert-error { background: var(--rose-light); color: var(--rose); }
.alert-info { background: var(--blue-light); color: var(--royal); }
.alert-success { background: var(--emerald-light); color: var(--emerald); }

.section-title { font-size: 20px; font-weight: 800; color: var(--navy); margin-bottom: 4px; }
.section-sub { font-size: 13px; color: var(--slate-500); margin-bottom: 16px; }

.grid { display: grid; gap: 16px; }
.grid-3 { grid-template-columns: repeat(3, 1fr); }
.grid-4 { grid-template-columns: repeat(4, 1fr); }
.grid-2 { grid-template-columns: repeat(2, 1fr); }

.stat-card { text-align: center; padding: 20px; }
.stat-num { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 12px; font-weight: 600; color: var(--slate-500); margin-top: 2px; }

.room-img, .product-img {
  width: 100%; height: 160px; object-fit: cover; border-radius: 12px; background: var(--slate-100);
}

.empty-state {
  text-align: center; padding: 48px 24px; color: var(--slate-500);
}
.empty-icon { font-size: 40px; margin-bottom: 12px; }

.loading { display: flex; align-items: center; justify-content: center; padding: 48px; color: var(--slate-500); font-weight: 600; }

.spinner {
  width: 32px; height: 32px; border: 3px solid var(--slate-200); border-top-color: var(--royal);
  border-radius: 50%; animation: spin 0.8s linear infinite; margin: 0 auto;
}
@keyframes spin { to { transform: rotate(360deg); } }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.5); z-index: 100;
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.modal {
  background: var(--white); border-radius: 20px; padding: 24px; width: 100%; max-width: 480px;
  max-height: 85vh; overflow-y: auto;
}

.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px;
  background: linear-gradient(135deg, var(--navy) 0%, var(--royal) 100%);
}
.auth-card { background: var(--white); border-radius: 24px; padding: 32px; width: 100%; max-width: 420px; }

.chat-bubble {
  max-width: 75%; padding: 10px 14px; border-radius: 16px; font-size: 14px;
}
.chat-mine { background: var(--royal); color: #fff; border-bottom-right-radius: 4px; margin-left: auto; }
.chat-other { background: var(--slate-100); color: var(--slate-900); border-bottom-left-radius: 4px; }

@media (max-width: 900px) {
  .grid-3, .grid-4 { grid-template-columns: repeat(2, 1fr); }
  .nav-tabs { overflow-x: auto; }
  .page { padding: 16px; }
}
@media (max-width: 600px) {
  .grid-2, .grid-3, .grid-4 { grid-template-columns: 1fr; }
  .topbar { padding: 0 12px; }
  .brand-sub { display: none; }
}
`
export default styles
