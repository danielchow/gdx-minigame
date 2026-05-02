# Tools

Utility scripts for the gdx-minigame project.

## scrape-wx-api.py

Fetches the **complete WeChat minigame `wx.*` API documentation** from the official developer docs site and saves it as local markdown files.

### How it works

1. Fetches any minigame API page (the sidebar is server-rendered on every page)
2. Extracts all API doc URLs from the sidebar navigation (~900+ links)
3. Downloads each page in parallel, extracts the `<div class="content custom">` block
4. Converts HTML → clean markdown (tables, headers, code blocks, inline formatting)
5. Saves to `references/wx-api-docs/<category>/<...>/<name>.md`

### Usage

```bash
# Fetch everything (skips files that already exist and are >200 bytes)
python3 tools/scrape-wx-api.py

# Fetch only one category
python3 tools/scrape-wx-api.py --category base
python3 tools/scrape-wx-api.py --category network/websocket
python3 tools/scrape-wx-api.py --category media/audio

# Just list URLs without fetching
python3 tools/scrape-wx-api.py --dry-run

# Control parallelism and retries
python3 tools/scrape-wx-api.py --concurrency 12 --retries 3

# Custom output directory
python3 tools/scrape-wx-api.py --output ./my-api-docs
```

### Options

| Flag | Default | Description |
|------|---------|-------------|
| `--category` | (all) | Filter to a specific category path (e.g. `base`, `device/keyboard`) |
| `--concurrency` | 8 | Number of parallel downloads |
| `--retries` | 2 | Retries per page on failure |
| `--output` | `references/wx-api-docs` | Output directory |
| `--dry-run` | off | List discovered URLs without fetching |

### Output

```
references/wx-api-docs/
├── base/              # System info, lifecycle, performance, debug, update, crypto
├── device/            # Touch, keyboard, mouse, accelerometer, gyroscope, screen, ...
├── file/              # FileSystemManager with all sync/async methods
├── media/             # Audio, video, camera, recorder, VoIP
├── network/           # HTTP, WebSocket, TCP, UDP, download, upload
├── render/            # Canvas, image, frame rate, font, cursor
├── storage/           # Local storage, background fetch
├── ad/                # Banner, rewarded video, interstitial, custom ads
├── open-api/          # Login, user info, cloud data, game club, ...
├── share/             # Share menu, timeline, favorites
├── ui/                # Toast, modal, loading, menu, window
├── ai/                # Inference, vision kit
├── worker/            # Web Workers
└── ...                # navigate, payment, data-analysis, location, etc.
```

Each file starts with a `> Source:` link to the original page and contains the full API documentation in markdown (signatures, parameter tables, return values, code examples).

### Requirements

- Python 3.7+ (no external dependencies — uses only stdlib)
- Internet access to `developers.weixin.qq.com`

### Refreshing

Re-run the script anytime. It skips files that already exist and are larger than 200 bytes, so incremental updates are fast. To force re-fetch, delete the output directory first:

```bash
rm -rf references/wx-api-docs
python3 tools/scrape-wx-api.py
```

## fix-wx-api-docs.py

Post-processes scraped WeChat minigame API docs to fix 3 systemic issues:

1. **Broken heading anchors** — `## # Title` → `## Title` (anchor link artifact)
2. **Translation footer spam** — strips "The translations are provided by WeChat Translation..." boilerplate
3. **Flattened table rows** — splits long lines where table rows merged together

Also fixes: headings on the same line as table content, leading whitespace on headings.

### Usage

```bash
# Fix all files in-place
python3 tools/fix-wx-api-docs.py

# Preview changes without writing
python3 tools/fix-wx-api-docs.py --dry-run

# Fix only one category
python3 tools/fix-wx-api-docs.py --category network

# Show per-file stats
python3 tools/fix-wx-api-docs.py --verbose
```

### Typical workflow

```bash
# 1. Scrape (or refresh) all API docs
python3 tools/scrape-wx-api.py

# 2. Post-process to clean up
python3 tools/fix-wx-api-docs.py
```

### Requirements

- Python 3.7+ (no external dependencies — uses only stdlib)
