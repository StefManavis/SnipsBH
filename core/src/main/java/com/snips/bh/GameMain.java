package com.snips.bh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.snips.bh.actor.*;


public class GameMain extends ApplicationAdapter {
    // Virtual world size
    public static final float WORLD_W = 800f;
    public static final float WORLD_H = 450f;

    private OrthographicCamera camera;
    private Viewport viewport;
   private ShapeRenderer shapes;
   private Player player;

   //ENEMIES
   private final Array<Enemy> enemies = new Array<>();
   private final EnemySpawner spawner = new EnemySpawner();



   @Override
    public void create(){
       //initialize camera/viewport
       camera = new OrthographicCamera();
       viewport = new FitViewport(WORLD_W, WORLD_H, camera);
       viewport.apply();
       camera.position.set(WORLD_W * 0.5F, WORLD_H * 0.5F, 0F);

       shapes = new ShapeRenderer();

       // start in the center of the current window
       player = new Player(WORLD_W / 2f, WORLD_H / 2f);
   }

   @Override
    public void render(){
       float dt = Gdx.graphics.getDeltaTime();

       //update
       player.update(dt, WORLD_W, WORLD_H);

       //draw
       Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
       Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

       camera.update();
       shapes.setProjectionMatrix(camera.combined);

       shapes.begin(ShapeType.Filled);
       shapes.circle(player.pos.x, player.pos.y, player.r); // player
       shapes.end();

       // --- Exit on Shift + Enter ---
       if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
           Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
           Gdx.app.exit(); // triggers dispose() safely
       }



       spawner.updateAndMaybeSpawn(dt, enemies, WORLD_W, WORLD_H);

       for (int i = enemies.size - 1; i >= 0; i--) {
           Enemy e = enemies.get(i);
           e.update(dt, player.pos);

           // optional cleanup: if it flies way off (after overshooting)
           float margin = 200f;
           if (e.pos.x < -margin || e.pos.x > WORLD_W + margin || e.pos.y < -margin || e.pos.y > WORLD_H + margin) {
               enemies.removeIndex(i);
           }
       }

       // draw (with ShapeRenderer in Filled mode)
       shapes.begin(ShapeType.Filled);
       for (Enemy e : enemies) e.render(shapes);
       shapes.end();
   }

   @Override
   public void resize(int width, int height){
       viewport.update(width, height, true);
   }
   @Override
    public void dispose(){
       shapes.dispose();
   }
}
