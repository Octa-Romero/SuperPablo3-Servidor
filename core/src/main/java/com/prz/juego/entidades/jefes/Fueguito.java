package com.prz.juego.entidades.jefes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.utilidades.Render;

public class Fueguito {

    private static final float DANIO = 0.5f;
    private static final float ESCALA_ALTO_SPRITE = 1.5f;
    private static final int CANTIDAD_FRAMES = 2;
    private static final float DURACION_FRAME = 0.08f;

    private static final float VELOCIDAD_RADIAL = 450f;
    private static final float ANCHO_RADIAL = 42f;
    private static final float ALTO_RADIAL = 64f;

    private final Sprite SPRITE;
    private final Animation<TextureRegion> ANIMACION;
    private final Circle HITBOX = new Circle();
    private final Jugador JUGADOR;
    private final boolean ES_RADIAL;
    private final float TAMANO;
    private final float VELOCIDAD;
    private final float SUELO_Y;
    private final float VELOCIDAD_X;
    private final float VELOCIDAD_Y;
    private final float ANCHO_MAPA;
    private final float ALTO_MAPA;

    private float x;
    private float y;
    private float tiempoAnimacion;

    // Constructor para fueguito de lluvia
    public Fueguito(
            float x,
            float y,
            float tamano,
            float velocidad,
            float sueloY,
            Jugador jugador,
            Texture textura) {
        this.x = x;
        this.y = y;
        this.TAMANO = tamano;
        this.VELOCIDAD = velocidad;
        this.SUELO_Y = sueloY;
        this.JUGADOR = jugador;
        this.ES_RADIAL = false;
        this.VELOCIDAD_X = 0f;
        this.VELOCIDAD_Y = 0f;
        this.ANCHO_MAPA = 0f;
        this.ALTO_MAPA = 0f;
        TextureRegion[][] frames = TextureRegion.split(
            textura,
            textura.getWidth() / CANTIDAD_FRAMES,
            textura.getHeight());
        ANIMACION = new Animation<>(DURACION_FRAME, frames[0][0], frames[0][1]);
        ANIMACION.setPlayMode(Animation.PlayMode.LOOP);
        SPRITE = new Sprite(frames[0][0]);
        SPRITE.setSize(tamano, tamano * ESCALA_ALTO_SPRITE);
        SPRITE.setPosition(x - tamano / 2f, y);
        HITBOX.set(x, y + tamano / 2f, tamano / 2f);
    }

    // Constructor para fueguitos en 8 direcciones
    public Fueguito(
            float x,
            float y,
            float angulo,
            float anchoMapa,
            float altoMapa,
            Jugador jugador,
            Animation<TextureRegion> animacion) {
        this.x = x;
        this.y = y;
        this.JUGADOR = jugador;
        this.ES_RADIAL = true;
        this.TAMANO = 0f;
        this.VELOCIDAD = 0f;
        this.SUELO_Y = 0f;
        this.ANCHO_MAPA = anchoMapa;
        this.ALTO_MAPA = altoMapa;
        ANIMACION = animacion;
        VELOCIDAD_X = (float) Math.cos(angulo) * VELOCIDAD_RADIAL;
        VELOCIDAD_Y = (float) Math.sin(angulo) * VELOCIDAD_RADIAL;
        SPRITE = new Sprite(animacion.getKeyFrame(0f));
        SPRITE.setSize(ANCHO_RADIAL, ALTO_RADIAL);
        SPRITE.setOriginCenter();
        SPRITE.setRotation((float) Math.toDegrees(angulo) + 90f);
        actualizarPosicionRadial();
    }

    public boolean update(float delta) {
        tiempoAnimacion += delta;
        SPRITE.setRegion(ANIMACION.getKeyFrame(tiempoAnimacion));

        if (ES_RADIAL) {
            return updateRadial(delta);
        }
        return updateVertical(delta);
    }

    private boolean updateVertical(float delta) {
        y -= VELOCIDAD * delta;
        SPRITE.setY(y);
        HITBOX.setY(y + TAMANO / 2f);

        if (y <= SUELO_Y) {
            return false;
        }

        if (Intersector.overlaps(HITBOX, JUGADOR.getBounds())) {
            JUGADOR.restarVida(DANIO);
            return false;
        }
        return true;
    }

    private boolean updateRadial(float delta) {
        x += VELOCIDAD_X * delta;
        y += VELOCIDAD_Y * delta;
        actualizarPosicionRadial();

        if (Intersector.overlaps(HITBOX, JUGADOR.getBounds())) {
            JUGADOR.restarVida(DANIO);
            return false;
        }

        return x >= -ALTO_RADIAL
            && x <= ANCHO_MAPA + ALTO_RADIAL
            && y >= -ALTO_RADIAL
            && y <= ALTO_MAPA + ALTO_RADIAL;
    }

    private void actualizarPosicionRadial() {
        float direccionX = VELOCIDAD_X / VELOCIDAD_RADIAL;
        float direccionY = VELOCIDAD_Y / VELOCIDAD_RADIAL;
        float centroX = x - direccionX * (ALTO_RADIAL - ANCHO_RADIAL) / 2f;
        float centroY = y - direccionY * (ALTO_RADIAL - ANCHO_RADIAL) / 2f;
        SPRITE.setPosition(centroX - ANCHO_RADIAL / 2f, centroY - ALTO_RADIAL / 2f);
        HITBOX.set(x, y, ANCHO_RADIAL / 2f);
    }

    public Circle getHitbox() {
        return HITBOX;
    }

    public void dibujar() {
        SPRITE.draw(Render.batch);
    }
}
