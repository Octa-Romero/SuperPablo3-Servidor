package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.utilidades.Render;

public class Fueguito {

    private static final float DANIO = 0.5f;
    private static final float ESCALA_ALTO_SPRITE = 1.5f;
    private static final float VELOCIDAD_RADIAL = 450f;
    private static final float ANCHO_RADIAL = 42f;
    private static final float ALTO_RADIAL = 64f;

    private final Sprite SPRITE;
    private final Circle HITBOX = new Circle();
    private final Jugador[] JUGADORES;
    private final boolean ES_RADIAL;
    private final float TAMANO;
    private final float VELOCIDAD;
    private final float SUELO_Y;
    private final float VELOCIDAD_X;
    private final float VELOCIDAD_Y;
    private final float ANCHO_MAPA;
    private final float ALTO_MAPA;

    private int id;
    private float x;
    private float y;

    private Fueguito(
        boolean radial,
        float x,
        float y,
        float tamano,
        float velocidad,
        float sueloY,
        float angulo,
        float anchoMapa,
        float altoMapa,
        Jugador[] jugadores,
        TextureRegion region) {
        this.ES_RADIAL = radial;
        this.x = x;
        this.y = y;
        this.TAMANO = tamano;
        this.VELOCIDAD = velocidad;
        this.SUELO_Y = sueloY;
        this.ANCHO_MAPA = anchoMapa;
        this.ALTO_MAPA = altoMapa;
        this.JUGADORES = jugadores;
        this.VELOCIDAD_X = radial ? MathUtils.cos(angulo) * VELOCIDAD_RADIAL : 0f;
        this.VELOCIDAD_Y = radial ? MathUtils.sin(angulo) * VELOCIDAD_RADIAL : 0f;

        SPRITE = new Sprite(region);

        if (radial) {
            SPRITE.setSize(ANCHO_RADIAL, ALTO_RADIAL);
            SPRITE.setOriginCenter();
            SPRITE.setRotation(angulo * MathUtils.radiansToDegrees + 90f);
            actualizarPosicionRadial();
        } else {
            SPRITE.setSize(tamano, tamano * ESCALA_ALTO_SPRITE);
            SPRITE.setPosition(x - tamano / 2f, y);
            HITBOX.set(x, y + tamano / 2f, tamano / 2f);
        }
    }

    public static Fueguito crearVertical(
        float x,
        float y,
        float tamano,
        float velocidad,
        float sueloY,
        Jugador[] jugadores,
        TextureRegion region) {
        return new Fueguito(false, x, y, tamano, velocidad, sueloY, 0f, 0f, 0f, jugadores, region);
    }

    public static Fueguito crearRadial(
        float x,
        float y,
        float angulo,
        float anchoMapa,
        float altoMapa,
        Jugador[] jugadores,
        TextureRegion region) {
        return new Fueguito(
            true, x, y, 0f, 0f, 0f, angulo, anchoMapa, altoMapa, jugadores, region);
    }

    public boolean update(float delta) {
        return ES_RADIAL ? updateRadial(delta) : updateVertical(delta);
    }

    private boolean updateVertical(float delta) {
        y -= VELOCIDAD * delta;
        SPRITE.setY(y);
        HITBOX.setY(y + TAMANO / 2f);

        return y > SUELO_Y && !golpeaJugador();
    }

    private boolean updateRadial(float delta) {
        x += VELOCIDAD_X * delta;
        y += VELOCIDAD_Y * delta;
        actualizarPosicionRadial();

        if (golpeaJugador()) {
            return false;
        }

        return x >= -ALTO_RADIAL
            && x <= ANCHO_MAPA + ALTO_RADIAL
            && y >= -ALTO_RADIAL
            && y <= ALTO_MAPA + ALTO_RADIAL;
    }

    private boolean golpeaJugador() {
        for (Jugador j : JUGADORES) {
            if (!j.estaMuerto() && Intersector.overlaps(HITBOX, j.getBounds())) {
                j.restarVida(DANIO);
                return true;
            }
        }
        return false;
    }

    private void actualizarPosicionRadial() {
        float direccionX = VELOCIDAD_X / VELOCIDAD_RADIAL;
        float direccionY = VELOCIDAD_Y / VELOCIDAD_RADIAL;
        float centroX = x - direccionX * (ALTO_RADIAL - ANCHO_RADIAL) / 2f;
        float centroY = y - direccionY * (ALTO_RADIAL - ANCHO_RADIAL) / 2f;

        SPRITE.setPosition(centroX - ANCHO_RADIAL / 2f, centroY - ALTO_RADIAL / 2f);
        HITBOX.set(x, y, ANCHO_RADIAL / 2f);
    }

    public void dibujar() {
        SPRITE.draw(Render.batch);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean esRadial() {
        return ES_RADIAL;
    }

    public float getXSprite() {
        return SPRITE.getX();
    }

    public float getYSprite() {
        return SPRITE.getY();
    }

    public float getTamano() {
        return TAMANO;
    }

    public float getRotacion() {
        return SPRITE.getRotation();
    }

    public Circle getHitbox() {
        return HITBOX;
    }
}
