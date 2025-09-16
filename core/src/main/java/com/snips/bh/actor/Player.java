package com.snips.bh.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;



public class Player {
    public final Vector2 pos = new Vector2(100, 100);//Vector for position of Player
    public float x, y;//Center position in pixels   // PROBABLY REMOVE LATER
    public float r = 14f;//radius in pixels
    public float speed = 420f;//pixels/s
    //public float margin = 16f;//clamp margin from screen edges


    public Player(float x, float y){//Constructor for Player
        this.x = x;
        this.y = y;
    }

    public void update(float dt, float worldW, float worldH){
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



        //clamp inside window

        x = MathUtils.clamp(x, r, worldW - r);
        y = MathUtils.clamp(y, r, worldH -r);
    }
}
