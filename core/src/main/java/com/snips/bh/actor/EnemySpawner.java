package com.snips.bh.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class EnemySpawner {
    private float timer = 0f;
    public float spawnInterval = 0.9f; // seconds between spawns
    public float offscreenMargin = 24f;
    public int maxEnemies = 30;


    //random points just outside one of the four edges
    private Vector2 randomOffscreen(float worldW, float worldH){
        int edge = MathUtils.random(3); //0:L, 1:R,2:D,3:UP
        float x,y;

        //noinspection EnhancedSwitchMigration
        switch(edge){
            case 0: //left
                x = -offscreenMargin;
                y = MathUtils.random(0f, worldH);
                break;
            case 1: // right
                x = worldW + offscreenMargin;
                y = MathUtils.random(0f, worldH);
                break;
            case 2: // bottom
                x = MathUtils.random(0f, worldW);
                y = -offscreenMargin;
                break;
            default: // top
                x = MathUtils.random(0f, worldW);
                y = worldH + offscreenMargin;
        }
        return new Vector2(x, y);
    }

    //Call every frame, will add enemies at fixed intervals
    public void updateAndMaybeSpawn(float dt, Array<Enemy> enemies, float worldW, float worldH){
        timer += dt;

        if(enemies.size >= maxEnemies) return;// Check if at max enemy limit

        while(timer >= spawnInterval){
            timer -= spawnInterval;
            Vector2 p = randomOffscreen(worldW, worldH);
            enemies.add(new Enemy(p.x, p.y));
        }
    }
}
