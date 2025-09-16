package com.snips.bh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.snips.bh.actor.Bullet;
import com.snips.bh.actor.Enemy;
import com.snips.bh.actor.EnemySpawner;
import com.snips.bh.actor.Player;
import com.snips.bh.actor.Weapon;

public class GameMain extends ApplicationAdapter {
    // Virtual world size
    public static final float WORLD_W = 800f;
    public static final float WORLD_H = 450f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private Player player;

    // ENEMIES
    private final Array<Enemy> enemies = new Array<>();
    private final EnemySpawner spawner = new EnemySpawner();

    // WEAPON & BULLETS
    private final Weapon weapon = new Weapon();       // defaults ok; tune later or swap subclass (e.g., new SMG())
    private final Array<Bullet> bullets = new Array<>();

    @Override
    public void create() {
        // Enable blending to allow alpha in shapes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Initialize camera/viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);

        shapes = new ShapeRenderer();

        // Start player in the center
        player = new Player(WORLD_W / 2f, WORLD_H / 2f);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        if (dt > 0.033f) {
            // clamp very large frames (minimize tunneling)
            dt = 0.033f;
        }

        // --- Exit on Shift + Enter ---
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
            Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            Gdx.app.exit(); // triggers dispose() safely
        }

        // --- Update player ---
        player.update(dt, WORLD_W, WORLD_H);
        if (player.touchDamageCooldown > 0f) {
            player.touchDamageCooldown -= dt;
            if (player.touchDamageCooldown < 0f) {
                player.touchDamageCooldown = 0f;
            }
        }

        // --- Spawner & enemies update ---
        spawner.updateAndMaybeSpawn(dt, enemies, WORLD_W, WORLD_H);

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(dt, player.pos);

            // optional cleanup: if it flies way off (after overshooting)
            float margin = 200f;
            if (e.pos.x < -margin || e.pos.x > WORLD_W + margin ||
                e.pos.y < -margin || e.pos.y > WORLD_H + margin) {
                enemies.removeIndex(i);
                continue;
            }

            if (!e.isAlive()) {
                enemies.removeIndex(i);
            }
        }

        // --- Weapon auto-aim & fire (spawns bullets) ---
        weapon.updateAuto(dt, player.pos, enemies, bullets);

        // --- Bullets update & bullet→enemy collisions ---
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);

            if (!b.alive) {
                bullets.removeIndex(i);
                continue;
            }

            // collide against enemies (simple circle overlap)
            boolean consumed = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (!e.isAlive()) {
                    continue;
                }

                float rr = (b.r + e.r);
                rr *= rr;
                if (b.pos.dst2(e.pos) <= rr) {
                    e.damage(b.damage);
                    b.alive = false;
                    consumed = true;

                    if (!e.isAlive()) {
                        enemies.removeIndex(j);
                        // TODO: add score / VFX
                    }
                    break; // bullet consumed
                }
            }

            if (consumed) {
                bullets.removeIndex(i);
            }
        }

        // --- COLLISIONS: Enemy vs Player (push-out + touch damage) ---
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);

            float dx = e.pos.x - player.pos.x;
            float dy = e.pos.y - player.pos.y;
            float minDist = e.r + player.r;
            float dist2 = dx * dx + dy * dy;

            if (dist2 > 0f && dist2 < minDist * minDist) {
                float dist = (float) Math.sqrt(dist2);
                float overlap = minDist - dist;

                // normal
                float nx = dx / dist;
                float ny = dy / dist;

                // Push the ENEMY out so it cannot enter the player
                e.pos.x += nx * overlap;
                e.pos.y += ny * overlap;

                // Optional: touch damage with cooldown
                if (player.touchDamageCooldown <= 0f) {
                    player.damage(5f);                 // tune this value
                    player.touchDamageCooldown = 0.4f; // 400 ms i-frames
                }
            }
        }

        // --- COLLISIONS: Enemy vs Enemy (soft separation) ---
        for (int i = 0; i < enemies.size; i++) {
            Enemy a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Enemy b = enemies.get(j);

                float dx = b.pos.x - a.pos.x;
                float dy = b.pos.y - a.pos.y;
                float minDist = a.r + b.r;
                float dist2 = dx * dx + dy * dy;

                if (dist2 > 0f && dist2 < minDist * minDist) {
                    float dist = (float) Math.sqrt(dist2);
                    float overlap = (minDist - dist);

                    // normal
                    float nx = dx / dist;
                    float ny = dy / dist;

                    // equal mass: split the correction 50/50
                    float half = overlap * 0.5f;
                    a.pos.x -= nx * half;
                    a.pos.y -= ny * half;
                    b.pos.x += nx * half;
                    b.pos.y += ny * half;
                }
            }
        }

        // --- Clear & set matrices ---
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);

        // --- RENDER ---
        shapes.begin(ShapeType.Filled);

        // Player (white)
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.circle(player.pos.x, player.pos.y, player.r);

        // Player HP bar (only after taking damage)
        if (player.hasTakenDamage()) {
            float pct = player.healthPct();
            float barW = 30f;
            float barH = 6f;
            float barX = player.pos.x - barW / 2f;
            float barY = player.pos.y + player.r + 8f; // a little above the head

            // background (semi-transparent dark)
            shapes.setColor(0f, 0f, 0f, 0.35f);
            shapes.rect(barX, barY, barW, barH);

            // fill (semi-transparent green)
            shapes.setColor(0f, 1f, 0f, 0.7f);
            shapes.rect(barX, barY, barW * pct, barH);
        }

        // Bullets (light gray)
        shapes.setColor(0.85f, 0.85f, 0.9f, 1f);
        for (int i = 0; i < bullets.size; i++) {
            Bullet b = bullets.get(i);
            shapes.circle(b.pos.x, b.pos.y, b.r);
        }

        // Enemies (red)
        shapes.setColor(1f, 0f, 0f, 1f);
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).render(shapes);
        }

        shapes.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
