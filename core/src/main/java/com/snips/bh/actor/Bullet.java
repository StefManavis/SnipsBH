package com.snips.bh.actor;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();
    public float r = 1f;
    public float speed = 900f;
    public float damage = 10f;
    public boolean alive = true;

    public Bullet(float x, float y, float vx, float vy, float radius) {
        this.pos.set(x, y);
        this.vel.set(vx, vy);
        this.r = radius;
    }

    public void update(float dt) {
        pos.x += vel.x * dt;
        pos.y += vel.y * dt;
        // simple lifetime bounds check can stay external; alive flag controlled by collisions
    }

    public void kill() { alive = false; }

    public void render(ShapeRenderer shapes) {
        if (!alive) return;

        shapes.setColor(0f, 0f, 0f, 1f);

        // Compute angle from velocity
        float angleDeg = (float)Math.toDegrees(Math.atan2(vel.y, vel.x));

        // Save current transform
        shapes.identity();
        shapes.translate(pos.x, pos.y, 0);
        shapes.rotate(0, 0, 1, angleDeg);

        // Draw oval centered at (0,0)
        float width = r * 2f;   // longer along velocity
        float height = r * 1f;  // shorter across velocity
        shapes.ellipse(-width/2f, -height/2f, width, height);

        // Restore default transform
        shapes.identity();
    }
}
