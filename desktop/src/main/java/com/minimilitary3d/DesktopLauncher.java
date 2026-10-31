package com.minimilitary3d;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mini Military 3D");
        config.setWindowedMode(1280, 720);
        config.setWindowSizeLimits(960, 540, -1, -1);
        config.useVsync(true);
        // 🔥 Запрашиваем OpenGL 4.1 Core (у 5090 Ti лимит uniformов огромный)
        config.setOpenGLEmulation(
            Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 4, 1);
        config.setForegroundFPS(120);

        // 🔧 ФИКС HiDPI: работаем в физических пикселях
        // (иначе glViewport меньше окна → UI в углу)
        config.setHdpiMode(HdpiMode.Pixels);

        // Выводим инфу о GPU в лог
        System.out.println("[GPU] OpenGL: " + com.badlogic.gdx.Gdx.gl.glGetString(com.badlogic.gdx.graphics.GL20.GL_VERSION));
        System.out.println("[GPU] Renderer: " + com.badlogic.gdx.Gdx.gl.glGetString(com.badlogic.gdx.graphics.GL20.GL_RENDERER));
        int[] maxUniforms = new int[1];
        com.badlogic.gdx.Gdx.gl.glGetIntegerv(com.badlogic.gdx.graphics.GL20.GL_MAX_VERTEX_UNIFORM_COMPONENTS, maxUniforms, 0);
        System.out.println("[GPU] MaxVertexUniforms: " + maxUniforms[0]);

        new Lwjgl3Application(new MainGame(), config);
    }
}
