# TECH_STACK.md - Hust Adventure (HustGame)

Technical specs, library versions, build structures, and architecture of Hust Adventure (HustGame).

---

## 1. Core Language & Runtime
* **Language**: Java 21 (`java.sourceCompatibility = 21` in root Gradle, `java.targetCompatibility = 21` in `lwjgl3` module).
* **JDK**: Temurin 21 (Adoptium OpenJDK 21).
* **Garbage Collection (GC) Optimization**:
  * **Object Pooling**: Avoid `new` in `update()` and `render()`. Reuse objects (e.g., `ExpGem` implements `Pool.Poolable`, `Projectile` inherits `MapObject` and managed via `GamePools` or freed by `EntityFactory.freeEntity()`). Collider of pooled entities reused to maintain Zero Allocation. Character states are reused via `EntityState` constants (`IDLE`, `MOVING`, `DEAD`) to eliminate state-transition allocations.
  * **Native Memory & Static Resources**: Release non-JVM heap resources (`Texture`, `Shader`, `SpriteBatch`, `ShapeRenderer`, `TiledMap`, `RayHandler`, `World`) by implementing `Disposable` and calling `dispose()`.
    * Reuse `ShapeRenderer` in `ScreenTransition`: Inherits `BaseScreen`, uses global `ShapeRenderer` shared by `HustGame` to avoid extra native allocation.
    * Intermediate Loading Transition (`LevelLoadingScreen`): Prevents synchronous map loading block hitches by rendering a `"Đang tải bản đồ..."` UI frame to the screen prior to loading level assets.
    * Clean static fields holding native resources (e.g., `bulletTexture` in `Projectile`, `whitePixel` in `ShapeDrawUtils`) using static `disposeStatic()` or `disposeStaticResources()` on game shutdown.
  * **Zero-Allocation UI rendering**: Avoid string manipulation in rendering path. UI components (e.g., `DebugUI`, `SimonPuzzle`, `MemoryCardPuzzle`, `SpeedMathPuzzle`) must use class-level shared `StringBuilder` (cleared via `setLength(0)`) and custom non-allocating float formatter (e.g. `appendFloat()`) instead of `String.format()` or `+` operator. `BitmapFont.draw()` draws `CharSequence` directly.

---

## 2. Build Automation
* **Build Tool**: Gradle.
* **Gradle Wrapper**: `9.4.0`.
* **Core Tasks**:
  * Build: `./gradlew build`
  * Run Desktop: `./gradlew lwjgl3:run` (Note: Desktop launcher is in `lwjgl3` module, not `desktop`).
  * Clean: `./gradlew clean`
* **Build Plugins**:
  * Lombok (`io.freefair.lombok` version `9.5.0`): Annotations like `@Builder` for code generation and boilerplate reduction.

---

## 3. Core Game Framework
* **Framework**: libGDX `1.14.0` (`gdxVersion` in `gradle.properties`).
* **Graphics Backend**: LWJGL3 (Desktop).
* **Core Libraries**:
  * `gdx` (`1.14.0`): Game logic, screens, utilities.
  * `gdx-backend-lwjgl3` (`1.14.0`): Desktop environment launcher.
  * `gdx-box2d` (`1.14.0`): Physics bindings used only for `RayHandler` lighting (not entity collisions).
  * `gdx-freetype` (`1.14.0`): Generates TrueType font (`.ttf`) supporting Vietnamese Unicode.
  * `gdx-ai` (`1.8.2`): *Not used*. Project uses custom AI and State Machine (`AIBehavior`, `EntityState`).
  * `box2dlights` (`1.5`): Dynamic 2D lighting and shadows (`PointLight`, `RayHandler`).

* **Audio & Music**:
  * Music is loaded as `com.badlogic.gdx.audio.Music` (for streaming background tracks).
  * SFX is loaded as `com.badlogic.gdx.audio.Sound` (for short, low-latency sound effects).
  * Resources are organized in subdirectories: `assets/audio/music/` and `assets/audio/sfx/` (categorized under `enemy/`, `interact/`, `player/`, and `puzzle_boss/`).

> [!NOTE]
> No third-party ECS libraries (e.g., Ashley) or UI frameworks (e.g., VisUI) are used. UI uses vanilla Scene2D (`Stage`, `Table`, `Actor`) and custom components.

---

## 4. Architectural Patterns

* **GameObject Base Identity Design**: `GameObject` is the root abstract class containing identity details (`id`, `name`, `spritePath`). Both world entities (`MapObject` -> `Character` -> `Player`/`Enemy`) and logical items (`Item` -> `BaseWeapon`/`Gear` implementing `Equipable`) inherit `GameObject`.
* **Composition Pattern & DI/DIP**:
  * Entities use interchangeable `MovementBehavior`. `AIBehavior` inherits `MovementBehavior`.
  * `Enemy` does not shadow parent `Character.movementBehavior`; it uses the parent field polymorphically.
  * `Enemy` receives dependencies (`Player`, `EntityManager`) via constructor-based DI during instantiation (provided by `EntityFactoryImpl`), avoiding per-frame argument passing.
  * Game loop in `PlayScreen.render()` updates entities through `EntityManager` without manual `instanceof Enemy` checks.
  * Enemy behaviors (`AIBehavior` for movement, `AttackBehavior` for attacks, `DeathBehavior` for death) injected via `EnemyBehaviors` container.
  * Core managers/catalogs/builders (`UpgradeCatalog`, `LevelUpChoiceBuilder`, `DebugOptionRegistry`, `LevelFactory`, `ItemManager`) are instantiated as instances in `HustGame` and injected via constructor dependency injection (DI), eliminating static mutable state. Clients depend on the `GameProgressContext` interface rather than the concrete `ProgressContext` implementation.
  * **Dynamic Combat Gating**: Auto-attacks and manual attacks are restricted during non-combat phases (e.g., Floor 1, Library puzzles, final Boss Room Q&A) via the abstract `isAutoAttackAllowed()` method on `LevelBehavior`. This capability is propagated to the domain layer (`Player` and `BaseWeapon`) through the `GameProgressContext` interface to ensure zero coupling between entities and specific screen/level behaviors.
* **Clean & Layered Architecture**:
  * Domain-level classes do not depend on framework/presentation APIs.
  * Input reading is decoupled via the pure Java `PlayerController` domain interface, isolating GDX input details to `InputReader` inside the presentation/framework package `hust.adventure.input`.
  * Lighting is decoupled via the pure Java `LightProvider` and `GameLight` domain interfaces, isolating `box2dlights` GDX details (e.g., `PointLight` and wrapper `PointLightWrapper`) to `LightingManager`.
* **Zero Hardcoding in Debug Menu, Transitions & Map Assets**:
  * `DebugOptionRegistry` and level behaviors (`LabBehavior`, `OutsideBehavior`, `LibraryBehavior`) are data-driven. Map, item, enemy lists in debug menu are queried dynamically from loaders (`WeaponDataLoader`, `GearDataLoader`, `ItemDataLoader`, `EnemyDataLoader`, `LevelDataLoader`).
  * `GameAssetManager` loads map assets and level background music (BGM) dynamically based on the configuration inside `levels.json`, removing hardcoded file paths.
  * Level behaviors (`LibraryBehavior`, `LabBehavior`) do not hardcode transition targets; they query target `LevelConfig` fields from `LevelDataLoader` to dispatch coordinates and map paths dynamically.
  * Level behaviors spawn entities using literal JSON IDs (e.g., `"null_pointer"`) instead of local string constants.
  * `DebugOptionRegistry` does not access the player via global static state. `Player` and factories (`WeaponFactory`, `GearFactory`) are injected into the constructor of `DebugInputHandler`.
* **Data-Driven Enemy Design**: All enemies (including bosses) are defined in `assets/configs/enemies.json`. `EnemyDataLoader` loads JSON into `EnemyConfig`; `EntityFactoryImpl` passes config to concrete `Enemy` class. No individual subclass for enemies exists in Java.
* **Data-Driven Item Design**: Items defined in `assets/configs/items.json`. `ItemDataLoader` parses JSON using `JsonReader` + `JsonValue` (polymorphic deserialization to distinguish `"base"` and `"consumable"` via `type`). `ItemFactory` instantiates items using `EffectProvider` registry (`ObjectMap<String, EffectProvider>`) instead of `switch-case`. `ItemManager` is an instantiable registry class, injected via DI.
* **Data-Driven Gear Design**: All 6 passive gear types defined in `assets/configs/gears.json`. Configuration mapping `onEquipEffect` and `onEquipValue` is registered via `GearEffectApplier` (`ObjectMap<String, GearEffectApplier>`) in `GearFactory` to modify multipliers on `Player` (Max HP, power, cooldown, area, magnet, speed). No hardcoded ID checks in `Player` getters. `PlayerPersistenceService` restores accumulated gear effects on level transition.
* **Data-Driven Weapon Design**: Weapons configured in `assets/configs/weapons.json`. `WeaponDataLoader` parses JSON config (`WeaponConfigCatalog`, `WeaponConfig`, `WeaponLevelConfig`). `WeaponFactory` instantiates weapons using registry `WeaponProvider` (`ObjectMap<String, WeaponProvider>`) instead of `switch-case`. Upgrade logic is centralized in `BaseWeapon.upgrade()`.
* **Structured Character Assets**: Character sprites (such as the Player) are structured under `assets/character/` using the `atlas.json` and `atlas.png` texture atlas specification. The asset manager loads the single `atlas.png` texture, and the entity class parses coordinates from `atlas.json` to extract rotations and walk animations dynamically.
* **State Pattern**: Used for play mode (`PlayMode` enum: `RUNNING`, `IN_UI`, `PAUSED`) and entity state via `EntityState` interface (`IdleState`, `MovingState`, `DeadState`). States are shared via static constants (`EntityState.IDLE`, `EntityState.MOVING`, `EntityState.DEAD`) to prevent Garbage Collection allocations during gameplay. Circular dependencies between concrete state classes (e.g., `PlayingGameState`) and `ProgressContext` are eliminated by having states depend strictly on the `GameProgressContext` interface. The default state supplier is registered statically on startup in `HustGame`.
* **SSOT via Dependency Injection**: `PlayerStats` is the Single Source of Truth for HP and stamina. `ProgressContext` owns `PlayerStats` and implements `GameProgressContext`. `Player` constructor receives `PlayerStats` (`new Player(..., stats)`) and stores it in `private final PlayerStats stats`. All HP/stamina access delegates to `stats`. `EntityFactoryImpl` injects `PlayerStats` from the injected `GameProgressContext` into `Player`.

* **Single Source of Truth for Entity Creation**: `EntityFactoryImpl` is the sole entry point for memory allocation and initialization of all game entities (`Player`, `Enemy`, `Projectile`, `ItemDrop`, `ExpGem`, `FloatingBook`, `Candle`, `StaticObject`). Using `new` directly in logic or level behavior code is prohibited.
* **Observer Pattern (Event System)**: `EventDispatcher` (Singleton) and `EventListener` handle asynchronous communication to achieve loose coupling. Events include `MAP_TRANSITION`, `LEVEL_UP`, `PLAYER_DIED`, `INVENTORY_OPENED`, `INVENTORY_CLOSED`, `INVENTORY_CHANGED`, `ITEM_DROPPED`.
* **UI DTO Pattern**: UI components (`HUD`, `StatusEffectsHUD`, `InventoryUI`, `LevelUpUI`) do not reference `ProgressContext`, sub-contexts, `Player`, or game models. Data passed via DTOs (`HUDData`, `StatusEffectsData`, `InventoryUIData`, `LevelUpUIData`). Callback `Consumer<Integer>` processes choices in `LevelUpUI`.
* **Wave Survival & Transition Guard**: Level behaviors that require survival (such as `OutsideBehavior` and `Floor1Behavior`) utilize `WaveManager` to spawn wave configuration entries and guard transitions by overriding `canTransition()`, which checks `MapDirector.canTransition()` and `EntityManager.hasActiveEnemies()`.
* **Upgrade & Passive Stats System**:
  * Weapons (levels 1-5) and 6 gear types are upgraded dynamically based on `weapons.json` and `gears.json` configs loaded into `UpgradeCatalog`.
  * Level-up choices built via `LevelUpChoiceBuilder` using ID lists from data loaders.
  * Stats applied as multipliers (Power, Cooldown, Area, Speed, Magnet, Max HP) on `Player`.
  * Saturated damage calculation uses `BaseWeapon.getEffectiveDamage()` which multiplies base damage by owner power multiplier (`owner.getPowerMultiplier()`) and global `damageMultiplier` from the player's `progressContext`.
  * Weapon/Gear states saved in `ProgressContext` (`PlayerStats`) and restored via `PlayerPersistenceService.restore()` at `PlayScreen.show()` and inside `Player` constructor. HP/stamina bypass persistence service and sync automatically via shared `PlayerStats`.
* **Primitive Rendering Separation & Graphics Isolation**: Domain classes (`MapObject`, `BaseWeapon`, `Enemy`, `ExpGem`, `GarlicAuraWeapon`, `WhipWeapon`) must not contain or call shape rendering code (e.g., `ShapeDrawUtils` or `ShapeRenderer`). Shape rendering (including Garlic Aura radius and Whip hit flashing) is centralized in `GameWeaponRenderer` (implementing `WeaponEffectVisitor` from domain) in `hust.adventure.graphics` to isolate graphics dependencies. Fallback geometries for sprite-less objects use `getShapeFallbackColor()` on `MapObject` to render shape outlines polymorphically. Visual overlays for telegraphed enemy behaviors (e.g., charging warnings) are exposed through domain capability interfaces (e.g., `TelegraphedBehavior`) and rendered polymorphically by `GameRenderer` using `ShapeDrawUtils.drawLine` in the sprite batch pass for zero-allocation performance.
* **Decoupling Camera from UI**: UI components do not instantiate or own `OrthographicCamera`. A central camera (`uiCam`) in `GameRenderer` applies its projection matrix to `SpriteBatch` and `ShapeRenderer` once before drawing the entire UI.
* **Native Memory Management**: Asset loading centralized in `GameAssetManager` (wraps `AssetManager`). Release resources via `Disposable` in key systems (`PlayScreen`, `EntityManager`, `WorldManager`, `LightingManager`).
* **Coordinates & Collision**:
  * World units in **pixels** (no Box2D meter scale).
  * Custom collision detection via **Spatial Hashing** and **Bitmask Collision Matrix** in `CollisionManager`.
    * *Decoupled Collision & Walls*: The collision system (`Collider`, `CollisionManager`) operates on a lightweight `Collidable` interface instead of the heavy `MapObject` class. This allows static walls (`WallEntity`) to implement `Collidable` directly and bypass the overhead of `MapObject` (such as FSM, sprite rendering, and position updating) while remaining fully integrated into the collision checking engine.
    * *Decoupled Hitbox & Sprite*: Visual sprite dimensions (`width`, `height`) are decoupled from physical hitbox/collision boundaries (`hitboxWidth`, `hitboxHeight`). Collision detection, boundary checks, and spatial grid hashing use hitbox sizes, allowing fine-grained collision boundaries (e.g. 32x32 player hitbox on 50x50 sprite). Defaults to sprite size if hitbox size is not specified.
    * *Static vs Dynamic Grid*: `staticGrid` (walls, hashed once during map load) is separate from `dynamicGrid` (moving entities, rebuilt every frame) to optimize performance.
    * *Zero Allocation*: Grid cell arrays (`Array<Collider>`) are pooled.
    * *Wall Collision Checks*: All Enemy entities respect `CollisionManager.canMove()` wall collision checks during position updates (`setX`/`setY`) to prevent walking through terrain/obstacles. The `checkWallCollisions` flag allows selectively bypassing checks during spawn initialization or boundary clamping.
  * **Tiled Map Loading (`WorldManager`)**:
    * Finds collision layers via map properties (`collisionLayer`) or layer properties (`collision`/`isCollision`). Fallback order: `"collision"`, `"Border"`, `"Object Layer 1"`.
    * Classifies map layers (background vs foreground) via `classifyLayers()`.
    * Robustly resolves Player spawn point from layer configured via map property `spawnLayer`, layer property `isSpawn`, or exact fallback names (`Spawn`/`spawn`). Flexibly supports extracting coordinates from any `MapObject` (including `RectangleMapObject` and point-based objects), resolving to their center point (both center-X and center-Y) to align with player hitbox origins and ensure fault-tolerant map transitions.
    * **Dynamic Depth Sorting & Transitive Grouping**: Assigns `zIndex` based on layer definition index and `subZIndex` based on object XML definition order in the TMX file. Transitive connected-components grouping merges overlapping bounding boxes (e.g., Desk -> Monitor -> Monitor Screen) and aligns their sorting bottom-Y baseline, breaking rendering ties using `zIndex` and `subZIndex` to match Tiled maps rendering behavior.
  * Default viewport: $800 \times 600$ pixels.
  * Default entity size: $32 \times 32$ pixels.

---

## 5. Testing & Profiling
* **Frameworks**: JUnit 5 (`junit-jupiter`) and Mockito (`mockito-core`, `mockito-junit-jupiter`) defined in `core/build.gradle`.
* **Coverage**: Tests implemented for `PlayerStats`, `LootDropService`, `CollisionManager`, `EventDispatcher`, `PlayerPersistenceService`, `EntityFactoryImpl`, and `WaveManager` under `core/src/test/java/`.
