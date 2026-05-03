#!/usr/bin/env python3
"""
scrape-miniprogram-api.py — Fetch WeChat mini program API docs.

Many mini program APIs (e.g. wx.setStorageSync, wx.getStorageSync) also work
in mini games. This script discovers all mini program API URLs from the sidebar,
fetches each page in parallel, converts to clean markdown, and saves to disk.

Usage:
    python3 tools/scrape-miniprogram-api.py                        # fetch all APIs
    python3 tools/scrape-miniprogram-api.py --dry-run              # just list URLs
    python3 tools/scrape-miniprogram-api.py --category storage     # only "storage" category
    python3 tools/scrape-miniprogram-api.py --concurrency 10       # parallel downloads
    python3 tools/scrape-miniprogram-api.py --output ./my-dir      # custom output dir
    python3 tools/scrape-miniprogram-api.py --retry 3              # retries on failure

Output: references/miniprogram-api-docs/<category>/<...>/<name>.md
"""

import argparse
import html as html_mod
import os
import re
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from urllib.parse import quote, urlparse
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError

BASE_URL = "https://developers.weixin.qq.com"
# Mini program API index — any mini program API page has the full sidebar
INDEX_URL = f"{BASE_URL}/miniprogram/dev/api/storage/wx.setStorageSync.html"

# Path prefix that identifies mini program API links in the sidebar
API_PATH_PREFIX = "/miniprogram/dev/api/"


def fetch_text(url: str, timeout: int = 20) -> str:
    """Fetch URL and return decoded text."""
    parsed = urlparse(url)
    encoded_path = quote(parsed.path, safe='/:@!$&\'()*+,;=')
    url = parsed._replace(path=encoded_path).geturl()
    req = Request(url, headers={"User-Agent": "Mozilla/5.0 (compatible; wx-api-scraper/1.0)"})
    with urlopen(req, timeout=timeout) as resp:
        charset = resp.headers.get_content_charset() or "utf-8"
        return resp.read().decode(charset, errors="replace")


def extract_api_urls(index_html: str) -> list[str]:
    """Extract all API doc URLs from the sidebar of any mini program API page."""
    pattern = rf'href="({re.escape(API_PATH_PREFIX)}[^"]+\.html)"'
    urls = re.findall(pattern, index_html)
    # Deduplicate while preserving order
    seen = set()
    result = []
    for u in urls:
        if u not in seen:
            seen.add(u)
            result.append(u)
    return result


def html_to_markdown(html_content: str, source_url: str) -> str:
    """Convert the content div HTML to clean markdown."""
    # Find the main content div
    start_marker = 'class="content custom"'
    start = html_content.find(start_marker)
    if start == -1:
        return f"# Error\n\nSource: {source_url}\n\nNo content div found on page."

    # Find the content end — look for page-nav, footer, or sidebar
    content_start = html_content.find(">", start) + 1
    content_end = len(html_content)
    for marker in ['<div class="page-nav', '<footer', '<div class="sidebar']:
        pos = html_content.find(marker, start)
        if pos != -1 and pos < content_end:
            content_end = pos

    text = html_content[content_start:content_end]

    # ── Preserve code blocks first ──
    code_blocks = []

    def save_pre(m):
        code_blocks.append(m.group(0))
        return f"\x00CODEBLOCK{len(code_blocks) - 1}\x00"

    text = re.sub(r"<pre[^>]*>.*?</pre>", save_pre, text, flags=re.DOTALL)
    text = re.sub(r"<code[^>]*>(.*?)</code>", r"`\1`", text, flags=re.DOTALL)

    # ── Tables ──
    def table_to_md(table_html):
        rows = re.findall(r"<tr[^>]*>(.*?)</tr>", table_html, re.DOTALL)
        if not rows:
            return table_html
        md_rows = []
        for i, row in enumerate(rows):
            cells = re.findall(r"<t[hd][^>]*>(.*?)</t[hd]>", row, re.DOTALL)
            cells = [re.sub(r"<[^>]+>", "", c).strip() for c in cells]
            md_rows.append("| " + " | ".join(cells) + " |")
            if i == 0:
                md_rows.append("| " + " | ".join(["---"] * len(cells)) + " |")
        return "\n".join(md_rows)

    text = re.sub(
        r"<table[^>]*>(.*?)</table>",
        lambda m: table_to_md(m.group(1)),
        text,
        flags=re.DOTALL,
    )

    # ── Headers ──
    for level, tag in [(1, "h1"), (2, "h2"), (3, "h3"), (4, "h4"), (5, "h5")]:
        prefix = "#" * level
        text = re.sub(
            rf"<{tag}[^>]*>(.*?)</{tag}>",
            lambda m, p=prefix: f"\n{p} {re.sub(r'<[^>]+>', '', m.group(1)).strip()}\n",
            text,
            flags=re.DOTALL,
        )
    # Fix anchor-only headers ("# " → "## " since it's inside content div)
    text = re.sub(r'^# # ', '## ', text, flags=re.MULTILINE)

    # ── Lists ──
    text = re.sub(r"<li[^>]*>", "- ", text)
    text = re.sub(r"</li>", "\n", text)
    text = re.sub(r"</?[ou]l[^>]*>", "\n", text)

    # ── Inline formatting ──
    text = re.sub(r"<br\s*/?>", "\n", text)
    text = re.sub(r"</?p[^>]*>", "\n", text)
    text = re.sub(r"<strong[^>]*>(.*?)</strong>", r"**\1**", text, flags=re.DOTALL)
    text = re.sub(r"<b[^>]*>(.*?)</b>", r"**\1**", text, flags=re.DOTALL)
    text = re.sub(r"<em[^>]*>(.*?)</em>", r"*\1*", text, flags=re.DOTALL)

    # ── Links ──
    text = re.sub(
        r'<a[^>]*href="([^"]*)"[^>]*>(.*?)</a>', r"[\2](\1)", text, flags=re.DOTALL
    )

    # ── Remove remaining tags ──
    text = re.sub(r"<[^>]+>", "", text)

    # ── Restore code blocks ──
    for i, block in enumerate(code_blocks):
        code_content = re.sub(r"<[^>]+>", "", block).strip()
        # Try to detect language from class
        lang = ""
        lang_match = re.search(r'class="[^"]*language-(\w+)', block)
        if lang_match:
            lang = lang_match.group(1)
        text = text.replace(
            f"\x00CODEBLOCK{i}\x00", f"\n```{lang}\n{code_content}\n```\n"
        )

    # ── Decode HTML entities ──
    text = html_mod.unescape(text)

    # ── Clean up whitespace ──
    text = re.sub(r"\n{3,}", "\n\n", text)
    text = re.sub(r"[ \t]+$", "", text, flags=re.MULTILINE)
    text = text.strip()

    # ── Add source URL header ──
    return f"> Source: {source_url}\n\n{text}"


def fetch_one(rel_path: str, output_dir: str, retries: int = 2) -> tuple[str, str, int]:
    """Fetch a single API doc page and save as markdown.
    Returns (rel_path, status, size).
    status: "ok", "skip", "fail"
    """
    url = f"{BASE_URL}{rel_path}"

    # Compute output path
    api_path = rel_path.removeprefix(API_PATH_PREFIX)
    md_name = api_path.removesuffix(".html") + ".md"
    out_file = os.path.join(output_dir, md_name)

    # Skip if already fetched and non-trivial
    if os.path.exists(out_file) and os.path.getsize(out_file) > 200:
        return (rel_path, "skip", os.path.getsize(out_file))

    # Fetch with retries
    html = None
    for attempt in range(retries + 1):
        try:
            html = fetch_text(url)
            break
        except Exception as e:
            if attempt < retries:
                time.sleep(0.5 * (attempt + 1))
            else:
                os.makedirs(os.path.dirname(out_file), exist_ok=True)
                with open(out_file, "w") as f:
                    f.write(f"# Fetch Error\n\nSource: {url}\n\nError: {e}\n")
                return (rel_path, "fail", 0)

    # Convert to markdown
    markdown = html_to_markdown(html, url)

    # Save
    os.makedirs(os.path.dirname(out_file), exist_ok=True)
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(markdown)

    return (rel_path, "ok", len(markdown.encode("utf-8")))


def main():
    parser = argparse.ArgumentParser(description="Scrape WeChat mini program API docs")
    parser.add_argument("--dry-run", action="store_true", help="List URLs without fetching")
    parser.add_argument("--category", default="", help="Filter to a specific category (e.g. 'storage', 'base/system')")
    parser.add_argument("--concurrency", type=int, default=8, help="Parallel downloads (default: 8)")
    parser.add_argument("--output", default="", help="Output directory (default: references/miniprogram-api-docs)")
    parser.add_argument("--retries", type=int, default=2, help="Retries per page (default: 2)")
    args = parser.parse_args()

    # Resolve paths
    script_dir = Path(__file__).parent.resolve()
    project_dir = script_dir.parent
    output_dir = args.output or str(project_dir / "references" / "miniprogram-api-docs")

    print(f"==> Step 1: Discovering mini program API URLs from sidebar...")
    print(f"    Index: {INDEX_URL}")
    index_html = fetch_text(INDEX_URL)
    urls = extract_api_urls(index_html)
    print(f"    Found {len(urls)} API URLs in sidebar")

    # Filter by category
    if args.category:
        prefix = f"{API_PATH_PREFIX}{args.category.strip('/')}/"
        urls = [u for u in urls if u.startswith(prefix)]
        print(f"    Filtered to {len(urls)} URLs for category '{args.category}'")

    if args.dry_run:
        print(f"\n==> Dry run — {len(urls)} URLs that would be fetched:")
        for u in urls:
            print(f"  {u}")
        print(f"\n    Total: {len(urls)} pages")
        return

    print(f"\n==> Step 2: Fetching {len(urls)} API pages (concurrency={args.concurrency})...")
    print(f"    Output: {output_dir}")
    os.makedirs(output_dir, exist_ok=True)

    ok = 0
    skip = 0
    fail = 0
    t0 = time.time()

    with ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        futures = {pool.submit(fetch_one, url, output_dir, args.retries): url for url in urls}
        for future in as_completed(futures):
            rel_path, status, size = future.result()
            api_name = rel_path.removeprefix(API_PATH_PREFIX)
            if status == "ok":
                ok += 1
                print(f"  OK    {api_name} ({size:,} bytes)")
            elif status == "skip":
                skip += 1
                print(f"  SKIP  {api_name}")
            else:
                fail += 1
                print(f"  FAIL  {api_name}")

    elapsed = time.time() - t0

    # Summary
    total_files = sum(1 for _ in Path(output_dir).rglob("*.md"))
    print(f"\n==> Done in {elapsed:.1f}s!")
    print(f"    Output:    {output_dir}")
    print(f"    Fetched:   {ok}")
    print(f"    Skipped:   {skip} (already existed)")
    print(f"    Failed:    {fail}")
    print(f"    Total md:  {total_files}")


if __name__ == "__main__":
    main()
