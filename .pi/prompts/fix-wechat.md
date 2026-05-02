---
description: Debug WeChat DevTools errors with scout→plan→review→work flow, then rebuild and test
argument-hint: "<console-output-from-wechat-devtools>"
---

I'm testing the Solitaire minigame output in WeChat DevTools. Here is the console output from the devtools:

$@

---

Identify each distinct issue in the console output above. For **each** issue, run this full pipeline **sequentially** (finish issue A completely before starting issue B):

## Per-issue pipeline

### Step 1 — Scout

Use a **scout** subagent to figure out the root cause of the issue and how to fix it in the gdx-minigame backend. The scout should investigate freely — read whatever source files, generated output, or config are relevant to this specific error.

### Step 2 — Plan

Use a **planner** subagent to make a concrete fix plan based on the scout's findings.

### Step 3 — Review plan

Use a **reviewer** subagent to review the plan. **If the plan has issues, fix the plan and re-review until it's sound.**

### Step 4 — Implement + review loop

Use a **worker** subagent to implement the plan, then a **reviewer** subagent to review the implementation.
- If the reviewer finds issues → **send it back to the worker to fix**, then review again.
- Repeat this worker→reviewer cycle until the issue is cleanly solved.

### Step 5 — Build and publish

After **all** issues are resolved, build and publish the backend, then rebuild Solitaire so I can test again in WeChat DevTools. Use the project's standard build commands (the gdx-minigame backend publishes to Maven Local, then rebuild the Solitaire minigame variant).

Report what was fixed and what to look for in the next WeChat DevTools test run.
