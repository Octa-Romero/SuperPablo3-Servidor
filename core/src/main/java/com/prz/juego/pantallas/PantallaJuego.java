package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.rango.Proyectil;
import com.prz.juego.entidades.jugadores.BolaFuego;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.entidades.jugadores.Personajes;
import com.prz.juego.niveles.Hub;
import com.prz.juego.niveles.Nivel;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.principal.Navegable;
import com.prz.juego.sistemas.Hud;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class PantallaJuego implements Screen {

    private Servidor servidor;
    private final Navegable NAV;
    private Jugador[] jugadores = new Jugador[2];
    private final Nivel NIVEL;
    private final MenuPausa MENU_PAUSA;
    private boolean pausado = false;
    private final Hud HUD;
    private Hub hub;
    private float tiempoEstadoHub = 0f;
    private boolean seleccionNotificada = false;

    public PantallaJuego(Navegable NAV, Servidor servidor) {
        this(NAV, servidor, null);
    }

    public PantallaJuego(Navegable nav, Servidor servidor, String rutaNivel) {
        this.NAV = nav;
        this.NIVEL = rutaNivel == null ? null : Nivel.crear(rutaNivel);
        this.MENU_PAUSA = new MenuPausa(nav, this);
        this.servidor = servidor;

        JugadorRed[] estadosRed = servidor.getJugadoresRed();

        jugadores = new Jugador[estadosRed.length];

        for (int i = 0; i < jugadores.length; i++) {
            JugadorRed estado = estadosRed[i];

            Personajes personaje = Personajes.valueOf(estado.getPersonaje());

            jugadores[i] = personaje.crear(50 + i * 100, 150);
            jugadores[i].setEstadoRed(estado);
        }

            if (rutaNivel == null) {
                hub = new Hub();
                hub.cargar("Niveles/Niveles/Hub.tmx", jugadores);
            } else {
                hub = null;
                NIVEL.cargar(jugadores);
            }

            HUD = new Hud(jugadores[0], Render.batch);

            if (NIVEL != null) {
                HUD.setJefe(NIVEL.getJefe());
            }
    }


    @Override
    public void show() {
        Gdx.input.setCursorCatched(!pausado);

        InputMultiplexer mux = new InputMultiplexer();

        mux.addProcessor(MENU_PAUSA.getStage());
        //mux.addProcessor(ENTRADA);

        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla();

        if (!pausado) {
            if (hub != null) {
                hub.update(delta);

                for(Jugador jugador : jugadores)
                {
                    servidor.enviarPosicion(jugador);
                }

                tiempoEstadoHub += delta;

                if (tiempoEstadoHub >= 0.25f) {
                    tiempoEstadoHub = 0f;

                    for (Jugador jugador : jugadores) {
                        servidor.enviarEstadoCentroHub(
                            jugador.getEstadoRed(),
                            hub.estaEnCentroHub(jugador)
                        );

                        servidor.enviarEstadoSeleccion(
                            jugador.getEstadoRed()
                        );
                    }
                }

                if (hub.contarJugadoresConfirmados() == 2
                    && !seleccionNotificada) {

                    seleccionNotificada = true;
                    servidor.enviarInicioSeleccionNivel();
                    NAV.cambiarPantalla(new SeleccionNivel(NAV, servidor));
                }
            } else {
                NIVEL.update(delta);

                for (Jugador jugador : jugadores) {
                    servidor.enviarPosicion(jugador);
                    servidor.enviarEstadoAtaque(jugador);
                    servidor.enviarEstadoVida(jugador);
                }

                for (Enemigo e : NIVEL.getEnemigosNuevos()) {
                    servidor.enviarCreacionEnemigos(e.getId(), e.getTipo(), e.getX(), e.getY(), e.getVida());
                }

                for (Enemigo e : NIVEL.getEnemigosActivos()) {
                    servidor.enviarEstadoEnemigo(e.getId(), e.getX(), e.getY(), e.getDireccion(), e.estaAtacando(), e.getVida());
                }

                for (Integer id : NIVEL.getEnemigosEliminados()) {
                    servidor.enviarEliminacionEnemigo(id);
                }

                for (Lanzable p : NIVEL.getProyectilesNuevos()) {

                    if (p instanceof BolaFuego bola) {
                        servidor.enviarCreacionProyectil(
                            p.getId(), "WALTER",
                            p.getX(), p.getY(),
                            bola.getDireccion()
                        );

                    } else if (p instanceof Proyectil proyectilEnemigo) {
                        servidor.enviarCreacionProyectil(
                            p.getId(), "ENEMIGO",
                            p.getX(), p.getY(),
                            proyectilEnemigo.getAngulo()
                        );
                    }
                }

                for (Lanzable p : NIVEL.getProyectilesActivos()) {
                    servidor.enviarEstadoProyectil(p.getId(), p.getX(), p.getY());
                }

                for (Integer id : NIVEL.getProyectilesEliminados()) {
                    servidor.enviarEliminacionProyectil(id);
                }

                if (NIVEL.enemigoReliquiaDerrotado()) {
                    NAV.cambiarPantalla(new PantallaFinal(NAV));
                    return;
                }
            }
        }

        // Dibujar el Hub o el NIVEL seleccionado
        if (hub != null) {
            hub.renderMapa();

            Render.begin(hub.getCamara());
            hub.render();
            hub.dibujarAlertaCentro();
            Render.end();

        } else {
            NIVEL.renderMapa();

            Render.begin(NIVEL.getCamara());
            NIVEL.render();
            Render.end();

            NIVEL.renderDebug();

            if (NIVEL.isGameOver()) {
                NAV.cambiarPantalla(new GameOver(NAV));
                return;
            }
        }

        if (hub == null) {
            HUD.setTieneReliquia(NIVEL.tieneReliquia());
            HUD.dibujar();
        }

        if (pausado) {
            MENU_PAUSA.update(delta);
            MENU_PAUSA.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (hub != null) {
            hub.resize(width, height);
        } else {
            NIVEL.resize(width, height);
        }

        MENU_PAUSA.resize(width, height);
        HUD.actualizarTamano(width, height);
    }

    public void setPausa(boolean valor) {
        this.pausado = valor;
        Gdx.input.setCursorCatched(!valor);
    }

    private void limitarJugadorDentroDePantalla() {
        Camera CAMARA_ACTUAL = hub != null ? hub.getCamara() : NIVEL.getCamara();
        float CENTRO_Y = Gdx.graphics.getHeight() / 2f;
        float BORDE_IZQUIERDO = CAMARA_ACTUAL.unproject(new Vector3(0, CENTRO_Y, 0)).x;
        float BORDE_DERECHO = CAMARA_ACTUAL.unproject(new Vector3(Gdx.graphics.getWidth(), CENTRO_Y, 0)).x;

        for (Jugador jugador : jugadores) {
            jugador.limitarMovimientoHorizontal(BORDE_IZQUIERDO, BORDE_DERECHO);
        }
    }

    public void guardarPosicionCamara() {
        if (hub != null) {
            hub.guardarPosicionCamara();
        } else {
            NIVEL.guardarPosicionCamara();
        }
    }

    public boolean estaEnHub() {
        return NIVEL == null;
    }

    public void volverAlHub() {
        Musica.MENU.sonar();
        NAV.cambiarPantalla(new PantallaJuego(NAV, servidor));
    }

    public void restaurarPosicionCamara() {
        if (hub != null) {
            hub.restaurarPosicionCamara();
        } else {
            NIVEL.restaurarPosicionCamara();
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
        Gdx.input.setCursorCatched(false);
        Sonido.FUEGO_SOSTENIDO.detenerLoop();
    }

    @Override
    public void dispose() {
        if (hub != null) {
            hub.dispose();
        } else {
            NIVEL.dispose();
        }
        MENU_PAUSA.dispose();
        HUD.dispose();
    }
}

