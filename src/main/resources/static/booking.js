"use strict";
const $ = id => document.getElementById(id);
const api = "/api/dat-ve";
const money = n => new Intl.NumberFormat("vi-VN",{style:"currency",currency:"VND"}).format(n);
const day = d => d ? d.split("-").reverse().join("/") : "";
const time = t => t?.slice(0,5) ?? "";
let movies = [], shows = [], seats = [], movie, showing, token = 0, busy = false;
const picked = new Set();
const quantities = new Map();
let products = [], account = null, paymentConfig = null, countdown = null;
const loginUrl = "/DangNhap.html?next=%2FDatVe.html";
const resumeKey = "cinema-booking-selection";
const productTotal = () => products.reduce((sum, product) => sum + product.gia * (quantities.get(product.maSanPham) || 0), 0);

function node(tag,text,css) {
  const e = document.createElement(tag);
  if(text !== undefined) e.textContent = text;
  if(css) e.className = css;
  return e;
}
function button(text,css,action) {
  const b = node("button",text,css); b.type = "button"; b.addEventListener("click",action); return b;
}
function note(text,error=false) {
  $("notice").hidden = !text; $("notice").textContent = text;
  $("notice").classList.toggle("error",error);
}
async function request(path,options) {
  const headers = new Headers(options?.headers);
  if (options?.method === "POST") {
    const tokenResponse = await fetch("/api/auth/csrf");
    if (!tokenResponse.ok) throw new Error("Không thể xác thực yêu cầu.");
    const token = await tokenResponse.json();
    headers.set(token.headerName, token.token);
  }
  const response = await fetch(api+path,{cache:"no-store",...options,headers});
  if(!response.ok) {
    const message = (await response.json().catch(()=>({}))).message;
    const err = new Error(message || "Không tải được dữ liệu. Vui lòng thử lại.");
    err.status = response.status; throw err;
  }
  return response.json();
}
function step(n) {
  token++;
  ["movies","shows","seats"].forEach((id,i)=>$(id).hidden=i!==n);
  note("");
}
const fold = s => s.toLowerCase().normalize("NFD").replace(/\p{Diacritic}/gu,"").replaceAll("đ","d");
function drawMovies() {
  const q=fold($("movie-search").value.trim()), list=$("movie-list");
  list.replaceChildren();
  for(const item of movies.filter(m=>fold(m.name+" "+(m.genres??"")+" "+m.id).includes(q))) {
    const card=button(undefined,"movie",()=>openShows(item));
    card.append(node("strong",item.name),node("span",[item.genres,item.minutes ? item.minutes+" phút" : ""].filter(Boolean).join(" · ")),node("span","Xem suất chiếu →"));
    list.append(card);
  }
  if(!list.childElementCount) list.append(node("p","Không tìm thấy phim phù hợp."));
}
async function openShows(item, previousDate) {
  movie=item; shows=[]; step(1);
  const current=token;
  $("show-title").textContent=item.name; $("show-list").replaceChildren();
  $("show-date").replaceChildren(); $("show-date").disabled=true; note("Đang tải suất chiếu…");
  try {
    const result=(await request("/suat-chieu?maPhim="+encodeURIComponent(item.id))).map(s => ({
      id: s.maSuatChieu, date: s.ngayChieu, start: s.gioBatDau, end: s.gioKetThuc,
      movie: s.tenPhim, room: s.maPhong, roomName: s.tenPhong, free: s.sucChua - s.soGheDaDat
    }));
    if(current!==token)return;
    shows=result; note("");
    const dates=[...new Set(shows.map(s=>s.date))];
    for(const d of dates) { const o=node("option",day(d));o.value=d;$("show-date").append(o); }
    if(dates.includes(previousDate)) $("show-date").value=previousDate;
    $("show-date").disabled=dates.length<2;
    drawShows();
  } catch(e){if(current===token)note(e.message,true);}
}
function drawShows(){
  const list=$("show-list");list.replaceChildren();
  for(const s of shows.filter(s=>s.date===$("show-date").value)){
    const card=button(undefined,"show",()=>openSeats(s));
    card.append(node("strong",time(s.start)+" – "+time(s.end)),node("span",s.roomName||s.room),
      node("span",s.free>0?s.free+" ghế trống":"Hết ghế"));
    card.disabled=s.free<=0;list.append(card);
  }
  if(!list.childElementCount)list.append(node("p","Chưa có suất chiếu cho ngày này."));
}
async function openSeats(s) {
  showing=s; picked.clear(); seats=[]; step(2);
  $("seat-show").textContent=[s.movie,day(s.date),time(s.start),s.roomName||s.room].join(" · ");
  drawSeats(); await refreshSeats();
}
async function refreshSeats(){
  const current=++token;
  $("reload-seats").disabled=true; $("checkout").disabled=true; note("Đang cập nhật ghế…");
  try{
    const result=(await request("/suat-chieu/"+encodeURIComponent(showing.id)+"/ghe")).map(s => ({
      id: s.maGhe, row: s.hangGhe, type: s.loaiGhe, sold: s.daDat, price: s.gia
    }));
    if(current!==token)return;
    seats=result; const free=new Set(seats.filter(s=>!s.sold).map(s=>s.id));
    for(const id of picked)if(!free.has(id))picked.delete(id);
    drawSeats();note(seats.length?"":"Phòng này chưa có sơ đồ ghế.");
  } catch(e){if(current===token)note(e.message,true);}
  finally {if(current===token)$("reload-seats").disabled=false;}
}
function total(){return seats.filter(s=>picked.has(s.id)).reduce((v,s)=>v+s.price,0);}
function drawSeats() {
  const map=$("seat-map");map.replaceChildren();
  const groups=new Map();
  for(const s of seats){const label=s.row||s.id.slice(0,1);if(!groups.has(label))groups.set(label,[]);groups.get(label).push(s);}
  for(const [label,group] of groups){
    const row=node("div",undefined,"seat-row"), buttons=node("div");
    row.append(node("b",label),buttons);
    for(const s of group){
      const b=button(s.id,["seat",s.sold?"sold":picked.has(s.id)?"chosen":s.type?.toUpperCase()==="VIP"?"vip":""].join(" "),()=>{
        if(picked.has(s.id))picked.delete(s.id);
        else if(picked.size<10)picked.add(s.id);
        else {note("Mỗi lần đặt tối đa 10 ghế.",true);return;}
        note("");drawSeats();
        map.querySelectorAll("button")[seats.indexOf(s)]?.focus();
      });
      b.disabled=s.sold;b.setAttribute("aria-pressed",String(picked.has(s.id)));
      b.setAttribute("aria-label","Ghế "+s.id+", "+(s.sold?"đã bán":money(s.price)));
      buttons.append(b);
    }
    map.append(row);
  }
  $("seat-selection").textContent=picked.size?[...picked].sort().join(", "):"Chưa chọn ghế";
  $("seat-total").textContent=money(total()); $("checkout").disabled=!picked.size;
}
function showReceipt(receipt) {
  const pending = receipt.trangThaiThanhToan === "PENDING";
  $("receipt-badge").textContent = pending ? "CHUYỂN KHOẢN" : "VÉ ĐIỆN TỬ";
  $("receipt-title").textContent = pending ? "Đơn đang chờ thanh toán" : "Đặt vé thành công";
  $("receipt-info").replaceChildren(
    node("h3", receipt.tenPhim),
    node("p", [day(receipt.ngayChieu), time(receipt.gioBatDau), receipt.tenPhong].join(" · ")),
    node("p", "Mã hóa đơn: " + receipt.maHoaDon),
    node("p", "Tổng tiền: " + money(receipt.thanhTien))
  );
  $("receipt-tickets").replaceChildren(...receipt.ghe.map(id => {
    const seat = seats.find(s => s.id === id);
    const ticket = (receipt.ve ?? []).find(t => t.maGhe === id);
    const row = node("li");
    row.append(node("strong", "Ghế " + id));
    if (ticket) row.append(node("code", ticket.maVe));
    if (seat) row.append(node("span", money(seat.price)));
    return row;
  }));
  const transfer = $("transfer-details");
  transfer.replaceChildren();
  transfer.hidden = !pending;
  clearInterval(countdown);
  if (pending) {
    const timer = node("strong");
    const qr = node("img");
    qr.src = receipt.qrImageUrl;
    qr.alt = "Mã QR chuyển khoản " + receipt.nganHang;
    qr.width = qr.height = 168;
    transfer.append(qr, node("p", receipt.nganHang + " · " + money(receipt.thanhTien)),
      node("p", "Nội dung chuyển khoản: " + receipt.maThamChieuChuyenKhoan), timer,
      node("p", "Nhập đúng số tiền và nội dung. Vé được xác nhận sau khi quản trị viên đối soát."));
    const expiry = new Date(receipt.hetHanThanhToan + "+07:00").getTime();
    const update = () => {
      const seconds = Math.max(0, Math.ceil((expiry - Date.now()) / 1000));
      timer.textContent = seconds ? "Giữ ghế: " + Math.floor(seconds / 60) + ":" + String(seconds % 60).padStart(2, "0") : "Đã hết thời gian giữ ghế.";
      if (!seconds) clearInterval(countdown);
    };
    countdown = setInterval(update, 1000);
    update();
  }
  $("receipt").showModal();
}
function updateCheckoutSummary() {
  $("checkout-summary").textContent = showing.movie + " · Ghế " +
    ([...picked].sort().join(", ") || "chưa chọn") + " · " + money(total() + productTotal());
}
function drawProducts() {
  $("extras").hidden = !products.length;
  $("products").replaceChildren(...products.map(product => {
    const field = node("label", product.tenSanPham + " · " + money(product.gia));
    const input = node("input");
    input.type = "number";
    input.min = "0";
    input.max = "20";
    input.value = quantities.get(product.maSanPham) || "0";
    input.addEventListener("input", () => {
      quantities.set(product.maSanPham, Math.min(20, Math.max(0, input.valueAsNumber || 0)));
      updateCheckoutSummary();
    });
    field.append(input);
    return field;
  }));
}
async function loadSession() {
  const response = await fetch("/api/auth/me");
  if (!response.ok) return;
  account = await response.json();
  const link = $("account-link");
  if (account.vaiTro === "CUSTOMER") {
    link.textContent = "Tài khoản";
    link.href = "/TaiKhoan.html";
  } else {
    link.textContent = "Quản lý";
    link.href = "/Database.html";
  }
  $("logout").hidden = false;
}
$("logout").addEventListener("click", async () => {
  try {
    const token = await (await fetch("/api/auth/csrf")).json();
    const response = await fetch("/api/auth/logout", {method:"POST",headers:{[token.headerName]:token.token}});
    if (!response.ok) throw new Error("Không thể đăng xuất.");
    location.reload();
  } catch (error) { note(error.message,true); }
});
$("movie-search").addEventListener("input",drawMovies);
$("back-movies").addEventListener("click",()=>step(0));
$("back-shows").addEventListener("click",()=>openShows(movie,showing.date));
$("show-date").addEventListener("change",drawShows);
$("reload-seats").addEventListener("click",refreshSeats);
$("checkout").addEventListener("click", async () => {
  if (!picked.size) return;
  if (!account) {
    sessionStorage.setItem(resumeKey, JSON.stringify({ movieId: movie.id, showId: showing.id, date: showing.date, seats: [...picked] }));
    location.href = loginUrl;
    return;
  }
  if (account.vaiTro !== "CUSTOMER") { note("Hãy đăng nhập bằng tài khoản khách hàng để đặt vé.", true); return; }
  const current = token;
  $("checkout").disabled = true;
  try {
    const [menu, config, profile] = await Promise.all([
      request("/do-an"), request("/payment-config"), fetch("/api/account").then(r => {
        if (!r.ok) throw new Error("Vui lòng đăng nhập lại để đặt vé.");
        return r.json();
      })
    ]);
    if (current !== token) return;
    products = menu;
    paymentConfig = config;
    $("customer-summary").textContent = [profile.hoTen, profile.sdt].filter(Boolean).join(" · ");
    $("payment").options[1].disabled = !paymentConfig.chuyenKhoanDaCauHinh;
    if ($("payment").options[1].disabled) $("payment").selectedIndex = 0;
    drawProducts();
    updateCheckoutSummary();
    $("checkout-error").hidden = true;
    $("checkout-dialog").showModal();
  } catch (error) { if (current === token) note(error.message, true); }
  finally { if (current === token) $("checkout").disabled = !picked.size; }
});
$("payment").addEventListener("change", () => {
  const transfer = $("payment").selectedIndex === 1;
  $("payment-note").hidden = !transfer;
  if (transfer) $("payment-note").textContent = "Ghế được giữ " + paymentConfig.thoiHanGiuGhePhut + " phút để chuyển khoản.";
});
$("cancel-checkout").addEventListener("click",()=>$("checkout-dialog").close());
$("checkout-dialog").addEventListener("cancel",e=>{if(busy)e.preventDefault();});
$("checkout-form").addEventListener("submit",async e=>{
  e.preventDefault();if(busy||!picked.size)return;
  busy=true;$("buy").disabled=$("cancel-checkout").disabled=true;$("checkout-error").hidden=true;
  try {
    const fields=Object.fromEntries(new FormData(e.currentTarget));
    const receipt=await request("",{method:"POST",headers:{"Content-Type":"application/json"},
      body:JSON.stringify({
        maSuatChieu:showing.id, ghe:[...picked], hinhThucThanhToan:fields.payment,
        sanPham:[...quantities].filter(([,quantity])=>quantity>0).map(([id,quantity])=>({maSanPham:id,soLuong:quantity}))
      })});
    $("checkout-dialog").close(); quantities.clear(); picked.clear(); showReceipt(receipt); await refreshSeats();
  } catch(err) {
    $("checkout-error").textContent=err.message;$("checkout-error").hidden=false;
    if(err.status===409) { await refreshSeats(); updateCheckoutSummary(); }
  } finally {busy=false;$("buy").disabled=$("cancel-checkout").disabled=false;}
});
$("close-receipt").addEventListener("click",()=>$("receipt").close());
$("print-receipt").addEventListener("click",()=>window.print());
async function initialize() {
  note("Đang tải danh sách phim…");
  const list = await request("/phim");
  movies = list.map(m => ({id:m.maPhim,name:m.tenPhim,minutes:m.thoiLuong,genres:m.theLoai}));
  drawMovies();
  note("");
  await loadSession();
  if (account?.vaiTro !== "CUSTOMER") return;
  const saved = JSON.parse(sessionStorage.getItem(resumeKey) || "null");
  sessionStorage.removeItem(resumeKey);
  const item = saved && movies.find(m => m.id === saved.movieId);
  if (!item) return;
  await openShows(item, saved.date);
  const show = shows.find(s => s.id === saved.showId);
  if (!show || $("shows").hidden) return;
  await openSeats(show);
  for (const seat of seats) if (!seat.sold && saved.seats.includes(seat.id)) picked.add(seat.id);
  drawSeats();
}
initialize().catch(error => note(error.message, true));
$("receipt").addEventListener("close", () => clearInterval(countdown));
