package com.minimilitary3d;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

public class DesktopLauncher {
    public static void main(String[] args) throws Exception {
        // === Файл-лог для диагностики ===
        try {
            File logFile = new File(System.getProperty("user.home"), "MiniMilitary3D.log");
            PrintStream ps = new PrintStream(new FileOutputStream(logFile, true), true, "UTF-8");
            System.setOut(ps);
            System.setErr(ps);
            System.out.println("\n\n===== LAUNCH " + new Date() + " =====");
            System.out.println("CWD: " + System.getProperty("user.dir"));
            System.out.println("JAR: " + DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation());
            System.out.println("Log file: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("Mini Military 3D");
            config.setWindowedMode(1280, 720);
            config.setWindowSizeLimits(960, 540, -1, -1);
            config.useVsync(true);
            config.setForegroundFPS(120);

            new Lwjgl3Application(new MainGame(), config);
        } catch (Throwable t) {
            System.err.println("FATAL ERROR:");
            t.printStackTrace();
            System.err.flush();
            throw t;
        }
    }
}
