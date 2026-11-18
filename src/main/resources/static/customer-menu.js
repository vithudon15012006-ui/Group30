"use strict";
(() => {
  document
    .getElementById("close-account")
    .addEventListener("click", () =>
      document.getElementById("customer-account").close(),
    );
  const link = document.getElementById("account-link"),
    logout = document.getElementById("logout");
  const host = document.createElement("div");
  host.className = "customer-menu";
  link.before(host);
  host.append(link);
  const panel = document.createElement("div");
  panel.className = "customer-menu-panel";
  panel.hidden = true;
  host.append(panel);
  const icons = {
    settings:
      '<path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z"/><path d="m9 3-1 3-3 1-2 3 2 3v4l3 1 2 3 3-2 4-1 1-3 3-2-2-3V6l-4-1-2-2Z"/>',
    history: '<path d="M3 11a9 9 0 1 1 2 7M3 4v7h7M12 7v5l3 2"/>',
    bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/>',
    moon: '<path d="M20 15A9 9 0 0 1 9 4a9 9 0 1 0 11 11Z"/>',
    sun: '<circle cx="12" cy="12" r="4"/><path d="M12 1v3m0 16v3M1 12h3m16 0h3M4 4l2 2m12 12 2 2M4 20l2-2M18 6l2-2"/>',
    logout: '<path d="M9 4H4v16h5M9 12h12m-4-4 4 4-4 4"/>',
  };
  const labelButton = (b, label, icon) => {
    b.replaceChildren();
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("aria-hidden", "true");
    svg.innerHTML = icons[icon];
    const span = document.createElement("span");
    span.textContent = label;
    b.append(svg, span);
  };
  const action = (label, fn) => {
    const b = document.createElement("button");
    b.type = "button";
    labelButton(
      b,
      label,
      label === "Cài đặt"
        ? "settings"
        : label === "Lịch sử đặt vé"
          ? "history"
          : label === "Thông báo"
            ? "bell"
            : "moon",
    );
    b.onclick = fn;
    panel.append(b);
    return b;
  };
  const close = () => {
    panel.hidden = true;
    link.setAttribute("aria-expanded", "false");
  };
  const openAccount = (view) => {
    close();
    window.openCustomerAccount(view);
  };
  action("Cài đặt", () => openAccount("profile"));
  action("Lịch sử đặt vé", () => openAccount("history"));
  const notice = document.createElement("p");
  notice.className = "customer-notice";
  notice.hidden = true;
  notice.setAttribute("role", "status");
  action("Thông báo", async () => {
    notice.hidden = !notice.hidden;
    if (notice.hidden) return;
    notice.textContent = "Đang tải…";
    try {
      const response = await fetch("/api/account/orders");
      if (!response.ok) throw Error();
      const orders = await response.json();
      const pending = orders.filter(
        (o) => o.trangThaiThanhToan === "PENDING",
      ).length;
      notice.textContent = pending
        ? "Có " +
          pending +
          " đơn đang chờ thanh toán. Xem trong Lịch sử đặt vé."
        : "Bạn không có thông báo mới.";
    } catch {
      notice.textContent = "Chưa tải được thông báo.";
    }
  });
  panel.append(notice);
  const theme = action("Chế độ tối", () =>
    setTheme(document.documentElement.dataset.theme !== "dark"),
  );
  function setTheme(dark) {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    labelButton(
      theme,
      dark ? "Chế độ sáng" : "Chế độ tối",
      dark ? "sun" : "moon",
    );
    localStorage.setItem("cinema-customer-theme", dark ? "dark" : "light");
  }
  setTheme(localStorage.getItem("cinema-customer-theme") === "dark");
  labelButton(logout, "Đăng xuất", "logout");
  panel.append(logout);
  logout.classList.add("customer-logout");
  link.addEventListener("click", (event) => {
    if (!link.querySelector("svg")) return;
    event.preventDefault();
    panel.hidden = !panel.hidden;
    link.setAttribute("aria-expanded", String(!panel.hidden));
  });
  document.addEventListener("click", (event) => {
    if (!host.contains(event.target)) close();
  });
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") close();
  });
})();
