package com.prz.juego.niveles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.*;
import com.prz.juego.entidades.jugadores.*;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.sistemas.CuadroDialogo;
import com.prz.juego.utilidades.Debug;
import com.prz.juego.utilidades.Dialogo;
import com.prz.juego.utilidades.Entrada;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Render;

import java.awt.*;
import java.util.ArrayList;

public class Hub {

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Jugador jugador;
    private Colisiones colision;
    private ShapeRenderer shapeRenderer;
    private Rectangle centroHub;
    private BitmapFont fontAlerta;
    private Rectangle zonaPancrasio;
    private Dialogo dialogoPancrasio;
    private CuadroDialogo cuadroDialogo;

    private final ArrayList<Entidad> ENTIDADES = new ArrayList<>();

    public void cargar(String ruta, Jugador jugador) {
        camara = new Camara();
        this.jugador = jugador;
        this.jugador.escalar(2f);
        this.jugador.setVelocidadX(Math.round(this.jugador.getVelocidadX() * 1.5f));
        this.jugador.setAccionesHabilitadas(false);
        shapeRenderer = new ShapeRenderer();

        inicializarFuenteAlerta();

        TmxMapLoader mapLoader = new TmxMapLoader();
        mapa = mapLoader.load(ruta);

        if (mapa.getLayers().get("pancrasio") != null) {
            for (MapObject object : mapa.getLayers().get("pancrasio").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    zonaPancrasio = ((RectangleMapObject) object).getRectangle();
                    break;
                }
            }
        }

        dialogoPancrasio =
                new Dialogo(
                        "¡Bienvenidos, aventureros!",
                        "Pronto tendré mercancía para ustedes.",
                        "Vuelvan cuando esté todo preparado.");

        cuadroDialogo = new CuadroDialogo();

        for (MapObject object : mapa.getLayers().get("centroHub").getObjects()) {
            if (object instanceof RectangleMapObject) {
                centroHub = ((RectangleMapObject) object).getRectangle();
            }
        }

        colision = new Colisiones(mapa);

        MapProperties props = mapa.getProperties();

        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        int mapWidthTiles = props.get("width", Integer.class);
        int mapHeightTiles = props.get("height", Integer.class);

        float mapTotalWidth = tileWidth * mapWidthTiles;
        float mapTotalHeight = tileHeight * mapHeightTiles;

        camara.setLimitesMapa(mapTotalWidth, mapTotalHeight);

        mapRenderer = new OrthogonalTiledMapRenderer(mapa, 1f);
    }

    private boolean estaCercaPancrasio() {
        return zonaPancrasio != null && zonaPancrasio.overlaps(jugador.getBounds());
    }

    private void inicializarFuenteAlerta() {
        fontAlerta = FuenteJuego.generar(28);
    }

    public void renderMapa() {
        mapRenderer.setView(camara.getCamera());
        mapRenderer.render();
    }

    public void render() {
        Render.batch.setProjectionMatrix(camara.getCamera().combined);

        for (Entidad entidad : ENTIDADES) {
            entidad.dibujar();
        }

        jugador.dibujar();

        if (cuadroDialogo.estaVisible()) {
            cuadroDialogo.dibujar(Render.batch, camara.getCamera());
        } else if (estaCercaPancrasio()) {
            dibujarAlertaPancrasio();
        }
    }

    public void renderDebug() {
        if (!Debug.mostrarHitboxes()) {
            return;
        }

        shapeRenderer.setProjectionMatrix(camara.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        colision.dibujarHitboxes(shapeRenderer);

        for (Entidad entidad : ENTIDADES) {
            entidad.dibujarHitbox(shapeRenderer);
        }

        shapeRenderer.end();
    }

    public void update(float delta, Entrada entrada) {

        jugador.update(delta);
        colision.chequearColision(jugador);

        for (Entidad entidad : ENTIDADES) {
            entidad.update(delta);
            colision.chequearColision(entidad);
        }

        for (int i = ENTIDADES.size() - 1; i >= 0; i--) {
            Entidad entidad = ENTIDADES.get(i);

            if (entidad instanceof Enemigo) {
                Enemigo enemigo = (Enemigo) entidad;

                if (enemigo.estaMuerto()) {
                    enemigo.dispose();
                    ENTIDADES.remove(i);
                }
            }
        }
        camara.seguirPersonaje(
                jugador.getX() + jugador.getAncho() / 2f,
                jugador.getY() + jugador.getAlto() / 2f,
                delta);

        // La tecla E tambien abre la seleccion de nivel desde el centro del hub.
        // Solo consumirla aqui cuando haya una interaccion con Pancrasio.
        if ((cuadroDialogo.estaVisible() || estaCercaPancrasio()) && entrada.aprietaE()) {
            if (cuadroDialogo.estaVisible()) {
                dialogoPancrasio.avanzar();

                if (dialogoPancrasio.estaActivo()) {
                    cuadroDialogo.mostrar(dialogoPancrasio.getMensajeActual());
                } else {
                    cuadroDialogo.ocultar();
                }
            } else if (estaCercaPancrasio()) {
                dialogoPancrasio.iniciar();

                cuadroDialogo.mostrar(dialogoPancrasio.getMensajeActual());
            }
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

        fontAlerta.dispose();

        if (cuadroDialogo != null) {
            cuadroDialogo.dispose();
        }
    }

    public boolean estaEnCentroHub() {

        if (centroHub == null) {
            return false;
        }

        return centroHub.overlaps(jugador.getBounds());
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

    public void dibujarAlertaCentro() {
        if (!estaEnCentroHub()) {
            return;
        }

        String mensaje = "Presioná E para elegir nivel";

        GlyphLayout layout = new GlyphLayout(fontAlerta, mensaje);

        float x = centroHub.x + centroHub.width / 2f - layout.width / 2f;
        float y = centroHub.y + centroHub.height + 100f;

        fontAlerta.draw(Render.batch, mensaje, x, y);
    }

    private void dibujarAlertaPancrasio() {
        String mensaje = "Presioná E para hablar";

        GlyphLayout layout = new GlyphLayout(fontAlerta, mensaje);

        float ancho = getCamara().viewportWidth;
        float alto = getCamara().viewportHeight;

        float x = getCamara().position.x - ancho / 2f + (ancho - layout.width) / 2f;

        float y = getCamara().position.y - alto / 2f + 80;

        fontAlerta.draw(Render.batch, mensaje, x, y);
    }
}
