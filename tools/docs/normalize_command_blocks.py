#!/usr/bin/env python3
"""Ensure guide command blocks include Bash + PowerShell and Docker + Podman variants."""

from __future__ import annotations

import glob
import os
import re

SHELL_LANGS = {"bash", "sh", "shell", "zsh"}


def powershell_from_bash(content: str) -> str:
    lines = []
    for line in content.splitlines():
        converted = line
        if converted.startswith("./"):
            converted = ".\\" + converted[2:]
        converted = converted.replace("./mvnw", ".\\mvnw.cmd")
        converted = converted.replace("./gradlew", ".\\gradlew.bat")
        lines.append(converted)
    return "\n".join(lines)


def podman_from_docker(content: str) -> str:
    converted = content
    converted = converted.replace("docker-compose", "podman-compose")
    converted = converted.replace("docker compose", "podman compose")
    converted = re.sub(r"\bdocker\b", "podman", converted)
    return converted


def normalize_file(path: str) -> bool:
    with open(path, "r", encoding="utf-8", errors="ignore") as handle:
        text = handle.read()

    lines = text.splitlines()
    output: list[str] = []
    changed = False
    i = 0

    while i < len(lines):
        line = lines[i]
        fence = re.match(r"^```([A-Za-z0-9_-]+)\s*$", line)
        if not fence:
            output.append(line)
            i += 1
            continue

        lang = fence.group(1).lower()
        block_start = i
        i += 1
        block_lines: list[str] = []
        while i < len(lines) and lines[i] != "```":
            block_lines.append(lines[i])
            i += 1

        if i >= len(lines):
            output.extend(lines[block_start:])
            break

        # include original block
        output.append(lines[block_start])
        output.extend(block_lines)
        output.append("```")

        # closed fence
        i += 1

        # peek forward skipping blank lines
        j = i
        while j < len(lines) and lines[j].strip() == "":
            j += 1

        next_lang = None
        if j < len(lines):
            next_fence = re.match(r"^```([A-Za-z0-9_-]+)\s*$", lines[j])
            if next_fence:
                next_lang = next_fence.group(1).lower()

        block_text = "\n".join(block_lines)

        # Add PowerShell after Bash-like blocks when absent.
        if lang in SHELL_LANGS and next_lang != "powershell":
            output.append("")
            output.append("```powershell")
            output.extend(powershell_from_bash(block_text).splitlines())
            output.append("```")
            changed = True

        # Add Podman variant when Docker command appears and no podman in following block.
        has_docker = re.search(r"\bdocker\b|docker compose|docker-compose", block_text) is not None
        if has_docker and "podman" not in block_text.lower() and next_lang not in {"podman", "bash"}:
            podman_text = podman_from_docker(block_text)
            if podman_text != block_text:
                output.append("")
                output.append("```bash")
                output.extend(podman_text.splitlines())
                output.append("```")
                changed = True

    normalized = "\n".join(output)
    if text.endswith("\n"):
        normalized += "\n"

    if changed and normalized != text:
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(normalized)
        return True

    return False


def main() -> None:
    changed_count = 0
    for project_dir in sorted(glob.glob("[0-9]*-*")):
        if not os.path.isdir(project_dir):
            continue
        for name in sorted(os.listdir(project_dir)):
            low = name.lower()
            if not low.endswith(".md"):
                continue
            if not any(token in low for token in ("lab", "guia", "guide")):
                continue
            path = os.path.join(project_dir, name)
            if normalize_file(path):
                changed_count += 1

    print(f"Updated {changed_count} guide files")


if __name__ == "__main__":
    main()

