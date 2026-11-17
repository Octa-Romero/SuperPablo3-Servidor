package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Render;

import java.util.Collections;
import java.util.List;

public class Meteorito {

    private static final float TAMANO = 82f;
    private static final float ESCALA_ANCHO_FUEGO = 1.35f;
    private static final float ANCHO_BOUNDS_FUEGO = 110f;
    private static final float ALTO_BOUNDS_FUEGO = 38f;
    private static final float VELOCIDAD_ROTACION = 180f;
    private static final float DURACION_FUEGO = 15f;
    private static final float GRAVEDAD = 250f;
    private static final float DANIO = 1f;

    private final float X;
    private final float SUELO_Y;
    private final Jugador[] JUGADORES;
    private final TextureRegion REGION_FUEGO;
    private final Sprite SPRITE;
    private final Rectangle BOUNDS = new Rectangle();
    private final int SENTIDO_ROTACION = MathUtils.randomBoolean() ? 1 : -1;
    private final float ANCHO_FUEGO;
    private final float ALTO_FUEGO;

    private int id;
    private float y;
    private float velocidadY = 0f;
    private float tiempoFuego = 0f;
    private boolean impacto = false;

    public Meteorito(
        float x,
        float y,
        float sueloY,
        Jugador[] jugadores,
        Texture textura,
        TextureRegion regionFuego) {
        this.X = x;
        this.y = y;
        this.SUELO_Y = sueloY;
        this.JUGADORES = jugadores;
        this.REGION_FUEGO = regionFuego;
        this.ANCHO_FUEGO = TAMANO * ESCALA_ANCHO_FUEGO;
        this.ALTO_FUEGO = ANCHO_FUEGO * regionFuego.getRegionHeight() / regionFuego.getRegionWidth();

        SPRITE = new Sprite(textura);
        SPRITE.setSize(TAMANO, TAMANO);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x - TAMANO / 2f, y);
        BOUNDS.set(x - TAMANO / 2f, y, TAMANO, TAMANO);
    }

    public boolean update(float delta) {
        if (impacto) {
            tiempoFuego += delta;
            daniarJugadores();
            return tiempoFuego < DURACION_FUEGO;
        }

        velocidadY -= GRAVEDAD * delta;
        y += velocidadY * delta;
        SPRITE.setPosition(X - TAMANO / 2f, y);
        SPRITE.rotate(SENTIDO_ROTACION * VELOCIDAD_ROTACION * delta);
        BOUNDS.setPosition(X - TAMANO / 2f, y);

        if (y <= SUELO_Y) {
            aterrizar();
        } else {
            daniarJugadores();
        }
        return true;
    }

    private void aterrizar() {
        impacto = true;
        tiempoFuego = 0f;
        y = SUELO_Y;

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion("METEORITO", X, y);
        }

        BOUNDS.set(X - ANCHO_BOUNDS_FUEGO / 2f, SUELO_Y, ANCHO_BOUNDS_FUEGO, ALTO_BOUNDS_FUEGO);
        SPRITE.setRegion(REGION_FUEGO);
        SPRITE.setSize(ANCHO_FUEGO, ALTO_FUEGO);
        SPRITE.setRotation(0f);
        SPRITE.setPosition(X - ANCHO_FUEGO / 2f, SUELO_Y);
    }

    private void daniarJugadores() {
        for (Jugador j : JUGADORES) {
            if (!j.estaMuerto() && BOUNDS.overlaps(j.getBounds())) {
                j.restarVida(DANIO);
            }
        }
    }

    public void dibujar() {
        SPRITE.draw(Render.batch);
    }

    public Rectangle getBounds() {
        return BOUNDS;
    }

    public List<Rectangle> getBoundsOla() {
        return Collections.emptyList();
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

    public float getRotacion() {
        return SPRITE.getRotation();
    }

    public float getTiempoEfecto() {
        return tiempoFuego;
    }
}
