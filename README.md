# Morning Stretch

A Fabric mod for **Minecraft 26.3** that lets your character stretch: arms flung wide and up, a slight lean back and a big yawn, just like after waking up.

Made by Matyáš Záruba and Lukáš Záruba.

*Česká verze: [README.cs.md](README.cs.md)*

## Features

| When | What happens |
|---|---|
| You wake up in a bed | Your character stretches automatically |
| You press **R** (rebindable) | Your character stretches any time |
| Multiplayer | Nearby players see the animation too |

While stretching (about 4 seconds):

- you walk at sneaking speed and cannot sprint,
- other players do not see your name tag or your dot on the locator bar,
- commands cannot find you (`@a`, `@p`, `@r`, `@e` or your name), and your name is not offered in command suggestions (Tab).

The stretch comes with breathing and yawning sounds and works in both first and third person.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.3.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and Morning Stretch into your `mods` folder.

Install the mod on **both the client and the server** for everything to work. With only one side:

- **client only** (e.g. on a vanilla server): you can stretch, but only you see it; the slowdown and name suggestion hiding still apply,
- **server only**: players stretch after waking up and are hidden from the locator bar and from commands, but nobody sees the animation and there is no key.

## Building from source

Requirements: Java 17+ to run Gradle. The mod itself targets Java 25; Gradle downloads it automatically when it is missing.

```bash
./gradlew build       # build/libs/morning-stretch-<version>.jar
./gradlew runClient   # start a development client with the mod
```

The development client has a `/stretchtest spawn` command that places a mannequin which stretches every 10 seconds, so you can test the hiding without a second player (`/stretchtest remove` removes them).

## Releasing

1. Merge the changes to `main`. Pull request titles end up in the changelog, so write them for players.
2. Run the **release** workflow (*Actions → release → Run workflow*). Pick which part of the version to bump (`patch`, `minor`, `major`) or type an exact version.

The workflow takes `main`, bumps `mod_version` in `gradle.properties` and writes the changelog section for the version into [CHANGELOG.md](CHANGELOG.md): the hand-written **Unreleased** section if there is one, otherwise the list of pull requests merged since the last release ([tools/release_changelog.py](tools/release_changelog.py)). Then it commits and tags the release (`v<version>`) and publishes the mod to Modrinth and GitHub Releases with that changelog.

It needs the `MODRINTH_TOKEN` secret in the `default` environment (*Settings → Environments*): a Modrinth personal access token with the *Read projects*, *Write projects* and *Create versions* scopes.

The Modrinth project page is kept in the repository and synced on every release by [tools/sync_modrinth_project.py](tools/sync_modrinth_project.py): summary, categories, license and links in [docs/modrinth-project.json](docs/modrinth-project.json), the description in [docs/modrinth-description.md](docs/modrinth-description.md), the icon and the gallery images in [docs/gallery](docs/gallery). To sync it without a release, run the **modrinth-project** workflow. The icon and gallery images are rendered by [tools/render_icon.py](tools/render_icon.py).

`./gradlew publishMods` without `MODRINTH_TOKEN` only does a dry run.

## License

[LGPL-3.0-or-later](LICENSE.LESSER) (see also [LICENSE](LICENSE)).
