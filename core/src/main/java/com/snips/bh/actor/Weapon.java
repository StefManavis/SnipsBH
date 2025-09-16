package com.snips.bh.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Weapon {
    // --- Config ---
    private float shotsPerSecond = 6f;
    private float bulletSpeed = 650f;
    private float bulletDamage = 10f;
    private float bulletRadius = 3f;
    private float bulletLifeSec = 2.5f;
    private float spreadDeg = 0f;
    private int   projectilesPerShot = 1;
    private boolean automatic = true; // currently unused but harmless
    public float maxTargetRange = 800f;

    // --- State ---
    private boolean triggerDown = false; // currently unused but harmless
    private float cooldown = 0f;

    // --- Scratch ---
    private final Vector2 tmp = new Vector2();

    // --------- Fluent config (optional convenience) ---------
    public Weapon rate(float shotsPerSecond) {
        if (shotsPerSecond < 0.1f) {
            this.shotsPerSecond = 0.1f;
        } else {
            this.shotsPerSecond = shotsPerSecond;
        }
        return this;
    }

    public Weapon bulletSpeed(float s) {
        this.bulletSpeed = s;
        return this;
    }

    public Weapon damage(float d) {
        this.bulletDamage = d;
        return this;
    }

    public Weapon bulletRadius(float r) {
        this.bulletRadius = r;
        return this;
    }

    public Weapon bulletLife(float sec) {
        this.bulletLifeSec = sec;
        return this;
    }

    public Weapon spreadDeg(float deg) {
        if (deg < 0f) {
            this.spreadDeg = 0f;
        } else {
            this.spreadDeg = deg;
        }
        return this;
    }

    public Weapon pellets(int n) {
        if (n < 1) {
            this.projectilesPerShot = 1;
        } else {
            this.projectilesPerShot = n;
        }
        return this;
    }

    public Weapon maxRange(float px) {
        if (px < 1f) {
            this.maxTargetRange = 1f;
        } else {
            this.maxTargetRange = px;
        }
        return this;
    }

    // --------- AUTO AIM CODE ---------
    public void updateAuto(float dt,
                           Vector2 origin,
                           Array<? extends Targetable> enemies,
                           Array<Bullet> outBullets) {

        cooldown = Math.max(0f, cooldown - dt);

        Targetable t = findNearestAlive(origin, enemies, maxTargetRange);
        if (t == null) {
            return;
        }

        if (cooldown <= 0f) {
            // IMPORTANT: don't mutate t.getPos(); copy into tmp, then subtract origin
            Vector2 aimDir = tmp.set(t.getPos()).sub(origin);
            if (aimDir.len2() > 0.0001f) {
                shoot(origin, aimDir.nor(), outBullets);
                cooldown = 1f / shotsPerSecond;
            } else {
                // Direction too small; skip this frame.
            }
        } else {
            // Still cooling down; skip firing this frame.
        }
    }

    private Targetable findNearestAlive(Vector2 origin,
                                        Array<? extends Targetable> enemies,
                                        float maxRangePx) {
        if (enemies == null) {
            return null;
        }

        if (enemies.size == 0) {
            return null;
        }

        float bestDst2 = maxRangePx * maxRangePx;
        Targetable best = null;

        for (int i = 0; i < enemies.size; i++) {
            Targetable e = enemies.get(i);

            if (e == null) {
                continue;
            }

            if (!e.isAlive()) {
                continue;
            }

            float d2 = origin.dst2(e.getPos());
            if (d2 < bestDst2) {
                bestDst2 = d2;
                best = e;
            } else {
                // Not closer than current best; keep searching.
            }
        }

        if (best == null) {
            return null;
        } else {
            return best;
        }
    }

    private void shoot(Vector2 origin, Vector2 aimDir, Array<Bullet> outBullets) {
        tmp.set(aimDir).nor();

        for (int i = 0; i < projectilesPerShot; i++) {
            Vector2 dir = tmp.cpy();

            if (spreadDeg > 0f) {
                float angle = MathUtils.random(-spreadDeg, spreadDeg);
                dir.rotateDeg(angle);
            } else {
                // No spread requested; keep direction as-is.
            }

            Vector2 vel = dir.scl(bulletSpeed);
            Bullet b = new Bullet(origin, vel, bulletRadius, bulletDamage, bulletLifeSec);
            outBullets.add(b);
        }
    }
}
