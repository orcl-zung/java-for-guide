#!/usr/bin/env python3
"""重建 assets/search-index.js —— ⌘K 全局搜索的静态索引。
用法：python3 scripts/build_search_index.py
（课程内容有增删改后重跑一次，索引与页面文本保持一致）"""
import html.parser
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent

PAGES = [
    ("课程主页", "index.html"),
    ("0001 · 面试作战地图", "lessons/0001-interview-battle-map.html"),
    ("0002 · MySQL 追问链", "lessons/0002-mysql-follow-up-chains.html"),
    ("0003 · 简历改版实战", "lessons/0003-resume-rework.html"),
    ("高频考点速查", "reference/0001-high-frequency-topics.html"),
    ("项目追问应答手册", "reference/0002-project-question-bank.html"),
    ("高信任资源库", "resources.html"),
]

TAG_SEL = {"h2", "h3", "summary"}   # 章节标题 + 追问链问题
CLS_SEL = {"q-line", "t"}           # 手册条目 + 资源卡片标题


class Extractor(html.parser.HTMLParser):
    """提取目标元素的文本（与浏览器 textContent 归一化后保持一致）。"""

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.capturing = None
        self.buf = []
        self.texts = []

    def handle_starttag(self, tag, attrs):
        if self.capturing is None:
            classes = set(dict(attrs).get("class", "").split())
            if tag in TAG_SEL or classes & CLS_SEL:
                self.capturing = tag
                self.buf = []

    def handle_endtag(self, tag):
        if self.capturing == tag:
            text = re.sub(r"\s+", " ", "".join(self.buf)).strip()
            if len(text) > 1:
                self.texts.append(text)
            self.capturing = None
            self.buf = []

    def handle_data(self, data):
        if self.capturing is not None:
            self.buf.append(data)


def main():
    entries = []
    for title, rel in PAGES:
        path = ROOT / rel
        if not path.exists():
            print(f"!! 页面缺失，跳过：{rel}")
            continue
        ex = Extractor()
        ex.feed(path.read_text(encoding="utf-8"))
        for t in ex.texts:
            entries.append({"p": rel, "pt": title, "t": t})
        print(f"{rel}: {len(ex.texts)} 条")

    out = ROOT / "assets" / "search-index.js"
    out.write_text(
        "/* 由 scripts/build_search_index.py 生成，请勿手改；内容更新后重跑该脚本 */\n"
        "window.JOG_INDEX = "
        + json.dumps(entries, ensure_ascii=False, separators=(",", ":"))
        + ";\n",
        encoding="utf-8",
    )
    print(f"→ {out.relative_to(ROOT)}，共 {len(entries)} 条")


if __name__ == "__main__":
    main()
