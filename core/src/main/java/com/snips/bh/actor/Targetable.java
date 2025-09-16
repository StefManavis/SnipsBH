package com.snips.bh.actor;

import com.badlogic.gdx.math.Vector2;

public interface Targetable {
    Vector2 getPos();   // return a *reference* to the position vector
    boolean isAlive();
}
