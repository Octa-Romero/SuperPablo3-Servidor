package com.prz.juego.niveles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.red.JugadorRed;
import com.prz.juego.red.Servidor;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

public class Hub {

    private static final float ESCALA_JUGADOR = 2f;
    private static final float MULTIPLICADOR_VELOCIDAD = 1.5f;
    private static final String CAPA_CENTRO_HUB = "centroHub";

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;
    private Colisiones colision;
    private Jugador[] jugadores;
    private Rectangle centroHub;
    private float mapaAncho;

    public void cargar(String ruta, Jugador[] jugadores) {
        this.jugadores = jugadores;
        camara = new Camara();
        shapeRenderer = new ShapeRenderer();

        for (Jugador jugador : jugadores) {
            jugador.escalar(ESCALA_JUGADOR);
            jugador.setVelocidadX(Math.round(jugador.getVelocidadX() * MULTIPLICADOR_VELOCIDAD));
            jugador.setAccionesHabilitadas(false);
        }

        mapa = new TmxMapLoader().load(ruta);
        centroHub = buscarRectangulo(CAPA_CENTRO_HUB);
        colision = new Colisiones(mapa);

        Servidor.instancia.enviarMusica("HUB");

        MapProperties props = mapa.getProperties();
        mapaAncho = props.get("tilewidth", Integer.class) * props.get("width", Integer.class);
        float mapaAlto = props.get("tileheight", Integer.class) * props.get("height", Integer.class);

        camara.setLimitesMapa(mapaAncho, mapaAlto);
        mapRenderer = new OrthogonalTiledMapRenderer(mapa, 1f);
    }

    private Rectangle buscarRectangulo(String nombreCapa) {
        MapLayer capa = mapa.getLayers().get(nombreCapa);
        if (capa == null) {
            return null;
        }

        Rectangle resultado = null;
        for (MapObject objeto : capa.getObjects()) {
            if (objeto instanceof RectangleMapObject) {
                resultado = ((RectangleMapObject) objeto).getRectangle();
            }
        }
        return resultado;
    }

    public void update(float delta) {
        for (Jugador jugador : jugadores) {
            jugador.update(delta);
            colision.chequearColision(jugador);
            restringirBordes(jugador);
        }

        Jugador seguido = jugadores[0];
        camara.seguirPersonaje(
            seguido.getX() + seguido.getAncho() / 2f,
            seguido.getY() + seguido.getAlto() / 2f,
            delta);
    }

    private void restringirBordes(Jugador jugador) {
        float x = MathUtils.clamp(jugador.getX(), 0f, mapaAncho - jugador.getAncho());
        if (x != jugador.getX()) {
            jugador.setX(x);
        }
    }

    public void renderMapa() {
        mapRenderer.setView(camara.getCamera());
        mapRenderer.render();
    }

    public void render() {
        Render.batch.setProjectionMatrix(camara.getCamera().combined);

        for (Jugador jugador : jugadores) {
            jugador.dibujar();
        }
    }

    private boolean confirmoSeleccion(Jugador jugador) {
        JugadorRed estadoRed = jugador.getEstadoRed();
        return estadoRed != null && estadoRed.solicitaSeleccion();
    }

    public int contarJugadoresConfirmados() {
        int cont = 0;
        for (Jugador jugador : jugadores) {
            if (confirmoSeleccion(jugador)) {
                cont++;
            }
        }
        return cont;
    }

    public boolean ambosConfirmaronSeleccion() {
        return contarJugadoresConfirmados() == jugadores.length;
    }

    public boolean estaEnCentroHub(Jugador jugador) {
        return centroHub != null && centroHub.overlaps(jugador.getBounds());
    }

    public boolean estaEnCentroHub() {
        for (Jugador jugador : jugadores) {
            if (estaEnCentroHub(jugador)) {
                return true;
            }
        }
        return false;
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
    }
}
