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
    private final Jugador JUGADOR;
    private final Animation<TextureRegion> ANIMACION_FUEGO;
    private final Sprite SPRITE;
    private final Rectangle BOUNDS = new Rectangle();
    private final int SENTIDO_ROTACION = Math.random() < 0.5 ? 1 : -1;
    private final float ANCHO_FUEGO;
    private final float ALTO_FUEGO;

    private float y;
    private float velocidadY;
    private float tiempoFuego;
    private boolean impacto;

    public Meteorito(
            float x,
            float y,
            float sueloY,
            Jugador jugador,
            Texture textura,
            Animation<TextureRegion> animacionFuego) {
        this.X = x;
        this.y = y;
        this.SUELO_Y = sueloY;
        this.JUGADOR = jugador;
        this.ANIMACION_FUEGO = animacionFuego;
        TextureRegion frameFuego = animacionFuego.getKeyFrame(0f);
        ANCHO_FUEGO = TAMANO * ESCALA_ANCHO_FUEGO;
        ALTO_FUEGO =
                ANCHO_FUEGO * frameFuego.getRegionHeight() / frameFuego.getRegionWidth();
        SPRITE = new Sprite(textura);
        SPRITE.setSize(TAMANO, TAMANO);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x - TAMANO / 2f, y);
        BOUNDS.set(x - TAMANO / 2f, y, TAMANO, TAMANO);
    }

    public boolean update(float delta) {
        if (!impacto) {
            velocidadY -= GRAVEDAD * delta;
            y += velocidadY * delta;
            SPRITE.setPosition(X - TAMANO / 2f, y);
            SPRITE.rotate(SENTIDO_ROTACION * VELOCIDAD_ROTACION * delta);
            BOUNDS.setPosition(X - TAMANO / 2f, y);

            if (y <= SUELO_Y) {
                impacto = true;
                Sonido.METEORITO.sonar();
                tiempoFuego = 0f;
                y = SUELO_Y;
                BOUNDS.set(
                        X - ANCHO_BOUNDS_FUEGO / 2f,
                        SUELO_Y,
                        ANCHO_BOUNDS_FUEGO,
                        ALTO_BOUNDS_FUEGO);
            } else if (BOUNDS.overlaps(JUGADOR.getBounds())) {
                JUGADOR.restarVida(DANIO);
            }
            return true;
        }

        tiempoFuego += delta;
        SPRITE.setRegion(ANIMACION_FUEGO.getKeyFrame(tiempoFuego));
        SPRITE.setSize(ANCHO_FUEGO, ALTO_FUEGO);
        SPRITE.setRotation(0f);
        SPRITE.setPosition(X - ANCHO_FUEGO / 2f, SUELO_Y);

        if (BOUNDS.overlaps(JUGADOR.getBounds())) {
            JUGADOR.restarVida(DANIO);
        }
        return tiempoFuego < DURACION_FUEGO;
    }

    public Rectangle getBounds() {
        return BOUNDS;
    }

    public List<Rectangle> getBoundsOla() {
        return new ArrayList<>();
    }

    public void dibujar() {
        SPRITE.draw(Render.batch);
    }
}
