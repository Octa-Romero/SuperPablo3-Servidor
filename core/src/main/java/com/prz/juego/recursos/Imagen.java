package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Imagen extends Image {

    private static final float GROSOR_CONTORNO = 2f;
    private boolean contorno;

    public Imagen(String ruta) {
        this(GestorRecursos.obtenerTextura(ruta), false);
    }

    public Imagen(Texture textura) {
        this(textura, false);
    }

    public Imagen(Texture textura, boolean contorno) {
        super(textura);
        this.contorno = contorno;
    }

    public Imagen(Drawable drawable, boolean contorno) {
        super(drawable);
        this.contorno = contorno;
    }

    public void setContorno(boolean contorno) {
        this.contorno = contorno;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Drawable drawable = getDrawable();
        if (contorno && drawable != null) {
            Color colorAnterior = batch.getColor().cpy();
            batch.setColor(0f, 0f, 0f, getColor().a * parentAlpha);
            drawable.draw(
                    batch,
                    getX() - GROSOR_CONTORNO,
                    getY() - GROSOR_CONTORNO,
                    getWidth() + GROSOR_CONTORNO * 2,
                    getHeight() + GROSOR_CONTORNO * 2);
            batch.setColor(colorAnterior);
        }

        super.draw(batch, parentAlpha);
    }

    public void dibujar(SpriteBatch batch) {
        draw(batch, 1f);
    }

    public void dispose() {
        setDrawable(null);
    }
}
