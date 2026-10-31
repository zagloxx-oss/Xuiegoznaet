package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider pbrProvider = new PBRShaderProvider();
        pbrProvider.config.numPointLights = 4;
        pbrProvider.config.numDirectionalLights = 1;
        pbrProvider.config.numSpotLights = 0;
        pbrProvider.config.numBones = 60;
        sceneManager = new SceneManager(pbrProvider);
        environment = new Environment();

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.25f, 0.2f, 0.35f, 1f));

        dirLight = new DirectionalLight().set(new Color(0.55f, 0.45f, 0.75f, 1f), new Vector3(-1f, -1f, -1f));
        environment.add(dirLight);

        neonLights[0] = makeNeonEx(new Color(1f, 0f, 0.5f, 1f), new Vector3(-25, 10, -25), 3000f, 60f);
        neonLights[1] = makeNeonEx(new Color(0f, 0.5f, 1f, 1f), new Vector3(25, 10, -25), 3000f, 60f);
        neonLights[2] = makeNeonEx(new Color(1f, 0f, 0.5f, 1f), new Vector3(-25, 10, 25), 3000f, 60f);
        neonLights[3] = makeNeonEx(new Color(0f, 0.5f, 1f, 1f), new Vector3(25, 10, 25), 3000f, 60f);
        for (PointLightEx l : neonLights) if (l != null) environment.add(l);

        flashLight = makeNeonEx(new Color(1f, 1f, 1f, 1f), new Vector3(0, 0, 0), 10f, 20f);
        // flashLight disabled (PBR point light limit is 5)

        muzzleLight = makeNeonEx(new Color(1f, 0.7f, 0.2f, 1f), new Vector3(0, 0, 0), 0f, 60f);
        environment.add(muzzleLight);

        sceneManager.environment = environment;

        createArena();
        loadWeapon();
        createEffects();
        createAmbientEmbers();

        if (isMultiplayer) {
            loadEnemy();
            loadArmor();
            loadBomb();
        }
    }

    private void createArena() {
        ModelBuilder mb = new ModelBuilder();
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        Texture floorTex = createFloorTexture();
        floorEmissiveAttr = PBRColorAttribute.createEmissive(new Color(0.4f, 1f, 2f, 1f));
        Material floorMat = new Material(
                PBRTextureAttribute.createBaseColorTexture(floorTex),
                PBRColorAttribute.createBaseColorFactor(Color.WHITE),
                PBRFloatAttribute.createMetallic(0.9f),
                PBRFloatAttribute.createRoughness(0.2f),
                PBRTextureAttribute.createEmissiveTexture(floorTex),
                floorEmissiveAttr
        );

        mb.begin();
        MeshPartBuilder floorBuilder = mb.part("floor", GL20.GL_TRIANGLES, attr, floorMat);
        floorBuilder.setUVRange(0, 0, 15f, 15f);
        floorBuilder.rect(-35f, 0f, 35f, 35f, 0f, 35f, 35f, 0f, -35f, -35f, 0f, -35f, 0f, 1f, 0f);
        sceneManager.addScene(new Scene(new ModelInstance(mb.end())));

        Texture wallTex = createWallTexture();
        wallEmissiveAttr = PBRColorAttribute.createEmissive(new Color(2f, 0f, 1f, 1f));
        Material wallMat = new Material(
                PBRTextureAttribute.createBaseColorTexture(wallTex),
                PBRColorAttribute.createBaseColorFactor(Color.WHITE),
                PBRFloatAttribute.createMetallic(0.6f),
                PBRFloatAttribute.createRoughness(0.5f),
                PBRTextureAttribute.createEmissiveTexture(wallTex),
                wallEmissiveAttr
        );

        Model wallModel = mb.createBox(1f, 5f, 1f, wallMat, attr);

        addWall(wallModel, 0, 2.5f, -35, 70, 1, 1);
        addWall(wallModel, 0, 2.5f, 35, 70, 1, 1);
        addWall(wallModel, -35, 2.5f, 0, 1, 1, 70);
        addWall(wallModel, 35, 2.5f, 0, 1, 1, 70);

        addWall(wallModel, 0, 2.5f, 0, 15, 1, 1);
        addWall(wallModel, -7.5f, 2.5f, 5, 1, 1, 10);
        addWall(wallModel, 7.5f, 2.5f, -5, 1, 1, 10);

        addWall(wallModel, -15, 2.5f, -15, 1, 1, 15);
        addWall(wallModel, -15, 2.5f, 15, 1, 1, 15);
        addWall(wallModel, -25, 2.5f, 0, 10, 1, 1);

        addWall(wallModel, 15, 2.5f, 15, 1, 1, 15);
        addWall(wallModel, 15, 2.5f, -15, 1, 1, 15);
        addWall(wallModel, 25, 2.5f, 0, 10, 1, 1);

        addWall(wallModel, -20, 2.5f, 25, 15, 1, 1);
        addWall(wallModel, -12, 2.5f, 18, 1, 1, 14);

        addWall(wallModel, 20, 2.5f, -25, 15, 1, 1);
        addWall(wallModel, 12, 2.5f, -18, 1, 1, 14);

        Material boxMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(0.25f, 0.25f, 0.28f, 1f)),
                PBRFloatAttribute.createMetallic(1.0f),
                PBRFloatAttribute.createRoughness(0.35f),
                PBRColorAttribute.createEmissive(new Color(0.15f, 0.3f, 0.6f, 1f))
        );
        Model boxModel = mb.createBox(3f, 3f, 3f, boxMat, attr);

        addBox(boxModel, -5, 1.5f, -10);
        addBox(boxModel, 5, 1.5f, 10);
        addBox(boxModel, -20, 1.5f, -5);
        addBox(boxModel, 20, 1.5f, 5);
        addBox(boxModel, -10, 1.5f, 20);
        addBox(boxModel, 10, 1.5f, -20);
        addBox(boxModel, -25, 1.5f, 15);
        addBox(boxModel, 25, 1.5f, -15);
        addBox(boxModel, 0, 1.5f, -25);
        addBox(boxModel, 0, 1.5f, 25);

        Material pillarMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(0.1f, 0.1f, 0.1f, 1f)),
                PBRColorAttribute.createEmissive(new Color(0f, 2f, 2f, 1f)),
                PBRFloatAttribute.createMetallic(0.7f),
                PBRFloatAttribute.createRoughness(0.25f)
        );
        Model pillarModel = mb.createCylinder(1.5f, 1.5f, 8f, 20, pillarMat, attr);
        ModelInstance p1 = new ModelInstance(pillarModel); p1.transform.setToTranslation(-18, 4f, 0f);
        ModelInstance p2 = new ModelInstance(pillarModel); p2.transform.setToTranslation(18, 4f, 0f);
        sceneManager.addScene(new Scene(p1));
        sceneManager.addScene(new Scene(p2));

        if (!isMultiplayer) {
            Material targetMat = new Material(
                    PBRColorAttribute.createBaseColorFactor(new Color(0f, 1f, 0.3f, 1f)),
                    PBRColorAttribute.createEmissive(new Color(0f, 3f, 0.5f, 1f))
            );
            Model targetModel = mb.createBox(1.5f, 1.5f, 1.5f, targetMat, attr);
            Vector3[] tPos = { new Vector3(-20, 2.5f, -20), new Vector3(20, 2.5f, 20), new Vector3(0, 1.5f, 10) };
            for (int i = 0; i < 3; i++) {
                targets[i] = new ModelInstance(targetModel);
                targets[i].transform.setToTranslation(tPos[i]);
                sceneManager.addScene(new Scene(targets[i]));
            }
        }
    }

    private void addWall(Model template, float x, float y, float z, float w, float h, float d) {
        ModelInstance inst = new ModelInstance(template);
        inst.transform.setToTranslation(x, y, z).scale(w, h, d);
        sceneManager.addScene(new Scene(inst));
    }

    private void addBox(Model template, float x, float y, float z) {
        ModelInstance inst = new ModelInstance(template);
        inst.transform.setToTranslation(x, y, z);
        sceneManager.addScene(new Scene(inst));
    }

    // =========================================================
    //                    ЗАГРУЗКА БОТОВ
    // =========================================================
    public void setBotManager(BotManager bm) {
        this.botManager = bm;
    }

    /** Загружаем 5 сцен с моделью knight.glb (как у мультиплеерного врага) */
    public void loadBots() {
        if (botsLoaded) return;
        botsLoaded = true;

        try {
            SceneAsset asset = new GLBLoader().load(Gdx.files.internal("models/knight.glb"));

            // Парсим анимации
            for (Animation anim : asset.animations) {
                String id = anim.id.toLowerCase();
                if (id.contains("idle")) botIdleId = anim.id;
                if (id.contains("walk")) botWalkId = anim.id;
                if (id.contains("run") || id.contains("sprint")) botRunId = anim.id;
                if (id.contains("shoot") || id.contains("fire") || id.contains("attack")) botShootId = anim.id;
            }

            Gdx.app.log("BOTS", "Loaded knight.glb - idle=" + botIdleId
                    + " walk=" + botWalkId + " run=" + botRunId + " shoot=" + botShootId);

            // Создаём 5 сцен — каждая на своём ModelInstance (общий Model)
            for (int i = 0; i < 5; i++) {
                ModelInstance inst = new ModelInstance(asset.scene.model);
                Scene s = new Scene(inst);
                s.modelInstance.transform.setToTranslation(0, -1000, 0);
                sceneManager.addScene(s);
                botScenes[i] = s;
                botAnims[i] = new AnimationController(inst);
                if (botIdleId != null) {
                    botAnims[i].setAnimation(botIdleId, -1);
                    botAnimState[i] = 0;
                } else if (botWalkId != null) {
                    botAnims[i].setAnimation(botWalkId, -1);
                    botAnimState[i] = 1;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("BOTS", "Failed to load knight.glb, using fallback: " + e.getMessage());

            // Fallback — красные кубы
            ModelBuilder mb = new ModelBuilder();
            Material mat = new Material(
                    PBRColorAttribute.createBaseColorFactor(new Color(1f, 0.2f, 0.2f, 1f)),
                    PBRColorAttribute.createEmissive(new Color(2f, 0.2f, 0.2f, 1f))
            );
            Model box = mb.createBox(0.8f, 1.8f, 0.8f, mat,
                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

            for (int i = 0; i < 5; i++) {
                ModelInstance inst = new ModelInstance(box);
                Scene s = new Scene(inst);
                s.modelInstance.transform.setToTranslation(0, -1000, 0);
                sceneManager.addScene(s);
                botScenes[i] = s;
                botAnims[i] = null;
            }
        }
    }

    /** Синхронизируем 3D-позиции сцен с логикой ботов */
    private void updateBots(float delta) {
        if (botManager == null || !botsLoaded) return;

        Bot[] bots = botManager.getBots();
        for (int i = 0; i < 5 && i < bots.length; i++) {
            Bot bot = bots[i];
            Scene scene = botScenes[i];
            if (scene == null) continue;

            if (bot.dead) {
                scene.modelInstance.transform.setToTranslation(0, -1000, 0);
                continue;
            }

            float finalY = bot.pos.y + bot.yOffset;
            scene.modelInstance.transform.idt()
                    .setToTranslation(bot.pos.x, finalY, bot.pos.z)
                    .rotate(Vector3.Y, bot.yaw);

            AnimationController anim = botAnims[i];
            if (anim == null) continue;

            // Выбор анимации
            String targetAnim = null;
            int wantedState = 0;

            if (bot.shooting && botShootId != null) {
                targetAnim = botShootId;
                wantedState = 3;
            } else if (bot.isMoving && bot.walkAnimTime > 0.1f) {
                targetAnim = botRunId != null ? botRunId : botWalkId;
                wantedState = botRunId != null ? 2 : 1;
            } else {
                targetAnim = botIdleId;
                wantedState = 0;
            }

            if (targetAnim != null && (anim.current == null || !anim.current.animation.id.equals(targetAnim))) {
                if (bot.shooting) {
                    anim.setAnimation(targetAnim, 1);
                } else {
                    anim.setAnimation(targetAnim, -1);
                }
                botAnimState[i] = wantedState;
            }

            anim.update(delta);
        }
    }

    /** Трассер от выстрела бота — визуальный эффект */
    public void triggerBotShot(Vector3 fromPos, Vector3 hitPoint) {
        float len = fromPos.dst(hitPoint);
        if (len < 0.5f) return;

        Vector3 dir = hitPoint.cpy().sub(fromPos).nor();
        Vector3 mid = fromPos.cpy().lerp(hitPoint, 0.5f);
        Vector3 up = new Vector3(0, 0, 1);
        Vector3 cross = new Vector3(up).crs(dir);

        ModelInstance coreInst = new ModelInstance(tracerModel);
        coreInst.transform.idt().setToTranslation(mid);
        if (cross.len() > 0.001f) coreInst.transform.rotate(cross.nor(), (float)Math.toDegrees(Math.acos(up.dot(dir))));
        coreInst.transform.scale(1, 1, len);
        Scene coreScene = new Scene(coreInst);
        sceneManager.addScene(coreScene);

        ModelInstance glowInst = new ModelInstance(tracerGlowModel);
        glowInst.transform.idt().setToTranslation(mid);
        if (cross.len() > 0.001f) glowInst.transform.rotate(cross.nor(), (float)Math.toDegrees(Math.acos(up.dot(dir))));
        glowInst.transform.scale(1, 1, len * 0.9f);
        Scene glowScene = new Scene(glowInst);
        sceneManager.addScene(glowScene);

        tracers.add(new Tracer(coreScene, glowScene, 0.1f));
    }

    // =========================================================
    //                  МУЛЬТИПЛЕЕРНЫЙ ВРАГ
    // =========================================================
    private void loadArmor() {
        try {
            SceneAsset asset = new GLBLoader().load(Gdx.files.internal("models/armor.glb"));
            armorScene = new Scene(asset.scene);
            sceneManager.addScene(armorScene);
            createArmorOrbit();
            respawnArmorRandomly();
        } catch (Exception e) { Gdx.app.error("ARMOR", "Failed to load armor.glb"); }
    }

    private void createArmorOrbit() {
        ModelBuilder mb = new ModelBuilder();
        Material orbitMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(0.2f, 1f, 1f, 1f)),
                PBRColorAttribute.createEmissive(new Color(0f, 3f, 3f, 1f))
        );
        Model orbitModel = mb.createBox(0.15f, 0.15f, 0.15f, orbitMat,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        for (int i = 0; i < 6; i++) {
            ModelInstance inst = new ModelInstance(orbitModel);
            inst.transform.setToTranslation(0, -100, 0);
            Scene s = new Scene(inst);
            sceneManager.addScene(s);
            armorOrbitScenes.add(s);
        }
    }

    private void loadBomb() {
        try {
            SceneAsset asset = new GLBLoader().load(Gdx.files.internal("models/c4.glb"));
            bombScene = new Scene(asset.scene);
            bombScene.modelInstance.transform.setToTranslation(0, -100, 0);
            sceneManager.addScene(bombScene);
        } catch (Exception e) { Gdx.app.error("BOMB", "Failed to load c4.glb"); }
    }

    public void showBomb(Vector3 pos) { bombScene.modelInstance.transform.setToTranslation(pos); }
    public void hideBomb() { bombScene.modelInstance.transform.setToTranslation(0, -100, 0); }
    public boolean isArmorAvailable() { return armorAvailable; }
    public Vector3 getArmorPos() { return armorPos; }
    public void pickupArmor() {
        armorAvailable = false;
        armorScene.modelInstance.transform.setToTranslation(0, -100, 0);
        for (Scene s : armorOrbitScenes) s.modelInstance.transform.setToTranslation(0, -100, 0);
    }

    private void respawnArmorRandomly() {
        armorPos.set(-25 + (float)Math.random() * 50, 1.5f, -25 + (float)Math.random() * 50);
        armorScene.modelInstance.transform.setToTranslation(armorPos);
        armorAvailable = true;
    }

    private void loadWeapon() {
        try {
            SceneAsset asset = new GLBLoader().load(Gdx.files.internal("models/fps_ak-74m_animations.glb"));
            weaponScene = new Scene(asset.scene);
            sceneManager.addScene(weaponScene);

            animController = new AnimationController(weaponScene.modelInstance);
            for (Animation anim : asset.animations) {
                String id = anim.id.toLowerCase();
                if (id.contains("idle")) idleId = anim.id;
                if (id.contains("shoot") || id.contains("fire") || id.contains("shot")) shootId = anim.id;
                if (id.contains("reload")) { reloadId = anim.id; actualReloadDuration = anim.duration; }
                if (id.contains("walk")) walkId = anim.id;
                if (id.contains("run") || id.contains("sprint")) runId = anim.id;
            }
            if (idleId != null) animController.setAnimation(idleId, -1);
        } catch (Exception e) { Gdx.app.error("MODEL", e.getMessage()); }
    }

    public float getReloadDuration() { return actualReloadDuration; }
    public boolean isEnemyAlive() { return !isEnemyDead; }
    public Vector3 getEnemyPos() { return currentEnemyPos; }
    public void setEnemyDead(boolean dead) {
        this.isEnemyDead = dead;
        if (enemyScene != null) {
            enemyScene.modelInstance.transform.setToTranslation(0, -1000, 0);
            if (enemyWeaponScene != null) enemyWeaponScene.modelInstance.transform.setToTranslation(0, -1000, 0);
        }
    }
    public void respawnEnemy(float x, float z) {
        enemyTargetPos.set(x, 0, z);
        currentEnemyPos.set(x, 0, z);
        isEnemyDead = false;
    }

    private void loadEnemy() {
        try {
            SceneAsset asset = new GLBLoader().load(Gdx.files.internal("models/knight.glb"));
            enemyScene = new Scene(asset.scene);
            enemyScene.modelInstance.transform.setToTranslation(enemyTargetPos);
            sceneManager.addScene(enemyScene);

            enemyAnimController = new AnimationController(enemyScene.modelInstance);
            for (Animation anim : asset.animations) {
                String id = anim.id.toLowerCase();
                if (id.contains("idle")) enemyIdleId = anim.id;
                if (id.contains("walk")) enemyWalkId = anim.id;
                if (id.contains("run") || id.contains("sprint")) enemyRunId = anim.id;
                if (id.contains("shoot") || id.contains("fire") || id.contains("attack")) enemyShootId = anim.id;
            }
            if (enemyIdleId != null) { enemyAnimController.setAnimation(enemyIdleId, -1); currentEnemyAnimState = 0; }
        } catch (Exception e) {
            Gdx.app.error("ENEMY", "Failed to load knight.glb! " + e.getMessage());
            ModelBuilder mbEnemy = new ModelBuilder();
            Material enemyMat = new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createEmissive(Color.RED));
            Model enemyModel = mbEnemy.createBox(1f, 2f, 1f, enemyMat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
            ModelInstance enemyInst = new ModelInstance(enemyModel);
            enemyScene = new Scene(enemyInst);
            enemyScene.modelInstance.transform.setToTranslation(enemyTargetPos);
            sceneManager.addScene(enemyScene);
        }

        try {
            SceneAsset weaponAsset = new GLBLoader().load(Gdx.files.internal("models/akm.glb"));
            enemyWeaponScene = new Scene(weaponAsset.scene);
            enemyWeaponScene.modelInstance.transform.setToTranslation(enemyTargetPos).scale(0.4f, 0.4f, 0.4f);
            sceneManager.addScene(enemyWeaponScene);
        } catch (Exception e) {
            ModelBuilder mbW = new ModelBuilder();
            Model enemyWeaponModel = mbW.createBox(0.08f, 0.08f, 0.7f, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
            enemyWeaponScene = new Scene(new ModelInstance(enemyWeaponModel));
            enemyWeaponScene.modelInstance.transform.setToTranslation(enemyTargetPos);
            sceneManager.addScene(enemyWeaponScene);
        }
    }

    public void updateEnemyNetwork(float x, float z, float yaw, int animState) {
        if (isEnemyDead) return;
        enemyTargetPos.set(x, 0f, z);
        enemyTargetYaw = yaw;
        if (enemyAnimController != null && currentEnemyAnimState != animState) {
            String targetAnim = null;
            if (animState == 0) targetAnim = enemyIdleId;
            else if (animState == 1) targetAnim = enemyWalkId;
            else if (animState == 2) targetAnim = enemyRunId != null ? enemyRunId : enemyWalkId;
            if (targetAnim != null) { enemyAnimController.setAnimation(targetAnim, -1); currentEnemyAnimState = animState; }
        }
    }

    public void playEnemyShootAnim() {
        if (isEnemyDead) return;
        if (enemyShootId != null && enemyAnimController != null) {
            enemyAnimController.setAnimation(enemyShootId, 1);
        }
    }

    private void createEffects() {
        ModelBuilder mb = new ModelBuilder();
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        Material coreMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(1f, 1f, 0.95f, 1f)),
                PBRColorAttribute.createEmissive(new Color(14f, 14f, 13f, 1f))
        );
        Model coreModel = mb.createSphere(1f, 1f, 1f, 6, 6, coreMat, attr);

        Material petalMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(1f, 0.65f, 0.2f, 1f)),
                PBRColorAttribute.createEmissive(new Color(9f, 4.5f, 0.6f, 1f))
        );
        Model petalModel = mb.createBox(0.025f, 0.025f, 0.22f, petalMat, attr);

        ModelInstance core = new ModelInstance(coreModel);
        core.transform.setToTranslation(0, -1000, 0);
        sceneManager.addScene(new Scene(core));
        flashParts.add(core);

        for (int i = 0; i < 5; i++) {
            ModelInstance petal = new ModelInstance(petalModel);
            petal.transform.setToTranslation(0, -1000, 0);
            sceneManager.addScene(new Scene(petal));
            flashParts.add(petal);
        }

        Material tracerMat = new Material(
                PBRColorAttribute.createBaseColorFactor(Color.WHITE),
                PBRColorAttribute.createEmissive(new Color(4f, 4f, 0f, 1f))
        );
        tracerModel = mb.createBox(0.06f, 0.06f, 1f, tracerMat, attr);

        Material tracerGlowMat = new Material(
                PBRColorAttribute.createBaseColorFactor(new Color(1f, 0.7f, 0f, 1f)),
                PBRColorAttribute.createEmissive(new Color(2f, 1.4f, 0f, 1f))
        );
        tracerGlowModel = mb.createBox(0.22f, 0.22f, 1f, tracerGlowMat, attr);

        Material impactFlashMat = new Material(
                PBRColorAttribute.createBaseColorFactor(Color.WHITE),
                PBRColorAttribute.createEmissive(new Color(6f, 6f, 6f, 1f))
        );
        sharedFlashModel = mb.createSphere(1f, 1f, 1f, 10, 10, impactFlashMat, attr);

        Material smokeMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.25f, 0.25f, 0.28f, 0.5f)),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.5f)
        );
        smokeModel = mb.createSphere(1f, 1f, 1f, 8, 8, smokeMat, attr);
    }

    private void createAmbientEmbers() {
        int count;
        switch (GameSettings.effectsQuality) {
            case 0: count = 8; break;
            case 1: count = 16; break;
            case 3: count = 40; break;
            default: count = 30;
        }

        ModelBuilder mb = new ModelBuilder();
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        Color[] colors = {
                new Color(1f, 0.4f, 0.1f, 1f),
                new Color(0f, 0.8f, 1f, 1f),
                new Color(1f, 0.2f, 0.6f, 1f),
                new Color(0.8f, 0.3f, 1f, 1f)
        };

        for (int i = 0; i < count; i++) {
            Color c = colors[i % colors.length];
            Material emberMat = new Material(
                    PBRColorAttribute.createBaseColorFactor(c),
                    PBRColorAttribute.createEmissive(new Color(c.r * 4f, c.g * 4f, c.b * 4f, 1f))
            );
            float size = 0.06f + (float)Math.random() * 0.08f;
            Model emberModel = mb.createBox(size, size, size, emberMat, attr);
            ModelInstance inst = new ModelInstance(emberModel);
            inst.transform.setToTranslation(0, -100, 0);
            Scene s = new Scene(inst);
            sceneManager.addScene(s);

            float speed = 0.3f + (float)Math.random() * 0.7f;
            float phase = (float)Math.random() * 6.28f;
            float radius = 3f + (float)Math.random() * 25f;
            float height = 0.5f + (float)Math.random() * 1.5f;
            float baseY = 1f + (float)Math.random() * 10f;
            embers.add(new AmbientEmber(s, speed, phase, radius, height, baseY));
        }
    }

    public void render(float delta, PerspectiveCamera camera, Vector3 playerPos, boolean isAiming, boolean isFiring, boolean isReloading, float moveSpeed) {
        globalTime += delta;
        sceneManager.setCamera(camera);
        flashLight.position.set(camera.position).add(camera.direction.cpy().scl(1.0f));

        // === БОТЫ (обновляем визуал) ===
        updateBots(delta);

        // === ОСВЕЩЕНИЕ ===
        float lightMul = 0.4f + GameSettings.lightingQuality * 0.3f;
        for (int i = 0; i < neonLights.length; i++) {
            if (neonLights[i] != null) {
                float phase = i * 1.3f;
                float base = (i == 4 ? 4000f : 3000f) * lightMul;
                neonLights[i].intensity = base + MathUtils.sin(globalTime * 1.5f + phase) * base * 0.35f;
            }
        }

        // === EMISSIVE ===
        float bloomMul = GameSettings.bloom ? (1f + GameSettings.bloomIntensity * 0.5f) : 0.6f;
        float reflMul = GameSettings.reflections ? 1f : 0.2f;
        float emissiveMul = bloomMul * reflMul;

        if (floorEmissiveAttr != null) {
            float p = 0.7f + 0.3f * MathUtils.sin(globalTime * 0.9f);
            floorEmissiveAttr.color.set(
                    0.4f * p * emissiveMul,
                    1f * p * emissiveMul,
                    2f * p * emissiveMul,
                    1f);
        }
        if (wallEmissiveAttr != null) {
            float p = 0.7f + 0.3f * MathUtils.sin(globalTime * 1.2f + 2f);
            wallEmissiveAttr.color.set(
                    2f * p * emissiveMul, 0f,
                    1f * p * emissiveMul, 1f);
        }

        // === МУЛЬТИПЛЕЕРНЫЙ ENEMY ===
        if (isMultiplayer && enemyScene != null && !isEnemyDead) {
            enemyScene.modelInstance.transform.getTranslation(currentEnemyPos);
            if (currentEnemyPos.dst2(enemyTargetPos) > 25f) currentEnemyPos.set(enemyTargetPos);
            else currentEnemyPos.lerp(enemyTargetPos, 10f * delta);

            enemyScene.modelInstance.transform.idt().setToTranslation(currentEnemyPos).rotate(Vector3.Y, enemyTargetYaw);
            if (enemyAnimController != null) enemyAnimController.update(delta);

            if (enemyWeaponScene != null) {
                enemyWeaponScene.modelInstance.transform.idt()
                        .setToTranslation(currentEnemyPos)
                        .translate(0.3f, 1.2f, 0.5f)
                        .rotate(Vector3.Y, enemyTargetYaw)
                        .rotate(Vector3.Y, -90f)
                        .scale(0.4f, 0.4f, 0.4f);
                enemyWeaponScene.modelInstance.calculateTransforms();
            }
        }

        // === АРМОР ===
        if (isMultiplayer && armorAvailable && armorScene != null) {
            armorRot += 90f * delta;
            float bob = MathUtils.sin(armorRot * 0.05f) * 0.25f;
            armorScene.modelInstance.transform.idt()
                    .setToTranslation(armorPos)
                    .rotate(Vector3.Y, armorRot)
                    .translate(0, bob, 0);

            for (int i = 0; i < armorOrbitScenes.size; i++) {
                float ang = globalTime * 120f + i * (360f / armorOrbitScenes.size);
                float y = 0.8f + MathUtils.sin(globalTime * 3f + i) * 0.4f;
                armorOrbitScenes.get(i).modelInstance.transform.setToTranslation(
                        armorPos.x + MathUtils.cosDeg(ang) * 1.6f,
                        armorPos.y + y,
                        armorPos.z + MathUtils.sinDeg(ang) * 1.6f
                );
            }
        }

        // === BOMB ===
        if (bombScene != null) {
            Vector3 bombPos = new Vector3();
            bombScene.modelInstance.transform.getTranslation(bombPos);
            if (bombPos.y > -50f) {
                float p = 1f + 0.08f * MathUtils.sin(globalTime * 8f);
                bombScene.modelInstance.transform.idt().setToTranslation(bombPos).scale(p, p, p);
            }
        }

        // === ОРУЖИЕ ===
        if (animController != null) {
            if (!isReloading && !isFiring) {
                String target = idleId;
                if (moveSpeed > 0.7f && runId != null) target = runId;
                else if (moveSpeed > 0.15f && walkId != null) target = walkId;
                if (target != null && (animController.current == null || !animController.current.animation.id.equals(target))) animController.setAnimation(target, -1);
            }
            animController.update(delta);
        }

        Vector3 targetPos = isAiming ? adsPos : hipPos;
        Vector3 targetRot = isAiming ? adsRot : hipRot;
        currentGunPos.lerp(targetPos, 10 * delta);
        currentGunRot.x = MathUtils.lerp(currentGunRot.x, targetRot.x, 10 * delta);
        currentGunRot.y = MathUtils.lerp(currentGunRot.y, targetRot.y, 10 * delta);

        if (GameSettings.weaponSway > 0.01f && moveSpeed > 0.15f) {
            swayTime += delta * (moveSpeed > 0.7f ? 10f : 7f);
        } else {
            swayTime = MathUtils.lerp(swayTime, 0f, 5f * delta);
        }
        float swayX = MathUtils.sin(swayTime) * 0.015f * GameSettings.weaponSway;
        float swayY = MathUtils.cos(swayTime * 2f) * 0.010f * GameSettings.weaponSway;
        float swayRot = MathUtils.sin(swayTime * 0.5f) * 1.5f * GameSettings.weaponSway;

        if (weaponScene != null) {
            Matrix4 gunTransform = new Matrix4(camera.view).inv();
            gunTransform
                    .rotate(Vector3.Y, currentGunRot.y + swayRot)
                    .rotate(Vector3.X, currentGunRot.x)
                    .translate(currentGunPos.x + swayX, currentGunPos.y + swayY, currentGunPos.z);
            weaponScene.modelInstance.transform.set(gunTransform);

            float cx = (isAiming ? adsMuzzleXPercent : hipMuzzleXPercent) * Gdx.graphics.getWidth();
            float cy = (isAiming ? adsMuzzleYPercent : hipMuzzleYPercent) * Gdx.graphics.getHeight();
            Ray muzzleRay = camera.getPickRay(cx, cy);
            currentMuzzleWorldPos.set(muzzleRay.direction).scl(1.0f).add(camera.position);

            if (muzzleFlashTimer > 0) {
                muzzleFlashTimer -= delta;

                Vector3 gunDir = muzzleRay.direction.cpy().nor();
                Vector3 zAxis = new Vector3(0, 0, 1);
                Vector3 rotAxis = new Vector3(zAxis).crs(gunDir);
                float rotAngle = (float) Math.toDegrees(Math.acos(
                        MathUtils.clamp(zAxis.dot(gunDir), -1f, 1f)));

                ModelInstance core = flashParts.get(0);
                core.transform.idt().setToTranslation(currentMuzzleWorldPos);
                float coreSize = 0.035f + (float) Math.random() * 0.015f;
                core.transform.scale(coreSize, coreSize, coreSize);

                for (int i = 0; i < 5; i++) {
                    ModelInstance petal = flashParts.get(1 + i);
                    petal.transform.idt().setToTranslation(currentMuzzleWorldPos);

                    if (rotAxis.len() > 0.001f) {
                        petal.transform.rotate(rotAxis.nor(), rotAngle);
                    }

                    if (i == 1)      petal.transform.rotate(Vector3.X, 28f);
                    else if (i == 2) petal.transform.rotate(Vector3.X, -28f);
                    else if (i == 3) petal.transform.rotate(Vector3.Y, 28f);
                    else if (i == 4) petal.transform.rotate(Vector3.Y, -28f);

                    petal.transform.rotate(Vector3.Y, (float)(Math.random() - 0.5) * 22f);
                    petal.transform.rotate(Vector3.X, (float)(Math.random() - 0.5) * 12f);
                    petal.transform.translate(0, 0, 0.09f);

                    float lenScale = (i == 0) ? 1.25f : 0.85f;
                    lenScale *= 0.85f + (float) Math.random() * 0.3f;
                    float wScale = 0.7f + (float) Math.random() * 0.35f;
                    petal.transform.scale(wScale, wScale, lenScale);
                }

                muzzleLight.intensity = 22000f;
                muzzleLight.position.set(currentMuzzleWorldPos).add(gunDir.scl(0.25f));
            } else {
                for (ModelInstance p : flashParts) p.transform.setToTranslation(0, -1000, 0);
                muzzleLight.intensity = MathUtils.lerp(muzzleLight.intensity, 0f, 25f * delta);
            }
        }

        // === ИСКРЫ ===
        for (int i = sparks.size - 1; i >= 0; i--) {
            Spark s = sparks.get(i);
            s.life -= delta;
            if (s.life <= 0) { sceneManager.removeScene(s.scene); sparks.removeIndex(i); }
            else {
                s.velocity.y -= 12f * delta;
                Vector3 tmp = new Vector3();
                s.scene.modelInstance.transform.getTranslation(tmp);
                tmp.add(s.velocity.cpy().scl(delta));
                float t = s.life / s.maxLife;
                float scale = 0.4f + t * 0.8f;
                s.scene.modelInstance.transform.idt().setToTranslation(tmp).scale(scale, scale, scale);
            }
        }

        // === ТРАССЕРЫ ===
        for (int i = tracers.size - 1; i >= 0; i--) {
            Tracer t = tracers.get(i);
            t.life -= delta;
            if (t.life <= 0) {
                sceneManager.removeScene(t.scene);
                if (t.glowScene != null) sceneManager.removeScene(t.glowScene);
                tracers.removeIndex(i);
            }
        }

        // === IMPACT FLASHES ===
        for (int i = impactFlashes.size - 1; i >= 0; i--) {
            ImpactFlash f = impactFlashes.get(i);
            f.life -= delta;
            if (f.life <= 0) {
                sceneManager.removeScene(f.scene);
                impactFlashes.removeIndex(i);
            } else {
                float t = f.life / f.maxLife;
                float s = 0.3f + (1f - t) * 2.2f;
                f.scene.modelInstance.transform.scale(s, s, s);
            }
        }

        // === EMBERS ===
        if (GameSettings.particles) {
            for (AmbientEmber e : embers) {
                float x = MathUtils.cos(globalTime * e.speed + e.phase) * e.radius;
                float z = MathUtils.sin(globalTime * e.speed + e.phase) * e.radius;
                float y = e.baseY + MathUtils.sin(globalTime * e.speed * 1.7f + e.phase) * e.heightOffset;
                e.scene.modelInstance.transform.idt().setToTranslation(x, y, z);
            }
        } else {
            for (AmbientEmber e : embers) {
                e.scene.modelInstance.transform.idt().setToTranslation(0, -1000, 0);
            }
        }

        sceneManager.update(delta);
        sceneManager.render();
    }

    public void playReloadAnim() { if (reloadId != null && animController != null) animController.setAnimation(reloadId, 1); }
    public void playShootAnim() { if (shootId != null && animController != null) { animController.current = null; animController.setAnimation(shootId, 1); } }

    public void triggerShootEffects(Vector3 hitPoint, boolean hitTarget) {
        muzzleFlashTimer = 0.045f;

        Vector3 dir = hitPoint.cpy().sub(currentMuzzleWorldPos).nor();
        Vector3 mid = currentMuzzleWorldPos.cpy().lerp(hitPoint, 0.5f);
        Vector3 up = new Vector3(0, 0, 1);
        Vector3 cross = new Vector3(up).crs(dir);
        float len = currentMuzzleWorldPos.dst(hitPoint);

        ModelInstance coreInst = new ModelInstance(tracerModel);
        coreInst.transform.idt().setToTranslation(mid);
        if (cross.len() > 0.001f) coreInst.transform.rotate(cross.nor(), (float)Math.toDegrees(Math.acos(up.dot(dir))));
        coreInst.transform.scale(1, 1, len);
        Scene coreScene = new Scene(coreInst);
        sceneManager.addScene(coreScene);

        ModelInstance glowInst = new ModelInstance(tracerGlowModel);
        glowInst.transform.idt().setToTranslation(mid);
        if (cross.len() > 0.001f) glowInst.transform.rotate(cross.nor(), (float)Math.toDegrees(Math.acos(up.dot(dir))));
        glowInst.transform.scale(1, 1, len * 0.9f);
        Scene glowScene = new Scene(glowInst);
        sceneManager.addScene(glowScene);

        tracers.add(new Tracer(coreScene, glowScene, 0.1f));

        ModelInstance flashInst = new ModelInstance(sharedFlashModel);
        flashInst.transform.idt().setToTranslation(hitPoint);
        Scene flashScene = new Scene(flashInst);
        sceneManager.addScene(flashScene);
        impactFlashes.add(new ImpactFlash(flashScene, 0.12f));

        if (!GameSettings.particles) return;

        int baseCount;
        switch (GameSettings.effectsQuality) {
            case 0: baseCount = 4; break;
            case 1: baseCount = 8; break;
            case 3: baseCount = 18; break;
            default: baseCount = 14;
        }
        int count = hitTarget ? baseCount : (int)(baseCount * 0.7f);

        if (count > 0) {
            Color sparkColor = hitTarget ? new Color(0f, 1f, 1f, 1f) : new Color(1f, 0.6f, 0.1f, 1f);
            Material sparkMat = new Material(
                    PBRColorAttribute.createBaseColorFactor(sparkColor),
                    PBRColorAttribute.createEmissive(new Color(sparkColor.r * 4f, sparkColor.g * 4f, sparkColor.b * 4f, 1f))
            );
            ModelBuilder mb = new ModelBuilder();
            int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
            float size = 0.06f + (float)Math.random() * 0.04f;
            Model sModel = mb.createBox(size, size, size, sparkMat, attr);

            for (int i = 0; i < count; i++) {
                ModelInstance sInst = new ModelInstance(sModel);
                sInst.transform.setToTranslation(hitPoint);
                Vector3 vel = new Vector3(
                        (float)(Math.random() - 0.5) * 8f,
                        (float)(Math.random() - 0.3) * 8f,
                        (float)(Math.random() - 0.5) * 8f
                );
                Scene sScene = new Scene(sInst);
                sceneManager.addScene(sScene);
                sparks.add(new Spark(sScene, vel, 0.5f + (float)Math.random() * 0.3f));
            }
        }

        if (GameSettings.effectsQuality >= 1) {
            ModelInstance smokeInst = new ModelInstance(smokeModel);
            smokeInst.transform.idt().setToTranslation(hitPoint).scale(0.5f, 0.5f, 0.5f);
            Scene smokeScene = new Scene(smokeInst);
            sceneManager.addScene(smokeScene);
            impactFlashes.add(new ImpactFlash(smokeScene, 0.35f));
        }
    }

    public void hideTarget(int index) { targets[index].transform.setToTranslation(0, -100f, 0); }
    public void showTarget(int index) {
        Vector3[] tPos = { new Vector3(-20, 2.5f, -20), new Vector3(20, 2.5f, 20), new Vector3(0, 1.5f, 10) };
        targets[index].transform.setToTranslation(tPos[index]);
    }

    public void dispose() { sceneManager.dispose(); }
}