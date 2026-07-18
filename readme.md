# Create: Chemworks

A Minecraft 1.21.1 mod built with NeoForge and Create.

## Requirements

- A Java 21 JDK (Gradle can provision one automatically through Foojay)
- IntelliJ IDEA, Eclipse, or VS Code with Java support

## First run

```sh
./gradlew build
./gradlew runClientParshwa
./gradlew runClientHursh
./gradlew runClientAdvait
./gradlew runClientMilet
```

Each client task has an isolated run directory and an offline development identity.
Change the `dev_username_*` values in `gradle.properties` to use different display
names. These values do not log into or verify real Microsoft/Minecraft accounts.

The client uses `run/`. Its development profile includes Create, JEI, e4mc, Sodium,
FerriteCore, ModernFix, Jade, AppleSkin, and Mouse Tweaks. These helper mods are
declared as `localRuntime`, so they are available during development but are not
published as dependencies of Chemworks.

Use **Open to LAN** in a single-player world to get an e4mc share link. Never expose
a world containing secrets or operator permissions to people you do not trust.

The dedicated server profile uses `run-server/` and intentionally does not copy
client configuration or accept the EULA automatically.

## IDE setup

Import this directory as a Gradle project and select the Java 21 toolchain. Run
`./gradlew idea` if IntelliJ run configurations do not appear after Gradle sync.
