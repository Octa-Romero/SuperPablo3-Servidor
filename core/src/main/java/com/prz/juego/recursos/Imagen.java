package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Imagen {

    private Texture t;
    private Sprite s;

    public Imagen(String ruta) {
        t = GestorRecursos.obtenerTextura(ruta);
        s = new Sprite(t);
    }

    public void setPosition(float x, float y)
    {
        s.setPosition(x, y);
    }

    public void dibujar(SpriteBatch batch)
    {
        s.draw(batch);
    }

    public void setSize(float ancho, float alto)
    {
        s.setSize(ancho, alto);
    }

    public void dispose() {
        s = null;
        t = null;
    }

    public void setX(float x) {
        s.setX(x);
    }

    public void setY(float y)
    {
        s.setY(y);
    }
}
