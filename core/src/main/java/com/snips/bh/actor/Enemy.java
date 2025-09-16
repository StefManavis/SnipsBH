package com.snips.bh.actor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Enemy implements Targetable{
    public final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    public float r = 10f;
    public float speed = 140f;
    public float turnSpeed = 6f;
    public boolean alive = true;

    public Enemy(float x, float y){
        pos.set(x, y);
    }

    //Smooth homing, velocity steers toward player without snapping
    public void update(float dt, Vector2 playerPos){
        //desired direction toward player
        Vector2 desired = new Vector2(playerPos).sub(pos);
        if(desired.isZero()) return;

        desired.nor().scl(speed); //desired velocity
        vel.lerp(desired, turnSpeed * dt);//steer toward desired(smooth)
        pos.mulAdd(vel, dt);
    }

    public void render(ShapeRenderer sr){

        sr.circle(pos.x, pos.y, r);
    }
    @Override public Vector2 getPos(){
        return pos;
    }
    @Override public boolean isAlive(){
        return alive;
    }
}
