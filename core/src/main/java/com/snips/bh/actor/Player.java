package com.snips.bh.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public final Vector2 pos = new Vector2(100, 100);//Vector for position of Player
    public float x, y;//Center position in pixels   // Probs REMOVE LATER
    public float r = 28f;//radius in pixels
    public float speed = 420f;//pixels/s
    public float margin = 16f;//clamp margin from screen edges


    public Player(float x, float y){//Constructor for Player
        this.x = x;
        this.y = y;
    }

    public void update(float dt){
        float dx = 0, dy = 0;

        // Hold-to-move: poll keys every frame
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;


        //normalize diagonal
        if(dx != 0 || dy != 0){
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            pos.x += (dx / len) * speed * dt;
            pos.y += (dy / len) * speed * dt;
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
