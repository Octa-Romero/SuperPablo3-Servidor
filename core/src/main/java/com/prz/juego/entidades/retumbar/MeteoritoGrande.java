package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Render;

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
    private static final float ESCALA_ALTO_BOUNDS_OLA = 0.65f;
    private static final float DESPLAZAMIENTO_BASE_FUEGO = -40f;
    private static final float ALTURA_OSCILACION = 35f;
    private static final float DURACION_FUEGO_OLA = 0.6f;
    private static final float DANIO = 1f;

    private final float X;
    private final float SUELO_Y;
    private final float ANCHO_MAPA;
    private final Jugador[] JUGADORES;
    private final TextureRegion REGION_FUEGO;
    private final Sprite SPRITE;
    private final Rectangle BOUNDS = new Rectangle();
    private final int SENTIDO_ROTACION = MathUtils.randomBoolean() ? 1 : -1;
    private final List<FuegoOla> FUEGOS_OLA = new ArrayList<>();
    private final int CANTIDAD_PASOS_OLA;

    private int id;
    private float y;
    private float velocidadY = 0f;
    private boolean impacto = false;
    private float tiempoTotalOla = 0f;
    private float tiempoGeneracionOla = 0f;
    private int pasoActualOla = 1;

    public MeteoritoGrande(
        float x,
        float y,
        float sueloY,
        float anchoMapa,
        Jugador[] jugadores,
        Texture textura,
        TextureRegion regionFuego) {
        this.X = x;
        this.y = y;
        this.SUELO_Y = sueloY;
        this.ANCHO_MAPA = anchoMapa;
        this.JUGADORES = jugadores;
        this.REGION_FUEGO = regionFuego;
        this.CANTIDAD_PASOS_OLA = (int) Math.ceil(Math.max(x, anchoMapa - x) / VELOCIDAD_EXPANSION);

        SPRITE = new Sprite(textura);
        SPRITE.setSize(TAMANO, TAMANO);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x - TAMANO / 2f, y);
        BOUNDS.set(x - TAMANO / 2f, y, TAMANO, TAMANO);
    }

    public boolean update(float delta) {
        if (impacto) {
            return updateOlaFuego(delta);
        }

        velocidadY -= GRAVEDAD * delta;
        y += velocidadY * delta;
        SPRITE.setPosition(X - TAMANO / 2f, y);
        SPRITE.rotate(SENTIDO_ROTACION * VELOCIDAD_ROTACION * delta);
        BOUNDS.set(X - TAMANO / 2f, y, TAMANO, TAMANO);

        if (y <= SUELO_Y) {
            impacto = true;

            if (Servidor.instancia != null) {
                Servidor.instancia.enviarSonidoConPosicion("METEORITO", X, y);
            }

            FUEGOS_OLA.add(new FuegoOla(X));
            return updateOlaFuego(0f);
        }

        daniarJugadores(BOUNDS);
        return true;
    }

    private boolean updateOlaFuego(float delta) {
        tiempoTotalOla += delta;
        tiempoGeneracionOla += delta;

        while (pasoActualOla <= CANTIDAD_PASOS_OLA && tiempoGeneracionOla >= INTERVALO_GENERACION) {
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

    private void daniarJugadores(Rectangle zona) {
        for (Jugador j : JUGADORES) {
            if (!j.estaMuerto() && zona.overlaps(j.getBounds())) {
                j.restarVida(DANIO);
            }
        }
    }

    public void dibujar() {
        if (!impacto) {
            SPRITE.draw(Render.batch);
            return;
        }

        for (FuegoOla fuego : FUEGOS_OLA) {
            Render.batch.draw(
                REGION_FUEGO,
                fuego.x - ANCHO_FUEGO_OLA / 2f,
                fuego.obtenerY(),
                ANCHO_FUEGO_OLA,
                ALTO_FUEGO_OLA);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX() {
        return X;
    }

    public float getY() {
        return y;
    }

    public boolean estaImpactando() {
        return impacto;
    }

    public float getTiempoEfecto() {
        return tiempoTotalOla;
    }

    public float getRotacion() {
        return SPRITE.getRotation();
    }

    private class FuegoOla {

        private final float x;
        private final Rectangle bounds = new Rectangle();
        private float tiempo = 0f;
        private boolean danioAplicado = false;

        private FuegoOla(float x) {
            this.x = x;
            actualizarBounds();
        }

        private boolean update(float delta) {
            tiempo += delta;
            actualizarBounds();

            if (!danioAplicado) {
                daniarJugadores(bounds);
                danioAplicado = true;
            }
            return tiempo < DURACION_FUEGO_OLA;
        }

        private float obtenerY() {
            float progreso = Math.min(1f, tiempo / DURACION_FUEGO_OLA);
            return SUELO_Y
                + DESPLAZAMIENTO_BASE_FUEGO
                + ALTURA_OSCILACION * MathUtils.sin(MathUtils.PI * progreso);
        }

        private void actualizarBounds() {
            bounds.set(
                x - ANCHO_FUEGO_OLA / 2f,
                obtenerY(),
                ANCHO_FUEGO_OLA,
                ALTO_FUEGO_OLA * ESCALA_ALTO_BOUNDS_OLA);
        }
    }
}
