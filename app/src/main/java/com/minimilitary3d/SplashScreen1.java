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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Game;

public class SplashScreen1 extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout layout;
    private OrthographicCamera camera;
    private Game game;
    
    private float time = 0f;
    private final String title = "AWANGARD ENGINE";
    private final String subtitle = "> SYSTEM INITIALIZING...";

    public SplashScreen1(Game game) { this.game = game; }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(camera.combined);
        layout = new GlyphLayout();

        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/en.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 120;
            p.borderWidth = 2f;
            p.borderColor = new Color(0, 0.3f, 0, 1);
            p.color = Color.WHITE;
            font = generator.generateFont(p);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() { game.setScreen(new SplashScreen2(game)); }
        }, 4f);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.01f, 0.03f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float cx = w / 2f, cy = h / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // === ФОНОВАЯ ГРАФИКА ===
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        
        // Неоновая сетка
        shapes.setColor(0, 0.4f, 0, 0.15f);
        for (float x = 0; x < w; x += 80) shapes.rect(x, 0, 1, h);
        for (float y = 0; y < h; y += 80) shapes.rect(0, y, w, 1);
        
        // Сканирующая линия
        if (time > 0.2f) {
            float scanY = ((time - 0.2f) * 800f) % h;
            shapes.setColor(0, 1f, 0, 0.7f);
            shapes.rect(0, h - scanY, w, 3);
            shapes.setColor(0, 1f, 0, 0.2f);
            shapes.rect(0, h - scanY - 30, w, 30);
        }
        
        // Угловые HUD-скобки
        if (time > 0.5f) {
            float bl = 70f, m = 40f;
            shapes.setColor(0, 1f, 0, 0.8f);
            shapes.rect(m, h - m - bl, 4, bl);       // левый верх
            shapes.rect(m, h - m - 4, bl, 4);
            shapes.rect(w - m - 4, h - m - bl, 4, bl); // правый верх
            shapes.rect(w - m - bl, h - m - 4, bl, 4);
            shapes.rect(m, m, 4, bl);                 // левый низ
            shapes.rect(m, m, bl, 4);
            shapes.rect(w - m - 4, m, 4, bl);         // правый низ
            shapes.rect(w - m - bl, m, bl, 4);
        }
        
        // Пульсирующее свечение по центру (для появления текста)
        if (time > 0.6f && time < 1.5f) {
            float glow = (float)Math.sin((time - 0.6f) * 10f);
            shapes.setColor(0, 1f, 0, 0.1f * glow);
            shapes.circle(cx, cy, 300 + glow * 50, 64);
        }

        shapes.end();

        // === ТЕКСТ ===
        if (time < 0.8f) return;
        float t = time - 0.8f;
        
        // Появление с overshoot (эффект пружины)
        float scale = (t < 0.4f) ? Interpolation.swingOut.apply(t / 0.4f) : 1f + MathUtils.sin(time * 3f) * 0.02f;
        font.getData().setScale(scale);
        
        // Глитч (первые 0.4 секунды)
        float glitch = 0f;
        if (t < 0.4f) glitch = MathUtils.random(-15f, 15f);
        
        // Альфа (появление и исчезновение)
        float alpha = Math.min(1f, t * 3f);
        if (time > 3.5f) alpha = Math.max(0f, 1f - (time - 3.5f) * 2f);

        layout.setText(font, title);
        float tx = cx - layout.width / 2f, ty = cy + layout.height / 2f;

        batch.begin();
        
        // Глитч-слой красный
        if (glitch > 0) {
            font.setColor(1f, 0f, 0f, alpha * 0.7f);
            font.draw(batch, layout, tx - glitch, ty);
            // Глитч-слой синий
            font.setColor(0f, 0.5f, 1f, alpha * 0.7f);
            font.draw(batch, layout, tx + glitch, ty);
        }
        
        // Слой свечения (чуть больше, полупрозрачный)
        font.getData().setScale(scale * 1.06f);
        layout.setText(font, title);
        font.setColor(0f, 1f, 0.3f, alpha * 0.35f);
        font.draw(batch, layout, cx - layout.width / 2f, ty);
        
        // Основной белый текст
        font.getData().setScale(scale);
        layout.setText(font, title);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, layout, tx, ty);
        
        // Печатающийся подзаголовок
        if (t > 0.5f) {
            font.getData().setScale(0.35f);
            String sub = subtitle;
            int chars = Math.min(sub.length(), (int)((t - 0.5f) * 25));
            String vis = sub.substring(0, chars);
            layout.setText(font, sub);
            float sx = cx - layout.width / 2f;
            
            font.setColor(0f, 1f, 0f, alpha);
            font.draw(batch, vis, sx, cy - 100);
            
            // Мигающий курсор
            if ((int)(time * 4) % 2 == 0) {
                layout.setText(font, vis);
                font.draw(batch, "_", sx + layout.width + 5, cy - 100);
            }
        }
        
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose(); shapes.dispose(); font.dispose();
    }
}