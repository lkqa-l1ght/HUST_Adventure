# TMX Map Design Rules & Standards

This document defines the naming conventions, layer structure, and properties that all TMX maps must adhere to for successful loading in the game engine.

---

## 1. Map Properties

Every map configuration in Tiled can specify custom properties to override default system behavior.

| Property | Type | Description |
| :--- | :--- | :--- |
| `collisionLayer` | `string` | Custom collision layer name. Overrides fallback settings. |
| `spawnLayer` | `string` | Custom spawn point layer name (default: `Spawn`). |
| `zoom` | `float` | Custom camera zoom multiplier for this level. |

---

## 2. Layer Naming & Properties

Layer rendering and collision behaviors are determined by their naming conventions and properties.

### 2.1 Background & Foreground Classification

By default, layers are classified as **background** or **foreground** to determine rendering order (e.g., drawing behind or in front of the player).

* **Explicit Override**: Add custom property `isBackground` (`boolean` or `string` `"true"`/`"1"`) on any layer to force background classification.
* **Name-Based Default**: If no explicit property exists, the engine classifies layers by matching their names (case-insensitive) against configured background names:
  * `via he`, `duong`, `grass`, `nha1`, `background`, `floor`, `tile layer 1`

### 2.2 Collision Layer Rules

Solid physical bounds (walls, borders, barriers) must be placed in a collision layer containing rectangle map objects.

* **Explicit Override**: Mark any layer with a property `collision` or `isCollision` set to `true` (or `"true"`/`"1"`).
* **Fallback Scan**: If no layer has explicit collision properties, the engine checks for default named layers in order:
  1. `collision`
  2. `Border`
  3. `Object Layer 1`

> [!WARNING]
> Ensure only rectangle shape objects are used inside collision layers. Other shape types are ignored by the physics parser.

---

## 3. Object Layer Rules

Specific object layers are processed by specialized parsers registered in `WorldManager`.

### 3.1 Portal Layer (`Portals`)

Contains transition trigger areas that teleport the player to other levels.

* **Layer Name**: `Portals` (Configured in `map_config.json`).
* **Object Type**: `RectangleMapObject`.
* **Required Properties**:
  * `target` (`string`): The destination level ID matching an entry in `levels.json` (e.g. `LIBRARY`, `TANG_1`).
  * `spawnX` (`float`): X coordinate in pixels where the player will spawn in the target map.
  * `spawnY` (`float`): Y coordinate in pixels where the player will spawn in the target map.

### 3.2 Spawn Layer (`Spawn`)

Defines the initial entry position of the player on the map.

* **Layer Name**: `Spawn` (Explicitly marked layer with `isSpawn` property also accepted).
* **Object Type**: `RectangleMapObject`.
* **Behavior**: The engine uses the center-X and top-Y coordinates of the first rectangle object as the player spawn point.

### 3.3 Lighting Objects Layer (`LightingObjects`)

Defines static decorative items that emit visual lighting effects in dark rooms.

* **Layer Name**: `LightingObjects`.
* **Required Object Name**:
  * `Book` - Spawns a floating, illuminated book.
  * `Candle` - Spawns a lit candle object.
* **Properties**: Must contain `x` and `y` properties specifying the pixel coordinate anchors.
