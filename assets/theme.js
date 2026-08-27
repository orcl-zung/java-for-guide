/* =========================================================================
   theme.js — dark/light theme with no flash-of-wrong-theme.
   - On load: use saved choice, else follow the OS preference.
   - Injects a fixed round toggle (top-right) into every page that includes it.
   - Listens to OS changes only when the user hasn't chosen manually.
   Load this in <head> (no defer) so data-theme is set before first paint.
   ========================================================================= */
(function () {
  "use strict";
  var KEY = "jog-theme";
  var root = document.documentElement;

  function systemPrefersDark() {
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  }
  function saved() { try { return localStorage.getItem(KEY); } catch (e) { return null; } }
  function apply(t) { root.setAttribute("data-theme", t); }
  function current() { return root.getAttribute("data-theme") || (systemPrefersDark() ? "dark" : "light"); }

  apply(saved() || (systemPrefersDark() ? "dark" : "light"));

  if (window.matchMedia) {
    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", function (e) {
      if (!saved()) apply(e.matches ? "dark" : "light");
      syncIcon();
    });
  }

  function syncIcon(btn) {
    var b = btn || document.querySelector(".theme-toggle");
    if (!b) return;
    b.textContent = current() === "dark" ? "☀" : "☾";
    b.setAttribute("title", current() === "dark" ? "切换亮色" : "切换暗色");
  }

  window.JOG = window.JOG || {};
  window.JOG.toggleTheme = function () {
    var next = current() === "dark" ? "light" : "dark";
    apply(next);
    try { localStorage.setItem(KEY, next); } catch (e) {}
    syncIcon();
  };

  function ready(fn) {
    if (document.readyState !== "loading") fn();
    else document.addEventListener("DOMContentLoaded", fn);
  }
  ready(function () {
    var b = document.createElement("button");
    b.type = "button";
    b.className = "theme-toggle";
    b.setAttribute("aria-label", "Toggle dark mode");
    b.addEventListener("click", function () { window.JOG.toggleTheme(); });
    document.body.appendChild(b);
    syncIcon(b);
  });
})();
