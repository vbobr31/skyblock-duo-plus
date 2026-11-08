# SkyblockDuoPlus

Classic shared-island SkyBlock plugin for Spigot/Paper 1.12.2 (Java 8).

## Features
- Shared starter island for two players: owner invites one friend.
- Expanded 31x31 grass/stone island with starter tree, chest, water/lava and a few ore blocks.
- Sponge Lucky Block at the center approach; breaks into a random resource and regenerates.
- No economy, shop, auction house, currency or trading plugin is bundled.

## Build
Run GitHub Actions workflow **Build SkyblockDuoPlus**, or locally:
`mvn clean package`
Artifact: `target/skyblock-duo-plus-1.0.0.jar`

## Install
1. Use a Minecraft Java 1.12.2 Spigot/Paper-compatible server with Java 8.
2. Upload the JAR to `plugins/`.
3. Restart the server.
4. First player runs `/island`; owner invites the online friend with `/island invite PlayerName`; friend runs `/island accept`.
5. Use `/luckyblock` for a portable sponge Lucky Block.

## Important limitations
This is an initial plugin scaffold, not a full multi-island SkyBlock network. It creates one shared island in the first loaded world. Back up the world before installing. The Lucky Block is a custom sponge block and its drops are a simple configurable-in-source list. Test on a disposable server before public use. Minecraft 1.14 compatibility is not guaranteed; target 1.12.2 first.
