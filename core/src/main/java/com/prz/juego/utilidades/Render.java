package com.prz.juego.utilidades;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Render {

    public static SpriteBatch batch;

    public static void limpiarPantalla()
    {
        ScreenUtils.clear(0, 0, 0, 1);
    }

    public static void begin(Camera camara) {
        batch.setProjectionMatrix(camara.combined);
        batch.begin();
    }

    public static void end() {
        batch.end();
    }
}
