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
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.Reliquia;
import com.prz.juego.entidades.Tumba;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.retumbar.Retumbar;
import com.prz.juego.entidades.jugadores.*;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.JugadorRed;
import com.prz.juego.red.Servidor;
import com.prz.juego.sistemas.Camara;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Nivel {

    private static final float INTERVALO_ENVIO_TUMBA = 0.5f;
    private static final float TAMANO_RELIQUIA = 48f;
    private static final float ANCHO_PORTAL = 120f;
    private static final float ALTO_PORTAL = 160f;
    private static final float INTERVALO_REENVIO_PORTAL = 1f;
    private static final int CANTIDAD_JUGADORES = 2;
    private static final String CAPA_AVANCE = "avance";

    private final boolean[] VOTOS_PORTAL = new boolean[CANTIDAD_JUGADORES];

    private Camara camara;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Jugador[] jugadores;
    private Pablo pablo;
    private Walter walter;
    private Colisiones colision;
    private boolean jefeMuerto = false;
    private float tiempoDesdeMuerteJefe = 0f;
    private boolean creditosDisparados = false;
    private final ArrayList<Entidad> ENTIDADES = new ArrayList<>();
    private final ArrayList<Entidad> ENTIDADES_PENDIENTES = new ArrayList<>();
    private final ArrayList<Entidad> ENTIDADES_PENDIENTES_ELIMINAR = new ArrayList<>();
    private final ArrayList<Lanzable> PROYECTILES_NUEVOS = new ArrayList<>();
    private final ArrayList<Lanzable> PROYECTILES_ACTIVOS = new ArrayList<>();
    private final ArrayList<Integer> PROYECTILES_ELIMINADOS = new ArrayList<>();
    private final ArrayList<Reliquia> RELIQUIAS = new ArrayList<>();
    private final ArrayList<Enemigo> ENEMIGOS_NUEVOS = new ArrayList<>();
    private ArrayList<Integer> idEnemigosEliminados = new ArrayList<>();
    private Tumba[] tumbas;
    private float[] tiemposEnvioTumba;
    private ShapeRenderer shapeRenderer;
    private boolean gameOver = false;
    private boolean reliquiaMinijefeProcesada = false;
    private Reliquia reliquiaMinijefe;
    private int numeroNivel = 0;
    private Rectangle zonaPortal;
    private boolean portalEsAvance = false;
    private boolean volverAlHubConfirmado = false;
    private boolean avanceConfirmado = false;
    private float tiempoReenvioPortal = 0f;
    private int desconexionesVistas = 0;
    private float anchoMapa;
    protected String musica;
    private int idProyectil = 1;
    private int idEnemigo = 1;
    private static final String TEXTURA_PORTAL = "Menu/Portales/portalHub.png";

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
        this.numeroNivel = Servidor.instancia.getNumeroNivelActual();
        desconexionesVistas = Servidor.instancia.getContadorDesconexiones();
        tumbas = new Tumba[jugadores.length];
        tiemposEnvioTumba = new float[jugadores.length];

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

        zonaPortal = buscarRectangulo(CAPA_AVANCE);
        portalEsAvance = zonaPortal != null;

        for (Jugador jugador : jugadores) {
            jugador.setColisiones(colision);
            if (jugador.getEstadoRed() != null) {
                jugador.getEstadoRed().descartarAccionesPendientes();
            }
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
        configurarEntornoEspecifico(mapTotalWidth, mapTotalHeight);

        mapRenderer = new OrthogonalTiledMapRenderer(mapa, 1f);

        if (musica != null) {
            Servidor.instancia.enviarMusica(musica);
        }
    }

    private Rectangle buscarRectangulo(String nombreCapa) {
        MapLayer capa = mapa.getLayers().get(nombreCapa);
        if (capa == null) {
            return null;
        }

        for (MapObject objeto : capa.getObjects()) {
            if (objeto instanceof RectangleMapObject rectangulo) {
                return rectangulo.getRectangle();
            }
        }
        return null;
    }

    protected abstract String getRutaMapa();

    protected abstract void configurarContenidoEspecifico(Jugador[] jugadores);

    public float getPosicionInicialX() {
        return 50;
    }

    public float getPosicionInicialY() {
        return 150;
    }

    public String getMusica() {
        return musica;
    }

    private void registrarEnemigo(Entidad entidad) {
        if (entidad instanceof Enemigo enemigo
            && enemigo.getTipo() != TipoEnemigo.JEFE) {

            enemigo.setId(idEnemigo++);
            ENEMIGOS_NUEVOS.add(enemigo);
        }
    }

    protected void agregarEntidad(Entidad entidad) {
        if (entidad instanceof EnemigoRango rango) {
            rango.configurarDisparo(colision, PROYECTILES_ACTIVOS);
        }

        ENTIDADES.add(entidad);
        registrarEnemigo(entidad);
    }

    protected void agregarEntidadDiferida(Entidad entidad) {
        if (entidad instanceof EnemigoRango rango) {
            rango.configurarDisparo(colision, PROYECTILES_ACTIVOS);
        }

        registrarEnemigo(entidad);
        ENTIDADES_PENDIENTES.add(entidad);
    }

    protected void agregarEntidadAlFondo(Entidad entidad) {
        if (entidad instanceof EnemigoRango rango) {
            rango.configurarDisparo(colision, PROYECTILES_ACTIVOS);
        }

        ENTIDADES.add(0, entidad);
        registrarEnemigo(entidad);
    }

    protected void eliminarEntidadDiferida(Entidad entidad) {
        if (entidad instanceof EnemigoRango) {
            ((EnemigoRango) entidad).cancelarAtaques();
        }

        if (!ENTIDADES_PENDIENTES_ELIMINAR.contains(entidad)) {
            ENTIDADES_PENDIENTES_ELIMINAR.add(entidad);
        }
    }

    protected void configurarEntornoEspecifico(float ancho, float alto) {}

    public Retumbar getJefe()
    {
        return null;
    }

    public void renderMapa() {
        mapRenderer.setView(camara.getCamera());
        mapRenderer.render();
    }

    public void render() {
        Render.batch.setProjectionMatrix(camara.getCamera().combined);

        dibujarPortal();

        for (Entidad entidad : ENTIDADES) {
            if (entidad == pablo || entidad == walter) {
                continue;
            }
            entidad.dibujar();
        }

        for (Jugador jugador : jugadores) {
            if (jugador != null) {
                jugador.dibujar();
            }
        }

        for (Lanzable proyectil : PROYECTILES_ACTIVOS) {
            proyectil.dibujar();
        }

        for (Reliquia reliquia : RELIQUIAS) {
            reliquia.dibujar();
        }

        Retumbar jefe = getJefe();
        if (jefe != null) {
            jefe.dibujarAtaquesExtra();
        }
    }

    public void update(float delta) {

        for (Entidad entidad : ENTIDADES) {
            entidad.update(delta);
            if (entidad.debeVerificarColisionesMapa()) {
                colision.chequearColision(entidad);
            }
            entidad.limitarMovimientoHorizontal(0, anchoMapa);
        }

        programarEliminacionEnemigosMuertos();
        procesarEliminaciones();

        ENTIDADES.addAll(ENTIDADES_PENDIENTES);
        ENTIDADES_PENDIENTES.clear();

        for(Lanzable proyectil : PROYECTILES_ACTIVOS)
        {
            if(proyectil.getId() == 0) {
                proyectil.setId(idProyectil++);
                PROYECTILES_NUEVOS.add(proyectil);
            }
        }

        for (int i = PROYECTILES_ACTIVOS.size() - 1; i >= 0; i--) {
            Lanzable proyectil = PROYECTILES_ACTIVOS.get(i);

            proyectil.update(delta);

            if (!proyectil.estaActiva()) {
                PROYECTILES_ELIMINADOS.add(proyectil.getId());
                PROYECTILES_ACTIVOS.remove(i);
            }
        }

        for (int i = ENTIDADES.size() - 1; i >= 0; i--) {
            Entidad entidad = ENTIDADES.get(i);

            if (entidad instanceof Enemigo enemigo) {

                if (enemigo instanceof Retumbar jefe
                    && jefe.estaMuerto()
                    && !jefeMuerto) {

                    jefeMuerto = true;
                    tiempoDesdeMuerteJefe = 0f;

                    Servidor.instancia.enviarEliminacionAtaquesJefe();

                    ENTIDADES.remove(jefe.getManoIzquierda());
                    ENTIDADES.remove(jefe.getManoDerecha());
                }
            }
        }

        if (jefeMuerto && !creditosDisparados) {
            tiempoDesdeMuerteJefe += delta;
            if (tiempoDesdeMuerteJefe >= 2f) {
                boolean algunJugadorVivo = !jugadores[0].estaMuerto() || !jugadores[1].estaMuerto();
                if (algunJugadorVivo) {
                    creditosDisparados = true;
                    Servidor.instancia.enviarCreditos();
                }
            }
        }

        for (int i = RELIQUIAS.size() - 1; i >= 0; i--) {
            Reliquia reliquia = RELIQUIAS.get(i);

            for (Jugador jugador : jugadores) {
                if (reliquia.actualizar(jugador, delta)) {
                    if (reliquia == reliquiaMinijefe) {
                        reliquiaMinijefe = null;
                        Servidor.instancia.marcarReliquiaRecogida(numeroNivel);
                        abrirPortalHub(reliquia.getX() + TAMANO_RELIQUIA / 2f, reliquia.getY());
                    }

                    Servidor.instancia.enviarEliminacionReliquia();

                    reliquia.dispose();
                    RELIQUIAS.remove(i);
                    break;
                }
            }

            if (!reliquia.estaRecogida()) {
                Servidor.instancia.enviarPosicionReliquia(reliquia.getX(), reliquia.getY());
            }
        }

        actualizarPortal(delta);

        Jugador jugadorParaCamara;
        if (jugadores[0].estaMuerto() && !jugadores[1].estaMuerto()) {
            jugadorParaCamara = jugadores[1];
        } else if (jugadores[1].estaMuerto() && !jugadores[0].estaMuerto()) {
            jugadorParaCamara = jugadores[0];
        } else {
            jugadorParaCamara = jugadores[0].getX() >= jugadores[1].getX()
                ? jugadores[0] : jugadores[1];
        }

        camara.seguirPersonaje(
            jugadorParaCamara.getX() + jugadorParaCamara.getAncho() / 2f,
            jugadorParaCamara.getY() + jugadorParaCamara.getAlto() / 2f,
            delta
        );

        actualizarTumbas(delta);

        if (jugadores[0].estaMuerto() && jugadores[1].estaMuerto()) {
            gameOver = true;
        }

    }

    private void programarEliminacionEnemigosMuertos() {
        for (Entidad entidad : ENTIDADES) {
            if (entidad instanceof Enemigo enemigo
                && enemigo.getTipo() != TipoEnemigo.JEFE
                && enemigo.estaMuerto()) {
                soltarReliquiaMinijefe(enemigo);
                eliminarEntidadDiferida(enemigo);
            }
        }
    }

    private void soltarReliquiaMinijefe(Enemigo enemigo) {
        if (reliquiaMinijefeProcesada || !enemigo.debeSoltarReliquia()) {
            return;
        }

        reliquiaMinijefeProcesada = true;

        float centroX = enemigo.getX() + enemigo.getAncho() / 2f;
        float baseY = enemigo.getY();
        float alturaAparicion = enemigo.getAlto() / 2f;

        if (enemigo.getY() + enemigo.getAlto() < 0f) {
            Jugador referencia = jugadores[0].estaMuerto() ? jugadores[1] : jugadores[0];
            centroX = referencia.getX() + referencia.getAncho() / 2f;
            baseY = referencia.getY();
            alturaAparicion = referencia.getAlto();
        }

        if (!Servidor.instancia.puedeSoltarReliquia(numeroNivel)) {
            abrirPortalHub(centroX, baseY);
            return;
        }

        Reliquia reliquia =
            new Reliquia(centroX - TAMANO_RELIQUIA / 2f, baseY + alturaAparicion, colision);
        reliquiaMinijefe = reliquia;
        RELIQUIAS.add(reliquia);
        Servidor.instancia.enviarCreacionReliquia(reliquia.getX(), reliquia.getY());
    }

    private void abrirPortalHub(float centroX, float baseY) {
        float x = MathUtils.clamp(centroX - ANCHO_PORTAL / 2f, 0f, Math.max(0f, anchoMapa - ANCHO_PORTAL));

        zonaPortal = new Rectangle(x, baseY, ANCHO_PORTAL, ALTO_PORTAL);
        portalEsAvance = false;
        tiempoReenvioPortal = 0f;
        Arrays.fill(VOTOS_PORTAL, false);

        for (Jugador jugador : jugadores) {
            if (jugador.getEstadoRed() != null) {
                jugador.getEstadoRed().descartarAccionesPendientes();
            }
        }

        Servidor.instancia.enviarPortal(zonaPortal.x, zonaPortal.y, zonaPortal.width, zonaPortal.height);
    }

    private void actualizarPortal(float delta) {
        int desconexionesActuales = Servidor.instancia.getContadorDesconexiones();
        if (desconexionesActuales != desconexionesVistas) {
            desconexionesVistas = desconexionesActuales;
            Arrays.fill(VOTOS_PORTAL, false);
        }

        for (int i = 0; i < jugadores.length; i++) {
            JugadorRed estadoRed = jugadores[i].getEstadoRed();
            if (estadoRed == null) {
                continue;
            }

            boolean presiono = estadoRed.interactua();
            if (zonaPortal == null) {
                continue;
            }

            if (presiono) {
                if (VOTOS_PORTAL[i]) {
                    VOTOS_PORTAL[i] = false;
                } else if (!jugadores[i].estaMuerto() && zonaPortal.overlaps(jugadores[i].getBounds())) {
                    VOTOS_PORTAL[i] = true;
                }
            }

            Servidor.instancia.enviarVotoPortal(estadoRed, VOTOS_PORTAL[i]);
        }

        if (zonaPortal == null) {
            return;
        }

        if (!portalEsAvance) {
            tiempoReenvioPortal += delta;
            if (tiempoReenvioPortal >= INTERVALO_REENVIO_PORTAL) {
                tiempoReenvioPortal = 0f;
                Servidor.instancia.enviarPortal(
                    zonaPortal.x, zonaPortal.y, zonaPortal.width, zonaPortal.height);
            }
        }

        confirmarPortalSiCorresponde();
    }

    private void confirmarPortalSiCorresponde() {
        boolean algunVoto = false;
        boolean todosConfirman = true;

        for (int i = 0; i < jugadores.length; i++) {
            algunVoto |= VOTOS_PORTAL[i];
            todosConfirman &= VOTOS_PORTAL[i] || jugadores[i].estaMuerto();
        }

        if (!algunVoto || !todosConfirman) {
            return;
        }

        Arrays.fill(VOTOS_PORTAL, false);

        if (portalEsAvance) {
            avanceConfirmado = true;
        } else {
            volverAlHubConfirmado = true;
        }
    }

    public boolean consumirVolverAlHub() {
        boolean confirmado = volverAlHubConfirmado;
        volverAlHubConfirmado = false;
        return confirmado;
    }

    public boolean consumirAvance() {
        boolean confirmado = avanceConfirmado;
        avanceConfirmado = false;
        return confirmado;
    }

    private void procesarEliminaciones() {
        for (Entidad entidad : ENTIDADES_PENDIENTES_ELIMINAR) {
            boolean estabaActiva = ENTIDADES.remove(entidad);
            boolean estabaPendiente = ENTIDADES_PENDIENTES.remove(entidad);

            if (!estabaActiva && !estabaPendiente) {
                continue;
            }

            if (entidad instanceof Enemigo enemigo && enemigo.getId() != 0) {
                idEnemigosEliminados.add(enemigo.getId());
            }

            entidad.dispose();
        }

        ENTIDADES_PENDIENTES_ELIMINAR.clear();
    }

    private void actualizarTumbas(float delta) {
        for (int i = 0; i < jugadores.length; i++) {
            Jugador jugador = jugadores[i];

            if (!jugador.estaMuerto()) {
                continue;
            }

            if (tumbas[i] == null) {
                tumbas[i] = new Tumba(
                    jugador.getX() + jugador.getAncho() / 2f,
                    jugador.getY(),
                    colision
                );
                tiemposEnvioTumba[i] = 0f;
                Servidor.instancia.enviarSonido("MUERTE");
            }

            Tumba tumba = tumbas[i];
            boolean estabaEnSuelo = tumba.estaEnSuelo();

            tumba.actualizar(delta);

            boolean enviar;

            if (!tumba.estaEnSuelo() || !estabaEnSuelo) {
                enviar = true;
            } else {
                tiemposEnvioTumba[i] += delta;
                enviar = tiemposEnvioTumba[i] >= INTERVALO_ENVIO_TUMBA;
            }

            if (enviar) {
                tiemposEnvioTumba[i] = 0f;
                Servidor.instancia.enviarEstadoTumba(
                    jugador.getPersonaje().name(),
                    tumba.getCentroX(),
                    tumba.getY()
                );
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
        for (Entidad entidad : ENTIDADES) {
            if (entidad != pablo && entidad != walter) {
                entidad.dispose();
            }
        }

        ENTIDADES.clear();
        ENTIDADES_PENDIENTES.clear();
        ENTIDADES_PENDIENTES_ELIMINAR.clear();
        PROYECTILES_ACTIVOS.clear();

        for (Reliquia reliquia : RELIQUIAS) {
            reliquia.dispose();
        }

        RELIQUIAS.clear();
        reliquiaMinijefe = null;
    }

    public Camera getCamara() {
        return camara.getCamera();
    }

    public ArrayList<Enemigo> getEnemigosNuevos() {
        ArrayList<Enemigo> nuevos = new ArrayList<>(ENEMIGOS_NUEVOS);
        ENEMIGOS_NUEVOS.clear();
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

    public ArrayList<Reliquia> getReliquiasActivas() {
        return new ArrayList<>(RELIQUIAS);
    }

    public Rectangle getPortalHub() {
        return portalEsAvance ? null : zonaPortal;
    }

    public boolean creditosDisparados() {
        return creditosDisparados;
    }

    private void dibujarPortal() {
        if (zonaPortal == null || portalEsAvance) {
            return;
        }

        Render.batch.draw(
            GestorRecursos.obtenerTextura(TEXTURA_PORTAL),
            zonaPortal.x,
            zonaPortal.y,
            zonaPortal.width,
            zonaPortal.height);
    }
}
