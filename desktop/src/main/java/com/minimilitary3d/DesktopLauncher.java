package com.minimilitary3d;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class DesktopLauncher {
    private static final String LOG = "MiniMilitary3D_run.log";

    public static void main(String[] args) {
        log("=== LAUNCH " + new Date() + " ===");
        log("Java: " + System.getProperty("java.version"));
        log("CWD: " + System.getProperty("user.dir"));
        log("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        log("JAR: " + DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation());

        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("Mini Military 3D");
            config.setWindowedMode(1280, 720);
            config.setWindowSizeLimits(960, 540, -1, -1);
            config.useVsync(true);
            config.setForegroundFPS(120);
            log("Config OK, starting Lwjgl3Application...");
            new Lwjgl3Application(new MainGame(), config);
            log("Lwjgl3Application exited normally");
        } catch (Throwable t) {
            log("FATAL ERROR:");
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            log(sw.toString());
        }
    }

    private static void log(String msg) {
        System.out.println(msg);
        try (FileWriter fw = new FileWriter(LOG, true)) {
            fw.write(msg + "\n");
        } catch (Exception ignored) {}
    }
}
