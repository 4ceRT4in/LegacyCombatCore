# Legacy Combat Core

A Fabric mod for Minecraft 1.21.1 that provides a clean foundation for legacy-style combat mechanics.

## Requirements

- Java Development Kit 21
- Minecraft 1.21.1
- Fabric Loader 0.19.5 or newer
- Fabric API

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Development

Import the repository as a Gradle project in IntelliJ IDEA or another Java IDE.

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

The built mod JAR is written to `build/libs/`.

Common code lives in `src/main`, while client-only code lives in `src/client`. Mixin configuration files are present and ready for future combat hooks.

## Implemented mechanics

- Minecraft 1.8.9 base, sprint and Knockback-enchantment velocity application
- Minecraft 1.8.9 probabilistic knockback-resistance behavior
- Minecraft 1.8.9 hit-sound behavior without the modern split player-attack sounds

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
