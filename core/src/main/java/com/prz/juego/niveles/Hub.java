package com.prz.juego.niveles;

import com.badlogic.gdx.graphics.Camera;
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
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.sistemas.CuadroDialogo;
import com.prz.juego.utilidades.Debug;
import com.prz.juego.utilidades.Dialogo;
import com.prz.juego.utilidades.Entrada;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;

public class Hub {

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Jugador[] jugadores;
    private Colisiones colision;
    private ShapeRenderer shapeRenderer;
    private Rectangle centroHub;
    private BitmapFont fontAlerta;
    private Rectangle zonaPancrasio;
    private Dialogo dialogoPancrasio;
    private CuadroDialogo cuadroDialogo;

    public void cargar(String ruta, Jugador[] jugadores) {
        camara = new Camara();
        this.jugadores = jugadores;

        for(Jugador jugador : jugadores) {
            jugador.escalar(2f);
            jugador.setVelocidadX(Math.round(jugador.getVelocidadX() * 1.5f));
            jugador.setAccionesHabilitadas(false);
        }
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
        if (zonaPancrasio == null) return false;

        for (Jugador jugador : jugadores) {
            if (zonaPancrasio.overlaps(jugador.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private boolean estaCercaPancrasio(Jugador jugador) {
        return zonaPancrasio != null
            && zonaPancrasio.overlaps(jugador.getBounds());
    }

    public boolean ambosConfirmaronSeleccion()
    {
        int cont = 0;
        for(Jugador jugador : jugadores)
        {
            if(jugador.getEstadoRed().solicitaSeleccion())
            {
                cont++;
            }
        }
        return cont == jugadores.length;
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

        for (Jugador jugador : jugadores) {
            jugador.dibujar();
        }


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

        shapeRenderer.end();
    }

    public void update(float delta) {

        for (Jugador jugador : jugadores) {
            jugador.update(delta);
            colision.chequearColision(jugador);
        }

        camara.seguirPersonaje(
                jugadores[0].getX() + jugadores[0].getAncho() / 2f,
                jugadores[0].getY() + jugadores[0].getAlto() / 2f,
                delta);

        // La tecla E tambien abre la seleccion de nivel desde el centro del hub.
        // Solo consumirla aqui cuando haya una interaccion con Pancrasio.
        for (Jugador jugador : jugadores) {
            JugadorRed estadoRed = jugador.getEstadoRed();

            if (estadoRed != null
                //&& estadoRed.aprietaE()
                && (cuadroDialogo.estaVisible()
                || estaCercaPancrasio(jugador))) {

                if (cuadroDialogo.estaVisible()) {
                    dialogoPancrasio.avanzar();

                    if (dialogoPancrasio.estaActivo()) {
                        cuadroDialogo.mostrar(dialogoPancrasio.getMensajeActual());
                    } else {
                        cuadroDialogo.ocultar();
                    }
                } else {
                    dialogoPancrasio.iniciar();
                    cuadroDialogo.mostrar(dialogoPancrasio.getMensajeActual());
                }
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
        if (centroHub == null) return false;

        for (Jugador jugador : jugadores) {
            if (centroHub.overlaps(jugador.getBounds())) {
                return true;
            }
        }
        return false;
    }

    public boolean estaEnCentroHub(Jugador jugador) {
        return centroHub != null
            && centroHub.overlaps(jugador.getBounds());
    }

    public int contarJugadoresConfirmados() {
        int cont = 0;

        for (Jugador jugador : jugadores) {
            if (jugador.getEstadoRed().solicitaSeleccion()) {
                cont++;
            }
        }

        return cont;
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
