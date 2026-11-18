(() => {
  const header = document.querySelector('.admin-topbar');
  const sidebar = document.querySelector('.admin-sidebar');
  if (!header || !sidebar) return;

  const icon = (paths) => '<svg viewBox="0 0 24 24" aria-hidden="true">' + paths + '</svg>';
  const person = icon('<circle cx="12" cy="7" r="4"/><path d="M4 22v-3a8 8 0 0 1 16 0v3"/>');
  const chevron = icon('<path d="m8 10 4 4 4-4"/>');
  const bell = icon('<path d="M5 10a7 7 0 0 1 14 0v5l2 3H3l2-3zm5 11h4"/>');
  const exit = icon('<path d="M9 4H4v16h5m6-16 6 8-6 8m-6-8h12"/>');

  const wrapper = document.createElement('div');
  wrapper.className = 'account-dropdown';
  wrapper.innerHTML = `
    <button id="account-toggle" class="account-toggle" type="button" aria-label="Mở menu tài khoản" aria-expanded="false" aria-controls="account-panel">${person}</button>
    <div id="account-panel" class="account-panel" hidden>
      <div class="account-panel-heading"><strong id="account-name">Tài khoản</strong><span id="account-role"></span></div>
      <button id="account-notifications" class="account-action" type="button" aria-expanded="false" aria-controls="account-notice">${bell}<span>Thông báo</span></button>
      <div id="account-notice" class="account-notice" role="status" hidden></div>
      <div id="account-theme-slot"></div>
      <div id="account-logout-slot"></div>
    </div>`;
  header.append(wrapper);

  const footer = document.createElement('button');
  footer.type = 'button';
  footer.className = 'sidebar-account';
  footer.setAttribute('aria-label', 'Mở menu tài khoản');
  footer.setAttribute('aria-expanded', 'false');
  footer.setAttribute('aria-controls', 'account-panel');
  footer.innerHTML = '<span class="sidebar-avatar">Q</span><span class="sidebar-identity"><strong>Quản trị viên</strong></span>' + chevron;
  sidebar.append(footer);

  const trigger = document.getElementById('account-toggle');
  const panel = document.getElementById('account-panel');
  function setOpen(open) {
    panel.hidden = !open;
    trigger.setAttribute('aria-expanded', String(open));
    footer.setAttribute('aria-expanded', String(open));
  }
  trigger.addEventListener('click', () => setOpen(panel.hidden));
  footer.addEventListener('click', () => {
    setOpen(panel.hidden);
    if (!panel.hidden) trigger.focus();
  });
  document.addEventListener('click', event => {
    if (!wrapper.contains(event.target) && !footer.contains(event.target)) setOpen(false);
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !panel.hidden) {
      setOpen(false);
      trigger.focus();
    }
  });
  const theme = document.getElementById('ui-theme');
  if (theme) {
    theme.classList.add('account-action');
    document.getElementById('account-theme-slot').append(theme);
  }

  // Keep the existing page's authenticated logout handler.
  const logout = document.getElementById('logout');
  if (logout) {
    logout.classList.add('account-action');
    logout.insertAdjacentHTML('afterbegin', exit);
    document.getElementById('account-logout-slot').append(logout);
  }

  let account = null;
  const session = fetch('/api/auth/me').then(async response => {
    if (!response.ok) return null;
    account = await response.json();
    const role = account.vaiTro === 'ADMIN' ? 'Quản trị viên' : 'Nhân viên';
    const name = account.hoTen || account.email || role;
    document.getElementById('account-name').textContent = name;
    document.getElementById('account-role').textContent = role;
    footer.querySelector('strong').textContent = name;
    footer.querySelector('.sidebar-avatar').textContent = (account.hoTen || role).trim().charAt(0).toUpperCase();
    return account;
  }).catch(() => null);

  document.getElementById('account-notifications').addEventListener('click', async event => {
    const button = event.currentTarget;
    const notice = document.getElementById('account-notice');
    const open = notice.hidden;
    notice.hidden = !open;
    button.setAttribute('aria-expanded', String(open));
    if (!open) return;
    notice.textContent = 'Đang tải thông báo…';
    const current = await session;
    if (!current) {
      notice.textContent = 'Không thể tải thông báo. Vui lòng đăng nhập lại.';
      return;
    }
    if (current.vaiTro !== 'ADMIN') {
      notice.textContent = 'Chưa có thông báo.';
      return;
    }
    try {
      const response = await fetch('/api/admin/payments/summary');
      if (!response.ok) throw new Error();
      const summary = await response.json();
      if (!summary.soDonChoThanhToan) {
        notice.textContent = 'Không có đơn chờ xác nhận.';
        return;
      }
      const link = document.createElement('a');
      link.href = '/QuanTriThanhToan.html';
      link.textContent = summary.soDonChoThanhToan + ' đơn đang chờ xác nhận thanh toán.';
      notice.replaceChildren(link);
    } catch {
      notice.textContent = 'Không thể tải thông báo. Hãy mở lại để thử lại.';
    }
  });
})();
