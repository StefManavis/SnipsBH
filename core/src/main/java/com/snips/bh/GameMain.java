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
       //Enable blending to make objects opaque
       Gdx.gl.glEnable(GL20.GL_BLEND);
       Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
       //initialize camera/viewport
       camera = new OrthographicCamera();
       viewport = new FitViewport(WORLD_W, WORLD_H, camera);
       viewport.apply();
       camera.position.set(WORLD_W * 0.5F, WORLD_H * 0.5F, 0F);

       shapes = new ShapeRenderer();

       // start in the center of the current window
       player = new Player(WORLD_W / 2f, WORLD_H / 2f);
   }

   @SuppressWarnings("DuplicatedCode")
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

       // --- COLLISIONS: Enemy vs Player ---
       for (int i = 0; i < enemies.size; i++) {
           Enemy e = enemies.get(i);

           float dx = e.pos.x - player.pos.x;
           float dy = e.pos.y - player.pos.y;
           float minDist = e.r + player.r;
           float dist2 = dx*dx + dy*dy;

           if (dist2 > 0f && dist2 < minDist*minDist) {
               float dist = (float)Math.sqrt(dist2);
               float overlap = minDist - dist;

               // normal
               float nx = dx / dist;
               float ny = dy / dist;

               // Push the ENEMY out so it cannot enter the player
               e.pos.x += nx * overlap;
               e.pos.y += ny * overlap;

               // Optional: touch damage with cooldown
               if (player.touchDamageCooldown <= 0f) {
                   player.damage(5f);                 // tune this value
                   player.touchDamageCooldown = 0.4f; // 400 ms i-frames
               }
           }
       }

       // --- COLLISIONS: Enemy vs Enemy ---
       for (int i = 0; i < enemies.size; i++) {
           Enemy a = enemies.get(i);
           for (int j = i + 1; j < enemies.size; j++) {
               Enemy b = enemies.get(j);

               float dx = b.pos.x - a.pos.x;
               float dy = b.pos.y - a.pos.y;
               float minDist = a.r + b.r;
               float dist2 = dx*dx + dy*dy;

               if (dist2 > 0f && dist2 < minDist*minDist) {
                   float dist = (float)Math.sqrt(dist2);
                   float overlap = (minDist - dist);

                   // normal
                   float nx = dx / dist;
                   float ny = dy / dist;

                   // equal mass: split the correction 50/50
                   float half = overlap * 0.5f;
                   a.pos.x -= nx * half;
                   a.pos.y -= ny * half;
                   b.pos.x += nx * half;
                   b.pos.y += ny * half;
               }
           }
       }

       /* RENDER OBJECTS IN HERE */
       // draw player
       shapes.begin(ShapeType.Filled);
       // Player (white)
       shapes.setColor(1f, 1f, 1f, 1f);
       shapes.circle(player.pos.x, player.pos.y, player.r);

       // --- HP BAR ---
      if(player.hasTakenDamage()) {
          float pct = player.healthPct();
          float barW = 30f;
          float barH = 6f;
          float barX = player.pos.x - barW / 2f;
          float barY = player.pos.y + player.r + 8f; // a little above the head

          // background (semi-transparent dark)
          shapes.setColor(0f, 0f, 0f, 0.35f);
          shapes.rect(barX, barY, barW, barH);

          // fill (semi-transparent green)
          shapes.setColor(0f, 1f, 0f, 0.7f);
          shapes.rect(barX, barY, barW * pct, barH);
      }

       // draw enemies (still in the same begin/end)
       shapes.setColor(1f, 0f, 0f, 1f); // RED
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
