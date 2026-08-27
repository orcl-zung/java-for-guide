/* =========================================================================
   nav.js — persistent course sidebar with resize + collapse.
   Auto-injected into every page that loads it. Derives the correct relative
   prefix from the page's own style.css link (works at any depth / mount).
   - Drag the right edge to resize (persisted).       [desktop]
   - Toggle button collapses / expands the sidebar.   [desktop]
   - Same button opens / closes it as an overlay.     [mobile]
   ========================================================================= */
(function () {
  "use strict";

  var NAV = [
    { title: "W1 · 清点弹药", items: [
      { n: "0001", t: "面试作战地图", h: "lessons/0001-interview-battle-map.html" },
      { n: "0002", t: "MySQL 追问链", h: "lessons/0002-mysql-follow-up-chains.html" }
    ]},
    { title: "参考手册", items: [
      { n: "", t: "高频考点速查", h: "reference/0001-high-frequency-topics.html" }
    ]}
  ];

  var W_KEY = "jog-nav-w";
  var COL_KEY = "jog-nav-collapsed";
  var DESKTOP = "(min-width: 1024px)";
  var MIN_W = 210, MAX_W = 440;

  function ready(fn) {
    if (document.readyState !== "loading") fn();
    else document.addEventListener("DOMContentLoaded", fn);
  }

  ready(function () {
    var styleLink = document.querySelector('link[href*="style.css"]');
    var prefix = styleLink ? styleLink.getAttribute("href").replace(/assets\/style\.css.*$/, "") : "";
    var cur = (location.pathname.split("/").pop() || "index.html").toLowerCase();

    // restore saved width
    try {
      var w = parseInt(localStorage.getItem(W_KEY), 10);
      if (w >= MIN_W && w <= MAX_W) document.documentElement.style.setProperty("--nav-w", w + "px");
    } catch (e) {}

    // ---- build sidebar ----
    var aside = document.createElement("aside");
    aside.className = "sidenav";
    var html = '<a class="brand" href="' + prefix + 'index.html">⚔️ Java Offer 突围战</a>';
    NAV.forEach(function (sec) {
      html += '<div class="sec">' + sec.title + "</div>";
      sec.items.forEach(function (it) {
        var file = it.h.split("/").pop().toLowerCase();
        var cls = "item" + (file === cur ? " active" : "");
        var num = it.n ? '<span class="num">' + it.n + "</span> " : "";
        html += '<a class="' + cls + '" href="' + prefix + it.h + '">' + num + it.t + "</a>";
      });
    });
    aside.innerHTML = html;
    document.body.appendChild(aside);

    // ---- resize handle (body sibling, so sidebar overflow can't clip it) ----
    var handle = document.createElement("div");
    handle.className = "resize-handle";
    handle.title = "Drag to resize";
    document.body.appendChild(handle);

    // ---- toggle button + backdrop ----
    var btn = document.createElement("button");
    btn.type = "button";
    btn.className = "nav-toggle";
    btn.setAttribute("aria-label", "Toggle course menu");
    var back = document.createElement("div");
    back.className = "nav-backdrop";
    document.body.appendChild(btn);
    document.body.appendChild(back);
    document.body.classList.add("has-nav");

    function isDesktop() { return window.matchMedia(DESKTOP).matches; }

    // restore collapsed (desktop only)
    try {
      if (localStorage.getItem(COL_KEY) === "1" && isDesktop()) document.body.classList.add("nav-collapsed");
    } catch (e) {}

    function syncIcon() {
      if (isDesktop()) {
        btn.textContent = document.body.classList.contains("nav-collapsed") ? "»" : "«";
      } else {
        btn.textContent = aside.classList.contains("open") ? "✕" : "≡";
      }
    }
    syncIcon();

    function closeMobile() { aside.classList.remove("open"); back.classList.remove("show"); }

    btn.addEventListener("click", function () {
      if (isDesktop()) {
        var c = document.body.classList.toggle("nav-collapsed");
        try { localStorage.setItem(COL_KEY, c ? "1" : "0"); } catch (e) {}
      } else {
        var o = aside.classList.toggle("open");
        back.classList.toggle("show", o);
      }
      syncIcon();
    });
    back.addEventListener("click", function () { closeMobile(); syncIcon(); });

    // reset state when crossing the desktop/mobile breakpoint
    window.matchMedia(DESKTOP).addEventListener("change", function () {
      closeMobile();
      if (!isDesktop()) document.body.classList.remove("nav-collapsed");
      syncIcon();
    });

    // ---- drag to resize (desktop) ----
    var dragging = false, startX = 0, startW = 0;
    handle.addEventListener("pointerdown", function (e) {
      if (!isDesktop() || document.body.classList.contains("nav-collapsed")) return;
      dragging = true;
      startX = e.clientX;
      startW = aside.getBoundingClientRect().width;
      document.body.classList.add("resizing");
      try { handle.setPointerCapture(e.pointerId); } catch (err) {}
      e.preventDefault();
    });
    handle.addEventListener("pointermove", function (e) {
      if (!dragging) return;
      var w = Math.min(MAX_W, Math.max(MIN_W, startW + (e.clientX - startX)));
      document.documentElement.style.setProperty("--nav-w", w + "px");
    });
    function endDrag() {
      if (!dragging) return;
      dragging = false;
      document.body.classList.remove("resizing");
      try { localStorage.setItem(W_KEY, Math.round(aside.getBoundingClientRect().width)); } catch (e) {}
    }
    handle.addEventListener("pointerup", endDrag);
    handle.addEventListener("pointercancel", endDrag);
  });
})();
