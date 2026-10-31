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
        config.setForegroundFPS(120);

        // 🔧 ФИКС HiDPI: работаем в физических пикселях
        // (иначе glViewport меньше окна → UI в углу)
        config.setHdpiMode(HdpiMode.Pixels);

        new Lwjgl3Application(new MainGame(), config);
    }
}
