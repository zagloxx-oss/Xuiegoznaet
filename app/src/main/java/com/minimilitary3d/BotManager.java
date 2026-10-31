package com.minimilitary3d;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class BotManager {

    public interface PlayerDamageCallback {
        void onDamage(int dmg, Vector3 fromPos);
    }
    public interface BotShootCallback {
        void onBotShoot(Bot shooter, Vector3 fromPos, Vector3 hitPoint, boolean hit);
    }
    public interface BotDamagedCallback {
        void onBotDamaged(Bot bot, int dmg, Vector3 fromPos, boolean killed);
    }

    private final Bot[] bots = new Bot[5];
    private final Array<BoundingBox> solidBoxes;

    private static final float ARENA_MIN = -33f;
    private static final float ARENA_MAX = 33f;

    public BotManager(Array<BoundingBox> boxes) {
        this.solidBoxes = boxes;
        for (int i = 0; i < 5; i++) {
            bots[i] = new Bot(i);
            spawnRandom(bots[i]);
        }
    }

    public Bot[] getBots() { return bots; }

    private void spawnRandom(Bot bot) {
        float x, z;
        int tries = 0;
        do {
            x = ARENA_MIN + (float)Math.random() * (ARENA_MAX - ARENA_MIN);
            z = ARENA_MIN + (float)Math.random() * (ARENA_MAX - ARENA_MIN);
            tries++;
        } while (isInsideWall(x, z) && tries < 50);

        bot.pos.set(x, 0, z);
        bot.yOffset = 0f;
        bot.hp = 100;
        bot.dead = false;
        bot.state = Bot.State.PATROL;
        bot.stateTimer = 0;
        bot.reactionTimer = 0;
        bot.fireCooldown = 0;
        bot.burstRemaining = 0;
        bot.burstCooldown = 0;
        bot.hasLastKnown = false;
        bot.jetpackFuel = GameSettings.jetpackMaxFuel;
        bot.jetpackActive = false;
        bot.targetPos.set(x + (MathUtils.random() - 0.5f) * 15f, 0, z + (MathUtils.random() - 0.5f) * 15f);
        bot.isMoving = true;
        bot.shooting = false;
        bot.shootAnimTimer = 0;
    }

    private boolean isInsideWall(float x, float z) {
        for (BoundingBox b : solidBoxes) {
            if (x >= b.min.x - 0.6f && x <= b.max.x + 0.6f
                    && z >= b.min.z - 0.6f && z <= b.max.z + 0.6f
                    && b.max.y > 1.5f) {
                return true;
            }
        }
        return false;
    }

    public void update(float delta, Vector3 playerPos, boolean playerAlive,
                       PlayerDamageCallback dmgCb, BotShootCallback shootCb,
                       BotDamagedCallback botDmgCb) {

        for (Bot bot : bots) {
            if (bot.dead) {
                bot.respawnTimer -= delta;
                if (bot.respawnTimer <= 0) spawnRandom(bot);
                continue;
            }

            // Видимость игрока
            boolean canSeePlayer = playerAlive && canSeePosition(bot, playerPos);

            // Видимость других ботов
            Bot visibleBot = null;
            float bestBotDist = Float.MAX_VALUE;
            for (Bot other : bots) {
                if (other == bot || other.dead) continue;
                Vector3 otherPos = new Vector3(other.pos.x, other.pos.y + other.yOffset, other.pos.z);
                if (!canSeePosition(bot, otherPos)) continue;
                float d = bot.pos.dst(other.pos);
                if (d < bestBotDist) {
                    bestBotDist = d;
                    visibleBot = other;
                }
            }

            // Выбор ближайшей цели
            boolean targetIsPlayer = false;
            Bot targetBot = null;
            Vector3 targetPos = null;

            float playerDist = canSeePlayer ? bot.pos.dst(playerPos) : Float.MAX_VALUE;

            if (canSeePlayer && (visibleBot == null || playerDist <= bestBotDist)) {
                targetIsPlayer = true;
                targetPos = playerPos;
            } else if (visibleBot != null) {
                targetBot = visibleBot;
                targetPos = new Vector3(visibleBot.pos.x, visibleBot.pos.y + visibleBot.yOffset, visibleBot.pos.z);
            }

            // Нашли цель — combat
            if (targetPos != null) {
                bot.lastKnownPlayerPos.set(targetPos);
                bot.hasLastKnown = true;
                if (bot.state != Bot.State.COMBAT
                        && bot.state != Bot.State.JETPACK
                        && bot.state != Bot.State.COVER) {
                    bot.state = Bot.State.COMBAT;
                    bot.stateTimer = 0;
                    bot.reactionTimer = 0.4f + MathUtils.random() * 0.7f;
                }
            }

            // Таймеры
            if (bot.reactionTimer > 0) bot.reactionTimer -= delta;
            if (bot.fireCooldown > 0) bot.fireCooldown -= delta;
            if (bot.burstCooldown > 0) bot.burstCooldown -= delta;
            if (bot.shootAnimTimer > 0) {
                bot.shootAnimTimer -= delta;
                if (bot.shootAnimTimer <= 0) bot.shooting = false;
            }

            switch (bot.state) {
                case PATROL:  updatePatrol(bot, delta); break;
                case COMBAT:  updateCombat(bot, delta, targetPos, targetIsPlayer, targetBot, dmgCb, shootCb, botDmgCb); break;
                case SEARCH:  updateSearch(bot, delta, targetPos != null); break;
                case COVER:   updateCover(bot, delta); break;
                case JETPACK: updateJetpack(bot, delta, targetPos, targetIsPlayer, targetBot, dmgCb, shootCb, botDmgCb); break;
            }

            updateMovement(bot, delta);

            // Джетпак
            if (bot.jetpackActive && bot.jetpackFuel > 0f) {
                bot.yOffset += GameSettings.jetpackForce * 0.8f * delta;
                bot.jetpackFuel -= delta;
                if (bot.jetpackFuel < 0) { bot.jetpackFuel = 0; bot.jetpackActive = false; }
                if (bot.yOffset > GameSettings.jetpackMaxHeight) bot.yOffset = GameSettings.jetpackMaxHeight;
            } else {
                bot.jetpackActive = false;
                if (bot.yOffset > 0f) {
                    bot.yOffset -= 9.8f * delta;
                    if (bot.yOffset < 0) bot.yOffset = 0;
                }
                bot.jetpackFuel = Math.min(GameSettings.jetpackMaxFuel,
                        bot.jetpackFuel + GameSettings.jetpackRechargeRate * delta * 0.7f);
            }
        }
    }

    private boolean canSeePosition(Bot bot, Vector3 targetPos) {
        Vector3 eye = new Vector3(bot.pos.x, 1.6f + bot.yOffset, bot.pos.z);
        Vector3 targetHead = new Vector3(targetPos.x, targetPos.y + 0.5f, targetPos.z);
        Vector3 toTarget = targetHead.cpy().sub(eye);
        float dist = toTarget.len();
        if (dist > 55f) return false;
        toTarget.nor();

        float fov = (bot.state == Bot.State.COMBAT) ? 70f : 60f;

        float dx = targetPos.x - bot.pos.x;
        float dz = targetPos.z - bot.pos.z;
        float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
        float yawDiff = targetYaw - bot.yaw;
        while (yawDiff > 180f) yawDiff -= 360f;
        while (yawDiff < -180f) yawDiff += 360f;
        if (Math.abs(yawDiff) > fov) return false;

        Ray ray = new Ray(eye, toTarget);
        for (BoundingBox b : solidBoxes) {
            Vector3 hit = new Vector3();
            if (Intersector.intersectRayBounds(ray, b, hit)) {
                if (ray.origin.dst(hit) < dist - 0.5f) return false;
            }
        }
        return true;
    }

    private void updatePatrol(Bot bot, float delta) {
        bot.stateTimer += delta;
        float distToTarget = (float)Math.sqrt(
                (bot.pos.x - bot.targetPos.x) * (bot.pos.x - bot.targetPos.x) +
                (bot.pos.z - bot.targetPos.z) * (bot.pos.z - bot.targetPos.z));

        if (distToTarget < 1.5f || bot.stateTimer > 8f) {
            for (int i = 0; i < 10; i++) {
                float nx = bot.pos.x + (MathUtils.random() - 0.5f) * 25f;
                float nz = bot.pos.z + (MathUtils.random() - 0.5f) * 25f;
                nx = MathUtils.clamp(nx, ARENA_MIN, ARENA_MAX);
                nz = MathUtils.clamp(nz, ARENA_MIN, ARENA_MAX);
                if (!isInsideWall(nx, nz)) {
                    bot.targetPos.set(nx, 0, nz);
                    break;
                }
            }
            bot.stateTimer = 0;
        }

        bot.isMoving = true;
        float dx = bot.targetPos.x - bot.pos.x;
        float dz = bot.targetPos.z - bot.pos.z;
        if (Math.abs(dx) + Math.abs(dz) > 0.1f) {
            float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
            bot.yaw = MathUtils.lerpAngleDeg(bot.yaw, targetYaw, 4f * delta);
        }
    }

    private void updateCombat(Bot bot, float delta, Vector3 targetPos, boolean targetIsPlayer, Bot targetBot,
                              PlayerDamageCallback dmgCb, BotShootCallback shootCb, BotDamagedCallback botDmgCb) {

        if (targetPos == null) {
            bot.stateTimer += delta;
            if (bot.stateTimer > 2f) {
                bot.state = Bot.State.SEARCH;
                bot.stateTimer = 0;
                if (bot.hasLastKnown) {
                    bot.targetPos.set(bot.lastKnownPlayerPos);
                    bot.isMoving = true;
                }
            }
            return;
        }

        bot.stateTimer = 0;

        // Прицеливание с ошибкой
        float dx = targetPos.x - bot.pos.x;
        float dz = targetPos.z - bot.pos.z;
        float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
        float err = MathUtils.sin(bot.id * 1.3f + bot.stateTimer * 2f) * 5f
                  + (MathUtils.random() - 0.5f) * 3f;
        targetYaw += err;
        bot.yaw = MathUtils.lerpAngleDeg(bot.yaw, targetYaw, 6f * delta);

        // === СТРЕЛЬБА ОЧЕРЕДЯМИ ===
        if (bot.reactionTimer <= 0) {
            if (bot.burstRemaining > 0) {
                if (bot.fireCooldown <= 0) {
                    fireAtTarget(bot, targetPos, targetIsPlayer, targetBot, dmgCb, shootCb, botDmgCb);
                    bot.fireCooldown = 0.87f + MathUtils.random() * 0.07f;
                    bot.burstRemaining--;
                    if (bot.burstRemaining <= 0) {
                        bot.burstCooldown = 0.7f + MathUtils.random() * 0.8f;
                    }
                }
            } else if (bot.burstCooldown <= 0) {
                bot.burstRemaining = 3 + (int)(Math.random() * 4); // 3-6 выстрелов
                bot.fireCooldown = 0f;
            }
        }

        // Стрейф
        bot.stateTimer += delta;
        if (bot.stateTimer > 1.5f) {
            bot.stateTimer = 0;
            if (MathUtils.random() < 0.6f) {
                float nx = bot.pos.x + (MathUtils.random() - 0.5f) * 8f;
                float nz = bot.pos.z + (MathUtils.random() - 0.5f) * 8f;
                nx = MathUtils.clamp(nx, ARENA_MIN, ARENA_MAX);
                nz = MathUtils.clamp(nz, ARENA_MIN, ARENA_MAX);
                if (!isInsideWall(nx, nz)) {
                    bot.targetPos.set(nx, 0, nz);
                    bot.isMoving = true;
                }
            } else {
                bot.isMoving = false;
            }
        }

        // Редкий прыжок на джетпаке
        if (bot.jetpackFuel > 8f && MathUtils.random() < 0.002f) {
            bot.state = Bot.State.JETPACK;
            bot.jetpackActive = true;
            bot.stateTimer = 2.5f;
        }
    }

    private void updateSearch(Bot bot, float delta, boolean canSeeTarget) {
        bot.stateTimer += delta;
        if (canSeeTarget) {
            bot.state = Bot.State.COMBAT;
            bot.stateTimer = 0;
            bot.reactionTimer = 0.4f + MathUtils.random() * 0.6f;
            return;
        }
        float distToTarget = (float)Math.sqrt(
                (bot.pos.x - bot.targetPos.x) * (bot.pos.x - bot.targetPos.x) +
                (bot.pos.z - bot.targetPos.z) * (bot.pos.z - bot.targetPos.z));
        if (distToTarget < 2f || bot.stateTimer > 6f) {
            bot.state = Bot.State.PATROL;
            bot.stateTimer = 0;
            bot.hasLastKnown = false;
        }
        bot.isMoving = true;
        float dx = bot.targetPos.x - bot.pos.x;
        float dz = bot.targetPos.z - bot.pos.z;
        if (Math.abs(dx) + Math.abs(dz) > 0.1f) {
            float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
            bot.yaw = MathUtils.lerpAngleDeg(bot.yaw, targetYaw, 4f * delta);
        }
    }

    private void updateCover(Bot bot, float delta) {
        bot.stateTimer += delta;
        if (bot.stateTimer > 3f) {
            bot.state = Bot.State.COMBAT;
            bot.stateTimer = 0;
        }
        bot.isMoving = true;
        float dx = bot.targetPos.x - bot.pos.x;
        float dz = bot.targetPos.z - bot.pos.z;
        if (Math.abs(dx) + Math.abs(dz) > 0.1f) {
            float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
            bot.yaw = MathUtils.lerpAngleDeg(bot.yaw, targetYaw, 4f * delta);
        }
    }

    private void updateJetpack(Bot bot, float delta, Vector3 targetPos, boolean targetIsPlayer, Bot targetBot,
                               PlayerDamageCallback dmgCb, BotShootCallback shootCb, BotDamagedCallback botDmgCb) {
        bot.stateTimer -= delta;

        if (targetPos != null) {
            float dx = targetPos.x - bot.pos.x;
            float dz = targetPos.z - bot.pos.z;
            float targetYaw = MathUtils.atan2(dx, -dz) * MathUtils.radiansToDegrees;
            bot.yaw = MathUtils.lerpAngleDeg(bot.yaw, targetYaw, 8f * delta);

            if (bot.reactionTimer <= 0 && bot.fireCooldown <= 0) {
                fireAtTarget(bot, targetPos, targetIsPlayer, targetBot, dmgCb, shootCb, botDmgCb);
                bot.fireCooldown = 0.15f + MathUtils.random() * 0.1f;
            }
        }

        bot.isMoving = false;
        if (bot.stateTimer <= 0 || bot.jetpackFuel <= 0f) {
            bot.state = Bot.State.COMBAT;
            bot.stateTimer = 0;
            bot.jetpackActive = false;
        }
    }

    private void fireAtTarget(Bot bot, Vector3 targetPos, boolean targetIsPlayer, Bot targetBot,
                              PlayerDamageCallback dmgCb, BotShootCallback shootCb, BotDamagedCallback botDmgCb) {
        bot.shooting = true;
        bot.shootAnimTimer = 0.12f;

        Vector3 eye = new Vector3(bot.pos.x, 1.6f + bot.yOffset, bot.pos.z);
        Vector3 aimPoint = new Vector3(targetPos.x, targetPos.y + 0.6f, targetPos.z);
        Vector3 dir = aimPoint.cpy().sub(eye).nor();

        float spreadDeg = 4f + MathUtils.random() * 12f;
        float spreadRad = (float)Math.toRadians(spreadDeg);

        Vector3 right = new Vector3(dir).crs(Vector3.Y).nor();
        Vector3 up = new Vector3(right).crs(dir).nor();
        float rx = (MathUtils.random() - 0.5f) * spreadRad;
        float ry = (MathUtils.random() - 0.5f) * spreadRad;
        dir.add(right.scl((float)Math.tan(rx)));
        dir.add(up.scl((float)Math.tan(ry)));
        dir.nor();

        Ray ray = new Ray(eye, dir);

        float minWallDist = Float.MAX_VALUE;
        for (BoundingBox b : solidBoxes) {
            Vector3 hit = new Vector3();
            if (Intersector.intersectRayBounds(ray, b, hit)) {
                minWallDist = Math.min(minWallDist, ray.origin.dst(hit));
            }
        }

        boolean hit = false;
        Vector3 hitPoint = ray.origin.cpy().add(dir.cpy().scl(Math.min(minWallDist, 55f)));

        if (targetIsPlayer) {
            Vector3 playerSphere = new Vector3(targetPos.x, targetPos.y + 0.6f, targetPos.z);
            float d = raySphereHit(ray, playerSphere, 0.7f);
            if (d > 0 && d < minWallDist) {
                int dmg = 6 + (int)(Math.random() * 12);
                if (dmgCb != null) dmgCb.onDamage(dmg, bot.pos);
                hitPoint.set(ray.origin).add(dir.cpy().scl(d));
                hit = true;
            }
        } else if (targetBot != null) {
            float targetY = targetBot.pos.y + targetBot.yOffset;
            Vector3 botCenter = new Vector3(targetBot.pos.x, targetY + 0.9f, targetBot.pos.z);
            float d = raySphereHit(ray, botCenter, 0.55f);
            if (d > 0 && d < minWallDist) {
                int dmg = 8 + (int)(Math.random() * 10);
                boolean killed = damageBot(targetBot, dmg);
                if (botDmgCb != null) botDmgCb.onBotDamaged(targetBot, dmg, bot.pos, killed);
                hitPoint.set(ray.origin).add(dir.cpy().scl(d));
                hit = true;
            }
        }

        if (shootCb != null) shootCb.onBotShoot(bot, eye, hitPoint, hit);
    }

    private float raySphereHit(Ray ray, Vector3 center, float radius) {
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

    private void updateMovement(Bot bot, float delta) {
        if (!bot.isMoving) {
            bot.walkAnimTime = MathUtils.lerp(bot.walkAnimTime, 0, 3f * delta);
            return;
        }

        float dx = bot.targetPos.x - bot.pos.x;
        float dz = bot.targetPos.z - bot.pos.z;
        float dist = (float)Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.3f) {
            bot.isMoving = false;
            return;
        }

        float nx = dx / dist;
        float nz = dz / dist;
        float speed = bot.moveSpeed * delta;

        float oldX = bot.pos.x;
        float oldZ = bot.pos.z;
        bot.pos.x += nx * speed;
        bot.pos.z += nz * speed;

        if (bot.yOffset < 4f) {
            for (BoundingBox b : solidBoxes) {
                if (b.max.y < 0.5f) continue;
                if (bot.pos.x >= b.min.x - 0.5f && bot.pos.x <= b.max.x + 0.5f
                        && bot.pos.z >= b.min.z - 0.5f && bot.pos.z <= b.max.z + 0.5f) {
                    if (!(oldX + nx * speed >= b.min.x - 0.5f && oldX + nx * speed <= b.max.x + 0.5f)) {
                        bot.pos.x = oldX + nx * speed;
                    } else {
                        bot.pos.x = oldX;
                    }
                    if (!(oldZ + nz * speed >= b.min.z - 0.5f && oldZ + nz * speed <= b.max.z + 0.5f)) {
                        bot.pos.z = oldZ + nz * speed;
                    } else {
                        bot.pos.z = oldZ;
                    }
                    if (bot.pos.x == oldX && bot.pos.z == oldZ) {
                        bot.targetPos.set(
                            bot.pos.x + (MathUtils.random() - 0.5f) * 15f,
                            0,
                            bot.pos.z + (MathUtils.random() - 0.5f) * 15f
                        );
                    }
                }
            }
        }

        bot.walkAnimTime += delta * 5f;
    }

    public boolean damageBot(Bot bot, int dmg) {
        if (bot.dead) return false;
        bot.hp -= dmg;
        if (bot.hp <= 0) {
            bot.dead = true;
            bot.hp = 0;
            bot.respawnTimer = 5f;   // как у игрока
            bot.state = Bot.State.PATROL;
            bot.jetpackActive = false;
            bot.yOffset = 0f;
            return true;
        }
        if (MathUtils.random() < 0.25f) {
            bot.state = Bot.State.COVER;
            bot.stateTimer = 0;
            if (bot.hasLastKnown) {
                float dx = bot.pos.x - bot.lastKnownPlayerPos.x;
                float dz = bot.pos.z - bot.lastKnownPlayerPos.z;
                float len = (float)Math.sqrt(dx*dx+dz*dz);
                if (len > 0.1f) {
                    bot.targetPos.set(
                        MathUtils.clamp(bot.pos.x + dx/len * 10f, ARENA_MIN, ARENA_MAX),
                        0,
                        MathUtils.clamp(bot.pos.z + dz/len * 10f, ARENA_MIN, ARENA_MAX)
                    );
                    bot.isMoving = true;
                }
            }
        }
        return false;
    }
}