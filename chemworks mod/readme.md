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

Each client task has an isolated run directory and a named DevLogin profile. On the
first launch, copy the device code printed in the terminal, open
`https://www.microsoft.com/link`, and sign in with the matching Minecraft-owning
Microsoft account. DevLogin caches each profile under `~/.devlogin/`; never commit,
share, or upload that directory because its files contain authentication tokens.

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

## Chemistry testing

- Right-click a **Creative Flask**, then choose an element in the periodic-table UI.
- Insert up to nine configured flasks by right-clicking a **Reaction Tester**.
- Pulse the tester with redstone to test every unique pair. Results append to
  `logs/create-chemworks-reaction-tester.log`. Sneak-right-click empty-handed to remove a flask.
- Empty-hand right-click opens the experiment editor for temperature, pressure, concentration,
  and catalyst/reactant roles. Reports include estimated forward and reverse rates when reversible.
- Creative Flasks accept an element, a validated compound, `e_-`, or a solution written as
  `x(compound) + y(compound)`. The coefficients define its concentration ratio.
- Run `reactiontest batch` as an operator for 1,000 checks, or `reactiontest batch <count>`
  for up to 100,000. Each run writes a timestamped report in `logs/`.
