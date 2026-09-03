/* =========================================================================
   toc.js — 单篇右侧大纲（精简风：细线 + 小字 + 滚动高亮）
   - 从当前页 h2/h3 自动生成，自动补锚点 id
   - 宽屏（≥1280px 且正文右侧空隙足够）才显示，窄屏自动隐藏
   - IntersectionObserver 滚动定位当前节
   ========================================================================= */
(function () {
  "use strict";

  var cur = (location.pathname.split("/").pop() || "index.html").toLowerCase();
  if (cur === "index.html") return; // 主页是门户，不出大纲

  function ready(fn) {
    if (document.readyState !== "loading") fn();
    else document.addEventListener("DOMContentLoaded", fn);
  }

  ready(function () {
    var page = document.querySelector(".page");
    if (!page) return;
    var heads = page.querySelectorAll("h2, h3");
    if (heads.length < 3) return;

    document.body.classList.add("has-toc");

    var toc = document.createElement("nav");
    toc.className = "toc";
    toc.setAttribute("aria-label", "本页大纲");
    var html = '<div class="toc-ttl">本页大纲</div>';
    var secOpen = false;
    heads.forEach(function (h, i) {
      if (!h.id) h.id = "toc-" + i;
      var txt = h.textContent.replace(/\s+/g, " ").trim();
      if (h.tagName === "H3") {
        html += '<a class="l3" href="#' + h.id + '" data-t="' + h.id + '" title="' +
          txt.replace(/"/g, "&quot;") + '"><span class="tx">' + txt + "</span></a>";
      } else {
        if (secOpen) html += "</div>";
        html += '<div class="toc-sec"><a class="l2" href="#' + h.id + '" data-t="' + h.id + '" title="' +
          txt.replace(/"/g, "&quot;") + '"><i class="dash"></i><span class="tx">' + txt + "</span></a>";
        secOpen = true;
      }
    });
    if (secOpen) html += "</div>";
    toc.innerHTML = html;
    document.body.appendChild(toc);

    /* 贴正文右缘定位；空隙不足就隐藏（侧栏被拉宽时自动让位） */
    function place() {
      var r = page.getBoundingClientRect();
      var gap = window.innerWidth - r.right;
      if (window.innerWidth >= 1280 && gap >= 200) {
        toc.style.display = "block";
        toc.style.left = Math.round(r.right + 14) + "px";
      } else {
        toc.style.display = "none";
      }
    }
    place();
    window.addEventListener("resize", place);

    /* 滚动定位当前节：取"最后一个越过阅读线（视口顶部往下 140px）"的标题 */
    var links = {};
    toc.querySelectorAll("a").forEach(function (a) { links[a.getAttribute("data-t")] = a; });
    var ticking = false;
    function spy() {
      var line = 140, current = null;
      for (var i = 0; i < heads.length; i++) {
        if (heads[i].getBoundingClientRect().top <= line) current = heads[i];
        else break;
      }
      if (current === null && heads.length) current = heads[0]; // 页首时亮第一条
      toc.querySelectorAll("a.on").forEach(function (a) { a.classList.remove("on"); });
      if (current && links[current.id]) links[current.id].classList.add("on");
      ticking = false;
    }
    function onScroll() {
      if (!ticking) { ticking = true; requestAnimationFrame(spy); }
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    spy();
  });
})();
