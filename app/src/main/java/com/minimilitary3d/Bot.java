package com.minimilitary3d;

import com.badlogic.gdx.math.Vector3;

public class Bot {
    public int id;
    public Vector3 pos = new Vector3();
    public Vector3 targetPos = new Vector3();

    public float yOffset = 0f;
    public float yaw = 0f;
    public float pitch = 0f;

    public int hp = 100;
    public boolean dead = false;
    public float respawnTimer = 0f;

    public enum State { PATROL, COMBAT, SEARCH, COVER, JETPACK }
    public State state = State.PATROL;
    public float stateTimer = 0f;
    public float reactionTimer = 0f;
    public float fireCooldown = 0f;

    // === ОЧЕРЕДИ ===
    public int burstRemaining = 0;      // сколько выстрелов осталось в серии
    public float burstCooldown = 0f;    // пауза между сериями

    public Vector3 lastKnownPlayerPos = new Vector3();
    public boolean hasLastKnown = false;

    public float jetpackFuel;
    public boolean jetpackActive = false;

    public boolean isMoving = false;
    public float moveSpeed = 3.5f;

    public boolean shooting = false;
    public float shootAnimTimer = 0f;
    public float walkAnimTime = 0f;

    public Bot(int id) {
        this.id = id;
        this.jetpackFuel = GameSettings.jetpackMaxFuel;
    }
}