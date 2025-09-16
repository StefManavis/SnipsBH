package com.snips.bh.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

public class Player {
    public float x, y;//Center position in pixels
    public float r = 28f;//radius in pixels
    public float speed = 420f;//pixels/s
    public float margin = 16f;//clamp margin from screen edges


    public Player(float x, float y){//Constructor for Player
        this.x = x;
        this.y = y;
    }

    public void update(float dt){
        float dx = 0, dy = 0;
        if(Gdx.input.isKeyJustPressed(Input.Keys.W)) dy += 1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) dy -= 1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.A)) dx -= 1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.D)) dx += 1;

        //normalize diagonal
        if(dx != 0 || dy != 0){
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            dx /= len;
            dy /= len;
        }

        x += dx * speed * dt;
        y += dy * speed * dt;

        //clamp inside window
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float minX = margin + r, maxX = w - margin - r;
        float minY = margin +r, maxY = h - margin - r;
        x = MathUtils.clamp(x, minX, maxX);
        y = MathUtils.clamp(y, minY, maxY);
    }
}
