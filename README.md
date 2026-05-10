# Smoking Warning Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/Mod%20Loader-NeoForge-orange)](https://neoforged.net)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net)

**Author: 寿云** · [Bilibili](https://space.bilibili.com/1832031043)

**Contributors: 寿云, ChatGPT, DeepSeek**

## Overview

Smoking Warning Mod is a **Minecraft 1.21.1 + NeoForge** survival expansion mod.

This mod features tobacco farming, processing, cigarette crafting, smoking mechanics, addiction progression, lung cancer stages, a full treatment system, HUD display, village no-smoking zones, tobacco villagers, **Create mechanical power integration**, and **optional recipe viewer compatibility** (JEI / REI / Jade).

The core design philosophy is: **short-term benefits, long-term consequences.** Smoking may provide brief positive effects early on, but as cigarette consumption increases, players progress through light, medium, and heavy addiction stages, eventually facing coughing, lung cancer, and health damage.

## Important Notice

All content related to smoking, tobacco, lung cancer, and treatment in this mod is intended as **gameplay mechanics and health-awareness storytelling**. It does **not** constitute real medical advice, and it does **not** encourage real-life smoking.

**Smoking is harmful to your health.**

If you have real health concerns, please seek professional medical help.

## Supported Environment

| Item | Details |
|------|---------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| Required Dependencies | None |

**Optional Compatibility (not required):**

| Mod | Purpose |
|-----|---------|
| [Create](https://modrinth.com/mod/create) 6.0.10+ | Mechanical tobacco processing pipeline |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) | Recipe viewer |
| [REI](https://modrinth.com/mod/rei) 16.x | Recipe viewer |
| [Jade](https://www.curseforge.com/minecraft/mc-mods/jade) | Block info display |

All third-party mods listed above are **optional** — none are required.

## Gameplay

### Tobacco Farming

- Plant tobacco seeds on farmland
- Tobacco crops grow through 7 stages
- Harvest mature crops for tobacco leaves

### Basic Processing Chain

```
Tobacco Leaf → [Drying Rack] → Dried Tobacco Leaf → [Tobacco Grinder] → Tobacco Shreds → [Tobacco Workbench] → Cigarette
```

### Cigarette System

- **Regular Cigarette** — Basic cigarette; brief Speed and Haste on finish
- **Variant Cigarettes** — Menthol, Honey, Blaze, Phantom, Ender, Glow, Redstone, Netherite — each with unique effects
- **Special Cigarettes** — Huazi, Lotus, Rick V — fun easter-egg variants
- Every cigarette has durability and must be smoked continuously
- A cigarette butt remains after finishing
- Brief screen distortion on finish
- Positive rewards decay as addiction deepens
- Smoking in the lung cancer stage deals direct health damage

### Addiction System

| Stage | Cigarettes Smoked | Effects |
|-------|-------------------|---------|
| None | 0–4 | No penalties |
| Light | 5–9 | Occasional coughing |
| Medium | 10–19 | Worsening cough, Weakness, Mining Fatigue, max health reduction |
| Heavy / Lung Cancer | 20+ | Severe coughing, lung cancer effect, smoking deals damage |

Higher addiction stages mean harsher penalties and weaker cigarette rewards.

### Treatment System

Lung cancer cannot be cured instantly with a single item. Treatment is **expensive, long-term, and painful** — and requires quitting smoking.

Treatment progression:

1. **Diagnosis** — Use the Lung Scanner (requires Heavy addiction or Lung Cancer stage)
2. **Chemotherapy** — Use Chemotherapy Medicine to control the disease (+10% progress; causes Weakness, Nausea, Slowness)
3. **Radiotherapy** — Use Radiotherapy Core (requires ≥20% progress; +20% progress)
4. **Targeted Therapy** — Use Targeted Therapy Medicine (requires ≥40% progress; +25% progress)
5. **Rehabilitation Plan** — Use the plan to enter rehab (requires ≥80% progress)
6. Stay smoke-free for an extended period
7. Lung cancer enters remission — but addiction records are **not** fully cleared

Smoking during treatment reduces progress. Smoking during rehab causes complete failure.

### Treatment Guide

Players can purchase the **Lung Cancer Treatment Guide** from a Master Tobacco Villager. The guide uses the vanilla book interface and explains the full treatment process.

### Village No-Smoking Zones

- Villages are no-smoking zones; a warning appears on entry
- Smoking in village areas **provokes Iron Golems**
- Iron Golems will attack smoking players
- Tobacco Villagers can spawn in villages; Masters sell the Treatment Guide

### HUD System

- Displays cigarettes smoked and current addiction stage
- Progress bar shows distance to the next stage
- Lung cancer stage shows a danger warning
- Press **H** to enter HUD edit mode
- Drag to reposition, scroll to resize
- Position and scale are saved

### Create / Mechanical Power (Optional)

**Create is not required.** When Create 6.0.10+ is installed, a full **automated tobacco processing pipeline** becomes available.

The mechanical route does **not** replace the manual route. Original recipes and blocks (Drying Rack, Tobacco Grinder, Tobacco Workbench) are fully preserved. The Create pipeline is better suited for mid-to-late-game mass automation.

Full industrial pipeline:

```
Tobacco Leaf → [Encased Fan + Fire/Campfire] → Dried Tobacco Leaf
Dried Tobacco Leaf → [Millstone] / [Crushing Wheels] → Coarse Tobacco Shreds
Coarse Tobacco Shreds → [Millstone] / [Mixer + Sugar] → Refined Tobacco Shreds
Paper → [Mechanical Press] → Cigarette Paper
String + Cigarette Paper → [Mixer] → Cigarette Filter
Cigarette Paper + Refined Shreds + Filter → [Sequenced Assembly] → Unfinished Cigarette
Unfinished Cigarette → [Mechanical Press] → Cigarette
Cigarette + Special Material → [Deployer + Press] → Variant Cigarette
Ender Cigarette → [7-Step Sequenced Assembly] → Rick V
Cigarette → [Sequenced Assembly] → Huazi / Lotus
```

#### Mechanical Variant Cigarette Recipes

| Cigarette | Material Added |
|-----------|---------------|
| Menthol | Ice or Snowball |
| Honey | Honey Bottle |
| Blaze | Blaze Powder |
| Phantom | Phantom Membrane |
| Ender | Ender Pearl |
| Glow | Glowstone Dust |
| Redstone | Redstone Dust |
| Netherite | Netherite Scrap |

#### New Intermediate Items

| Item | Purpose |
|------|---------|
| Coarse Tobacco Shreds | First grinding stage |
| Refined Tobacco Shreds | Rolling material |
| Cigarette Paper | Rolling material |
| Cigarette Filter | Rolling material |
| Unfinished Cigarette | Semi-finished product |
| Packaged Cigarette | Industrial package; craftable to unpack into cigarettes |

### Recipe Viewer Compatibility

The following optional integrations are supported:

- **JEI** — View all custom recipes (tobacco workbench, drying rack, grinder, Create mechanical)
- **REI** — View custom recipe categories
- **Jade** — View drying rack leaf count, drying progress, grinder/workbench info

All are optional and not required.

## Commands

For development/testing only (requires OP):

```
/tobacco find   — Locate and teleport to nearest tobacco villager or workbench
/tobacco spawn  — Spawn a tobacco villager and workbench at your location
```

## License

MIT License.

## Disclaimer

This mod does **not** encourage real-life smoking.

**Smoking is harmful to your health.**

The lung cancer and treatment content in this mod is gameplay fiction, not medical advice. If you have health concerns, please consult a doctor.
