package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSettings {

    // ============================================
    //                   УПРАВЛЕНИЕ
    // ============================================
    public static float sensitivity = 1.0f;
    public static float adsSensitivity = 0.65f;
    public static float touchLookSensitivity = 1.0f;
    public static boolean invertY = false;
    public static boolean invertX = false;
    public static boolean useGyro = false;
    public static float gyroSensitivity = 1.0f;
    public static boolean vibration = true;
    public static float vibrationIntensity = 1.0f;
    public static float joystickSize = 1.0f;
    public static float joystickOpacity = 0.8f;
    public static float joystickDeadzone = 0.1f;
    public static boolean autoFire = false;
    public static boolean tapToFire = false;
    public static boolean leftHanded = false;
    public static float buttonScale = 1.0f;
    public static float buttonOpacity = 0.85f;

    // ============================================
    //                      HUD
    // ============================================
    public static float hudOpacity = 1.0f;
    public static float hudScale = 1.0f;
    public static boolean showHp = true;
    public static boolean showArmor = true;
    public static boolean showAmmo = true;
    public static boolean showMinimap = true;
    public static boolean showCompass = true;
    public static boolean showScore = true;
    public static boolean showDamageNumbers = true;
    public static boolean showHitmarker = true;
    public static boolean showKillFeed = true;
    public static boolean showFps = false;
    public static boolean showPing = false;
    public static boolean showNetStats = false;
    public static int crosshairStyle = 0;
    public static float crosshairSize = 1.0f;
    public static float crosshairOpacity = 1.0f;
    public static int crosshairColor = 0;

    // ============================================
    //                ВИДЕО / ГРАФИКА
    // ============================================
    public static int resolutionWidth = 0;
    public static int resolutionHeight = 0;
    public static boolean fullscreen = true;
    public static int fpsLimit = 0;
    public static boolean vsync = false;
    public static float renderScale = 1.0f;

    public static int textureQuality = 2;
    public static int modelQuality = 2;
    public static int anisotropicFiltering = 0;
    public static int lodBias = 1;

    public static int shadowQuality = 2;
    public static float shadowDistance = 40f;
    public static int lightingQuality = 2;

    public static float viewDistance = 100f;
    public static int effectsQuality = 2;
    public static boolean particles = true;
    public static boolean reflections = true;
    public static boolean bloom = true;
    public static float bloomIntensity = 1.0f;
    public static int antiAliasing = 0;

    public static boolean motionBlur = false;
    public static float motionBlurAmount = 0.3f;
    public static boolean vignette = true;
    public static float vignetteAmount = 0.5f;
    public static boolean chromaticAberration = true;
    public static float chromaticAmount = 0.4f;
    public static boolean filmGrain = false;
    public static float filmGrainAmount = 0.2f;

    public static float brightness = 0.0f;
    public static float contrast = 0.0f;
    public static float gamma = 1.0f;
    public static float saturation = 1.0f;

    public static float fov = 75f;
    public static float adsFovMultiplier = 0.6f;

    // ============================================
    //                     ЗВУК
    // ============================================
    public static float masterVolume = 1.0f;
    public static float musicVolume = 0.4f;
    public static float sfxVolume = 1.0f;
    public static float voiceVolume = 1.0f;
    public static float ambientVolume = 0.7f;
    public static float weaponVolume = 1.0f;
    public static float footstepVolume = 0.8f;
    public static boolean spatialAudio = true;
    public static int audioQuality = 2;
    public static boolean muteOnBackground = true;

    // ============================================
    //                   ГЕЙМПЛЕЙ
    // ============================================
    public static int difficulty = 1;
    public static boolean autoReload = true;
    public static boolean showTutorial = true;
    public static boolean subtitles = true;
    public static int language = 0;
    public static float cameraShake = 1.0f;
    public static float weaponSway = 1.0f;
    public static float bobAmount = 1.0f;
    public static float fovSprintMultiplier = 1.05f;

    // ============================================
    //                   ДЖЕТПАК
    // ============================================
    public static float jetpackForce = 6f;
    public static float jetpackMaxFuel = 15f;
    public static float jetpackRechargeRate = 0.5f;
    public static float jetpackMaxHeight = 25f;

    // ============================================
    //                     СЕТЬ
    // ============================================
    public static int networkQuality = 1;
    public static int serverRegion = 0;
    public static int interpDelayMs = 100;

    // ============================================
    //             СКРЫТЫЕ ЧИТЫ (dev mode)
    // ============================================
    public static boolean isDevMode = false;
    public static boolean aimbot = false;
    public static boolean esp = false;
    public static boolean magic = false;
    public static boolean triggerBot = false;
    public static boolean wallhack = false;
    public static boolean noRecoil = false;
    public static boolean noSpread = false;
    public static boolean fastReload = false;
    public static boolean speedHack = false;
    public static boolean infiniteAmmo = false;
    public static boolean godMode = false;
    public static float aimbotFov = 150f;
    public static float aimbotSmoothing = 5f;
    public static boolean magicHeadshot = true;
    public static boolean showEspBox = true;
    public static boolean showEspSkeleton = false;
    public static boolean showEspHealth = true;
    public static int espColor = 0;
    public static float espBoxScale = 1.0f;

    // ============================================
    //                  ПРЕСЕТЫ
    // ============================================
    public static final int PRESET_POTATO = 0;
    public static final int PRESET_LOW    = 1;
    public static final int PRESET_MED    = 2;
    public static final int PRESET_HIGH   = 3;
    public static final int PRESET_ULTRA  = 4;

    public static void setPreset(int preset) {
        switch (preset) {
            case PRESET_POTATO:
                textureQuality = 0; modelQuality = 0;
                shadowQuality = 0; lightingQuality = 0;
                effectsQuality = 0; antiAliasing = 0; anisotropicFiltering = 0;
                renderScale = 0.6f; viewDistance = 50f; shadowDistance = 15f;
                lodBias = 0; bloom = false; reflections = false;
                particles = false; motionBlur = false; filmGrain = false;
                chromaticAberration = false; vignette = false;
                fpsLimit = 30;
                break;
            case PRESET_LOW:
                textureQuality = 1; modelQuality = 1;
                shadowQuality = 1; lightingQuality = 1;
                effectsQuality = 1; antiAliasing = 0; anisotropicFiltering = 1;
                renderScale = 0.8f; viewDistance = 70f; shadowDistance = 25f;
                lodBias = 0; bloom = false; reflections = false;
                particles = true; motionBlur = false; filmGrain = false;
                chromaticAberration = true; vignette = true;
                fpsLimit = 60;
                break;
            case PRESET_MED:
                textureQuality = 2; modelQuality = 2;
                shadowQuality = 2; lightingQuality = 2;
                effectsQuality = 2; antiAliasing = 1; anisotropicFiltering = 2;
                renderScale = 1.0f; viewDistance = 100f; shadowDistance = 40f;
                lodBias = 1; bloom = true; reflections = true;
                particles = true; motionBlur = false; filmGrain = false;
                chromaticAberration = true; vignette = true;
                fpsLimit = 60;
                break;
            case PRESET_HIGH:
                textureQuality = 3; modelQuality = 2;
                shadowQuality = 3; lightingQuality = 3;
                effectsQuality = 3; antiAliasing = 2; anisotropicFiltering = 3;
                renderScale = 1.0f; viewDistance = 180f; shadowDistance = 60f;
                lodBias = 2; bloom = true; reflections = true;
                particles = true; motionBlur = false; filmGrain = true;
                chromaticAberration = true; vignette = true;
                fpsLimit = 0;
                break;
            case PRESET_ULTRA:
                textureQuality = 3; modelQuality = 3;
                shadowQuality = 4; lightingQuality = 4;
                effectsQuality = 3; antiAliasing = 3; anisotropicFiltering = 4;
                renderScale = 1.0f; viewDistance = 300f; shadowDistance = 100f;
                lodBias = 2; bloom = true; reflections = true;
                particles = true; motionBlur = true; filmGrain = true;
                chromaticAberration = true; vignette = true;
                fpsLimit = 0;
                break;
        }
        clamp();
    }

    // ============================================
    //              СБРОС / ВАЛИДАЦИЯ
    // ============================================
    public static void resetToDefaults() {
        sensitivity = 1.0f; adsSensitivity = 0.65f; touchLookSensitivity = 1.0f;
        invertY = false; invertX = false; useGyro = false; gyroSensitivity = 1.0f;
        vibration = true; vibrationIntensity = 1.0f;
        joystickSize = 1.0f; joystickOpacity = 0.8f; joystickDeadzone = 0.1f;
        autoFire = false; tapToFire = false; leftHanded = false;
        buttonScale = 1.0f; buttonOpacity = 0.85f;

        hudOpacity = 1.0f; hudScale = 1.0f;
        showHp = true; showArmor = true; showAmmo = true;
        showMinimap = true; showCompass = true; showScore = true;
        showDamageNumbers = true; showHitmarker = true; showKillFeed = true;
        showFps = false; showPing = false; showNetStats = false;
        crosshairStyle = 0; crosshairSize = 1.0f; crosshairOpacity = 1.0f; crosshairColor = 0;

        resolutionWidth = 0; resolutionHeight = 0; fullscreen = true;
        fpsLimit = 0; vsync = false; renderScale = 1.0f;
        textureQuality = 2; modelQuality = 2; anisotropicFiltering = 0; lodBias = 1;
        shadowQuality = 2; shadowDistance = 40f; lightingQuality = 2;
        viewDistance = 100f; effectsQuality = 2; particles = true;
        reflections = true; bloom = true; bloomIntensity = 1.0f; antiAliasing = 0;
        motionBlur = false; motionBlurAmount = 0.3f;
        vignette = true; vignetteAmount = 0.5f;
        chromaticAberration = true; chromaticAmount = 0.4f;
        filmGrain = false; filmGrainAmount = 0.2f;
        brightness = 0.0f; contrast = 0.0f; gamma = 1.0f; saturation = 1.0f;
        fov = 75f; adsFovMultiplier = 0.6f;

        masterVolume = 1.0f; musicVolume = 0.4f; sfxVolume = 1.0f;
        voiceVolume = 1.0f; ambientVolume = 0.7f;
        weaponVolume = 1.0f; footstepVolume = 0.8f;
        spatialAudio = true; audioQuality = 2; muteOnBackground = true;

        difficulty = 1; autoReload = true; showTutorial = true; subtitles = true;
        language = 0; cameraShake = 1.0f; weaponSway = 1.0f;
        bobAmount = 1.0f; fovSprintMultiplier = 1.05f;

        jetpackForce = 6f;
        jetpackMaxFuel = 15f;
        jetpackRechargeRate = 0.5f;
        jetpackMaxHeight = 25f;

        networkQuality = 1; serverRegion = 0; interpDelayMs = 100;

        isDevMode = false;
        aimbot = false; esp = false; magic = false;
        triggerBot = false; wallhack = false;
        noRecoil = false; noSpread = false; fastReload = false;
        speedHack = false; infiniteAmmo = false; godMode = false;
        aimbotFov = 150f; aimbotSmoothing = 5f; magicHeadshot = true;
        showEspBox = true; showEspSkeleton = false; showEspHealth = true;
        espColor = 0; espBoxScale = 1.0f;
    }

    public static void clamp() {
        sensitivity         = clampf(sensitivity,         0.1f,  5.0f);
        adsSensitivity      = clampf(adsSensitivity,      0.1f,  2.0f);
        touchLookSensitivity= clampf(touchLookSensitivity,0.1f,  3.0f);
        gyroSensitivity     = clampf(gyroSensitivity,     0.1f,  3.0f);
        vibrationIntensity  = clampf(vibrationIntensity,  0f,    2.0f);
        joystickSize        = clampf(joystickSize,        0.5f,  1.5f);
        joystickOpacity     = clampf(joystickOpacity,     0.2f,  1.0f);
        joystickDeadzone    = clampf(joystickDeadzone,    0f,    0.5f);
        buttonScale         = clampf(buttonScale,         0.7f,  1.4f);
        buttonOpacity       = clampf(buttonOpacity,       0.2f,  1.0f);

        hudOpacity          = clampf(hudOpacity,          0.2f,  1.0f);
        hudScale            = clampf(hudScale,            0.7f,  1.4f);
        crosshairSize       = clampf(crosshairSize,       0.5f,  2.0f);
        crosshairOpacity    = clampf(crosshairOpacity,    0.2f,  1.0f);

        renderScale         = clampf(renderScale,         0.5f,  1.5f);
        viewDistance        = clampf(viewDistance,        30f,   300f);
        shadowDistance      = clampf(shadowDistance,      5f,    150f);
        bloomIntensity      = clampf(bloomIntensity,      0f,    2.0f);
        motionBlurAmount    = clampf(motionBlurAmount,    0f,    1.0f);
        vignetteAmount      = clampf(vignetteAmount,      0f,    1.0f);
        chromaticAmount     = clampf(chromaticAmount,     0f,    1.0f);
        filmGrainAmount     = clampf(filmGrainAmount,     0f,    1.0f);
        brightness          = clampf(brightness,         -0.5f,  0.5f);
        contrast            = clampf(contrast,           -1.0f,  1.0f);
        gamma               = clampf(gamma,               0.8f,  1.2f);
        saturation          = clampf(saturation,          0f,    2.0f);
        fov                 = clampf(fov,                 60f,   110f);
        adsFovMultiplier    = clampf(adsFovMultiplier,    0.3f,  0.9f);

        masterVolume        = clampf(masterVolume,        0f,    1.0f);
        musicVolume         = clampf(musicVolume,         0f,    1.0f);
        sfxVolume           = clampf(sfxVolume,           0f,    1.0f);
        voiceVolume         = clampf(voiceVolume,         0f,    1.0f);
        ambientVolume       = clampf(ambientVolume,       0f,    1.0f);
        weaponVolume        = clampf(weaponVolume,        0f,    1.0f);
        footstepVolume      = clampf(footstepVolume,      0f,    1.0f);

        cameraShake         = clampf(cameraShake,         0f,    2.0f);
        weaponSway          = clampf(weaponSway,          0f,    2.0f);
        bobAmount           = clampf(bobAmount,           0f,    2.0f);
        fovSprintMultiplier = clampf(fovSprintMultiplier, 1.0f,  1.3f);

        jetpackForce        = clampf(jetpackForce,        1f,    20f);
        jetpackMaxFuel      = clampf(jetpackMaxFuel,      1f,    60f);
        jetpackRechargeRate = clampf(jetpackRechargeRate, 0.1f,  3f);
        jetpackMaxHeight    = clampf(jetpackMaxHeight,    2f,    80f);

        interpDelayMs       = Math.max(50, Math.min(250, interpDelayMs));

        aimbotFov           = clampf(aimbotFov,           10f,   800f);
        aimbotSmoothing     = clampf(aimbotSmoothing,     1f,    20f);
        espBoxScale         = clampf(espBoxScale,         0.5f,  3.0f);

        if (textureQuality     < 0) textureQuality = 0; if (textureQuality > 3) textureQuality = 3;
        if (modelQuality       < 0) modelQuality = 0;   if (modelQuality > 3) modelQuality = 3;
        if (shadowQuality      < 0) shadowQuality = 0;  if (shadowQuality > 4) shadowQuality = 4;
        if (lightingQuality    < 0) lightingQuality = 0;if (lightingQuality > 3) lightingQuality = 3;
        if (effectsQuality     < 0) effectsQuality = 0; if (effectsQuality > 3) effectsQuality = 3;
        if (antiAliasing       < 0) antiAliasing = 0;   if (antiAliasing > 3) antiAliasing = 3;
        if (anisotropicFiltering < 0) anisotropicFiltering = 0; if (anisotropicFiltering > 4) anisotropicFiltering = 4;
        if (lodBias            < 0) lodBias = 0;        if (lodBias > 2) lodBias = 2;
        if (audioQuality       < 0) audioQuality = 0;   if (audioQuality > 3) audioQuality = 3;
        if (difficulty         < 0) difficulty = 0;     if (difficulty > 3) difficulty = 3;
        if (networkQuality     < 0) networkQuality = 0; if (networkQuality > 2) networkQuality = 2;
        if (serverRegion       < 0) serverRegion = 0;   if (serverRegion > 3) serverRegion = 3;
        if (language           < 0) language = 0;       if (language > 1) language = 1;
        if (crosshairStyle     < 0) crosshairStyle = 0; if (crosshairStyle > 3) crosshairStyle = 3;
        if (crosshairColor     < 0) crosshairColor = 0; if (crosshairColor > 5) crosshairColor = 5;
        if (espColor           < 0) espColor = 0;       if (espColor > 4) espColor = 4;
    }

    private static float clampf(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    // ============================================
    //              СОХРАНЕНИЕ / ЗАГРУЗКА
    // ============================================
    private static final String PREF_NAME = "minimilitary_settings";

    public static void save() {
        Preferences p = Gdx.app.getPreferences(PREF_NAME);

        // Управление
        p.putFloat("sensitivity", sensitivity);           p.putFloat("adsSensitivity", adsSensitivity);
        p.putFloat("touchLookSensitivity", touchLookSensitivity);
        p.putBoolean("invertY", invertY);                 p.putBoolean("invertX", invertX);
        p.putBoolean("useGyro", useGyro);                 p.putFloat("gyroSensitivity", gyroSensitivity);
        p.putBoolean("vibration", vibration);             p.putFloat("vibrationIntensity", vibrationIntensity);
        p.putFloat("joystickSize", joystickSize);         p.putFloat("joystickOpacity", joystickOpacity);
        p.putFloat("joystickDeadzone", joystickDeadzone); p.putBoolean("autoFire", autoFire);
        p.putBoolean("tapToFire", tapToFire);             p.putBoolean("leftHanded", leftHanded);
        p.putFloat("buttonScale", buttonScale);           p.putFloat("buttonOpacity", buttonOpacity);

        // HUD
        p.putFloat("hudOpacity", hudOpacity);             p.putFloat("hudScale", hudScale);
        p.putBoolean("showHp", showHp);                   p.putBoolean("showArmor", showArmor);
        p.putBoolean("showAmmo", showAmmo);               p.putBoolean("showMinimap", showMinimap);
        p.putBoolean("showCompass", showCompass);         p.putBoolean("showScore", showScore);
        p.putBoolean("showDamageNumbers", showDamageNumbers);
        p.putBoolean("showHitmarker", showHitmarker);     p.putBoolean("showKillFeed", showKillFeed);
        p.putBoolean("showFps", showFps);                 p.putBoolean("showPing", showPing);
        p.putBoolean("showNetStats", showNetStats);
        p.putInteger("crosshairStyle", crosshairStyle);   p.putFloat("crosshairSize", crosshairSize);
        p.putFloat("crosshairOpacity", crosshairOpacity); p.putInteger("crosshairColor", crosshairColor);

        // Видео
        p.putInteger("resolutionWidth", resolutionWidth); p.putInteger("resolutionHeight", resolutionHeight);
        p.putBoolean("fullscreen", fullscreen);           p.putInteger("fpsLimit", fpsLimit);
        p.putBoolean("vsync", vsync);                     p.putFloat("renderScale", renderScale);
        p.putInteger("textureQuality", textureQuality);   p.putInteger("modelQuality", modelQuality);
        p.putInteger("anisotropicFiltering", anisotropicFiltering); p.putInteger("lodBias", lodBias);
        p.putInteger("shadowQuality", shadowQuality);     p.putFloat("shadowDistance", shadowDistance);
        p.putInteger("lightingQuality", lightingQuality); p.putFloat("viewDistance", viewDistance);
        p.putInteger("effectsQuality", effectsQuality);   p.putBoolean("particles", particles);
        p.putBoolean("reflections", reflections);         p.putBoolean("bloom", bloom);
        p.putFloat("bloomIntensity", bloomIntensity);     p.putInteger("antiAliasing", antiAliasing);
        p.putBoolean("motionBlur", motionBlur);           p.putFloat("motionBlurAmount", motionBlurAmount);
        p.putBoolean("vignette", vignette);               p.putFloat("vignetteAmount", vignetteAmount);
        p.putBoolean("chromaticAberration", chromaticAberration); p.putFloat("chromaticAmount", chromaticAmount);
        p.putBoolean("filmGrain", filmGrain);             p.putFloat("filmGrainAmount", filmGrainAmount);
        p.putFloat("brightness", brightness);             p.putFloat("contrast", contrast);
        p.putFloat("gamma", gamma);                       p.putFloat("saturation", saturation);
        p.putFloat("fov", fov);                           p.putFloat("adsFovMultiplier", adsFovMultiplier);

        // Звук
        p.putFloat("masterVolume", masterVolume);         p.putFloat("musicVolume", musicVolume);
        p.putFloat("sfxVolume", sfxVolume);               p.putFloat("voiceVolume", voiceVolume);
        p.putFloat("ambientVolume", ambientVolume);       p.putFloat("weaponVolume", weaponVolume);
        p.putFloat("footstepVolume", footstepVolume);     p.putBoolean("spatialAudio", spatialAudio);
        p.putInteger("audioQuality", audioQuality);       p.putBoolean("muteOnBackground", muteOnBackground);

        // Геймплей
        p.putInteger("difficulty", difficulty);           p.putBoolean("autoReload", autoReload);
        p.putBoolean("showTutorial", showTutorial);       p.putBoolean("subtitles", subtitles);
        p.putInteger("language", language);               p.putFloat("cameraShake", cameraShake);
        p.putFloat("weaponSway", weaponSway);             p.putFloat("bobAmount", bobAmount);
        p.putFloat("fovSprintMultiplier", fovSprintMultiplier);

        // Джетпак
        p.putFloat("jetpackForce", jetpackForce);
        p.putFloat("jetpackMaxFuel", jetpackMaxFuel);
        p.putFloat("jetpackRechargeRate", jetpackRechargeRate);
        p.putFloat("jetpackMaxHeight", jetpackMaxHeight);

        // Сеть
        p.putInteger("networkQuality", networkQuality);   p.putInteger("serverRegion", serverRegion);
        p.putInteger("interpDelayMs", interpDelayMs);

        // Читы
        p.putBoolean("isDevMode", isDevMode);             p.putBoolean("aimbot", aimbot);
        p.putBoolean("esp", esp);                         p.putBoolean("magic", magic);
        p.putBoolean("triggerBot", triggerBot);           p.putBoolean("wallhack", wallhack);
        p.putBoolean("noRecoil", noRecoil);               p.putBoolean("noSpread", noSpread);
        p.putBoolean("fastReload", fastReload);           p.putBoolean("speedHack", speedHack);
        p.putBoolean("infiniteAmmo", infiniteAmmo);       p.putBoolean("godMode", godMode);
        p.putFloat("aimbotFov", aimbotFov);               p.putFloat("aimbotSmoothing", aimbotSmoothing);
        p.putBoolean("magicHeadshot", magicHeadshot);     p.putBoolean("showEspBox", showEspBox);
        p.putBoolean("showEspSkeleton", showEspSkeleton); p.putBoolean("showEspHealth", showEspHealth);
        p.putInteger("espColor", espColor);               p.putFloat("espBoxScale", espBoxScale);

        p.flush();
    }

    public static void load() {
        Preferences p = Gdx.app.getPreferences(PREF_NAME);

        // Управление
        sensitivity          = p.getFloat("sensitivity",          sensitivity);
        adsSensitivity       = p.getFloat("adsSensitivity",       adsSensitivity);
        touchLookSensitivity = p.getFloat("touchLookSensitivity", touchLookSensitivity);
        invertY              = p.getBoolean("invertY",            invertY);
        invertX              = p.getBoolean("invertX",            invertX);
        useGyro              = p.getBoolean("useGyro",            useGyro);
        gyroSensitivity      = p.getFloat("gyroSensitivity",      gyroSensitivity);
        vibration            = p.getBoolean("vibration",          vibration);
        vibrationIntensity   = p.getFloat("vibrationIntensity",   vibrationIntensity);
        joystickSize         = p.getFloat("joystickSize",         joystickSize);
        joystickOpacity      = p.getFloat("joystickOpacity",      joystickOpacity);
        joystickDeadzone     = p.getFloat("joystickDeadzone",     joystickDeadzone);
        autoFire             = p.getBoolean("autoFire",           autoFire);
        tapToFire            = p.getBoolean("tapToFire",          tapToFire);
        leftHanded           = p.getBoolean("leftHanded",         leftHanded);
        buttonScale          = p.getFloat("buttonScale",          buttonScale);
        buttonOpacity        = p.getFloat("buttonOpacity",        buttonOpacity);

        // HUD
        hudOpacity           = p.getFloat("hudOpacity",           hudOpacity);
        hudScale             = p.getFloat("hudScale",             hudScale);
        showHp               = p.getBoolean("showHp",             showHp);
        showArmor            = p.getBoolean("showArmor",          showArmor);
        showAmmo             = p.getBoolean("showAmmo",           showAmmo);
        showMinimap          = p.getBoolean("showMinimap",        showMinimap);
        showCompass          = p.getBoolean("showCompass",        showCompass);
        showScore            = p.getBoolean("showScore",          showScore);
        showDamageNumbers    = p.getBoolean("showDamageNumbers",  showDamageNumbers);
        showHitmarker        = p.getBoolean("showHitmarker",      showHitmarker);
        showKillFeed         = p.getBoolean("showKillFeed",       showKillFeed);
        showFps              = p.getBoolean("showFps",            showFps);
        showPing             = p.getBoolean("showPing",           showPing);
        showNetStats         = p.getBoolean("showNetStats",       showNetStats);
        crosshairStyle       = p.getInteger("crosshairStyle",     crosshairStyle);
        crosshairSize        = p.getFloat("crosshairSize",        crosshairSize);
        crosshairOpacity     = p.getFloat("crosshairOpacity",     crosshairOpacity);
        crosshairColor       = p.getInteger("crosshairColor",     crosshairColor);

        // Видео
        resolutionWidth      = p.getInteger("resolutionWidth",    resolutionWidth);
        resolutionHeight     = p.getInteger("resolutionHeight",   resolutionHeight);
        fullscreen           = p.getBoolean("fullscreen",         fullscreen);
        fpsLimit             = p.getInteger("fpsLimit",           fpsLimit);
        vsync                = p.getBoolean("vsync",              vsync);
        renderScale          = p.getFloat("renderScale",          renderScale);
        textureQuality       = p.getInteger("textureQuality",     textureQuality);
        modelQuality         = p.getInteger("modelQuality",       modelQuality);
        anisotropicFiltering = p.getInteger("anisotropicFiltering", anisotropicFiltering);
        lodBias              = p.getInteger("lodBias",            lodBias);
        shadowQuality        = p.getInteger("shadowQuality",      shadowQuality);
        shadowDistance       = p.getFloat("shadowDistance",       shadowDistance);
        lightingQuality      = p.getInteger("lightingQuality",    lightingQuality);
        viewDistance         = p.getFloat("viewDistance",         viewDistance);
        effectsQuality       = p.getInteger("effectsQuality",     effectsQuality);
        particles            = p.getBoolean("particles",          particles);
        reflections          = p.getBoolean("reflections",        reflections);
        bloom                = p.getBoolean("bloom",              bloom);
        bloomIntensity       = p.getFloat("bloomIntensity",       bloomIntensity);
        antiAliasing         = p.getInteger("antiAliasing",       antiAliasing);
        motionBlur           = p.getBoolean("motionBlur",         motionBlur);
        motionBlurAmount     = p.getFloat("motionBlurAmount",     motionBlurAmount);
        vignette             = p.getBoolean("vignette",           vignette);
        vignetteAmount       = p.getFloat("vignetteAmount",       vignetteAmount);
        chromaticAberration  = p.getBoolean("chromaticAberration", chromaticAberration);
        chromaticAmount      = p.getFloat("chromaticAmount",      chromaticAmount);
        filmGrain            = p.getBoolean("filmGrain",          filmGrain);
        filmGrainAmount      = p.getFloat("filmGrainAmount",      filmGrainAmount);
        brightness           = p.getFloat("brightness",           brightness);
        contrast             = p.getFloat("contrast",             contrast);
        gamma                = p.getFloat("gamma",                gamma);
        saturation           = p.getFloat("saturation",           saturation);
        fov                  = p.getFloat("fov",                  fov);
        adsFovMultiplier     = p.getFloat("adsFovMultiplier",     adsFovMultiplier);

        // Звук
        masterVolume         = p.getFloat("masterVolume",         masterVolume);
        musicVolume          = p.getFloat("musicVolume",          musicVolume);
        sfxVolume            = p.getFloat("sfxVolume",            sfxVolume);
        voiceVolume          = p.getFloat("voiceVolume",          voiceVolume);
        ambientVolume        = p.getFloat("ambientVolume",        ambientVolume);
        weaponVolume         = p.getFloat("weaponVolume",         weaponVolume);
        footstepVolume       = p.getFloat("footstepVolume",       footstepVolume);
        spatialAudio         = p.getBoolean("spatialAudio",       spatialAudio);
        audioQuality         = p.getInteger("audioQuality",       audioQuality);
        muteOnBackground     = p.getBoolean("muteOnBackground",   muteOnBackground);

        // Геймплей
        difficulty           = p.getInteger("difficulty",         difficulty);
        autoReload           = p.getBoolean("autoReload",         autoReload);
        showTutorial         = p.getBoolean("showTutorial",       showTutorial);
        subtitles            = p.getBoolean("subtitles",          subtitles);
        language             = p.getInteger("language",           language);
        cameraShake          = p.getFloat("cameraShake",          cameraShake);
        weaponSway           = p.getFloat("weaponSway",           weaponSway);
        bobAmount            = p.getFloat("bobAmount",            bobAmount);
        fovSprintMultiplier  = p.getFloat("fovSprintMultiplier",  fovSprintMultiplier);

        // Джетпак
        jetpackForce         = p.getFloat("jetpackForce",         jetpackForce);
        jetpackMaxFuel       = p.getFloat("jetpackMaxFuel",       jetpackMaxFuel);
        jetpackRechargeRate  = p.getFloat("jetpackRechargeRate",  jetpackRechargeRate);
        jetpackMaxHeight     = p.getFloat("jetpackMaxHeight",     jetpackMaxHeight);

        // Сеть
        networkQuality       = p.getInteger("networkQuality",     networkQuality);
        serverRegion         = p.getInteger("serverRegion",       serverRegion);
        interpDelayMs        = p.getInteger("interpDelayMs",      interpDelayMs);

        // Читы
        isDevMode            = p.getBoolean("isDevMode",          isDevMode);
        aimbot               = p.getBoolean("aimbot",             aimbot);
        esp                  = p.getBoolean("esp",                esp);
        magic                = p.getBoolean("magic",              magic);
        triggerBot           = p.getBoolean("triggerBot",         triggerBot);
        wallhack             = p.getBoolean("wallhack",           wallhack);
        noRecoil             = p.getBoolean("noRecoil",           noRecoil);
        noSpread             = p.getBoolean("noSpread",           noSpread);
        fastReload           = p.getBoolean("fastReload",         fastReload);
        speedHack            = p.getBoolean("speedHack",          speedHack);
        infiniteAmmo         = p.getBoolean("infiniteAmmo",       infiniteAmmo);
        godMode              = p.getBoolean("godMode",            godMode);
        aimbotFov            = p.getFloat("aimbotFov",            aimbotFov);
        aimbotSmoothing      = p.getFloat("aimbotSmoothing",      aimbotSmoothing);
        magicHeadshot        = p.getBoolean("magicHeadshot",      magicHeadshot);
        showEspBox           = p.getBoolean("showEspBox",         showEspBox);
        showEspSkeleton      = p.getBoolean("showEspSkeleton",    showEspSkeleton);
        showEspHealth        = p.getBoolean("showEspHealth",      showEspHealth);
        espColor             = p.getInteger("espColor",           espColor);
        espBoxScale          = p.getFloat("espBoxScale",          espBoxScale);

        clamp();
    }
}