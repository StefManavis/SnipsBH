package com.snips.bh.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Player implements Targetable {
    public final Vector2 pos = new Vector2();
    public float r = 14f;
    public float speed = 420f;

    public float maxHP = 100f;
    public float hp    = maxHP;
    public float touchDamageCooldown = 0f;

    public static float XP = 0f;
    public static final int   MAX_LEVEL = 10;
    public static final float BASE_XP   = 1000f;
    public static final float GROWTH    = 1.2f;;
    public int currLevel = 1;


    private Texture sprite;
    private float angleDeg = 0f;

    public Player(float x, float y, Texture spriteTex) {
        this.pos.set(x, y);
        this.sprite = spriteTex;
    }

    public void update(float dt, float worldW, float worldH) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;
        if (dx != 0 || dy != 0) {
            float inv = (float)(1.0/Math.sqrt(dx*dx+dy*dy));
            pos.x += dx * inv * speed * dt;
            pos.y += dy * inv * speed * dt;
            pos.x = MathUtils.clamp(pos.x, r, worldW - r);
            pos.y = MathUtils.clamp(pos.y, r, worldH - r);
        }
        if (touchDamageCooldown > 0f) {
            touchDamageCooldown -= dt;
            if (touchDamageCooldown < 0f) touchDamageCooldown = 0f;
        }
    }

    public void faceToward(Vector2 target) {
        float dx = target.x - pos.x;
        float dy = target.y - pos.y;
        angleDeg = (float)Math.toDegrees(Math.atan2(dy, dx)) - 90f;
    }

    public void render(SpriteBatch batch) {
        float scale = 1.7f;
        float size = r * 2f * scale;
        float originX = size * 0.5f;
        float originY = size * 0.55f; // waist pivot

        batch.draw(
            sprite,
            pos.x - originX,
            pos.y - originY,
            originX, originY,
            size, size,
            1f, 1f,
            angleDeg,
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

    // Draw a purple XP bar (HUD, top-left in world coords)
    public void renderXpBar(ShapeRenderer shapes, float worldW, float worldH) {
        float pad = 12f;
        float w   = 220f;
        float h   = 10f;
        float x   = pad;
        float y   = worldH - pad - h;

        // background
        shapes.setColor(0f, 0f, 0f, 0.5f);
        shapes.rect(x, y, w, h);

        // fill (purple)
        float pct = xpProgress();
        shapes.setColor(0.55f, 0.25f, 0.85f, 1f);
        shapes.rect(x, y, w * pct, h);
    }

    public void damage(float amount) { hp = MathUtils.clamp(hp - amount, 0f, maxHP); }
    public void heal(float amount)   { hp = MathUtils.clamp(hp + amount, 0f, maxHP); }
    public boolean hasTakenDamage()  { return hp < maxHP; }
    public float healthPct()         { return hp / maxHP; }

    public static float xpToNextLevel(int level) {
        if (level >= MAX_LEVEL) return Float.POSITIVE_INFINITY; // already at max
        if (level < 1) level = 1;
        return BASE_XP * (float)Math.pow(GROWTH, level - 1);
    }
    // 0..1 progress for the current level using global XP and currLevel
    public float xpProgress() {
        float need = xpToNextLevel(currLevel);
        if (!Float.isFinite(need) || need <= 0f) return 1f;
        return MathUtils.clamp(XP / need, 0f, 1f);
    }



    @Override public Vector2 getPos() { return pos; }
    @Override public boolean isAlive() { return hp > 0f; }
}
