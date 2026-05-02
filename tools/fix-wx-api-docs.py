#!/usr/bin/env python3
"""
fix-wx-api-docs.py — Post-process scraped WeChat minigame API docs.

Fixes 3 systemic issues found by the quality review:
  1. Broken heading anchors:  "## # Title"  →  "## Title"
  2. Translation footer spam: strips the boilerplate "The translations are provided..."
  3. Flattened table rows:   splits long lines where table rows were merged together

Also fixes:
  - Table rows merged with headings on the same line: "|...|## heading" → split
  - Multiple headings on the same line: "## heading ### subheading" → split
  - Leading whitespace on heading lines: " ## title" → "## title"

Usage:
    python3 tools/fix-wx-api-docs.py                          # fix all files in-place
    python3 tools/fix-wx-api-docs.py --dry-run                # preview changes without writing
    python3 tools/fix-wx-api-docs.py --category network       # fix only one category
    python3 tools/fix-wx-api-docs.py --verbose                # show per-file diff stats

Output: modifies files in-place under references/wx-api-docs/
"""

import argparse
import os
import re
import sys
from pathlib import Path


# ═══════════════════════════════════════════════════════════
# Fix functions — each is a pure text→text transform
# ═══════════════════════════════════════════════════════════

def fix_footer(text: str) -> str:
    """Remove the 'The translations are provided by WeChat Translation...' boilerplate."""
    return re.sub(
        r'\n?\s*The translations are provided by WeChat Translation.*?'
        r'\[Tap to report\.\]\(javascript:;\)\s*$',
        '', text, flags=re.DOTALL
    )


def fix_inline_headings(text: str) -> str:
    """Split lines where headings are merged with other content on the same line.

    Handles three cases:
      1. "|...table row...| ## heading"  → split into table row + heading
      2. "|...table row...|## heading"   → split (no space before ##)
      3. "## heading ### subheading"     → split into two headings
      4. " ## heading ### subheading"    → same with leading space
    """
    lines = text.split('\n')
    result = []

    for line in lines:
        # Case 1 & 2: table row followed by heading (|## or | ##)
        # Find the LAST occurrence of | followed by optional spaces then ##
        # We use finditer to find all and use the last one that has actual table content before it
        table_heading_matches = list(re.finditer(r'\|\s*(#{1,5}\s+)', line))
        if table_heading_matches:
            m = table_heading_matches[-1]  # use the last match
            table_part = line[:m.start()].rstrip()
            # heading_part starts after the | and any spaces (strip the pipe)
            heading_part = line[m.end() - len(m.group().lstrip('|').lstrip()):].strip()
            # Simpler: just strip leading | from the heading portion
            raw_heading = line[m.start():].strip()
            heading_part = raw_heading.lstrip('|').strip()
            # Only split if there's real table content (not just a bare |)
            if table_part and table_part != '|':
                # Remove trailing | from table part if present
                table_part = table_part.rstrip('|').rstrip()
                result.append(table_part)
                result.append('')
                result.append(heading_part)
                continue
            elif not table_part:
                # Line starts with |## — strip the leading |
                result.append(heading_part)
                continue

        # Case 3 & 4: multiple headings on one line
        # Find all heading markers (## through #####)
        heading_positions = list(re.finditer(r'#{2,5}\s+\S', line))
        if len(heading_positions) > 1:
            # Split at each heading marker after the first
            parts = []
            prev_start = 0
            for i, hm in enumerate(heading_positions):
                if i == 0:
                    continue
                # Find the actual split point — look backwards for whitespace boundary
                pos = hm.start()
                # The text before this heading belongs to the previous heading
                parts.append(line[prev_start:pos].strip())
                prev_start = pos
            parts.append(line[prev_start:].strip())

            for i, part in enumerate(parts):
                if part:
                    result.append(part)
                    if i < len(parts) - 1:
                        result.append('')
            continue

        result.append(line)

    return '\n'.join(result)


def fix_headings(text: str) -> str:
    """Fix broken heading anchors: '## # Title' → '## Title'.

    The scraper extracted HTML anchor links inside h-tags as literal '# ' text.
    Also handles leading whitespace before headings.
    Runs multiple passes to handle chained fixes.
    """
    HEADING_RE = re.compile(r'^\s*(#{1,5})\s+#\s+(.+)', re.MULTILINE)
    for _ in range(5):
        text = HEADING_RE.sub(r'\1 \2', text)
    return text


def fix_leading_space_headings(text: str) -> str:
    """Remove leading whitespace from heading lines: ' ## title' → '## title'."""
    return re.sub(r'^\s+(#{1,5}\s)', r'\1', text, flags=re.MULTILINE)


# ── Fix 3: Flattened table rows ────────────────────────────

# Pattern matching a new field definition in a flattened table line.
# Catches things like:
#   "  dataType string json 否 返回的数据格式"
#   "  timeout number  否 超时时间"
#   "  success function  否 回调函数"
PARAM_CONTINUATION_RE = re.compile(
    r'  (\w[\w.]*?)\s+'                       # param name (camelCase)
    r'(string|number|boolean|Object|Array\.?<[^>]*>?|ArrayBuffer|function|'
    r'string/object/ArrayBuffer|string/Object/Arraybuffer|Object|string/ArrayBuffer)\s+'
    r'(?:'
    r'(?:[^\s，（。（(]|\([^)]*\))+\s+'        # default value
    r'(是|否)\s+'                              # required flag
    r'|'
    r'(是|否)\s+'                              # just the required flag (no default)
    r')',
    re.UNICODE
)

def fix_flattened_tables(text: str) -> str:
    """Split long lines where table rows were merged together."""
    lines = text.split('\n')
    result = []

    for line in lines:
        if len(line) < 200 or '|' not in line:
            result.append(line)
            continue

        # Find all field start positions
        splits = []
        for m in PARAM_CONTINUATION_RE.finditer(line):
            splits.append(m.start())

        if len(splits) < 2:
            result.append(line)
            continue

        # Split into separate lines at each field start
        first_field_start = splits[0]

        # Everything before the first new field stays on the original line
        result.append(line[:first_field_start].rstrip())

        # Each field becomes its own table row
        chunks = []
        prev = first_field_start
        for sp in splits[1:]:
            chunks.append(line[prev:sp])
            prev = sp
        chunks.append(line[prev:])

        for chunk in chunks:
            chunk = chunk.strip()
            if chunk:
                result.append(f"|  | {chunk} |")

    return '\n'.join(result)


# ═══════════════════════════════════════════════════════════
# Pipeline
# ═══════════════════════════════════════════════════════════

def process_file(filepath: str, dry_run: bool = False, verbose: bool = False) -> dict:
    """Apply all fixes to a single file. Returns change stats."""
    with open(filepath, 'r', encoding='utf-8') as f:
        original = f.read()

    text = original
    text = fix_footer(text)                # 1. strip footer noise
    text = fix_inline_headings(text)       # 2. split merged headings (creates new lines)
    text = fix_headings(text)              # 3. fix ## # → ## (after split creates new lines)
    text = fix_leading_space_headings(text) # 4. strip leading whitespace from headings
    text = fix_flattened_tables(text)      # 5. split flattened table rows
    # Run heading fixes again — the first pass can produce mid-line ## # patterns
    # that become new lines after fix_headings strips the first one
    text = fix_inline_headings(text)       # 6. catch any remaining merged headings
    text = fix_headings(text)              # 7. final heading cleanup
    text = fix_leading_space_headings(text) # 8. final whitespace cleanup

    # Clean up: trim trailing whitespace, ensure single trailing newline
    text = text.rstrip() + '\n'

    stats = {
        'changed': text != original,
        'path': filepath,
        'orig_size': len(original),
        'new_size': len(text),
    }

    if stats['changed'] and not dry_run:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(text)

    return stats


def main():
    parser = argparse.ArgumentParser(description='Post-process scraped WeChat minigame API docs')
    parser.add_argument('--dry-run', action='store_true', help='Preview changes without writing files')
    parser.add_argument('--category', default='', help='Fix only one category (e.g. "base", "network")')
    parser.add_argument('--verbose', '-v', action='store_true', help='Show per-file change stats')
    parser.add_argument('--input', default='', help='Input directory (default: references/wx-api-docs)')
    args = parser.parse_args()

    script_dir = Path(__file__).parent.resolve()
    project_dir = script_dir.parent
    input_dir = args.input or str(project_dir / 'references' / 'wx-api-docs')

    if not os.path.isdir(input_dir):
        print(f"Error: directory not found: {input_dir}")
        sys.exit(1)

    # Collect all .md files
    if args.category:
        pattern = os.path.join(input_dir, args.category.strip('/'), '**', '*.md')
    else:
        pattern = os.path.join(input_dir, '**', '*.md')

    import glob
    files = sorted(glob.glob(pattern, recursive=True))
    if not files:
        print(f"No .md files found matching: {pattern}")
        sys.exit(1)

    print(f"{'[DRY RUN] ' if args.dry_run else ''}Processing {len(files)} files in {input_dir}...")
    if args.category:
        print(f"    Category filter: {args.category}")
    print()

    changed = 0
    unchanged = 0
    total_saved = 0

    for filepath in files:
        stats = process_file(filepath, dry_run=args.dry_run, verbose=args.verbose)
        rel = os.path.relpath(filepath, input_dir)

        if stats['changed']:
            changed += 1
            delta = stats['new_size'] - stats['orig_size']
            total_saved += delta
            if args.verbose:
                sign = '+' if delta > 0 else ''
                print(f"  {'WOULD FIX' if args.dry_run else 'FIXED':10s} {rel:60s}  ({sign}{delta:,d} bytes)")
        else:
            unchanged += 1

    print()
    print(f"{'Would apply' if args.dry_run else 'Applied'} fixes to {changed} of {len(files)} files")
    print(f"Unchanged: {unchanged}")
    if changed:
        sign = '+' if total_saved > 0 else ''
        print(f"Size delta: {sign}{total_saved:,d} bytes")
    if args.dry_run:
        print(f"\nRun without --dry-run to apply changes.")


if __name__ == '__main__':
    main()
