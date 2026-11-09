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
    private static final float DISTANCIA_MAXIMA = 1000;
    private static final float RADIO = 12f;
    private static final float VELOCIDAD_ROTACION = -720f;
    private float x;
    private float y;
    private final float DIRECCION;
    private float distanciaRecorrida;
    private final Sprite SPRITE;
    private final Circle BOUNDS;
    private boolean activa = true;
    private final Walter WALTER;
    private final Colisiones COLISION;
    private final ArrayList<Entidad> ENTIDADES;
    private final ArrayList<Lanzable> PROYECTILES;

    public BolaFuego(
            float x,
            float y,
            float DIRECCION,
            Walter WALTER,
            Colisiones COLISION,
            ArrayList<Entidad> ENTIDADES,
            ArrayList<Lanzable> PROYECTILES) {
        this.x = x;
        this.y = y;
        this.DIRECCION = DIRECCION;
        this.WALTER = WALTER;
        this.COLISION = COLISION;
        this.ENTIDADES = ENTIDADES;
        this.PROYECTILES = PROYECTILES;
        SPRITE = new Sprite(GestorRecursos.obtenerTextura("Personajes/Walter/bola_fuego.png"));
        SPRITE.setSize(RADIO * 2, RADIO * 2);
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
        SPRITE.rotate(VELOCIDAD_ROTACION * DIRECCION * delta);

        x += DIRECCION * movimiento;
        distanciaRecorrida += movimiento;

        BOUNDS.setPosition(x, y);
        SPRITE.setPosition(x - RADIO, y - RADIO);

        if (distanciaRecorrida >= DISTANCIA_MAXIMA) {
            activa = false;
            return;
        }

        if (chocaConPared()) {
            activa = false;
            return;
        }

        for (Entidad entidad : ENTIDADES) {
            if (!(entidad instanceof Enemigo)) {
                continue;
            }

            Enemigo enemigo = (Enemigo) entidad;

            if (enemigo.estaMuerto() || !enemigo.puedeRecibirDanio()) {
                continue;
            }

            if (chocaConEnemigo(enemigo)) {
                enemigo.restarVida(WALTER.getDanio());
                activa = false;
                return;
            }
        }

        for (Lanzable lanzable : PROYECTILES) {
            if (!(lanzable instanceof Proyectil)) {
                continue;
            }

            Proyectil proyectil = (Proyectil) lanzable;

            if (proyectil.estaActiva() && proyectil.chocaCon(BOUNDS)) {
                proyectil.restarVida(WALTER.getDanio());
                activa = false;
                return;
            }
        }
    }

    private boolean chocaConPared() {
        for (Rectangle pared : COLISION.getColisiones()) {
            float puntoX = Math.max(pared.x, Math.min(x, pared.x + pared.width));
            float puntoY = Math.max(pared.y, Math.min(y, pared.y + pared.height));
            float distanciaX = x - puntoX;
            float distanciaY = y - puntoY;

            if (distanciaX * distanciaX + distanciaY * distanciaY <= RADIO * RADIO) {
                return true;
            }
        }
        return false;
    }

    private boolean chocaConEnemigo(Enemigo enemigo) {
        Rectangle enemigoBounds = enemigo.getBounds();
        float puntoX =
                Math.max(enemigoBounds.x, Math.min(x, enemigoBounds.x + enemigoBounds.width));
        float puntoY =
                Math.max(enemigoBounds.y, Math.min(y, enemigoBounds.y + enemigoBounds.height));
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
}
