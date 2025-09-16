package com.snips.bh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.snips.bh.actor.Player;
import com.badlogic.gdx.Input;


public class GameMain extends ApplicationAdapter {
   private ShapeRenderer shapes;
   private Player player;

   @Override
    public void create(){
       shapes = new ShapeRenderer();
       // start in the center of the current window
       player = new Player(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 2f);
   }

   @Override
    public void render(){
       float dt = Gdx.graphics.getDeltaTime();

       //update
       player.update(dt);

       //draw
       Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
       Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

       shapes.begin(ShapeType.Filled);
       shapes.circle(player.x, player.y, player.r); // player
       shapes.end();

       // --- Exit on Shift + Esc ---
       if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
           Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
           Gdx.app.exit(); // triggers dispose() safely
       }

   }
   @Override
    public void dispose(){
       shapes.dispose();
   }
}
