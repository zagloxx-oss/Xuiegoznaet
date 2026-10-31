package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class SettingsMenu {

    interface FloatGet { float get(); }
    interface FloatSet { void set(float v); }
    interface BoolGet  { boolean get(); }
    interface BoolSet  { void set(boolean v); }
    interface IntGet   { int get(); }
    interface IntSet   { void set(int v); }
    interface VoidAct  { void run(); }

    static class SliderItem {
        String label; float min, max; String suffix;
        FloatGet get; FloatSet set;
        SliderItem(String l, float mn, float mx, String sfx, FloatGet g, FloatSet s) {
            label = l; min = mn; max = mx; suffix = sfx; get = g; set = s;
        }
        float norm() { return (get.get() - min) / (max - min); }
    }
    static class ToggleItem {
        String label; BoolGet get; BoolSet set;
        ToggleItem(String l, BoolGet g, BoolSet s) { label = l; get = g; set = s; }
    }
    static class OptionItem {
        String label; String[] options; IntGet get; IntSet set;
        OptionItem(String l, String[] o, IntGet g, IntSet s) { label = l; options = o; get = g; set = s; }
    }

    private OrthographicCamera cam;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont titleFont, labelFont, smallFont, tabFont;
    private GlyphLayout layout;

    public int activeTab = 0;
    public int activeSubTab = 0;

    private float scrollY = 0f;
    private float contentHeight = 0f;
    private float viewportTop, viewportBottom, viewportLeft, viewportRight;

    private int activeSliderId = -1;
    private int scrollTouchId  = -1;
    private float scrollStartTouch = 0f;
    private float scrollStartValue = 0f;

    private float sliderStartX = 0f;
    private float sliderStartY = 0f;
    private boolean sliderIsScrolling = false;

    private Runnable onBack;

    private static final float TAB_H    = 90f;
    private static final float SUBTAB_H = 64f;
    private static final float ITEM_H   = 110f;
    private static final float ITEM_GAP = 14f;
    private static final float SLIDER_W = 380f;
    private static final float SIDE_PAD = 40f;
    private static final float BOTTOM_H = 190f;
    private static final float TOGGLE_W = 120f;
    private static final float OPTION_W = 220f;

    private final Vector2 backBtn    = new Vector2();
    private final Vector2 presetBtn  = new Vector2();
    private final Vector2 resetBtn   = new Vector2();
    private final Vector2 applyBtn   = new Vector2();

    private float appliedFeedbackTimer = 0f;

    private List<Object> currentItems = new ArrayList<>();

    private final String[] TABS = {"ВИДЕО", "УПРАВЛ.", "HUD", "ЗВУК", "ГЕЙМПЛЕЙ", "СЕТЬ", "ЧИТЫ"};
    private final String[] VIDEO_SUBS = {"РАЗРЕШ.", "КАЧЕСТВО", "ТЕНИ", "ЭФФЕКТЫ", "ПОСТ", "ЦВЕТ"};
    private final String[] PRESET_NAMES = {"POTATO", "LOW", "MED", "HIGH", "ULTRA"};

    private int builtTab = -1, builtSubTab = -1;
    private boolean builtDevMode = false;

    public void init(Runnable onBack) {
        this.onBack = onBack;
        cam = new OrthographicCamera();
        cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        batch.setProjectionMatrix(cam.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(cam.combined);
        layout = new GlyphLayout();

        String cyr = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        try {
            FreeTypeFontGenerator g = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ru.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p;

            p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 60; p.color = Color.WHITE;
            p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyr;
            p.borderWidth = 2f; p.borderColor = new Color(0, 0, 0, 0.8f);
            titleFont = g.generateFont(p);

            p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 32; p.color = Color.WHITE;
            p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyr;
            p.borderWidth = 1.5f; p.borderColor = new Color(0, 0, 0, 0.7f);
            labelFont = g.generateFont(p);

            p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 24; p.color = Color.WHITE;
            p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyr;
            p.borderWidth = 1f; p.borderColor = new Color(0, 0, 0, 0.7f);
            smallFont = g.generateFont(p);

            p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 28; p.color = Color.WHITE;
            p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyr;
            p.borderWidth = 1.2f; p.borderColor = new Color(0, 0, 0, 0.8f);
            tabFont = g.generateFont(p);

            g.dispose();
        } catch (Exception e) {
            Gdx.app.error("SETTINGS", "Font error: " + e.getMessage());
            titleFont = new BitmapFont(); labelFont = new BitmapFont();
            smallFont = new BitmapFont(); tabFont = new BitmapFont();
        }
        updateLayout();
    }

    private void updateLayout() {
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float tabsTop = h - 90f;
        float tabsBottom = tabsTop - TAB_H;
        float subtabsBottom = tabsBottom - (activeTab == 0 ? (SUBTAB_H + 10f) : 0f);

        viewportTop = subtabsBottom - 20f;
        viewportBottom = BOTTOM_H;
        viewportLeft = SIDE_PAD;
        viewportRight = w - SIDE_PAD;

        presetBtn.set(SIDE_PAD, 60);
        resetBtn.set(SIDE_PAD + 215, 60);
        applyBtn.set(SIDE_PAD + 385, 60);
        backBtn.set(w - 260, 60);
    }

    public void resize(int w, int h) {
        cam.setToOrtho(false, w, h);
        batch.setProjectionMatrix(cam.combined);
        shapes.setProjectionMatrix(cam.combined);
        updateLayout();
    }

    private void rebuildItems() {
        if (activeTab == builtTab && activeSubTab == builtSubTab
                && GameSettings.isDevMode == builtDevMode && !currentItems.isEmpty()) return;
        builtTab = activeTab; builtSubTab = activeSubTab; builtDevMode = GameSettings.isDevMode;
        currentItems.clear();
        switch (activeTab) {
            case 0: buildVideo(); break;
            case 1: buildControls(); break;
            case 2: buildHud(); break;
            case 3: buildAudio(); break;
            case 4: buildGameplay(); break;
            case 5: buildNetwork(); break;
            case 6: buildCheats(); break;
        }
        contentHeight = currentItems.size() * (ITEM_H + ITEM_GAP);
        scrollY = MathUtils.clamp(scrollY, 0f, Math.max(0f, contentHeight - (viewportTop - viewportBottom)));
    }

    private void forceRebuild() { builtTab = -1; rebuildItems(); }

    private void buildVideo() {
        if (activeSubTab == 0) {
            currentItems.add(new OptionItem("Разрешение",
                    new String[]{"Нативное", "1280x720", "1600x900", "1920x1080", "2560x1440"},
                    () -> {
                        int w = GameSettings.resolutionWidth;
                        if (w == 0) return 0; if (w == 1280) return 1;
                        if (w == 1600) return 2; if (w == 1920) return 3; return 4;
                    },
                    i -> {
                        int[][] res = {{0,0},{1280,720},{1600,900},{1920,1080},{2560,1440}};
                        GameSettings.resolutionWidth = res[i][0];
                        GameSettings.resolutionHeight = res[i][1];
                        try {
                            if (GameSettings.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                            else if (res[i][0] > 0) Gdx.graphics.setWindowedMode(res[i][0], res[i][1]);
                        } catch (Exception ex) {}
                    }));
            currentItems.add(new ToggleItem("Полный экран",
                    () -> GameSettings.fullscreen,
                    v -> {
                        GameSettings.fullscreen = v;
                        try {
                            if (v) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                            else  Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                        } catch (Exception ex) {}
                    }));
            currentItems.add(new ToggleItem("V-Sync",
                    () -> GameSettings.vsync,
                    v -> { GameSettings.vsync = v; Gdx.graphics.setVSync(v); }));
            currentItems.add(new SliderItem("Лимит FPS", 0, 240, " fps",
                    () -> GameSettings.fpsLimit,
                    v -> {
                        GameSettings.fpsLimit = Math.round(v);
                        Gdx.graphics.setForegroundFPS(GameSettings.fpsLimit > 0 ? GameSettings.fpsLimit : 0);
                    }));
            currentItems.add(new SliderItem("Масштаб рендера", 0.5f, 1.5f, "x",
                    () -> GameSettings.renderScale, v -> GameSettings.renderScale = v));
        } else if (activeSubTab == 1) {
            currentItems.add(new OptionItem("Текстуры",
                    new String[]{"Низкие","Средние","Высокие","Ультра"},
                    () -> GameSettings.textureQuality, i -> GameSettings.textureQuality = i));
            currentItems.add(new OptionItem("Модели",
                    new String[]{"Низкие","Средние","Высокие","Ультра"},
                    () -> GameSettings.modelQuality, i -> GameSettings.modelQuality = i));
            currentItems.add(new OptionItem("Анизотроп. фильтрация",
                    new String[]{"Выкл","2x","4x","8x","16x"},
                    () -> GameSettings.anisotropicFiltering, i -> GameSettings.anisotropicFiltering = i));
            currentItems.add(new OptionItem("Детализация LOD",
                    new String[]{"Низкая","Средняя","Высокая"},
                    () -> GameSettings.lodBias, i -> GameSettings.lodBias = i));
        } else if (activeSubTab == 2) {
            currentItems.add(new OptionItem("Качество теней",
                    new String[]{"Выкл","Низкое","Среднее","Высокое","Ультра"},
                    () -> GameSettings.shadowQuality, i -> GameSettings.shadowQuality = i));
            currentItems.add(new SliderItem("Дальность теней", 5f, 150f, " м",
                    () -> GameSettings.shadowDistance, v -> GameSettings.shadowDistance = v));
            currentItems.add(new OptionItem("Освещение",
                    new String[]{"Низкое","Среднее","Высокое","Ультра"},
                    () -> GameSettings.lightingQuality, i -> GameSettings.lightingQuality = i));
        } else if (activeSubTab == 3) {
            currentItems.add(new OptionItem("Качество эффектов",
                    new String[]{"Выкл","Низкое","Среднее","Высокое"},
                    () -> GameSettings.effectsQuality, i -> GameSettings.effectsQuality = i));
            currentItems.add(new SliderItem("Дальность прорисовки", 30f, 300f, " м",
                    () -> GameSettings.viewDistance, v -> GameSettings.viewDistance = v));
            currentItems.add(new ToggleItem("Частицы", () -> GameSettings.particles, v -> GameSettings.particles = v));
            currentItems.add(new ToggleItem("Отражения", () -> GameSettings.reflections, v -> GameSettings.reflections = v));
            currentItems.add(new ToggleItem("Bloom", () -> GameSettings.bloom, v -> GameSettings.bloom = v));
            currentItems.add(new SliderItem("Сила Bloom", 0f, 2f, "",
                    () -> GameSettings.bloomIntensity, v -> GameSettings.bloomIntensity = v));
            currentItems.add(new OptionItem("Сглаживание",
                    new String[]{"Выкл","MSAA 2x","MSAA 4x","MSAA 8x"},
                    () -> GameSettings.antiAliasing, i -> GameSettings.antiAliasing = i));
        } else if (activeSubTab == 4) {
            currentItems.add(new ToggleItem("Motion Blur", () -> GameSettings.motionBlur, v -> GameSettings.motionBlur = v));
            currentItems.add(new SliderItem("Сила Motion Blur", 0f, 1f, "",
                    () -> GameSettings.motionBlurAmount, v -> GameSettings.motionBlurAmount = v));
            currentItems.add(new ToggleItem("Виньетка", () -> GameSettings.vignette, v -> GameSettings.vignette = v));
            currentItems.add(new SliderItem("Сила виньетки", 0f, 1f, "",
                    () -> GameSettings.vignetteAmount, v -> GameSettings.vignetteAmount = v));
            currentItems.add(new ToggleItem("Хром. аберрация", () -> GameSettings.chromaticAberration, v -> GameSettings.chromaticAberration = v));
            currentItems.add(new SliderItem("Сила хром. аберрации", 0f, 1f, "",
                    () -> GameSettings.chromaticAmount, v -> GameSettings.chromaticAmount = v));
            currentItems.add(new ToggleItem("Плёночное зерно", () -> GameSettings.filmGrain, v -> GameSettings.filmGrain = v));
            currentItems.add(new SliderItem("Сила зерна", 0f, 1f, "",
                    () -> GameSettings.filmGrainAmount, v -> GameSettings.filmGrainAmount = v));
        } else if (activeSubTab == 5) {
            currentItems.add(new SliderItem("Яркость", -0.5f, 0.5f, "",
                    () -> GameSettings.brightness, v -> GameSettings.brightness = v));
            currentItems.add(new SliderItem("Контраст", -1f, 1f, "",
                    () -> GameSettings.contrast, v -> GameSettings.contrast = v));
            currentItems.add(new SliderItem("Гамма", 0.8f, 1.2f, "",
                    () -> GameSettings.gamma, v -> GameSettings.gamma = v));
            currentItems.add(new SliderItem("Насыщенность", 0f, 2f, "",
                    () -> GameSettings.saturation, v -> GameSettings.saturation = v));
            currentItems.add(new SliderItem("FOV", 60f, 110f, "°",
                    () -> GameSettings.fov, v -> GameSettings.fov = v));
            currentItems.add(new SliderItem("FOV в прицеле", 0.3f, 0.9f, "x",
                    () -> GameSettings.adsFovMultiplier, v -> GameSettings.adsFovMultiplier = v));
        }
    }

    private void buildControls() {
        currentItems.add(new SliderItem("Чувствительность", 0.1f, 5f, "x",
                () -> GameSettings.sensitivity, v -> GameSettings.sensitivity = v));
        currentItems.add(new SliderItem("Чувств. в прицеле", 0.1f, 2f, "x",
                () -> GameSettings.adsSensitivity, v -> GameSettings.adsSensitivity = v));
        currentItems.add(new SliderItem("Чувств. обзора", 0.1f, 3f, "x",
                () -> GameSettings.touchLookSensitivity, v -> GameSettings.touchLookSensitivity = v));
        currentItems.add(new ToggleItem("Инверсия Y", () -> GameSettings.invertY, v -> GameSettings.invertY = v));
        currentItems.add(new ToggleItem("Инверсия X", () -> GameSettings.invertX, v -> GameSettings.invertX = v));
        currentItems.add(new ToggleItem("Гироскоп", () -> GameSettings.useGyro, v -> GameSettings.useGyro = v));
        currentItems.add(new SliderItem("Чувств. гироскопа", 0.1f, 3f, "x",
                () -> GameSettings.gyroSensitivity, v -> GameSettings.gyroSensitivity = v));
        currentItems.add(new ToggleItem("Вибрация", () -> GameSettings.vibration, v -> GameSettings.vibration = v));
        currentItems.add(new SliderItem("Сила вибрации", 0f, 2f, "x",
                () -> GameSettings.vibrationIntensity, v -> GameSettings.vibrationIntensity = v));
        currentItems.add(new SliderItem("Размер джойстика", 0.5f, 1.5f, "x",
                () -> GameSettings.joystickSize, v -> GameSettings.joystickSize = v));
        currentItems.add(new SliderItem("Прозрачность джойстика", 0.2f, 1f, "",
                () -> GameSettings.joystickOpacity, v -> GameSettings.joystickOpacity = v));
        currentItems.add(new SliderItem("Мёртвая зона джойстика", 0f, 0.5f, "",
                () -> GameSettings.joystickDeadzone, v -> GameSettings.joystickDeadzone = v));
        currentItems.add(new ToggleItem("Авто-огонь", () -> GameSettings.autoFire, v -> GameSettings.autoFire = v));
        currentItems.add(new ToggleItem("Огонь по тапу", () -> GameSettings.tapToFire, v -> GameSettings.tapToFire = v));
        currentItems.add(new ToggleItem("Левша (зеркало)", () -> GameSettings.leftHanded, v -> GameSettings.leftHanded = v));
        currentItems.add(new SliderItem("Размер кнопок", 0.7f, 1.4f, "x",
                () -> GameSettings.buttonScale, v -> GameSettings.buttonScale = v));
        currentItems.add(new SliderItem("Прозрачность кнопок", 0.2f, 1f, "",
                () -> GameSettings.buttonOpacity, v -> GameSettings.buttonOpacity = v));
    }

    private void buildHud() {
        currentItems.add(new SliderItem("Прозрачность HUD", 0.2f, 1f, "",
                () -> GameSettings.hudOpacity, v -> GameSettings.hudOpacity = v));
        currentItems.add(new SliderItem("Масштаб HUD", 0.7f, 1.4f, "x",
                () -> GameSettings.hudScale, v -> GameSettings.hudScale = v));
        currentItems.add(new ToggleItem("Показывать HP", () -> GameSettings.showHp, v -> GameSettings.showHp = v));
        currentItems.add(new ToggleItem("Показывать броню", () -> GameSettings.showArmor, v -> GameSettings.showArmor = v));
        currentItems.add(new ToggleItem("Показывать патроны", () -> GameSettings.showAmmo, v -> GameSettings.showAmmo = v));
        currentItems.add(new ToggleItem("Мини-карта", () -> GameSettings.showMinimap, v -> GameSettings.showMinimap = v));
        currentItems.add(new ToggleItem("Компас", () -> GameSettings.showCompass, v -> GameSettings.showCompass = v));
        currentItems.add(new ToggleItem("Счёт", () -> GameSettings.showScore, v -> GameSettings.showScore = v));
        currentItems.add(new ToggleItem("Урон цифрами", () -> GameSettings.showDamageNumbers, v -> GameSettings.showDamageNumbers = v));
        currentItems.add(new ToggleItem("Хитмаркер", () -> GameSettings.showHitmarker, v -> GameSettings.showHitmarker = v));
        currentItems.add(new ToggleItem("Kill feed", () -> GameSettings.showKillFeed, v -> GameSettings.showKillFeed = v));
        currentItems.add(new ToggleItem("Показывать FPS", () -> GameSettings.showFps, v -> GameSettings.showFps = v));
        currentItems.add(new ToggleItem("Показывать пинг", () -> GameSettings.showPing, v -> GameSettings.showPing = v));
        currentItems.add(new ToggleItem("Сетевая статистика", () -> GameSettings.showNetStats, v -> GameSettings.showNetStats = v));
        currentItems.add(new OptionItem("Стиль прицела",
                new String[]{"Крест","Точка","Круг","Динамич."},
                () -> GameSettings.crosshairStyle, i -> GameSettings.crosshairStyle = i));
        currentItems.add(new SliderItem("Размер прицела", 0.5f, 2f, "x",
                () -> GameSettings.crosshairSize, v -> GameSettings.crosshairSize = v));
        currentItems.add(new SliderItem("Прозрачность прицела", 0.2f, 1f, "",
                () -> GameSettings.crosshairOpacity, v -> GameSettings.crosshairOpacity = v));
        currentItems.add(new OptionItem("Цвет прицела",
                new String[]{"Белый","Зелёный","Красный","Жёлтый","Циан","Розовый"},
                () -> GameSettings.crosshairColor, i -> GameSettings.crosshairColor = i));
    }

    private void buildAudio() {
        currentItems.add(new SliderItem("Общая громкость", 0f, 1f, "",
                () -> GameSettings.masterVolume, v -> GameSettings.masterVolume = v));
        currentItems.add(new SliderItem("Музыка", 0f, 1f, "",
                () -> GameSettings.musicVolume, v -> GameSettings.musicVolume = v));
        currentItems.add(new SliderItem("Эффекты (SFX)", 0f, 1f, "",
                () -> GameSettings.sfxVolume, v -> GameSettings.sfxVolume = v));
        currentItems.add(new SliderItem("Голос", 0f, 1f, "",
                () -> GameSettings.voiceVolume, v -> GameSettings.voiceVolume = v));
        currentItems.add(new SliderItem("Окружение", 0f, 1f, "",
                () -> GameSettings.ambientVolume, v -> GameSettings.ambientVolume = v));
        currentItems.add(new SliderItem("Оружие", 0f, 1f, "",
                () -> GameSettings.weaponVolume, v -> GameSettings.weaponVolume = v));
        currentItems.add(new SliderItem("Шаги", 0f, 1f, "",
                () -> GameSettings.footstepVolume, v -> GameSettings.footstepVolume = v));
        currentItems.add(new ToggleItem("Пространственный звук", () -> GameSettings.spatialAudio, v -> GameSettings.spatialAudio = v));
        currentItems.add(new OptionItem("Качество звука",
                new String[]{"Низкое","Среднее","Высокое","Ультра"},
                () -> GameSettings.audioQuality, i -> GameSettings.audioQuality = i));
        currentItems.add(new ToggleItem("Тишина в фоне", () -> GameSettings.muteOnBackground, v -> GameSettings.muteOnBackground = v));
    }

    private void buildGameplay() {
        currentItems.add(new OptionItem("Сложность",
                new String[]{"Лёгкая","Обычная","Сложная","Кошмар"},
                () -> GameSettings.difficulty, i -> GameSettings.difficulty = i));
        currentItems.add(new ToggleItem("Авто-перезарядка", () -> GameSettings.autoReload, v -> GameSettings.autoReload = v));
        currentItems.add(new ToggleItem("Туториал", () -> GameSettings.showTutorial, v -> GameSettings.showTutorial = v));
        currentItems.add(new ToggleItem("Субтитры", () -> GameSettings.subtitles, v -> GameSettings.subtitles = v));
        currentItems.add(new OptionItem("Язык",
                new String[]{"Русский","English"},
                () -> GameSettings.language, i -> GameSettings.language = i));
        currentItems.add(new SliderItem("Тряска камеры", 0f, 2f, "x",
                () -> GameSettings.cameraShake, v -> GameSettings.cameraShake = v));
        currentItems.add(new SliderItem("Покачивание оружия", 0f, 2f, "x",
                () -> GameSettings.weaponSway, v -> GameSettings.weaponSway = v));
        currentItems.add(new SliderItem("Покачивание при ходьбе", 0f, 2f, "x",
                () -> GameSettings.bobAmount, v -> GameSettings.bobAmount = v));
        currentItems.add(new SliderItem("FOV при беге", 1f, 1.3f, "x",
                () -> GameSettings.fovSprintMultiplier, v -> GameSettings.fovSprintMultiplier = v));
    }

    private void buildNetwork() {
        currentItems.add(new OptionItem("Качество сети",
                new String[]{"Низкое","Среднее","Высокое"},
                () -> GameSettings.networkQuality, i -> GameSettings.networkQuality = i));
        currentItems.add(new OptionItem("Регион сервера",
                new String[]{"Авто","EU","US","ASIA"},
                () -> GameSettings.serverRegion, i -> GameSettings.serverRegion = i));
        currentItems.add(new SliderItem("Задержка интерполяции", 50f, 250f, " мс",
                () -> GameSettings.interpDelayMs, v -> GameSettings.interpDelayMs = Math.round(v)));
    }

    private void buildCheats() {
        currentItems.add(new ToggleItem("Aimbot", () -> GameSettings.aimbot, v -> GameSettings.aimbot = v));
        currentItems.add(new ToggleItem("ESP (Wallhack)", () -> GameSettings.esp, v -> GameSettings.esp = v));
        currentItems.add(new ToggleItem("Magic Bullet", () -> GameSettings.magic, v -> GameSettings.magic = v));
        currentItems.add(new ToggleItem("Trigger Bot", () -> GameSettings.triggerBot, v -> GameSettings.triggerBot = v));
        currentItems.add(new ToggleItem("No Recoil", () -> GameSettings.noRecoil, v -> GameSettings.noRecoil = v));
        currentItems.add(new ToggleItem("No Spread", () -> GameSettings.noSpread, v -> GameSettings.noSpread = v));
        currentItems.add(new ToggleItem("Fast Reload", () -> GameSettings.fastReload, v -> GameSettings.fastReload = v));
        currentItems.add(new ToggleItem("Speed Hack", () -> GameSettings.speedHack, v -> GameSettings.speedHack = v));
        currentItems.add(new ToggleItem("Infinite Ammo", () -> GameSettings.infiniteAmmo, v -> GameSettings.infiniteAmmo = v));
        currentItems.add(new ToggleItem("God Mode", () -> GameSettings.godMode, v -> GameSettings.godMode = v));
        currentItems.add(new SliderItem("Aimbot FOV", 10f, 800f, "",
                () -> GameSettings.aimbotFov, v -> GameSettings.aimbotFov = v));
        currentItems.add(new SliderItem("Aimbot smoothing", 1f, 20f, "",
                () -> GameSettings.aimbotSmoothing, v -> GameSettings.aimbotSmoothing = v));
        currentItems.add(new ToggleItem("Magic: Headshot", () -> GameSettings.magicHeadshot, v -> GameSettings.magicHeadshot = v));
        currentItems.add(new ToggleItem("ESP: Box", () -> GameSettings.showEspBox, v -> GameSettings.showEspBox = v));
        currentItems.add(new ToggleItem("ESP: Skeleton", () -> GameSettings.showEspSkeleton, v -> GameSettings.showEspSkeleton = v));
        currentItems.add(new ToggleItem("ESP: Health", () -> GameSettings.showEspHealth, v -> GameSettings.showEspHealth = v));
        currentItems.add(new OptionItem("ESP: цвет",
                new String[]{"Красный","Зелёный","Циан","Жёлтый","Розовый"},
                () -> GameSettings.espColor, i -> GameSettings.espColor = i));
        currentItems.add(new SliderItem("ESP: масштаб бокса", 0.5f, 3f, "x",
                () -> GameSettings.espBoxScale, v -> GameSettings.espBoxScale = v));
    }

    public void render(float delta) {
        updateLayout();
        rebuildItems();
        if (appliedFeedbackTimer > 0) appliedFeedbackTimer -= delta;

        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.02f, 0.06f, 0.98f);
        shapes.rect(0, 0, w, h);
        shapes.end();

        int tabsCount = GameSettings.isDevMode ? 7 : 6;
        float tabW = (w - SIDE_PAD * 2) / tabsCount;
        float tabsTopY = h - 90f;
        float tabsBottomY = tabsTopY - TAB_H;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabsCount; i++) {
            float tx = SIDE_PAD + i * tabW;
            boolean act = (i == activeTab);
            shapes.setColor(act ? new Color(0.15f, 0.45f, 0.95f, 1f) : new Color(0.06f, 0.08f, 0.14f, 1f));
            shapes.rect(tx + 3, tabsBottomY + 3, tabW - 6, TAB_H - 6);
            if (act) {
                shapes.setColor(0.3f, 0.75f, 1f, 0.7f);
                shapes.rect(tx + 3, tabsBottomY + TAB_H - 8, tabW - 6, 4);
            }
        }
        if (activeTab == 0) {
            float stW = (w - SIDE_PAD * 2) / VIDEO_SUBS.length;
            float stTopY = tabsBottomY - 10f;
            for (int i = 0; i < VIDEO_SUBS.length; i++) {
                float tx = SIDE_PAD + i * stW;
                boolean act = (i == activeSubTab);
                shapes.setColor(act ? new Color(0.2f, 0.5f, 0.9f, 1f) : new Color(0.04f, 0.05f, 0.1f, 1f));
                shapes.rect(tx + 3, stTopY - SUBTAB_H + 3, stW - 6, SUBTAB_H - 6);
            }
        }
        shapes.end();

        float bbW = Gdx.graphics.getBackBufferWidth();
        float bbH = Gdx.graphics.getBackBufferHeight();
        int sX = (int)(viewportLeft * (bbW / w));
        int sY = (int)(viewportBottom * (bbH / h));
        int sW = (int)((viewportRight - viewportLeft) * (bbW / w));
        int sH = (int)((viewportTop - viewportBottom) * (bbH / h));
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(sX, sY, sW, sH);

        float firstY = viewportTop - ITEM_H + scrollY;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < currentItems.size(); i++) {
            float y = firstY - i * (ITEM_H + ITEM_GAP);
            if (y + ITEM_H < viewportBottom - 20 || y > viewportTop + 20) continue;

            shapes.setColor(0.06f, 0.08f, 0.14f, 0.95f);
            shapes.rect(viewportLeft + 20, y, viewportRight - viewportLeft - 40, ITEM_H);
            shapes.setColor(0.15f, 0.3f, 0.55f, 0.7f);
            shapes.rect(viewportLeft + 20, y, 4, ITEM_H);

            Object item = currentItems.get(i);
            float ix = viewportLeft + 40;
            if (item instanceof SliderItem) drawSlider((SliderItem) item, ix, y);
            else if (item instanceof ToggleItem) drawToggle((ToggleItem) item, ix, y);
            else if (item instanceof OptionItem) drawOption((OptionItem) item, ix, y);
        }
        shapes.end();

        batch.begin();
        for (int i = 0; i < currentItems.size(); i++) {
            float y = firstY - i * (ITEM_H + ITEM_GAP);
            if (y + ITEM_H < viewportBottom - 20 || y > viewportTop + 20) continue;
            drawItemText(currentItems.get(i), viewportLeft + 40, y);
        }
        batch.end();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        batch.begin();
        for (int i = 0; i < tabsCount; i++) {
            float tx = SIDE_PAD + i * tabW;
            tabFont.setColor(i == activeTab ? Color.WHITE : new Color(0.6f, 0.65f, 0.75f, 1f));
            layout.setText(tabFont, TABS[i]);
            tabFont.draw(batch, TABS[i], tx + tabW / 2f - layout.width / 2f, tabsBottomY + TAB_H / 2f + layout.height / 2f);
        }
        if (activeTab == 0) {
            float stW = (w - SIDE_PAD * 2) / VIDEO_SUBS.length;
            float stTopY = tabsBottomY - 10f;
            for (int i = 0; i < VIDEO_SUBS.length; i++) {
                float tx = SIDE_PAD + i * stW;
                smallFont.setColor(i == activeSubTab ? Color.WHITE : new Color(0.5f, 0.55f, 0.65f, 1f));
                layout.setText(smallFont, VIDEO_SUBS[i]);
                smallFont.draw(batch, VIDEO_SUBS[i], tx + stW / 2f - layout.width / 2f, stTopY - SUBTAB_H / 2f + layout.height / 2f);
            }
        }
        titleFont.setColor(0.4f, 0.85f, 1f, 1f);
        layout.setText(titleFont, "НАСТРОЙКИ");
        titleFont.draw(batch, "НАСТРОЙКИ", w / 2f - layout.width / 2f, h - 20);
        batch.end();

        // Кнопки внизу
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.2f, 0.6f, 1f, 1f);
        shapes.rect(presetBtn.x, presetBtn.y, 195, 80);
        shapes.setColor(0.08f, 0.1f, 0.16f, 1f);
        shapes.rect(presetBtn.x + 4, presetBtn.y + 4, 187, 72);

        shapes.setColor(1f, 0.5f, 0.2f, 1f);
        shapes.rect(resetBtn.x, resetBtn.y, 150, 80);
        shapes.setColor(0.08f, 0.1f, 0.16f, 1f);
        shapes.rect(resetBtn.x + 4, resetBtn.y + 4, 142, 72);

        // ПРИМЕНИТЬ — зелёная, подсвечивается если активно
        boolean flash = appliedFeedbackTimer > 0;
        shapes.setColor(flash ? new Color(0.2f, 1f, 0.4f, 1f) : new Color(0.15f, 0.75f, 0.35f, 1f));
        shapes.rect(applyBtn.x, applyBtn.y, 195, 80);
        shapes.setColor(0.08f, 0.1f, 0.16f, 1f);
        shapes.rect(applyBtn.x + 4, applyBtn.y + 4, 187, 72);

        shapes.setColor(0.55f, 0.6f, 0.75f, 1f);
        shapes.rect(backBtn.x, backBtn.y, 240, 80);
        shapes.setColor(0.08f, 0.1f, 0.16f, 1f);
        shapes.rect(backBtn.x + 4, backBtn.y + 4, 232, 72);

        shapes.end();

        batch.begin();
        labelFont.setColor(Color.WHITE);
        String pLabel = "ПРЕСЕТ: " + PRESET_NAMES[detectPreset()];
        layout.setText(labelFont, pLabel);
        labelFont.draw(batch, pLabel, presetBtn.x + 97 - layout.width / 2f, presetBtn.y + 50);

        layout.setText(labelFont, "СБРОС");
        labelFont.draw(batch, "СБРОС", resetBtn.x + 75 - layout.width / 2f, resetBtn.y + 50);

        if (flash) labelFont.setColor(0.3f, 1f, 0.5f, 1f); else labelFont.setColor(Color.WHITE);
        layout.setText(labelFont, flash ? "✓ ГОТОВО" : "ПРИМЕНИТЬ");
        labelFont.draw(batch, flash ? "✓ ГОТОВО" : "ПРИМЕНИТЬ", applyBtn.x + 97 - layout.width / 2f, applyBtn.y + 50);

        labelFont.setColor(Color.WHITE);
        layout.setText(labelFont, "НАЗАД");
        labelFont.draw(batch, "НАЗАД", backBtn.x + 120 - layout.width / 2f, backBtn.y + 50);

        smallFont.setColor(0.5f, 1f, 0.5f, 0.85f);
        smallFont.draw(batch, "Изменения сохраняются автоматически. Тяни вниз чтобы прокрутить", SIDE_PAD, 35);
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String formatVal(SliderItem s) {
        float v = s.get.get();
        if (s.max - s.min <= 2f && s.min >= 0f) return String.format("%.2f%s", v, s.suffix);
        return String.format("%.0f%s", v, s.suffix);
    }

    private void drawSlider(SliderItem s, float x, float y) {
        float trackY = y + 26, trackX = x + 16;
        shapes.setColor(0.1f, 0.12f, 0.2f, 1f);
        shapes.rect(trackX, trackY, SLIDER_W, 16);
        float n = MathUtils.clamp(s.norm(), 0f, 1f);
        shapes.setColor(0.2f, 0.6f, 1f, 1f);
        shapes.rect(trackX, trackY, SLIDER_W * n, 16);
        float kx = trackX + SLIDER_W * n;
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.circle(kx, trackY + 8, 16, 20);
        shapes.setColor(0.3f, 0.75f, 1f, 1f);
        shapes.circle(kx, trackY + 8, 10, 16);
    }

    private void drawToggle(ToggleItem t, float x, float y) {
        float th = 60;
        float tx = viewportRight - 20 - TOGGLE_W - 20;
        float ty = y + (ITEM_H - th) / 2f;
        boolean on = t.get.get();
        shapes.setColor(on ? new Color(0.15f, 0.9f, 0.4f, 1f) : new Color(0.25f, 0.28f, 0.35f, 1f));
        shapes.rect(tx, ty, TOGGLE_W, th);
        float knobX = on ? tx + TOGGLE_W - th / 2f : tx + th / 2f;
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.circle(knobX, ty + th / 2f, th / 2f - 5, 20);
    }

    private void drawOption(OptionItem o, float x, float y) {
        float oh = 60;
        float ox = viewportRight - 20 - OPTION_W - 20;
        float oy = y + (ITEM_H - oh) / 2f;
        shapes.setColor(0.1f, 0.15f, 0.25f, 1f);
        shapes.rect(ox, oy, OPTION_W, oh);
        shapes.setColor(0.2f, 0.5f, 1f, 0.9f);
        shapes.rect(ox, oy, 3, oh);
        shapes.setColor(0.7f, 0.85f, 1f, 1f);
        shapes.triangle(ox + 10, oy + oh / 2f, ox + 22, oy + oh / 2f + 10, ox + 22, oy + oh / 2f - 10);
        shapes.triangle(ox + OPTION_W - 10, oy + oh / 2f, ox + OPTION_W - 22, oy + oh / 2f + 10, ox + OPTION_W - 22, oy + oh / 2f - 10);
    }

    private void drawItemText(Object item, float x, float y) {
        if (item instanceof SliderItem) {
            SliderItem s = (SliderItem) item;
            labelFont.setColor(Color.WHITE);
            labelFont.draw(batch, s.label, x + 16, y + ITEM_H - 22);
            smallFont.setColor(0.5f, 0.85f, 1f, 1f);
            String val = formatVal(s);
            layout.setText(smallFont, val);
            smallFont.draw(batch, val, x + 16 + SLIDER_W + 20, y + ITEM_H - 24);
        } else if (item instanceof ToggleItem) {
            ToggleItem t = (ToggleItem) item;
            labelFont.setColor(Color.WHITE);
            labelFont.draw(batch, t.label, x + 16, y + ITEM_H / 2f + 12);
            smallFont.setColor(t.get.get() ? new Color(0.3f, 1f, 0.4f, 1f) : new Color(0.55f, 0.55f, 0.6f, 1f));
            String v = t.get.get() ? "ВКЛ" : "ВЫКЛ";
            layout.setText(smallFont, v);
            smallFont.draw(batch, v, viewportRight - 20 - TOGGLE_W - 20 - 60, y + ITEM_H / 2f + 8);
        } else if (item instanceof OptionItem) {
            OptionItem o = (OptionItem) item;
            labelFont.setColor(Color.WHITE);
            labelFont.draw(batch, o.label, x + 16, y + ITEM_H / 2f + 12);
            smallFont.setColor(0.5f, 0.85f, 1f, 1f);
            String v = o.options[o.get.get()];
            layout.setText(smallFont, v);
            float optX = viewportRight - 20 - OPTION_W - 20;
            smallFont.draw(batch, v, optX + OPTION_W / 2f - layout.width / 2f, y + ITEM_H / 2f + 8);
        }
    }

    // =========================================================
    //                    INPUT (главные фиксы)
    // =========================================================
    public boolean touchDown(int sx, int sy, int pointer) {
        float worldY = Gdx.graphics.getHeight() - sy;
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        int tabsCount = GameSettings.isDevMode ? 7 : 6;
        float tabW = (w - SIDE_PAD * 2) / tabsCount;
        float tabsTopY = h - 90f;
        float tabsBottomY = tabsTopY - TAB_H;

        for (int i = 0; i < tabsCount; i++) {
            float tx = SIDE_PAD + i * tabW;
            if (sx >= tx && sx <= tx + tabW && worldY >= tabsBottomY && worldY <= tabsTopY) {
                if (activeTab != i) { activeTab = i; activeSubTab = 0; scrollY = 0f; }
                return true;
            }
        }
        if (activeTab == 0) {
            float stW = (w - SIDE_PAD * 2) / VIDEO_SUBS.length;
            float stTopY = tabsBottomY - 10f;
            for (int i = 0; i < VIDEO_SUBS.length; i++) {
                float tx = SIDE_PAD + i * stW;
                if (sx >= tx && sx <= tx + stW && worldY >= stTopY - SUBTAB_H && worldY <= stTopY) {
                    if (activeSubTab != i) { activeSubTab = i; scrollY = 0f; }
                    return true;
                }
            }
        }

        // Кнопки
        if (sx >= presetBtn.x && sx <= presetBtn.x + 195 && worldY >= presetBtn.y && worldY <= presetBtn.y + 80) {
            int cur = detectPreset();
            GameSettings.setPreset((cur + 1) % 5);
            forceRebuild(); GameSettings.save();
            return true;
        }
        if (sx >= resetBtn.x && sx <= resetBtn.x + 150 && worldY >= resetBtn.y && worldY <= resetBtn.y + 80) {
            GameSettings.resetToDefaults(); forceRebuild(); GameSettings.save();
            return true;
        }
        if (sx >= applyBtn.x && sx <= applyBtn.x + 195 && worldY >= applyBtn.y && worldY <= applyBtn.y + 80) {
            GameSettings.clamp();
            GameSettings.save();
            try {
                if (GameSettings.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else if (GameSettings.resolutionWidth > 0) Gdx.graphics.setWindowedMode(GameSettings.resolutionWidth, GameSettings.resolutionHeight);
                Gdx.graphics.setVSync(GameSettings.vsync);
                Gdx.graphics.setForegroundFPS(GameSettings.fpsLimit > 0 ? GameSettings.fpsLimit : 0);
            } catch (Exception ex) {}
            appliedFeedbackTimer = 1.5f;
            return true;
        }
        if (sx >= backBtn.x && sx <= backBtn.x + 240 && worldY >= backBtn.y && worldY <= backBtn.y + 80) {
            GameSettings.save();
            if (onBack != null) onBack.run();
            return true;
        }

        // Items
        if (sx >= viewportLeft && sx <= viewportRight && worldY >= viewportBottom && worldY <= viewportTop) {
            float firstY = viewportTop - ITEM_H + scrollY;
            for (int i = 0; i < currentItems.size(); i++) {
                float y = firstY - i * (ITEM_H + ITEM_GAP);
                if (worldY >= y && worldY <= y + ITEM_H) {
                    Object item = currentItems.get(i);
                    float ix = viewportLeft + 40;
                    boolean handled = false;

                    if (item instanceof SliderItem) {
                        SliderItem s = (SliderItem) item;
                        // Только дорожка слайдера
                        if (sx >= ix + 16 - 10 && sx <= ix + 16 + SLIDER_W + 10) {
                            activeSliderId = i;
                            sliderStartX = sx;
                            sliderStartY = worldY;
                            sliderIsScrolling = false;
                            applySlider(s, ix, sx);
                            handled = true;
                        }
                    } else if (item instanceof ToggleItem) {
                        ToggleItem t = (ToggleItem) item;
                        float toggleX = viewportRight - 20 - TOGGLE_W - 20;
                        // Только область тогла (с запасом ±25)
                        if (sx >= toggleX - 25 && sx <= toggleX + TOGGLE_W + 25) {
                            t.set.set(!t.get.get());
                            GameSettings.clamp();
                            handled = true;
                        }
                    } else if (item instanceof OptionItem) {
                        OptionItem o = (OptionItem) item;
                        float optX = viewportRight - 20 - OPTION_W - 20;
                        // Только область опции
                        if (sx >= optX - 25 && sx <= optX + OPTION_W + 25) {
                            if (sx >= optX + OPTION_W - 50) o.set.set((o.get.get() + 1) % o.options.length);
                            else if (sx <= optX + 50) o.set.set((o.get.get() - 1 + o.options.length) % o.options.length);
                            else o.set.set((o.get.get() + 1) % o.options.length);
                            GameSettings.clamp();
                            handled = true;
                        }
                    }

                    if (handled) return true;
                    // Не попали в контрол → скролл
                    scrollTouchId = pointer;
                    scrollStartTouch = worldY;
                    scrollStartValue = scrollY;
                    return true;
                }
            }
            // Пустое место → скролл
            scrollTouchId = pointer;
            scrollStartTouch = worldY;
            scrollStartValue = scrollY;
            return true;
        }
        return false;
    }

    public boolean touchDragged(int sx, int sy, int pointer) {
        float worldY = Gdx.graphics.getHeight() - sy;

        if (activeSliderId != -1 && activeSliderId < currentItems.size()) {
            float dx = Math.abs(sx - sliderStartX);
            float dy = Math.abs(worldY - sliderStartY);

            // Если движение больше по Y и >40px — это скролл, отменяем слайдер
            if (!sliderIsScrolling && dy > 40 && dy > dx * 1.5f) {
                sliderIsScrolling = true;
                activeSliderId = -1;
                scrollTouchId = pointer;
                scrollStartTouch = worldY;
                scrollStartValue = scrollY;
                return true;
            }
            if (!sliderIsScrolling) {
                Object item = currentItems.get(activeSliderId);
                if (item instanceof SliderItem) applySlider((SliderItem) item, viewportLeft + 40, sx);
                return true;
            }
        }
        if (pointer == scrollTouchId) {
            float dy = worldY - scrollStartTouch;
            float maxScroll = Math.max(0f, contentHeight - (viewportTop - viewportBottom));
            scrollY = MathUtils.clamp(scrollStartValue + dy, 0f, maxScroll);
            return true;
        }
        return false;
    }

    public boolean touchUp(int pointer) {
        if (activeSliderId != -1) { GameSettings.save(); activeSliderId = -1; }
        if (pointer == scrollTouchId) scrollTouchId = -1;
        sliderIsScrolling = false;
        return false;
    }

    private void applySlider(SliderItem s, float trackX, float touchX) {
        float n = MathUtils.clamp((touchX - (trackX + 16)) / SLIDER_W, 0f, 1f);
        s.set.set(s.min + n * (s.max - s.min));
        GameSettings.clamp();
    }

    private int detectPreset() {
        if (GameSettings.viewDistance <= 50f) return 0;
        if (GameSettings.viewDistance <= 70f) return 1;
        if (GameSettings.viewDistance <= 100f) return 2;
        if (GameSettings.viewDistance <= 180f) return 3;
        return 4;
    }

    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (titleFont != null) titleFont.dispose();
        if (labelFont != null) labelFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (tabFont != null) tabFont.dispose();
    }
}