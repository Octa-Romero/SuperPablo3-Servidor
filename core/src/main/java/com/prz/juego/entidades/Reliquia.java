
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

    private final Sprite sprite;
    private final Rectangle bounds;
    private final Colisiones colisiones;

    private float velocidadY = VELOCIDAD_INICIAL_Y;
    private float tiempoDesdeAparicion = 0f;
    private float tiempoAnimacion = 0f;
    private float ySuelo;

    private boolean enSuelo = false;
    private boolean recogida = false;

    public Reliquia(float x, float y, Colisiones colisiones) {
        Texture textura = GestorRecursos.obtenerTextura(
            "Reliquia/reliquia.png"
        );

        this.colisiones = colisiones;

        sprite = new Sprite(textura);
        sprite.setSize(TAMANO, TAMANO);
        sprite.setOriginCenter();
        sprite.setPosition(x, y);

        bounds = new Rectangle(x, y, TAMANO, TAMANO);
    }

    public void dibujar() {
        if (!recogida) {
            sprite.draw(Render.batch);
        }
    }

    public boolean actualizar(Jugador jugador, float delta) {
        if (recogida) {
            return true;
        }

        tiempoDesdeAparicion += delta;
        tiempoAnimacion += delta;

        if (!enSuelo) {
            actualizarCaida(delta);
        } else {
            actualizarAnimacion();
        }

        if (tiempoDesdeAparicion >= TIEMPO_PROTEGIDO
            && bounds.overlaps(jugador.getBounds())) {
            recogida = true;
            System.out.println("[RELIQUIA] ¡El jugador la recogió!");
            return true;
        }

        return false;
    }

    private void actualizarCaida(float delta) {
        float yAnterior = bounds.y;

        velocidadY -= GRAVEDAD * delta;
        float nuevoY = bounds.y + velocidadY * delta;

        if (velocidadY <= 0f) {
            float yAnteriorInferior = yAnterior;
            float yNuevoInferior = nuevoY;

            for (Rectangle suelo : colisiones.getColisiones()) {
                boolean haySolapamientoHorizontal =
                    bounds.x + bounds.width > suelo.x
                        && bounds.x < suelo.x + suelo.width;

                float alturaSuelo = suelo.y + suelo.height;

                boolean cruzaSuelo =
                    yAnteriorInferior >= alturaSuelo
                        && yNuevoInferior <= alturaSuelo;

                if (haySolapamientoHorizontal && cruzaSuelo) {
                    nuevoY = alturaSuelo;
                    enSuelo = true;
                    velocidadY = 0f;
                    ySuelo = nuevoY;
                    break;
                }
            }
        }

        bounds.y = nuevoY;
        sprite.setPosition(bounds.x, bounds.y);
    }

    private void actualizarAnimacion() {
        float flotacion = (float) Math.sin(
            tiempoAnimacion * VELOCIDAD_FLOTACION
        ) * AMPLITUD_FLOTACION;

        sprite.setPosition(bounds.x, ySuelo + flotacion);
        sprite.rotate(VELOCIDAD_ROTACION * com.badlogic.gdx.Gdx.graphics.getDeltaTime());
    }

    public boolean estaRecogida() {
        return recogida;
    }

    public void dispose() {
        // La textura pertenece al GestorRecursos.
    }
}
