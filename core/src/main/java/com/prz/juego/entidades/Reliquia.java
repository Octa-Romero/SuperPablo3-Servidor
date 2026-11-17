package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

public class Reliquia {

    private static final float TAMANO = 48f;
    private static final float GRAVEDAD = 900f;
    private static final float VELOCIDAD_INICIAL_Y = 180f;
    private static final float AMPLITUD_FLOTACION = 4f;
    private static final float VELOCIDAD_FLOTACION = 3f;
    private static final float VELOCIDAD_ROTACION = 90f;
    private static final float TIEMPO_PROTEGIDO = 0.75f;
    private static final String TEXTURA = "Reliquia/reliquia.png";

    private final Sprite SPRITE;
    private final Rectangle BOUNDS;
    private final Colisiones COLISIONES;

    private float velocidadY = VELOCIDAD_INICIAL_Y;
    private float tiempoDesdeAparicion = 0f;
    private float tiempoAnimacion = 0f;
    private float ySuelo = 0f;
    private boolean enSuelo = false;
    private boolean recogida = false;

    public Reliquia(float x, float y, Colisiones colisiones) {
        Texture textura = GestorRecursos.obtenerTextura(TEXTURA);

        COLISIONES = colisiones;

        SPRITE = new Sprite(textura);
        SPRITE.setSize(TAMANO, TAMANO);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x, y);

        BOUNDS = new Rectangle(x, y, TAMANO, TAMANO);
    }

    public void dibujar() {
        if (!recogida) {
            SPRITE.draw(Render.batch);
        }
    }

    public boolean actualizar(Jugador jugador, float delta) {
        if (recogida) {
            return true;
        }

        tiempoDesdeAparicion += delta;
        tiempoAnimacion += delta;

        if (enSuelo) {
            actualizarAnimacion(delta);
        } else {
            actualizarCaida(delta);
        }

        if (tiempoDesdeAparicion >= TIEMPO_PROTEGIDO && BOUNDS.overlaps(jugador.getBounds())) {
            recogida = true;
        }

        return recogida;
    }

    private void actualizarCaida(float delta) {
        float yAnterior = BOUNDS.y;

        velocidadY -= GRAVEDAD * delta;
        float nuevoY = yAnterior + velocidadY * delta;

        if (velocidadY <= 0f) {
            for (Rectangle suelo : COLISIONES.getColisiones()) {
                boolean solapaHorizontal =
                    BOUNDS.x + BOUNDS.width > suelo.x && BOUNDS.x < suelo.x + suelo.width;
                float alturaSuelo = suelo.y + suelo.height;
                boolean cruzaSuelo = yAnterior >= alturaSuelo && nuevoY <= alturaSuelo;

                if (solapaHorizontal && cruzaSuelo) {
                    nuevoY = alturaSuelo;
                    enSuelo = true;
                    velocidadY = 0f;
                    ySuelo = nuevoY;
                    break;
                }
            }
        }

        BOUNDS.y = nuevoY;
        SPRITE.setPosition(BOUNDS.x, BOUNDS.y);
    }

    private void actualizarAnimacion(float delta) {
        float flotacion = (float) Math.sin(tiempoAnimacion * VELOCIDAD_FLOTACION) * AMPLITUD_FLOTACION;

        SPRITE.setPosition(BOUNDS.x, ySuelo + flotacion);
        SPRITE.rotate(VELOCIDAD_ROTACION * delta);
    }

    public boolean estaRecogida() {
        return recogida;
    }

    public void dispose() {
    }
}
