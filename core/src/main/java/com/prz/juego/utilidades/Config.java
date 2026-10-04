package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;

public class Config {

    public static final int ANCHO_BASE = 1280;
    public static final int ALTO_BASE = 720;

    public static boolean fullscreen = true;

    public static void setFullscreen() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        fullscreen = true;
    }

    public static void setWindowed(int w, int h) {
        Gdx.graphics.setWindowedMode(w, h);
        fullscreen = false;
    }

    public static void toggleFullscreen() {
        if (fullscreen) {
            setWindowed(ANCHO_BASE, ALTO_BASE);
        } else {
            setFullscreen();
        }
    }

    public static int getAncho() {
        return Gdx.graphics.getWidth();
    }

    public static int getAlto() {
        return Gdx.graphics.getHeight();
    }
}
