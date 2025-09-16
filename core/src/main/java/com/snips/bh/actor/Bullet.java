package com.snips.bh.actor;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();
    public float r = 3f;
    public float damage = 10f;
    public boolean alive = true;

    // Lifetime cap (seconds) to auto-despawn
    private float time = 0f, maxLife = 3.0f;

    // Reusable bounds circle to avoid GC (don’t hold on to this outside!)
    private final Circle bounds = new Circle();

    public Bullet(Vector2 origin, Vector2 velocity, float radius, float damage, float maxLifeSec) {
        this.pos.set(origin);
        this.vel.set(velocity);
        this.r = radius;
        this.damage = damage;
        this.maxLife = maxLifeSec;
    }

    public void update(float dt) {
        if (!alive) return;
        pos.mulAdd(vel, dt);
        time += dt;
        if (time >= maxLife) alive = false;
    }

    /** Returns a reusable circle for broad-phase checks. Valid only until next call. */
    public Circle getBounds() {
        bounds.set(pos.x, pos.y, r); // libGDX Circle#set(float x, float y, float radius)
        return bounds;
    }
}
