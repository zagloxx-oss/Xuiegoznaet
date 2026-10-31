package com.minimilitary3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class MainGame extends Game {
    @Override
    public void create() {
        // ⚡ Ловим все ошибки и выводим их в лог
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Gdx.app.error("CRASH", "КРИТИЧЕСКАЯ ОШИБКА: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // === ЗАГРУЗКА НАСТРОЕК ДО ПЕРВОГО ЭКРАНА ===
        GameSettings.load();
        applyVideoSettings();

        setScreen(new SplashScreen1(this));
    }

    /**
     * Применяет настройки видео: разрешение / fullscreen / vsync.
     * Все вызовы в try-catch, потому что на Android некоторые из них
     * могут кидать исключения на устройствах с фиксированным разрешением.
     */
    private void applyVideoSettings() {
        try {
            if (GameSettings.fullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else if (GameSettings.resolutionWidth > 0 && GameSettings.resolutionHeight > 0) {
                Gdx.graphics.setWindowedMode(GameSettings.resolutionWidth, GameSettings.resolutionHeight);
            }
        } catch (Exception e) {
            Gdx.app.error("VIDEO", "applyVideoSettings resolution: " + e.getMessage());
        }

        try {
            Gdx.graphics.setVSync(GameSettings.vsync);
        } catch (Exception e) {
            Gdx.app.error("VIDEO", "applyVideoSettings vsync: " + e.getMessage());
        }

        // Лимит FPS
        try {
            if (GameSettings.fpsLimit > 0) {
                Gdx.graphics.setForegroundFPS(GameSettings.fpsLimit);
            } else {
                Gdx.graphics.setForegroundFPS(0); // unlimited
            }
        } catch (Exception e) {
            Gdx.app.error("VIDEO", "applyVideoSettings fpsLimit: " + e.getMessage());
        }
    }

    /**
     * Вызывается при сворачивании приложения (сворачивание на Android,
     * сворачивание окна на десктопе).
     */
    @Override
    public void pause() {
        GameSettings.save();
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        GameSettings.save();
        super.dispose();
    }
}