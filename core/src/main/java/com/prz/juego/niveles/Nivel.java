package com.prz.juego.niveles;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.prz.juego.entidades.*;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Debug;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;

public class Nivel {

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Jugador[] jugadores;
    private Pablo pablo;
    private Walter walter;
    private Colisiones colision;
    private final ArrayList<Entidad> entidades = new ArrayList<>();
    private final ArrayList<Enemigo> enemigosNuevos = new ArrayList<>();
    private ArrayList<Integer> idEnemigosEliminados = new ArrayList<>();
    private ShapeRenderer shapeRenderer;
    private boolean gameOver = false;
    private int idEnemigo = 1;

    public void cargar(String ruta, Jugador[] jugadores) {
        camara = new Camara();

        this.jugadores = jugadores;

        for (Jugador jugador : jugadores) {
            entidades.add(jugador);

            if (jugador instanceof Pablo p) {
                pablo = p;
            } else if (jugador instanceof Walter w) {
                walter = w;
            }
        }

        agregarEnemigo(new Bicho1(idEnemigo++, 2000, 300, jugadores[0]));
        agregarEnemigo(new Bicho1(idEnemigo++, 2200, 300, jugadores[0]));

        shapeRenderer = new ShapeRenderer();

        TmxMapLoader mapLoader = new TmxMapLoader();
        mapa = mapLoader.load(ruta);

        colision = new Colisiones(mapa);

        pablo.setEntidades(entidades);

        walter.configurarAtaque(entidades, colision);

        MapProperties props = mapa.getProperties();

        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        int mapWidthTiles = props.get("width", Integer.class);
        int mapHeightTiles = props.get("height", Integer.class);

        float mapTotalWidth = tileWidth * mapWidthTiles;
        float mapTotalHeight = tileHeight * mapHeightTiles;

        camara.setLimitesMapa(mapTotalWidth, mapTotalHeight);

        mapRenderer = new OrthogonalTiledMapRenderer(mapa, 1f);

        Musica.NIVEL1.sonar();
    }

    public void renderMapa() {
        mapRenderer.setView(camara.getCamera());
        mapRenderer.render();
    }

    public void render() {
        Render.batch.setProjectionMatrix(camara.getCamera().combined);

        for (Entidad entidad : entidades) {
            entidad.dibujar();

            if (entidad instanceof Enemigo) {
                ((Enemigo) entidad).dibujarAtaque();
            }
        }

        pablo.dibujarAtaque();
        walter.dibujarOrbes();
    }

    public void renderDebug() {
        if (!Debug.mostrarHitboxes()) {
            return;
        }

        shapeRenderer.setProjectionMatrix(camara.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        colision.dibujarHitboxes(shapeRenderer);

        for (Entidad entidad : entidades) {
            entidad.dibujarHitbox(shapeRenderer);
        }

        walter.dibujarOrbesHitbox(shapeRenderer);

        shapeRenderer.end();
    }

    public void update(float delta) {

        // Actualizar entidades
        for (Entidad entidad : entidades) {
            entidad.update(delta);
            colision.chequearColision(entidad);
        }

        // Eliminar enemigos muertos
        for (int i = entidades.size() - 1; i >= 0; i--) {
            Entidad entidad = entidades.get(i);

            if (entidad instanceof Enemigo) {
                Enemigo enemigo = (Enemigo) entidad;

                if (enemigo.estaMuerto()) {
                    idEnemigosEliminados.add(enemigo.getId());

                    enemigo.dispose();
                    entidades.remove(i);
                }
            }
        }

        // Cámara
        camara.seguirPersonaje(
            jugadores[0].getX() + jugadores[0].getAncho() / 2f,
            jugadores[0].getY() + jugadores[0].getAlto() / 2f,
            delta
        );

        // Alternar hitboxes
        //if (entrada.aprietaF1()) {
        //    Debug.cambiarHitboxes();
        //}

        if (jugadores[0].estaMuerto() && jugadores[1].estaMuerto()) {
            gameOver = true;
        }

    }

    public void resize(int width, int height) {
        camara.actualizarTamano(width, height);
    }

    public void dispose() {
        if (mapa != null) {
            mapa.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        for(Entidad e : entidades)
        {
            e.dispose();
        }
        entidades.clear();
    }

    public Camera getCamara() {
        return camara.getCamera();
    }

    public void guardarPosicionCamara() {
        camara.guardarPosicion();
    }

    public void restaurarPosicionCamara() {
        camara.restaurarPosicion();
    }

    public boolean isGameOver(){return this.gameOver;}

    private void agregarEnemigo(Enemigo enemigo)
    {
        entidades.add(enemigo);
        enemigosNuevos.add(enemigo);
    }

    public ArrayList<Enemigo> getEnemigosNuevos() {
        ArrayList<Enemigo> nuevos = new ArrayList<>(enemigosNuevos);
        enemigosNuevos.clear();
        return nuevos;
    }

    public ArrayList<Enemigo> getEnemigosActivos()
    {
        ArrayList<Enemigo> enemigos = new ArrayList<>();
        for(Entidad e : entidades)
        {
            if(e instanceof Enemigo)
            {
                enemigos.add((Enemigo) e);
            }
        }
        return enemigos;
    }

    public ArrayList<Integer> getEnemigosEliminados() {
        ArrayList<Integer> copia = new ArrayList<>(idEnemigosEliminados);
        idEnemigosEliminados.clear();
        return copia;
    }

    public ArrayList<OrbeMagica> getOrbesNuevos() {
        return walter.getOrbesNuevos();
    }

    public ArrayList<OrbeMagica> getOrbesActivos() {
        return walter.getOrbesActivos();
    }

    public ArrayList<Integer> getOrbesEliminados() {
        return walter.getOrbesEliminados();
    }
}
