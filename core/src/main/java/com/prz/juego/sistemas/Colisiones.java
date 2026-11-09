package com.prz.juego.sistemas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.jugadores.Jugador;

import java.util.ArrayList;

public class Colisiones {

    private final ArrayList<Rectangle> COLISIONES;
    private final ArrayList<Rectangle> ESCALERAS = new ArrayList<>();
    private final ArrayList<Polygon> PENDIENTES = new ArrayList<>();
    ;

    public Colisiones(TiledMap tiledMap) {
        COLISIONES = new ArrayList<>();
        cargarColisiones(tiledMap);
    }

    private void cargarColisiones(TiledMap tiledMap) {
        for (MapObject object : tiledMap.getLayers().get("colisiones").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                COLISIONES.add(rect);
            }
        }
        MapLayer capaPendientes = tiledMap.getLayers().get("pendiente");

        if (capaPendientes != null) {
            for (MapObject object : capaPendientes.getObjects()) {
                if (object instanceof PolygonMapObject) {
                    Polygon poligono = ((PolygonMapObject) object).getPolygon();
                    PENDIENTES.add(poligono);
                }
            }
        }

        MapLayer capaEscaleras = tiledMap.getLayers().get("escaleras");

        if (capaEscaleras != null) {
            for (MapObject object : capaEscaleras.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    ESCALERAS.add(rect);
                }
            }
        }
    }

    public boolean estaEnEscalera(Jugador jugador) {
        for (Rectangle escalera : ESCALERAS) {
            if (jugador.getBounds().overlaps(escalera)) {
                return true;
            }
        }

        return false;
    }

    public void chequearColision(Entidad entidad) {
        boolean estabaEnSuelo = entidad.estaEnSuelo();
        entidad.setEnSuelo(false);
        entidad.setChocoPared(false);
        boolean chocoPared = false;

        for (Rectangle suelo : COLISIONES) {
            if (!entidad.getBounds().overlaps(suelo)) {
                continue;
            }

            // Cayendo sobre el suelo
            if (entidad.getYAnterior() >= suelo.y + suelo.height && entidad.getVelocidadY() <= 0) {
                entidad.setY(suelo.y + suelo.height);
                entidad.setVelocidadY(0);
                entidad.setEnSuelo(true);
                continue;
            }

            // Saltando y golpeando el techo
            if (entidad.getYAnterior() + entidad.getAlto() <= suelo.y
                    && entidad.getVelocidadY() > 0) {

                if (entidad instanceof Jugador && ((Jugador) entidad).estaTrepando()) {
                    continue;
                }

                entidad.setY(suelo.y - entidad.getAlto());
                entidad.setVelocidadY(0);
            }
        }

        entidad.getBounds().setPosition(entidad.getX(), entidad.getY());

        for (Rectangle pared : COLISIONES) {
            if (!entidad.getBounds().overlaps(pared)) {
                continue;
            }

            // Viene desde izquierda
            if (entidad.getXAnterior() + entidad.getAncho() <= pared.x
                    && entidad.getX() + entidad.getAncho() > pared.x) {
                entidad.setX(pared.x - entidad.getAncho());
                chocoPared = true;
                continue;
            }

            // Viene desde derecha
            if (entidad.getXAnterior() >= pared.x + pared.width
                    && entidad.getX() < entidad.getXAnterior()) {
                entidad.setX(pared.x + pared.width);
                chocoPared = true;
            }
        }
        chequearPendiente(entidad);

        if (!entidad.estaEnSuelo()) {
            Rectangle pies =
                    new Rectangle(entidad.getX(), entidad.getY() - 2, entidad.getAncho(), 3);

            for (Rectangle suelo : COLISIONES) {
                if (pies.overlaps(suelo)) {
                    entidad.setEnSuelo(true);
                    if (entidad.getVelocidadY() <= 0) {
                        entidad.setY(suelo.y + suelo.height);
                        entidad.setVelocidadY(0);
                    }
                }
            }
        }

        if (chocoPared
                && entidad instanceof Enemigo
                && !(entidad instanceof EnemigoRango)
                && estabaEnSuelo) {
            entidad.saltar();
        }
        entidad.setChocoPared(chocoPared);
    }

    private void chequearPendiente(Entidad entidad) {
        float centroX = entidad.getX() + entidad.getAncho() / 2f;

        for (Polygon pendiente : PENDIENTES) {
            float[] puntos = pendiente.getTransformedVertices();

            float xInicio = puntos[0];
            float yInicio = puntos[1];

            float xFin = puntos[2];
            float yFin = puntos[3];

            if (xInicio > xFin) {
                float auxX = xInicio;
                float auxY = yInicio;

                xInicio = xFin;
                yInicio = yFin;

                xFin = auxX;
                yFin = auxY;
            }

            if (centroX < xInicio || centroX > xFin) {
                continue;
            }

            float progreso = (centroX - xInicio) / (xFin - xInicio);
            float ySuelo = yInicio + progreso * (yFin - yInicio);

            if (entidad.getVelocidadY() <= 0
                    && entidad.getY() <= ySuelo
                    && entidad.getY() + entidad.getAlto() >= ySuelo) {

                entidad.setY(ySuelo);
                entidad.setVelocidadY(0);
                entidad.setEnSuelo(true);
                return;
            }
        }
    }

    public ArrayList<Rectangle> getColisiones() {
        return COLISIONES;
    }

    public void dibujarHitboxes(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.BLUE);
        for (Rectangle colision : COLISIONES) {
            shapeRenderer.rect(colision.x, colision.y, colision.width, colision.height);
        }

        shapeRenderer.setColor(Color.PINK);
        for (Polygon pendiente : PENDIENTES) {
            float[] vertices = pendiente.getTransformedVertices();
            if (vertices.length >= 6) {
                shapeRenderer.polygon(vertices);
            } else if (vertices.length >= 4) {
                shapeRenderer.polyline(vertices);
            }
        }

        for (Rectangle escalera : ESCALERAS) {
            shapeRenderer.rect(escalera.x, escalera.y, escalera.width, escalera.height);
        }
    }
}
