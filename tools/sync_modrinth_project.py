"""Sync the Modrinth project page from the repository (docs/modrinth-project.json).

Updates the summary, description, categories, license and links, uploads the icon and
replaces the gallery images listed in the config. Versions are published separately by
`./gradlew publishMods`.

Usage: MODRINTH_TOKEN=... python3 tools/sync_modrinth_project.py
Without MODRINTH_TOKEN it only prints what it would do.
"""
import json
import os
import pathlib
import sys
import urllib.error
import urllib.parse
import urllib.request

API = "https://api.modrinth.com/v2"
USER_AGENT = "LZaruba/morning-stretch release workflow (github.com/LZaruba/morning-stretch)"
ROOT = pathlib.Path(__file__).resolve().parent.parent
PROJECT_FIELDS = ("description", "categories", "additional_categories", "license_id", "source_url", "issues_url")


def request(method, path, token, body=None, content_type=None, query=None):
    url = f"{API}{path}"
    if query:
        url += "?" + urllib.parse.urlencode(query)
    headers = {"Authorization": token, "User-Agent": USER_AGENT}
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=body, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            payload = response.read()
            return json.loads(payload) if payload else None
    except urllib.error.HTTPError as error:
        sys.exit(f"::error::Modrinth {method} {path} failed: {error.code} {error.read().decode(errors='replace')}")


def image_type(path):
    ext = path.suffix.lstrip(".").lower()
    return ext, f"image/{'jpeg' if ext == 'jpg' else ext}"


def main():
    config = json.loads((ROOT / "docs/modrinth-project.json").read_text())
    slug = config["slug"]
    token = os.environ.get("MODRINTH_TOKEN")

    fields = {key: config[key] for key in PROJECT_FIELDS if key in config}
    fields["body"] = (ROOT / config["body_file"]).read_text()
    icon = ROOT / config["icon"]
    gallery = config.get("gallery", [])

    if not token:
        print(f"Dry run (no MODRINTH_TOKEN): would update project '{slug}'")
        for key, value in fields.items():
            print(f"  {key}: {value if key != 'body' else f'<{len(value)} characters>'}")
        print(f"  icon: {icon.relative_to(ROOT)} ({icon.stat().st_size} bytes)")
        for item in gallery:
            print(f"  gallery: {item['file']} ({item['title']}, featured={item.get('featured', False)})")
        return

    project = request("GET", f"/project/{slug}", token)
    project_id = project["id"]

    request("PATCH", f"/project/{project_id}", token, json.dumps(fields).encode(), "application/json")
    print(f"Updated project fields: {', '.join(fields)}")

    ext, mime = image_type(icon)
    request("PATCH", f"/project/{project_id}/icon", token, icon.read_bytes(), mime, {"ext": ext})
    print(f"Uploaded icon {icon.relative_to(ROOT)}")

    # Replace gallery images with the same title, so re-running keeps one copy of each.
    titles = {item["title"] for item in gallery}
    for existing in project.get("gallery", []):
        if existing.get("title") in titles:
            request("DELETE", f"/project/{project_id}/gallery", token, query={"url": existing["url"]})
    for ordering, item in enumerate(gallery):
        path = ROOT / item["file"]
        ext, mime = image_type(path)
        query = {
            "ext": ext,
            "featured": str(item.get("featured", False)).lower(),
            "title": item["title"],
            "description": item.get("description", ""),
            "ordering": ordering,
        }
        request("POST", f"/project/{project_id}/gallery", token, path.read_bytes(), mime, query)
        print(f"Uploaded gallery image {item['file']}")


if __name__ == "__main__":
    main()
