package com.snips.bh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Simple pause menu with RESUME and QUIT buttons. */
public class PauseScreen extends ScreenAdapter {
    // Match your world size (from PlayScreen)
    public static final float WORLD_W = 450f;
    public static final float WORLD_H = 1000f;

    private final SnipsBHGame game;
    private final PlayScreen play; // we resume back to this instance

    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Vector2 mouseWorld = new Vector2();

    private static class Btn {
        float x, y, w, h; String label;
        boolean hit(float px, float py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }
    private final Btn resumeBtn = new Btn();
    private final Btn quitBtn   = new Btn();

    public PauseScreen(SnipsBHGame game, PlayScreen play) {
        this.game = game;
        this.play = play;
    }

    @Override public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Enable blending for the dim overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        font = new BitmapFont(); // default font is fine
        layout();
    }

    private void layout() {
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();

        float btnW = Math.min(260f, W * 0.8f);
        float btnH = 64f;
        float gap  = 18f;
        float cx   = (W - btnW) * 0.5f;
        float topY = H * 0.55f;

        resumeBtn.x = cx; resumeBtn.y = topY;             resumeBtn.w = btnW; resumeBtn.h = btnH; resumeBtn.label = "RESUME";
        quitBtn.x   = cx; quitBtn.y   = topY - btnH - gap; quitBtn.w   = btnW; quitBtn.h   = btnH; quitBtn.label   = "QUIT";
    }

    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.shapes.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        // Dim overlay
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(0f, 0f, 0f, 0.45f);
        game.shapes.rect(0, 0, WORLD_W, WORLD_H);

        // Buttons
        game.shapes.setColor(0.15f, 0.6f, 0.9f, 1f); // Resume
        game.shapes.rect(resumeBtn.x, resumeBtn.y, resumeBtn.w, resumeBtn.h);
        game.shapes.setColor(0.8f, 0.15f, 0.15f, 1f); // Quit
        game.shapes.rect(quitBtn.x, quitBtn.y, quitBtn.w, quitBtn.h);
        game.shapes.end();

        // Title + labels
        game.batch.begin();
        String title = "PAUSED";
        glyph.setText(font, title);
        float titleX = (viewport.getWorldWidth() - glyph.width) * 0.5f;
        float titleY = viewport.getWorldHeight() - 30f;
        font.draw(game.batch, glyph, titleX, titleY);

        drawBtn(resumeBtn);
        drawBtn(quitBtn);
        game.batch.end();

        // Input (mouse/touch)
        if (Gdx.input.justTouched()) {
            mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(mouseWorld);
            if (resumeBtn.hit(mouseWorld.x, mouseWorld.y)) {
                game.setScreen(play); // back to gameplay
                return;
            } else if (quitBtn.hit(mouseWorld.x, mouseWorld.y)) {
                // free play screen resources and go to main menu
                play.dispose();
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        // Keyboard: ESC resumes
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(play);
        }
    }

    private void drawBtn(Btn b) {
        glyph.setText(font, b.label);
        float tx = b.x + (b.w - glyph.width) * 0.5f;
        float ty = b.y + (b.h + glyph.height) * 0.5f;
        font.draw(game.batch, glyph, tx, ty);
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        layout();
    }

    @Override public void dispose() {
        if (font != null) font.dispose();
    }
}
