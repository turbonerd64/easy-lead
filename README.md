# Easy Lead 🪢

A Minecraft Fabric mod for **Minecraft 26.2+** that removes the restriction limiting leads/leashes to fence posts. **Easy Lead** allows players to tether and untether horses (and any other leashable mobs) to **any solid block type** (stone, wood logs, cobblestone, dirt, deepslate, bricks, walls, etc.).

---

## ✨ Features

- **Tether to Any Solid Block**: Right-click any solid block with a lead or while holding leashed animals to anchor them.
- **Untether by Right-Clicking**: Right-clicking the anchor block effortlessly untethers the animals and returns the leashes directly to the player's hands.
- **Native Vanilla Physics**: Seamlessly integrates with Minecraft's native `Leashable` tension, elastic spring damping, and snap mechanics.
- **Block Destruction Safety**: Mining or exploding the anchor block cleanly detaches animals and drops leads on the ground.
- **Datapack Tag Support**: Custom `#easylead:leash_anchors` and `#easylead:leash_anchors_blacklist` tags for modpack creators.

---

## 🛠️ Building & Developing

### Requirements
- **Java 25+** (e.g. Azul Zulu 25 or Eclipse Temurin 25)
- **Fabric Loader 0.19.3+**
- **Minecraft 26.2**

### Build Commands
```bash
./gradlew build
```
The compiled jar will be generated under `build/libs/easylead-1.0.0+26.2.jar`.

---

## 📄 License
This project is licensed under the MIT License.
