package com.prz.juego.entidades.jugadores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.rango.Proyectil;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;

public class BolaFuego implements Lanzable {

    private static final float VELOCIDAD = 400f;
    private static final float DISTANCIA_MAXIMA = 1000f;
    private static final float RADIO = 12f;
    private static final float VELOCIDAD_ROTACION = -720f;
    private static final String TEXTURA = "Personajes/Walter/bola_fuego.png";

    private final int DIRECCION;
    private final Sprite SPRITE;
    private final Circle BOUNDS;
    private final Walter WALTER;
    private final Colisiones COLISION;
    private final ArrayList<Entidad> ENTIDADES;
    private final ArrayList<Lanzable> PROYECTILES;

    private int id;
    private float x;
    private float y;
    private float distanciaRecorrida = 0f;
    private boolean activa = true;

    public BolaFuego(
        float x,
        float y,
        int direccion,
        Walter walter,
        Colisiones colision,
        ArrayList<Entidad> entidades,
        ArrayList<Lanzable> proyectiles) {
        this.x = x;
        this.y = y;
        this.DIRECCION = direccion;
        this.WALTER = walter;
        this.COLISION = colision;
        this.ENTIDADES = entidades;
        this.PROYECTILES = proyectiles;

        SPRITE = new Sprite(GestorRecursos.obtenerTextura(TEXTURA));
        SPRITE.setSize(RADIO * 2f, RADIO * 2f);
        SPRITE.setOriginCenter();
        SPRITE.setPosition(x - RADIO, y - RADIO);
        BOUNDS = new Circle(x, y, RADIO);
    }

    @Override
    public void update(float delta) {
        if (!activa) {
            return;
        }

        float movimiento = VELOCIDAD * delta;
        x += DIRECCION * movimiento;
        distanciaRecorrida += movimiento;

        SPRITE.rotate(VELOCIDAD_ROTACION * DIRECCION * delta);
        SPRITE.setPosition(x - RADIO, y - RADIO);
        BOUNDS.setPosition(x, y);

        if (distanciaRecorrida >= DISTANCIA_MAXIMA || chocaConPared()) {
            activa = false;
            return;
        }

        if (golpearEnemigo() || golpearProyectil()) {
            activa = false;
        }
    }

    private boolean golpearEnemigo() {
        for (Entidad entidad : ENTIDADES) {
            if (!(entidad instanceof Enemigo)) {
                continue;
            }

            Enemigo enemigo = (Enemigo) entidad;

            if (enemigo.estaMuerto() || !enemigo.puedeRecibirDanio()) {
                continue;
            }

            if (chocaConRectangulo(enemigo.getBounds())) {
                enemigo.restarVida(WALTER.getDanio());
                return true;
            }
        }
        return false;
    }

    private boolean golpearProyectil() {
        for (Lanzable lanzable : PROYECTILES) {
            if (!(lanzable instanceof Proyectil)) {
                continue;
            }

            Proyectil proyectil = (Proyectil) lanzable;

            if (proyectil.estaActiva() && proyectil.chocaCon(BOUNDS)) {
                proyectil.restarVida(WALTER.getDanio());
                return true;
            }
        }
        return false;
    }

    private boolean chocaConPared() {
        for (Rectangle pared : COLISION.getColisiones()) {
            if (chocaConRectangulo(pared)) {
                return true;
            }
        }
        return false;
    }

    private boolean chocaConRectangulo(Rectangle rectangulo) {
        float puntoX = Math.max(rectangulo.x, Math.min(x, rectangulo.x + rectangulo.width));
        float puntoY = Math.max(rectangulo.y, Math.min(y, rectangulo.y + rectangulo.height));
        float distanciaX = x - puntoX;
        float distanciaY = y - puntoY;

        return distanciaX * distanciaX + distanciaY * distanciaY <= RADIO * RADIO;
    }

    @Override
    public void dibujar() {
        if (activa) {
            SPRITE.draw(Render.batch);
        }
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        if (!activa) {
            return;
        }

        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(BOUNDS.x, BOUNDS.y, BOUNDS.radius);
    }

    @Override
    public boolean estaActiva() {
        return activa;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public int getDireccion() {
        return DIRECCION;
    }
}
