package hust.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import hust.adventure.entities.base.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import hust.adventure.HustGame;
import hust.adventure.collision.CollisionManager;
import hust.adventure.core.LootDropService;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.context.LevelManager;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.entities.factory.EntityFactoryImpl;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.player.input.DebugInputHandler;
import hust.adventure.input.InputReader;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.MapTransitionData;
import hust.adventure.gamestate.PlayMode;
import hust.adventure.graphics.CameraManager;
import hust.adventure.graphics.GameRenderer;
import hust.adventure.graphics.LightingManager;
import hust.adventure.screens.levels.LevelBehavior;
import hust.adventure.ui.UIManager;
import hust.adventure.ui.LevelUpChoiceData;
import hust.adventure.ui.LevelUpUIData;
import hust.adventure.ui.components.UpgradeAction;
import hust.adventure.world.InfiniteMapRenderer;
import hust.adventure.world.MapChunk;
import hust.adventure.world.WorldManager;
import hust.adventure.world.MapConfig;
import hust.adventure.world.MapConfigLoader;
import hust.adventure.world.LightingObjectData;
import hust.adventure.world.parsers.WallParser;
import hust.adventure.world.parsers.PortalParser;
import hust.adventure.world.parsers.LightingObjectParser;
import hust.adventure.world.parsers.StaticDecorParser;

/**
 * Concrete gameplay screen. Manages systems lifecycles and delegates gameplay logic to LevelBehavior.
 */
public class PlayScreen extends BaseScreen implements EventListener {
    private final LevelConfig config;
    private final LevelBehavior behavior;
    private final GameProgressContext progressContext;
    private PlayMode state;

    private final WorldManager worldManager;
    private final LevelManager levelManager;
    private final EntityManager entityManager;
    private final UIManager uiManager;
    private final InputReader inputReader;
    private final EntityFactory entityFactory;
    private final CollisionManager collisionManager;
    private final DebugInputHandler debugInputHandler;
    private final Array<UpgradeAction> currentLevelUpActions = new Array<>();

    private CameraManager cameraManager;
    private OrthogonalTiledMapRenderer mapRenderer;
    private GameRenderer gameRenderer;
    private LightingManager lightingManager;
    private Player player;

    private int[] backgroundLayers;
    private int[] foregroundLayers;

    private ShaderProgram silhouetteShader;
    private ShaderProgram discardShader;

    private LootDropService lootDropService;
    private Viewport viewport;

    private static final float VIEW_WIDTH = 800f;
    private static final float VIEW_HEIGHT = 600f;

    public PlayScreen(final HustGame game, final LevelConfig config, final LevelBehavior behavior) {
        this(game, config, behavior, MapConfigLoader.load());
    }

    public PlayScreen(final HustGame game, final LevelConfig config, final LevelBehavior behavior, final MapConfig mapConfig) {
        super(game);
        if (config == null) {
            throw new IllegalArgumentException("LevelConfig cannot be null");
        }
        if (behavior == null) {
            throw new IllegalArgumentException("LevelBehavior cannot be null");
        }
        if (mapConfig == null) {
            throw new IllegalArgumentException("MapConfig cannot be null");
        }
        this.config = config;
        this.behavior = behavior;
        this.progressContext = game.getProgressContext();
        this.state = PlayMode.RUNNING;

        this.worldManager = new WorldManager(mapConfig);
        this.worldManager.registerParser(mapConfig.getCollisionLayerKey(), new WallParser());
        this.worldManager.registerParser(mapConfig.getPortalLayerName(), new PortalParser());
        this.worldManager.registerParser(mapConfig.getLightingLayerName(), new LightingObjectParser());

        final StaticDecorParser decorParser = new StaticDecorParser();
        if (mapConfig.getDecorLayerNames() != null) {
            for (final String name : mapConfig.getDecorLayerNames()) {
                this.worldManager.registerParser(name, decorParser);
            }
        }
        if (mapConfig.getBackgroundLayerNames() != null) {
            for (final String name : mapConfig.getBackgroundLayerNames()) {
                this.worldManager.registerParser(name, decorParser);
            }
        }
        if (mapConfig.getGroundLayerNames() != null) {
            for (final String name : mapConfig.getGroundLayerNames()) {
                this.worldManager.registerParser(name, decorParser);
            }
        }

        this.levelManager = new LevelManager(progressContext);
        this.entityManager = new EntityManager();
        this.collisionManager = new CollisionManager(entityManager, 64f);

        final EnemyDataLoader enemyDataManager = game.getEnemyDataManager();
        this.entityFactory = new EntityFactoryImpl(game.getAssetManager(), entityManager, collisionManager,
                enemyDataManager, progressContext, game.getPlayerPersistenceService());
        this.uiManager = new UIManager(progressContext);
        this.inputReader = new InputReader();
        this.lightingManager = new LightingManager();
        this.debugInputHandler = new DebugInputHandler(uiManager, entityFactory, game.getAssetManager(), inputReader,
                game.getWeaponFactory(), game.getGearFactory(), game.getDebugOptionRegistry(), progressContext);
        this.uiManager.getDebugUI().setInputHandler(debugInputHandler);

        EventDispatcher.getInstance().addListener(EventType.LEVEL_UP, this);
        EventDispatcher.getInstance().addListener(EventType.PLAYER_DIED, this);

        initShaders();
    }

    private void initShaders() {
        if (Gdx.files == null) {
            return;
        }
        final String vert = Gdx.files.internal("shaders/default.vert").readString();
        final String fragSil = Gdx.files.internal("shaders/silhouette.frag").readString();
        final String fragDisc = Gdx.files.internal("shaders/discard.frag").readString();

        this.silhouetteShader = new ShaderProgram(vert, fragSil);
        this.discardShader = new ShaderProgram(vert, fragDisc);
    }

    @Override
    public void show() {
        final InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(inputReader);
        Gdx.input.setInputProcessor(multiplexer);

        loadMap(config);
        initLevel();
    }

    private void loadMap(final LevelConfig config) {
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        // Ghi lại màn hiện tại để GameOverScreen biết restart vào đâu
        progressContext.setCurrentLevelConfig(config);

        lightingManager.setAmbientLight(config.getAmbientColor());
        worldManager.loadMap(game.getAssetManager().getTiledMap(config.getMapPath()));
        spawnMapLightingObjects();
        spawnMapDecorObjects();
        collisionManager.setMap(worldManager.getCurrentMap(), worldManager.getWalls());
        mapRenderer = new OrthogonalTiledMapRenderer(worldManager.getCurrentMap());

        final int tileW = worldManager.getCurrentMap().getProperties().get("tilewidth", Integer.class);
        final int tileH = worldManager.getCurrentMap().getProperties().get("tileheight", Integer.class);
        final float mapW = worldManager.getCurrentMap().getProperties().get("width", Integer.class) * (float) tileW;
        final float mapH = worldManager.getCurrentMap().getProperties().get("height", Integer.class) * (float) tileH;
        final float zoom = worldManager.getCurrentMap().getProperties().get("zoom", config.getZoom(), Float.class);

        if (cameraManager == null) {
            cameraManager = new CameraManager(VIEW_WIDTH, VIEW_HEIGHT);
        }
        if (viewport == null) {
            viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, cameraManager.getCamera());
        }
        boolean infinite = config.isInfinite();
        collisionManager.setInfinite(infinite);
        cameraManager.setInfinite(infinite);

        cameraManager.setZoom(zoom);
        cameraManager.setMapBounds(mapW, mapH);

        for (final MapLayer layer : worldManager.getCurrentMap().getLayers()) {
            if (layer instanceof TiledMapImageLayer) {
                final Object repeatXProp = layer.getProperties().get("repeatx");
                final Object repeatYProp = layer.getProperties().get("repeaty");

                final boolean repeatX = "1".equals(repeatXProp) || Boolean.TRUE.equals(repeatXProp);
                final boolean repeatY = "1".equals(repeatYProp) || Boolean.TRUE.equals(repeatYProp);

                if (repeatX || repeatY) {
                    final TextureRegion region = ((TiledMapImageLayer) layer).getTextureRegion();
                    if (region != null) {
                        final MapChunk chunk = new MapChunk(region);
                        final InfiniteMapRenderer infiniteRenderer = new InfiniteMapRenderer(cameraManager, chunk);
                        worldManager.initInfiniteWorld(infiniteRenderer);
                        layer.setVisible(false);
                        break;
                    }
                }
            }
        }

        // Ưu tiên spawn point từ TMX (objectgroup "Spawn"), fallback sang LevelConfig
        Vector2 tmxSpawn = worldManager.getSpawnPoint();
        float spawnX = (tmxSpawn != null) ? tmxSpawn.x : config.getSpawnX();
        float spawnY = (tmxSpawn != null) ? tmxSpawn.y : config.getSpawnY();

        if (player == null) {
            player = entityFactory.createPlayer(spawnX, spawnY, progressContext.getGlobalInventory(), inputReader);
            cameraManager.setTarget(player);
        } else {
            player.setX(spawnX);
            player.setY(spawnY);
            player.setCollisionManager(collisionManager);
        }

        if (lootDropService == null) {
            lootDropService = new LootDropService(entityFactory, game.getAssetManager(), game.getItemManager());
        }

        gameRenderer = GameRenderer.builder().cameraManager(cameraManager).entityManager(entityManager)
                .batch(game.getSpriteBatch()).silhouetteShader(silhouetteShader).discardShader(discardShader)
                .hud(uiManager.getHud()).statusEffectsHUD(uiManager.getStatusEffectsHUD())
                .inventoryUI(uiManager.getInventoryUI()).levelUpUI(uiManager.getLevelUpUI())
                .damageTextManager(uiManager.getDamageTextManager()).debugUI(uiManager.getDebugUI())
                .worldManager(worldManager).progressContext(progressContext).build();

        setupLayerIndices();

        if (game.getAudioManager() != null && config.getBgmPath() != null) {
            game.getAudioManager().playMusic(config.getBgmPath(), true);
        }
    }

    private void setupLayerIndices() {
        final int[][] layers = worldManager.classifyLayers();
        backgroundLayers = layers[0];
        foregroundLayers = layers[1];
    }

    private void spawnMapLightingObjects() {
        for (final LightingObjectData data : worldManager.getParseResult().getLightingObjects()) {
            final String name = data.getName();
            if ("Book".equalsIgnoreCase(name)) {
                entityFactory.createFloatingBook(data.getX(), data.getY(), lightingManager);
            } else if ("Candle".equalsIgnoreCase(name)) {
                entityFactory.createCandle(data.getX(), data.getY(), lightingManager);
            }
        }
    }

    private void spawnMapDecorObjects() {
        for (final MapObject decor : worldManager.getParseResult().getDecorEntities()) {
            entityManager.addEntity(decor);
        }
    }

    private void initLevel() {
        behavior.init(this);
        if (progressContext != null) {
            progressContext.setAutoAttackAllowed(behavior.isAutoAttackAllowed());
        }
    }

    private void updateLevel(float delta) {
        behavior.update(this, delta);
        if (progressContext != null) {
            progressContext.setAutoAttackAllowed(behavior.isAutoAttackAllowed());
        }
    }

    private void drawLevel() {
        behavior.draw(this);
    }

    @Override
    public void render(float delta) {
        if (state == PlayMode.RUNNING && !game.getScreenTransition().isTransitioning()) {
            if (behavior != null && behavior.isPuzzleActive()) {
                updateLevel(delta);
            } else {
                entityManager.update(delta, entityFactory);
                lightingManager.update();
                collisionManager.update();
                checkTriggers();
                updateLevel(delta);
            }
        }

        // uiManager.update chạy MỌI lúc (kể cả IN_UI) để nhận input từ LevelUpUI, InventoryUI
        uiManager.update(delta, player, index -> {
            if (index >= 0 && index < currentLevelUpActions.size) {
                final UpgradeAction action = currentLevelUpActions.get(index);
                if (action != null) {
                    action.execute(player);
                }
            }
        });

        if (gameRenderer != null) {
            final boolean puzzleActive = behavior != null && behavior.isPuzzleActive();
            gameRenderer.render(delta, mapRenderer, backgroundLayers, foregroundLayers, player, shapeRenderer, font,
                    lightingManager, puzzleActive);
        }

        drawLevel();
        handleInput();
        inputReader.update();
    }

    private void handleInput() {
        if (inputReader.isInventoryJustPressed()) {
            boolean nextState = !progressContext.isInventoryOpen();
            progressContext.setInventoryOpen(nextState);
            if (nextState) {
                this.state = PlayMode.IN_UI;
                EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.INVENTORY_OPENED, null));
            } else {
                this.state = PlayMode.RUNNING;
                EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.INVENTORY_CLOSED, null));
            }
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_UI_CLICK);
        }
        if (inputReader.isDebugJustPressed()) {
            progressContext.setShowDebug(!progressContext.isShowDebug());
            if (!progressContext.isShowDebug()) {
                debugInputHandler.cancelDebug();
                if (state == PlayMode.IN_UI) {
                    state = PlayMode.RUNNING;
                }
            }
        }
        if (inputReader.isHitboxJustPressed()) {
            progressContext.setShowHitbox(!progressContext.isShowHitbox());
        }

        if (progressContext.isShowDebug()) {
            final PlayMode newState = debugInputHandler.handleDebugInput(player, state);
            if (newState != null) {
                state = newState;
            }
        }
    }

    private void checkTriggers() {
        if (game.getScreenTransition().isTransitioning()) {
            return;
        }

        if (behavior != null && !behavior.canTransition(this)) {
            return;
        }

        for (final WorldManager.Portal portal : worldManager.getPortals()) {
            if (portal.getBounds().overlaps(player.getBounds())) {
                MapTransitionData data = new MapTransitionData(portal.getTargetMap(), portal.getSpawnX(),
                        portal.getSpawnY());
                GameEvent<MapTransitionData> event = new GameEvent<>(EventType.MAP_TRANSITION, data);

                EventDispatcher.getInstance().dispatch(event);
                break;
            }
        }
    }

    @Override
    public void onEvent(GameEvent<?> event) {
        if (event.getType() == EventType.LEVEL_UP) {
            this.state = PlayMode.IN_UI;
            final Array<UpgradeAction> choices = game.getLevelUpChoiceBuilder().getLevelUpChoices(player);
            this.currentLevelUpActions.clear();
            this.currentLevelUpActions.addAll(choices);

            final java.util.List<LevelUpChoiceData> choiceDTOs = new java.util.ArrayList<>();
            for (int i = 0; i < choices.size; i++) {
                final UpgradeAction action = choices.get(i);
                choiceDTOs.add(new LevelUpChoiceData(action.getName(), action.getDescription()));
            }
            final LevelUpUIData levelUpUIData = new LevelUpUIData(choiceDTOs);

            uiManager.getLevelUpUI().setChoices(levelUpUIData);
            uiManager.getLevelUpUI().setOnResume(() -> {
                this.state = PlayMode.RUNNING;
            });
        } else if (event.getType() == EventType.PLAYER_DIED) {
            if (!game.getScreenTransition().isTransitioning()) {
                game.getScreenTransition().fadeOut(new GameOverScreen(game), 0.8f);
            }
        }

        // Delegate to behavior (always safe — default no-op in LevelBehavior)
        behavior.onEvent(event);
    }

    public GameProgressContext getProgressContext() {
        return progressContext;
    }

    public HustGame getGame() {
        return game;
    }

    public LevelConfig getConfig() {
        return config;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public UIManager getUIManager() {
        return uiManager;
    }

    public InputReader getInputReader() {
        return inputReader;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public Camera getCamera() {
        return cameraManager.getCamera();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public BitmapFont getFont() {
        return font;
    }

    public PlayMode getState() {
        return state;
    }

    public void setState(final PlayMode state) {
        this.state = state;
    }

    public LightingManager getLightingManager() {
        return lightingManager;
    }

    public LootDropService getLootDropService() {
        return lootDropService;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, false);
        }
        if (gameRenderer != null) {
            gameRenderer.resize(width, height);
        }
        if (behavior != null) {
            behavior.resize(width, height);
        }
    }

    public void unproject(final Vector2 screenCoords) {
        if (gameRenderer != null && gameRenderer.getUiViewport() != null) {
            gameRenderer.getUiViewport().unproject(screenCoords);
        }
    }

    @Override
    public void hide() {
        if (player != null) {
            game.getPlayerPersistenceService().save(player);
        }
    }

    @Override
    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.LEVEL_UP, this);
        EventDispatcher.getInstance().removeListener(EventType.PLAYER_DIED, this);

        if (behavior != null) {
            behavior.dispose(this);
        }

        levelManager.dispose();
        worldManager.dispose();
        entityManager.dispose();
        uiManager.dispose();
        if (lootDropService != null) {
            lootDropService.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (silhouetteShader != null) {
            silhouetteShader.dispose();
        }
        if (discardShader != null) {
            discardShader.dispose();
        }
        if (lightingManager != null) {
            lightingManager.dispose();
        }
    }
}
