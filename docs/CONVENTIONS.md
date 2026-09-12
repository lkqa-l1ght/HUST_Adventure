# CONVENTIONS.md — Hust Adventure (HustGame)

> **Mandatory Guidelines.** All new code, refactoring, and maintenance must adhere to these rules. AI Agents and developers must review this document before writing code to prevent native memory leaks, garbage collection spikes, and OOP structure violations.

---

## Table of Contents
1. [Naming & Formatting Rules](#1-naming--formatting-rules)
2. [Memory & Resource Management](#2-memory--resource-management)
3. [Strict OOP & Design Guidelines](#3-strict-oop--design-guidelines)
4. [Lifecycle & Threading Rules](#4-lifecycle--threading-rules)
5. [Documentation Standards](#5-documentation-standards)
6. [Good & Bad Code Examples](#6-good--bad-code-examples)
7. [Exception Handling & Testing](#7-exception-handling--testing)

---

## 1. Naming & Formatting Rules

### 1.1 Identifier Rules
* **Class / Interface / Enum**: `UpperCamelCase` (e.g., `MapObject`, `EntityState`, `AIBehavior`).
* **Method**: `lowerCamelCase`, verb-based (e.g., `update()`, `takeDamage()`, `spawnEntity()`).
* **Instance Field**: `lowerCamelCase` (e.g., `currentHealth`, `movementSpeed`).
* **Constant (static final)**: `UPPER_SNAKE_CASE` (e.g., `MAX_HEALTH`, `TILE_SIZE`).
* **Render/Update Temp Variables**: Prefix with `tmp` (e.g., `tmpVector`, `tmpRect`).
* **Pool / Factory classes**: Suffix with `Pool` or `Factory` (e.g., `GamePools`, `EntityFactory`).
* **Screen classes**: Suffix with `Screen` (e.g., `PlayScreen`, `MenuScreen`).
* **Manager classes**: Suffix with `Manager` (e.g., `CollisionManager`, `WorldManager`).
* **Event enum / constant**: `UPPER_SNAKE_CASE` (e.g., `MAP_TRANSITION`, `LEVEL_UP`).

### 1.2 Package & File Structure
* Filename must match the public class exactly.
* Packages must represent functional hierarchy: `hust.adventure.core`, `hust.adventure.entities`, `hust.adventure.screens`, `hust.adventure.ui`, `hust.adventure.events`.
* Avoid generic names like `Manager2`, `Helper`, `Util`. Classes must have distinct scopes of responsibility.

### 1.3 Formatting
* **Encoding**: UTF-8.
* **Indentation**: 4 spaces (no tabs).
* **Max Line Length**: 120 characters.
* **Braces**: K&R style (opening brace on the same line).
* **Imports**: No wildcard imports (e.g., do NOT use `import com.badlogic.gdx.*`). Import classes individually.
* **Fully Qualified Class Names**: Forbidden inside code for declarations or static access (e.g., do NOT use `hust.adventure.core.GameAssetManager` directly in code body). Declare `import` at the top.
* **Blank lines**: One blank line between methods; two blank lines between major logical sections.

```java
// CORRECT
public void update(float delta) {
    if (isDead()) {
        handleDeath();
        return;
    }
    movementBehavior.update(this, delta);
}

// INCORRECT
public void update(float delta)
{
    if(isDead()) { handleDeath(); return; }
    movementBehavior.update(this,delta);
}
```

---

## 2. Memory & Resource Management

### 2.1 Zero Allocation in Game Loop
> **STRICTLY PROHIBITED**: Do not instantiate (`new`) complex objects inside `update(float delta)` or `render(float delta)` methods.

Reuse objects (e.g., temporary vectors, rectangles) as class fields or extract them from pools:

```java
// CORRECT — instantiated once, reused in loops
private final Vector2 tmpVelocity = new Vector2();

public void update(float delta) {
    tmpVelocity.set(velocityX, velocityY).scl(delta);
    setX(getX() + tmpVelocity.x);
    setY(getY() + tmpVelocity.y);
}

// INCORRECT — heap allocation triggers Garbage Collection (GC) in loop
public void update(float delta) {
    Vector2 velocity = new Vector2(velocityX, velocityY); // GC trigger!
    setX(getX() + velocity.scl(delta).x);
}
```

* **STRICTLY PROHIBITED**: Do not use `String.format()` or string concatenation (`+`) inside `render()` or loop updates (e.g., debug overlays, UI text). Use class-level `StringBuilder` (reset via `setLength(0)`) and custom non-allocating float formatters (e.g., `appendFloat()`). libGDX `BitmapFont.draw()` supports `StringBuilder` directly.

### 2.2 Object Pooling
* High-turnover entities (`Projectile`, `ExpGem`, `DamageText`) **must** be managed using object pools via `GamePools`.
* **Dynamic Pool Registration**: To avoid compile-time dependencies from core logic to UI/presentation objects (like `DamageText`), pools for presentation objects must be registered dynamically on startup (e.g., in `HustGame.java`) using `GamePools.registerPool(Class<T> type, DefaultPool.PoolSupplier<T> supplier)`.
* Acquire: `GamePools.obtain(Class<T> type)`.
* Release: `GamePools.free(entity)` (or via factory `freeEntity(MapObject)`).
* Pooled classes must implement `com.badlogic.gdx.utils.Pool.Poolable` and override `reset()`.
* `reset()` must clear all internal states to initial values to prevent memory leaks or logic bugs.
* **Collider Reuse**: Do not allocate a new `Collider` when reusing pooled entities. Check `getCollider() == null` for lazy instantiation; otherwise, reuse and reconfigure.
* **Asset Decoupling**: Avoid static texture fields or static loaders inside pooled/dynamic objects (e.g. `Projectile`, `ItemDrop`). Instead, inject pre-loaded textures via `GameAssetManager` through the constructors or `init` methods.
* **Dynamic Map Loading**: Maps and level BGMs must not be hardcoded in `GameAssetManager`. They must be loaded dynamically by passing `LevelDataLoader` to `GameAssetManager.loadAllAssets()`, using `levels.json` as the Single Source of Truth.

```java
public class Projectile extends MapObject implements Pool.Poolable {
    private float vx, vy;
    private float damage;
    private Color color;
    private Texture texture;

    @Override
    public void reset() {
        setDestroyed(false);
        vx = 0;
        vy = 0;
        damage = 0;
        color = Color.WHITE;
        texture = null;
        if (getCollider() != null) {
            getCollider().setListener(null); // Clear listeners to avoid reference leaks
        }
    }
}
```

### 2.3 Native Resource Management (Disposable)
All classes encapsulating native (non-JVM heap) resources **must** implement `com.badlogic.gdx.utils.Disposable`.

| Resource Type | libGDX / box2dLight Classes | Disposal Location |
|---|---|---|
| Textures | `Texture`, `TextureAtlas` | Managed by `GameAssetManager` |
| Fonts | `BitmapFont` (from `FreeTypeFontGenerator`) | Generator must be disposed immediately after compiling the font |
| Shaders | `ShaderProgram` | Owning Screen or Renderer |
| Batches | `SpriteBatch`, `ShapeRenderer` | `PlayScreen.dispose()` or initiating method |
| Maps | `TiledMap` | **DO NOT** dispose in `WorldManager`. Managed by `GameAssetManager` |
| Lights | `RayHandler`, `PointLight` | `LightingManager.dispose()` |
| Physics | `World` (Box2D) | `LightingManager.dispose()` |

* **Rules**:
  1. Call `dispose()` in reverse order of instantiation.
  2. Nullify references after disposal to prevent double-free errors.
  3. Do not call `dispose()` on resources tracked by `GameAssetManager`.
  4. Screens/UIs extending `BaseScreen` must use the shared `shapeRenderer` from `HustGame` (`HustGame.getShapeRenderer()`). Do not instantiate a separate `ShapeRenderer` or dispose of the shared instance.

### 2.4 Static Resources
Static fields referencing native resources (e.g., `whitePixel` in `HUD`/`ShapeDrawUtils`) **must** provide a static `disposeStatic()` or `disposeStaticResources()` cleanup method, invoked during game shutdown at `HustGame.dispose()`:

```java
public class HUD {
    private static Texture whitePixel;

    public static void disposeStatic() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }
}
```

---

## 3. Strict OOP & Design Guidelines

### 3.1 Composition over Inheritance
* **Behavior Composition**: Separate movement and AI logic into independent components. `AIBehavior` inherits `MovementBehavior` to standardize character movement (e.g., `ChaseBehavior`, `WanderAIBehavior`).
* **Inheritance Depth Limits**:
  * Entities: Max 4 levels (e.g., `GameObject → MapObject → Character → Player`/`Enemy`).
  * Items/Gear: Max 3 levels (e.g., `GameObject → Item → BaseWeapon`/`Gear`).
* Do not inherit classes solely for utility sharing. Use static utilities or delegation.
* Only Mobs inherit from `Enemy` to obtain AI attributes, collision damage metrics, and debug status.
* **Decoupled Static Walls**: Static walls (`WallEntity`) must implement `Collidable` directly instead of inheriting from `MapObject`, avoiding state-machine, sprite, or update loop overhead.
* **`Collider` Abstraction**: `Collider` must hold and check bounds using `Collidable` references instead of concrete `MapObject` instances to cleanly support non-actor world objects.
* **Graphics Isolation & Renderer Responsibility**: Domain classes (entities, weapons, and items like `Enemy`, `ExpGem`, `GarlicAuraWeapon`, `WhipWeapon`) must not import or call graphics drawing helpers like `ShapeDrawUtils` or `ShapeRenderer`. All shape rendering must be handled within the presentation layer. Weapon visual effects are rendered using the **Visitor Pattern** via `WeaponEffectVisitor` and implemented by `GameWeaponRenderer` in the presentation layer.
* **Audio & Sound Decoupling**: Domain classes must not load or play audio files directly. Sound effects and music must be triggered by dispatching semantic events (`EXP_GAINED`, `ITEM_PICKED_UP`, `PUZZLE_SOLVED`, `PLAY_SFX`) via `EventDispatcher`. `AudioManager` in the core assets package is the single source of authority that listens to these events, maps them to physical audio asset paths (e.g. `.mp3`/`.wav` files in structured subdirectories), and handles the actual playback.
* **Shape Fallback Render Hints**: Entities without sprite assets (e.g., `ExpGem`, sprite-less enemies) must declare a fallback shape color by overriding `getShapeFallbackColor()` on `MapObject`, returning a GDX `Color` (or `null` if they render via `draw()`). The `GameRenderer` uses this color value to draw fallback geometries without using `instanceof` checks.
* **Data-Driven Mobs**: All enemy specifications (including bosses) are loaded from `enemies.json` via `EnemyConfig`. Do not write distinct Java subclasses for specific enemies.
* **Data-Driven Items**: Defined in `items.json`. `ItemDataLoader` executes polymorphic deserialization via `JsonReader` + `JsonValue` (distinguishing `"base"` and `"consumable"` via `type`). `ItemFactory` compiles items using `EffectProvider` registry (`ObjectMap<String, EffectProvider>`) instead of a `switch-case`. `ItemManager` is a pure registry.
* **Data-Driven Gears**: Defined in `gears.json`. `GearFactory` applies passive bonuses to `Player` stats multipliers via `GearEffectApplier` registry (`ObjectMap<String, GearEffectApplier>`). Avoid hardcoded ID loops in `Player` getters. `PlayerPersistenceService` restores gear level effects.
* **Data-Driven Weapons**: Configured in `weapons.json`. `WeaponFactory` maps weapons using `WeaponProvider` registry (`ObjectMap<String, WeaponProvider>`) instead of a `switch-case`. Centralized level changes are processed in `BaseWeapon.upgrade()`.
* **Structured Character Assets**: Player and character sprites are structured under `assets/character/` using a texture atlas (`atlas.json`, `atlas.png`). The asset manager loads the texture atlas, and classes parse the region coordinates dynamically from `atlas.json`. This avoids loading multiple small texture files and keeps asset loading consolidated.
* **SRP Behavior Segregation**: `AIBehavior` (movement), `AttackBehavior` (attacks), and `DeathBehavior` (death logic) are injected via `BehaviorRegistry` into `Enemy`. `Enemy` does not shadow parent `Character.movementBehavior`.
* **Decoupled Visual Overlays via Capability Interfaces**: Visual indicators or telegraph overlays for behavior logic must be decoupled from core entity models (e.g., `Enemy`). Expose behavioral overlay properties (such as coordinates, active flags, and timers) through domain capability interfaces (e.g., `TelegraphedBehavior` extending `AIBehavior`). The presentation layer (`GameRenderer`) can check for this capability interface on the active behavior to render overlays polymorphically without polluting the entity class or using concrete class casting.
* **Mob Speed Control**: Mobs retrieve speed dynamically using `enemy.getSpeed()`. Do not store speed parameters in AI constructors.
* **Mob Wall Collisions**: Mob coordinates are updated via `setX` and `setY`, which automatically perform wall collision checks using `CollisionManager.canMove()` (when `checkWallCollisions` is enabled) to prevent enemies from passing through terrain, ensuring unified and simple collision enforcement.
* **Weapon Damage Output**: All weapon classes must calculate damage using `getEffectiveDamage()` instead of `getBaseDamage()` to automatically integrate player power multipliers and global multipliers.
* **Zero Hardcoding in Debug Menu & Transitions**: Options are loaded dynamically. Spawn scripts must use literal string configuration IDs matching JSON configurations (e.g., `"null_pointer"`). Level behaviors and Debug transition commands must not hardcode target map paths or spawn coordinates for transitions; they must look up target `LevelConfig` objects from `LevelDataLoader` (Single Source of Truth) and use their configured default coordinates as the fallback, so that both debug and normal map transitions share the same spawn resolution logic.
* **Decoupled Hitbox & Sprite**: `MapObject` separates visual sprite dimensions (`width`, `height`) and physical collision boundaries (`hitboxWidth`, `hitboxHeight`). Always use `getHitboxWidth()` and `getHitboxHeight()` for collision detection, movement boundaries, and spatial hashing, keeping `width` and `height` solely for sprite drawing.
* **Wave Survival Levels Transition Lock**: Wave-based levels (e.g., maps using `WaveManager`) must override `canTransition()` to check `waveManager.isFinished()` and confirm no active enemies remain using `EntityManager.hasActiveEnemies()`, preventing premature level transitions while waves are active.
* **Decoupled Combat Restrictions**: Auto-attacks and manual attacks are restricted during non-combat phases (e.g., Floor 1, Library puzzles, final Boss Room Q&A) via the abstract `isAutoAttackAllowed()` method on `LevelBehavior`. This capability is propagated to the domain layer (`Player` and `BaseWeapon`) through the `GameProgressContext` interface to ensure zero coupling between entities and specific screen/level behaviors.
* **Intermediate Loading Screen**: Map transitions (e.g., portal stepping), game start, and game over restarts must be routed through `LevelLoadingScreen` to ensure the screen flushes rendering before blocking synchronous asset loads.
* **Settings & Guide Screens**: The Settings button is removed from the game menu. A Hướng dẫn (Guide) button is used instead to overlay gameplay controls.

### 3.2 State Pattern
* Character states (`MovingState`, `IdleState`, `DeadState`) must implement the `EntityState` interface.
* Character states should be reused via the static constants in `EntityState` (`EntityState.IDLE`, `EntityState.MOVING`, `EntityState.DEAD`) to eliminate Garbage Collection allocations inside frame updates.
* State transitions are processed via `MapObject.setState(final EntityState newState)`.
* State transitions must guard against self-transitions (e.g. `this.state == newState`) to prevent redundant exit/enter updates.
* State methods receive the calling entity reference:
  * `void enter(MapObject entity);`
  * `void exit(MapObject entity);`
  * `void update(MapObject entity, float delta);`

### 3.3 Observer / Event System
* Decoupled system communication must go through `EventDispatcher` (Singleton). Never trigger direct sibling class notifications.
* Main loops must not poll states (e.g., checking HP for GameOver). Trigger reactions using event listeners (e.g., `PLAYER_DIED`, `INVENTORY_OPENED`).
* Events are indexed via `EventType` enum and wrapped in `GameEvent`.
* Listeners **must** invoke `removeListener()` in their `dispose()` method to prevent reference memory leaks.
* UI components receive DTO packages from orchestrators (`PlayScreen`, `UIManager`) rather than listening directly to raw data streams.

```java
// Constructor
EventDispatcher.getInstance().addListener(EventType.LEVEL_UP, this);

// Disposal
@Override
public void dispose() {
    EventDispatcher.getInstance().removeListener(EventType.LEVEL_UP, this);
}
```

### 3.4 Singletons
* Only `EventDispatcher` and `GameAssetManager` may exist as Singletons.
* Do not declare new static Singletons. Inject dependencies through constructor DI.
* Do not reference global state statically. Pass variables as method arguments or inject them.
* Do not use static mutable fields or static classes to store game configurations, managers, factories, or catalog choices (e.g., `UpgradeCatalog`, `LevelUpChoiceBuilder`, `DebugOptionRegistry`, `LevelFactory`, `ItemManager`). Instead, instantiate them inside `HustGame` and inject them via constructor dependency injection (DI). Classes should depend on the `GameProgressContext` interface rather than concrete `ProgressContext` to allow decoupled state management.
* **Injectable Configurations**: Avoid loading configuration files statically inside class constructors (e.g., using `MapConfigLoader.load()`). Overload or design constructors to accept config instances (e.g., `MapConfig`) to allow proper unit testing and configuration injection.
* **Clean and Layered Decoupling**: Domain classes must not import or depend on LibGDX graphics or framework UI/input APIs. Implementations must wrap GDX classes behind pure Java Domain interfaces.
  * **Input Decoupling**: Input reading details are wrapped by `PlayerController` interface. The implementation `InputReader` must reside in the presentation/framework package `hust.adventure.input` rather than domain packages.
  * **Lighting Decoupling**: Lighting details are decoupled via `LightProvider` and `GameLight` interfaces. Domain entities like `Candle` and `FloatingBook` must only use the `GameLight` abstraction to interact with lighting. Concrete libGDX `PointLight` implementation and wrappers (like `PointLightWrapper`) are isolated inside `LightingManager`.
  * **State Context Decoupling**: Game states and states (like `PlayingGameState`) must interact with the game progress context strictly via the `GameProgressContext` interface rather than concrete `ProgressContext`. Registering default states/suppliers (e.g., `PlayingGameState::new`) should be done statically on startup (e.g., in `HustGame.java`) to break cyclic dependency loops.

### 3.5 Single Responsibility Principle (SRP)
* `PlayScreen` only handles level life and mode transitions; delegates rendering to `GameRenderer`, and script behavior to `LevelBehavior`.
* `CollisionManager` only checks grid-based collision; does not apply damage directly.
* `DebugUI` only renders the overlay; delegates keys to `DebugInputHandler` and options to `DebugOptionRegistry`.
* UI components (`HUD`, `StatusEffectsHUD`, `InventoryUI`, `LevelUpUI`) must be decoupled from logic models. Pass data using DTO structures (`HUDData`, `StatusEffectsData`, `InventoryUIData`, `LevelUpUIData`).
* `MapObject` only handles coordinates, dimensions, bounds; no primitive drawing (delegated to `ShapeDrawUtils`).
* `WorldManager` only parses Tiled maps and classifies layers; no entity spawn logic.
* `ProgressContext` only directs global game states; delegates sub-contexts (`PlayerStats`, `DebugContext`, `SpellState`).
* **Stamina isolation**: Only `Player` has stamina. Cast `Character` to `Player` before performing stamina adjustments (e.g., in status effects or item consumption).
* **HP Override capability**: HP methods on `Character` are non-final, permitting the `Player` class to delegate HP updates to `PlayerStats` (SSOT). `Character.setHp()` handles clamping internally, while `Player.setHp()` delegates to `PlayerStats.setHp()`.
* **SSOT via Dependency Injection**: `PlayerStats` manages player HP/stamina states. Injected via constructor into `Player`. `PlayerPersistenceService` only manages weapon/gear persistence.

### 3.6 Encapsulation & Visibility
* All entity fields **must** be `private` or `protected`. Expose state via getter/setter.
* DTOs use `private final` fields with clean getters. No public mutable states.
* Setters must validate inputs before assigning. Getters must not contain side-effects.
* **Collection Encapsulation**: Any getters returning internal collections/lists from data objects or parser outputs (such as `MapParseResult`) must return unmodifiable wrappers (e.g., `Collections.unmodifiableList(...)`) to prevent external mutability bugs.

### 3.7 Factory Pattern
* All entities (`Player`, `Enemy`, `Projectile`, `ItemDrop`, `ExpGem`, `FloatingBook`, `Candle`, `StaticObject`) must be instantiated via `EntityFactory` interface and `EntityFactoryImpl`.
* Using `new` to instantiate entities outside `EntityFactoryImpl.java` is strictly prohibited.

### 3.8 Builder Pattern & Lombok
* Classes with 3+ constructor parameters or adjacent parameters of identical type (e.g., `boolean, boolean`) must use the **Builder Pattern**.
* Apply Lombok `@Builder` annotations directly to constructors to eliminate boilerplates.

### 3.9 Coordinate Conventions & Factory Methods
* Avoid overloaded constructors with ambiguous coordinate conventions (e.g., one expecting bottom-left and another expecting center). Instead, hide constructors under package-private/private visibility and expose descriptive public static factory methods (e.g., `StaticObject.fromBottomLeft(...)` and `StaticObject.fromCenter(...)`) to document the expected coordinate system.
* **Tiled Map Spawn Points**: Coordinate parsing of spawn objects (e.g. Point or Rectangle map objects on the spawn layer) must resolve coordinates to their center point (both center-X and center-Y) so that they directly align with the player hitbox's center coordinate convention. Rectangle coordinates must not be parsed using top/bottom edges as offsets.

### 3.10 Tiled Rendering & Depth-Sorting Order
* **Map Layer & Object Rendering Order**: Render order must be determined dynamically based on the layers and objects XML definitions in the TMX file (via `zIndex` and `subZIndex` fields) instead of relying on hardcoded config list order.
* **Transitive Connected-Components Overlap Grouping**: Any group of overlapping objects (e.g. Desk -> Monitor -> Monitor Screen) must have their `sortingY` coordinate aligned dynamically to the lowest bottom-Y baseline of the group. This allows the composite setup to Y-sort as a single unified obstacle relative to characters, while tie-breaking the drawing order using their relative `zIndex` and `subZIndex` values.
* **Rotation Convention**: TMX rotation is clockwise in degrees, whereas GDX `SpriteBatch.draw` expects counter-clockwise. To render correctly around the bottom-left corner of the object (Tiled's rotation origin), negate the rotation (`-rotation`) and pass `(0f, 0f)` as the drawing origin coordinates.
* **Map Layer Naming & Configuration**: Layer names in TMX must be accurate (`collision`, `Spawn`, `spawn`). Foreground or sorting-dependent decor layers (e.g., grass `co`, `nen`) must be explicitly declared in `map_config.json`'s `decorLayerNames` or `collisionFallbackLayerNames` to ensure correct rendering depth and logic initialization.
* **Tiled Map Tile Dimensions**: Static tile objects (e.g., chairs, desks) parsed from tile layers (`TiledMapTileLayer`) must be instantiated using their native `TextureRegion` dimensions (`region.getRegionWidth()` and `region.getRegionHeight()`) to prevent distortion/stretching caused by hardcoding their dimensions to the grid cell size (`tileWidth` / `tileHeight`).

---

## 4. Lifecycle & Threading Rules

### 4.1 Screen Lifecycle
Implement screen lifecycles completely:
```
create/show → resize → render (loop) → pause → resume → hide → dispose
```
Clamp delta times to prevent logic breaks during frame drops.

### 4.2 Threading Rules
* **All rendering, logic updates, and libGDX calls must execute on the GL Thread (main thread).**
* For background calculations (e.g., file loading, complex pathfinding), post results back to the GL Thread using `Gdx.app.postRunnable()`.
* Never invoke libGDX GL APIs from a background thread.

### 4.3 Game States
The `PlayMode` enum (`RUNNING`, `IN_UI`, `PAUSED`) gates game logic execution in `PlayScreen.render()`:

```java
@Override
public void render(float delta) {
    if (state == PlayMode.RUNNING && !game.getScreenTransition().isTransitioning()) {
        entityManager.update(delta, entityFactory);
        for (var e : entityManager.getEntities()) {
            if (e instanceof Enemy) {
                ((Enemy) e).handleUpdate(delta * progressContext.getEnemyTimeScale(), player, entityManager);
            }
        }
        lightingManager.update();
        collisionManager.update();
        checkTriggers();
        updateLevel(delta);
    }
    uiManager.update(delta, player);
    if (gameRenderer != null) {
        gameRenderer.render(delta, mapRenderer, backgroundLayers, foregroundLayers, player, shapeRenderer, font, lightingManager);
    }
}
```

### 4.4 Render Order
* `GameRenderer` structures drawing operations:
  1. Clear Screen/Buffers (`Gdx.gl.glClear`).
  2. Draw Infinite Background.
  3. Draw TiledMap Background Layers.
  4. Draw Y-Sorted Entities.
  5. Draw Foreground Layers (via Stencil Buffer) and Silhouette Shader.
  6. Draw Damage text floating numbers.
  7. Draw RayHandler shadows/lights.
  8. Draw UI Layer overlay (HUD, inventory, level up, debug panel).

> [!IMPORTANT]
> The UI (`HUD`, `StatusEffectsHUD`, `InventoryUI`, `LevelUpUI`, `DebugUI`, and the library mini-games like `SimonPuzzle`, `MemoryCardPuzzle`, `SpeedMathPuzzle` managed by `PuzzleSequencer`) is rendered manually via `SpriteBatch` and `ShapeRenderer` matching a single centralized `uiCam` in `GameRenderer`. Do not use Scene2D `Stage` for in-game HUD overlay or puzzle rendering (use manual SpriteBatch/ShapeRenderer for zero-allocation hot path). Scene2D `Stage` + `Table` is permitted for non-gameplay screens (e.g., MainMenuScreen, Hướng dẫn) where allocation is acceptable.

---

## 5. Documentation Standards

### 5.1 Javadoc
* Required for all public/protected classes, interfaces, methods, and non-obvious static final constants.

```java
/**
 * Short summary of the method.
 *
 * <p>Additional implementation details or constraints.</p>
 *
 * @param delta  Elapsed time since the last frame, in seconds.
 * @return       Description of the return value.
 */
```

### 5.2 Inline Comments
* Focus on explaining "why" instead of "what".
* Mark tasks with `// TODO: description + author`.
* Mark bugs with `// FIXME: bug details`.
* Mark critical code performance overrides with `// PERF: details`.

---

## 6. Good & Bad Code Examples

### 6.1 Native Resource Allocation
```java
// ✅ GOOD: Implements Disposable, disposes in reverse order
public class LightingManager implements LightProvider, Disposable {
    private final World world;
    private final RayHandler rayHandler;

    public LightingManager() {
        this.world = new World(new Vector2(0, 0), true);
        this.rayHandler = new RayHandler(world);
    }

    @Override
    public void dispose() {
        rayHandler.dispose();
        world.dispose();
    }
}

// ❌ BAD: Native resources are retained without cleanups
public class LightingManager {
    public World world;
    public RayHandler rayHandler;
    // Missing dispose() leads to native Box2D memory leaks
}
```

### 6.2 Object Pooling
```java
// ✅ GOOD: Utilizes central GamePools registration
public void spawnAmmo(float x, float y) {
    Projectile p = GamePools.obtain(Projectile.class);
    p.init(x, y, 100f, 0f, 10f, Color.RED, true);
    entityManager.addEntity(p);
}

public void destroyAmmo(Projectile p) {
    entityManager.removeEntity(p);
    GamePools.free(p);
}

// ❌ BAD: Instantiates directly inside loop, triggering GC pressure
public void spawnAmmo(float x, float y) {
    Projectile p = new Projectile(x, y, 100f, 0f, 10f, Color.RED, true); // Heap allocation spike!
    entityManager.addEntity(p);
}
```

### 6.3 Event Registration
```java
// ✅ GOOD: Registers listener, cleans up reference on dispose
public class PlayerEventHandler implements EventListener {
    private final Player player;

    public PlayerEventHandler(final Player player) {
        this.player = player;
        EventDispatcher.getInstance().addListener(EventType.PUZZLE_FAILED, this);
    }

    @Override
    public void onEvent(final GameEvent<?> event) {
        if (event.getType() == EventType.PUZZLE_FAILED) {
            final Float damage = (Float) event.getData();
            player.takeDamage(damage);
        }
    }

    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.PUZZLE_FAILED, this);
    }
}

// ❌ BAD: Erroneous event key, missing disposal cleanup leaks memory
public class Player extends Character implements EventListener {
    public Player(...) {
        EventDispatcher.getInstance().addListener(GameEvent.PUZZLE_FAILED, this); // Wrong enum key
    }

    public void onEvent(GameEvent event, Object data) { // Wrong method signature
        if (event == GameEvent.PUZZLE_FAILED) { ... }
    }
    // Missing dispose registration leaks this entity globally
}
```

---

## 7. Exception Handling & Testing

### 7.1 Exception Guidelines
* **Catch Specific Exceptions**: Do not catch `Exception` or `Throwable`. Intercept `SerializationException` or `GdxRuntimeException` directly.
* **Fail-Fast**: Raise a wrapped `GdxRuntimeException` immediately upon config load issues to halt invalid system executions.
* **Let Collision Failures Propagate**: Do not swallow exceptions in `CollisionManager` logic; let errors bubble to surface bugs quickly.

### 7.2 Testing Guidelines
* **Mock Gdx.app**: Mock `Gdx.app` via Mockito (`Gdx.app = mock(Application.class)`) in test initialization to prevent null pointers on log accesses.
* **Singleton Cleanup**: Call `EventDispatcher.resetInstance()` in `@BeforeEach` setup steps to isolate unit test states.
* **Bypass Native camera logic**: Avoid native libGDX dependency errors (like UnsatisfiedLinkError in `Matrix4.prj`) when testing code that uses a `Camera` by mocking `Camera` or creating a lightweight `Camera` subclass that overrides `update()` methods to do nothing.
* **Reflection for private fields**: When checking private fields of instantiated entities that do not expose public getters (like the private `amount` field in `ExpGem`), use Java reflection to inspect the field values in test assertions.
* **Mock Asset Manager chain**: Mock the dependencies of the asset manager (`mockContext.getGame()`, `mockGame.getAssetManager()`, and asset getters) in unit tests for UI or puzzle components that load textures or sounds dynamically on initialization, preventing `NullPointerException` failures in the test runner.