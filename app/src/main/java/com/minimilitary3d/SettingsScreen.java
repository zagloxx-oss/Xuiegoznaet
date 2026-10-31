package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

/**
 * Обёртка-Screen для SettingsMenu.
 * Нужна для открытия настроек из главного меню (MenuScreen),
 * где нет GameScreen со state machine.
 *
 * Использование:
 *   game.setScreen(new SettingsScreen(() -> game.setScreen(new MenuScreen(game))));
 */
public class SettingsScreen extends ScreenAdapter {
    private final SettingsMenu menu = new SettingsMenu();
    private final Runnable onBack;

    public SettingsScreen(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void show() {
        menu.init(onBack);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return menu.touchDown(screenX, screenY, pointer);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return menu.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return menu.touchUp(pointer);
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        menu.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        menu.resize(width, height);
    }

    @Override
    public void hide() {
        // на всякий — сохраняем если вышли без кнопки НАЗАД
        GameSettings.save();
    }

    @Override
    public void dispose() {
        menu.dispose();
    }
}