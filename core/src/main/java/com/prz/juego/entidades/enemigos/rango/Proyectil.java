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

    private final float ANCHO;
    private final float ALTO;
    private final float VELOCIDAD;
    private final float DISTANCIA_MAXIMA;
    private final Vector2 DIRECCION;
    private final double DANIO;
    private final Sprite SPRITE;
    private final Polygon BOUNDS;
    private final Polygon POLIGONO_TEMPORAL = new Polygon();
    private final float[] VERTICES_TEMPORALES = new float[8];
    private final Jugador JUGADOR;
    private final Colisiones COLISION;

    private int id;
    private float x;
    private float y;
    private double vida;
    private float distanciaRecorrida = 0f;
    private boolean activa = true;

    public Proyectil(
        float x,
        float y,
        Vector2 direccion,
        float ancho,
        float alto,
        float velocidad,
        float distanciaMaxima,
        double danio,
        double vida,
        Jugador jugador,
        Colisiones colision,
        Texture textura) {
        this.x = x;
        this.y = y;
        this.DIRECCION = new Vector2(direccion).nor();
        this.ANCHO = ancho;
        this.ALTO = alto;
        this.VELOCIDAD = velocidad;
        this.DISTANCIA_MAXIMA = distanciaMaxima;
        this.DANIO = danio;
        this.vida = vida;
        this.JUGADOR = jugador;
        this.COLISION = colision;

        float angulo = DIRECCION.angleDeg();

        SPRITE = new Sprite(textura);
        SPRITE.setSize(ancho, alto);
        SPRITE.setOriginCenter();
        SPRITE.setRotation(angulo);

        BOUNDS = new Polygon(new float[] {0, 0, ancho, 0, ancho, alto, 0, alto});
        BOUNDS.setOrigin(ancho / 2f, alto / 2f);
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
        float derecha = rectangulo.x + rectangulo.width;
        float arriba = rectangulo.y + rectangulo.height;

        VERTICES_TEMPORALES[0] = rectangulo.x;
        VERTICES_TEMPORALES[1] = rectangulo.y;
        VERTICES_TEMPORALES[2] = derecha;
        VERTICES_TEMPORALES[3] = rectangulo.y;
        VERTICES_TEMPORALES[4] = derecha;
        VERTICES_TEMPORALES[5] = arriba;
        VERTICES_TEMPORALES[6] = rectangulo.x;
        VERTICES_TEMPORALES[7] = arriba;
        POLIGONO_TEMPORAL.setVertices(VERTICES_TEMPORALES);

        return Intersector.overlapConvexPolygons(BOUNDS, POLIGONO_TEMPORAL);
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

    public float getAngulo() {
        return DIRECCION.angleDeg();
    }

    public float getAncho() {
        return ANCHO;
    }

    public float getAlto() {
        return ALTO;
    }

    public void desactivar() {
        activa = false;
    }
}
