package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;

public class Imagen extends Image {

    private static final float GROSOR_CONTORNO = 2f;
    private static final float[][] DESPLAZAMIENTOS = {
        {-1f, -1f}, {-1f, 0f}, {-1f, 1f},
        {0f, -1f}, {0f, 1f},
        {1f, -1f}, {1f, 0f}, {1f, 1f}
    };

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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Drawable drawable = getDrawable();
        if (contorno && drawable != null) {
            validate();
            float colorAnterior = batch.getPackedColor();
            batch.setColor(0f, 0f, 0f, getColor().a * parentAlpha);
            for (float[] desplazamiento : DESPLAZAMIENTOS) {
                dibujarCapa(batch, drawable,
                    desplazamiento[0] * GROSOR_CONTORNO,
                    desplazamiento[1] * GROSOR_CONTORNO);
            }
            batch.setPackedColor(colorAnterior);
        }

        super.draw(batch, parentAlpha);
    }

    private void dibujarCapa(Batch batch, Drawable drawable, float desplazamientoX, float desplazamientoY) {
        float x = getX() + desplazamientoX + getImageX();
        float y = getY() + desplazamientoY + getImageY();
        float escalaX = getScaleX();
        float escalaY = getScaleY();
        float rotacion = getRotation();

        if (drawable instanceof TransformDrawable && (escalaX != 1f || escalaY != 1f || rotacion != 0f)) {
            ((TransformDrawable) drawable).draw(batch, x, y,
                getOriginX() - getImageX(), getOriginY() - getImageY(),
                getImageWidth(), getImageHeight(),
                escalaX, escalaY, rotacion);
        } else {
            drawable.draw(batch, x, y, getImageWidth() * escalaX, getImageHeight() * escalaY);
        }
    }

    public void dibujar(Batch batch) {
        draw(batch, 1f);
    }

    public void dispose() {
        setDrawable(null);
    }
}
