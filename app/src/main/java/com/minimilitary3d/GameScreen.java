package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Locale;

public class GameScreen extends ScreenAdapter {
    public enum State { PLAYING, PAUSED, SETTINGS, DEAD }

    private Game game;
    private PerspectiveCamera camera;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private SettingsMenu settingsMenu;
    private DesktopInputHandler desktopInput;

    private State currentState = State.PLAYING;

    private NetworkManager network;
    private boolean isMultiplayer = false;
    private boolean isHost = false;
    private float sendTimer = 0f;
    private String netDebug = "NO NETWORK DATA";
    private Vector3 killerPos = new Vector3();
    private float respawnTimer = 0f;
    private int currentEnemyAnimState = 0;

    private BotManager botManager;

    private int playerArmor = 0;
    private int maxArmor = 100;
    private boolean bombActive = false;
    private float bombTimer = 60f;
    private boolean isPlanting = false;
    private float plantTimer = 0f;
    private boolean isDefusing = false;
    private float defuseTimer = 10f;
    private Vector3 bombPos = new Vector3();
    private boolean canPlant = false;
    private boolean canDefuse = false;

    private int secretTapCount = 0;
    private float lastSecretTapTime = 0;

    private Vector3 playerPos = new Vector3(0, 1.6f, 25f);
    private float playerYOffset = 0f;
    private float yaw = 0f, pitch = 0f;
    private int playerHP = 100, maxHP = 100;

    private float jetpackFuel;
    private boolean isJetpacking = false;
    private int jetpackTouchId = -1;

    private int moveTouchId = -1, lookTouchId = -1, fireTouchId = -1, aimTouchId = -1, reloadTouchId = -1;
    private float lastLookX, lastLookY;
    private Vector3 moveDirection = new Vector3();
    private boolean joystickActive = false;
    private Vector2 joyCenter = new Vector2();
    private Vector2 joyKnobOffset = new Vector2();
    private float joyRadius = 130f, joyKnobRadius = 55f, joyThreshold = 0.7f;

    private boolean isAiming = false, isFiring = false, isReloading = false;
    private int ammo = 30, maxAmmo = 30;
    private float shootDelay = 0.13f, shootCooldown = 0f, reloadDuration = 2.0f;
    private float recoilPitch = 0f, recoilYaw = 0f;
    private float currentSpread = 0.01f, targetSpread = 0.01f, spreadSpeed = 5f;

    private Vector3[] targetPositions;
    private boolean[] targetActive;
    private Array<BoundingBox> solidBoxes = new Array<>();
    private Vector3 tmpCheckVec = new Vector3();

    private Sound sndAk, sndStep1, sndStep2, sndEnemyStep, sndEnemyAk;
    private Music bgMusic;
    private float stepTimer = 0f;
    private float enemyStepTimer = 0f;
    private boolean stepToggle = false;
    private String statusText = "";
    private float statusTimer = 0;

    private float walkTime = 0f;
    private float cameraShakeAmount = 0f;

    public GameScreen(Game game, boolean isHost, boolean isLan, String ip) {
        this(game, isHost, isLan, ip, null);
    }

    public GameScreen(Game game, boolean isHost, boolean isLan, String ip, NetworkManager net) {
        this.game = game;
        this.isMultiplayer = isLan;
        this.isHost = isHost;

        if (net != null) this.network = net;
        else if (isLan) {
            this.network = new NetworkManager();
            if (isHost) this.network.startServer();
            else this.network.connectToServer(ip);
        }

        if (isLan) {
            if (isHost) playerPos.set(15, 1.6f, 15);
            else { playerPos.set(-15, 1.6f, -15); yaw = 180f; }
        } else {
            playerPos.set(0, 1.6f, 25f);
        }

        jetpackFuel = GameSettings.jetpackMaxFuel;
    }

    private float sfxVolume() { return GameSettings.masterVolume * GameSettings.sfxVolume; }
    private float weaponVolume() { return sfxVolume() * GameSettings.weaponVolume; }
    private float footstepVolume() { return sfxVolume() * GameSettings.footstepVolume; }

    private void playSpatial(Sound snd, Vector3 soundPos, float baseVol) {
        if (snd == null || baseVol < 0.01f) return;
        if (!GameSettings.spatialAudio || soundPos == null) {
            snd.play(baseVol);
            return;
        }
        float dist = playerPos.dst(soundPos);
        float atten = MathUtils.clamp(1f - dist / 60f, 0f, 1f);
        float vol = baseVol * atten;
        if (vol < 0.01f) return;

        float dx = soundPos.x - playerPos.x;
        float dz = soundPos.z - playerPos.z;
        float soundAngle = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
        float relAngle = soundAngle - yaw;
        while (relAngle > 180f) relAngle -= 360f;
        while (relAngle < -180f) relAngle += 360f;
        float pan = MathUtils.clamp(MathUtils.sinDeg(relAngle), -1f, 1f);
        snd.play(vol, 1f, pan);
    }

    private float difficultyDamageMul() {
        switch (GameSettings.difficulty) {
            case 0: return 0.5f;
            case 2: return 1.5f;
            case 3: return 2.2f;
            default: return 1f;
        }
    }

    /** Ближайший живой бот в радиусе (для magic/aimbot/triggerbot) */
    private Bot findClosestBot(float maxDist) {
        if (botManager == null) return null;
        Bot best = null;
        float bestDist = maxDist;
        for (Bot b : botManager.getBots()) {
            if (b.dead) continue;
            float d = playerPos.dst(b.pos);
            if (d < bestDist) { bestDist = d; best = b; }
        }
        return best;
    }

    @Override
    public void show() {
        camera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.05f;
        camera.far = Math.max(100f, GameSettings.viewDistance);

        worldRenderer = new WorldRenderer();
        worldRenderer.init(isMultiplayer);

        hudRenderer = new HudRenderer();
        hudRenderer.init();

        settingsMenu = new SettingsMenu();
        settingsMenu.init(() -> {
            GameSettings.save();
            currentState = State.PAUSED;
        });

        targetPositions = new Vector3[3];
        targetActive = new boolean[3];
        targetPositions[0] = new Vector3(-20, 2.5f, -20);
        targetPositions[1] = new Vector3(20, 2.5f, 20);
        targetPositions[2] = new Vector3(0, 0.5f, 10);
        for (int i = 0; i < 3; i++) targetActive[i] = true;

        createArenaCollisions();

        if (!isMultiplayer) {
            botManager = new BotManager(solidBoxes);
            worldRenderer.setBotManager(botManager);
            worldRenderer.loadBots();
        }

        try { sndAk = Gdx.audio.newSound(Gdx.files.internal("music/ak.mp3")); } catch (Exception e) {}
        try { sndEnemyAk = Gdx.audio.newSound(Gdx.files.internal("music/ak_enemy.mp3")); } catch (Exception e) {}
        try { sndStep1 = Gdx.audio.newSound(Gdx.files.internal("music/footstep-on-concrete-1.ogg")); } catch (Exception e) {}
        try { sndStep2 = Gdx.audio.newSound(Gdx.files.internal("music/footstep-on-concrete-2.ogg")); } catch (Exception e) {}
        try { sndEnemyStep = Gdx.audio.newSound(Gdx.files.internal("music/footstep-on-concrete-3.ogg")); } catch (Exception e) {}

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            desktopInput = new DesktopInputHandler(this);
            Gdx.input.setInputProcessor(desktopInput);
        } else {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
                float worldY = h - screenY;

                if (currentState == State.SETTINGS) {
                    if (screenX >= w / 2f - 300 && screenX <= w / 2f + 300
                            && worldY >= h - 100 && worldY <= h - 10) {
                        float now = TimeUtils.millis();
                        if (now - lastSecretTapTime < 500) {
                            secretTapCount++;
                            if (secretTapCount >= 5) {
                                GameSettings.isDevMode = !GameSettings.isDevMode;
                                secretTapCount = 0;
                            }
                        } else {
                            secretTapCount = 1;
                        }
                        lastSecretTapTime = now;
                    }
                    if (settingsMenu.touchDown(screenX, screenY, pointer)) return true;
                    return true;
                }

                if (currentState == State.PLAYING) {
                    if (canPlant && !bombActive && checkCircle(screenX, (int) worldY, hudRenderer.bombBtn, 70f)) {
                        isPlanting = true; plantTimer = 5f; return true;
                    }
                    if (canDefuse && bombActive && checkCircle(screenX, (int) worldY, hudRenderer.bombBtn, 70f)) {
                        isDefusing = true; return true;
                    }

                    float bs = GameSettings.buttonScale;
                    float topSize = hudRenderer.topBtnSize * bs;
                    float fireR = hudRenderer.fireRadius * bs;
                    float subR = hudRenderer.subRadius * bs;

                    if (checkBtn(hudRenderer.stopBtn, topSize, topSize, screenX, worldY)) {
                        currentState = State.PAUSED; return true;
                    }
                    if (checkBtn(hudRenderer.settingsBtn, topSize, topSize, screenX, worldY)) {
                        GameSettings.save();
                        currentState = State.SETTINGS; return true;
                    }

                    if (checkCircle(screenX, (int) worldY, hudRenderer.jetpackBtn, subR)) {
                        if (jetpackTouchId == -1) {
                            jetpackTouchId = pointer;
                            isJetpacking = true;
                        }
                        return true;
                    }

                    if (checkCircle(screenX, (int) worldY, hudRenderer.fireBtn, fireR)) {
                        if (fireTouchId == -1) { fireTouchId = pointer; isFiring = true; } return true;
                    }
                    if (checkCircle(screenX, (int) worldY, hudRenderer.reloadBtn, subR)) {
                        if (reloadTouchId == -1) { reloadTouchId = pointer; reload(); } return true;
                    }
                    if (checkCircle(screenX, (int) worldY, hudRenderer.aimBtn, subR)) {
                        if (aimTouchId == -1) { aimTouchId = pointer; isAiming = !isAiming; } return true;
                    }

                } else if (currentState == State.PAUSED) {
                    if (checkBtn(hudRenderer.resumeBtn, 400, 100, screenX, worldY)) {
                        currentState = State.PLAYING; return true;
                    }
                    if (checkBtn(hudRenderer.pauseSettingsBtn, 400, 100, screenX, worldY)) {
                        GameSettings.save();
                        currentState = State.SETTINGS; return true;
                    }
                    if (checkBtn(hudRenderer.exitBtn, 400, 100, screenX, worldY)) {
                        GameSettings.save();
                        if (network != null) network.dispose();
                        game.setScreen(new MenuScreen(game));
                        return true;
                    }
                }

                if (currentState != State.PLAYING) return true;

                if (screenX < w / 2) {
                    if (moveTouchId == -1) {
                        moveTouchId = pointer;
                        joystickActive = true;
                        joyCenter.set(screenX, worldY);
                        joyKnobOffset.set(0, 0);
                    }
                } else {
                    if (lookTouchId == -1) {
                        lookTouchId = pointer;
                        lastLookX = screenX;
                        lastLookY = screenY;
                    }
                }
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                float h = Gdx.graphics.getHeight(), worldY = h - screenY;

                if (currentState == State.SETTINGS) {
                    settingsMenu.touchDragged(screenX, screenY, pointer);
                    return true;
                }
                if (currentState != State.PLAYING) return true;

                if (pointer == moveTouchId && joystickActive) {
                    float jr = joyRadius * GameSettings.joystickSize;
                    float dx = screenX - joyCenter.x, dy = worldY - joyCenter.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > jr) { dx = dx / dist * jr; dy = dy / dist * jr; }
                    joyKnobOffset.set(dx, dy);

                    float normalizedDist = dist / jr;
                    if (normalizedDist < GameSettings.joystickDeadzone) {
                        moveDirection.setZero();
                    } else {
                        moveDirection.set(dx / jr, 0, -(dy / jr));
                    }
                }
                if (pointer == lookTouchId) {
                    float deltaX = screenX - lastLookX;
                    float deltaY = screenY - lastLookY;

                    float sens = GameSettings.sensitivity * GameSettings.touchLookSensitivity;
                    if (isAiming) sens *= GameSettings.adsSensitivity;

                    float invX = GameSettings.invertX ? -1f : 1f;
                    float invY = GameSettings.invertY ? -1f : 1f;

                    yaw += deltaX * 0.2f * sens * invX;
                    pitch -= deltaY * 0.2f * sens * invY;
                    pitch = MathUtils.clamp(pitch, -89f, 89f);
                    lastLookX = screenX;
                    lastLookY = screenY;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (currentState == State.SETTINGS) {
                    settingsMenu.touchUp(pointer);
                    return true;
                }

                if (pointer == moveTouchId) {
                    moveTouchId = -1;
                    joystickActive = false;
                    moveDirection.setZero();
                    joyKnobOffset.set(0, 0);
                }
                if (pointer == lookTouchId) lookTouchId = -1;
                if (pointer == fireTouchId) { fireTouchId = -1; isFiring = false; }
                if (pointer == aimTouchId) aimTouchId = -1;
                if (pointer == reloadTouchId) reloadTouchId = -1;
                if (pointer == jetpackTouchId) { jetpackTouchId = -1; isJetpacking = false; }

                isDefusing = false;
                defuseTimer = 10f;
                return true;
            }
        });
        }
    }

    private boolean checkCircle(int touchX, int touchY, Vector2 center, float radius) {
        float dx = touchX - center.x;
        float dy = touchY - center.y;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    private boolean checkBtn(Vector2 pos, float w, float h, float x, float y) {
        return x >= pos.x && x <= pos.x + w && y >= pos.y && y <= pos.y + h;
    }

    private void handleBombLogic(float delta) {
        if (!isMultiplayer) return;

        if (isHost) canPlant = playerPos.x < -10 && playerPos.z < -10;
        else canPlant = playerPos.x > 10 && playerPos.z > 10;

        if (isPlanting) {
            plantTimer -= delta;
            if (plantTimer <= 0) {
                isPlanting = false;
                bombActive = true;
                bombPos.set(playerPos);
                try { network.sendData("PLANT_BOMB|" + bombPos.x + "," + bombPos.z); } catch (Exception e) {}
                worldRenderer.showBomb(bombPos);
            }
            return;
        }

        if (bombActive) {
            bombTimer -= delta;
            if (bombTimer <= 0) {
                currentState = State.DEAD;
                playerHP = 0;
                respawnTimer = 5f;
                bombActive = false;
                worldRenderer.hideBomb();
                try { network.sendData("BOMB_EXPLODED"); } catch (Exception e) {}
            } else {
                canDefuse = playerPos.dst(bombPos) < 3.0f;
                if (isDefusing && canDefuse) {
                    defuseTimer -= delta;
                    if (defuseTimer <= 0) {
                        bombActive = false;
                        isDefusing = false;
                        defuseTimer = 10f;
                        worldRenderer.hideBomb();
                        try { network.sendData("BOMB_DEFUSED"); } catch (Exception e) {}
                        statusText = "BOMB DEFUSED!";
                        statusTimer = 2f;
                    }
                }
            }
        }
    }

    private void checkArmorPickup() {
        if (!isMultiplayer) return;
        if (worldRenderer.isArmorAvailable() && playerPos.dst(worldRenderer.getArmorPos()) < 1.5f) {
            playerArmor = maxArmor;
            worldRenderer.pickupArmor();
        }
    }

    private void reload() {
        if (isReloading || ammo == maxAmmo) return;
        isReloading = true;
        worldRenderer.playReloadAnim();
        float baseDur = worldRenderer.getReloadDuration();
        reloadDuration = GameSettings.fastReload ? baseDur * 0.5f : baseDur;
        Timer.schedule(new Timer.Task() {
            public void run() { ammo = maxAmmo; isReloading = false; }
        }, reloadDuration);
    }

    private void shoot() {
        if (isReloading) return;
        if (!GameSettings.infiniteAmmo && ammo <= 0) return;

        if (!GameSettings.infiniteAmmo) ammo--;
        shootCooldown = shootDelay;
        if (sndAk != null) sndAk.play(weaponVolume());

        if (!GameSettings.noRecoil) {
            recoilPitch += 1.5f;
            recoilYaw += (Math.random() - 0.5f) * 1.5f;
        }

        cameraShakeAmount = Math.min(1.5f, cameraShakeAmount + 0.5f * GameSettings.cameraShake);

        worldRenderer.playShootAnim();

        Vector3 shootDir = camera.direction.cpy();

        // === MAGIC BULLET ===
        if (GameSettings.magic) {
            // В мультиплеере — на врага
            if (isMultiplayer && worldRenderer.isEnemyAlive()) {
                Vector3 enemyPos = worldRenderer.getEnemyPos();
                Vector3 magicAim = enemyPos.cpy();
                if (GameSettings.magicHeadshot) magicAim.y += 1.8f;
                else magicAim.y += 1.2f;
                shootDir = magicAim.sub(camera.position).nor();
            }
            // В одиночке — на ближайшего бота
            else if (!isMultiplayer) {
                Bot target = findClosestBot(80f);
                if (target != null) {
                    Vector3 magicAim = new Vector3(
                            target.pos.x,
                            target.pos.y + target.yOffset + (GameSettings.magicHeadshot ? 1.7f : 1.2f),
                            target.pos.z
                    );
                    shootDir = magicAim.sub(camera.position).nor();
                }
            }
        }

        if (currentSpread > 0 && !GameSettings.magic) {
            Vector3 right = new Vector3(shootDir).crs(camera.up).nor();
            Vector3 up = new Vector3(right).crs(shootDir).nor();
            float r = (float) Math.random() * currentSpread;
            float a = (float) (Math.random() * Math.PI * 2);
            shootDir.add(right.scl(MathUtils.cos(a) * r));
            shootDir.add(up.scl(MathUtils.sin(a) * r));
            shootDir.nor();
        }

        Ray ray = new Ray(camera.position, shootDir);
        boolean hit = false;
        Vector3 hitPoint = camera.position.cpy().add(shootDir.cpy().scl(50f));

        // === СТЕНЫ ===
        float minWallDist = Float.MAX_VALUE;
        Vector3 wallHit = new Vector3();
        for (BoundingBox b : solidBoxes) {
            if (Intersector.intersectRayBounds(ray, b, wallHit)) {
                float d = ray.origin.dst(wallHit);
                if (d < minWallDist) minWallDist = d;
            }
        }

        // === МУЛЬТИПЛЕЕРНЫЙ ВРАГ ===
        if (isMultiplayer && worldRenderer.isEnemyAlive()) {
            Vector3 enemyPos = worldRenderer.getEnemyPos();
            BoundingBox headBox = new BoundingBox(new Vector3(enemyPos.x - 0.3f, 1.6f, enemyPos.z - 0.3f), new Vector3(enemyPos.x + 0.3f, 2.0f, enemyPos.z + 0.3f));
            BoundingBox bodyBox = new BoundingBox(new Vector3(enemyPos.x - 0.5f, 0.8f, enemyPos.z - 0.5f), new Vector3(enemyPos.x + 0.5f, 1.6f, enemyPos.z + 0.5f));
            BoundingBox legBox = new BoundingBox(new Vector3(enemyPos.x - 0.4f, 0.0f, enemyPos.z - 0.4f), new Vector3(enemyPos.x + 0.4f, 0.8f, enemyPos.z + 0.4f));

            Vector3 intersection = new Vector3();
            float enemyDist = Float.MAX_VALUE;
            String part = "";

            if (Intersector.intersectRayBounds(ray, headBox, intersection)) { enemyDist = ray.origin.dst(intersection); part = "HEAD"; }
            else if (Intersector.intersectRayBounds(ray, bodyBox, intersection)) { enemyDist = ray.origin.dst(intersection); part = "BODY"; }
            else if (Intersector.intersectRayBounds(ray, legBox, intersection)) { enemyDist = ray.origin.dst(intersection); part = "LIMB"; }

            if (enemyDist < minWallDist && !part.isEmpty()) {
                int dmg = part.equals("HEAD") ? 34 : part.equals("BODY") ? 15 : 10;
                try { network.sendData("DMG|" + dmg + "|" + part); } catch (Exception e) {}
                hitPoint.set(intersection);
                hit = true;
                hudRenderer.triggerHitmarker();
            }
        }

        // === БОТЫ ===
        if (!isMultiplayer && botManager != null) {
            float nearestBotDist = Float.MAX_VALUE;
            Bot nearestBot = null;
            Vector3 nearestHitPoint = new Vector3();
            boolean nearestWasHeadshot = false;

            for (Bot bot : botManager.getBots()) {
                if (bot.dead) continue;

                float botY = bot.pos.y + bot.yOffset;
                Vector3 botCenter = new Vector3(bot.pos.x, botY + 0.9f, bot.pos.z);
                Vector3 botHead = new Vector3(bot.pos.x, botY + 1.7f, bot.pos.z);

                float headDist = rayDistanceToSphere(ray, botHead, 0.35f);
                float bodyDist = rayDistanceToSphere(ray, botCenter, 0.55f);

                float bestDist = Float.MAX_VALUE;
                boolean isHead = false;

                if (headDist > 0 && headDist < minWallDist && headDist < bestDist) {
                    bestDist = headDist; isHead = true;
                }
                if (bodyDist > 0 && bodyDist < minWallDist && bodyDist < bestDist) {
                    bestDist = bodyDist; isHead = false;
                }

                if (bestDist < Float.MAX_VALUE && bestDist < nearestBotDist) {
                    nearestBotDist = bestDist;
                    nearestBot = bot;
                    nearestHitPoint.set(ray.origin).add(shootDir.cpy().scl(bestDist));
                    nearestWasHeadshot = isHead;
                }
            }

            if (nearestBot != null) {
                int dmg = nearestWasHeadshot ? 34 : 15;
                boolean killed = botManager.damageBot(nearestBot, dmg);
                hitPoint.set(nearestHitPoint);
                hit = true;
                hudRenderer.triggerHitmarker();

                if (killed) {
                    statusText = "BOT DOWN";
                    statusTimer = 1.0f;
                }
            }
        }

        if (!hit && minWallDist < 50f) hitPoint.set(wallHit);

        worldRenderer.triggerShootEffects(hitPoint, hit);
    }

    private float rayDistanceToSphere(Ray ray, Vector3 center, float radius) {
        Vector3 oc = center.cpy().sub(ray.origin);
        float t = oc.dot(ray.direction);
        if (t < 0) return -1;
        Vector3 closest = ray.origin.cpy().add(ray.direction.cpy().scl(t));
        float d = closest.dst(center);
        if (d > radius) return -1;
        float offset = (float)Math.sqrt(radius * radius - d * d);
        float dist = t - offset;
        return dist > 0 ? dist : t + offset;
    }

    // =========================================================
    //                        AIMBOT
    // =========================================================
    private void applyAimbot(float delta) {
        if (!GameSettings.aimbot) return;
        if (!isFiring && !isAiming) return;

        Vector3 targetPos = null;

        if (isMultiplayer && worldRenderer.isEnemyAlive()) {
            targetPos = worldRenderer.getEnemyPos().cpy().add(0, 1.5f, 0);
        } else if (!isMultiplayer && botManager != null) {
            Bot closest = findClosestBot(80f);
            if (closest != null) {
                targetPos = new Vector3(closest.pos.x, closest.pos.y + closest.yOffset + 1.5f, closest.pos.z);
            }
        }

        if (targetPos == null) return;

        Vector3 proj = targetPos.cpy();
        camera.project(proj);
        if (proj.z <= 0 || proj.z >= 1) return;

        float dx = proj.x - Gdx.graphics.getWidth() / 2f;
        float dy = proj.y - Gdx.graphics.getHeight() / 2f;
        float dist2D = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist2D > GameSettings.aimbotFov) return;

        Vector3 dir = targetPos.cpy().sub(camera.position).nor();
        float targetYaw = MathUtils.atan2(dir.x, -dir.z) * MathUtils.radiansToDegrees;
        float targetPitch = MathUtils.asin(dir.y) * MathUtils.radiansToDegrees;

        float smooth = 10f / Math.max(1f, GameSettings.aimbotSmoothing);
        yaw = MathUtils.lerpAngleDeg(yaw, targetYaw, smooth * delta);
        pitch = MathUtils.lerp(pitch, targetPitch, smooth * delta);
        pitch = MathUtils.clamp(pitch, -89f, 89f);
    }

    private void applyTriggerBot(float delta) {
        if (!GameSettings.triggerBot) return;
        if (shootCooldown > 0 || isReloading) return;

        Vector3 targetPos = null;
        if (isMultiplayer && worldRenderer.isEnemyAlive()) {
            targetPos = worldRenderer.getEnemyPos().cpy().add(0, 1.5f, 0);
        } else if (!isMultiplayer && botManager != null) {
            Bot closest = findClosestBot(80f);
            if (closest != null) targetPos = new Vector3(closest.pos.x, closest.pos.y + closest.yOffset + 1.5f, closest.pos.z);
        }
        if (targetPos == null) return;

        Vector3 proj = targetPos.cpy();
        camera.project(proj);
        if (proj.z <= 0 || proj.z >= 1) return;

        float dx = proj.x - Gdx.graphics.getWidth() / 2f;
        float dy = proj.y - Gdx.graphics.getHeight() / 2f;
        float dist2D = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist2D < 60f) shoot();
    }

    private void handleJetpack(float delta) {
        if (isJetpacking && jetpackFuel > 0f) {
            playerYOffset += GameSettings.jetpackForce * delta;
            jetpackFuel -= delta;
            if (jetpackFuel < 0f) { jetpackFuel = 0f; isJetpacking = false; }
            if (playerYOffset > GameSettings.jetpackMaxHeight) {
                playerYOffset = GameSettings.jetpackMaxHeight;
            }
        } else {
            if (playerYOffset > 0) {
                playerYOffset -= 9.8f * delta;
                if (playerYOffset < 0) playerYOffset = 0;
            }
            if (!isJetpacking) {
                jetpackFuel = Math.min(GameSettings.jetpackMaxFuel,
                        jetpackFuel + GameSettings.jetpackRechargeRate * delta);
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.01f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (currentState == State.SETTINGS) {
            settingsMenu.render(delta);
            return;
        }

        float dt = (currentState == State.PLAYING) ? delta : 0f;

        if (desktopInput != null) desktopInput.update(delta);

        if (isMultiplayer && network != null) {
            if (currentState == State.PLAYING) {
                sendTimer -= delta;
                if (sendTimer <= 0) {
                    sendTimer = 0.066f;
                    int myAnimState = moveDirection.len() > joyThreshold ? 2 : moveDirection.len() > 0.15f ? 1 : 0;
                    String myData = String.format(Locale.US, "POS|%.2f,%.2f,%.2f,%.2f,%.2f,%d,%d", playerPos.x, playerPos.y, playerPos.z, yaw, pitch, myAnimState, isFiring ? 1 : 0);
                    try { network.sendData(myData); } catch (Exception e) {}
                }
            }

            if (!network.lastReceivedData.isEmpty()) {
                try {
                    String[] parts = network.lastReceivedData.split("\\|");
                    String cmd = parts[0];
                    netDebug = "RECV: " + cmd;

                    if (cmd.equals("ME") && isHost) {
                        try { network.sendData("START|"); } catch (Exception e) {}
                    }
                    else if (cmd.equals("POS") && parts.length > 1) {
                        String[] c = parts[1].split(",");
                        float ex = Float.parseFloat(c[0]);
                        float ez = Float.parseFloat(c[2]);
                        int eAnim = Integer.parseInt(c[5]);
                        int eFire = Integer.parseInt(c[6]);

                        currentEnemyAnimState = eAnim;
                        worldRenderer.updateEnemyNetwork(ex, ez, Float.parseFloat(c[3]), eAnim);

                        if (eFire == 1) {
                            worldRenderer.playEnemyShootAnim();
                            playSpatial(sndEnemyAk, worldRenderer.getEnemyPos(), weaponVolume());
                        }
                    }
                    else if (cmd.equals("DMG") && parts.length > 2) {
                        if (GameSettings.godMode) {
                            netDebug = "GOD MODE";
                        } else {
                            int baseDmg = Integer.parseInt(parts[1]);
                            int dmg = Math.round(baseDmg * difficultyDamageMul());
                            if (playerArmor > 0) {
                                int armorAbsorb = Math.min(playerArmor, dmg);
                                playerArmor -= armorAbsorb;
                                dmg -= armorAbsorb;
                            }
                            playerHP -= dmg;
                            if (sndEnemyAk != null) sndEnemyAk.play(weaponVolume());

                            if (playerHP <= 0 && currentState != State.DEAD) {
                                currentState = State.DEAD;
                                playerHP = 0;
                                respawnTimer = 5f;
                                killerPos.set(worldRenderer.getEnemyPos());
                                try { network.sendData("KILLED_BY|" + killerPos.x + "," + killerPos.y + "," + killerPos.z); } catch (Exception e) {}
                            } else if (playerHP > 0 && GameSettings.vibration && GameSettings.vibrationIntensity > 0f) {
                                int ms = Math.max(1, (int)(50 * GameSettings.vibrationIntensity));
                                Gdx.input.vibrate(ms);
                            }
                        }
                    }
                    else if (cmd.equals("KILLED_BY") && parts.length > 1) {
                        String[] c = parts[1].split(",");
                        killerPos.set(Float.parseFloat(c[0]), Float.parseFloat(c[1]), Float.parseFloat(c[2]));
                    }
                    else if (cmd.equals("PLANT_BOMB") && parts.length > 1) {
                        String[] c = parts[1].split(",");
                        bombActive = true;
                        bombTimer = 60f;
                        bombPos.set(Float.parseFloat(c[0]), 0, Float.parseFloat(c[1]));
                        worldRenderer.showBomb(bombPos);
                    }
                    else if (cmd.equals("BOMB_DEFUSED") || cmd.equals("BOMB_EXPLODED")) {
                        bombActive = false;
                        worldRenderer.hideBomb();
                    }
                    else if (cmd.equals("RESPAWN") && parts.length > 1) {
                        String[] c = parts[1].split(",");
                        worldRenderer.setEnemyDead(false);
                        worldRenderer.respawnEnemy(Float.parseFloat(c[0]), Float.parseFloat(c[1]));
                    }
                } catch (Exception e) {
                    netDebug = "PARSE ERR";
                }
            } else {
                netDebug = "NO NETWORK DATA";
            }
        }

        // === БОТЫ ===
        if (!isMultiplayer && botManager != null && currentState == State.PLAYING) {
            botManager.update(delta, playerPos, playerHP > 0,
                // 1) Бот попал в игрока
                (dmg, fromPos) -> {
                    if (GameSettings.godMode) return;
                    int realDmg = Math.round(dmg * difficultyDamageMul());
                    if (playerArmor > 0) {
                        int absorb = Math.min(playerArmor, realDmg);
                        playerArmor -= absorb;
                        realDmg -= absorb;
                    }
                    playerHP -= realDmg;

                    if (GameSettings.vibration && GameSettings.vibrationIntensity > 0f) {
                        int ms = Math.max(1, (int)(30 * GameSettings.vibrationIntensity));
                        Gdx.input.vibrate(ms);
                    }

                    if (playerHP <= 0 && currentState != State.DEAD) {
                        currentState = State.DEAD;
                        playerHP = 0;
                        respawnTimer = 5f;
                        killerPos.set(fromPos);
                    }
                },
                // 2) Бот выстрелил
                (shooter, fromPos, hitPoint, hitPlayer) -> {
                    worldRenderer.triggerBotShot(fromPos, hitPoint);
                    playSpatial(sndEnemyAk, shooter.pos, weaponVolume() * 0.7f);
                },
                // 3) Бот получил урон
                (bot, dmg, fromPos, killed) -> {
                    if (killed) {
                        statusText = "BOT DOWN";
                        statusTimer = 1.0f;
                        playSpatial(sndEnemyAk, fromPos, weaponVolume() * 0.5f);
                    }
                }
            );
        }

        handleBombLogic(dt);
        checkArmorPickup();

        if (isMultiplayer && worldRenderer.isEnemyAlive() && currentEnemyAnimState > 0 && currentState == State.PLAYING) {
            float dist = playerPos.dst(worldRenderer.getEnemyPos());
            float vol = MathUtils.clamp(1.0f - (dist / 60f), 0f, 1.0f);
            if (vol > 0.05f) {
                enemyStepTimer -= delta;
                if (enemyStepTimer <= 0) {
                    playSpatial(sndEnemyStep, worldRenderer.getEnemyPos(), footstepVolume());
                    enemyStepTimer = (currentEnemyAnimState == 2) ? 0.3f : 0.5f;
                }
            } else {
                enemyStepTimer = 0;
            }
        } else {
            enemyStepTimer = 0;
        }

        if (currentState == State.DEAD) {
            respawnTimer -= delta;
            if (killerPos.len() > 0) {
                Vector3 lookDir = killerPos.cpy().sub(camera.position).nor();
                float targetYaw = MathUtils.atan2(lookDir.x, -lookDir.z) * MathUtils.radiansToDegrees;
                float targetPitch = MathUtils.asin(lookDir.y) * MathUtils.radiansToDegrees;
                yaw = MathUtils.lerpAngleDeg(yaw, targetYaw, 5f * delta);
                pitch = MathUtils.lerp(pitch, targetPitch, 5f * delta);
                playerPos.lerp(killerPos.cpy().add(0, 2f, 2f), 2f * delta);
            }

            if (respawnTimer <= 0) {
                float rx, rz; boolean valid = false;
                do {
                    rx = -25 + (float) Math.random() * 50;
                    rz = -25 + (float) Math.random() * 50;
                    tmpCheckVec.set(rx, 1.6f, rz);
                    valid = true;
                    for (BoundingBox b : solidBoxes) {
                        if (b.contains(tmpCheckVec)) { valid = false; break; }
                    }
                } while (!valid);

                playerPos.set(rx, 1.6f, rz);
                playerYOffset = 0f;
                playerHP = 100;
                playerArmor = 0;
                currentState = State.PLAYING;
                if (isMultiplayer) {
                    try { network.sendData("RESPAWN|" + rx + "," + rz); } catch (Exception e) {}
                }
            }

            camera.position.set(playerPos);
            camera.direction.set(MathUtils.sinDeg(yaw) * MathUtils.cosDeg(pitch), MathUtils.sinDeg(pitch), -MathUtils.cosDeg(yaw) * MathUtils.cosDeg(pitch)).nor();
            camera.up.set(Vector3.Y);
            camera.update();
            worldRenderer.render(dt, camera, playerPos, false, false, false, 0);
            hudRenderer.render(ammo, maxAmmo, playerHP, maxHP, playerArmor, maxArmor, joystickActive, joyCenter, joyKnobOffset, joyRadius, joyKnobRadius,
                    currentSpread, false, "YOU DIED", respawnTimer, currentState, targetPositions, targetActive, camera, netDebug, canPlant, canDefuse, bombActive, bombTimer, isPlanting, plantTimer, isDefusing, defuseTimer, worldRenderer.getEnemyPos(), worldRenderer.isEnemyAlive(),
                    jetpackFuel, GameSettings.jetpackMaxFuel, false,
                    botManager != null ? botManager.getBots() : null);
            return;
        }

        if (currentState == State.PLAYING) {
            applyAimbot(dt);

            if (GameSettings.noSpread) {
                targetSpread = 0.0001f;
            } else if (isAiming) targetSpread = 0.0001f;
            else if (moveDirection.len() > joyThreshold) targetSpread = 0.15f;
            else if (moveDirection.len() > 0.15f) targetSpread = 0.05f;
            else targetSpread = 0.01f;
            currentSpread = MathUtils.lerp(currentSpread, targetSpread, spreadSpeed * dt);

            if (shootCooldown > 0) shootCooldown -= dt;
            if (isFiring && !isReloading && shootCooldown <= 0) shoot();

            if (GameSettings.autoReload && ammo == 0 && !isReloading) reload();

            applyTriggerBot(dt);

            handleJetpack(dt);

            if (moveDirection.len() > 0) {
                float maxSpeed = 5f;
                if (moveDirection.len() > joyThreshold) maxSpeed = 7f;
                if (isAiming) maxSpeed *= 0.4f;
                if (GameSettings.speedHack) maxSpeed *= 1.5f;
                if (playerYOffset > 0.5f) maxSpeed *= 0.85f;

                float speed = maxSpeed * dt;
                Vector3 forward = new Vector3(MathUtils.sinDeg(yaw), 0, -MathUtils.cosDeg(yaw)).nor();
                Vector3 right = new Vector3(MathUtils.cosDeg(yaw), 0, MathUtils.sinDeg(yaw)).nor();

                float moveX = (forward.x * -moveDirection.z + right.x * moveDirection.x) * speed;
                float moveZ = (forward.z * -moveDirection.z + right.z * moveDirection.x) * speed;

                float oldX = playerPos.x, oldZ = playerPos.z;
                playerPos.x += moveX;
                playerPos.z += moveZ;

                if (playerYOffset < 4f) {
                    float checkY = playerPos.y + 0.1f;
                    tmpCheckVec.set(playerPos.x, checkY, playerPos.z);
                    for (BoundingBox b : solidBoxes) {
                        if (checkY < b.max.y && b.contains(tmpCheckVec)) {
                            tmpCheckVec.set(oldX, checkY, playerPos.z);
                            if (!b.contains(tmpCheckVec)) playerPos.x = oldX;
                            tmpCheckVec.set(playerPos.x, checkY, oldZ);
                            if (!b.contains(tmpCheckVec)) playerPos.z = oldZ;
                        }
                    }
                }

                if (playerYOffset < 0.1f) {
                    stepTimer -= dt;
                    if (stepTimer <= 0) {
                        if (stepToggle) { if (sndStep1 != null) sndStep1.play(footstepVolume()); }
                        else { if (sndStep2 != null) sndStep2.play(footstepVolume()); }
                        stepToggle = !stepToggle;
                        stepTimer = (moveDirection.len() > joyThreshold) ? 0.3f : 0.5f;
                    }
                }

                walkTime += dt * (moveDirection.len() > joyThreshold ? 12f : 8f);
            } else {
                stepTimer = 0;
                walkTime = MathUtils.lerp(walkTime, 0, 4f * dt);
            }

            float groundH = getGroundHeight(playerPos.x, playerPos.z);
            playerPos.y = 1.6f + groundH + playerYOffset;

            recoilPitch = MathUtils.lerp(recoilPitch, 0, 10 * dt);
            recoilYaw = MathUtils.lerp(recoilYaw, 0, 10 * dt);
        }

        float bobOffset = 0f;
        float bobSway = 0f;
        if (currentState == State.PLAYING && GameSettings.bobAmount > 0f && walkTime > 0.01f && playerYOffset < 0.1f) {
            bobOffset = MathUtils.sin(walkTime) * 0.035f * GameSettings.bobAmount;
            bobSway = MathUtils.cos(walkTime * 0.5f) * 0.02f * GameSettings.bobAmount;
        }

        camera.position.set(playerPos);
        camera.position.y += bobOffset;

        float finalYaw = yaw + bobSway;
        float finalPitch = pitch + recoilPitch;
        camera.direction.set(
                MathUtils.sinDeg(finalYaw) * MathUtils.cosDeg(finalPitch),
                MathUtils.sinDeg(finalPitch),
                -MathUtils.cosDeg(finalYaw) * MathUtils.cosDeg(finalPitch)
        ).nor();
        camera.direction.rotate(Vector3.Y, recoilYaw);

        if (cameraShakeAmount > 0.01f) {
            camera.direction.x += (MathUtils.random() - 0.5f) * cameraShakeAmount * 0.02f;
            camera.direction.y += (MathUtils.random() - 0.5f) * cameraShakeAmount * 0.02f;
            camera.direction.z += (MathUtils.random() - 0.5f) * cameraShakeAmount * 0.02f;
            camera.direction.nor();
            cameraShakeAmount = MathUtils.lerp(cameraShakeAmount, 0f, 6f * dt);
        }

        camera.up.set(Vector3.Y);

        float targetFov;
        if (isAiming) targetFov = GameSettings.fov * GameSettings.adsFovMultiplier;
        else if (moveDirection.len() > joyThreshold) targetFov = GameSettings.fov * GameSettings.fovSprintMultiplier;
        else targetFov = GameSettings.fov;
        camera.fieldOfView = MathUtils.lerp(camera.fieldOfView, targetFov, 10 * dt);
        camera.far = Math.max(100f, GameSettings.viewDistance);
        camera.update();

        if (statusTimer > 0) statusTimer -= delta;

        worldRenderer.render(dt, camera, playerPos, isAiming, isFiring, isReloading, moveDirection.len());

        hudRenderer.render(ammo, maxAmmo, playerHP, maxHP, playerArmor, maxArmor, joystickActive, joyCenter, joyKnobOffset, joyRadius, joyKnobRadius,
                currentSpread, isAiming, statusText, statusTimer, currentState, targetPositions, targetActive, camera, netDebug, canPlant, canDefuse, bombActive, bombTimer, isPlanting, plantTimer, isDefusing, defuseTimer, worldRenderer.getEnemyPos(), worldRenderer.isEnemyAlive(),
                jetpackFuel, GameSettings.jetpackMaxFuel, isJetpacking,
                botManager != null ? botManager.getBots() : null);
    }

    private float getGroundHeight(float x, float z) {
        float h = 0f;
        if (x >= -25 && x <= -15 && z >= -25 && z <= -15) h = 2f;
        else if (x >= -25 && x <= -15 && z > -15 && z < -5) h = ((-5f - z) / 10f) * 2f;
        if (x >= 15 && x <= 25 && z >= 15 && z <= 25) h = Math.max(h, 2f);
        else if (x >= 15 && x <= 25 && z > 5 && z < 15) h = Math.max(h, ((z - 5f) / 10f) * 2f);
        return h;
    }

    private void createArenaCollisions() {
        solidBoxes.add(new BoundingBox(new Vector3(-35, 0, -35), new Vector3(35, 5, -34)));
        solidBoxes.add(new BoundingBox(new Vector3(-35, 0, 34), new Vector3(35, 5, 35)));
        solidBoxes.add(new BoundingBox(new Vector3(-35, 0, -35), new Vector3(-34, 5, 35)));
        solidBoxes.add(new BoundingBox(new Vector3(34, 0, -35), new Vector3(35, 5, 35)));

        solidBoxes.add(new BoundingBox(new Vector3(-7.5f, 0, -0.5f), new Vector3(7.5f, 5, 0.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-8f, 0, 0), new Vector3(-7f, 5, 10f)));
        solidBoxes.add(new BoundingBox(new Vector3(7f, 0, -10f), new Vector3(8f, 5, 0f)));

        solidBoxes.add(new BoundingBox(new Vector3(-15.5f, 0, -22.5f), new Vector3(-14.5f, 5, -7.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-15.5f, 0, 7.5f), new Vector3(-14.5f, 5, 22.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-30f, 0, -0.5f), new Vector3(-20f, 5, 0.5f)));

        solidBoxes.add(new BoundingBox(new Vector3(14.5f, 0, 7.5f), new Vector3(15.5f, 5, 22.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(14.5f, 0, -22.5f), new Vector3(15.5f, 5, -7.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(20f, 0, -0.5f), new Vector3(30f, 5, 0.5f)));

        solidBoxes.add(new BoundingBox(new Vector3(-27.5f, 0, 24.5f), new Vector3(-12.5f, 5, 25.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-12.5f, 0, 11f), new Vector3(-11.5f, 5, 25f)));
        solidBoxes.add(new BoundingBox(new Vector3(12.5f, 0, -25.5f), new Vector3(27.5f, 5, -24.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(11.5f, 0, -25f), new Vector3(12.5f, 5, -11f)));

        solidBoxes.add(new BoundingBox(new Vector3(-6.5f, 0, -11.5f), new Vector3(-3.5f, 3, -8.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(3.5f, 0, 8.5f), new Vector3(6.5f, 3, 11.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-21.5f, 0, -6.5f), new Vector3(-18.5f, 3, -3.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(18.5f, 0, 3.5f), new Vector3(21.5f, 3, 6.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-11.5f, 0, 18.5f), new Vector3(-8.5f, 3, 21.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(8.5f, 0, -21.5f), new Vector3(11.5f, 3, -18.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-26.5f, 0, 13.5f), new Vector3(-23.5f, 3, 16.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(23.5f, 0, -16.5f), new Vector3(26.5f, 3, -13.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-1.5f, 0, -26.5f), new Vector3(1.5f, 3, -23.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(-1.5f, 0, 23.5f), new Vector3(1.5f, 3, 26.5f)));

        solidBoxes.add(new BoundingBox(new Vector3(-19.5f, 0, -1.5f), new Vector3(-16.5f, 5, 1.5f)));
        solidBoxes.add(new BoundingBox(new Vector3(16.5f, 0, -1.5f), new Vector3(19.5f, 5, 1.5f)));
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        hudRenderer.resize(width, height);
        if (settingsMenu != null) settingsMenu.resize(width, height);
    }

    @Override
    public void pause() {
        GameSettings.save();
        super.pause();
    }

    @Override
    public void dispose() {
        GameSettings.save();
        if (bgMusic != null) bgMusic.dispose();
        if (sndAk != null) sndAk.dispose();
        if (sndStep1 != null) sndStep1.dispose();
        if (sndStep2 != null) sndStep2.dispose();
        if (sndEnemyStep != null) sndEnemyStep.dispose();
        if (sndEnemyAk != null) sndEnemyAk.dispose();
        if (network != null) network.dispose();
        if (settingsMenu != null) settingsMenu.dispose();
        if (desktopInput != null) desktopInput.dispose();
        worldRenderer.dispose();
        hudRenderer.dispose();
    }

    public void applyLookDelta(float dx, float dy) {
        float sens = GameSettings.sensitivity * GameSettings.touchLookSensitivity;
        if (isAiming) sens *= GameSettings.adsSensitivity;
        float invX = GameSettings.invertX ? -1f : 1f;
        float invY = GameSettings.invertY ? -1f : 1f;
        yaw += dx * 0.15f * sens * invX;
        pitch -= dy * 0.15f * sens * invY;
        pitch = MathUtils.clamp(pitch, -89f, 89f);
    }

    public void setMoveInput(float mx, float my) {
        if (Math.abs(mx) < 0.01f && Math.abs(my) < 0.01f) {
            moveDirection.setZero();
        } else {
            float len = (float)Math.sqrt(mx * mx + my * my);
            if (len > 1f) { mx /= len; my /= len; }
            moveDirection.set(mx, 0, my);
        }
    }

    public void setFiring(boolean f) { if (currentState == State.PLAYING) isFiring = f; }
    public void setAiming(boolean a) { isAiming = a; }
    public void setJetpack(boolean j) { isJetpacking = j; }
    public void doReload() { reload(); }
    public boolean isPaused() { return currentState == State.PAUSED; }

    public void togglePause() {
        if (currentState == State.PLAYING) currentState = State.PAUSED;
        else if (currentState == State.PAUSED) currentState = State.PLAYING;
    }

}