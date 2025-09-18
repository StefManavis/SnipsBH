package com.snips.bh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.snips.bh.actor.*;

public class GameMain extends ApplicationAdapter {
    public static final float WORLD_H = 1000f;
    public static final float WORLD_W = 450f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;

    private Texture bg;
    private Texture soldier;
    private Texture zombie;

    private Player player;

    private final Array<Enemy> enemies = new Array<>();
    private final Weapon weapon = new Weapon();
    private final Array<Bullet> bullets = new Array<>();

    private Array<Texture> zombieTextures;
    private EnemySpawner spawner;

    @Override
    public void create() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);

        shapes = new ShapeRenderer();
        batch  = new SpriteBatch();

        // background and player texture
        bg = new Texture(Gdx.files.internal("bg_street.png"));
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        soldier = new Texture(Gdx.files.internal("soldier.png"));
        soldier.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // load zombie textures
        zombieTextures = new Array<>();
        zombie = new Texture(Gdx.files.internal("zombie.png"));
        zombie.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        zombieTextures.add(zombie);

        // now initialize spawner with textures
        spawner = new EnemySpawner(zombieTextures);

        // create player in center
        player = new Player(WORLD_W / 2f, WORLD_H / 2f, soldier);
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.033f);

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
            Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            Gdx.app.exit();
        }

        update(dt);
        draw();
    }

    private void update(float dt) {
        player.update(dt, WORLD_W, WORLD_H);

        spawner.updateAndMaybeSpawn(dt, enemies, WORLD_W, WORLD_H);
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(dt, player.getPos());
            e.faceToward(player.getPos());
            if (!e.isAlive()) enemies.removeIndex(i);
        }

        // aim player toward nearest enemy
        Enemy nearest = findNearestEnemy(player.getPos());
        if (nearest != null) player.faceToward(nearest.getPos());

        weapon.updateAuto(dt, player.getPos(), enemies, bullets);

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);
            if (!b.alive) bullets.removeIndex(i);
        }

        handleCollisions();
    }

    private Enemy findNearestEnemy(com.badlogic.gdx.math.Vector2 p) {
        Enemy best = null;
        float bestD2 = Float.MAX_VALUE;
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (!e.isAlive()) continue;
            float d2 = e.getPos().dst2(p);
            if (d2 < bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }

    private void handleCollisions() {
        // Enemy vs Player push-out + touch damage
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            float dx = e.pos.x - player.pos.x;
            float dy = e.pos.y - player.pos.y;
            float minDist = e.r + player.r;
            float dist2 = dx * dx + dy * dy;
            if (dist2 > 0f && dist2 < minDist * minDist) {
                float dist = (float) Math.sqrt(dist2);
                float overlap = minDist - dist;
                float nx = dx / dist, ny = dy / dist;
                e.pos.x += nx * overlap;
                e.pos.y += ny * overlap;
                if (player.touchDamageCooldown <= 0f) {
                    player.damage(5f);
                    player.touchDamageCooldown = 0.4f;
                }
            }
        }

        // Enemy vs Enemy soft separation
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
                    float overlap = (minDist - dist) * 0.5f;
                    float nx = dx / dist, ny = dy / dist;
                    a.pos.x -= nx * overlap;
                    a.pos.y -= ny * overlap;
                    b.pos.x += nx * overlap;
                    b.pos.y += ny * overlap;
                }
            }
        }

        // Bullet -> Enemy
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            boolean consumed = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (!e.isAlive()) continue;
                float rr = b.r + e.r; rr *= rr;
                if (b.pos.dst2(e.pos) <= rr) {
                    e.damage(b.damage);
                    b.kill();
                    consumed = true;
                    if (!e.isAlive()) enemies.removeIndex(j);
                    break;
                }
            }
            if (consumed) bullets.removeIndex(i);
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bg, 0, 0, WORLD_W, WORLD_H);
        player.render(batch);
        for (Enemy e : enemies) {
            e.render(batch);
        }
        batch.end();

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeType.Filled);

        player.renderHpBar(shapes);
        for (Enemy e : enemies) {
            e.renderHpBar(shapes);
        }

        // bullets
        for (Bullet b : bullets) {
            b.render(shapes);
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
        batch.dispose();
        bg.dispose();
        soldier.dispose();
        zombie.dispose();
        for (Texture t : zombieTextures) {
            t.dispose();
        }
    }
}
