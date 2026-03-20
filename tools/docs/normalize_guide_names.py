#!/usr/bin/env python3
"""Normalize lab guide names to start with the project prefix and update markdown references."""

from __future__ import annotations

import glob
import os
import re
import urllib.parse
from dataclasses import dataclass


@dataclass(frozen=True)
class RenameItem:
    old_path: str
    new_path: str
    old_name: str
    new_name: str


def should_rename(name: str, project_prefix: str) -> bool:
    low = name.lower()
    if not low.endswith(".md"):
        return False
    if name in {"README.md", ".synthesis.md"}:
        return False
    if not any(token in low for token in ("lab", "guia", "guide")):
        return False
    return not name.startswith(f"{project_prefix}-")


def target_name(name: str, project_prefix: str) -> str:
    base = re.sub(r"^\d+(?:\.\d+)?[- _]*", "", name)
    return f"{project_prefix}-{base}"


def collect_renames() -> list[RenameItem]:
    renames: list[RenameItem] = []
    for project_dir in sorted(glob.glob("[0-9]*-*")):
        if not os.path.isdir(project_dir):
            continue
        project_prefix = project_dir.split("-", 1)[0]
        for entry in sorted(os.listdir(project_dir)):
            if not should_rename(entry, project_prefix):
                continue
            new_name = target_name(entry, project_prefix)
            old_path = os.path.join(project_dir, entry)
            new_path = os.path.join(project_dir, new_name)
            if os.path.exists(new_path):
                raise FileExistsError(f"Target already exists: {new_path}")
            renames.append(RenameItem(old_path, new_path, entry, new_name))
    return renames


def apply_renames(renames: list[RenameItem]) -> None:
    for item in renames:
        os.rename(item.old_path, item.new_path)


def update_markdown_references(renames: list[RenameItem]) -> int:
    updated = 0
    markdown_files: list[str] = []
    for dirpath, _, filenames in os.walk("."):
        for filename in filenames:
            if filename.lower().endswith(".md"):
                markdown_files.append(os.path.join(dirpath, filename))

    for path in markdown_files:
        with open(path, "r", encoding="utf-8", errors="ignore") as handle:
            original = handle.read()

        rewritten = original
        for item in renames:
            replacements = [
                (item.old_path, item.new_path),
                (item.old_name, item.new_name),
                (item.old_path.replace(" ", "%20"), item.new_path.replace(" ", "%20")),
                (item.old_name.replace(" ", "%20"), item.new_name.replace(" ", "%20")),
                (urllib.parse.quote(item.old_path), urllib.parse.quote(item.new_path)),
                (urllib.parse.quote(item.old_name), urllib.parse.quote(item.new_name)),
            ]
            for old, new in replacements:
                rewritten = rewritten.replace(old, new)

        if rewritten != original:
            with open(path, "w", encoding="utf-8") as handle:
                handle.write(rewritten)
            updated += 1
    return updated


def main() -> None:
    renames = collect_renames()
    if not renames:
        print("No guide files needed renaming")
        return

    apply_renames(renames)
    touched = update_markdown_references(renames)
    print(f"Renamed {len(renames)} guide files")
    print(f"Updated {touched} markdown files with new references")


if __name__ == "__main__":
    main()

