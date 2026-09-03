/* =========================================================================
   search.js — ⌘K / Ctrl+K 全局搜索面板（精简命令面板）
   - 首次打开时抓取全站页面，解析 h2/h3/summary/.q-line/.card .t 建索引
   - 多关键词 AND 匹配；↑↓ 选择，⏎ 跳转，esc 关闭；右下角🔍按钮可点
   - 跳转带 ?s= 片段，目标页加载后自动展开 details、滚动定位并高亮
   ========================================================================= */
(function () {
  "use strict";

  var styleLink = document.querySelector('link[href*="style.css"]');
  var prefix = styleLink ? styleLink.getAttribute("href").replace(/assets\/style\.css.*$/, "") : "";
  var SEL = "h2, h3, details > summary, .q-line, .card .t";

  function norm(s) { return (s || "").replace(/\s+/g, " ").trim(); }
  function esc(s) {
    return s.replace(/[&<>"]/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
    });
  }
  function curFile() { return (location.pathname.split("/").pop() || "index.html").toLowerCase(); }

  /* ---------- 索引（优先读预生成静态索引；无则 fetch 现建，file:// 下会失败） ---------- */
  var idx = null, building = false;
  function pageList() {
    var list = [{ t: "课程主页", h: "index.html" }];
    (window.JOG_NAV || []).forEach(function (sec) {
      sec.items.forEach(function (it) { list.push({ t: it.t, h: it.h }); });
    });
    return list;
  }
  function buildIndex(done) {
    if (idx) return done();
    if (window.JOG_INDEX && window.JOG_INDEX.length) { idx = window.JOG_INDEX; return done(); }
    if (building) {
      var t = setInterval(function () { if (idx) { clearInterval(t); done(); } }, 120);
      return;
    }
    building = true;
    var pages = pageList(), entries = [], left = pages.length;
    pages.forEach(function (pg) {
      fetch(prefix + pg.h, { credentials: "same-origin" })
        .then(function (r) { if (!r.ok) throw new Error(r.status); return r.text(); })
        .then(function (html) {
          var doc = new DOMParser().parseFromString(html, "text/html");
          doc.querySelectorAll(SEL).forEach(function (el) {
            var tx = norm(el.textContent);
            if (tx && tx.length > 1) entries.push({ p: pg.h, pt: pg.t, t: tx });
          });
        })
        .catch(function () {})
        .finally(function () { if (--left === 0) { idx = entries; done(); } });
    });
  }

  function find(q) {
    var terms = norm(q).toLowerCase().split(" ").filter(Boolean);
    if (!terms.length || !idx) return [];
    var hit = [];
    for (var i = 0; i < idx.length; i++) {
      var hay = idx[i].t.toLowerCase(), ok = true;
      for (var j = 0; j < terms.length; j++) {
        if (hay.indexOf(terms[j]) < 0) { ok = false; break; }
      }
      if (ok) hit.push(idx[i]);
    }
    hit.sort(function (a, b) {
      return a.t.toLowerCase().indexOf(terms[0]) - b.t.toLowerCase().indexOf(terms[0]);
    });
    return hit.slice(0, 14);
  }

  /* ---------- 面板 ---------- */
  var overlay, input, list, active = 0, results = [];

  function build() {
    overlay = document.createElement("div");
    overlay.className = "cmdk-overlay";
    overlay.innerHTML =
      '<div class="cmdk" role="dialog" aria-label="全局搜索">' +
        '<div class="cmdk-bar"><span class="cmdk-ico">🔍</span>' +
        '<input type="text" placeholder="搜索课程…（如：最左前缀 / ReadView / 死锁）" aria-label="搜索课程">' +
        "<kbd>esc</kbd></div>" +
        '<div class="cmdk-list"></div>' +
        '<div class="cmdk-foot"><span>↑↓ 选择</span><span>⏎ 打开</span><span>esc 关闭</span></div>' +
      "</div>";
    document.body.appendChild(overlay);
    input = overlay.querySelector("input");
    list = overlay.querySelector(".cmdk-list");
    overlay.addEventListener("click", function (e) { if (e.target === overlay) close(); });
    input.addEventListener("input", function () { active = 0; render(); });
    input.addEventListener("keydown", function (e) {
      if (e.key === "ArrowDown") { e.preventDefault(); move(1); }
      else if (e.key === "ArrowUp") { e.preventDefault(); move(-1); }
      else if (e.key === "Enter") { e.preventDefault(); if (results[active]) go(results[active]); }
      else if (e.key === "Escape") { e.preventDefault(); close(); }
    });
    list.addEventListener("click", function (e) {
      var it = e.target.closest(".cmdk-item");
      if (it) go(results[+it.getAttribute("data-i")]);
    });
    list.addEventListener("mousemove", function (e) {
      var it = e.target.closest(".cmdk-item");
      if (it && +it.getAttribute("data-i") !== active) { active = +it.getAttribute("data-i"); paint(); }
    });
  }

  function render() {
    if (!overlay) return;
    if (!idx) { list.innerHTML = '<div class="cmdk-empty">索引构建中…</div>'; return; }
    if (!idx.length) { list.innerHTML = '<div class="cmdk-empty">索引为空——请通过本地服务器或 GitHub Pages 访问本站点</div>'; return; }
    var q = input.value;
    if (!norm(q)) {
      list.innerHTML = '<div class="cmdk-empty">输入关键词，搜索全部课程与手册</div>';
      results = [];
      return;
    }
    results = find(q);
    if (!results.length) { list.innerHTML = '<div class="cmdk-empty">没有命中「' + esc(q) + "」</div>"; return; }
    var terms = norm(q).toLowerCase().split(" ").filter(Boolean);
    list.innerHTML = results.map(function (r, i) {
      var tx = esc(r.t);
      terms.forEach(function (t) {
        var re = new RegExp("(" + t.replace(/[.*+?^${}()|[\]\\]/g, "\\$&") + ")", "ig");
        tx = tx.replace(re, "<mark>$1</mark>");
      });
      return '<div class="cmdk-item' + (i === active ? " active" : "") + '" data-i="' + i + '">' +
        '<span class="tx">' + tx + '</span><span class="pg">' + esc(r.pt) + "</span></div>";
    }).join("");
  }
  function paint() {
    list.querySelectorAll(".cmdk-item").forEach(function (el, i) {
      el.classList.toggle("active", i === active);
    });
  }
  function move(d) {
    if (!results.length) return;
    active = (active + d + results.length) % results.length;
    paint();
    var el = list.querySelector(".cmdk-item.active");
    if (el) el.scrollIntoView({ block: "nearest" });
  }

  function open() {
    if (!overlay) build();
    overlay.classList.add("show");
    active = 0;
    input.value = "";
    render();
    setTimeout(function () { input.focus(); }, 0);
    buildIndex(render);
  }
  function close() { if (overlay) overlay.classList.remove("show"); }

  /* ---------- 跳转 ---------- */
  function go(r) {
    close();
    var key = r.t.slice(0, 24);
    if (r.p.split("/").pop().toLowerCase() === curFile()) jumpTo(key);
    else location.href = prefix + r.p + "?s=" + encodeURIComponent(key);
  }
  function jumpTo(key) {
    var els = document.querySelectorAll(SEL);
    for (var i = 0; i < els.length; i++) {
      if (norm(els[i].textContent).indexOf(key) >= 0) {
        var d = els[i].closest("details");
        while (d) { d.open = true; d = d.parentElement ? d.parentElement.closest("details") : null; }
        els[i].scrollIntoView({ behavior: "smooth", block: "start" });
        els[i].classList.add("flash-hit");
        (function (el) { setTimeout(function () { el.classList.remove("flash-hit"); }, 2600); })(els[i]);
        return true;
      }
    }
    return false;
  }

  /* ---------- 入口：⌘K / Ctrl+K + 浮动按钮 ---------- */
  document.addEventListener("keydown", function (e) {
    if ((e.metaKey || e.ctrlKey) && !e.shiftKey && e.key.toLowerCase() === "k") {
      e.preventDefault();
      if (overlay && overlay.classList.contains("show")) close(); else open();
    }
  });

  var fab = document.createElement("button");
  fab.type = "button";
  fab.className = "search-fab";
  fab.setAttribute("aria-label", "搜索课程（⌘K）");
  fab.innerHTML = "🔍<kbd>⌘K</kbd>";
  fab.addEventListener("click", open);
  document.body.appendChild(fab);

  /* ---------- 落地：处理 ?s= 片段 ---------- */
  function land() {
    var m = location.search.match(/[?&]s=([^&]+)/);
    if (!m) return;
    var key = decodeURIComponent(m[1]);
    setTimeout(function () {
      jumpTo(key);
      try { history.replaceState(null, "", location.pathname); } catch (e) {}
    }, 60);
  }
  if (document.readyState !== "loading") land();
  else document.addEventListener("DOMContentLoaded", land);
})();
