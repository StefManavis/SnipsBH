package com.snips.bh.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Weapon {
    // simple auto-fire weapon towards nearest enemy
    private float cooldown = 0f;
    private float fireRate = 10f; // bullets per second
    private float bulletSpeed = 900f;
    private float bulletRadius = 4f;

    public void updateAuto(float dt, Vector2 playerPos, Array<Enemy> enemies, Array<Bullet> bullets) {
        cooldown -= dt;
        if (cooldown > 0f) return;

        Enemy target = findNearest(playerPos, enemies);
        if (target == null) return;

        // ready to fire
        cooldown = 1f / fireRate;

        float dx = target.pos.x - playerPos.x;
        float dy = target.pos.y - playerPos.y;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1e-4f) return;

        float vx = (dx / len) * bulletSpeed;
        float vy = (dy / len) * bulletSpeed;

        bullets.add(new Bullet(playerPos.x, playerPos.y, vx, vy, bulletRadius));
    }

    private Enemy findNearest(Vector2 p, Array<Enemy> enemies) {
        Enemy best = null;
        float bestD2 = Float.MAX_VALUE;
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (!e.isAlive()) continue;
            float d2 = e.pos.dst2(p);
            if (d2 < bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }
}
