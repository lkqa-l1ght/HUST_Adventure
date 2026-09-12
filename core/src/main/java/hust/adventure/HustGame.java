package hust.adventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.LoadingScreen;
import hust.adventure.screens.ScreenTransition;
import hust.adventure.screens.LevelLoadingScreen;
import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.core.data.GearDataLoader;
import hust.adventure.core.data.ItemDataLoader;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.core.data.LevelDataLoader;
import hust.adventure.core.data.WaveDataLoader;
import hust.adventure.core.data.WeaponDataLoader;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.MapTransitionData;
import hust.adventure.screens.levels.LevelFactory;
import hust.adventure.screens.GameOverScreen;
import hust.adventure.ui.HUD;
import hust.adventure.ui.InventoryUI;
import hust.adventure.entities.player.PlayerPersistenceService;
import hust.adventure.graphics.ShapeDrawUtils;
import hust.adventure.core.assets.AudioManager;
import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.ui.DamageText;
import hust.adventure.utils.GamePools;
import hust.adventure.core.context.ProgressContext;
import hust.adventure.core.context.GameProgressContext;

import com.badlogic.gdx.Screen;

import hust.adventure.items.base.ItemConfig;
import hust.adventure.items.base.ItemFactory;
import hust.adventure.items.base.ItemManager;
import hust.adventure.items.gear.GearFactory;
import hust.adventure.items.weapons.WeaponFactory;
import hust.adventure.ui.LevelUpChoiceBuilder;
import hust.adventure.ui.DebugOptionRegistry;
import hust.adventure.ui.components.GearUpgradeAction;
import hust.adventure.ui.components.WeaponUpgradeAction;

/**
 * Lớp gốc quản lý vòng đời ứng dụng và lưu trữ các phân hệ trung tâm.
 */
public class HustGame extends Game implements EventListener {
    private ProgressContext progressContext;
    private ItemManager itemManager;
    private LevelFactory levelFactory;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GameAssetManager assetManager;
    private EventDispatcher eventDispatcher;
    public ScreenTransition screenTransition;
    private AudioManager audioManager;
    private LevelDataLoader levelDataManager;
    private WaveDataLoader waveDataManager;
    private WeaponFactory weaponFactory;
    private GearFactory gearFactory;
    private PlayerPersistenceService playerPersistenceService;

    private ItemDataLoader itemDataManager;
    private GearDataLoader gearDataManager;
    private WeaponDataLoader weaponDataManager;
    private EnemyDataLoader enemyDataManager;

    private LevelUpChoiceBuilder levelUpChoiceBuilder;
    private DebugOptionRegistry debugOptionRegistry;

    @Override
    public void create() {

        GamePools.registerPool(DamageText::new);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assetManager = new GameAssetManager();

        itemManager = new ItemManager();
        progressContext = new ProgressContext(itemManager);
        levelFactory = new LevelFactory();

        eventDispatcher = EventDispatcher.getInstance();
        eventDispatcher.addListener(EventType.MAP_TRANSITION, this);
        eventDispatcher.addListener(EventType.ITEM_PICKED_UP, progressContext);
        eventDispatcher.addListener(EventType.ITEM_USED, progressContext);
        eventDispatcher.addListener(EventType.PLAYER_DIED, progressContext);
        eventDispatcher.addListener(EventType.ACADEMIC_SUSPENSION, progressContext);

        audioManager = new AudioManager(assetManager);
        eventDispatcher.addListener(EventType.PLAY_SFX, audioManager);
        eventDispatcher.addListener(EventType.PLAY_BGM, audioManager);
        eventDispatcher.addListener(EventType.LEVEL_UP, audioManager);
        eventDispatcher.addListener(EventType.EXP_GAINED, audioManager);
        eventDispatcher.addListener(EventType.ITEM_PICKED_UP, audioManager);
        eventDispatcher.addListener(EventType.ITEM_USED, audioManager);
        eventDispatcher.addListener(EventType.PUZZLE_SOLVED, audioManager);
        eventDispatcher.addListener(EventType.PUZZLE_FAILED, audioManager);
        eventDispatcher.addListener(EventType.ENTITY_DAMAGED, audioManager);
        eventDispatcher.addListener(EventType.ENTITY_DIED, audioManager);

        screenTransition = new ScreenTransition(this);

        // Khởi tạo font hỗ trợ tiếng Việt
        final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
        final FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 18;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ";
        font = generator.generateFont(parameter);
        generator.dispose();

        // Nạp và đăng ký các vật phẩm từ cấu hình JSON
        itemDataManager = new ItemDataLoader("configs/items.json");
        final ItemFactory itemFactory = new ItemFactory();
        for (final ItemConfig config : itemDataManager.getAllConfigs()) {
            itemManager.register(itemFactory.createItem(config));
        }

        // Nạp và đăng ký cấu hình Gears từ JSON
        gearDataManager = new GearDataLoader("configs/gears.json");
        this.gearFactory = new GearFactory(gearDataManager);

        // Nạp và đăng ký cấu hình Weapons từ JSON
        weaponDataManager = new WeaponDataLoader("configs/weapons.json");
        this.weaponFactory = new WeaponFactory(weaponDataManager);

        this.playerPersistenceService = new PlayerPersistenceService(progressContext, gearFactory, weaponFactory);

        // Nạp và đăng ký cấu hình Levels từ JSON
        levelDataManager = new LevelDataLoader("configs/levels.json");

        // Nạp và đăng ký cấu hình Waves từ JSON
        waveDataManager = new WaveDataLoader("configs/waves.json");

        // Nạp cấu hình Enemies từ JSON
        enemyDataManager = new EnemyDataLoader("configs/enemies.json");

        // Khởi tạo các builder thông qua constructor DI
        levelUpChoiceBuilder = LevelUpChoiceBuilder.builder()
                .gearDataManager(gearDataManager)
                .weaponDataManager(weaponDataManager)
                .progressContext(progressContext)
                .weaponFactory(weaponFactory)
                .gearFactory(gearFactory)
                .build();
        debugOptionRegistry = new DebugOptionRegistry(enemyDataManager, itemDataManager, weaponDataManager,
                gearDataManager, levelDataManager);

        // Khởi đầu bằng màn hình tải tài nguyên
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void onEvent(final GameEvent<?> event) {
        if (event.getType() == EventType.MAP_TRANSITION) {
            handleMapTransition((MapTransitionData) event.getData());
        }
    }

    private void handleMapTransition(final MapTransitionData data) {
        if (screenTransition.isTransitioning())
            return;

        if (progressContext != null && progressContext.getMapDirector() != null
                && !progressContext.getMapDirector().canTransition()) {
            return;
        }

        final LevelConfig template = levelDataManager.getLevelConfigByMapPath(data.getTargetMap());
        if (template == null)
            return;

        final String nextId = template.getLevelId();
        final LevelConfig config = new LevelConfig(nextId, template.getName(), data.getTargetMap(), data.getSpawnX(),
                data.getSpawnY(), template.getZoom(), template.getBgmPath(), template.getAmbientColor(),
                template.isInfinite());

        if (progressContext != null && progressContext.getMapDirector() != null) {
            progressContext.getMapDirector().advanceToNextMap();
        }

        final LevelLoadingScreen loadingScreen = new LevelLoadingScreen(this, config);
        screenTransition.fadeOut(loadingScreen, 0.5f);
    }

    @Override
    public void setScreen(final Screen screen) {
        final Screen oldScreen = this.screen;
        super.setScreen(screen);
        if (oldScreen != null && oldScreen != screen) {
            oldScreen.dispose();
        }
    }

    @Override
    public void render() {
        if (audioManager != null) {
            audioManager.update(Gdx.graphics.getDeltaTime());
        }

        // Ủy quyền render cho Screen hiện hành
        super.render();

        // Vẽ hiệu ứng chuyển cảnh nếu có
        if (screenTransition != null) {
            screenTransition.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void dispose() {
        eventDispatcher.removeListener(EventType.MAP_TRANSITION, this);
        if (progressContext != null) {
            eventDispatcher.removeListener(EventType.ITEM_PICKED_UP, progressContext);
            eventDispatcher.removeListener(EventType.ITEM_USED, progressContext);
            eventDispatcher.removeListener(EventType.PLAYER_DIED, progressContext);
            eventDispatcher.removeListener(EventType.ACADEMIC_SUSPENSION, progressContext);
        }
        if (audioManager != null) {
            eventDispatcher.removeListener(EventType.PLAY_SFX, audioManager);
            eventDispatcher.removeListener(EventType.PLAY_BGM, audioManager);
            eventDispatcher.removeListener(EventType.LEVEL_UP, audioManager);
            eventDispatcher.removeListener(EventType.EXP_GAINED, audioManager);
            eventDispatcher.removeListener(EventType.ITEM_PICKED_UP, audioManager);
            eventDispatcher.removeListener(EventType.ITEM_USED, audioManager);
            eventDispatcher.removeListener(EventType.PUZZLE_SOLVED, audioManager);
            eventDispatcher.removeListener(EventType.PUZZLE_FAILED, audioManager);
            eventDispatcher.removeListener(EventType.ENTITY_DAMAGED, audioManager);
            eventDispatcher.removeListener(EventType.ENTITY_DIED, audioManager);
            audioManager.dispose();
        }

        if (screen != null)
            screen.dispose();
        if (spriteBatch != null)
            spriteBatch.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (font != null)
            font.dispose();
        if (assetManager != null)
            assetManager.dispose();
        if (screenTransition != null)
            screenTransition.dispose();
        GameOverScreen.disposeStatic();
        HUD.disposeStatic();
        InventoryUI.disposeStatic();
        ShapeDrawUtils.disposeStatic();
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public GameAssetManager getAssetManager() {
        return assetManager;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public BitmapFont getFont() {
        return font;
    }

    public ScreenTransition getScreenTransition() {
        return screenTransition;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public LevelDataLoader getLevelDataManager() {
        return levelDataManager;
    }

    public WaveDataLoader getWaveDataManager() {
        return waveDataManager;
    }

    public WeaponFactory getWeaponFactory() {
        return weaponFactory;
    }

    public GearFactory getGearFactory() {
        return gearFactory;
    }

    public ItemDataLoader getItemDataManager() {
        return itemDataManager;
    }

    public GearDataLoader getGearDataManager() {
        return gearDataManager;
    }

    public WeaponDataLoader getWeaponDataManager() {
        return weaponDataManager;
    }

    public EnemyDataLoader getEnemyDataManager() {
        return enemyDataManager;
    }


    public LevelUpChoiceBuilder getLevelUpChoiceBuilder() {
        return levelUpChoiceBuilder;
    }

    public DebugOptionRegistry getDebugOptionRegistry() {
        return debugOptionRegistry;
    }

    public GameProgressContext getProgressContext() {
        return progressContext;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public LevelFactory getLevelFactory() {
        return levelFactory;
    }

    public PlayerPersistenceService getPlayerPersistenceService() {
        return playerPersistenceService;
    }
}
