package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProfileScreen extends ScreenAdapter {
    private OrthographicCamera uiCam;
    private SpriteBatch batch;
    private BitmapFont font;
    private Game game;
    private boolean isHost;

    public ProfileScreen(Game game, boolean isHost) {
        this.game = game;
        this.isHost = isHost;
    }

    @Override
    public void show() {
        uiCam = new OrthographicCamera();
        uiCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        font = new BitmapFont();

        Gdx.input.getTextInput(new TextInputListener() {
            @Override
            public void input(String text) {
                if (text != null && !text.trim().isEmpty()) {
                    Gdx.app.getPreferences("GamePrefs").putString("PlayerName", text.trim()).flush();
                    
                    if (isHost) {
                        game.setScreen(new LobbyScreen(game, text.trim(), true, ""));
                    } else {
                        // Если это клиент, после имени просим IP
                        Gdx.input.getTextInput(new TextInputListener() {
                            @Override
                            public void input(String ipText) {
                                if (ipText != null && !ipText.trim().isEmpty()) {
                                    game.setScreen(new LobbyScreen(game, text.trim(), false, ipText.trim()));
                                }
                            }
                            @Override
                            public void canceled() {}
                        }, "Enter Server IP", "", "192.168.1.XXX");
                    }
                } else {
                    game.setScreen(new MenuScreen(game));
                }
            }

            @Override
            public void canceled() {
                game.setScreen(new MenuScreen(game));
            }
        }, "ENTER YOUR NICKNAME", "", "Player");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.01f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        font.getData().setScale(3f);
        font.setColor(Color.WHITE);
        font.draw(batch, "WAITING FOR INPUT...", Gdx.graphics.getWidth()/2f - 250, Gdx.graphics.getHeight()/2f);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose(); font.dispose();
    }
}