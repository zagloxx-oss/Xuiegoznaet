package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;

public class MenuScreen extends ScreenAdapter {
    private OrthographicCamera uiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private GlyphLayout layout;
    private Game game;

    private float time = 0f;
    private float menuFadeIn = 0f;

    private Button btnSingle, btnCreate, btnJoin, btnSettings, btnExit;
    private Button activeButton = null;

    private static class Button {
        String text;
        float x, y, w, h;
        Color baseColor;
        float pressAmount = 0f;
        boolean pressed = false;

        Button(String text, Color color) {
            this.text = text;
            this.baseColor = new Color(color);
        }
    }

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        // подгружаем актуальные настройки (например если вернулись с SettingsScreen)
        GameSettings.load();

        uiCam = new OrthographicCamera();
        uiCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        batch.setProjectionMatrix(uiCam.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(uiCam.combined);
        layout = new GlyphLayout();

        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ru.ttf"));

            // Русский алфавит для генератора
            String cyrillic = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

            // Шрифт для заголовка (английский)
            FreeTypeFontGenerator.FreeTypeFontParameter pTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pTitle.size = 120;
            pTitle.color = Color.WHITE;
            pTitle.borderWidth = 2f;
            pTitle.borderColor = new Color(0.8f, 0f, 0.8f, 1f);
            titleFont = generator.generateFont(pTitle);

            // Шрифт для кнопок (ДОБАВЛЕН РУССКИЙ АЛФАВИТ)
            FreeTypeFontGenerator.FreeTypeFontParameter pBtn = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pBtn.size = 50;
            pBtn.color = Color.WHITE;
            pBtn.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            buttonFont = generator.generateFont(pBtn);

            generator.dispose();
        } catch (Exception e) {
            titleFont = new BitmapFont();
            buttonFont = new BitmapFont();
            Gdx.app.error("FONT", "Error loading font: " + e.getMessage());
        }

        // Раскладка кнопок (5 кнопок теперь)
        float cx = Gdx.graphics.getWidth() / 2f;
        float btnW = Gdx.graphics.getWidth() * 0.55f;
        float btnH = Gdx.graphics.getHeight() * 0.085f;
        float gap = 18f;
        float startY = Gdx.graphics.getHeight() * 0.62f;

        btnSingle = new Button("ОДИНОЧНАЯ ИГРА", new Color(0.1f, 0.8f, 0.4f, 1f));
        btnSingle.x = cx - btnW / 2f; btnSingle.y = startY; btnSingle.w = btnW; btnSingle.h = btnH;

        btnCreate = new Button("СОЗДАТЬ ЛОББИ", new Color(0.2f, 0.5f, 1f, 1f));
        btnCreate.x = cx - btnW / 2f; btnCreate.y = startY - (btnH + gap); btnCreate.w = btnW; btnCreate.h = btnH;

        btnJoin = new Button("ПОДКЛЮЧИТЬСЯ", new Color(1f, 0.6f, 0.1f, 1f));
        btnJoin.x = cx - btnW / 2f; btnJoin.y = startY - (btnH + gap) * 2; btnJoin.w = btnW; btnJoin.h = btnH;

        btnSettings = new Button("НАСТРОЙКИ", new Color(0.6f, 0.25f, 1f, 1f));
        btnSettings.x = cx - btnW / 2f; btnSettings.y = startY - (btnH + gap) * 3; btnSettings.w = btnW; btnSettings.h = btnH;

        btnExit = new Button("ВЫХОД", new Color(0.8f, 0.1f, 0.1f, 1f));
        btnExit.x = cx - btnW / 2f; btnExit.y = startY - (btnH + gap) * 4; btnExit.w = btnW; btnExit.h = btnH;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float worldY = Gdx.graphics.getHeight() - screenY;
                Button[] btns = {btnSingle, btnCreate, btnJoin, btnSettings, btnExit};
                for (Button b : btns) {
                    if (isInside(b, screenX, worldY)) {
                        b.pressed = true;
                        activeButton = b;
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (activeButton != null) {
                    float worldY = Gdx.graphics.getHeight() - screenY;
                    if (isInside(activeButton, screenX, worldY)) {
                        Preferences prefs = Gdx.app.getPreferences("GamePrefs");
                        String savedName = prefs.getString("PlayerName", "");

                        if (activeButton == btnSingle) {
                            game.setScreen(new GameScreen(game, false, false, null));
                        } else if (activeButton == btnCreate) {
                            if (savedName.isEmpty()) {
                                game.setScreen(new ProfileScreen(game, true));
                            } else {
                                game.setScreen(new LobbyScreen(game, savedName, true, ""));
                            }
                        } else if (activeButton == btnJoin) {
                            if (savedName.isEmpty()) {
                                game.setScreen(new ProfileScreen(game, false));
                            } else {
                                Gdx.input.getTextInput(new TextInputListener() {
                                    @Override
                                    public void input(String ipText) {
                                        if (ipText != null && !ipText.trim().isEmpty()) {
                                            game.setScreen(new LobbyScreen(game, savedName, false, ipText.trim()));
                                        }
                                    }
                                    @Override
                                    public void canceled() {}
                                }, "IP сервера", "", "192.168.1.XXX");
                            }
                        } else if (activeButton == btnSettings) {
                            GameSettings.save();
                            game.setScreen(new SettingsScreen(
                                    () -> game.setScreen(new MenuScreen(game))
                            ));
                        } else if (activeButton == btnExit) {
                            GameSettings.save();
                            Gdx.app.exit();
                        }
                    }
                    activeButton.pressed = false;
                    activeButton = null;
                }
                return true;
            }
        });
    }

    private boolean isInside(Button b, float x, float y) {
        return x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h;
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        menuFadeIn = MathUtils.lerp(menuFadeIn, 1f, 3f * delta);
        float cx = Gdx.graphics.getWidth() / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Фон
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.2f, 0.4f, 0.2f);
        float gridSize = 100f;
        float offset = (time * 20f) % gridSize;
        for (float x = -offset; x < Gdx.graphics.getWidth(); x += gridSize) shapes.rect(x, 0, 2, Gdx.graphics.getHeight());
        for (float y = -offset; y < Gdx.graphics.getHeight(); y += gridSize) shapes.rect(0, y, Gdx.graphics.getWidth(), 2);

        for (int i = 0; i < 15; i++) {
            float px = (MathUtils.sin(time * 0.5f + i) * 0.5f + 0.5f) * Gdx.graphics.getWidth();
            float py = (MathUtils.cos(time * 0.3f + i * 2) * 0.5f + 0.5f) * Gdx.graphics.getHeight();
            float size = 3f + MathUtils.sin(time * 2f + i) * 2f;
            shapes.setColor(0.8f, 0.1f, 0.8f, 0.5f);
            shapes.circle(px, py, size, 8);
        }
        shapes.end();

        // Кнопки (Фон)
        Button[] btns = {btnSingle, btnCreate, btnJoin, btnSettings, btnExit};
        for (Button b : btns) {
            float target = b.pressed ? 1f : 0f;
            b.pressAmount = MathUtils.lerp(b.pressAmount, target, 15f * delta);
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Button b : btns) drawButtonBackground(b);
        shapes.end();

        // Кнопки (Рамки)
        Gdx.gl.glLineWidth(3f);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (Button b : btns) drawButtonBorder(b);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // Текст
        batch.begin();

        // Заголовок
        String title = "AWANGARD 3D";
        titleFont.getData().setScale(1f);
        layout.setText(titleFont, title);
        float tX = cx - layout.width / 2f;
        float tY = Gdx.graphics.getHeight() * 0.88f;
        float glitch = (MathUtils.sin(time * 10f) > 0.9f) ? MathUtils.random(-5f, 5f) : 0f;

        titleFont.setColor(1f, 0f, 0f, menuFadeIn * 0.7f); titleFont.draw(batch, title, tX - 3f + glitch, tY);
        titleFont.setColor(0f, 0.5f, 1f, menuFadeIn * 0.7f); titleFont.draw(batch, title, tX + 3f + glitch, tY);
        titleFont.setColor(1f, 1f, 1f, menuFadeIn); titleFont.draw(batch, title, tX, tY);

        // Текст на кнопках
        buttonFont.getData().setScale(1f);
        for (Button b : btns) {
            float press = b.pressAmount;
            buttonFont.getData().setScale(1f - press * 0.1f);

            layout.setText(buttonFont, b.text);
            float textX = b.x + b.w / 2f - layout.width / 2f;
            float textY = b.y + b.h / 2f + layout.height / 2f;

            buttonFont.setColor(0, 0, 0, 0.8f * menuFadeIn);
            buttonFont.draw(batch, layout, textX + 2, textY - 2);

            float r = MathUtils.lerp(1f, b.baseColor.r, press);
            float g = MathUtils.lerp(1f, b.baseColor.g, press);
            float bl = MathUtils.lerp(1f, b.baseColor.b, press);
            buttonFont.setColor(r, g, bl, menuFadeIn);
            buttonFont.draw(batch, layout, textX, textY);
        }

        batch.end();
    }

    private void drawButtonBackground(Button b) {
        float press = b.pressAmount;
        float w = b.w * (1f - press * 0.05f);
        float h = b.h * (1f - press * 0.05f);
        float x = b.x + (b.w - w) / 2f;
        float y = b.y + (b.h - h) / 2f;

        shapes.setColor(0.05f, 0.05f, 0.1f, 0.85f * menuFadeIn);
        shapes.rect(x, y, w, h);

        if (press > 0.01f) {
            shapes.setColor(b.baseColor.r, b.baseColor.g, b.baseColor.b, 0.3f * press);
            shapes.rect(x, y, w, h);
        }
        if (!b.pressed && (MathUtils.sin(time * 2f + b.y) > 0.8f)) {
            shapes.setColor(b.baseColor.r, b.baseColor.g, b.baseColor.b, 0.2f);
            shapes.rect(x, y + h * 0.8f, w, h * 0.2f);
        }
    }

    private void drawButtonBorder(Button b) {
        float press = b.pressAmount;
        float w = b.w * (1f - press * 0.05f);
        float h = b.h * (1f - press * 0.05f);
        float x = b.x + (b.w - w) / 2f;
        float y = b.y + (b.h - h) / 2f;

        float alpha = MathUtils.lerp(0.5f, 1f, press) * menuFadeIn;
        shapes.setColor(b.baseColor.r, b.baseColor.g, b.baseColor.b, alpha);
        float corner = 20f;

        shapes.line(x, y + h, x, y + h - corner); shapes.line(x, y + h, x + corner, y + h);
        shapes.line(x + w, y + h, x + w, y + h - corner); shapes.line(x + w, y + h, x + w - corner, y + h);
        shapes.line(x, y, x, y + corner); shapes.line(x, y, x + corner, y);
        shapes.line(x + w, y, x + w, y + corner); shapes.line(x + w, y, x + w - corner, y);
    }

    @Override
    public void resize(int width, int height) {
        uiCam.setToOrtho(false, width, height);
        batch.setProjectionMatrix(uiCam.combined);
        shapes.setProjectionMatrix(uiCam.combined);
    }

    @Override
    public void dispose() {
        batch.dispose(); shapes.dispose();
        titleFont.dispose(); buttonFont.dispose();
    }
}