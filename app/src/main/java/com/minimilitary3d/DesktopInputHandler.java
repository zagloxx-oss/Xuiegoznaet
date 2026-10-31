package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

public class DesktopInputHandler extends InputAdapter {
    private final GameScreen game;
    private boolean mouseCaptured = true;
    private Cursor invisibleCursor;

    public DesktopInputHandler(GameScreen game) {
        this.game = game;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.CLEAR);
        pm.fill();
        invisibleCursor = Gdx.graphics.newCursor(pm, 0, 0);
        pm.dispose();
        setCapture(true);
    }

    private void setCapture(boolean capture) {
        mouseCaptured = capture;
        Gdx.input.setCursorCatched(capture);
        if (capture) Gdx.graphics.setCursor(invisibleCursor); else Gdx.input.setCursorCatched(false);
    }

    public void update(float delta) {
        if (mouseCaptured) {
            float dx = Gdx.input.getDeltaX();
            float dy = Gdx.input.getDeltaY();
            if (dx != 0 || dy != 0) game.applyLookDelta(dx, dy);
        }
        float mx = 0f, my = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) my -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) my += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) mx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) mx += 1f;
        game.setMoveInput(mx, my);
        game.setJetpack(Gdx.input.isKeyPressed(Input.Keys.SPACE));
        game.setFiring(Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        game.setAiming(Gdx.input.isButtonPressed(Input.Buttons.RIGHT));
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.R: game.doReload(); return true;
            case Input.Keys.ESCAPE:
                if (mouseCaptured) setCapture(false);
                else { game.togglePause(); setCapture(!game.isPaused()); }
                return true;
            case Input.Keys.F11:
                try {
                    if (Gdx.graphics.isFullscreen()) {
                        Gdx.graphics.setWindowedMode(1280, 720);
                        GameSettings.fullscreen = false;
                    } else {
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                        GameSettings.fullscreen = true;
                    }
                } catch (Exception ignored) {}
                return true;
            case Input.Keys.F1: GameSettings.isDevMode = !GameSettings.isDevMode; return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int sx, int sy, int pointer, int button) {
        if (!mouseCaptured) { setCapture(true); return true; }
        return false;
    }

    public void dispose() {
        Gdx.input.setCursorCatched(false);
        if (invisibleCursor != null) invisibleCursor.dispose();
    }
}
