package com.snips.bh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.snips.bh.actor.*;

public class PlayScreen extends ScreenAdapter {
    // Match your GameMain world size (from the uploaded file)
    public static final float WORLD_W = 450f;
    public static final float WORLD_H = 1000f;

    private final SnipsBHGame game;
    private OrthographicCamera camera;
    private FitViewport viewport;

    // Textures (as in GameMain)
    private Texture bg;
    private Texture soldier;
    private Texture zombie;
    private Array<Texture> zombieTextures;

    // Gameplay state
    private Player player;
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Bullet> bullets = new Array<>();
    private final Weapon weapon = new Weapon();
    private EnemySpawner spawner;

    // Scratch
    private final Vector2 tmp = new Vector2();

    public PlayScreen(SnipsBHGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Camera & viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Blending (as you had in GameMain.create)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // === Load textures (from your GameMain) ===
        bg = new Texture(Gdx.files.internal("bg_street.png"));
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        soldier = new Texture(Gdx.files.internal("soldier.png"));
        soldier.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        zombieTextures = new Array<>();
        zombie = new Texture(Gdx.files.internal("zombie.png"));
        zombie.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        zombieTextures.add(zombie);

        // Init spawner with textures
        spawner = new EnemySpawner(zombieTextures);

        // Player starts center with the soldier texture (as in GameMain)
        player = new Player(WORLD_W / 2f, WORLD_H / 2f, soldier);
    }

    @Override
    public void render(float dt) {
        // ---- UPDATE ----
        // Simple pause-to-menu with ESC (optional)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Update player
        player.update(dt, WORLD_W, WORLD_H);

        // Spawn/update enemies
        spawner.updateAndMaybeSpawn(dt, enemies, WORLD_W, WORLD_H);
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(dt, player.getPos());
            e.faceToward(player.getPos());
            if (!e.isAlive()) enemies.removeIndex(i);
        }

        // Auto-fire at nearest enemy and update bullets
        weapon.updateAuto(dt, player.getPos(), enemies, bullets);
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(dt);
            if (!b.alive) bullets.removeIndex(i);
        }

        // --- Player vs Enemy touch damage ---
        // Use player's touchDamageCooldown to throttle damage-on-contact
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            float rad = e.r + player.r;
            if (e.pos.dst2(player.pos) <= rad * rad) {
                if (player.touchDamageCooldown <= 0f) {
                    player.damage(10f);
                    player.touchDamageCooldown = 0.50f; // half-second i-frames
                }
            }
        }
        if (player.touchDamageCooldown > 0f) {
            player.touchDamageCooldown -= dt;
            if (player.touchDamageCooldown < 0f) player.touchDamageCooldown = 0f;
        }

        // --- Enemy vs Enemy simple separation (prevents perfect overlap) ---
        for (int i = 0; i < enemies.size; i++) {
            Enemy a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Enemy b = enemies.get(j);
                float overlap = (a.r + b.r) - (float)Math.sqrt(a.pos.dst2(b.pos));
                if (overlap > 0f) {
                    // push both away a bit along their separating axis
                    tmp.set(b.pos).sub(a.pos);
                    if (tmp.isZero()) tmp.set(1f, 0f);
                    tmp.nor().scl(overlap * 0.5f);
                    a.pos.sub(tmp);
                    b.pos.add(tmp);
                }
            }
        }

        // --- Bullet vs Enemy collisions ---
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            boolean consumed = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                float rr = e.r * e.r;
                if (e.pos.dst2(b.pos) <= rr) {
                    e.damage(b.damage);
                    b.kill();
                    consumed = true;
                    if (!e.isAlive()) enemies.removeIndex(j);
                    break;
                }
            }
            if (consumed) bullets.removeIndex(i);
        }

        // ---- DRAW ----
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final SpriteBatch batch = game.batch;
        final ShapeRenderer shapes = game.shapes;

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Background
        batch.begin();
        batch.draw(bg, 0, 0, WORLD_W, WORLD_H);
        // Player & enemies
        player.render(batch);
        for (Enemy e : enemies) e.render(batch);
        batch.end();

        // HP bars + bullets (ShapeRenderer)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.renderHpBar(shapes);
        for (Enemy e : enemies) e.renderHpBar(shapes);
        for (Bullet b : bullets) b.render(shapes);
        shapes.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        // dispose of textures we created here
        if (bg != null) bg.dispose();
        if (soldier != null) soldier.dispose();
        if (zombie != null) zombie.dispose();
        if (zombieTextures != null) {
            for (Texture t : zombieTextures) if (t != null) t.dispose();
        }
    }
}
