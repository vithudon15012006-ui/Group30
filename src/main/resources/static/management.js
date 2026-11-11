"use strict";
const specs = [
  ["Phim", "/api/phim", "maPhim:Mã phim,tenPhim:Tên phim,thoiLuong:Thời lượng (phút):number,ngayKhoiChieu:Ngày khởi chiếu:date,doanhThuUsd:Doanh thu (USD):number", true],
  ["Khách hàng", "/api/khach-hang", "maKhachHang:Mã khách hàng,hoTen:Họ tên,ngaySinh:Ngày sinh:date,sdt:Số điện thoại:tel,email:Email:email", true],
  ["Nhân viên", "nhan-vien", "MaNhanVien:Mã nhân viên,HoTen:Họ tên,NgaySinh:Ngày sinh:date,Sdt:Số điện thoại:tel,Email:Email:email,MaChucVu:Chức vụ", true],
  ["Phòng chiếu", "phong-chieu", "MaPhong:Mã phòng,TenPhong:Tên phòng,LoaiPhong:Loại phòng,Tang:Tầng:number,SucChua:Sức chứa:number", true],
  ["Suất chiếu", "suat-chieu", "MaSuatChieu:Mã suất,MaPhim:Mã phim,MaPhong:Mã phòng,NgayChieu:Ngày chiếu:date,GioBatDau:Bắt đầu:time,GioKetThuc:Kết thúc:time,MaQuanLy:Mã quản lý", false],
  ["Vé", "ve", "MaVe:Mã vé,MaSuatChieu:Mã suất,MaPhong:Mã phòng,MaGhe:Mã ghế,Gia:Giá vé (đ):number,MaHoaDon:Mã hóa đơn,MaNVBanve:Nhân viên bán vé", true],
  ["Hóa đơn", "hoa-don", "MaHoaDon:Mã hóa đơn,MaKhachHang:Mã khách,ThoiGianThanhToan:Thời gian thanh toán:datetime-local,HinhThucThanhToan:Hình thức,ThanhTien:Thành tiền (đ):number", true],
  ["Sản phẩm", "san-pham", "MaSanPham:Mã sản phẩm,TenSanPham:Tên sản phẩm,Gia:Giá (đ):number", true]
].map(([label, path, cols, edit = false]) => ({
  label, url: path.startsWith("/") ? path : "/api/danh-muc/" + path,
  slug: path.split("/").at(-1), edit,
  cols: cols.split(",").map(col => col.split(":"))
}));
const el = id => document.getElementById(id);
const catalog = el("catalog"), form = el("edit-form"), search = el("search");
const PAGE_SIZE = 20;
let spec = specs[0], data = [], page = 0, editId = null, version = 0, busy = false;
let pendingDelete = null;
let isAdmin = false;
const inputs = new Map();
const base = "/api/danh-muc/";
const canEdit = () => spec.edit || (isAdmin && spec.slug === "suat-chieu");
const references = {
  MaChucVu: [base+"chuc-vu","MaChucVu","TenChucVu"],
  MaPhim: ["/api/phim","maPhim","tenPhim"],
  MaPhong: [base+"phong-chieu","MaPhong","TenPhong"],
  MaQuanLy: [base+"quan-ly","MaQuanLy"],
  MaSuatChieu: [base+"suat-chieu","MaSuatChieu"],
  MaGhe: [base+"ghe","MaGhe","LoaiGhe"],
  MaHoaDon: [base+"hoa-don","MaHoaDon"],
  MaNVBanve: [base+"nhan-vien-thuong","MaNVThuong"],
  MaKhachHang: ["/api/khach-hang","maKhachHang","hoTen"]
};
const detailSpecs = {
  phim: [["Thể loại","the-loai","MaPhim",["TheLoai"]],["Đạo diễn","dao-dien","MaPhim",["DaoDien"]]],
  "phong-chieu": [["Ghế","ghe","MaPhong",["MaGhe","LoaiGhe"]]],
  "nhan-vien": [["Chức vụ","chuc-vu","MaChucVu",["TenChucVu"],"MaChucVu"],
                ["Lương cứng","quan-ly","MaQuanLy",["LuongCung"]],
                ["Lương theo giờ","nhan-vien-thuong","MaNVThuong",["LuongTheoGio"]],
                ["Ca được phân","phan-ca","MaNV",["MaCaLam"]]],
  "hoa-don": [["Sản phẩm trên hóa đơn","chi-tiet-san-pham","MaHoaDon",["MaSanPham","SoLuong"]],
              ["Vé trên hóa đơn","ve","MaHoaDon",["MaVe","MaSuatChieu","MaGhe"]]]
};
const required = {
  "suat-chieu": ["MaQuanLy","MaPhong","NgayChieu","GioBatDau","GioKetThuc"],
  ve: ["MaPhong","MaGhe","Gia","MaHoaDon","MaNVBanve"],
  "san-pham": ["Gia"]
};

function make(tag, text) {
  const item = document.createElement(tag);
  if (text !== undefined) item.textContent = text;
  return item;
}
function status(text, error = false) {
  el("message").textContent = text;
  el("message").classList.toggle("error", error);
}
function render() {
  const keyword = search.value.trim().toLocaleLowerCase("vi");
  const filtered = data.filter(row => spec.cols.some(([key]) =>
    String(row[key] ?? "").toLocaleLowerCase("vi").includes(keyword)));
  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  page = Math.min(page, pages - 1);
  const head = make("tr");
  for (const [, label] of spec.cols) {
    const th = make("th", label); th.scope = "col"; head.append(th);
  }
  if (canEdit() || detailSpecs[spec.slug]) head.append(make("th", "Thao tác"));
  el("headers").replaceChildren(head);
  const result = document.createDocumentFragment();
  for (const row of filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)) {
    const tr = make("tr");
    for (const [key] of spec.cols) tr.append(make("td", row[key] ?? "—"));
    if (canEdit() || detailSpecs[spec.slug]) {
      const cell = make("td"); cell.className = "actions";
      const edit = make("button", "Sửa"), remove = make("button", "Xóa");
      edit.type = remove.type = "button"; remove.className = "delete";
      edit.addEventListener("click", () => showEdit(row));
      remove.addEventListener("click", () => deleteRow(row));
      if (detailSpecs[spec.slug]) {
        const detail = make("button", "Chi tiết"); detail.type = "button";
        detail.addEventListener("click", () => showDetails(row));
        cell.append(detail);
      }
      if (canEdit() && !Number(row.DatTrucTuyen)) cell.append(edit, remove);
      if (Number(row.DatTrucTuyen)) cell.append(make("span", "Đặt trực tuyến"));
      tr.append(cell);
    }
    result.append(tr);
  }
  if (!filtered.length) {
    const tr = make("tr"), td = make("td", "Không có dữ liệu phù hợp.");
    td.colSpan = head.children.length; tr.append(td); result.append(tr);
  }
  el("rows").replaceChildren(result);
  el("pagination").hidden = pages === 1;
  el("page").textContent = "Trang " + (page + 1) + "/" + pages;
  el("previous").disabled = page === 0;
  el("next").disabled = page === pages - 1;
  return filtered.length + " dòng" + (keyword ? " phù hợp" : "");
}
function resetForm() {
  editId = null;
  form.reset();
  for (const input of inputs.values()) input.readOnly = false;
  updateSeatOptions();
  el("save").textContent = "Thêm";
}
function buildForm() {
  inputs.clear();
  el("fields").replaceChildren();
  el("add").hidden = !canEdit();
  if (canEdit()) spec.cols.forEach(([key, label, type = "text"], index) => {
    if (spec.slug === "suat-chieu" && index === 0) return;
    const field = make("label", label), input = make(key === "HinhThucThanhToan" ? "select" : "input");
    if (input.tagName === "SELECT") {
      for (const value of ["","Tiền mặt","Chuyển khoản"]) {
        const option = make("option", value || "Chọn hình thức"); option.value = value; input.append(option);
      }
    } else {
      input.type = type;
      if (type === "number" && key !== "Tang") input.min = "0";
      if (type === "time" || type === "datetime-local") input.step = "1";
      if (index === 0) input.pattern = "[A-Za-z0-9_\\-]+";
    }
    input.required = index < 2 || (required[spec.slug] ?? []).includes(key);
    field.append(input); el("fields").append(field); inputs.set(key, input);
    if (index > 0 && references[key]) attachChoices(key, input, field);
  });
  if (spec.slug === "ve") {
    const show = inputs.get("MaSuatChieu");
    const room = inputs.get("MaPhong");
    show.addEventListener("change", () => {
      const match = show.choices?.find(row => row.MaSuatChieu === show.value);
      if (match) room.value = match.MaPhong;
      updateSeatOptions();
    });
    room.addEventListener("change", updateSeatOptions);
  }
  resetForm();
}
function showEdit(row) {
  resetForm();
  editId = row[spec.cols[0][0]];
  for (const [key, input] of inputs) {
    const value = row[key] ?? "";
    input.value = input.type === "datetime-local" ? String(value).replace(" ", "T") : value;
  }
  const keyInput = inputs.get(spec.cols[0][0]);
  if (keyInput) keyInput.readOnly = true;
  updateSeatOptions();
  el("save").textContent = "Cập nhật";
  el("editor-title").textContent = "Sửa " + spec.label + " · " + editId;
  modalError("edit-message", "");
  el("editor").showModal();
  inputs.get(spec.cols[1][0]).focus();
}
function showAdd() {
  resetForm();
  el("editor-title").textContent = "Thêm " + spec.label;
  modalError("edit-message", "");
  el("editor").showModal();
  inputs.values().next().value.focus();
}
function modalError(id, message) {
  const box = el(id);
  box.textContent = message;
  box.hidden = !message;
}
async function request(url, options = {}) {
  const headers = new Headers(options.headers);
  if (options.method && options.method !== "GET") {
    const token = await (await request("/api/auth/csrf")).json();
    headers.set(token.headerName, token.token);
  }
  const res = await fetch(url, { ...options, headers });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const error = new Error(body.message || "Thao tác không thành công (HTTP " + res.status + "). Kiểm tra dữ liệu rồi thử lại.");
    error.status = res.status;
    throw error;
  }
  return res;
}
async function attachChoices(key, input, field) {
  const list = make("datalist"); list.id = "choices-" + key;
  input.setAttribute("list",list.id); field.append(list);
  const [url, id, label] = references[key];
  try {
    input.choices = await (await request(url)).json();
    if (!input.isConnected) return;
    if (key === "MaGhe") { updateSeatOptions(); return; }
    for (const row of input.choices) {
      if (key === "MaHoaDon" && Number(row.DatTrucTuyen)) continue;
      const option = make("option", label ? row[label] : ""); option.value = row[id]; list.append(option);
    }
  } catch { /* The user can still enter a known code manually. */ }
}
function updateSeatOptions() {
  if (spec.slug !== "ve") return;
  const list = el("choices-MaGhe");
  const room = inputs.get("MaPhong").value;
  const show = inputs.get("MaSuatChieu").value;
  const taken = new Set(data.filter(row => row.MaSuatChieu === show && row.MaPhong === room && row.MaVe !== editId)
    .map(row => row.MaGhe));
  list.replaceChildren();
  for (const row of inputs.get("MaGhe").choices ?? []) {
    if (row.MaPhong !== room || taken.has(row.MaGhe)) continue;
    const option = make("option", row.LoaiGhe);
    option.value = row.MaGhe;
    list.append(option);
  }
}
async function showDetails(row) {
  const current = spec, id = row[current.cols[0][0]], dialog = el("details");
  el("detail-title").textContent = current.label + " · " + id;
  const content = el("detail-content");
  content.replaceChildren(make("p","Đang tải…"));
  dialog.showModal();
  try {
    const groups = await Promise.all(detailSpecs[current.slug].map(async ([title, url, key, cols, from]) => {
      const rows = await (await request(base + url)).json();
      return [title,rows.filter(r => r[key] === (from ? row[from] : id)),cols];
    }));
    content.replaceChildren();
    for (const [title, rows, cols] of groups) {
      content.append(make("h3",title));
      if (!rows.length) { content.append(make("p","Chưa có dữ liệu.")); continue; }
      const list = make("ul");
      for (const r of rows) list.append(make("li",cols.map(c => r[c] ?? "—").join(" · ")));
      content.append(list);
    }
  } catch(err) { content.replaceChildren(make("p", err.message)); }
}
el("close-details").addEventListener("click", () => el("details").close());
async function refresh(notice) {
  const target = spec, token = ++version;
  status("Đang tải…");
  try {
    const rows = await (await request(target.url, { cache: "no-store" })).json();
    if (token !== version) return;
    data = rows;
    updateSeatOptions();
    const count = render();
    status(notice ? notice + " " + count : count);
  } catch (err) {
    if (token === version) status(err.message, true);
  }
}
async function mutate(method, rowId, body) {
  if (busy) return;
  const target = spec;
  busy = true;
  const deleting = method === "DELETE";
  const dialog = el(deleting ? "delete-dialog" : "editor");
  const errorId = deleting ? "delete-message" : "edit-message";
  const buttons = [...dialog.querySelectorAll("button")];
  buttons.forEach(b => b.disabled = true);
  modalError(errorId, "");
  try {
    const guardedDelete = deleting && ["phim", "khach-hang"].includes(target.slug);
    const url = target.slug === "suat-chieu" ? "/api/admin/schedules" : guardedDelete ? base + target.slug : target.url;
    await request(url + (rowId === null ? "" : "/" + encodeURIComponent(rowId)), {
      method, ...(body ? {headers: {"Content-Type": "application/json"}, body: JSON.stringify(body)} : {})
    });
    dialog.close();
    if (target === spec) { resetForm(); await refresh(deleting ? "Đã xóa." : "Đã lưu."); }
  } catch (err) {
    modalError(errorId, err.message);
  } finally { busy = false; buttons.forEach(b => b.disabled = false); }
}
function deleteRow(row) {
  pendingDelete = row[spec.cols[0][0]];
  el("delete-description").textContent = "Bản ghi " + pendingDelete + " trong mục " + spec.label
    + " sẽ bị xóa khỏi dữ liệu local. Bạn không thể hoàn tác thao tác này trên trang.";
  modalError("delete-message", "");
  el("delete-dialog").showModal();
}
for (const [i, s] of specs.entries()) {
  const op = make("option", s.label); op.value = i; catalog.append(op);
}
catalog.addEventListener("change", () => {
  spec = specs[Number(catalog.value)]; data = []; page = 0; search.value = "";
  el("table-title").textContent = spec.label;
  const workflow = {
    "suat-chieu": ["/QuanTriLichChieu.html", "Quản lý suất chiếu và giá vé"],
    "ve": ["/QuanTriThanhToan.html", "Quản lý giao dịch và vé"],
    "hoa-don": ["/QuanTriThanhToan.html", "Quản lý thanh toán"]
  }[spec.slug];
  el("catalog-workflow").hidden = !workflow || !isAdmin;
  if (workflow) {
    el("catalog-link").href = workflow[0];
    el("catalog-link").textContent = workflow[1];
  }
  buildForm(); render(); refresh();
});
form.addEventListener("submit", e => {
  e.preventDefault();
  const body = Object.fromEntries([...inputs].map(([key, input]) => [
    spec.slug === "suat-chieu" ? key[0].toLowerCase() + key.slice(1) : key,
    input.value.trim() === "" ? null : input.type === "number" ? Number(input.value) : input.value.trim()
  ]));
  mutate(editId === null ? "POST" : "PUT", editId, body);
});
el("add").addEventListener("click", showAdd);
el("cancel-edit").addEventListener("click", () => el("editor").close());
el("cancel-delete").addEventListener("click", () => el("delete-dialog").close());
el("confirm-delete").addEventListener("click", () => { if (pendingDelete !== null) mutate("DELETE", pendingDelete); });
el("delete-dialog").addEventListener("close", () => { pendingDelete = null; });
for (const id of ["editor","delete-dialog"]) el(id).addEventListener("cancel", event => { if (busy) event.preventDefault(); });
el("search-form").addEventListener("submit", e => { e.preventDefault(); page = 0; status(render()); });
el("reload").addEventListener("click", () => { search.value = ""; page = 0; refresh(); });
el("previous").addEventListener("click", () => { if (page > 0) page--; render(); });
el("next").addEventListener("click", () => { page++; render(); });
el("logout").addEventListener("click", async () => {
  try {
    await request("/api/auth/logout", { method: "POST" });
    location.href = "/DangNhap.html";
  } catch (error) { status(error.message, true); }
});
async function initialize() {
  try {
    const user = await (await request("/api/auth/me")).json();
    if (!["STAFF", "ADMIN"].includes(user.vaiTro)) {
      location.replace("/TaiKhoan.html");
      return;
    }
    isAdmin = user.vaiTro === "ADMIN";
    el("admin-home").hidden = !isAdmin;
    el("logout").hidden = false;
    buildForm(); render(); await refresh();
  } catch (error) {
    if (error.status === 401 || error.status === 403) {
      location.replace("/DangNhap.html?next=%2FDatabase.html");
    } else status(error.message, true);
  }
}
initialize();
