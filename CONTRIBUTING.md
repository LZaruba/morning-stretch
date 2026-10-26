# Contributing to Morning Stretch

Thanks for helping out! Bug reports, ideas and pull requests are all welcome.

## Reporting bugs and suggesting features

Open an [issue](https://github.com/LZaruba/morning-stretch/issues/new/choose) and pick the bug report or feature request template. For bugs, include the mod, Minecraft and Fabric versions and the log (`logs/latest.log`) if the game crashed.

Security problems should not go into a public issue; see [SECURITY.md](SECURITY.md).

## Development setup

You need Java 17 or newer to run Gradle. The mod targets Java 25, which Gradle downloads automatically when it is missing.

```bash
./gradlew build       # build/libs/morning-stretch-<version>.jar
./gradlew runClient   # start a development client with the mod
```

The development client has a `/stretchtest spawn` command that places a mannequin which stretches every 10 seconds, so you can check the animation, the hidden name tag, the locator bar and command hiding without a second player. `/stretchtest remove` removes the mannequins.

Code and everything in it (identifiers, comments, commit messages) is written in English.

## Pull requests

1. Fork the repository and create a branch from `main`.
2. Keep the change focused on one thing and follow the style of the surrounding code.
3. Run `./gradlew build` and try the change in `./gradlew runClient`.
4. Open a pull request against `main` and describe what you changed and how you tested it.

**The pull request title goes into the release changelog** shown to players on Modrinth and GitHub, so write it for players: "Stretch no longer restarts when pressing R repeatedly" rather than "Fix StretchClientState".

Every pull request is built automatically and reviewed by a maintainer before it is merged. Workflows from outside contributors start after a maintainer approves them.

## Releases

Releases are made by the maintainers with the release workflow; see the [README](README.md#releasing). Contributors do not need to change the version or `CHANGELOG.md`.

## License

By contributing you agree that your contribution is licensed under the project's license, [LGPL-3.0-or-later](LICENSE.LESSER).
