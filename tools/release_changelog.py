"""Write the changelog section for a release into CHANGELOG.md.

Turns the "## Unreleased" section into "## <version>" and starts a new empty one. When
Unreleased is empty, the section is generated from GitHub's release notes (the merged
pull requests since the previous release), passed as a file.

Usage: python3 tools/release_changelog.py <version> <github-notes.md>
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
# "* Title by @author in https://github.com/owner/repo/pull/12"
PULL_REQUEST_LINE = re.compile(r"^\* (?P<title>.+) by @\S+ in (?P<url>\S+/pull/(?P<number>\d+))$")


def notes_from_github(text):
    lines = []
    for line in text.splitlines():
        match = PULL_REQUEST_LINE.match(line.strip())
        if match:
            lines.append(f"- {match['title']} ([#{match['number']}]({match['url']}))")
    return "\n".join(lines)


def main():
    version, github_notes = sys.argv[1], pathlib.Path(sys.argv[2])
    path = ROOT / "CHANGELOG.md"
    head, found, rest = path.read_text().partition("## Unreleased\n")
    if not found:
        sys.exit("::error::CHANGELOG.md has no '## Unreleased' section")

    parts = re.split(r"(?m)^(?=## )", rest, maxsplit=1)
    notes = parts[0].strip()
    older = parts[1] if len(parts) > 1 else ""
    if not notes:
        notes = notes_from_github(github_notes.read_text())
        print("Unreleased is empty, using notes generated from merged pull requests")
    if not notes:
        sys.exit("::error::Nothing to release: Unreleased in CHANGELOG.md is empty and no pull requests were merged since the last release")

    section = f"## Unreleased\n\n## {version}\n\n{notes}\n"
    path.write_text(f"{head}{section}\n{older}".rstrip() + "\n")
    print(notes)


if __name__ == "__main__":
    main()
