package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

public class Tumba {

    private static final float GRAVEDAD = 400f;
    private static final float ANCHO = 48f;
    private static final float ALTO = 64f;

    private final Sprite sprite;
    private final Rectangle bounds;
    private final Colisiones colisiones;

    private float velocidadY = 0f;
    private boolean enSuelo = false;
    private boolean eliminada = false;

    public Tumba(float x, float y, Colisiones colisiones) {
        Texture textura = GestorRecursos.obtenerTextura(
            "Personajes/tumba.png"
        );

        this.colisiones = colisiones;

        sprite = new Sprite(textura);
        sprite.setSize(ANCHO, ALTO);
        // Centrar la tumba en la posición del jugador muerto
        sprite.setPosition(x - ANCHO / 2f, y);

        bounds = new Rectangle(x - ANCHO / 2f, y, ANCHO, ALTO);
    }

    public void dibujar() {
        if (!eliminada) {
            sprite.draw(Render.batch);
        }
    }

    public boolean actualizar(float delta) {
        if (eliminada) {
            return true;
        }

        if (!enSuelo) {
            actualizarCaida(delta);
        }

        // Verificar si salió de la pantalla
        if (bounds.y > 2000f) {
            eliminada = true;
        }

        return eliminada;
    }

    private void actualizarCaida(float delta) {
        float yAnterior = bounds.y;

        velocidadY -= GRAVEDAD * delta;
        float nuevoY = bounds.y + velocidadY * delta;

        if (velocidadY <= 0f) {
            for (Rectangle suelo : colisiones.getColisiones()) {
                boolean haySolapamientoHorizontal =
                    bounds.x + bounds.width > suelo.x
                        && bounds.x < suelo.x + suelo.width;

                float alturaSuelo = suelo.y + suelo.height;

                boolean cruzaSuelo =
                    yAnterior >= alturaSuelo
                        && nuevoY <= alturaSuelo;

                if (haySolapamientoHorizontal && cruzaSuelo) {
                    nuevoY = alturaSuelo;
                    enSuelo = true;
                    velocidadY = 0f;
                    break;
                }
            }
        }

        bounds.y = nuevoY;
        sprite.setPosition(bounds.x, bounds.y);
    }

    public boolean estaEliminada() {
        return eliminada;
    }

    public void dispose() {
        // La textura pertenece al GestorRecursos.
    }
}
