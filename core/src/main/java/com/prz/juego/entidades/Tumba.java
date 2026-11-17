package com.prz.juego.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.sistemas.Colisiones;

public class Tumba {

    private static final float GRAVEDAD = 400f;
    private static final float ANCHO = 48f;
    private static final float ALTO = 64f;
    private static final float Y_MINIMA = -10000f;

    private final Rectangle BOUNDS;
    private final Colisiones COLISIONES;

    private float velocidadY = 0f;
    private boolean enSuelo = false;

    public Tumba(float centroX, float y, Colisiones colisiones) {
        COLISIONES = colisiones;
        BOUNDS = new Rectangle(centroX - ANCHO / 2f, y, ANCHO, ALTO);
    }

    public void actualizar(float delta) {
        if (enSuelo) {
            return;
        }

        float yAnterior = BOUNDS.y;
        velocidadY -= GRAVEDAD * delta;
        float nuevoY = BOUNDS.y + velocidadY * delta;

        if (velocidadY <= 0f) {
            for (Rectangle suelo : COLISIONES.getColisiones()) {
                boolean haySolapamientoHorizontal =
                    BOUNDS.x + BOUNDS.width > suelo.x && BOUNDS.x < suelo.x + suelo.width;
                float alturaSuelo = suelo.y + suelo.height;
                boolean cruzaSuelo = yAnterior >= alturaSuelo && nuevoY <= alturaSuelo;

                if (haySolapamientoHorizontal && cruzaSuelo) {
                    nuevoY = alturaSuelo;
                    enSuelo = true;
                    break;
                }
            }
        }

        BOUNDS.y = nuevoY;

        if (BOUNDS.y < Y_MINIMA) {
            enSuelo = true;
        }
    }

    public boolean estaEnSuelo() {
        return enSuelo;
    }

    public float getCentroX() {
        return BOUNDS.x + ANCHO / 2f;
    }

    public float getY() {
        return BOUNDS.y;
    }
}
