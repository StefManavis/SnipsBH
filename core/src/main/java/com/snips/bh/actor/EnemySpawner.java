package com.snips.bh.actor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


public class EnemySpawner {

    private float timer = 0f;
    private float interval = 1.5f; // seconds
    private Array<Texture> zombieTextures;

    public EnemySpawner(Array<Texture> zombieTextures) {
        this.zombieTextures = zombieTextures;
    }

    public void updateAndMaybeSpawn(float dt, Array<Enemy> enemies, float worldW, float worldH) {
        timer += dt;
        if (timer >= interval) {
            timer -= interval;

            // pick a random zombie texture
            Texture tex = zombieTextures.random();

            // spawn at random edge
            float side = MathUtils.random(0, 3);
            Vector2 p = new Vector2();
            if (side == 0)      { p.set(-20f, MathUtils.random(0f, worldH)); }
            else if (side == 1) { p.set(worldW + 20f, MathUtils.random(0f, worldH)); }
            else if (side == 2) { p.set(MathUtils.random(0f, worldW), -20f); }
            else                { p.set(MathUtils.random(0f, worldW), worldH + 20f); }

            enemies.add(new Enemy(p.x, p.y, tex));
        }
    }
}
