package com.snips.bh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Minimal main menu without Scene2D:
 * - Title "MAIN MENU" (top-center)
 * - PLAY button -> switches to PlayScreen
 * - QUIT button -> exits app
 */
public class MenuScreen extends ScreenAdapter {
    // Match your game's world size from GameMain
    public static final float WORLD_W = 450f;
    public static final float WORLD_H = 1000f;

    private final SnipsBHGame game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Vector2 mouseWorld = new Vector2();

    private static class Btn {
        float x, y, w, h; String label;
        boolean hit(float px, float py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }
    private final Btn playBtn = new Btn();
    private final Btn quitBtn = new Btn();

    public MenuScreen(SnipsBHGame game) { this.game = game; }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);
        font = new BitmapFont(); // default font

        layoutButtons();
    }

    private void layoutButtons() {
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();
        float btnW = Math.min(260f, W * 0.8f);
        float btnH = 64f;
        float gap  = 18f;
        float cx   = (W - btnW) * 0.5f;
        float topY = H * 0.6f;

        playBtn.x = cx; playBtn.y = topY;             playBtn.w = btnW; playBtn.h = btnH; playBtn.label = "PLAY";
        quitBtn.x = cx; quitBtn.y = topY - btnH - gap; quitBtn.w = btnW; quitBtn.h = btnH; quitBtn.label = "QUIT";
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.shapes.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        // Buttons
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(0.15f, 0.7f, 0.2f, 1f); // Play
        game.shapes.rect(playBtn.x, playBtn.y, playBtn.w, playBtn.h);
        game.shapes.setColor(0.7f, 0.15f, 0.15f, 1f); // Quit
        game.shapes.rect(quitBtn.x, quitBtn.y, quitBtn.w, quitBtn.h);
        game.shapes.end();

        // Title + labels
        game.batch.begin();
        String title = "MAIN MENU";
        glyph.setText(font, title);
        float titleX = (viewport.getWorldWidth() - glyph.width) * 0.5f;
        float titleY = viewport.getWorldHeight() - 30f;
        font.draw(game.batch, glyph, titleX, titleY);

        drawBtn(playBtn);
        drawBtn(quitBtn);
        game.batch.end();

        // Input
        if (Gdx.input.justTouched()) {
            mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(mouseWorld);
            if (playBtn.hit(mouseWorld.x, mouseWorld.y)) {
                game.setScreen(new PlayScreen(game)); // go to gameplay
            } else if (quitBtn.hit(mouseWorld.x, mouseWorld.y)) {
                Gdx.app.exit();
            }
        }
    }

    private void drawBtn(Btn b) {
        glyph.setText(font, b.label);
        float tx = b.x + (b.w - glyph.width) * 0.5f;
        float ty = b.y + (b.h + glyph.height) * 0.5f;
        font.draw(game.batch, glyph, tx, ty);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        layoutButtons();
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }
}
