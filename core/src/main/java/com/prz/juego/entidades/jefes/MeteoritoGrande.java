package com.prz.juego.entidades.jefes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;
import java.util.List;

public class MeteoritoGrande {

    private static final float TAMANO = 150f;
    private static final float GRAVEDAD = 100f;
    private static final float VELOCIDAD_ROTACION = 100f;
    private static final float VELOCIDAD_EXPANSION = 55f;
    private static final float INTERVALO_GENERACION = 0.08f;
    private static final float ANCHO_FUEGO_OLA = 90f;
    private static final float ALTO_FUEGO_OLA = 110f;
    private static final float DESPLAZAMIENTO_BASE_FUEGO = -40f;
    private static final float ALTURA_OSCILACION = 35f;
    private static final float DURACION_FUEGO_OLA = 0.6f;
    private static final float DANIO = 1f;

    private final float X;
    private final float SUELO_Y;
    private final float ANCHO_MAPA;
    private final Jugador JUGADOR;
    private final Animation<TextureRegion> ANIMACION_FUEGO;
    private final Sprite SPRITE;
    private final Rectangle BOUNDS = new Rectangle();
    private final int SENTIDO_ROTACION = Math.random() < 0.5 ? 1 : -1;
    private final List<FuegoOla> FUEGOS_OLA = new ArrayList<>();
    private final int CANTIDAD_PASOS_OLA;

    private float y;
    private float velocidadY;
    private boolean impacto;
    private boolean danioAplicado;
    private boolean olaCreada;
    private float tiempoTotalOla;
    private float tiempoGeneracionOla;
    private int pasoActualOla;

    public MeteoritoGrande(
            float x,
            float y,
            float sueloY,
            float anchoMapa,
            Jugador jugador,
            Texture textura,
            Animation<TextureRegion> animacionFuego) {
        this.X = x;
        this.y = y;
        this.SUELO_Y = sueloY;
        this.ANCHO_MAPA = anchoMapa;
        this.JUGADOR = jugador;
        this.ANIMACION_FUEGO = animacionFuego;
        this.CANTIDAD_PASOS_OLA =
                (int) Math.ceil(Math.max(x, anchoMapa - x) / VELOCIDAD_EXPANSION);
        this.pasoActualOla = 1;
        this.olaCreada = false;
        this.impacto = false;
        this.danioAplicado = false;
        SPRITE = new Sprite(textura);
        SPRITE.setSize(TAMANO, TAMANO);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x - TAMANO / 2f, y);
        BOUNDS.set(x - TAMANO / 2f, y, TAMANO, TAMANO);
    }

    public boolean update(float delta) {
        if (impacto && olaCreada) {
            return updateOlaFuego(delta);
        }

        if (!impacto) {
            velocidadY -= GRAVEDAD * delta;
            y += velocidadY * delta;
            SPRITE.setPosition(X - TAMANO / 2f, y);
            SPRITE.rotate(SENTIDO_ROTACION * VELOCIDAD_ROTACION * delta);
            BOUNDS.set(X - TAMANO / 2f, y, TAMANO, TAMANO);

            if (y <= SUELO_Y) {
                impacto = true;
                Sonido.METEORITO.sonar();
                crearOlaFuego();
                return updateOlaFuego(0f);
            } else if (BOUNDS.overlaps(JUGADOR.getBounds())) {
                if (!danioAplicado) {
                    JUGADOR.restarVida(DANIO);
                    danioAplicado = true;
                }
            }
            return true;
        }
        return true;
    }

    private void crearOlaFuego() {
        FUEGOS_OLA.add(new FuegoOla(X));
        olaCreada = true;
    }

    private boolean updateOlaFuego(float delta) {
        tiempoTotalOla += delta;
        tiempoGeneracionOla += delta;
        while (pasoActualOla <= CANTIDAD_PASOS_OLA
                && tiempoGeneracionOla >= INTERVALO_GENERACION) {
            tiempoGeneracionOla -= INTERVALO_GENERACION;
            float desplazamiento = pasoActualOla * VELOCIDAD_EXPANSION;
            agregarFuegoOla(X - desplazamiento);
            agregarFuegoOla(X + desplazamiento);
            pasoActualOla++;
        }

        FUEGOS_OLA.removeIf(fuego -> !fuego.update(delta));
        return pasoActualOla <= CANTIDAD_PASOS_OLA || !FUEGOS_OLA.isEmpty();
    }

    private void agregarFuegoOla(float x) {
        if (x >= 0f && x <= ANCHO_MAPA) {
            FUEGOS_OLA.add(new FuegoOla(x));
        }
    }

    public Rectangle getBounds() {
        return BOUNDS;
    }

    public List<Rectangle> getBoundsOla() {
        List<Rectangle> bounds = new ArrayList<>(FUEGOS_OLA.size());
        for (FuegoOla fuego : FUEGOS_OLA) {
            bounds.add(fuego.bounds);
        }
        return bounds;
    }

    public void dibujar() {
        if (impacto && olaCreada) {
            dibujarOlaFuego();
            return;
        }
        SPRITE.draw(Render.batch);
    }

    private void dibujarOlaFuego() {
        TextureRegion frame = ANIMACION_FUEGO.getKeyFrame(tiempoTotalOla);
        for (FuegoOla fuego : FUEGOS_OLA) {
            float yFuego = fuego.obtenerY();
            Render.batch.draw(
                    frame,
                    fuego.x - ANCHO_FUEGO_OLA / 2f,
                    yFuego,
                    ANCHO_FUEGO_OLA,
                    ALTO_FUEGO_OLA);
        }
    }

    private class FuegoOla {
        private final float x;
        private final Rectangle bounds = new Rectangle();
        private float tiempo;
        private boolean danioAplicado;

        private FuegoOla(float x) {
            this.x = x;
            actualizarBounds();
        }

        private boolean update(float delta) {
            tiempo += delta;
            actualizarBounds();
            if (!danioAplicado && bounds.overlaps(JUGADOR.getBounds())) {
                JUGADOR.restarVida(DANIO);
                danioAplicado = true;
            }
            return tiempo < DURACION_FUEGO_OLA;
        }

        private float obtenerY() {
            float progreso = Math.min(1f, tiempo / DURACION_FUEGO_OLA);
            return SUELO_Y
                    + DESPLAZAMIENTO_BASE_FUEGO
                    + ALTURA_OSCILACION * (float) Math.sin(Math.PI * progreso);
        }

        private void actualizarBounds() {
            bounds.set(
                    x - ANCHO_FUEGO_OLA / 2f,
                    obtenerY(),
                    ANCHO_FUEGO_OLA,
                    ALTO_FUEGO_OLA * 0.65f);
        }
    }
}
