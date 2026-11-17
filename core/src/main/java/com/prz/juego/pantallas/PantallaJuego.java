package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.enemigos.rango.Proyectil;
import com.prz.juego.entidades.retumbar.AtaquesExtraRetumbar;
import com.prz.juego.entidades.retumbar.FuegoSuelo;
import com.prz.juego.entidades.retumbar.Fueguito;
import com.prz.juego.entidades.retumbar.Mano;
import com.prz.juego.entidades.retumbar.Meteorito;
import com.prz.juego.entidades.retumbar.MeteoritoGrande;
import com.prz.juego.entidades.retumbar.Retumbar;
import com.prz.juego.entidades.jugadores.BolaFuego;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.entidades.jugadores.Personajes;
import com.prz.juego.red.JugadorRed;
import com.prz.juego.niveles.Hub;
import com.prz.juego.niveles.Nivel;
import com.prz.juego.principal.Navegable;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Global;
import com.prz.juego.utilidades.Render;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PantallaJuego implements Screen {

    private static final String RUTA_HUB = "Niveles/Niveles/Hub.tmx";
    private static final int CANTIDAD_JUGADORES = 2;
    private static final int SPAWN_X_DEFECTO = 50;
    private static final int SPAWN_Y_DEFECTO = 150;
    private static final float DURACION_CREDITOS = 74f;

    private final Navegable NAV;
    private final Servidor SERVIDOR;
    private final Jugador[] JUGADORES;
    private final Queue<DestinoSnapshot> SNAPSHOTS_PENDIENTES = new ConcurrentLinkedQueue<>();

    private record DestinoSnapshot(InetAddress ip, int puerto) {
    }

    private Nivel nivel;
    private Hub hub;
    private boolean seleccionNivelActiva = false;
    private boolean nivelIniciado = false;
    private boolean creditosIniciados = false;
    private float tiempoCreditos = 0f;
    private boolean jefeEliminado = false;
    private boolean jefeCreado = false;
    private volatile boolean debeVolverAlHub = false;

    public PantallaJuego(Navegable nav, Servidor servidor) {
        this(nav, servidor, null);
    }

    public PantallaJuego(Navegable nav, Servidor servidor, String rutaNivel) {
        NAV = nav;
        SERVIDOR = servidor;
        nivel = rutaNivel == null ? null : Nivel.crear(rutaNivel);

        JugadorRed[] estadosRed = SERVIDOR.getJugadoresRed();
        JUGADORES = new Jugador[estadosRed.length];

        int spawnX = nivel != null ? (int) nivel.getPosicionInicialX() : SPAWN_X_DEFECTO;
        int spawnY = nivel != null ? (int) nivel.getPosicionInicialY() : SPAWN_Y_DEFECTO;

        for (int i = 0; i < JUGADORES.length; i++) {
            Personajes personaje = Personajes.valueOf(estadosRed[i].getPersonaje());
            JUGADORES[i] = personaje.crear(spawnX, spawnY);
            JUGADORES[i].setEstadoRed(estadosRed[i]);
        }

        if (nivel == null) {
            hub = new Hub();
            hub.cargar(RUTA_HUB, JUGADORES);
        } else {
            nivel.cargar(JUGADORES);
        }

        SERVIDOR.registrarCallbackVolverHub(this::marcarVolverAlHub);
        SERVIDOR.registrarCallbackSnapshotEntidades(this::encolarSnapshot);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (SERVIDOR.getCantClientes() == 0) {
            Global.empieza = false;
            NAV.cambiarPantalla(new PantallaMenu(NAV, SERVIDOR));
            return;
        }

        procesarSnapshotsPendientes();

        if (SERVIDOR.isJuegoPausado()) {
            Render.limpiarPantalla();
            dibujar();
            return;
        }

        if (creditosIniciados) {
            actualizarCreditos(delta);
            return;
        }

        if (debeVolverAlHub) {
            debeVolverAlHub = false;
            cargarHub();
        }

        Render.limpiarPantalla();

        if (hub != null) {
            actualizarHub(delta);
        } else {
            nivel.update(delta);
            enviarEstadoNivel();

            if (nivel.creditosDisparados()) {
                iniciarCreditos();
            }
        }

        dibujar();
    }

    private boolean hayJugadorEnSeleccion() {
        for (JugadorRed jugador : SERVIDOR.getJugadoresRed()) {
            if (jugador != null && JugadorRed.ESTADO_SELECCION_NIVEL.equals(jugador.getEstadoActual())) {
                return true;
            }
        }
        return false;
    }

    private void actualizarHub(float delta) {
        if (seleccionNivelActiva) {
            if (!nivelIniciado && !hayJugadorEnSeleccion()) {
                seleccionNivelActiva = false;
            } else {
                actualizarSeleccionNivel();
                return;
            }
        }

        hub.update(delta);

        for (Jugador jugador : JUGADORES) {
            SERVIDOR.enviarPosicion(jugador);
            SERVIDOR.enviarEstadoAtaque(jugador);
            SERVIDOR.enviarEstadoCentroHub(jugador.getEstadoRed(), hub.estaEnCentroHub(jugador));
            SERVIDOR.enviarEstadoSeleccion(jugador.getEstadoRed());
        }

        if (hub.contarJugadoresConfirmados() == CANTIDAD_JUGADORES) {
            seleccionNivelActiva = true;
            SERVIDOR.enviarInicioSeleccionNivel();
        }
    }

    private void actualizarSeleccionNivel() {
        JugadorRed[] estados = SERVIDOR.getJugadoresRed();

        if (nivelIniciado || estados[0] == null || estados[1] == null) {
            return;
        }

        int voto1 = estados[0].getNivelSeleccionado();
        int voto2 = estados[1].getNivelSeleccionado();

        SERVIDOR.enviarVotosNivel(estados[0], voto1, voto2);
        SERVIDOR.enviarVotosNivel(estados[1], voto2, voto1);

        if (voto1 == 0 || voto1 != voto2) {
            return;
        }

        nivelIniciado = true;

        for (JugadorRed jugador : estados) {
            jugador.setSolicitoSeleccionNivel(false);
            jugador.setNivelSeleccionado(0);
        }

        SERVIDOR.enviarInicioNivel(voto1);

        String rutaNivel = SERVIDOR.getNivelActual();
        if (rutaNivel != null) {
            NAV.iniciarJuego(rutaNivel);
        }
    }

    private void iniciarCreditos() {
        creditosIniciados = true;
        tiempoCreditos = 0f;
        detenerSonidosLoop();
    }

    private void actualizarCreditos(float delta) {
        tiempoCreditos += delta;

        if (tiempoCreditos >= DURACION_CREDITOS) {
            NAV.cambiarPantalla(new PantallaMenu(NAV, SERVIDOR));
            return;
        }

        Render.limpiarPantalla();
        dibujar();
    }

    private void dibujar() {
        if (hub != null) {
            hub.renderMapa();
            Render.begin(hub.getCamara());
            hub.render();
            Render.end();
        } else {
            nivel.renderMapa();
            Render.begin(nivel.getCamara());
            nivel.render();
            Render.end();
        }
    }

    private void enviarEstadoNivel() {
        enviarJugadores();
        enviarEnemigos();
        enviarProyectiles();

        Retumbar jefe = nivel.getJefe();
        if (jefe != null) {
            enviarJefe(jefe);
        }
    }

    private void enviarJugadores() {
        for (Jugador jugador : JUGADORES) {
            SERVIDOR.enviarPosicion(jugador);
            SERVIDOR.enviarEstadoAtaque(jugador);
            SERVIDOR.enviarEstadoVida(jugador);
            SERVIDOR.enviarEstadoInvencibilidad(jugador);
        }
    }

    private void enviarEnemigos() {
        for (Enemigo e : nivel.getEnemigosNuevos()) {
            SERVIDOR.enviarCreacionEnemigos(e.getId(), e.getTipo(), e.getX(), e.getY(), e.getVida());
        }

        for (Enemigo e : nivel.getEnemigosActivos()) {
            SERVIDOR.enviarEstadoEnemigo(e.getId(), e.getX(), e.getY(), e.getDireccion(), e.estaAtacando(),
                e.getVida(), e.estaSpriteVisible(), e.getTipoAnimacion(), e.getTiempoAnimacion());
        }

        for (Integer id : nivel.getEnemigosEliminados()) {
            SERVIDOR.enviarEliminacionJefe(id);
        }
    }

    private void enviarProyectiles() {
        for (Lanzable p : nivel.getProyectilesNuevos()) {
            if (p instanceof BolaFuego bola) {
                SERVIDOR.enviarCreacionProyectil(p.getId(), "WALTER", p.getX(), p.getY(), bola.getDireccion());
            } else if (p instanceof Proyectil proyectilEnemigo) {
                SERVIDOR.enviarCreacionProyectil(p.getId(), "ENEMIGO", p.getX(), p.getY(), proyectilEnemigo.getAngulo());
            }
        }

        for (Lanzable p : nivel.getProyectilesActivos()) {
            SERVIDOR.enviarEstadoProyectil(p.getId(), p.getX(), p.getY());
        }

        for (Integer id : nivel.getProyectilesEliminados()) {
            SERVIDOR.enviarEliminacionProyectil(id);
        }
    }

    private void enviarJefe(Retumbar jefe) {
        if (!jefeCreado) {
            SERVIDOR.enviarCreacionJefe(jefe.getX(), jefe.getY(), jefe.getVida());
            jefeCreado = true;
        }

        SERVIDOR.enviarEstadoJefe(jefe.getX(), jefe.getY(), jefe.getVida(), jefe.estaVisible(),
            jefe.getEscalaSprite(), jefe.getFaseEspecial());

        if (jefe.estaMuerto()) {
            if (!jefeEliminado && nivel.creditosDisparados()) {
                jefeEliminado = true;
                SERVIDOR.enviarEliminacionJefe();
            }
            return;
        }

        enviarEstadoMano(jefe.getManoIzquierda());
        enviarEstadoMano(jefe.getManoDerecha());

        FuegoSuelo fuego = jefe.getFuegoSuelo();
        SERVIDOR.enviarEstadoFuegoSuelo(fuego.getX(), fuego.getY(), fuego.getAlto(), fuego.estaActiva());

        AtaquesExtraRetumbar ataques = jefe.getAtaquesExtra();
        if (ataques != null) {
            enviarAtaquesExtra(jefe, ataques);
        }
    }

    private void enviarEstadoMano(Mano mano) {
        SERVIDOR.enviarEstadoMano(mano.getLado(), mano.getX(), mano.getY(), mano.getVida(), mano.estaVisible(),
            mano.getEscalaSprite(), mano.getEstadoAtaque(), mano.getEstiloAtaque(), mano.mostrarFuegoGolpe());
    }

    private void enviarAtaquesExtra(Retumbar jefe, AtaquesExtraRetumbar ataques) {
        SERVIDOR.enviarEstadoRayos(ataques.getFaseRayos(), ataques.mostrarAvisoRayos(), ataques.getPosicionesRayos());
        SERVIDOR.enviarEstadoLasers(ataques.getFaseLasers(), ataques.mostrarAvisoLaser(), ataques.getLasersActivos());

        for (Meteorito m : ataques.getMeteoritosNuevos()) {
            SERVIDOR.enviarCreacionMeteorito(m.getId(), "NORMAL", m.getX(), m.getY());
        }
        for (MeteoritoGrande m : ataques.getMeteoritosGrandesNuevos()) {
            SERVIDOR.enviarCreacionMeteorito(m.getId(), "GRANDE", m.getX(), m.getY());
        }
        for (Meteorito m : ataques.getMeteoritosActivos()) {
            SERVIDOR.enviarEstadoMeteorito(m.getId(), "NORMAL", m.getX(), m.getY(),
                m.estaImpactando(), m.getRotacion(), m.getTiempoEfecto());
        }
        for (MeteoritoGrande m : ataques.getMeteoritosGrandesActivos()) {
            SERVIDOR.enviarEstadoMeteorito(m.getId(), "GRANDE", m.getX(), m.getY(),
                m.estaImpactando(), m.getRotacion(), m.getTiempoEfecto());
        }
        for (int id : ataques.getMeteoritosEliminados()) {
            SERVIDOR.enviarEliminacionMeteorito(id);
        }

        SERVIDOR.enviarEstadoLaserJefe(jefe.estaLaserJefeVisible(), jefe.debeMostrarAvisoLaserJefe(),
            jefe.getAnguloLaserSegundaFase(), jefe.getTiempoAnimacionLaserSegundaFase());

        for (Fueguito f : ataques.getFueguitosNuevos()) {
            SERVIDOR.enviarCreacionFueguito(f.getId(), "LLUVIA", f.getXSprite(), f.getYSprite(),
                f.getTamano(), f.getRotacion());
        }
        for (Fueguito f : ataques.getFueguitosActivos()) {
            SERVIDOR.enviarEstadoFueguito(f.getId(), "LLUVIA", f.getXSprite(), f.getYSprite(),
                f.getTamano(), f.getRotacion());
        }
        for (int id : ataques.getFueguitosEliminados()) {
            SERVIDOR.enviarEliminacionFueguito(id, "LLUVIA");
        }

        for (Fueguito f : jefe.getFueguitosRadialesNuevos()) {
            SERVIDOR.enviarCreacionFueguito(f.getId(), "RADIAL", f.getXSprite(), f.getYSprite(),
                f.getTamano(), f.getRotacion());
        }
        for (Fueguito f : jefe.getFueguitosRadialesActivos()) {
            SERVIDOR.enviarEstadoFueguito(f.getId(), "RADIAL", f.getXSprite(), f.getYSprite(),
                f.getTamano(), f.getRotacion());
        }
        for (int id : jefe.getFueguitosRadialesEliminados()) {
            SERVIDOR.enviarEliminacionFueguito(id, "RADIAL");
        }
    }

    public void marcarVolverAlHub() {
        debeVolverAlHub = true;
    }

    public void cargarHub() {
        if (nivel != null) {
            nivel.dispose();
            nivel = null;
        }
        if (hub != null) {
            hub.dispose();
        }

        for (Jugador jugador : JUGADORES) {
            jugador.reiniciar(SPAWN_X_DEFECTO, SPAWN_Y_DEFECTO);
        }

        hub = new Hub();
        hub.cargar(RUTA_HUB, JUGADORES);
        hub.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        seleccionNivelActiva = false;
        nivelIniciado = false;
        jefeEliminado = false;
        jefeCreado = false;
        creditosIniciados = false;
        tiempoCreditos = 0f;
    }

    @Override
    public void resize(int width, int height) {
        if (hub != null) {
            hub.resize(width, height);
        } else {
            nivel.resize(width, height);
        }
    }

    private void encolarSnapshot(InetAddress ip, int puerto) {
        SNAPSHOTS_PENDIENTES.add(new DestinoSnapshot(ip, puerto));
    }

    private void procesarSnapshotsPendientes() {
        DestinoSnapshot destino = SNAPSHOTS_PENDIENTES.poll();

        while (destino != null) {
            enviarSnapshotEntidades(destino.ip(), destino.puerto());
            destino = SNAPSHOTS_PENDIENTES.poll();
        }
    }

    private void enviarSnapshotEntidades(InetAddress ip, int puerto) {
        if (hub != null || nivel == null) {
            enviar("SNAPSHOT_FIN", ip, puerto);
            return;
        }

        String musica = nivel.getMusica();
        if (musica != null) {
            enviar("MUSICA;" + musica, ip, puerto);
        }

        enviarSnapshotJugadores(ip, puerto);
        enviarSnapshotEnemigos(ip, puerto);
        enviarSnapshotProyectiles(ip, puerto);

        Retumbar jefe = nivel.getJefe();
        if (jefe != null) {
            enviarSnapshotJefe(jefe, ip, puerto);
        }

        enviar("SNAPSHOT_FIN", ip, puerto);
    }

    private void enviarSnapshotJugadores(InetAddress ip, int puerto) {
        for (Jugador j : JUGADORES) {
            String nombre = j.getPersonaje().name();
            enviar("POS;" + nombre + ";" + j.getX() + ";" + j.getY() + ";" + j.getDireccion() + ";"
                + j.estaEnSuelo() + ";" + j.estaTrepando(), ip, puerto);
            enviar("ATAQUE;" + nombre + ";" + j.estaAtacando() + ";" + j.getTipoAnimacion() + ";"
                + j.getTiempoAnimacion(), ip, puerto);
            enviar("VIDA;" + nombre + ";" + j.getVida(), ip, puerto);
            enviar("INVENCIBILIDAD;" + nombre + ";" + j.estaInvencible() + ";" + j.estaSpriteVisible(), ip, puerto);
        }
    }

    private void enviarSnapshotEnemigos(InetAddress ip, int puerto) {
        for (Enemigo e : nivel.getEnemigosActivos()) {
            if (e.getTipo() == TipoEnemigo.JEFE) {
                continue;
            }

            enviar("CREAR_ENEMIGO;" + e.getId() + ";" + e.getTipo() + ";" + e.getX() + ";" + e.getY() + ";"
                + e.getVida(), ip, puerto);
            enviar("ESTADO_ENEMIGO;" + e.getId() + ";" + e.getX() + ";" + e.getY() + ";" + e.getDireccion() + ";"
                + e.estaAtacando() + ";" + e.getVida() + ";" + e.estaSpriteVisible() + ";"
                + e.getTipoAnimacion() + ";" + e.getTiempoAnimacion(), ip, puerto);
        }
    }

    private void enviarSnapshotProyectiles(InetAddress ip, int puerto) {
        for (Lanzable p : nivel.getProyectilesActivos()) {
            if (p instanceof BolaFuego bola) {
                enviar("CREAR_PROYECTIL;" + p.getId() + ";WALTER;" + p.getX() + ";" + p.getY() + ";"
                    + bola.getDireccion(), ip, puerto);
            } else if (p instanceof Proyectil proyectilEnemigo) {
                enviar("CREAR_PROYECTIL;" + p.getId() + ";ENEMIGO;" + p.getX() + ";" + p.getY() + ";"
                    + proyectilEnemigo.getAngulo(), ip, puerto);
            }
            enviar("ESTADO_PROYECTIL;" + p.getId() + ";" + p.getX() + ";" + p.getY(), ip, puerto);
        }
    }

    private void enviarSnapshotJefe(Retumbar jefe, InetAddress ip, int puerto) {
        enviar("CREAR_JEFE;" + jefe.getX() + ";" + jefe.getY() + ";" + jefe.getVida(), ip, puerto);
        enviar("ESTADO_JEFE;" + jefe.getX() + ";" + jefe.getY() + ";" + jefe.getVida() + ";" + jefe.estaVisible()
            + ";" + jefe.getEscalaSprite() + ";" + jefe.getFaseEspecial(), ip, puerto);

        if (jefe.estaMuerto()) {
            return;
        }

        enviarSnapshotMano(jefe.getManoIzquierda(), ip, puerto);
        enviarSnapshotMano(jefe.getManoDerecha(), ip, puerto);

        FuegoSuelo fuego = jefe.getFuegoSuelo();
        enviar("ESTADO_FUEGO_SUELO;" + fuego.getX() + ";" + fuego.getY() + ";" + fuego.getAlto() + ";"
            + fuego.estaActiva(), ip, puerto);

        AtaquesExtraRetumbar ataques = jefe.getAtaquesExtra();
        if (ataques != null) {
            enviarSnapshotAtaquesExtra(jefe, ataques, ip, puerto);
        }
    }

    private void enviarSnapshotMano(Mano m, InetAddress ip, int puerto) {
        enviar("ESTADO_MANO;" + m.getLado() + ";" + m.getX() + ";" + m.getY() + ";" + m.getVida() + ";"
            + m.estaVisible() + ";" + m.getEscalaSprite() + ";" + m.getEstadoAtaque() + ";"
            + m.getEstiloAtaque() + ";" + m.mostrarFuegoGolpe(), ip, puerto);
    }

    private void enviarSnapshotAtaquesExtra(Retumbar jefe, AtaquesExtraRetumbar ataques, InetAddress ip, int puerto) {
        StringBuilder rayos = new StringBuilder("ESTADO_RAYOS;")
            .append(ataques.getFaseRayos()).append(";").append(ataques.mostrarAvisoRayos());
        for (float posicion : ataques.getPosicionesRayos()) {
            rayos.append(";").append(posicion);
        }
        enviar(rayos.toString(), ip, puerto);

        StringBuilder lasers = new StringBuilder("ESTADO_LASERES;")
            .append(ataques.getFaseLasers()).append(";").append(ataques.mostrarAvisoLaser());
        for (boolean activo : ataques.getLasersActivos()) {
            lasers.append(";").append(activo);
        }
        enviar(lasers.toString(), ip, puerto);

        for (Meteorito m : ataques.getMeteoritosActivos()) {
            enviar("CREAR_METEORITO;" + m.getId() + ";NORMAL;" + m.getX() + ";" + m.getY(), ip, puerto);
            enviar("ESTADO_METEORITO;" + m.getId() + ";NORMAL;" + m.getX() + ";" + m.getY() + ";"
                + m.estaImpactando() + ";" + m.getRotacion() + ";" + m.getTiempoEfecto(), ip, puerto);
        }
        for (MeteoritoGrande m : ataques.getMeteoritosGrandesActivos()) {
            enviar("CREAR_METEORITO;" + m.getId() + ";GRANDE;" + m.getX() + ";" + m.getY(), ip, puerto);
            enviar("ESTADO_METEORITO;" + m.getId() + ";GRANDE;" + m.getX() + ";" + m.getY() + ";"
                + m.estaImpactando() + ";" + m.getRotacion() + ";" + m.getTiempoEfecto(), ip, puerto);
        }

        enviar("ESTADO_LASER_JEFE;" + jefe.estaLaserJefeVisible() + ";" + jefe.debeMostrarAvisoLaserJefe() + ";"
            + jefe.getAnguloLaserSegundaFase() + ";" + jefe.getTiempoAnimacionLaserSegundaFase(), ip, puerto);

        for (Fueguito f : ataques.getFueguitosActivos()) {
            enviarSnapshotFueguito(f, "LLUVIA", ip, puerto);
        }
        for (Fueguito f : jefe.getFueguitosRadialesActivos()) {
            enviarSnapshotFueguito(f, "RADIAL", ip, puerto);
        }
    }

    private void enviarSnapshotFueguito(Fueguito f, String tipo, InetAddress ip, int puerto) {
        String datos = f.getId() + ";" + tipo + ";" + f.getXSprite() + ";" + f.getYSprite() + ";"
            + f.getTamano() + ";" + f.getRotacion();
        enviar("CREAR_FUEGUITO;" + datos, ip, puerto);
        enviar("ESTADO_FUEGUITO;" + datos, ip, puerto);
    }

    private void enviar(String mensaje, InetAddress ip, int puerto) {
        SERVIDOR.enviarMensajeASpecifico(mensaje, ip, puerto);
    }

    private void detenerSonidosLoop() {
        Retumbar jefe = nivel != null ? nivel.getJefe() : null;
        if (jefe != null) {
            SERVIDOR.enviarDetenerSonidoLoopConPosicion("FUEGO_SOSTENIDO", jefe.getX(), jefe.getY());
        } else {
            SERVIDOR.enviarDetenerSonidoLoop("FUEGO_SOSTENIDO");
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        detenerSonidosLoop();
    }

    @Override
    public void dispose() {
        if (hub != null) {
            hub.dispose();
        }
        if (nivel != null) {
            nivel.dispose();
        }
    }
}
