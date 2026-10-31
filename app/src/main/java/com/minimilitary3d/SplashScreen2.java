package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Game;

public class SplashScreen2 extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout layout;
    private OrthographicCamera camera;
    private Game game;

    private float time = 0f;
    private final String title = "AWANGARD TEAM";

    private float[] starX, starY, starSize, starBrightness;
    private float[] ringAges = new float[8];
    private boolean lastPulseOn = false;

    public SplashScreen2(Game game) { this.game = game; }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(camera.combined);
        layout = new GlyphLayout();

        int numStars = 250;
        starX = new float[numStars];
        starY = new float[numStars];
        starSize = new float[numStars];
        starBrightness = new float[numStars];
        RandomXS128 rng = new RandomXS128(42L);
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        for (int i = 0; i < numStars; i++) {
            starX[i] = rng.nextFloat() * w;
            starY[i] = rng.nextFloat() * h;
            starSize[i] = 0.4f + rng.nextFloat() * 1.4f;
            starBrightness[i] = 0.3f + rng.nextFloat() * 0.7f;
        }
        for (int i = 0; i < ringAges.length; i++) ringAges[i] = 100f;

        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/en.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 100;
            p.color = Color.WHITE;
            font = generator.generateFont(p);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
        }

        Timer.schedule(new Timer.Task() {
            @Override public void run() { game.setScreen(new MenuScreen(game)); }
        }, 6.5f);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.005f, 0.002f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float cx = w / 2f;
        float cy = h / 2f + h * 0.08f;
        float scale = Math.min(w, h) / 900f;
        float starR = 45f * scale;
        float beamLen = 500f * scale;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // === Вращение и наклон магнитной оси ===
        float rotSpeed = 2.5f;
        float rot = time * rotSpeed;
        float tilt = MathUtils.PI * 0.39f; // ~70°

        float mx = MathUtils.sin(tilt) * MathUtils.cos(rot);
        float my = MathUtils.cos(tilt);
        float mz = MathUtils.sin(tilt) * MathUtils.sin(rot);

        // Пульс: чем ближе магнитная ось к наблюдателю (+Z) — тем сильнее вспышка
        float pulse = 0f;
        if (mz > 0) {
            float p = MathUtils.clamp(mz, 0f, 1f);
            pulse = p * p * p;
        }

        // Триггер ударных волн на переднем фронте пульса
        boolean pulseOn = pulse > 0.3f;
        if (pulseOn && !lastPulseOn) {
            int idx = 0;
            float maxAge = -1;
            for (int i = 0; i < ringAges.length; i++) {
                if (ringAges[i] > maxAge) { maxAge = ringAges[i]; idx = i; }
            }
            ringAges[idx] = 0f;
        }
        lastPulseOn = pulseOn;
        for (int i = 0; i < ringAges.length; i++) ringAges[i] += delta;

        // Ортонормальный базис магнитной системы координат
        float ux = mx, uy = my, uz = mz;
        float vx = MathUtils.cos(tilt) * MathUtils.cos(rot);
        float vy = -MathUtils.sin(tilt);
        float vz = MathUtils.cos(tilt) * MathUtils.sin(rot);
        float wxx = uy * vz - uz * vy;
        float wyy = uz * vx - ux * vz;
        float wzz = ux * vy - uy * vx;

        // ===== КОСМОС + ЗВЁЗДЫ =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.015f, 0.008f, 0.05f, 1f);
        shapes.rect(0, 0, w, h);
        for (int i = 0; i < starX.length; i++) {
            float twinkle = 0.65f + 0.35f * MathUtils.sin(time * 1.8f + i * 0.7f);
            shapes.setColor(0.8f, 0.9f, 1f, starBrightness[i] * twinkle);
            shapes.circle(starX[i], starY[i], starSize[i] * scale, 6);
        }
        shapes.end();

        // ===== КОРОНА (мягкое свечение) =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        int coronaLayers = 35;
        for (int i = coronaLayers; i > 0; i--) {
            float rr = starR * (1f + i * 0.14f);
            float a = (1f - (float) i / coronaLayers) * 0.07f;
            shapes.setColor(0.6f + pulse * 0.3f, 0.2f + pulse * 0.4f, 1f, a);
            shapes.circle(cx, cy, rr, 32);
        }
        shapes.end();

        // ===== МАГНИТНЫЕ ЛИНИИ (диполь) =====
        Gdx.gl.glLineWidth(1.5f);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        float[] LValues = { 1.4f, 2.0f, 2.8f, 4.0f };
        int numAz = 4;
        int numPts = 32;
        for (int li = 0; li < LValues.length; li++) {
            float L = LValues[li];
            float thetaMin = (float) Math.asin(Math.sqrt(1f / L));
            for (int ai = 0; ai < numAz; ai++) {
                float phi = ai * MathUtils.PI / 2f;
                float cosPhi = MathUtils.cos(phi);
                float sinPhi = MathUtils.sin(phi);

                float prevX = 0, prevY = 0;
                boolean hasPrev = false;
                for (int pi = 0; pi <= numPts; pi++) {
                    float tt = thetaMin + (MathUtils.PI - 2 * thetaMin) * pi / numPts;
                    float sinT = MathUtils.sin(tt);
                    float cosT = MathUtils.cos(tt);
                    float r = L * sinT * sinT;
                    float zc = r * cosT;
                    float pc = r * sinT;

                    float wx3 = ux * zc + (vx * cosPhi + wxx * sinPhi) * pc;
                    float wy3 = uy * zc + (vy * cosPhi + wyy * sinPhi) * pc;
                    float wz3 = uz * zc + (vz * cosPhi + wzz * sinPhi) * pc;

                    float sx = cx + wx3 * starR;
                    float sy = cy + wy3 * starR;

                    if (hasPrev) {
                        float depth = MathUtils.clamp(wz3 / L, -1f, 1f);
                        float alpha = 0.25f + 0.35f * (depth + 1f) * 0.5f;
                        alpha = Math.min(1f, alpha + pulse * 0.6f);
                        shapes.setColor(0.3f + pulse * 0.5f, 0.6f + pulse * 0.4f, 1f, alpha);
                        shapes.line(prevX, prevY, sx, sy);
                    }
                    prevX = sx;
                    prevY = sy;
                    hasPrev = true;
                }
            }
        }
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // ===== ДАЛЬНИЙ ЛУЧ (за звездой) =====
        boolean northFar = mz < 0;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (northFar) drawBeam(shapes, cx, cy, mx, my, mz, starR, beamLen, pulse, scale);
        else          drawBeam(shapes, cx, cy, -mx, -my, -mz, starR, beamLen, pulse, scale);
        shapes.end();

        // ===== ТЕЛО НЕЙТРОННОЙ ЗВЕЗДЫ =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 8; i > 0; i--) {
            float rr = starR * (0.9f + i * 0.05f);
            shapes.setColor(0.7f, 0.4f, 1f, 0.12f * (1f - (float) i / 8f));
            shapes.circle(cx, cy, rr, 32);
        }
        shapes.setColor(0.85f, 0.75f, 1f, 1f);
        shapes.circle(cx, cy, starR * 0.9f, 40);
        shapes.setColor(1f, 0.95f, 1f, 1f);
        shapes.circle(cx, cy, starR * 0.6f, 32);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.circle(cx, cy, starR * 0.3f, 24);

        // Грануляция поверхности
        for (int i = 0; i < 15; i++) {
            float ang = i * 2.399f + time * 0.15f;
            float rr = starR * 0.75f * ((i * 7 + 3) % 11) / 11f;
            float px = cx + MathUtils.cos(ang) * rr;
            float py = cy + MathUtils.sin(ang) * rr;
            shapes.setColor(0.7f, 0.6f, 1f, 0.35f);
            shapes.circle(px, py, starR * 0.07f, 8);
        }
        shapes.end();

        // ===== ПОЛЯРНЫЕ ШАПКИ =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float npX = cx + mx * starR * 0.85f;
        float npY = cy + my * starR * 0.85f;
        float npBright = 0.5f + pulse * 0.5f + Math.max(0, mz) * 0.3f;
        for (int i = 5; i > 0; i--) {
            shapes.setColor(1f, 0.7f, 1f, 0.25f * i / 5f * npBright);
            shapes.circle(npX, npY, starR * 0.22f * (i / 3f), 16);
        }
        float spX = cx - mx * starR * 0.85f;
        float spY = cy - my * starR * 0.85f;
        float spBright = 0.5f + Math.max(0, -mz) * 0.5f;
        for (int i = 5; i > 0; i--) {
            shapes.setColor(1f, 0.5f, 1f, 0.2f * i / 5f * spBright);
            shapes.circle(spX, spY, starR * 0.2f * (i / 3f), 16);
        }
        shapes.end();

        // ===== БЛИЖНИЙ ЛУЧ (перед звездой) =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (northFar) drawBeam(shapes, cx, cy, -mx, -my, -mz, starR, beamLen, pulse, scale);
        else          drawBeam(shapes, cx, cy, mx, my, mz, starR, beamLen, pulse, scale);
        shapes.end();

        // ===== ЧАСТИЦЫ В ЛУЧАХ =====
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBeamParticles(shapes, cx, cy, mx, my, mz, starR, beamLen, pulse, scale, time);
        drawBeamParticles(shapes, cx, cy, -mx, -my, -mz, starR, beamLen, pulse, scale, time + 0.7f);
        shapes.end();

        // ===== ВСПЫШКА ПУЛЬСА + ДИФРАКЦИОННЫЕ ЛУЧИ =====
        if (pulse > 0.15f) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            float flashI = (pulse - 0.15f) / 0.85f;
            for (int i = 4; i > 0; i--) {
                shapes.setColor(1f, 0.9f, 1f, flashI * 0.25f / i);
                shapes.circle(cx, cy, starR * (1f + i * 0.6f), 32);
            }
            for (int spike = 0; spike < 4; spike++) {
                float ang = spike * MathUtils.PI / 2f + rot * 0.3f;
                float len = starR * 4f * flashI;
                float wdt = 10f * scale * flashI;
                shapes.setColor(1f, 1f, 1f, flashI * 0.5f);
                shapes.triangle(
                    cx + MathUtils.cos(ang) * len, cy + MathUtils.sin(ang) * len,
                    cx + MathUtils.cos(ang + MathUtils.PI / 2) * wdt,
                    cy + MathUtils.sin(ang + MathUtils.PI / 2) * wdt,
                    cx + MathUtils.cos(ang - MathUtils.PI / 2) * wdt,
                    cy + MathUtils.sin(ang - MathUtils.PI / 2) * wdt);
            }
            shapes.setColor(1f, 0.9f, 1f, flashI * 0.15f);
            shapes.rect(0, 0, w, h);
            shapes.end();
        }

        // ===== УДАРНЫЕ ВОЛНЫ =====
        Gdx.gl.glLineWidth(3f);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < ringAges.length; i++) {
            float age = ringAges[i];
            if (age < 0f || age > 2.5f) continue;
            float ringR = age * 400f * scale;
            float alpha = Math.max(0f, 1f - age / 2.5f) * 0.7f;
            if (ringR < 8f) continue;
            shapes.setColor(0.6f, 0.8f, 1f, alpha * 0.5f);
            shapes.circle(cx, cy, ringR, 64);
            shapes.setColor(1f, 0.95f, 1f, alpha);
            shapes.circle(cx, cy, ringR * 0.97f, 64);
        }
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // ===== ТЕКСТ =====
        if (time > 0.8f) {
            float textAlpha = Math.min(1f, (time - 0.8f) * 2f);
            if (time > 5.5f) textAlpha = Math.max(0f, 1f - (time - 5.5f) * 2f);
            if (textAlpha > 0f) {
                font.getData().setScale(scale * 0.9f);
                layout.setText(font, title);
                float textX = (w - layout.width) / 2f;
                float textY = cy - h * 0.32f;

                batch.begin();
                font.setColor(1f, 0.5f, 1f, textAlpha * 0.5f);
                font.getData().setScale(scale * 1.05f);
                font.draw(batch, title, textX - 2f, textY + 2f);
                font.setColor(1f, 0f, 0.5f, textAlpha * 0.6f);
                font.getData().setScale(scale * 0.9f);
                font.draw(batch, title, textX - 2.5f, textY);
                font.setColor(0f, 0.7f, 1f, textAlpha * 0.6f);
                font.draw(batch, title, textX + 2.5f, textY);
                float tb = 0.85f + pulse * 0.15f;
                font.setColor(tb, tb, 1f, textAlpha);
                font.draw(batch, title, textX, textY);
                batch.end();
            }
        }
    }

    private void drawBeam(ShapeRenderer sr, float cx, float cy, float dx, float dy, float dz,
                          float starR, float beamLen, float pulse, float scale) {
        float poleX = cx + dx * starR * 0.9f;
        float poleY = cy + dy * starR * 0.9f;
        float projScale = (float) Math.sqrt(Math.max(0.001f, 1f - dz * dz));
        float projLen = beamLen * projScale;

        float dirLen = (float) Math.sqrt(dx * dx + dy * dy);
        if (dirLen < 0.001f) dirLen = 0.001f;
        float nx = dx / dirLen, ny = dy / dirLen;
        float perpX = -ny, perpY = nx;

        float towardViewer = Math.max(0f, dz);
        float beamBright = 0.35f + towardViewer * 0.4f + pulse * 0.6f;
        if (dz < 0) beamBright *= 0.45f;

        float endX = poleX + nx * projLen;
        float endY = poleY + ny * projLen;
        float baseWidth = (55f + towardViewer * 220f) * scale;

        for (int layer = 5; layer >= 1; layer--) {
            float layerT = layer / 5f;
            float halfW = baseWidth * layerT;
            float alpha = (1f - layerT * 0.7f) * 0.14f * beamBright;
            sr.setColor(0.7f, 0.5f, 1f, alpha);
            sr.triangle(poleX, poleY,
                endX + perpX * halfW, endY + perpY * halfW,
                endX - perpX * halfW, endY - perpY * halfW);
        }
        float coreW = baseWidth * 0.22f;
        sr.setColor(1f, 0.9f, 1f, 0.35f * beamBright);
        sr.triangle(poleX, poleY,
            endX + perpX * coreW, endY + perpY * coreW,
            endX - perpX * coreW, endY - perpY * coreW);

        float coreW2 = baseWidth * 0.08f;
        sr.setColor(1f, 1f, 1f, 0.55f * beamBright);
        sr.triangle(poleX, poleY,
            endX + perpX * coreW2, endY + perpY * coreW2,
            endX - perpX * coreW2, endY - perpY * coreW2);

        if (towardViewer > 0.5f) {
            float flash = (towardViewer - 0.5f) * 2f;
            for (int i = 3; i > 0; i--) {
                sr.setColor(1f, 0.85f, 1f, 0.3f * flash / i);
                sr.circle(poleX, poleY, starR * 0.35f * i, 20);
            }
        }
    }

    private void drawBeamParticles(ShapeRenderer sr, float cx, float cy,
                                   float dx, float dy, float dz,
                                   float starR, float beamLen, float pulse,
                                   float scale, float timeOff) {
        float poleX = cx + dx * starR * 0.9f;
        float poleY = cy + dy * starR * 0.9f;
        float projScale = (float) Math.sqrt(Math.max(0.001f, 1f - dz * dz));
        float projLen = beamLen * projScale;

        float dirLen = (float) Math.sqrt(dx * dx + dy * dy);
        if (dirLen < 0.001f) return;
        float nx = dx / dirLen, ny = dy / dirLen;
        float perpX = -ny, perpY = nx;

        float towardViewer = Math.max(0f, dz);
        float alpha = 0.4f + towardViewer * 0.5f + pulse * 0.3f;
        if (dz < 0) alpha *= 0.5f;

        int numParticles = 18;
        for (int i = 0; i < numParticles; i++) {
            float phase = (timeOff * 1.8f + i * 0.31f) % 1f;
            float dist = phase * projLen;
            float spread = MathUtils.sin(phase * MathUtils.PI) * 40f * scale;

            float px = poleX + nx * dist;
            float py = poleY + ny * dist;
            float offset = MathUtils.sin(i * 3.7f + timeOff * 5f) * spread;
            px += perpX * offset;
            py += perpY * offset;

            float pAlpha = (1f - phase) * alpha;
            float pSize = (2f + phase * 4f) * scale;
            sr.setColor(0.6f, 0.8f, 1f, pAlpha * 0.4f);
            sr.circle(px, py, pSize * 2.5f, 6);
            sr.setColor(1f, 1f, 1f, pAlpha);
            sr.circle(px, py, pSize, 6);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}