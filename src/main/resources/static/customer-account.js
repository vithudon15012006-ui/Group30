let customerAccountTemplate;
async function loadCustomerAccountTemplate() {
  if (!customerAccountTemplate) {
    const response = await fetch("/customer-account-template.html");
    if (!response.ok) throw new Error("Không tải được thông tin tài khoản.");
    customerAccountTemplate = await response.text();
  }
  return customerAccountTemplate;
}
window.openCustomerAccount = async function (view) {
  const host = document.getElementById("account-content");
  const root = host.shadowRoot || host.attachShadow({ mode: "open" });
  root.innerHTML = await loadCustomerAccountTemplate();
  root
    .querySelectorAll("#profile-dialog .panel")
    .forEach(
      (panel) =>
        (panel.hidden =
          view === "history"
            ? !panel.classList.contains("wide")
            : panel.classList.contains("wide")),
    );
  document.getElementById("customer-account").dataset.view = view;
  root.querySelector(".profile-dialog-head h2").textContent =
    view === "history" ? "Lịch sử đặt vé" : "Cài đặt";
  document.getElementById("customer-account").showModal();

  const $ = (selector) => root.querySelector(selector);
  let csrf = null;
  let account = null;
  function escapeHtml(value) {
    return String(value ?? "").replace(
      /[&<>"']/g,
      (char) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[char],
    );
  }
  async function api(url, options = {}) {
    const settings = { ...options, headers: { ...(options.headers || {}) } };
    if (settings.method && settings.method !== "GET") {
      csrf ||= await fetch("/api/auth/csrf").then((r) => r.json());
      settings.headers[csrf.headerName] = csrf.token;
    }
    const response = await fetch(url, settings);
    const data = response.status === 204 ? null : await response.json();
    if (!response.ok)
      throw new Error(data?.message || "Yêu cầu chưa thể hoàn tất.");
    return data;
  }
  function message(text, kind = "success") {
    const el = $("#message");
    el.textContent = text;
    el.dataset.kind = kind;
    el.style.display = "block";
    el.scrollIntoView({ behavior: "smooth", block: "center" });
  }
  function showOrders(orders) {
    const host = $("#orders");
    if (!orders.length) {
      host.innerHTML = '<div class="empty">Bạn chưa có giao dịch nào.</div>';
      return;
    }
    const money = (value) =>
      new Intl.NumberFormat("vi-VN").format(value) + " ₫";
    host.innerHTML =
      '<div class="order-table-wrap"><table class="order-table"><thead><tr><th>Mã đơn / Ngày</th><th>Vé và sản phẩm</th><th>Thanh toán</th><th>Trạng thái</th><th>Thành tiền</th></tr></thead><tbody>' +
      orders
        .map(
          (order) =>
            `<tr><td>${escapeHtml(order.maHoaDon)}<small>${escapeHtml(order.thoiGianThanhToan || "")}</small></td><td>${order.ve.map((ticket) => `<div><strong>${escapeHtml(ticket.tenPhim)}</strong><small>${escapeHtml(ticket.ngayChieu)} ${escapeHtml(ticket.gioBatDau)} · ${escapeHtml(ticket.tenPhong)} · Ghế ${escapeHtml(ticket.maGhe)}</small></div>`).join("<br>")}${order.doAn.length ? "<small>" + order.doAn.map((item) => escapeHtml(item.tenSanPham) + " × " + item.soLuong).join(", ") + "</small>" : ""}${order.trangThaiThanhToan === "PENDING" ? `<small>Nội dung chuyển khoản: ${escapeHtml(order.maThamChieuChuyenKhoan || "")}<br>Hạn thanh toán: ${escapeHtml(order.hetHanThanhToan || "")}</small>${order.qrImageUrl === "/qr-chuyen-khoan-agribank.png" ? '<img src="/qr-chuyen-khoan-agribank.png" alt="QR chuyển khoản" width="160">' : ""}` : ""}</td><td>${escapeHtml(order.hinhThucThanhToan || "—")}</td><td><span class="status">${escapeHtml({ PAID: "Đã thanh toán", PENDING: "Chờ thanh toán", EXPIRED: "Hết hạn", CANCELLED: "Đã hủy" }[order.trangThaiThanhToan] || order.trangThaiThanhToan)}</span></td><td class="money">${money(order.thanhTien)}</td></tr>`,
        )
        .join("") +
      "</tbody></table></div>";
  }
  async function initialize() {
    try {
      account = await api("/api/account");
    } catch (error) {
      location.href = `/DangNhap.html?next=${encodeURIComponent("/TaiKhoan.html")}`;
      return;
    }
    if (account.vaiTro !== "CUSTOMER") {
      location.href = "/DangNhap.html";
      return;
    }
    $("#name").value = account.hoTen || "";
    $("#phone").value = account.sdt || "";
    showOrders(await api("/api/account/orders"));
  }
  $("#profile-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      account = await api("/api/account", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          hoTen: $("#name").value.trim(),
          sdt: $("#phone").value.trim(),
        }),
      });
      message("Thông tin tài khoản đã được cập nhật.");
    } catch (error) {
      message(error.message, "error");
    }
  });
  $("#password-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
      const result = await api("/api/account/password", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          matKhauCu: $("#old-password").value,
          matKhauMoi: $("#new-password").value,
        }),
      });
      form.reset();
      message(result.message);
    } catch (error) {
      message(error.message, "error");
    }
  });

  initialize().catch((error) => message(error.message, "error"));
};
