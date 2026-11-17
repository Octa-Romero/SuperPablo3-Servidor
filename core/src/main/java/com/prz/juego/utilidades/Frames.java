package com.prz.juego.utilidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Frames {

    private Frames() {
    }

    public static TextureRegion primero(Texture textura, int cantidadFrames) {
        return new TextureRegion(
            textura, 0, 0, textura.getWidth() / cantidadFrames, textura.getHeight());
    }
}
