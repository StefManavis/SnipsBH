package com.snips.bh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Root Game class. Holds shared SpriteBatch/ShapeRenderer and swaps screens.
 * Starts on MenuScreen.
 */
public class SnipsBHGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapes;

    @Override
    public void create() {
        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}
