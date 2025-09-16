package com.snips.bh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.snips.bh.actor.Player;


public class GameMain extends ApplicationAdapter {
    // Virtual world size
    public static final float WORLD_W = 800f;
    public static final float WORLD_H = 450f;

    private OrthographicCamera camera;
    private Viewport viewport;
   private ShapeRenderer shapes;
   private Player player;

   @Override
    public void create(){
       //initialize camera/viewport
       camera = new OrthographicCamera();
       viewport = new FitViewport(WORLD_W, WORLD_H, camera);
       viewport.apply();
       camera.position.set(WORLD_W * 0.5F, WORLD_H * 0.5F, 0F);

       shapes = new ShapeRenderer();

       // start in the center of the current window
       player = new Player(WORLD_W / 2f, 60f);
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
       shapes.circle(player.x, player.y, player.r); // player
       shapes.end();

       // --- Exit on Shift + Enter ---
       if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
           Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
           Gdx.app.exit(); // triggers dispose() safely
       }

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
