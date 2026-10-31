package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.utils.Array;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class LobbyScreen extends ScreenAdapter {
    private OrthographicCamera uiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    
    private BitmapFont titleFont;
    private BitmapFont btnFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Game game;
    private float time = 0f;

    private String myName;
    private boolean isHost;
    private String myIp;
    private String serverIp;

    private Array<PlayerData> players = new Array<>();
    private NetworkManager network;
    private float sendTimer = 0f;
    private String lobbyDebug = "";

    private Vector2 readyBtn = new Vector2();
    private Vector2 startBtn = new Vector2();
    private Vector2 backBtn = new Vector2();
    
    private boolean amIReady = false;
    private boolean isCountingDown = false;
    private float countdown = 5f;

    public LobbyScreen(Game game, String myName, boolean isHost, String ip) {
        this.game = game;
        this.myName = myName;
        this.isHost = isHost;
        this.serverIp = ip;
    }

    @Override
    public void show() {
        uiCam = new OrthographicCamera();
        uiCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        batch.setProjectionMatrix(uiCam.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(uiCam.combined);
        layout = new GlyphLayout();

        // Загрузка шрифтов
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ru.ttf"));
            String cyrillic = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

            FreeTypeFontGenerator.FreeTypeFontParameter pTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pTitle.size = 80; pTitle.color = Color.WHITE;
            titleFont = generator.generateFont(pTitle);

            FreeTypeFontGenerator.FreeTypeFontParameter pBtn = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pBtn.size = 50; pBtn.color = Color.WHITE;
            pBtn.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            btnFont = generator.generateFont(pBtn);

            FreeTypeFontGenerator.FreeTypeFontParameter pSmall = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pSmall.size = 35; pSmall.color = Color.WHITE;
            pSmall.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            smallFont = generator.generateFont(pSmall);

            generator.dispose();
        } catch (Exception e) {
            titleFont = new BitmapFont(); btnFont = new BitmapFont(); smallFont = new BitmapFont();
        }

        myIp = getLocalIpAddress();

        if (isHost && !myName.contains("(Host)")) {
            myName = myName + " (Host)";
        }

        network = new NetworkManager();
        try {
            if (isHost) {
                network.startServer();
                players.add(new PlayerData(myName, true));
            } else {
                network.connectToServer(serverIp); 
            }
        } catch (Exception e) {
            lobbyDebug = "CONNECT ERROR";
        }

        // Раскладка кнопок
        readyBtn.set(Gdx.graphics.getWidth() / 2f - 200, 80);
        startBtn.set(Gdx.graphics.getWidth() - 450, 80);
        backBtn.set(40, Gdx.graphics.getHeight() - 120);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float worldY = Gdx.graphics.getHeight() - screenY;

                if (screenX >= backBtn.x && screenX <= backBtn.x + 200 && worldY >= backBtn.y && worldY <= backBtn.y + 80) {
                    if (network != null) network.dispose();
                    game.setScreen(new MenuScreen(game));
                    return true;
                }

                if (screenX >= readyBtn.x && screenX <= readyBtn.x + 400 && worldY >= readyBtn.y && worldY <= readyBtn.y + 100) {
                    amIReady = !amIReady;
                    try {
                        if (isHost) {
                            if (players.size > 0) players.get(0).isReady = amIReady;
                        } else {
                            network.sendData("ME|" + myName + ":" + (amIReady ? "1" : "0"));
                        }
                    } catch (Exception e) {}
                    return true;
                }

                if (isHost && screenX >= startBtn.x && screenX <= startBtn.x + 400 && worldY >= startBtn.y && worldY <= startBtn.y + 100) {
                    if (allPlayersReady()) {
                        isCountingDown = true;
                    }
                    return true;
                }
                return true;
            }
        });
    }

    private boolean allPlayersReady() {
        if (players.size == 0) return false;
        for (PlayerData p : players) {
            if (!p.isReady) return false;
        }
        return true;
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ================= СЕТЕВАЯ ЛОГИКА =================
        if (network != null) {
            sendTimer -= delta;
            if (sendTimer <= 0 && !isCountingDown) {
                sendTimer = 0.1f; 
                try {
                    if (isHost) {
                        StringBuilder sb = new StringBuilder("LOBBY|");
                        for (int i = 0; i < players.size; i++) {
                            PlayerData p = players.get(i);
                            sb.append(p.name).append(":").append(p.isReady ? "1" : "0");
                            if (i < players.size - 1) sb.append(",");
                        }
                        network.sendData(sb.toString());
                        lobbyDebug = "SENT: " + sb.toString();
                    } else {
                        network.sendData("ME|" + myName + ":" + (amIReady ? "1" : "0"));
                        lobbyDebug = "SENT: ME";
                    }
                } catch (Exception e) {}
            }

            if (!network.lastReceivedData.isEmpty()) {
                try {
                    String data = network.lastReceivedData.trim();
                    lobbyDebug = "RECV: " + data;
                    
                    if (isHost && data.startsWith("ME|")) {
                        String[] parts = data.substring(3).split(":");
                        if (parts.length == 2) {
                            String pName = parts[0].trim();
                            boolean pReady = parts[1].trim().equals("1");
                            boolean found = false;
                            for (PlayerData p : players) {
                                if (p.name.equals(pName)) { p.isReady = pReady; found = true; break; }
                            }
                            if (!found && players.size < 4) {
                                PlayerData newP = new PlayerData(pName, false);
                                newP.isReady = pReady;
                                players.add(newP);
                            }
                        }
                    } 
                    else if (!isHost && data.startsWith("LOBBY|")) {
                        String[] pParts = data.substring(6).split(",");
                        Array<PlayerData> newPlayers = new Array<>();
                        for (String s : pParts) {
                            String[] split = s.split(":");
                            if (split.length == 2) {
                                PlayerData p = new PlayerData(split[0].trim(), false);
                                if (split[0].trim().equals(myName)) p.isReady = amIReady;
                                else p.isReady = split[1].trim().equals("1");
                                newPlayers.add(p);
                            }
                        }
                        if (newPlayers.size > 0) newPlayers.get(0).isHost = true;
                        players = newPlayers;
                    }
                    else if (!isHost && data.startsWith("START|")) {
                        game.setScreen(new GameScreen(game, false, true, serverIp, network));
                        return;
                    }
                } catch (Exception e) {}
            }
        }

        if (isCountingDown) {
            countdown -= delta;
            if (countdown <= 0) {
                if (isHost) {
                    for (int i = 0; i < 10; i++) {
                        try { network.sendData("START|"); } catch (Exception e) {}
                    }
                }
                game.setScreen(new GameScreen(game, isHost, true, "", network));
                return;
            }
        }

        // ================= ОТРИСОВКА ФОНА =================
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        
        // Движущаяся сетка
        shapes.setColor(0.1f, 0.2f, 0.4f, 0.15f);
        float gridSize = 100f;
        float offset = (time * 20f) % gridSize;
        for (float x = -offset; x < Gdx.graphics.getWidth(); x += gridSize) shapes.rect(x, 0, 2, Gdx.graphics.getHeight());
        for (float y = -offset; y < Gdx.graphics.getHeight(); y += gridSize) shapes.rect(0, y, Gdx.graphics.getWidth(), 2);

        // Частицы
        for (int i = 0; i < 15; i++) {
            float px = (MathUtils.sin(time * 0.5f + i) * 0.5f + 0.5f) * Gdx.graphics.getWidth();
            float py = (MathUtils.cos(time * 0.3f + i * 2) * 0.5f + 0.5f) * Gdx.graphics.getHeight();
            shapes.setColor(0.8f, 0.1f, 0.8f, 0.4f);
            shapes.circle(px, py, 3f, 8);
        }
        shapes.end();

        // ================= ОТРИСОВКА СЛОТОВ ИГРОКОВ =================
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float cellW = w * 0.45f;
        float cellH = h * 0.28f;

        // 1. Фоны слотов
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            int col = i % 2;
            int row = i / 2;
            float x = w * 0.025f + col * (cellW + w * 0.05f);
            float y = h * 0.6f - row * (cellH + h * 0.05f);

            // Темный стеклянный фон
            shapes.setColor(0.05f, 0.05f, 0.1f, 0.8f);
            shapes.rect(x, y, cellW, cellH);
        }
        shapes.end();

        // 2. Рамки слотов
        Gdx.gl.glLineWidth(3f);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < 4; i++) {
            int col = i % 2;
            int row = i / 2;
            float x = w * 0.025f + col * (cellW + w * 0.05f);
            float y = h * 0.6f - row * (cellH + h * 0.05f);

            PlayerData p = (i < players.size) ? players.get(i) : null;
            Color borderColor = new Color(0.2f, 0.2f, 0.2f, 1f);
            float pulse = 1f;

            if (p != null) {
                if (p.isReady) {
                    borderColor.set(0.1f, 1f, 0.3f, 1f); // Зеленый
                } else {
                    borderColor.set(1f, 0.2f, 0.2f, 1f); // Красный
                    pulse = MathUtils.sin(time * 5f) * 0.3f + 0.7f;
                }
            }

            shapes.setColor(borderColor.r, borderColor.g, borderColor.b, pulse);
            
            // HUD-скобки
            float c = 30f;
            shapes.line(x, y + cellH, x, y + cellH - c); shapes.line(x, y + cellH, x + c, y + cellH);
            shapes.line(x + cellW, y + cellH, x + cellW, y + cellH - c); shapes.line(x + cellW, y + cellH, x + cellW - c, y + cellH);
            shapes.line(x, y, x, y + c); shapes.line(x, y, x + c, y);
            shapes.line(x + cellW, y, x + cellW, y + c); shapes.line(x + cellW, y, x + cellW - c, y);
        }
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // ================= ОТРИСОВКА КНОПОК =================
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Подтвердить
        shapes.setColor(amIReady ? new Color(0.1f, 0.6f, 0.2f, 1f) : new Color(0.8f, 0.2f, 0.1f, 1f));
        shapes.rect(readyBtn.x, readyBtn.y, 400, 100);
        // Начать (только для хоста)
        if (isHost) {
            shapes.setColor(allPlayersReady() ? new Color(0.1f, 0.4f, 0.9f, 1f) : new Color(0.2f, 0.2f, 0.2f, 1f));
            shapes.rect(startBtn.x, startBtn.y, 400, 100);
        }
        // Назад
        shapes.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapes.rect(backBtn.x, backBtn.y, 200, 80);
        shapes.end();

        // ================= ОТРИСОВКА ТЕКСТА =================
        batch.begin();
        
        // Заголовок
        titleFont.getData().setScale(1f);
        layout.setText(titleFont, "ЛОББИ");
        float tX = w / 2f - layout.width / 2f;
        float tY = h * 0.92f;
        titleFont.setColor(1f, 1f, 1f, 1f);
        titleFont.draw(batch, layout, tX, tY);

        // Текст в слотах
        smallFont.getData().setScale(1f);
        for (int i = 0; i < 4; i++) {
            int col = i % 2;
            int row = i / 2;
            float x = w * 0.025f + col * (cellW + w * 0.05f);
            float y = h * 0.6f - row * (cellH + h * 0.05f);

            PlayerData p = (i < players.size) ? players.get(i) : null;

            if (p != null) {
                // Ник
                smallFont.getData().setScale(1.2f);
                smallFont.setColor(1f, 1f, 1f, 1f);
                smallFont.draw(batch, p.name, x + 30, y + cellH - 40);

                // Статус (Хост / Готов / Ожидание)
                smallFont.getData().setScale(0.8f);
                if (p.isHost) {
                    smallFont.setColor(0f, 1f, 1f, 1f); // Циан
                    smallFont.draw(batch, "ХОСТ", x + 30, y + cellH - 80);
                }
                
                smallFont.setColor(p.isReady ? Color.GREEN : Color.RED);
                smallFont.draw(batch, p.isReady ? "ГОТОВ" : "ОЖИДАНИЕ", x + 30, y + cellH - 110);
            } else {
                smallFont.getData().setScale(1f);
                smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
                smallFont.draw(batch, "ОЖИДАНИЕ ИГРОКА...", x + 30, y + cellH - 40);
            }
        }

        // Текст кнопок
        btnFont.getData().setScale(1f);
        
        // Confirm
        layout.setText(btnFont, amIReady ? "ПОДТВЕРЖДЕНО" : "ПОДТВЕРДИТЬ");
        btnFont.setColor(Color.WHITE);
        btnFont.draw(batch, layout, readyBtn.x + 200 - layout.width / 2f, readyBtn.y + 60);

        // Start
        if (isHost) {
            if (isCountingDown) {
                btnFont.getData().setScale(2f);
                layout.setText(btnFont, String.valueOf((int)Math.ceil(countdown)));
                btnFont.setColor(Color.WHITE);
                btnFont.draw(batch, layout, startBtn.x + 200 - layout.width / 2f, startBtn.y + 75);
            } else {
                btnFont.getData().setScale(1f);
                layout.setText(btnFont, "НАЧАТЬ");
                btnFont.setColor(Color.WHITE);
                btnFont.draw(batch, layout, startBtn.x + 200 - layout.width / 2f, startBtn.y + 60);
            }
        }

        // Back
        smallFont.getData().setScale(1f);
        layout.setText(smallFont, "НАЗАД");
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, layout, backBtn.x + 100 - layout.width / 2f, backBtn.y + 50);

        // IP адрес (внизу)
        smallFont.getData().setScale(0.8f);
        smallFont.setColor(0f, 1f, 1f, 0.7f);
        smallFont.draw(batch, "ВАШ IP: " + myIp, w - 450, 50);

        // Дебаг (маленьким шрифтом в углу)
        smallFont.getData().setScale(0.5f);
        smallFont.setColor(1f, 1f, 0f, 0.4f);
        smallFont.draw(batch, lobbyDebug, 20, 20);

        batch.end();
    }

    private String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) { }
        return "127.0.0.1";
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
        titleFont.dispose(); btnFont.dispose(); smallFont.dispose();
    }
}