#!/usr/bin/env python3
"""Validate guide naming and command parity rules."""

from __future__ import annotations

import glob
import os
import re

SHELL_TAGS = ("```bash", "```sh", "```shell", "```zsh")


def iter_guide_files() -> list[str]:
    files: list[str] = []
    for project_dir in sorted(glob.glob("[0-9]*-*")):
        if not os.path.isdir(project_dir):
            continue
        for name in sorted(os.listdir(project_dir)):
            low = name.lower()
            if not low.endswith(".md"):
                continue
            if not any(token in low for token in ("lab", "guia", "guide")):
                continue
            files.append(os.path.join(project_dir, name))
    return files


def main() -> None:
    issues: list[str] = []
    for path in iter_guide_files():
        directory = os.path.basename(os.path.dirname(path))
        prefix = directory.split("-", 1)[0]
        filename = os.path.basename(path)

        if not filename.startswith(f"{prefix}-"):
            issues.append(f"name-prefix: {path}")

        text = open(path, "r", encoding="utf-8", errors="ignore").read().lower()
        has_bash = any(tag in text for tag in SHELL_TAGS)
        has_powershell = "```powershell" in text
        if has_bash and not has_powershell:
            issues.append(f"missing-powershell: {path}")

        has_docker = re.search(r"(?m)^\s*docker(?:\s|$)", text) is not None
        has_podman = "podman" in text
        if has_docker and not has_podman:
            issues.append(f"missing-podman: {path}")

        if "azure" in text and "14-deploy-k8s-start" not in path:
            issues.append(f"azure-outside-14: {path}")

    if issues:
        print(f"FAIL: {len(issues)} issues")
        for issue in issues:
            print(issue)
        raise SystemExit(1)

    print("OK: all guide checks passed")


if __name__ == "__main__":
    main()

