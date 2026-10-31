package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class HudRenderer {
    private OrthographicCamera uiCamera;
    private SpriteBatch batch;
    private ShapeRenderer uiRenderer;

    private BitmapFont titleFont;
    private BitmapFont ammoFont;
    private BitmapFont textFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    private Texture texFire, texReload;
    private boolean showHitmarker = false;
    private float hitmarkerTimer = 0f;
    private float time = 0f;

    public final Vector2 stopBtn = new Vector2();
    public final Vector2 settingsBtn = new Vector2();
    public final Vector2 fireBtn = new Vector2();
    public final Vector2 reloadBtn = new Vector2();
    public final Vector2 aimBtn = new Vector2();
    public final Vector2 bombBtn = new Vector2();
    public final Vector2 jetpackBtn = new Vector2();

    public float fireRadius = 100f;
    public float subRadius = 65f;
    public float topBtnSize = 90f;

    public final Vector2 resumeBtn = new Vector2();
    public final Vector2 pauseSettingsBtn = new Vector2();
    public final Vector2 exitBtn = new Vector2();

    public void init() {
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        batch.setProjectionMatrix(uiCamera.combined);

        uiRenderer = new ShapeRenderer();
        uiRenderer.setProjectionMatrix(uiCamera.combined);

        layout = new GlyphLayout();

        texFire = makeBlackTransparent("interface/bom.png");
        texReload = makeBlackTransparent("interface/reload.png");

        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ru.ttf"));
            String cyrillic = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

            FreeTypeFontGenerator.FreeTypeFontParameter pTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pTitle.size = 70; pTitle.color = Color.WHITE;
            pTitle.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            pTitle.borderWidth = 2f; pTitle.borderColor = new Color(0, 0, 0, 0.8f);
            titleFont = generator.generateFont(pTitle);

            FreeTypeFontGenerator.FreeTypeFontParameter pAmmo = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pAmmo.size = 90; pAmmo.color = Color.CYAN;
            pAmmo.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
            pAmmo.borderWidth = 3f; pAmmo.borderColor = new Color(0, 0.4f, 0.5f, 1f);
            pAmmo.shadowOffsetX = 3; pAmmo.shadowOffsetY = 3; pAmmo.shadowColor = new Color(0, 0, 0, 0.7f);
            ammoFont = generator.generateFont(pAmmo);

            FreeTypeFontGenerator.FreeTypeFontParameter pText = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pText.size = 40; pText.color = Color.WHITE;
            pText.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            pText.borderWidth = 1.5f; pText.borderColor = new Color(0, 0, 0, 0.7f);
            textFont = generator.generateFont(pText);

            FreeTypeFontGenerator.FreeTypeFontParameter pSmall = new FreeTypeFontGenerator.FreeTypeFontParameter();
            pSmall.size = 28; pSmall.color = Color.WHITE;
            pSmall.characters = FreeTypeFontGenerator.DEFAULT_CHARS + cyrillic;
            pSmall.borderWidth = 1.2f; pSmall.borderColor = new Color(0, 0, 0, 0.7f);
            smallFont = generator.generateFont(pSmall);

            generator.dispose();
        } catch (Exception e) {
            titleFont = new BitmapFont(); ammoFont = new BitmapFont(); textFont = new BitmapFont(); smallFont = new BitmapFont();
        }

        updatePositions();
    }

    public void updatePositions() {
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        stopBtn.set(30, h - 120);
        settingsBtn.set(w - 120, h - 120);
        fireBtn.set(w - 160, 160);
        aimBtn.set(w - 350, 180);
        reloadBtn.set(w - 160, 350);
        bombBtn.set(w / 2f, 150);
        jetpackBtn.set(w - 350, 350);
        float menuW = 400;
        float startY = h / 2f + 100;
        resumeBtn.set(w/2f - menuW/2f, startY);
        pauseSettingsBtn.set(w/2f - menuW/2f, startY - 120);
        exitBtn.set(w/2f - menuW/2f, startY - 240);
    }

    private Texture makeBlackTransparent(String path) {
        Pixmap original = new Pixmap(Gdx.files.internal(path));
        Pixmap transparent = new Pixmap(original.getWidth(), original.getHeight(), Pixmap.Format.RGBA8888);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int color = original.getPixel(x, y);
                int r = (color >> 24) & 0xFF, g = (color >> 16) & 0xFF, b = (color >> 8) & 0xFF;
                if (r < 15 && g < 15 && b < 15) transparent.drawPixel(x, y, 0);
                else transparent.drawPixel(x, y, color);
            }
        }
        Texture tex = new Texture(transparent);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        original.dispose(); transparent.dispose();
        return tex;
    }

    public void triggerHitmarker() {
        showHitmarker = true;
        hitmarkerTimer = 0.12f;
    }

    private Color getEspColor() {
        switch (GameSettings.espColor) {
            case 0: return new Color(1f, 0.2f, 0.2f, 1f);
            case 1: return new Color(0.2f, 1f, 0.3f, 1f);
            case 2: return new Color(0.2f, 1f, 1f, 1f);
            case 3: return new Color(1f, 0.9f, 0.2f, 1f);
            case 4: return new Color(1f, 0.3f, 0.9f, 1f);
            default: return Color.RED;
        }
    }

    private Color getCrosshairColor() {
        switch (GameSettings.crosshairColor) {
            case 0: return Color.WHITE;
            case 1: return new Color(0.2f, 1f, 0.3f, 1f);
            case 2: return new Color(1f, 0.2f, 0.2f, 1f);
            case 3: return new Color(1f, 1f, 0.2f, 1f);
            case 4: return new Color(0.2f, 1f, 1f, 1f);
            case 5: return new Color(1f, 0.3f, 0.9f, 1f);
            default: return Color.WHITE;
        }
    }

    // =========================================================
    //              RENDER (с ботами)
    // =========================================================
    public void render(int ammo, int maxAmmo, int hp, int maxHP, int armor, int maxArmor,
                       boolean joyActive, Vector2 joyCenter, Vector2 joyKnob, float joyRad, float joyKnobRadius,
                       float spread, boolean isAiming, String status, float statusTimer,
                       GameScreen.State state, Vector3[] targets, boolean[] targetActive, PerspectiveCamera camera, String netStatus,
                       boolean canPlant, boolean canDefuse, boolean bombActive, float bombTimer, boolean isPlanting, float plantTimer, boolean isDefusing, float defuseTimer,
                       Vector3 enemyPos, boolean enemyAlive,
                       float jetpackFuel, float jetpackMaxFuel, boolean jetpackActive,
                       Bot[] bots) {

        time += Gdx.graphics.getDeltaTime();
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float cx = w / 2f, cy = h / 2f;

        float pulse = 0.5f + 0.5f * MathUtils.sin(time * 3f);
        float fastPulse = 0.5f + 0.5f * MathUtils.sin(time * 9f);

        if (hitmarkerTimer > 0) {
            hitmarkerTimer -= Gdx.graphics.getDeltaTime();
            if (hitmarkerTimer <= 0) showHitmarker = false;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        uiCamera.update();
        uiRenderer.setProjectionMatrix(uiCamera.combined);

        // === ESP для мультиплеерного врага ===
        if (GameSettings.esp && state == GameScreen.State.PLAYING && enemyAlive) {
            Vector3 proj = new Vector3(enemyPos.x, enemyPos.y + 1.0f, enemyPos.z);
            camera.project(proj);
            if (proj.z > 0 && proj.z < 1) {
                Color ec = getEspColor();
                uiRenderer.begin(ShapeRenderer.ShapeType.Line);
                uiRenderer.setColor(ec.r, ec.g, ec.b, 0.25f);
                Gdx.gl.glLineWidth(8f);
                uiRenderer.line(cx, h, proj.x, proj.y);
                uiRenderer.setColor(ec.r, ec.g, ec.b, 0.7f);
                Gdx.gl.glLineWidth(3f);
                uiRenderer.line(cx, h, proj.x, proj.y);

                if (GameSettings.showEspBox) {
                    float boxSize = (150f / (proj.z * 2f)) * GameSettings.espBoxScale;
                    if (boxSize > 300f) boxSize = 300f;
                    uiRenderer.setColor(ec.r, ec.g, ec.b, 0.35f);
                    Gdx.gl.glLineWidth(6f);
                    uiRenderer.rect(proj.x - boxSize/2, proj.y - boxSize/2, boxSize, boxSize);
                    uiRenderer.setColor(ec.r, ec.g, ec.b, 0.95f);
                    Gdx.gl.glLineWidth(2f);
                    uiRenderer.rect(proj.x - boxSize/2, proj.y - boxSize/2, boxSize, boxSize);
                }
                Gdx.gl.glLineWidth(1f);
                uiRenderer.end();
            }
        }

        // === ESP для ботов (одиночный режим) ===
        if (GameSettings.esp && state == GameScreen.State.PLAYING && bots != null) {
            Color ec = getEspColor();
            uiRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (Bot bot : bots) {
                if (bot == null || bot.dead) continue;

                float botY = bot.pos.y + bot.yOffset;
                // Голова
                Vector3 headProj = new Vector3(bot.pos.x, botY + 1.9f, bot.pos.z);
                camera.project(headProj);
                // Ноги
                Vector3 feetProj = new Vector3(bot.pos.x, botY, bot.pos.z);
                camera.project(feetProj);

                if (headProj.z <= 0 || headProj.z >= 1) continue;
                if (feetProj.z <= 0 || feetProj.z >= 1) continue;

                float boxHeight = Math.abs(headProj.y - feetProj.y);
                if (boxHeight < 5f) continue;
                float boxWidth = boxHeight * 0.55f * GameSettings.espBoxScale;
                boxHeight *= GameSettings.espBoxScale;

                float boxX = headProj.x - boxWidth / 2f;
                float boxY = feetProj.y;

                // Линия от низа экрана
                uiRenderer.setColor(ec.r, ec.g, ec.b, 0.25f);
                Gdx.gl.glLineWidth(6f);
                uiRenderer.line(cx, 0, headProj.x, headProj.y);
                uiRenderer.setColor(ec.r, ec.g, ec.b, 0.55f);
                Gdx.gl.glLineWidth(2f);
                uiRenderer.line(cx, 0, headProj.x, headProj.y);

                // Бокс
                if (GameSettings.showEspBox) {
                    uiRenderer.setColor(ec.r, ec.g, ec.b, 0.35f);
                    Gdx.gl.glLineWidth(5f);
                    uiRenderer.rect(boxX, boxY, boxWidth, boxHeight);
                    uiRenderer.setColor(ec.r, ec.g, ec.b, 0.95f);
                    Gdx.gl.glLineWidth(2f);
                    uiRenderer.rect(boxX, boxY, boxWidth, boxHeight);
                }

                // HP-бар над головой
                if (GameSettings.showEspHealth) {
                    float barH = 6f;
                    float barY = boxY + boxHeight + 8f;
                    float hpPct = MathUtils.clamp(bot.hp / 100f, 0f, 1f);

                    uiRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
                    uiRenderer.rect(boxX - 1, barY - 1, boxWidth + 2, barH + 2);

                    Color hpColor = hpPct > 0.6f ? new Color(0.2f, 1f, 0.3f, 1f)
                                   : hpPct > 0.3f ? new Color(1f, 0.8f, 0.2f, 1f)
                                   : new Color(1f, 0.2f, 0.2f, 1f);

                    uiRenderer.setColor(hpColor);
                    uiRenderer.rect(boxX, barY, boxWidth * hpPct, barH);
                }
            }
            Gdx.gl.glLineWidth(1f);
            uiRenderer.end();
        }

        uiRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (state == GameScreen.State.PLAYING) {
            if (GameSettings.vignette) {
                drawVignette(w, h, GameSettings.vignetteAmount);
            }

            // === ДЖОЙСТИК ===
            if (joyActive) {
                float jScale = GameSettings.joystickSize;
                float jOp = GameSettings.joystickOpacity;
                float jR = joyRad * jScale;
                float jKR = joyKnobRadius * jScale;

                glowCircle(joyCenter.x, joyCenter.y, jR * 1.15f, new Color(0.2f, 0.6f, 1f, 0.35f * jOp), 4);
                uiRenderer.setColor(0f, 0f, 0f, 0.35f * jOp);
                uiRenderer.circle(joyCenter.x, joyCenter.y, jR, 48);
                uiRenderer.setColor(0.15f, 0.45f, 0.9f, 0.15f * jOp);
                uiRenderer.circle(joyCenter.x, joyCenter.y, jR * 0.85f, 40);
                drawRingDots(joyCenter.x, joyCenter.y, jR, 3f,
                        new Color(0.3f, 0.85f, 1f, (0.5f + pulse * 0.3f) * jOp), 32);

                float kx = joyCenter.x + joyKnob.x * jScale;
                float ky = joyCenter.y + joyKnob.y * jScale;
                glowCircle(kx, ky, jKR * 1.3f, new Color(0.4f, 0.8f, 1f, 0.5f * jOp), 3);
                uiRenderer.setColor(0.15f, 0.4f, 0.85f, 0.95f * jOp);
                uiRenderer.circle(kx, ky, jKR, 32);
                uiRenderer.setColor(0.5f, 0.85f, 1f, 0.7f * jOp);
                uiRenderer.circle(kx, ky, jKR * 0.6f, 24);
                uiRenderer.setColor(1f, 1f, 1f, 0.9f * jOp);
                uiRenderer.circle(kx, ky, jKR * 0.25f, 16);

                if (joyKnob.len() > 5) {
                    float ang = joyKnob.angleDeg();
                    for (int i = 0; i < 3; i++) {
                        float t = i / 3f;
                        float ax = kx + MathUtils.cosDeg(ang) * (t * 10f * jScale);
                        float ay = ky + MathUtils.sinDeg(ang) * (t * 10f * jScale);
                        uiRenderer.setColor(1f, 1f, 1f, (0.9f - t * 0.6f) * jOp);
                        uiRenderer.circle(ax, ay, 3f - t, 8);
                    }
                }
            }

            // === HP / ARMOR ===
            float barW = 300, barH = 22;
            float barX = 40, barY = 80;

            if (GameSettings.showHp) {
                float hpPercent = hp / (float) maxHP;
                Color hpLow = new Color(1f, 0.1f, 0.15f, 1f);
                Color hpHigh = new Color(0.2f, 1f, 0.4f, 1f);
                Color hpCol = new Color(
                        hpLow.r + (hpHigh.r - hpLow.r) * hpPercent,
                        hpLow.g + (hpHigh.g - hpLow.g) * hpPercent,
                        hpLow.b + (hpHigh.b - hpLow.b) * hpPercent,
                        1f
                );
                if (hpPercent < 0.3f) {
                    glowRect(barX, barY, barW, barH, new Color(1f, 0.2f, 0.2f, 0.4f + fastPulse * 0.4f));
                } else {
                    glowRect(barX, barY, barW, barH, new Color(hpCol.r, hpCol.g, hpCol.b, 0.3f));
                }
                drawGradientBar(barX, barY, barW, barH, hpPercent, hpLow, hpCol);
            }

            if (GameSettings.showArmor && armor > 0) {
                float armorPercent = armor / (float) maxArmor;
                Color armCol = new Color(0.2f, 0.65f, 1f, 1f);
                float ay = GameSettings.showHp ? barY - 32 : barY;
                glowRect(barX, ay, barW, barH - 6, new Color(0.2f, 0.65f, 1f, 0.3f));
                drawGradientBar(barX, ay, barW, barH - 6, armorPercent,
                        new Color(0.1f, 0.35f, 0.8f, 1f), armCol);
            }

            // === КНОПКИ HUD ===
            float bs = GameSettings.buttonScale;
            float bOp = GameSettings.buttonOpacity;
            float fireR = fireRadius * bs;
            float subR = subRadius * bs;
            float topSize = topBtnSize * bs;

            drawJuicySquareButton(stopBtn, topSize, new Color(1f, 0.25f, 0.25f, bOp), pulse);
            drawJuicySquareButton(settingsBtn, topSize, new Color(0.25f, 0.6f, 1f, bOp), pulse);

            drawJuicyCircleButton(fireBtn, fireR, new Color(1f, 0.15f, 0.2f, bOp), fastPulse, true);
            drawJuicyCircleButton(aimBtn, subR,
                    isAiming ? new Color(1f, 0.9f, 0.2f, bOp) : new Color(0.85f, 0.85f, 0.9f, bOp * 0.85f), pulse, false);
            drawJuicyCircleButton(reloadBtn, subR, new Color(0.15f, 0.7f, 1f, bOp), pulse, false);

            if (canPlant && !bombActive) drawJuicyCircleButton(bombBtn, 70f * bs, new Color(1f, 0.55f, 0f, bOp), fastPulse, false);
            if (canDefuse && bombActive) drawJuicyCircleButton(bombBtn, 70f * bs, new Color(0.1f, 0.55f, 1f, bOp), fastPulse, false);

            // === ДЖЕТПАК ===
            {
                float jr = subR;
                float fuelPct = jetpackMaxFuel > 0f ? jetpackFuel / jetpackMaxFuel : 0f;

                Color jColor;
                if (jetpackActive && fuelPct > 0f) {
                    jColor = new Color(0.2f, 1f, 0.9f, bOp);
                } else if (fuelPct <= 0.01f) {
                    jColor = new Color(0.4f, 0.4f, 0.45f, bOp * 0.7f);
                } else {
                    jColor = new Color(0.3f, 0.7f, 1f, bOp);
                }

                drawJuicyCircleButton(jetpackBtn, jr, jColor, jetpackActive ? fastPulse : pulse, false);

                int segs = 32;
                int filled = (int)(segs * fuelPct);
                for (int i = 0; i < segs; i++) {
                    float a = -90f + 360f * i / segs;
                    float px = jetpackBtn.x + MathUtils.cosDeg(a) * (jr + 14f);
                    float py = jetpackBtn.y + MathUtils.sinDeg(a) * (jr + 14f);
                    if (i < filled) {
                        float t = fuelPct;
                        float r = 1f - t;
                        float g = t;
                        uiRenderer.setColor(r, g, 0.3f, 0.95f * bOp);
                    } else {
                        uiRenderer.setColor(0.15f, 0.15f, 0.2f, 0.5f * bOp);
                    }
                    uiRenderer.circle(px, py, 4f, 8);
                }
            }

            drawCrosshair(cx, cy, spread, isAiming, pulse);

            if (GameSettings.showHitmarker && showHitmarker) {
                float t = 1f - (hitmarkerTimer / 0.12f);
                float ringR = 18f + t * 45f;
                float a = 1f - t;
                drawRingDots(cx, cy, ringR, 4f * (1f - t * 0.5f), new Color(1f, 0.15f, 0.15f, a), 24);
                float s = 22f, th = 4f;
                uiRenderer.setColor(1f, 1f, 1f, a);
                uiRenderer.rect(cx - s, cy - th/2, s * 0.45f, th);
                uiRenderer.rect(cx + s * 0.55f, cy - th/2, s * 0.45f, th);
                uiRenderer.rect(cx - th/2, cy - s, th, s * 0.45f);
                uiRenderer.rect(cx - th/2, cy + s * 0.55f, th, s * 0.45f);
            }

            if (Math.abs(GameSettings.brightness) > 0.01f) {
                if (GameSettings.brightness > 0) {
                    uiRenderer.setColor(1f, 1f, 1f, Math.min(0.5f, GameSettings.brightness));
                } else {
                    uiRenderer.setColor(0f, 0f, 0f, Math.min(0.5f, -GameSettings.brightness));
                }
                uiRenderer.rect(0, 0, w, h);
            }

        } else if (state == GameScreen.State.PAUSED) {
            if (GameSettings.vignette) drawVignette(w, h, GameSettings.vignetteAmount);
            uiRenderer.setColor(0f, 0f, 0f, 0.82f); uiRenderer.rect(0, 0, w, h);

            float pw = 520, ph = 620;
            float px = w/2f - pw/2f, py = h/2f - ph/2f;
            glowRect(px, py, pw, ph, new Color(0.2f, 0.5f, 1f, 0.5f));
            uiRenderer.setColor(0.04f, 0.05f, 0.1f, 0.95f);
            roundedRect(px, py, pw, ph, 24f);
            uiRenderer.setColor(0.2f, 0.55f, 1f, 0.6f + pulse * 0.3f);
            drawBorderRect(px, py, pw, ph, 24f, 3f);

            drawCornerAccent(px, py + ph, 40f, new Color(0.3f, 0.8f, 1f, 1f), 1, -1);
            drawCornerAccent(px + pw, py + ph, 40f, new Color(0.3f, 0.8f, 1f, 1f), -1, -1);
            drawCornerAccent(px, py, 40f, new Color(0.3f, 0.8f, 1f, 1f), 1, 1);
            drawCornerAccent(px + pw, py, 40f, new Color(0.3f, 0.8f, 1f, 1f), -1, 1);

            drawJuicyMenuButton(resumeBtn, 400, 100, new Color(0.15f, 1f, 0.35f, 1f), pulse);
            drawJuicyMenuButton(pauseSettingsBtn, 400, 100, new Color(0.25f, 0.6f, 1f, 1f), pulse);
            drawJuicyMenuButton(exitBtn, 400, 100, new Color(1f, 0.2f, 0.25f, 1f), pulse);
        }

        uiRenderer.end();

        // === ТЕКСТ ===
        batch.begin();

        if (state == GameScreen.State.PLAYING) {
            float bs = GameSettings.buttonScale;
            float topSize = topBtnSize * bs;
            float subR = subRadius * bs;
            float fireR = fireRadius * bs;

            smallFont.setColor(Color.WHITE);
            layout.setText(smallFont, "||");
            smallFont.draw(batch, layout, stopBtn.x + topSize/2 - layout.width/2, stopBtn.y + topSize/2 + layout.height/2);

            layout.setText(smallFont, "O");
            smallFont.draw(batch, layout, settingsBtn.x + topSize/2 - layout.width/2, settingsBtn.y + topSize/2 + layout.height/2);

            if (texFire != null) {
                batch.draw(texFire, fireBtn.x - fireR * 0.8f, fireBtn.y - fireR * 0.8f, fireR * 1.6f, fireR * 1.6f);
            }
            if (texReload != null) {
                batch.draw(texReload, reloadBtn.x - subR * 0.7f, reloadBtn.y - subR * 0.7f, subR * 1.4f, subR * 1.4f);
            }
            smallFont.setColor(Color.WHITE);
            layout.setText(smallFont, "AIM");
            smallFont.draw(batch, layout, aimBtn.x - layout.width/2, aimBtn.y + layout.height/2);

            {
                float fuelPct = jetpackMaxFuel > 0f ? jetpackFuel / jetpackMaxFuel : 0f;
                String jText = fuelPct <= 0.01f ? "—" : "JET";
                smallFont.setColor(fuelPct > 0.01f ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
                layout.setText(smallFont, jText);
                smallFont.draw(batch, layout, jetpackBtn.x - layout.width/2, jetpackBtn.y + layout.height/2);

                smallFont.setColor(0.6f, 0.9f, 1f, 0.9f);
                String sec = String.format("%.0fс", jetpackFuel);
                layout.setText(smallFont, sec);
                smallFont.draw(batch, layout, jetpackBtn.x - layout.width/2, jetpackBtn.y - subR - 20f);
            }

            if (GameSettings.showAmmo) {
                ammoFont.setColor(Color.CYAN);
                layout.setText(ammoFont, String.valueOf(ammo));
                ammoFont.draw(batch, layout, w - 150 - layout.width, 80);
                smallFont.setColor(Color.GRAY);
                smallFont.draw(batch, "/ " + maxAmmo, w - 130, 80);
            }

            if (GameSettings.showFps) {
                smallFont.setColor(Color.YELLOW);
                smallFont.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 40, h - 150);
            }

            if (GameSettings.showNetStats || GameSettings.showPing) {
                smallFont.setColor(0.4f, 1f, 0.6f, 1f);
                smallFont.draw(batch, netStatus, 40, h - 180);
            }

            if (bombActive) {
                titleFont.setColor(1f, 0.25f, 0.25f, 1f);
                layout.setText(titleFont, "БОМБА: " + (int) bombTimer);
                titleFont.draw(batch, layout, cx - layout.width/2f, h - 120);
            }

            if (canPlant && !bombActive) {
                smallFont.setColor(Color.WHITE);
                layout.setText(smallFont, isPlanting ? "" + (int) Math.ceil(plantTimer) : "PLANT");
                smallFont.draw(batch, layout, bombBtn.x - layout.width/2f, bombBtn.y - 50);
            }
            if (canDefuse && bombActive) {
                smallFont.setColor(Color.WHITE);
                layout.setText(smallFont, isDefusing ? "" + (int) Math.ceil(defuseTimer) : "DEFUSE");
                smallFont.draw(batch, layout, bombBtn.x - layout.width/2f, bombBtn.y - 50);
            }

        } else if (state == GameScreen.State.PAUSED) {
            titleFont.setColor(0.6f, 0.9f, 1f, 1f);
            layout.setText(titleFont, "ПАУЗА");
            titleFont.draw(batch, layout, cx - layout.width/2f, h/2f + 250);

            textFont.setColor(Color.WHITE);
            layout.setText(textFont, "ПРОДОЛЖИТЬ");
            textFont.draw(batch, layout, resumeBtn.x + 200 - layout.width/2f, resumeBtn.y + 60);

            layout.setText(textFont, "НАСТРОЙКИ");
            textFont.draw(batch, layout, pauseSettingsBtn.x + 200 - layout.width/2f, pauseSettingsBtn.y + 60);

            layout.setText(textFont, "ВЫЙТИ В МЕНЮ");
            textFont.draw(batch, layout, exitBtn.x + 200 - layout.width/2f, exitBtn.y + 60);
        }

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCrosshair(float cx, float cy, float spread, boolean isAiming, float pulse) {
        float sizeMul = GameSettings.crosshairSize;
        float opacity = GameSettings.crosshairOpacity;
        Color cc = getCrosshairColor();

        if (isAiming) {
            float rOuter = 180f + pulse * 8f;
            for (int i = 0; i < 20; i++) {
                float a = i / 20f;
                uiRenderer.setColor(0f, 0f, 0f, 0.35f * a * opacity);
                float rr = rOuter + (20 - i) * 12f;
                uiRenderer.circle(cx, cy, rr, 48);
            }
            drawRingDots(cx, cy, rOuter, 3f, new Color(cc.r, cc.g, cc.b, 0.5f * opacity), 48);
            drawRingDots(cx, cy, rOuter + 10f, 2f, new Color(cc.r, cc.g, cc.b, 0.35f * opacity), 48);

            for (int i = 0; i < 8; i++) {
                float ang = i * 45f;
                for (int j = 0; j < 4; j++) {
                    float dist = rOuter - 12f + j * 6f;
                    float px = cx + MathUtils.cosDeg(ang) * dist;
                    float py = cy + MathUtils.sinDeg(ang) * dist;
                    uiRenderer.setColor(cc.r, cc.g, cc.b, (0.9f - j * 0.15f) * opacity);
                    uiRenderer.circle(px, py, 2.2f, 6);
                }
            }

            glowCircle(cx, cy, 12f, new Color(cc.r, cc.g, cc.b, 0.6f * opacity), 3);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.circle(cx, cy, 5f, 16);
            uiRenderer.setColor(1f, 1f, 1f, opacity);
            uiRenderer.circle(cx, cy, 2f, 12);
            return;
        }

        int style = GameSettings.crosshairStyle;
        if (style == 1) {
            glowCircle(cx, cy, 8f * sizeMul, new Color(cc.r, cc.g, cc.b, 0.7f * opacity), 3);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.circle(cx, cy, 4f * sizeMul, 16);
            uiRenderer.setColor(1f, 1f, 1f, opacity);
            uiRenderer.circle(cx, cy, 2f * sizeMul, 12);
        } else if (style == 2) {
            drawRingDots(cx, cy, 22f * sizeMul, 2.5f * sizeMul, new Color(cc.r, cc.g, cc.b, opacity), 24);
            glowCircle(cx, cy, 5f * sizeMul, new Color(cc.r, cc.g, cc.b, 0.7f * opacity), 2);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.circle(cx, cy, 2.5f * sizeMul, 12);
        } else if (style == 3) {
            float gap = (8f + spread * 2200f) * sizeMul;
            float lineLen = 18f * sizeMul;
            float thick = 3f * sizeMul;
            float chroma = 1.2f + pulse * 0.8f;

            uiRenderer.setColor(1f, 0.15f, 0.15f, 0.35f * opacity);
            uiRenderer.rect(cx - thick/2 + chroma, cy + gap, thick, lineLen);
            uiRenderer.rect(cx - thick/2 + chroma, cy - gap - lineLen, thick, lineLen);
            uiRenderer.rect(cx - gap - lineLen, cy - thick/2 + chroma, lineLen, thick);
            uiRenderer.rect(cx + gap, cy - thick/2 + chroma, lineLen, thick);

            uiRenderer.setColor(0.15f, 0.5f, 1f, 0.35f * opacity);
            uiRenderer.rect(cx - thick/2 - chroma, cy + gap, thick, lineLen);
            uiRenderer.rect(cx - thick/2 - chroma, cy - gap - lineLen, thick, lineLen);
            uiRenderer.rect(cx - gap - lineLen, cy - thick/2 - chroma, lineLen, thick);
            uiRenderer.rect(cx + gap, cy - thick/2 - chroma, lineLen, thick);

            for (int i = 0; i < 8; i++) {
                float t = i / 8f;
                float segH = lineLen / 8f;
                uiRenderer.setColor(cc.r, cc.g, cc.b, opacity * (0.4f + t * 0.6f));
                uiRenderer.rect(cx - thick/2, cy + gap + t * lineLen, thick, segH + 1);
                uiRenderer.rect(cx - thick/2, cy - gap - t * lineLen - segH, thick, segH + 1);
                uiRenderer.rect(cx - gap - t * lineLen - segH, cy - thick/2, segH + 1, thick);
                uiRenderer.rect(cx + gap + t * lineLen, cy - thick/2, segH + 1, thick);
            }

            float rot = time * 60f;
            for (int i = 0; i < 8; i++) {
                float a = rot + i * 360f / 8;
                float dist = (34f + pulse * 3f) * sizeMul;
                float px = cx + MathUtils.cosDeg(a) * dist;
                float py = cy + MathUtils.sinDeg(a) * dist;
                float da = (0.4f + pulse * 0.5f) * opacity;
                uiRenderer.setColor(cc.r, cc.g, cc.b, da);
                uiRenderer.circle(px, py, 2.5f, 8);
            }

            glowCircle(cx, cy, 6f, new Color(cc.r, cc.g, cc.b, 0.7f * opacity), 3);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.circle(cx, cy, 3.5f, 16);
            uiRenderer.setColor(1f, 1f, 1f, opacity);
            uiRenderer.circle(cx, cy, 1.5f, 12);
        } else {
            float gap = (6f + spread * 2200f) * sizeMul;
            float lineLen = 16f * sizeMul;
            float thick = 3f * sizeMul;
            uiRenderer.setColor(0f, 0f, 0f, 0.5f * opacity);
            uiRenderer.rect(cx - thick/2 - 1, cy + gap - 1, thick + 2, lineLen + 2);
            uiRenderer.rect(cx - thick/2 - 1, cy - gap - lineLen - 1, thick + 2, lineLen + 2);
            uiRenderer.rect(cx - gap - lineLen - 1, cy - thick/2 - 1, lineLen + 2, thick + 2);
            uiRenderer.rect(cx + gap - 1, cy - thick/2 - 1, lineLen + 2, thick + 2);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.rect(cx - thick/2, cy + gap, thick, lineLen);
            uiRenderer.rect(cx - thick/2, cy - gap - lineLen, thick, lineLen);
            uiRenderer.rect(cx - gap - lineLen, cy - thick/2, lineLen, thick);
            uiRenderer.rect(cx + gap, cy - thick/2, lineLen, thick);

            glowCircle(cx, cy, 5f, new Color(cc.r, cc.g, cc.b, 0.7f * opacity), 2);
            uiRenderer.setColor(cc.r, cc.g, cc.b, opacity);
            uiRenderer.circle(cx, cy, 2.2f * sizeMul, 12);
        }
    }

    private void glowCircle(float cx, float cy, float r, Color c, int layers) {
        for (int i = layers; i > 0; i--) {
            float rr = r * (1f + i * 0.22f);
            float aa = c.a * 0.15f / i;
            uiRenderer.setColor(c.r, c.g, c.b, aa);
            uiRenderer.circle(cx, cy, rr, 48);
        }
    }

    private void glowRect(float x, float y, float w, float h, Color c) {
        for (int i = 5; i > 0; i--) {
            float ex = i * 7f;
            uiRenderer.setColor(c.r, c.g, c.b, c.a * 0.18f);
            uiRenderer.rect(x - ex, y - ex, w + ex * 2, h + ex * 2);
        }
    }

    private void roundedRect(float x, float y, float w, float h, float r) {
        uiRenderer.rect(x + r, y, w - 2 * r, h);
        uiRenderer.rect(x, y + r, w, h - 2 * r);
        uiRenderer.circle(x + r, y + r, r, 20);
        uiRenderer.circle(x + w - r, y + r, r, 20);
        uiRenderer.circle(x + r, y + h - r, r, 20);
        uiRenderer.circle(x + w - r, y + h - r, r, 20);
    }

    private void drawBorderRect(float x, float y, float w, float h, float r, float th) {
        uiRenderer.rect(x + r, y, w - 2 * r, th);
        uiRenderer.rect(x + r, y + h - th, w - 2 * r, th);
        uiRenderer.rect(x, y + r, th, h - 2 * r);
        uiRenderer.rect(x + w - th, y + r, th, h - 2 * r);
        for (int i = 0; i <= 8; i++) {
            float a = i * 11.25f;
            uiRenderer.circle(x + r + MathUtils.cosDeg(180 + a) * r, y + h - r + MathUtils.sinDeg(180 + a) * r, th / 2f, 6);
            uiRenderer.circle(x + w - r + MathUtils.cosDeg(270 + a) * r, y + h - r + MathUtils.sinDeg(270 + a) * r, th / 2f, 6);
            uiRenderer.circle(x + r + MathUtils.cosDeg(90 + a) * r, y + r + MathUtils.sinDeg(90 + a) * r, th / 2f, 6);
            uiRenderer.circle(x + w - r + MathUtils.cosDeg(0 + a) * r, y + r + MathUtils.sinDeg(0 + a) * r, th / 2f, 6);
        }
    }

    private void drawRingDots(float cx, float cy, float r, float dotR, Color c, int segs) {
        uiRenderer.setColor(c);
        for (int i = 0; i < segs; i++) {
            float a = 360f * i / segs;
            float px = cx + MathUtils.cosDeg(a) * r;
            float py = cy + MathUtils.sinDeg(a) * r;
            uiRenderer.circle(px, py, dotR, 8);
        }
    }

    private void drawCornerAccent(float x, float y, float size, Color c, int dx, int dy) {
        uiRenderer.setColor(c);
        float th = 4f;
        uiRenderer.rect(dx > 0 ? x : x - size, dy > 0 ? y - th : y, size, th);
        uiRenderer.rect(dx > 0 ? x : x - th, dy > 0 ? y - size : y, th, size);
    }

    private void drawGradientBar(float x, float y, float w, float h, float percent, Color c1, Color c2) {
        float r = h / 2f;
        uiRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
        roundedRect(x, y, w, h, r);
        float fillW = Math.max(0, w * percent);
        if (fillW > r * 2) {
            int strips = 30;
            for (int i = 0; i < strips; i++) {
                float t = i / (float) (strips - 1);
                float sxp = x + r + (fillW - 2 * r) * t;
                float sww = (fillW - 2 * r) / strips + 1.2f;
                float rr = c1.r + (c2.r - c1.r) * t;
                float gg = c1.g + (c2.g - c1.g) * t;
                float bb = c1.b + (c2.b - c1.b) * t;
                uiRenderer.setColor(rr, gg, bb, 1f);
                uiRenderer.rect(sxp, y + 3, sww, h - 6);
            }
        }
        if (fillW > 0) {
            uiRenderer.setColor(c1);
            uiRenderer.circle(x + r, y + h / 2f, r - 3f, 16);
            if (fillW > r * 2) {
                uiRenderer.setColor(c2);
                uiRenderer.circle(x + fillW - r, y + h / 2f, r - 3f, 16);
            }
        }
        float shineT = (time * 0.4f) % 1f;
        float shineX = x + shineT * w;
        if (shineX < x + fillW - 10) {
            uiRenderer.setColor(1f, 1f, 1f, 0.35f);
            uiRenderer.rect(shineX, y + 3, 14, h - 6);
        }
        uiRenderer.setColor(1f, 1f, 1f, 0.25f);
        drawBorderRect(x, y, w, h, r, 1.5f);
    }

    private void drawJuicyCircleButton(Vector2 pos, float radius, Color color, float pulse, boolean big) {
        float glowScale = big ? (1.25f + pulse * 0.25f) : (1.2f + pulse * 0.15f);
        glowCircle(pos.x, pos.y, radius * glowScale, new Color(color.r, color.g, color.b, 0.7f * color.a), 5);
        uiRenderer.setColor(0f, 0f, 0f, 0.55f * color.a);
        uiRenderer.circle(pos.x, pos.y - 5f, radius * 1.03f, 40);
        uiRenderer.setColor(0.04f, 0.04f, 0.07f, 0.9f * color.a);
        uiRenderer.circle(pos.x, pos.y, radius, 48);
        uiRenderer.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, 0.5f * color.a);
        uiRenderer.circle(pos.x, pos.y - 3f, radius * 0.9f, 40);
        uiRenderer.setColor(color.r, color.g, color.b, 0.95f * color.a);
        uiRenderer.circle(pos.x, pos.y, radius, 48);
        uiRenderer.setColor(0.04f, 0.04f, 0.07f, 0.92f * color.a);
        uiRenderer.circle(pos.x, pos.y, radius - 5f, 48);
        uiRenderer.setColor(1f, 1f, 1f, 0.15f * color.a);
        uiRenderer.circle(pos.x, pos.y + radius * 0.3f, radius * 0.55f, 32);
        if (big) {
            drawRingDots(pos.x, pos.y, radius + 12f + pulse * 6f, 3f,
                    new Color(color.r, color.g, color.b, (0.4f + pulse * 0.4f) * color.a), 20);
        }
    }

    private void drawJuicySquareButton(Vector2 pos, float size, Color color, float pulse) {
        glowRect(pos.x, pos.y, size, size, new Color(color.r, color.g, color.b, 0.6f * color.a));
        uiRenderer.setColor(0f, 0f, 0f, 0.55f * color.a);
        roundedRect(pos.x + 2, pos.y - 4, size, size, 14f);
        uiRenderer.setColor(0.04f, 0.04f, 0.07f, 0.92f * color.a);
        roundedRect(pos.x, pos.y, size, size, 14f);
        uiRenderer.setColor(color.r, color.g, color.b, (0.9f + pulse * 0.1f) * color.a);
        drawBorderRect(pos.x, pos.y, size, size, 14f, 3f);
        uiRenderer.setColor(color.r, color.g, color.b, 0.12f * color.a);
        roundedRect(pos.x + 6, pos.y + 6, size - 12, size - 12, 10f);
    }

    private void drawJuicyMenuButton(Vector2 pos, float w, float h, Color color, float pulse) {
        glowRect(pos.x, pos.y, w, h, new Color(color.r, color.g, color.b, 0.5f));
        uiRenderer.setColor(0.05f, 0.06f, 0.1f, 0.95f);
        roundedRect(pos.x, pos.y, w, h, 16f);
        uiRenderer.setColor(color.r, color.g, color.b, 0.15f);
        roundedRect(pos.x + 4, pos.y + h / 2f, w - 8, h / 2f - 4, 12f);
        uiRenderer.setColor(color.r, color.g, color.b, 0.85f + pulse * 0.15f);
        drawBorderRect(pos.x, pos.y, w, h, 16f, 3f);
    }

    private void drawVignette(float w, float h, float amount) {
        if (amount <= 0.01f) return;
        int steps = 14;
        float th = Math.min(w, h) * 0.055f;
        for (int i = 0; i < steps; i++) {
            float a = (1f - i / (float) steps) * 0.32f * amount;
            uiRenderer.setColor(0f, 0f, 0f, a);
            float strip = th;
            float y0 = h - (i + 1) * strip;
            uiRenderer.rect(0, y0, w, strip);
            uiRenderer.rect(0, i * strip, w, strip);
            uiRenderer.rect(0, 0, (i + 1) * strip * 0.7f, h);
            uiRenderer.rect(w - (i + 1) * strip * 0.7f, 0, strip * 0.7f, h);
        }
    }

    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        batch.setProjectionMatrix(uiCamera.combined);
        uiRenderer.setProjectionMatrix(uiCamera.combined);
        updatePositions();
    }

    public void dispose() {
        batch.dispose(); uiRenderer.dispose();
        titleFont.dispose(); ammoFont.dispose(); textFont.dispose(); smallFont.dispose();
        if (texFire != null) texFire.dispose();
        if (texReload != null) texReload.dispose();
    }
}