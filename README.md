# ArcheryClock

**An independent, cross-platform rewrite and tribute to [ArcheryClock.com](https://www.archeryclock.com/).**

## About

The original ArcheryClock is an archery competition countdown system written in Pascal using the Lazarus development environment.

It provides a practical solution for managing countdowns during archery competitions. However, its underlying technology and platform support limit its availability across operating systems.

This project aims to address that limitation through an independent rewrite in Java.

The goal is not to redesign ArcheryClock or introduce a fundamentally different system. The goal is to preserve the original software as closely as possible while making it available on a wider range of platforms.

**Compatibility with the original is a primary design objective.**

Wherever technically feasible, this rewrite aims to preserve the original functionality, behaviour, user interface, configuration, communication protocols and hardware integrations.

The intention is to make the Java implementation a drop-in replacement for the original ArcheryClock, allowing existing users to switch implementations without having to change their established workflows or competition setup.

## Design principles

### Compatibility first

The original ArcheryClock defines the functional reference for this project.

Existing behaviour should be preserved unless there is a compelling technical reason to change it. Differences should be deliberate, documented and justified.

### Cross-platform availability

The original software is written in Pascal using Lazarus. This rewrite uses Java to make the application available on:

- Windows
- Linux
- macOS

The objective is to provide consistent behaviour across all supported platforms.

### Reliability

An archery countdown system must be dependable.

Timing must remain independent of graphical rendering, network communication and video streaming. A failure in one of these components should not interrupt the countdown itself.

### Familiar user experience

The original interface and operating workflow should be retained as closely as possible.

Existing users should recognise the application and be able to operate it without having to learn an entirely new system.

### Maintainability

The implementation uses a modern, widely supported technology stack, making the software easier to maintain and extend over time.

## Compatibility goals

The following areas are important compatibility targets:

- Countdown and timing behaviour.
- Competition and round management.
- User interface and controls.
- Existing configuration formats.
- Network communication and timing protocols.
- External displays and remote viewing.
- Supported hardware and peripheral integrations.

Compatibility will be implemented and verified progressively.

Not every feature or integration may be available immediately. Differences and limitations will be documented as development progresses.

## Technology

The application is developed using:

- **Java 27** — application runtime and core implementation.
- **Maven** — dependency management and build automation.
- **Swing / Java2D** — graphical user interface.
- **jpackage** — standalone application packaging.
- **JUnit 5** — automated testing.

## Building

A Java 27 JDK and Maven are required.

Build the project:

```bash
mvn clean package
```

Run the tests:

```bash
mvn test
```

Create a release build:

```bash
mvn clean package -Prelease
```

The release profile creates a self-contained application image and native installers in `target/dist/`. Build on each target operating system and architecture; `jpackage` does not cross-package. Windows creates an EXE installer, Linux creates DEB and RPM installers, and macOS creates a DMG installer. Windows builds require WiX; Linux builds require `dpkg-deb` and `rpmbuild`.

Regular Maven builds keep all JavaCV platform binaries. For a smaller local release, add the matching target profile, for example `mvn clean package -Prelease,release-macos-arm64` on Apple Silicon. GitHub Actions selects the matching profile for each release runner automatically.

## GitHub releases

Published packages are available on [GitHub Releases](https://github.com/ronlievens/archeryclock/releases).

The GitHub Actions workflow builds Linux and Windows packages for x64 and ARM64, plus macOS for ARM64. Each release build filters JavaCV native classifiers to its target OS and architecture; the ordinary Maven build still resolves all `javacv-platform` binaries. Linux releases include DEB, RPM and Flatpak packages; Flatpak uses the Freedesktop 25.08 runtime.

Push a tag such as `v1.2.3` to create a stable GitHub Release; the tag sets the Maven release version. Push to `main` to build and update the `snapshot-main` pre-release. Snapshot asset filenames retain the Maven `-SNAPSHOT` suffix. Asset names use `<artifactId>-<version>-<os>-<architecture>.<extension>`, with `-app-image` added for app-image archives. Windows ARM64 uses a Zulu JDK because Temurin does not publish Windows ARM64 builds.

```bash
git tag v1.2.3
git push origin v1.2.3
```

## Planned release targets

| Operating system | Architecture | Package |
|---|---|---|
| Windows | x64 / ARM64 | Application image + EXE installer |
| Linux | x64 / ARM64 | Application image + DEB, RPM and Flatpak |
| macOS | x64 / ARM64 | Application image + DMG installer |

A standalone JAR will also be provided.

Standalone packages include their own Java runtime and do not require users to install Java separately.

## Acknowledgement

This project is a tribute to the original ArcheryClock and the work behind it.

The original Pascal/Lazarus implementation is the functional and historical reference for this rewrite. Its design, functionality and established workflows are the foundation on which this independent implementation is built.

The objective is to preserve that work, not to replace its identity with something unrelated.

Contributors are listed in [AUTHORS.md](AUTHORS.md).

## Disclaimer

This is an independent rewrite and tribute project. It is not affiliated with, endorsed by, sponsored by or officially connected to the original ArcheryClock.com or its creator.

All rights to the original software, trademarks and branding remain with their respective owners.

## License

See [LICENSE](LICENSE) for the licensing terms of this implementation.

The license of this project does not grant rights to third-party trademarks, branding or intellectual property associated with the original ArcheryClock.
