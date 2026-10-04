package com.prz.juego.entidades;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class OrbeMagica {

    private static final float VELOCIDAD = 400f;
    private static final float DISTANCIA_MAXIMA = 400f;
    private static final float RADIO = 12f;
    private float x;
    private float y;
    private final float DIRECCION;
    private float distanciaRecorrida;
    private final Sprite SPRITE;
    private final Circle BOUNDS;
    private boolean activa = true;
    private final Walter walter;
    private final Colisiones colision;
    private final ArrayList<Entidad> entidades;

    public OrbeMagica(float x, float y, float DIRECCION, Walter walter, Colisiones colision, ArrayList<Entidad> entidades) {
        this.x = x;
        this.y = y;
        this.DIRECCION = DIRECCION;
        this.walter = walter;
        this.colision = colision;
        this.entidades = entidades;
        SPRITE = new Sprite(
            GestorRecursos.obtenerTextura("Personajes/Walter/orbe.png")
        );
        SPRITE.setSize(RADIO * 2, RADIO * 2);
        SPRITE.setPosition(x - RADIO, y - RADIO);
        BOUNDS = new Circle(x, y, RADIO);
    }

    public void update(float delta) {
        if (!activa) {
            return;
        }

        float movimiento = VELOCIDAD * delta;

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

        for (Entidad entidad : entidades) {
            if (!(entidad instanceof Enemigo)) {
                continue;
            }

            Enemigo enemigo = (Enemigo) entidad;

            if (enemigo.estaMuerto()) {
                continue;
            }

            if (chocaConEnemigo(enemigo)) {
                enemigo.restarVida(walter.getDanio());
                activa = false;
                return;
            }
        }
    }

    private boolean chocaConPared() {
        for (Rectangle pared : colision.getColisiones()) {
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
        float puntoX = Math.max(enemigoBounds.x, Math.min(x, enemigoBounds.x + enemigoBounds.width));
        float puntoY = Math.max(enemigoBounds.y, Math.min(y, enemigoBounds.y + enemigoBounds.height));
        float distanciaX = x - puntoX;
        float distanciaY = y - puntoY;

        return distanciaX * distanciaX + distanciaY * distanciaY <= RADIO * RADIO;
    }

    public void dibujar() {
        if (activa) {
            SPRITE.draw(Render.batch);
        }
    }

    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        if (!activa) {
            return;
        }

        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(BOUNDS.x, BOUNDS.y, BOUNDS.radius);
    }

    public boolean estaActiva() {
        return activa;
    }

    public void dispose() {
        activa = false;
    }
}
