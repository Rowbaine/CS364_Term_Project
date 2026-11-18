// =====================================================================
//  Mood Diary — เพิ่ม Flow ปฏิทิน / สถิติ / ตั้งค่า ลงไฟล์ Figma เดิม
//  วิธีใช้: เปิดไฟล์ "Mood Diary UI — Kawaii Wood" → Plugins → Scripter
//          → วางโค้ดทั้งหมดนี้ → กด ▶ Run   (รันซ้ำได้ จะลบของเก่าที่ชื่อซ้ำก่อน)
// =====================================================================

const page = figma.root.children.find(p => p.name.includes("Screens")) || figma.root.children[0];
await figma.setCurrentPageAsync(page);

// ---------- ตัวแปรสี (Theme · Day / Theme · Night) ----------
const allVars = await figma.variables.getLocalVariablesAsync("COLOR");
const cols = await figma.variables.getLocalVariableCollectionsAsync();
const dayCol = cols.find(c => c.name === "Theme · Day");
const nightCol = cols.find(c => c.name === "Theme · Night");
const V = {}, DAYNAME = {}, NIGHT = {};
for (const v of allVars) {
  if (v.variableCollectionId === dayCol.id) { V[v.name.replace("color/", "")] = v; DAYNAME[v.id] = v.name; }
  if (v.variableCollectionId === nightCol.id) NIGHT[v.name] = v;
}
const bp = k => figma.variables.setBoundVariableForPaint({ type: "SOLID", color: { r: 0, g: 0, b: 0 } }, "color", V[k]);
const fillV = (n, k) => { n.fills = [bp(k)]; };
const strokeV = (n, k, w) => { n.strokes = [bp(k)]; n.strokeWeight = w; n.strokeAlign = "INSIDE"; };
const shadowV = (n, k, y) => {
  n.effects = [figma.variables.setBoundVariableForEffect({ type: "DROP_SHADOW", color: { r: 0, g: 0, b: 0, a: 1 }, offset: { x: 0, y }, radius: 0, spread: 0, visible: true, blendMode: "NORMAL" }, "color", V[k])];
};
const hex = h => ({ r: parseInt(h.slice(0, 2), 16) / 255, g: parseInt(h.slice(2, 4), 16) / 255, b: parseInt(h.slice(4, 6), 16) / 255 });

// ---------- ฟอนต์ ----------
for (const [family, style] of [["Mitr", "Medium"], ["Mali", "Regular"], ["Mali", "SemiBold"], ["Mali", "Bold"]])
  await figma.loadFontAsync({ family, style });
const txt = (s, fam, sty, size, k) => { const t = figma.createText(); t.fontName = { family: fam, style: sty }; t.characters = s; t.fontSize = size; if (k) fillV(t, k); return t; };

// ---------- auto-layout helpers (Plugin API มาตรฐาน) ----------
const AL = (dir, props = {}) => {
  const f = figma.createFrame(); f.layoutMode = dir; f.primaryAxisSizingMode = "AUTO"; f.counterAxisSizingMode = "AUTO";
  f.fills = []; f.clipsContent = false; Object.assign(f, props); return f;
};
const add = (p, c, fill) => { p.appendChild(c); if (fill) c.layoutSizingHorizontal = "FILL"; return c; };
const icon = (inner, sw, size, varKey = "text") => {
  const n = figma.createNodeFromSvg(`<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">${inner.replace(/\/>/g, ` stroke="#4A2E1A" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round"/>`)}</svg>`);
  n.findAll(x => x.type === "VECTOR").forEach(v => { v.strokes = [bp(varKey)]; }); return n;
};

// ---------- components ที่มีอยู่แล้ว ----------
const compPage = figma.root.children.find(p => p.name.includes("Components"));
await compPage.loadAsync();
const findSet = name => compPage.findOne(n => n.type === "COMPONENT_SET" && n.name === name);
const variant = (set, value) => set.children.find(c => c.name.split("=")[1] === value);
const faceSet = findSet("Mood Face"), btnSet = findSet("Button"), pillSet = findSet("Mood Pill"), navSet = findSet("Bottom Nav");
const Card = compPage.findOne(n => n.type === "COMPONENT" && n.name === "Entry Card");
const FACE = { happy: variant(faceSet, "Happy"), calm: variant(faceSet, "Calm"), meh: variant(faceSet, "Meh"), sad: variant(faceSet, "Sad"), grumpy: variant(faceSet, "Grumpy"), tired: variant(faceSet, "Tired"), sleepy: variant(faceSet, "Sleepy") };
const LABEL = { happy: "ดีใจ", calm: "สบายใจ", meh: "เฉยๆ", sad: "เศร้า", grumpy: "หงุดหงิด", tired: "เหนื่อย" };
const COLOR = { happy: "FFB7C5", calm: "FFC27A", meh: "BFE08A", sad: "A9C4F5", grumpy: "FF9A8F", tired: "D2AE8A" };
const btnLabelKey = Object.keys(btnSet.componentPropertyDefinitions).find(k => k.startsWith("Label"));
const button = (style, label) => { const i = variant(btnSet, style).createInstance(); i.setProperties({ [btnLabelKey]: label }); return i; };
const face = (m, size) => { const i = FACE[m].createInstance(); i.resize(size, size); return i; };

// ---------- หาหน้าจอเดิม ----------
const byPrefix = p => page.children.find(f => f.name.startsWith(p));
const Home = byPrefix("1 · "), Pick = byPrefix("2 · "), Detail = byPrefix("4 · "), Settings = byPrefix("5 · ");
const NightHome = byPrefix("6 · "), Exit = byPrefix("7 · "), Draft = byPrefix("9 · "), Empty = byPrefix("10 · "), Small = byPrefix("11 · "), Tablet = byPrefix("12 · ");

// ลบของเก่า (กรณีรันซ้ำ)
for (const p of ["13 · ", "14 · ", "15 · ", "16 · ", "17 · ", "18 · "]) page.children.filter(f => f.name.startsWith(p)).forEach(f => f.remove());

const ROW_Y = 3500;
const screen = (name, x) => {
  const s = AL("VERTICAL", { name, itemSpacing: 12 }); s.resize(390, 844); s.primaryAxisSizingMode = "FIXED"; s.counterAxisSizingMode = "FIXED";
  s.paddingTop = 44; s.paddingLeft = s.paddingRight = 18; s.paddingBottom = 100; s.clipsContent = true; fillV(s, "bg"); s.cornerRadius = 36; s.x = x; s.y = ROW_Y; return s;
};
const addNav = (s, active) => { const n = variant(navSet, active).createInstance(); s.appendChild(n); n.layoutPositioning = "ABSOLUTE"; n.x = 0; n.y = 844 - 86; return n; };
const titleRow = (s, title, sub) => {
  const r = AL("HORIZONTAL", { name: "Title" }); add(s, r, true); r.primaryAxisAlignItems = "SPACE_BETWEEN"; r.counterAxisAlignItems = "CENTER"; r.paddingLeft = r.paddingRight = 4;
  r.appendChild(txt(title, "Mitr", "Medium", 26, "text"));
  const c = AL("HORIZONTAL", { name: "Summary" }); c.paddingLeft = c.paddingRight = 12; c.paddingTop = c.paddingBottom = 6; c.cornerRadius = 999; fillV(c, "card"); strokeV(c, "line", 2.5);
  c.appendChild(txt(sub, "Mali", "SemiBold", 13, "text")); r.appendChild(c); return r;
};
const card = (parent, name, spacing = 10) => {
  const c = AL("VERTICAL", { name, itemSpacing: spacing }); add(parent, c, true); c.paddingLeft = c.paddingRight = 14; c.paddingTop = c.paddingBottom = 14;
  c.cornerRadius = 22; fillV(c, "card"); strokeV(c, "line", 3); shadowV(c, "line", 4); return c;
};
const sqBtn = (inner, name) => {
  const b = AL("HORIZONTAL", { name }); b.primaryAxisAlignItems = "CENTER"; b.counterAxisAlignItems = "CENTER"; b.resize(44, 44); b.primaryAxisSizingMode = "FIXED"; b.counterAxisSizingMode = "FIXED";
  b.cornerRadius = 14; fillV(b, "card"); strokeV(b, "line", 3); shadowV(b, "line", 3); b.appendChild(icon(inner, 2.6, 22)); return b;
};
const segmented = (parent, opts, sel) => {
  const w = AL("HORIZONTAL", { name: "Segmented", itemSpacing: 8 }); add(parent, w, true); w.paddingLeft = w.paddingRight = w.paddingTop = w.paddingBottom = 4; w.cornerRadius = 18; fillV(w, "crate");
  return opts.map((label, i) => {
    const on = i === sel; const o = AL("HORIZONTAL", { name: on ? "Option (selected)" : "Option" }); o.primaryAxisAlignItems = "CENTER"; o.counterAxisAlignItems = "CENTER";
    w.appendChild(o); o.counterAxisSizingMode = "FIXED"; o.resize(100, 44); o.layoutSizingHorizontal = "FILL"; o.cornerRadius = 14;
    if (on) { fillV(o, "accent"); strokeV(o, "edge", 2.5); shadowV(o, "edge", 3); }
    o.appendChild(txt(label, "Mali", "Bold", 15, on ? "ink" : "text")); return o;
  });
};

// ================= 13–14 ปฏิทิน =================
const MOODS = { 1: "happy", 2: "calm", 3: "tired", 5: "meh", 6: "happy", 7: "grumpy", 8: "calm", 9: "calm", 10: "meh", 11: "sad", 12: "calm", 13: "tired", 15: "happy" };
const TODAY = 15;
const buildCalendar = (name, x, selected) => {
  const s = screen(name, x);
  titleRow(s, "ปฏิทิน", "บันทึกแล้ว 13 วัน");
  const mr = AL("HORIZONTAL", { name: "Month Switcher" }); add(s, mr, true); mr.primaryAxisAlignItems = "SPACE_BETWEEN"; mr.counterAxisAlignItems = "CENTER";
  mr.appendChild(sqBtn('<path d="M15 5l-7 7 7 7"/>', "Prev Month")); mr.appendChild(txt("พฤศจิกายน 2569", "Mitr", "Medium", 19, "text")); mr.appendChild(sqBtn('<path d="M9 5l7 7-7 7"/>', "Next Month"));
  const cal = card(s, "Month Grid", 2); cal.paddingLeft = cal.paddingRight = 8; cal.paddingTop = 10; cal.paddingBottom = 8;
  const head = AL("HORIZONTAL", { name: "Weekdays" }); add(cal, head, true); head.paddingBottom = 4;
  ["จ", "อ", "พ", "พฤ", "ศ", "ส", "อา"].forEach(d => { const t = txt(d, "Mali", "Bold", 12, "text-sub"); t.textAlignHorizontal = "CENTER"; add(head, t, true); });
  let day = 1; const cells = {};
  for (let w = 0; w < 6; w++) {
    const row = AL("HORIZONTAL", { name: "Week " + (w + 1), itemSpacing: 2 }); add(cal, row, true);
    for (let i = 0; i < 7; i++) {
      const idx = w * 7 + i; const c = AL("VERTICAL", { name: "Blank" }); c.counterAxisAlignItems = "CENTER"; c.primaryAxisAlignItems = "CENTER";
      row.appendChild(c); c.primaryAxisSizingMode = "FIXED"; c.resize(40, 48); c.layoutSizingHorizontal = "FILL"; c.cornerRadius = 12;
      if (idx < 6 || day > 30) continue; // 1 พ.ย. 2569 ตรงกับวันอาทิตย์
      const d = day++; c.name = "Day " + d; cells[d] = c;
      if (d === selected) { fillV(c, "accent"); strokeV(c, "edge", 2.5); }
      else if (d === TODAY) { strokeV(c, "line", 2); c.dashPattern = [4, 3]; }
      c.appendChild(txt(String(d), "Mali", (d === TODAY || d === selected) ? "Bold" : "SemiBold", 11, d === selected ? "ink" : (d > TODAY ? "dash" : "text")));
      if (MOODS[d]) c.appendChild(face(MOODS[d], 28));
      else if (d <= TODAY) {
        const e = figma.createEllipse(); e.name = "No entry"; e.resize(22, 22); e.fills = []; strokeV(e, "dash", 2); e.dashPattern = [4, 3];
        const slot = AL("HORIZONTAL", { name: "Slot" }); slot.paddingLeft = slot.paddingRight = slot.paddingTop = slot.paddingBottom = 3; slot.appendChild(e); c.appendChild(slot);
      }
    }
  }
  addNav(s, "Calendar");
  return { s, cells };
};
const cal1 = buildCalendar("13 · ปฏิทิน · เดือนนี้", 0, 15);
cal1.s.appendChild(txt("อาทิตย์ 15 พฤศจิกายน", "Mitr", "Medium", 16, "text"));
const calCard = Card.createInstance(); add(cal1.s, calCard, true);

const cal2 = buildCalendar("14 · ปฏิทิน · วันที่ยังไม่บันทึก", 470, 14);
cal2.s.appendChild(txt("เสาร์ 14 พฤศจิกายน", "Mitr", "Medium", 16, "text"));
const noEntry = AL("HORIZONTAL", { name: "No Entry Card", itemSpacing: 12 }); add(cal2.s, noEntry, true); noEntry.counterAxisAlignItems = "CENTER";
noEntry.paddingLeft = noEntry.paddingRight = 14; noEntry.paddingTop = noEntry.paddingBottom = 10; noEntry.cornerRadius = 22; fillV(noEntry, "card"); strokeV(noEntry, "line", 3); shadowV(noEntry, "line", 4);
noEntry.appendChild(face("sleepy", 52));
const neText = AL("VERTICAL", { name: "Text", itemSpacing: 2 }); add(noEntry, neText, true);
neText.appendChild(txt("ยังไม่มีบันทึกวันนี้", "Mali", "Bold", 15, "text")); neText.appendChild(txt("เขียนย้อนหลังได้นะ", "Mali", "Regular", 13, "text-sub"));
const writeBack = button("Paper", "เขียน"); noEntry.appendChild(writeBack);

// ================= 15–16 สถิติ =================
const STATS = {
  week: { sub: "บันทึก 6 วัน", top: "calm", topText: "2 จาก 6 วัน", data: { happy: 1, calm: 2, meh: 1, sad: 1, grumpy: 0, tired: 1 }, streak: "ต่อเนื่อง 5 วัน", best: "สถิติสูงสุด 9 วัน" },
  month: { sub: "บันทึก 13 วัน", top: "calm", topText: "4 จาก 13 วัน", data: { happy: 3, calm: 4, meh: 2, sad: 1, grumpy: 1, tired: 2 }, streak: "บันทึก 13 จาก 15 วัน", best: "เหลืออีก 15 วันในเดือนนี้" }
};
const buildStats = (name, x, mode) => {
  const st = STATS[mode]; const s = screen(name, x);
  titleRow(s, "สถิติ", st.sub);
  const seg = segmented(s, ["สัปดาห์นี้", "เดือนนี้"], mode === "week" ? 0 : 1);
  // อารมณ์ที่พบบ่อย
  const topC = AL("HORIZONTAL", { name: "Top Mood", itemSpacing: 14 }); add(s, topC, true); topC.counterAxisAlignItems = "CENTER";
  topC.paddingLeft = topC.paddingRight = 16; topC.paddingTop = topC.paddingBottom = 12; topC.cornerRadius = 22; fillV(topC, "accent"); strokeV(topC, "edge", 3); shadowV(topC, "edge", 4);
  topC.appendChild(face(st.top, 68));
  const tt = AL("VERTICAL", { name: "Text", itemSpacing: 2 }); add(topC, tt, true);
  tt.appendChild(txt("อารมณ์ที่พบบ่อยที่สุด", "Mali", "SemiBold", 13, "ink"));
  tt.appendChild(txt(LABEL[st.top], "Mitr", "Medium", 24, "ink"));
  tt.appendChild(txt(st.topText, "Mali", "Regular", 13, "ink"));
  // สัดส่วนอารมณ์
  const dist = card(s, "Mood Breakdown", 8);
  dist.appendChild(txt("สัดส่วนอารมณ์", "Mitr", "Medium", 16, "text"));
  const max = Math.max(...Object.values(st.data));
  for (const m of ["happy", "calm", "meh", "sad", "grumpy", "tired"]) {
    const r = AL("HORIZONTAL", { name: "Bar/" + m, itemSpacing: 10 }); add(dist, r, true); r.counterAxisAlignItems = "CENTER";
    r.appendChild(face(m, 28));
    const lb = txt(LABEL[m], "Mali", "SemiBold", 13, "text"); r.appendChild(lb); lb.textAutoResize = "NONE"; lb.resize(62, 18);
    const track = figma.createFrame(); track.name = "Track"; track.resize(180, 14); track.cornerRadius = 999; fillV(track, "crate"); strokeV(track, "line", 2); track.clipsContent = true;
    const val = st.data[m];
    if (val > 0) {
      const bar = figma.createRectangle(); bar.name = "Value"; bar.resize(Math.max(14, 180 * val / max), 14); bar.cornerRadius = 999;
      bar.fills = [{ type: "SOLID", color: hex(COLOR[m]) }]; track.appendChild(bar); bar.x = 0; bar.y = 0; bar.constraints = { horizontal: "SCALE", vertical: "STRETCH" };
    }
    add(r, track, true);
    const cnt = txt(String(val), "Mali", "Bold", 13, "text"); r.appendChild(cnt); cnt.textAutoResize = "NONE"; cnt.resize(18, 18); cnt.textAlignHorizontal = "RIGHT";
  }
  // ความต่อเนื่อง
  const sc = AL("HORIZONTAL", { name: "Streak", itemSpacing: 12 }); add(s, sc, true); sc.counterAxisAlignItems = "CENTER";
  sc.paddingLeft = sc.paddingRight = 16; sc.paddingTop = sc.paddingBottom = 12; sc.cornerRadius = 22; fillV(sc, "card"); strokeV(sc, "line", 3); shadowV(sc, "line", 4);
  const fire = AL("HORIZONTAL", { name: "Icon" }); fire.primaryAxisAlignItems = "CENTER"; fire.counterAxisAlignItems = "CENTER"; fire.resize(44, 44); fire.primaryAxisSizingMode = "FIXED"; fire.counterAxisSizingMode = "FIXED"; fire.cornerRadius = 999; fillV(fire, "crate"); strokeV(fire, "line", 2);
  const fi = figma.createNodeFromSvg('<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 3c2 4 6 6 6 11a6 6 0 0 1-12 0c0-3 2-4 3-7 1 2 2 3 3 3 0-3-1-5 0-7z" stroke="#E2574C" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>');
  fire.appendChild(fi); sc.appendChild(fire);
  const stt = AL("VERTICAL", { name: "Text" }); add(sc, stt, true);
  stt.appendChild(txt(st.streak, "Mitr", "Medium", 17, "text")); stt.appendChild(txt(st.best, "Mali", "Regular", 13, "text-sub"));
  addNav(s, "Stats");
  return { s, seg };
};
const statsW = buildStats("15 · สถิติ · สัปดาห์นี้", 940, "week");
const statsM = buildStats("16 · สถิติ · เดือนนี้", 1410, "month");

// ================= 17 ตั้งค่า · เลือกเวลาเตือน =================
const remind = Settings.clone(); remind.name = "17 · ตั้งค่า · เลือกเวลาเตือน"; remind.x = 1880; remind.y = ROW_Y;
const scrim = figma.createRectangle(); scrim.name = "Scrim"; scrim.resize(390, 844); scrim.fills = [{ type: "SOLID", color: hex("3C230F"), opacity: 0.45 }];
remind.appendChild(scrim); scrim.layoutPositioning = "ABSOLUTE"; scrim.x = 0; scrim.y = 0;
const sheet = AL("VERTICAL", { name: "Bottom Sheet", itemSpacing: 14 }); sheet.counterAxisAlignItems = "CENTER"; sheet.resize(390, 100); sheet.counterAxisSizingMode = "FIXED";
sheet.paddingLeft = sheet.paddingRight = 22; sheet.paddingTop = 12; sheet.paddingBottom = 30; sheet.topLeftRadius = sheet.topRightRadius = 30; fillV(sheet, "bg");
sheet.strokes = [bp("edge")]; sheet.strokeAlign = "INSIDE"; sheet.strokeTopWeight = 3; sheet.strokeBottomWeight = 0; sheet.strokeLeftWeight = 0; sheet.strokeRightWeight = 0;
const handle = figma.createRectangle(); handle.name = "Handle"; handle.resize(44, 5); handle.cornerRadius = 3; fillV(handle, "dash"); sheet.appendChild(handle);
const sh = txt("เวลาเตือนให้เขียน", "Mitr", "Medium", 20, "text"); sheet.appendChild(sh);
const wheel = AL("VERTICAL", { name: "Time Picker", itemSpacing: 4 }); wheel.counterAxisAlignItems = "CENTER"; add(sheet, wheel, true);
const t1 = txt("19:00", "Mitr", "Medium", 20, "dash"); wheel.appendChild(t1);
const selT = AL("HORIZONTAL", { name: "Selected Time" }); selT.primaryAxisAlignItems = "CENTER"; add(wheel, selT, true); selT.paddingTop = selT.paddingBottom = 6;
selT.cornerRadius = 18; fillV(selT, "accent"); strokeV(selT, "edge", 3); shadowV(selT, "edge", 4); selT.appendChild(txt("20:00", "Mitr", "Medium", 34, "ink"));
wheel.appendChild(txt("21:00", "Mitr", "Medium", 20, "dash"));
const dl = txt("วันที่เตือน", "Mali", "Bold", 13, "text-sub"); add(sheet, dl, true);
const days = AL("HORIZONTAL", { name: "Days", itemSpacing: 6 }); add(sheet, days, true);
["จ", "อ", "พ", "พฤ", "ศ", "ส", "อา"].forEach(d => {
  const c = AL("HORIZONTAL", { name: "Day/" + d }); c.primaryAxisAlignItems = "CENTER"; c.counterAxisAlignItems = "CENTER"; days.appendChild(c);
  c.counterAxisSizingMode = "FIXED"; c.resize(36, 40); c.layoutSizingHorizontal = "FILL"; c.cornerRadius = 999; fillV(c, "accent"); strokeV(c, "edge", 2); c.appendChild(txt(d, "Mali", "Bold", 13, "ink"));
});
const notif = AL("HORIZONTAL", { name: "Notification Preview", itemSpacing: 10 }); add(sheet, notif, true); notif.counterAxisAlignItems = "CENTER";
notif.paddingLeft = notif.paddingRight = 12; notif.paddingTop = notif.paddingBottom = 10; notif.cornerRadius = 18; fillV(notif, "card"); strokeV(notif, "line", 2.5);
notif.appendChild(face("happy", 36));
const nt = AL("VERTICAL", { name: "Text" }); add(notif, nt, true);
nt.appendChild(txt("สมุดอารมณ์ · 20:00", "Mali", "Bold", 12, "text-sub"));
const nb = txt("วันนี้รู้สึกยังไงบ้าง? มาบันทึกกันเถอะ", "Mali", "Regular", 14, "text"); add(nt, nb, true); nb.textAutoResize = "HEIGHT";
const sb = AL("HORIZONTAL", { name: "Buttons", itemSpacing: 12 }); add(sheet, sb, true);
const remCancel = button("Paper", "ยกเลิก"); add(sb, remCancel, true);
const remSave = button("Primary", "บันทึก"); add(sb, remSave, true);
remind.appendChild(sheet); sheet.layoutPositioning = "ABSOLUTE"; sheet.x = 0; sheet.y = 844 - sheet.height;

// ================= 18 ตั้งค่า · Night + English =================
const setNE = Settings.clone(); setNE.name = "18 · ตั้งค่า · Night + English (3.3 3.4)"; setNE.x = 2350; setNE.y = ROW_Y;
const MAP = {
  "ตั้งค่า": "Settings", "ภาษา": "Language", "ธีม": "Theme", "กลางวัน": "Day", "กลางคืน": "Night",
  "เตือนให้เขียนทุกวัน": "Daily reminder", "ทุกวันเวลา 20:00": "Every day at 20:00",
  "แสดงผลแนวตั้งเท่านั้น": "Portrait only", "ล็อกการหมุนจอ เพื่อให้หน้าจอไม่ล้นหรือซ้อนกัน": "Rotation is locked so nothing overflows",
  "ลบบันทึกทั้งหมด": "Delete all entries", "สมุดอารมณ์ เวอร์ชัน 1.0": "Mood Diary version 1.0"
};
let guard = 0, inst;
while ((inst = setNE.findOne(n => n.type === "INSTANCE")) && guard++ < 100) inst.detachInstance();
const remap = paints => paints.map(p => { const id = p.boundVariables && p.boundVariables.color && p.boundVariables.color.id; return (id && DAYNAME[id] && NIGHT[DAYNAME[id]]) ? figma.variables.setBoundVariableForPaint(p, "color", NIGHT[DAYNAME[id]]) : p; });
for (const n of [setNE, ...setNE.findAll(() => true)]) {
  if ("fills" in n && Array.isArray(n.fills) && n.fills.length) n.fills = remap(n.fills);
  if ("strokes" in n && Array.isArray(n.strokes) && n.strokes.length) n.strokes = remap(n.strokes);
  if ("effects" in n && n.effects.length) n.effects = n.effects.map(e => { const id = e.boundVariables && e.boundVariables.color && e.boundVariables.color.id; return (id && DAYNAME[id]) ? figma.variables.setBoundVariableForEffect(e, "color", NIGHT[DAYNAME[id]]) : e; });
  if (n.type === "TEXT" && MAP[n.characters] !== undefined) { await figma.loadFontAsync(n.fontName); n.characters = MAP[n.characters]; }
}
// สลับปุ่มที่เลือก: English + Night
const optByText = (root, t) => root.findOne(n => n.type === "FRAME" && n.name.startsWith("Option") && n.findOne(x => x.type === "TEXT" && x.characters === t));
const swapSel = (onNode, offNode) => {
  const onFills = onNode.fills, onStrokes = onNode.strokes, onEffects = onNode.effects;
  onNode.fills = []; onNode.strokes = []; onNode.effects = []; onNode.name = "Option";
  offNode.fills = onFills; offNode.strokes = onStrokes; offNode.effects = onEffects; offNode.strokeWeight = 2.5; offNode.name = "Option (selected)";
  const tOn = onNode.findOne(x => x.type === "TEXT"), tOff = offNode.findOne(x => x.type === "TEXT"); const a = tOn.fills; tOn.fills = tOff.fills; tOff.fills = a;
  const vOn = onNode.findAll(x => x.type === "VECTOR"), vOff = offNode.findAll(x => x.type === "VECTOR");
  if (vOn.length && vOff.length) { const s = vOn[0].strokes; vOn.forEach(v => v.strokes = vOff[0].strokes); vOff.forEach(v => v.strokes = s); }
};
swapSel(optByText(setNE, "ไทย"), optByText(setNE, "English"));
swapSel(optByText(setNE, "Day"), optByText(setNE, "Night"));

// ================= Prototype =================
const T = (type) => ({ type, easing: { type: "EASE_OUT" }, duration: 0.3 });
const go = async (node, dest, anim = "SMART_ANIMATE") => {
  if (!node || !dest) return;
  await node.setReactionsAsync([{ trigger: { type: "ON_CLICK" }, actions: [{ type: "NODE", destinationId: dest.id, navigation: "NAVIGATE", transition: T(anim), preserveScrollPosition: false, resetVideoPosition: false }] }]);
};
const tab = (root, label) => root.findOne(n => n.name === "Tab/" + label);
const addBtn = root => root.findOne(n => n.name === "Add Button");
// แท็บล่างของหน้าหลักทุกแบบ → ปฏิทิน / สถิติ
for (const r of [Home, Exit, Draft, Empty, Small, Tablet, NightHome].filter(Boolean)) {
  await go(tab(r, "ปฏิทิน"), cal1.s, "DISSOLVE"); await go(tab(r, "สถิติ"), statsW.s, "DISSOLVE");
}
// แท็บล่างของหน้าใหม่
for (const r of [cal1.s, cal2.s, statsW.s, statsM.s]) {
  await go(tab(r, "หน้าหลัก"), Home, "DISSOLVE"); await go(tab(r, "ปฏิทิน"), cal1.s, "DISSOLVE");
  await go(tab(r, "สถิติ"), statsW.s, "DISSOLVE"); await go(tab(r, "ตั้งค่า"), Settings, "DISSOLVE"); await go(addBtn(r), Pick);
}
await go(cal1.cells[14], cal2.s, "DISSOLVE"); await go(cal2.cells[15], cal1.s, "DISSOLVE");
await go(calCard, Detail); await go(writeBack, Pick);
await go(statsW.seg[1], statsM.s, "DISSOLVE"); await go(statsM.seg[0], statsW.s, "DISSOLVE");
// ตั้งค่า
await go(Settings.findOne(n => n.name === "เตือนให้เขียนทุกวัน"), remind, "DISSOLVE");
await go(remCancel, Settings, "DISSOLVE"); await go(remSave, Settings, "DISSOLVE");
await go(optByText(Settings, "English"), setNE, "DISSOLVE"); await go(optByText(Settings, "กลางคืน"), setNE, "DISSOLVE");
await go(optByText(setNE, "ไทย"), Settings, "DISSOLVE"); await go(optByText(setNE, "Day"), Settings, "DISSOLVE");
await go(setNE.findOne(n => n.name === "Back Button"), NightHome, "DISSOLVE");

figma.viewport.scrollAndZoomIntoView([cal1.s, cal2.s, statsW.s, statsM.s, remind, setNE]);
const msg = "เพิ่มหน้า ปฏิทิน 2 · สถิติ 2 · ตั้งค่า 2 และเชื่อม Prototype แล้ว";
try { figma.notify(msg); } catch (e) {}
msg;
