package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface Lanzable {

    void update(float delta);

    void dibujar();

    void dibujarHitbox(ShapeRenderer shapeRenderer);

    boolean estaActiva();

    void setId(int id);

    int getId();

    float getX();

    float getY();
}
