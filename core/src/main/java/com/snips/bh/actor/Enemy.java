package com.snips.bh.actor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Enemy implements Targetable {
    public final Vector2 pos = new Vector2();
    public float r = 14f;
    public float speed = 140f;
    public float maxHP = 300f;
    public float hp    = maxHP;
    private boolean alive = true;
    private float angleDeg = 0f;
    private final Texture sprite;


    public Enemy(float x, float y, Texture tex) {
        pos.set(x, y);
        this.sprite = tex;
    }

    public void update(float dt, Vector2 targetPos) {
        // simple chase
        float dx = targetPos.x - pos.x;
        float dy = targetPos.y - pos.y;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len > 1e-4f) {
            pos.x += (dx / len) * speed * dt;
            pos.y += (dy / len) * speed * dt;
        }
    }
    public void faceToward(Vector2 target) {
        float dx = target.x - pos.x;
        float dy = target.y - pos.y;
        angleDeg = (float)Math.toDegrees(Math.atan2(dy, dx)) + 90f;
    }

    public void damage(float d) {
        hp = MathUtils.clamp(hp - d, 0f, 1000f);
        if (hp <= 0f) alive = false;
    }

    public boolean isAlive() { return alive; }

    @Override
    public Vector2 getPos() { return pos; }
    public boolean hasTakenDamage()  { return hp < maxHP; }
    public float healthPct()         { return hp / maxHP; }

    public void render(SpriteBatch batch) {
        float size = r * 2f * 1.2f;
        float originX = size * 0.5f;
        float originY = size * 0.55f;
        batch.draw(
            sprite,
            pos.x - originX, pos.y - originY,
            originX, originY,
            size, size,
            1f, 1f,
            angleDeg, // enemies don’t rotate yet
            0, 0,
            sprite.getWidth(), sprite.getHeight(),
            false, false
        );
    }

    public void renderHpBar(ShapeRenderer shapes) {
        if (!hasTakenDamage()) return;
        float pct = healthPct();
        float barW = 30f;
        float barH = 6f;
        float barX = pos.x - barW / 2f;
        float barY = pos.y + r + 8f;

        shapes.setColor(0f, 0f, 0f, 0.35f);
        shapes.rect(barX, barY, barW, barH);

        shapes.setColor(0f, 1f, 0f, 0.7f);
        shapes.rect(barX, barY, barW * pct, barH);
    }
}
