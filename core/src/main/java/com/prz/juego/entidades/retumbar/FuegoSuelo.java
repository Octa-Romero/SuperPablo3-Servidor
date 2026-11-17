package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Frames;
import com.prz.juego.utilidades.Render;

public class FuegoSuelo {

    private static final float ALTO = 50f;
    private static final double DANIO = 1;
    private static final float TIEMPO_EMERGER = 0.1f;
    private static final float TIEMPO_BAJAR = 0.1f;
    private static final float ALTO_MINIMO = 1f;
    private static final float X_OCULTA = -100000f;
    private static final float SOLAPAMIENTO_LLAMA = 12f;
    private static final float MULTIPLICADOR_ALTURA_SPRITE = 2f;
    private static final int CANTIDAD_FRAMES = 2;

    private final float X_INICIO;
    private final float Y_SUELO;
    private final float ANCHO;
    private final Jugador[] JUGADORES;
    private final TextureRegion REGION;
    private final Rectangle BOUNDS = new Rectangle();

    private float x;
    private float alto = ALTO;
    private boolean activa = false;
    private boolean bajando = false;
    private float tiempoActiva = 0f;
    private float tiempoRestante = 0f;
    private float tiempoBajada = 0f;

    public FuegoSuelo(float xMin, float xMax, float ySuelo, Jugador[] jugadores) {
        X_INICIO = xMin;
        Y_SUELO = ySuelo;
        ANCHO = xMax - xMin;
        JUGADORES = jugadores;
        REGION = Frames.primero(GestorRecursos.obtenerTextura("Jefe/fuegoSuelo.png"), CANTIDAD_FRAMES);
        ocultar();
    }

    public void update(float delta) {
        if (!activa) {
            return;
        }

        if (bajando) {
            tiempoBajada += delta;
            aplicarAlto(Math.max(ALTO_MINIMO, ALTO * Math.max(0f, 1f - tiempoBajada / TIEMPO_BAJAR)));

            if (tiempoBajada >= TIEMPO_BAJAR) {
                ocultar();
            }
            return;
        }

        tiempoActiva += delta;
        tiempoRestante -= delta;

        aplicarAlto(Math.max(ALTO_MINIMO, ALTO * Math.min(1f, tiempoActiva / TIEMPO_EMERGER)));
        daniarJugadores();

        if (tiempoRestante <= 0f) {
            bajando = true;
            tiempoBajada = 0f;
        }
    }

    private void daniarJugadores() {
        for (Jugador j : JUGADORES) {
            if (!j.estaMuerto() && BOUNDS.overlaps(j.getBounds())) {
                j.restarVida(DANIO);
            }
        }
    }

    public void activar(float duracion) {
        activa = true;
        bajando = false;
        tiempoActiva = 0f;
        tiempoRestante = duracion;
        tiempoBajada = 0f;
        x = X_INICIO;
        aplicarAlto(ALTO_MINIMO);
    }

    public void desactivar() {
        ocultar();
    }

    private void ocultar() {
        activa = false;
        bajando = false;
        x = X_OCULTA;
        aplicarAlto(ALTO);
    }

    private void aplicarAlto(float nuevoAlto) {
        alto = nuevoAlto;
        BOUNDS.set(x, Y_SUELO, ANCHO, nuevoAlto);
    }

    public void dibujar() {
        if (!activa) {
            return;
        }

        float altoLlama = Math.max(ALTO_MINIMO, alto) * MULTIPLICADOR_ALTURA_SPRITE;
        float anchoLlama = altoLlama * REGION.getRegionWidth() / REGION.getRegionHeight();
        float paso = Math.max(1f, anchoLlama - SOLAPAMIENTO_LLAMA);
        float limite = X_INICIO + ANCHO - anchoLlama / 2f;

        for (float xLlama = X_INICIO - anchoLlama / 2f; xLlama < limite; xLlama += paso) {
            Render.batch.draw(REGION, xLlama, Y_SUELO, anchoLlama, altoLlama);
        }
    }

    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        if (activa) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height);
        }
    }

    public boolean estaActiva() {
        return activa;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return Y_SUELO;
    }

    public float getAncho() {
        return ANCHO;
    }

    public float getAlto() {
        return alto;
    }

    public Rectangle getBounds() {
        return BOUNDS;
    }
}
