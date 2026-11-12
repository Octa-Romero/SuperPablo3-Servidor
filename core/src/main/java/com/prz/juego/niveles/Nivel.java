package com.prz.juego.niveles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.Reliquia;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.jefes.Retumbar;
import com.prz.juego.entidades.jugadores.*;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Debug;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;

public abstract class Nivel {

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Jugador[] jugadores;
    private Pablo pablo;
    private Walter walter;
    private Colisiones colision;
    private boolean enemigoReliquiaDerrotado = false;
    private final ArrayList<Entidad> ENTIDADES = new ArrayList<>();
    private final ArrayList<Entidad> ENTIDADES_PENDIENTES = new ArrayList<>();
    private final ArrayList<Entidad> ENTIDADES_PENDIENTES_ELIMINAR = new ArrayList<>();
    private final ArrayList<Lanzable> PROYECTILES_NUEVOS = new ArrayList<>();
    private final ArrayList<Lanzable> PROYECTILES_ACTIVOS = new ArrayList<>();
    private final ArrayList<Integer> PROYECTILES_ELIMINADOS = new ArrayList<>();
    private final ArrayList<Reliquia> RELIQUIAS = new ArrayList<>();
    private final ArrayList<Enemigo> enemigosNuevos = new ArrayList<>();
    private ArrayList<Integer> idEnemigosEliminados = new ArrayList<>();
    private ShapeRenderer shapeRenderer;
    private boolean gameOver = false;
    private boolean tieneReliquia = false;
    private float anchoMapa;
    protected Musica musica = Musica.NIVEL;
    private int idProyectil = 1;
    private int idEnemigo = 1;

    public static Nivel crear(String ruta) {
        switch (ruta) {
            case Nivel1.RUTA:
                return new Nivel1();
            case Nivel2.RUTA:
                return new Nivel2();
            case Nivel3.RUTA:
                return new Nivel3();
            case Nivel4.RUTA:
                return new Nivel4();
            case Nivel5.RUTA:
                return new Nivel5();
            case Nivel6.RUTA:
                return new Nivel6();
            case Nivel7.RUTA:
                return new Nivel7();
            default:
                throw new IllegalArgumentException(
                    "No existe una clase para el nivel: " + ruta
                );
        }
    }

    public void cargar(Jugador[] jugadores) {
        camara = new Camara();

        this.jugadores = jugadores;

        for (Jugador jugador : jugadores) {
            ENTIDADES.add(jugador);

            if (jugador instanceof Pablo p) {
                pablo = p;
            } else if (jugador instanceof Walter w) {
                walter = w;
            }
        }

        shapeRenderer = new ShapeRenderer();

        TmxMapLoader mapLoader = new TmxMapLoader();
        mapa = mapLoader.load(getRutaMapa());

        colision = new Colisiones(mapa);

        for (Jugador jugador : jugadores)
        {
            jugador.setColisiones(colision);
        }

        pablo.setEntidades(ENTIDADES);
        pablo.setProyectiles(PROYECTILES_ACTIVOS);

        walter.configurarAtaque(ENTIDADES, colision, PROYECTILES_ACTIVOS);

        configurarContenidoEspecifico(jugadores);

        MapProperties props = mapa.getProperties();

        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        int mapWidthTiles = props.get("width", Integer.class);
        int mapHeightTiles = props.get("height", Integer.class);

        float mapTotalWidth = tileWidth * mapWidthTiles;
        float mapTotalHeight = tileHeight * mapHeightTiles;

        anchoMapa = mapTotalWidth;

        camara.setLimitesMapa(mapTotalWidth, mapTotalHeight);

        mapRenderer = new OrthogonalTiledMapRenderer(mapa, 1f);

        musica.sonar();
    }

    protected abstract String getRutaMapa();

    protected abstract void configurarContenidoEspecifico(Jugador[] jugadores);

    protected float getPosicionInicialX() {
        return 50;
    }

    protected float getPosicionInicialY() {
        return 150;
    }

    protected void agregarEntidad(Entidad entidad) {
        if (entidad instanceof EnemigoRango) {
            ((EnemigoRango) entidad).configurarDisparo(
                colision, PROYECTILES_ACTIVOS
            );
        }

        ENTIDADES.add(entidad);

        if (entidad instanceof Enemigo enemigo) {
            enemigosNuevos.add(enemigo);
        }
    }

    protected void agregarEntidadDiferida(Entidad entidad) {
        if (entidad instanceof EnemigoRango) {
            ((EnemigoRango) entidad).configurarDisparo(colision, PROYECTILES_ACTIVOS);
        }

        if (entidad instanceof Enemigo enemigo) {
            enemigosNuevos.add(enemigo);
        }

        ENTIDADES_PENDIENTES.add(entidad);
    }

    protected void eliminarEntidadDiferida(Entidad entidad) {
        if (entidad instanceof EnemigoRango) {
            ((EnemigoRango) entidad).cancelarAtaques();
        }

        if (!ENTIDADES_PENDIENTES_ELIMINAR.contains(entidad)) {
            ENTIDADES_PENDIENTES_ELIMINAR.add(entidad);
        }
    }

    protected void agregarEntidadAlFondo(Entidad entidad) {
        if (entidad instanceof EnemigoRango) {
            ((EnemigoRango) entidad).configurarDisparo(colision, PROYECTILES_ACTIVOS);
        }

        if (entidad instanceof Enemigo enemigo) {
            enemigosNuevos.add(enemigo);
        }

        ENTIDADES.add(0, entidad);
    }

    protected void configurarEntornoEspecifico(float ancho, float alto) {}

    // Por el momento los niveles no tienen jefe, solo Nivel7 lo sobrescribe
    public Retumbar getJefe() {
        return null;
    }

    public void renderMapa() {
        mapRenderer.setView(camara.getCamera());
        mapRenderer.render();
    }

    public void render() {
        Render.batch.setProjectionMatrix(camara.getCamera().combined);

        boolean dibujarJugadoresAlFrente = getJefe() != null;

        for (Entidad entidad : ENTIDADES) {
            if (dibujarJugadoresAlFrente
                && (entidad == pablo || entidad == walter)) {
                continue;
            }

            entidad.dibujar();
        }

        if (dibujarJugadoresAlFrente) {
            getJefe().dibujarAtaquesExtra();
            pablo.dibujar();
            walter.dibujar();
        }

        for (Lanzable proyectil : PROYECTILES_ACTIVOS) {
            proyectil.dibujar();
        }

        for (Reliquia reliquia : RELIQUIAS) {
            reliquia.dibujar();
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

        if (getJefe() != null) {
            getJefe().dibujarHitboxesAtaquesExtra(shapeRenderer);
        }

        for (Lanzable proyectil : PROYECTILES_ACTIVOS) {
            proyectil.dibujarHitbox(shapeRenderer);
        }

        shapeRenderer.end();
    }

    public void update(float delta) {

        // Actualizar entidades
        for (Entidad entidad : ENTIDADES) {
            entidad.update(delta);
            if (entidad.debeVerificarColisionesMapa()) {
                colision.chequearColision(entidad);
            }
            entidad.limitarMovimientoHorizontal(0, anchoMapa);
        }
        for (Entidad entidad : ENTIDADES_PENDIENTES_ELIMINAR) {
            if (ENTIDADES.remove(entidad)) {

                if (entidad instanceof Enemigo enemigo) {
                    idEnemigosEliminados.add(enemigo.getId());
                }

                entidad.dispose();
            }

            ENTIDADES_PENDIENTES.remove(entidad);
        }
        ENTIDADES_PENDIENTES_ELIMINAR.clear();
        ENTIDADES.addAll(ENTIDADES_PENDIENTES);
        ENTIDADES_PENDIENTES.clear();

        for(Lanzable proyectil : PROYECTILES_ACTIVOS)
        {
            if(proyectil.getId() == 0) {
                proyectil.setId(idProyectil++);
                PROYECTILES_NUEVOS.add(proyectil);
            }
        }

        // Actualizar proyectiles.
        for (int i = PROYECTILES_ACTIVOS.size() - 1; i >= 0; i--) {
            Lanzable proyectil = PROYECTILES_ACTIVOS.get(i);

            proyectil.update(delta);

            if (!proyectil.estaActiva()) {
                PROYECTILES_ELIMINADOS.add(proyectil.getId());
                PROYECTILES_ACTIVOS.remove(i);
            }
        }

        // Eliminar enemigos muertos y generar la reliquia si corresponde.
        for (int i = ENTIDADES.size() - 1; i >= 0; i--) {
            Entidad entidad = ENTIDADES.get(i);

            if (entidad instanceof Enemigo) {
                Enemigo enemigo = (Enemigo) entidad;

                if (enemigo.estaMuerto()) {
                    idEnemigosEliminados.add(enemigo.getId());

                    if (enemigo.sueltaReliquia()) {
                        enemigoReliquiaDerrotado = true;
                        System.out.println(
                            "[RELIQUIA] ¡El enemigo marcado murió!"
                        );

                        float xReliquia = enemigo.getX()
                            + (enemigo.getAncho() - 32f) / 2f;

                        float yReliquia = enemigo.getY();

                        System.out.println(
                            "[RELIQUIA] Posición: X="
                                + xReliquia + ", Y=" + yReliquia
                        );

                        RELIQUIAS.add(
                            new Reliquia(xReliquia, yReliquia, colision)
                        );

                        System.out.println(
                            "[RELIQUIA] Reliquia creada. Total: "
                                + RELIQUIAS.size()
                        );
                    }

                    enemigo.dispose();
                    ENTIDADES.remove(i);
                }
            }
        }

        for (int i = RELIQUIAS.size() - 1; i >= 0; i--) {
            Reliquia reliquia = RELIQUIAS.get(i);

            for (Jugador jugador : jugadores) {
                if (reliquia.actualizar(jugador, delta)) {
                    tieneReliquia = true;
                    reliquia.dispose();
                    RELIQUIAS.remove(i);
                    break;
                }
            }
        }

        // Actualizar cámara.
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

        for (Entidad entidad : ENTIDADES) {
            entidad.dispose();
        }

        ENTIDADES.clear();
        ENTIDADES_PENDIENTES.clear();
        ENTIDADES_PENDIENTES_ELIMINAR.clear();
        PROYECTILES_ACTIVOS.clear();

        for (Reliquia reliquia : RELIQUIAS) {
            reliquia.dispose();
        }

        RELIQUIAS.clear();
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

    public boolean isGameOver() {
        return this.gameOver;
    }

    public ArrayList<Enemigo> getEnemigosNuevos() {
        ArrayList<Enemigo> nuevos = new ArrayList<>(enemigosNuevos);
        enemigosNuevos.clear();
        return nuevos;
    }

    public ArrayList<Enemigo> getEnemigosActivos()
    {
        ArrayList<Enemigo> enemigos = new ArrayList<>();
        for(Entidad e : ENTIDADES)
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

    public ArrayList<Lanzable> getProyectilesNuevos() {
        ArrayList<Lanzable> copia = new ArrayList<>(PROYECTILES_NUEVOS);
        PROYECTILES_NUEVOS.clear();
        return copia;
    }

    public ArrayList<Lanzable> getProyectilesActivos() {
        return new ArrayList<>(PROYECTILES_ACTIVOS);
    }

    public ArrayList<Integer> getProyectilesEliminados() {
        ArrayList<Integer> copia = new ArrayList<>(PROYECTILES_ELIMINADOS);
        PROYECTILES_ELIMINADOS.clear();
        return copia;
    }

    public int getIdEnemigo()
    {
        return this.idEnemigo++;
    }

    public boolean tieneReliquia() {
        return tieneReliquia;
    }

    public boolean enemigoReliquiaDerrotado(){return enemigoReliquiaDerrotado;}
}
