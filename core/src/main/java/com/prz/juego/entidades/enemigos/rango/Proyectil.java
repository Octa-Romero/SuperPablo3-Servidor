package com.prz.juego.entidades.enemigos.rango;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

public class Proyectil implements Lanzable {

    private float x;
    private float y;
    private final float ANCHO;
    private final float ALTO;
    private final float VELOCIDAD;
    private final float DISTANCIA_MAXIMA;
    private final Vector2 DIRECCION;
    private final double DANIO;
    private double vida;
    private float distanciaRecorrida;
    private final Sprite SPRITE;
    private final Polygon BOUNDS;
    private boolean activa = true;
    private final Jugador JUGADOR;
    private final Colisiones COLISION;

    public Proyectil(
            float x,
            float y,
            Vector2 DIRECCION,
            float ANCHO,
            float ALTO,
            float VELOCIDAD,
            float DISTANCIA_MAXIMA,
            double DANIO,
            double vida,
            Jugador JUGADOR,
            Colisiones COLISION,
            Texture textura) {
        this.x = x;
        this.y = y;
        this.DIRECCION = new Vector2(DIRECCION).nor();
        this.ANCHO = ANCHO;
        this.ALTO = ALTO;
        this.VELOCIDAD = VELOCIDAD;
        this.DISTANCIA_MAXIMA = DISTANCIA_MAXIMA;
        this.DANIO = DANIO;
        this.vida = vida;
        this.JUGADOR = JUGADOR;
        this.COLISION = COLISION;

        float angulo = this.DIRECCION.angleDeg();

        SPRITE = new Sprite(textura);
        SPRITE.setSize(ANCHO, ALTO);
        SPRITE.setOriginCenter();
        SPRITE.setRotation(angulo);

        BOUNDS = new Polygon(new float[] {0, 0, ANCHO, 0, ANCHO, ALTO, 0, ALTO});
        BOUNDS.setOrigin(ANCHO / 2f, ALTO / 2f);
        BOUNDS.setRotation(angulo);

        actualizarPosicion();
    }

    @Override
    public void update(float delta) {
        if (!activa) {
            return;
        }

        float movimiento = VELOCIDAD * delta;

        x += DIRECCION.x * movimiento;
        y += DIRECCION.y * movimiento;
        distanciaRecorrida += movimiento;

        actualizarPosicion();

        if (distanciaRecorrida >= DISTANCIA_MAXIMA || chocaConPared()) {
            activa = false;
            return;
        }

        if (!JUGADOR.estaMuerto() && chocaCon(JUGADOR.getBounds())) {
            JUGADOR.restarVida(DANIO);
            activa = false;
        }
    }

    private void actualizarPosicion() {
        SPRITE.setPosition(x - ANCHO / 2f, y - ALTO / 2f);
        BOUNDS.setPosition(x - ANCHO / 2f, y - ALTO / 2f);
    }

    private boolean chocaConPared() {
        if (COLISION == null) {
            return false;
        }

        for (Rectangle pared : COLISION.getColisiones()) {
            if (chocaCon(pared)) {
                return true;
            }
        }
        return false;
    }

    public boolean chocaCon(Rectangle rectangulo) {
        Polygon poligono =
                new Polygon(
                        new float[] {
                            rectangulo.x,
                            rectangulo.y,
                            rectangulo.x + rectangulo.width,
                            rectangulo.y,
                            rectangulo.x + rectangulo.width,
                            rectangulo.y + rectangulo.height,
                            rectangulo.x,
                            rectangulo.y + rectangulo.height
                        });

        return Intersector.overlapConvexPolygons(BOUNDS, poligono);
    }

    public boolean chocaCon(Circle circulo) {
        if (BOUNDS.contains(circulo.x, circulo.y)) {
            return true;
        }

        float[] vertices = BOUNDS.getTransformedVertices();
        Vector2 centro = new Vector2(circulo.x, circulo.y);
        float radioCuadrado = circulo.radius * circulo.radius;

        for (int i = 0; i < vertices.length; i += 2) {
            int siguiente = (i + 2) % vertices.length;
            Vector2 inicio = new Vector2(vertices[i], vertices[i + 1]);
            Vector2 fin = new Vector2(vertices[siguiente], vertices[siguiente + 1]);

            if (Intersector.intersectSegmentCircle(inicio, fin, centro, radioCuadrado)) {
                return true;
            }
        }
        return false;
    }

    public void restarVida(double cantidad) {
        if (!activa) {
            return;
        }

        vida -= cantidad;

        if (vida <= 0) {
            vida = 0;
            activa = false;
        }
    }

    public double getVida() {
        return vida;
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
        shapeRenderer.polygon(BOUNDS.getTransformedVertices());
    }

    @Override
    public boolean estaActiva() {
        return activa;
    }

    public void desactivar() {
        activa = false;
    }
}
