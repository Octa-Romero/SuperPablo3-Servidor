package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Frames;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Retumbar extends Enemigo {

    private static final float ANCHO = 300f;
    private static final float ANCHO_SPRITE = 370f;
    private static final float ALTO = 400f;
    private static final float ALTURA_PECHO = ALTO * 0.77f;
    private static final int VELOCIDAD = 45;
    private static final float RECORRIDO = 250f;
    private static final double VIDA = 40;
    private static final double DANIO = 1;
    private static final float TIEMPO_ENTRE_ATAQUES = 3f;
    private static final int REPETICIONES_LEVANTAR_BAJAR = 5;
    private static final int REPETICIONES_TELETRANSPORTE = 3;
    private static final float TIEMPO_ENTRE_TELETRANSPORTES = 1f;

    private static final float ALTURA_SUBIDA_LENTA = 100f;
    private static final float DURACION_SUBIDA_LENTA = 2f;
    private static final float ALTURA_SUBIDA_RAPIDA = 200f;
    private static final float VELOCIDAD_SUBIDA_RAPIDA = 2500f;
    private static final float DURACION_FUEGO_SUELO = 0.5f;
    private static final float DURACION_TELETRANSPORTE = 0.15f;
    private static final float AMPLITUD_MOVIMIENTO_IDLE = 8f;
    private static final float FRECUENCIA_MOVIMIENTO_IDLE = 2.5f;
    private static final float DURACION_LASER_GIRATORIO = 0.6f;
    private static final float DURACION_APUNTAR_CENTRO = 2f;
    private static final float PAUSA_LASER_GIRATORIO = 1f;
    private static final float PAUSA_FUEGUITOS_DIRECCIONES = 0.2f;
    private static final float DURACION_SEGUIR_JUGADOR_LASER = 0.6f;
    private static final float DURACION_ESPERA_LASER_SEGUIDOR = 0.6f;
    private static final float DURACION_LASER_SEGUIDOR = 0.6f;
    private static final float PAUSA_LASER_SEGUIDOR = 0.8f;
    private static final float DURACION_ENCOGER_TELETRANSPORTE_ATAQUE = 0.2f;
    private static final float DURACION_OCULTO_TELETRANSPORTE_ATAQUE = 0.3f;
    private static final float DURACION_AGRANDAR_TELETRANSPORTE_ATAQUE = 0.2f;
    private static final float ANCHO_SPRITE_LASER_JEFE = 96f;
    private static final float ANCHO_HITBOX_LASER_JEFE = 56f;
    private static final float LARGO_LASER_JEFE = 1000f;
    private static final float ANGULO_LASER_CENTRO = -90f;
    private static final int CANTIDAD_FRAMES = 3;
    private static final int CANTIDAD_FRAMES_LASER = 2;
    private static final int CANTIDAD_FRAMES_FUEGUITO = 2;
    private static final int CANTIDAD_DIRECCIONES_FUEGUITOS = 8;
    private static final int CANTIDAD_DISPAROS_LASER_SEGUIDOR = 3;

    private enum TipoAtaque {
        LEVANTAR_BAJAR_MANOS,
        MANO_TELETRANSPORTE,
        ATAQUE_SUELO
    }

    private enum FaseSuelo {
        SUBIENDO_LENTO,
        SUBIDA_RAPIDA,
        VOLVIENDO
    }

    private enum FaseEspecial {
        NORMAL,
        DESAPARECIENDO,
        LLUVIA_FUEGUITOS,
        REAPARECIENDO,
        COMPLETADA
    }

    private enum AtaqueSegundaFase {
        NINGUNO,
        LASER_GIRATORIO,
        FUEGUITOS_DIRECCIONES,
        LASER_SEGUIDOR
    }

    private enum FaseAtaqueSegundaFase {
        LASER_GIRATORIO,
        LASER_APUNTANDO_CENTRO,
        LASER_CENTRO_ESPERA,
        ESPERA_LASER_OPUESTO,
        FUEGUITOS_ESPERA,
        FUEGUITOS_PAUSA,
        LASER_SEGUIDOR_AVISO,
        LASER_SEGUIDOR_ESPERA,
        LASER_SEGUIDOR_DISPARO,
        LASER_SEGUIDOR_PAUSA
    }

    private enum EstadoTeletransporteAtaque {
        NINGUNO,
        ENCOGIENDO,
        OCULTO,
        AGRANDANDO
    }

    private final float X_INICIAL;
    private final float Y_INICIAL;
    private final Mano MANO_IZQUIERDA;
    private final Mano MANO_DERECHA;
    private final FuegoSuelo FUEGO_SUELO;
    private final TextureRegion REGION_PIXEL;
    private final TextureRegion REGION_LASER;
    private final TextureRegion REGION_FUEGUITO;
    private final ArrayList<Fueguito> FUEGUITOS_RADIALES_NUEVOS = new ArrayList<>();
    private final List<Fueguito> FUEGUITOS_DIRECCIONADOS = new ArrayList<>();
    private final ArrayList<Integer> FUEGUITOS_RADIALES_ELIMINADOS = new ArrayList<>();
    private final ArrayList<Jugador> JUGADORES_DANIADOS_LASER = new ArrayList<>();
    private final Polygon HITBOX_LASER = new Polygon(new float[8]);
    private final Polygon HITBOX_JUGADOR_LASER = new Polygon(new float[8]);
    private final float[] VERTICES_JUGADOR_LASER = new float[8];

    private AtaquesExtraRetumbar ataquesExtra;
    private TipoAtaque ataqueActual;
    private boolean ejecutandoAtaque = false;
    private float tiempoAtaque = 0f;
    private int repeticionesAtaque = 0;
    private boolean manoDerechaAtacando = false;
    private Mano manoTeletransporte;
    private FaseSuelo faseSuelo;
    private float tiempoFase = 0f;
    private boolean esperandoTeletransporte = false;
    private float tiempoEsperaTeletransporte = 0f;
    private FaseEspecial faseEspecial = FaseEspecial.NORMAL;
    private float tiempoFaseEspecial = 0f;
    private float tiempoMovimientoIdle = 0f;
    private float anchoMapa;
    private float altoMapa;
    private float tiempoAtaqueSegundaFase = 0f;
    private float tiempoFaseAtaqueSegundaFase = 0f;
    private float tiempoTeletransporteAtaque = 0f;
    private float xDestinoTeletransporteAtaque = 0f;
    private AtaqueSegundaFase ataqueSegundaFase = AtaqueSegundaFase.NINGUNO;
    private EstadoTeletransporteAtaque estadoTeletransporteAtaque =
        EstadoTeletransporteAtaque.NINGUNO;
    private FaseAtaqueSegundaFase faseAtaqueSegundaFase;
    private float anguloLaserSegundaFase = 0f;
    private float anguloLaserInicial = 0f;
    private float direccionLaserInicial = 1f;
    private boolean segundaPasadaLaser = false;
    private boolean sonidoLaserActivo = false;
    private int posicionFueguitos = 0;
    private int disparosLaserSeguidor = 0;
    private int idFueguitoRadial = 1;
    private Jugador jugadorLaserSeguidor;

    public Retumbar(
        float centroX,
        float y,
        Jugador jugador,
        Jugador[] jugadores,
        float sueloXMin,
        float sueloXMax) {
        super(
            centroX - ANCHO / 2f,
            y,
            ANCHO,
            ALTO,
            VELOCIDAD,
            VIDA,
            DANIO,
            jugador,
            jugadores,
            GestorRecursos.obtenerTextura("Jefe/retumbar.png"));

        X_INICIAL = x;
        Y_INICIAL = y;
        duracionInvencibilidad = 0f;

        float offsetSpriteX = (ANCHO - ANCHO_SPRITE) / 2f;
        configurarSpriteVisual(ANCHO_SPRITE, ALTO + 100f, offsetSpriteX, offsetSpriteX, 0f);
        sprite.setRegion(Frames.primero(sprite.getTexture(), CANTIDAD_FRAMES));
        sprite.setOriginCenter();

        REGION_PIXEL = new TextureRegion(GestorRecursos.obtenerPixel());
        REGION_LASER =
            Frames.primero(
                GestorRecursos.obtenerTextura("Jefe/laser.png"), CANTIDAD_FRAMES_LASER);
        REGION_LASER.flip(false, true);
        REGION_FUEGUITO =
            Frames.primero(
                GestorRecursos.obtenerTextura("Jefe/fueguito.png"),
                CANTIDAD_FRAMES_FUEGUITO);

        HITBOX_LASER.setVertices(
            new float[] {
                0f,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                LARGO_LASER_JEFE,
                0f,
                LARGO_LASER_JEFE
            });
        HITBOX_LASER.setOrigin(ANCHO_HITBOX_LASER_JEFE / 2f, 0f);

        MANO_IZQUIERDA = new Mano(this, Mano.Lado.IZQUIERDA, jugador, jugadores);
        MANO_DERECHA = new Mano(this, Mano.Lado.DERECHA, jugador, jugadores);
        FUEGO_SUELO = new FuegoSuelo(sueloXMin, sueloXMax, y, jugadores);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        FUEGO_SUELO.update(delta);

        if (faseEspecial == FaseEspecial.NORMAL
            && MANO_IZQUIERDA.estaMuerto()
            && MANO_DERECHA.estaMuerto()) {
            iniciarFaseEspecial();
        }

        switch (faseEspecial) {
            case NORMAL:
                actualizarPrimeraFase(delta);
                break;
            case COMPLETADA:
                actualizarSegundaFase(delta);
                break;
            default:
                actualizarFaseEspecialFrame(delta);
                break;
        }
    }

    private void actualizarPrimeraFase(float delta) {
        actualizarMovimientoIdle(delta);
        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);
        actualizarComportamiento(delta, null);

        if (ataquesExtra != null) {
            ataquesExtra.update(delta, false);
        }

        terminarFrame(delta);
    }

    private void actualizarSegundaFase(float delta) {
        FUEGUITOS_DIRECCIONADOS.removeIf(
            fueguito -> {
                if (fueguito.update(delta)) {
                    return false;
                }
                FUEGUITOS_RADIALES_ELIMINADOS.add(fueguito.getId());
                return true;
            });

        if (ataquesExtra != null) {
            ataquesExtra.update(delta, true);
        }

        if (ataqueSegundaFase == AtaqueSegundaFase.NINGUNO) {
            actualizarAtaquesSegundaFase(delta);
        } else {
            ejecutarAtaqueSegundaFase(delta);
        }

        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);
        terminarFrame(delta);
    }

    private void actualizarFaseEspecialFrame(float delta) {
        actualizarMovimientoIdle(delta);
        guardarPosicionAnterior();
        moverBoss(delta);
        actualizarFaseEspecial(delta);
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
    }

    private void terminarFrame(float delta) {
        bounds.setPosition(x, y);
        tiempoAnimacion += delta;
        actualizarSpriteVisual();
    }

    private void actualizarAtaquesSegundaFase(float delta) {
        tiempoAtaqueSegundaFase += delta;
        if (tiempoAtaqueSegundaFase < TIEMPO_ENTRE_ATAQUES) {
            actualizarMovimientoIdle(delta);
            moverBoss(delta);
            return;
        }

        tiempoAtaqueSegundaFase = 0f;

        int opciones = ambosJugadoresMuertos() ? 2 : 3;
        switch (MathUtils.random(opciones - 1)) {
            case 0:
                iniciarLaserLateral();
                break;
            case 1:
                iniciarRafagaRadial();
                break;
            default:
                iniciarLaserApuntado();
                break;
        }
        ejecutarAtaqueSegundaFase(0f);
    }

    private void iniciarLaserLateral() {
        ataqueSegundaFase = AtaqueSegundaFase.LASER_GIRATORIO;
        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_GIRATORIO);
        segundaPasadaLaser = false;
        direccionLaserInicial = MathUtils.randomBoolean() ? 1f : -1f;
        anguloLaserSegundaFase = direccionLaserInicial > 0f ? 0f : 180f;
        iniciarTeletransporteAtaque(X_INICIAL);
    }

    private void iniciarRafagaRadial() {
        ataqueSegundaFase = AtaqueSegundaFase.FUEGUITOS_DIRECCIONES;
        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.FUEGUITOS_ESPERA);
        posicionFueguitos = 0;
        iniciarTeletransporteAtaque(X_INICIAL);
    }

    private void iniciarLaserApuntado() {
        ataqueSegundaFase = AtaqueSegundaFase.LASER_SEGUIDOR;
        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO);
        disparosLaserSeguidor = 0;
        jugadorLaserSeguidor = null;
        JUGADORES_DANIADOS_LASER.clear();
        actualizarAnguloHaciaJugador();
    }

    private void cambiarFaseSegundaFase(FaseAtaqueSegundaFase fase) {
        faseAtaqueSegundaFase = fase;
        tiempoFaseAtaqueSegundaFase = 0f;
    }

    private void ejecutarAtaqueSegundaFase(float delta) {
        if (ataqueSegundaFase == AtaqueSegundaFase.NINGUNO
            || actualizarTeletransporteAtaque(delta)) {
            return;
        }

        tiempoFaseAtaqueSegundaFase += delta;

        switch (faseAtaqueSegundaFase) {
            case LASER_GIRATORIO:
                iniciarSonidoLaser();
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    anguloLaserInicial = anguloLaserSegundaFase;
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_APUNTANDO_CENTRO);
                }
                break;

            case LASER_APUNTANDO_CENTRO:
                anguloLaserSegundaFase =
                    MathUtils.lerpAngleDeg(
                        anguloLaserInicial,
                        ANGULO_LASER_CENTRO,
                        Math.min(
                            1f,
                            tiempoFaseAtaqueSegundaFase / DURACION_APUNTAR_CENTRO));
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_APUNTAR_CENTRO) {
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_CENTRO_ESPERA);
                }
                break;

            case LASER_CENTRO_ESPERA:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    detenerSonidoLaser();
                    if (segundaPasadaLaser) {
                        terminarAtaqueSegundaFase();
                    } else {
                        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.ESPERA_LASER_OPUESTO);
                    }
                }
                break;

            case ESPERA_LASER_OPUESTO:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_LASER_GIRATORIO) {
                    segundaPasadaLaser = true;
                    anguloLaserSegundaFase = direccionLaserInicial > 0f ? 180f : 0f;
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_GIRATORIO);
                }
                break;

            case FUEGUITOS_ESPERA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_FUEGUITOS_DIRECCIONES) {
                    dispararRafagaRadial();
                    if (posicionFueguitos >= 2) {
                        terminarAtaqueSegundaFase();
                    } else {
                        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.FUEGUITOS_PAUSA);
                    }
                }
                break;

            case FUEGUITOS_PAUSA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_FUEGUITOS_DIRECCIONES) {
                    posicionFueguitos++;
                    iniciarTeletransporteAtaque(obtenerPosicionRafaga());
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.FUEGUITOS_ESPERA);
                }
                break;

            case LASER_SEGUIDOR_AVISO:
                actualizarAnguloHaciaJugador();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_SEGUIR_JUGADOR_LASER) {
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_SEGUIDOR_ESPERA);
                }
                break;

            case LASER_SEGUIDOR_ESPERA:
                if (tiempoFaseAtaqueSegundaFase >= DURACION_ESPERA_LASER_SEGUIDOR) {
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_SEGUIDOR_DISPARO);
                    JUGADORES_DANIADOS_LASER.clear();
                    reproducirSonido("RELAMPAGO");
                }
                break;

            case LASER_SEGUIDOR_DISPARO:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_SEGUIDOR) {
                    disparosLaserSeguidor++;
                    if (disparosLaserSeguidor >= CANTIDAD_DISPAROS_LASER_SEGUIDOR) {
                        terminarAtaqueSegundaFase();
                    } else {
                        cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_SEGUIDOR_PAUSA);
                    }
                }
                break;

            case LASER_SEGUIDOR_PAUSA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_LASER_SEGUIDOR) {
                    jugadorLaserSeguidor = null;
                    cambiarFaseSegundaFase(FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO);
                    actualizarAnguloHaciaJugador();
                }
                break;
        }
    }

    private void iniciarTeletransporteAtaque(float destinoX) {
        xDestinoTeletransporteAtaque =
            MathUtils.clamp(destinoX, 0f, Math.max(0f, anchoMapa - ancho));
        estadoTeletransporteAtaque = EstadoTeletransporteAtaque.ENCOGIENDO;
        tiempoTeletransporteAtaque = 0f;
        reproducirSonido("TP1");
    }

    private boolean actualizarTeletransporteAtaque(float delta) {
        if (estadoTeletransporteAtaque == EstadoTeletransporteAtaque.NINGUNO) {
            return false;
        }

        tiempoTeletransporteAtaque += delta;

        switch (estadoTeletransporteAtaque) {
            case ENCOGIENDO:
                sprite.setScale(
                    Math.max(
                        0f,
                        1f
                            - tiempoTeletransporteAtaque
                            / DURACION_ENCOGER_TELETRANSPORTE_ATAQUE));
                if (tiempoTeletransporteAtaque >= DURACION_ENCOGER_TELETRANSPORTE_ATAQUE) {
                    sprite.setScale(0f);
                    estadoTeletransporteAtaque = EstadoTeletransporteAtaque.OCULTO;
                    tiempoTeletransporteAtaque = 0f;
                }
                break;

            case OCULTO:
                if (tiempoTeletransporteAtaque >= DURACION_OCULTO_TELETRANSPORTE_ATAQUE) {
                    x = xDestinoTeletransporteAtaque;
                    y = Y_INICIAL;
                    bounds.setPosition(x, y);
                    actualizarSpriteVisual();
                    estadoTeletransporteAtaque = EstadoTeletransporteAtaque.AGRANDANDO;
                    tiempoTeletransporteAtaque = 0f;
                    reproducirSonido("TP2");
                }
                break;

            case AGRANDANDO:
                sprite.setScale(
                    Math.min(
                        1f,
                        tiempoTeletransporteAtaque
                            / DURACION_AGRANDAR_TELETRANSPORTE_ATAQUE));
                if (tiempoTeletransporteAtaque >= DURACION_AGRANDAR_TELETRANSPORTE_ATAQUE) {
                    sprite.setScale(1f);
                    estadoTeletransporteAtaque = EstadoTeletransporteAtaque.NINGUNO;
                    tiempoTeletransporteAtaque = 0f;
                }
                break;

            default:
                return false;
        }
        return true;
    }

    private void dispararRafagaRadial() {
        float centroX = x + ancho / 2f;
        float pechoY = obtenerPechoY();

        for (int i = 0; i < CANTIDAD_DIRECCIONES_FUEGUITOS; i++) {
            float angulo = i * MathUtils.PI2 / CANTIDAD_DIRECCIONES_FUEGUITOS;
            Fueguito fueguito =
                Fueguito.crearRadial(
                    centroX, pechoY, angulo, anchoMapa, altoMapa, jugadores, REGION_FUEGUITO);
            fueguito.setId(idFueguitoRadial++);

            FUEGUITOS_DIRECCIONADOS.add(fueguito);
            FUEGUITOS_RADIALES_NUEVOS.add(fueguito);
        }

        reproducirSonido("FUEGUITO");
    }

    private float obtenerPosicionRafaga() {
        if (posicionFueguitos == 1) {
            return Math.max(0f, X_INICIAL - RECORRIDO);
        }
        return Math.min(anchoMapa - ancho, X_INICIAL + RECORRIDO);
    }

    private void actualizarAnguloHaciaJugador() {
        if (jugadorLaserSeguidor == null || jugadorLaserSeguidor.estaMuerto()) {
            jugadorLaserSeguidor = obtenerJugadorAleatorio();
        }
        if (jugadorLaserSeguidor == null) {
            return;
        }

        Rectangle boundsObjetivo = jugadorLaserSeguidor.getBounds();
        float dx = boundsObjetivo.x + boundsObjetivo.width / 2f - (x + ancho / 2f);
        float dy = boundsObjetivo.y + boundsObjetivo.height / 2f - obtenerPechoY();
        anguloLaserSegundaFase = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
    }

    private float obtenerPechoY() {
        return y + ALTURA_PECHO;
    }

    private void terminarAtaqueSegundaFase() {
        ataqueSegundaFase = AtaqueSegundaFase.NINGUNO;
        faseAtaqueSegundaFase = null;
        tiempoFaseAtaqueSegundaFase = 0f;
        tiempoAtaqueSegundaFase = 0f;
    }

    private void iniciarSonidoLaser() {
        if (sonidoLaserActivo || Servidor.instancia == null) {
            return;
        }
        sonidoLaserActivo = true;
        Servidor.instancia.enviarSonidoLoopConPosicion("FUEGO_SOSTENIDO", x, y);
    }

    private void detenerSonidoLaser() {
        if (!sonidoLaserActivo || Servidor.instancia == null) {
            return;
        }
        sonidoLaserActivo = false;
        Servidor.instancia.enviarDetenerSonidoLoopConPosicion("FUEGO_SOSTENIDO", x, y);
    }

    private void reproducirSonido(String nombre) {
        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(nombre, x, y);
        }
    }

    private void daniarJugadorPorLaser() {
        actualizarHitboxLaser();
        boolean esLaserSeguidor = ataqueSegundaFase == AtaqueSegundaFase.LASER_SEGUIDOR;

        for (Jugador j : jugadores) {
            if (j.estaMuerto() || (esLaserSeguidor && JUGADORES_DANIADOS_LASER.contains(j))) {
                continue;
            }

            Rectangle boundsJugador = j.getBounds();
            float derecha = boundsJugador.x + boundsJugador.width;
            float arriba = boundsJugador.y + boundsJugador.height;
            VERTICES_JUGADOR_LASER[0] = boundsJugador.x;
            VERTICES_JUGADOR_LASER[1] = boundsJugador.y;
            VERTICES_JUGADOR_LASER[2] = derecha;
            VERTICES_JUGADOR_LASER[3] = boundsJugador.y;
            VERTICES_JUGADOR_LASER[4] = derecha;
            VERTICES_JUGADOR_LASER[5] = arriba;
            VERTICES_JUGADOR_LASER[6] = boundsJugador.x;
            VERTICES_JUGADOR_LASER[7] = arriba;
            HITBOX_JUGADOR_LASER.setVertices(VERTICES_JUGADOR_LASER);

            if (Intersector.overlapConvexPolygons(HITBOX_LASER, HITBOX_JUGADOR_LASER)) {
                j.restarVida(DANIO);
                if (esLaserSeguidor) {
                    JUGADORES_DANIADOS_LASER.add(j);
                }
            }
        }
    }

    private void actualizarHitboxLaser() {
        HITBOX_LASER.setPosition(x + ancho / 2f - ANCHO_HITBOX_LASER_JEFE / 2f, obtenerPechoY());
        HITBOX_LASER.setRotation(anguloLaserSegundaFase - 90f);
    }

    private boolean laserVisible() {
        if (estadoTeletransporteAtaque != EstadoTeletransporteAtaque.NINGUNO
            || faseAtaqueSegundaFase == null) {
            return false;
        }

        switch (faseAtaqueSegundaFase) {
            case LASER_GIRATORIO:
            case LASER_APUNTANDO_CENTRO:
            case LASER_CENTRO_ESPERA:
            case LASER_SEGUIDOR_DISPARO:
                return true;
            default:
                return false;
        }
    }

    private boolean mostrarAvisoLaser() {
        return estadoTeletransporteAtaque == EstadoTeletransporteAtaque.NINGUNO
            && (faseAtaqueSegundaFase == FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO
            || faseAtaqueSegundaFase == FaseAtaqueSegundaFase.LASER_SEGUIDOR_ESPERA);
    }

    private void dibujarLaserSegundaFase() {
        boolean aviso = mostrarAvisoLaser();
        if (!aviso && !laserVisible()) {
            return;
        }

        float origenX = x + ancho / 2f;
        float origenY = obtenerPechoY();
        float rotacion = anguloLaserSegundaFase - 90f;

        if (aviso) {
            float colorAnterior = Render.batch.getPackedColor();
            Render.batch.setColor(1f, 0f, 0f, 0.45f);
            Render.batch.draw(
                REGION_PIXEL,
                origenX - ANCHO_HITBOX_LASER_JEFE / 2f,
                origenY,
                ANCHO_HITBOX_LASER_JEFE / 2f,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                LARGO_LASER_JEFE,
                1f,
                1f,
                rotacion);
            Render.batch.setPackedColor(colorAnterior);
            return;
        }

        Render.batch.draw(
            REGION_LASER,
            origenX - ANCHO_SPRITE_LASER_JEFE / 2f,
            origenY,
            ANCHO_SPRITE_LASER_JEFE / 2f,
            0f,
            ANCHO_SPRITE_LASER_JEFE,
            LARGO_LASER_JEFE,
            1f,
            1f,
            rotacion);
    }

    private void actualizarMovimientoIdle(float delta) {
        tiempoMovimientoIdle += delta;
        y = Y_INICIAL
            + MathUtils.sin(tiempoMovimientoIdle * FRECUENCIA_MOVIMIENTO_IDLE)
            * AMPLITUD_MOVIMIENTO_IDLE;
    }

    private void iniciarFaseEspecial() {
        faseEspecial = FaseEspecial.DESAPARECIENDO;
        tiempoFaseEspecial = 0f;
        ejecutandoAtaque = false;
        tiempoAtaque = 0f;
        FUEGO_SUELO.desactivar();
        if (ataquesExtra != null) {
            ataquesExtra.cancelarAtaques();
        }
        spriteVisible = true;
        sprite.setScale(1f);
        reproducirSonido("TP1");
    }

    private void actualizarFaseEspecial(float delta) {
        tiempoFaseEspecial += delta;

        switch (faseEspecial) {
            case DESAPARECIENDO:
                sprite.setScale(Math.max(0f, 1f - tiempoFaseEspecial / DURACION_TELETRANSPORTE));
                if (tiempoFaseEspecial >= DURACION_TELETRANSPORTE) {
                    spriteVisible = false;
                    sprite.setScale(1f);
                    tiempoFaseEspecial = 0f;
                    faseEspecial = FaseEspecial.LLUVIA_FUEGUITOS;
                    if (ataquesExtra != null) {
                        ataquesExtra.iniciarLluviaFueguitos();
                    } else {
                        iniciarReaparicion();
                    }
                }
                break;

            case LLUVIA_FUEGUITOS:
                if (ataquesExtra != null) {
                    ataquesExtra.update(delta, false);
                }
                if (ataquesExtra == null || !ataquesExtra.estaLluviaFueguitosActiva()) {
                    iniciarReaparicion();
                }
                break;

            case REAPARECIENDO:
                sprite.setScale(Math.min(1f, tiempoFaseEspecial / DURACION_TELETRANSPORTE));
                if (tiempoFaseEspecial >= DURACION_TELETRANSPORTE) {
                    sprite.setScale(1f);
                    faseEspecial = FaseEspecial.COMPLETADA;
                    tiempoAtaque = 0f;
                }
                break;

            default:
                break;
        }
    }

    private void iniciarReaparicion() {
        faseEspecial = FaseEspecial.REAPARECIENDO;
        tiempoFaseEspecial = 0f;
        spriteVisible = true;
        sprite.setScale(0f);
        reproducirSonido("TP2");
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        if (!ejecutandoAtaque) {
            tiempoAtaque += delta;

            if (tiempoAtaque >= TIEMPO_ENTRE_ATAQUES) {
                iniciarAtaqueAleatorio();
            }
        }

        if (!ejecutandoAtaque || ataqueActual != TipoAtaque.ATAQUE_SUELO) {
            moverBoss(delta);
        }

        if (ejecutandoAtaque) {
            ejecutarAtaque(delta);
        }
    }

    private void moverBoss(float delta) {
        x += direccion * velocidad * delta;

        if (x >= X_INICIAL + RECORRIDO) {
            x = X_INICIAL + RECORRIDO;
            direccion = -1;
        } else if (x <= X_INICIAL - RECORRIDO) {
            x = X_INICIAL - RECORRIDO;
            direccion = 1;
        }
    }

    private boolean ambosJugadoresMuertos() {
        for (Jugador j : jugadores) {
            if (j != null && !j.estaMuerto()) {
                return false;
            }
        }
        return true;
    }

    private void iniciarAtaqueAleatorio() {
        if (MANO_IZQUIERDA.estaMuerto() && MANO_DERECHA.estaMuerto()) {
            tiempoAtaque = 0f;
            return;
        }

        TipoAtaque[] ataques =
            ambosJugadoresMuertos()
                ? new TipoAtaque[] {
                TipoAtaque.LEVANTAR_BAJAR_MANOS, TipoAtaque.ATAQUE_SUELO
            }
                : TipoAtaque.values();
        ataqueActual = ataques[MathUtils.random(ataques.length - 1)];

        ejecutandoAtaque = true;
        tiempoAtaque = 0f;
        repeticionesAtaque = 0;
        manoDerechaAtacando = true;
        esperandoTeletransporte = false;
        tiempoEsperaTeletransporte = 0f;
        manoTeletransporte = null;
        MANO_IZQUIERDA.restablecerPosicionInicial();
        MANO_DERECHA.restablecerPosicionInicial();

        if (faseEspecial == FaseEspecial.NORMAL) {
            reproducirSonido("RETUMBAR_ATAQUE");
        }

        switch (ataqueActual) {
            case LEVANTAR_BAJAR_MANOS:
                MANO_DERECHA.iniciarLevantarBajar();
                break;

            case MANO_TELETRANSPORTE:
                manoTeletransporte = seleccionarManoTeletransporte(null);
                if (manoTeletransporte != null) {
                    manoTeletransporte.iniciarTeletransporte();
                } else {
                    terminarAtaque();
                }
                break;

            case ATAQUE_SUELO:
                iniciarAtaqueSuelo();
                break;
        }
    }

    private void ejecutarAtaque(float delta) {
        switch (ataqueActual) {
            case LEVANTAR_BAJAR_MANOS:
                ejecutarLevantarBajarManos();
                break;
            case MANO_TELETRANSPORTE:
                ejecutarManoTeletransporte(delta);
                break;
            case ATAQUE_SUELO:
                ejecutarAtaqueSuelo(delta);
                break;
        }
    }

    private void ejecutarLevantarBajarManos() {
        if (manoDerechaAtacando && MANO_DERECHA.estaAtaqueCompletado()) {
            manoDerechaAtacando = false;
            MANO_IZQUIERDA.iniciarLevantarBajar();
        } else if (!manoDerechaAtacando && MANO_IZQUIERDA.estaAtaqueCompletado()) {
            repeticionesAtaque++;

            if (repeticionesAtaque >= REPETICIONES_LEVANTAR_BAJAR) {
                terminarAtaque();
            } else {
                manoDerechaAtacando = true;
                MANO_DERECHA.iniciarLevantarBajar();
            }
        }
    }

    private void ejecutarManoTeletransporte(float delta) {
        if (esperandoTeletransporte) {
            tiempoEsperaTeletransporte -= delta;

            if (tiempoEsperaTeletransporte <= 0f) {
                manoTeletransporte = seleccionarManoTeletransporte(manoTeletransporte);

                if (manoTeletransporte == null) {
                    terminarAtaque();
                    return;
                }

                esperandoTeletransporte = false;
                manoTeletransporte.iniciarTeletransporte();
            }
            return;
        }

        if (manoTeletransporte == null || manoTeletransporte.estaMuerto()) {
            terminarAtaque();
            return;
        }

        if (manoTeletransporte.estaAtaqueCompletado()) {
            repeticionesAtaque++;

            if (repeticionesAtaque >= REPETICIONES_TELETRANSPORTE) {
                terminarAtaque();
            } else {
                esperandoTeletransporte = true;
                tiempoEsperaTeletransporte = TIEMPO_ENTRE_TELETRANSPORTES;
            }
        }
    }

    private Mano seleccionarManoTeletransporte(Mano manoAnterior) {
        boolean izquierdaViva = !MANO_IZQUIERDA.estaMuerto();
        boolean derechaViva = !MANO_DERECHA.estaMuerto();

        if (!izquierdaViva && !derechaViva) {
            return null;
        }
        if (!derechaViva) {
            return MANO_IZQUIERDA;
        }
        if (!izquierdaViva) {
            return MANO_DERECHA;
        }
        if (manoAnterior == MANO_IZQUIERDA) {
            return MANO_DERECHA;
        }
        if (manoAnterior == MANO_DERECHA) {
            return MANO_IZQUIERDA;
        }
        return MathUtils.randomBoolean() ? MANO_IZQUIERDA : MANO_DERECHA;
    }

    private void iniciarAtaqueSuelo() {
        faseSuelo = FaseSuelo.SUBIENDO_LENTO;
        tiempoFase = 0f;

        float velocidadLenta = ALTURA_SUBIDA_LENTA / DURACION_SUBIDA_LENTA;
        MANO_IZQUIERDA.iniciarSubida(ALTURA_SUBIDA_LENTA, velocidadLenta);
        MANO_DERECHA.iniciarSubida(ALTURA_SUBIDA_LENTA, velocidadLenta);
    }

    private void ejecutarAtaqueSuelo(float delta) {
        tiempoFase += delta;

        switch (faseSuelo) {
            case SUBIENDO_LENTO:
                if (tiempoFase >= DURACION_SUBIDA_LENTA) {
                    faseSuelo = FaseSuelo.SUBIDA_RAPIDA;
                    tiempoFase = 0f;

                    MANO_IZQUIERDA.iniciarSubida(ALTURA_SUBIDA_RAPIDA, VELOCIDAD_SUBIDA_RAPIDA);
                    MANO_DERECHA.iniciarSubida(ALTURA_SUBIDA_RAPIDA, VELOCIDAD_SUBIDA_RAPIDA);

                    FUEGO_SUELO.activar(DURACION_FUEGO_SUELO);
                    reproducirSonido("FUEGO_SUELO");
                }
                break;

            case SUBIDA_RAPIDA:
                if (tiempoFase >= DURACION_FUEGO_SUELO) {
                    faseSuelo = FaseSuelo.VOLVIENDO;
                    MANO_IZQUIERDA.iniciarRegreso();
                    MANO_DERECHA.iniciarRegreso();
                }
                break;

            case VOLVIENDO:
                if (MANO_IZQUIERDA.estaAtaqueCompletado() && MANO_DERECHA.estaAtaqueCompletado()) {
                    terminarAtaque();
                }
                break;
        }
    }

    private void terminarAtaque() {
        ejecutandoAtaque = false;
        tiempoAtaque = 0f;
        esperandoTeletransporte = false;
        tiempoEsperaTeletransporte = 0f;
        manoTeletransporte = null;
    }

    @Override
    public boolean debeVerificarColisionesMapa() {
        return false;
    }

    @Override
    public boolean puedeRecibirDanio() {
        return MANO_IZQUIERDA.estaMuerto()
            && MANO_DERECHA.estaMuerto()
            && faseEspecial == FaseEspecial.COMPLETADA
            && estadoTeletransporteAtaque == EstadoTeletransporteAtaque.NINGUNO;
    }

    @Override
    public void restarVida(double cantidad) {
        if (puedeRecibirDanio()) {
            reproducirSonido("RETUMBAR_GOLPE_RECIBIDO");
            super.restarVida(cantidad);
        }
    }

    @Override
    public void saltar() {
    }

    public Mano getManoIzquierda() {
        return MANO_IZQUIERDA;
    }

    public Mano getManoDerecha() {
        return MANO_DERECHA;
    }

    public FuegoSuelo getFuegoSuelo() {
        return FUEGO_SUELO;
    }

    public boolean estaLaserJefeVisible() {
        return laserVisible();
    }

    public boolean debeMostrarAvisoLaserJefe() {
        return mostrarAvisoLaser();
    }

    public float getAnguloLaserSegundaFase() {
        return anguloLaserSegundaFase;
    }

    public float getTiempoAnimacionLaserSegundaFase() {
        return tiempoFaseAtaqueSegundaFase;
    }

    public void configurarAtaquesExtra(
        float anchoMapa,
        float altoMapa,
        Consumer<Entidad> agregarEntidad,
        Consumer<Entidad> eliminarEntidad) {
        this.anchoMapa = anchoMapa;
        this.altoMapa = altoMapa;
        ataquesExtra =
            new AtaquesExtraRetumbar(
                jugador, jugadores, anchoMapa, altoMapa, y, agregarEntidad, eliminarEntidad);
    }

    @Override
    public void dibujar() {
        if (muerto) {
            return;
        }

        super.dibujar();
        FUEGO_SUELO.dibujar();
        dibujarLaserSegundaFase();
        for (Fueguito fueguito : FUEGUITOS_DIRECCIONADOS) {
            fueguito.dibujar();
        }
    }

    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.JEFE;
    }

    public boolean estaVisible() {
        return spriteVisible;
    }

    public float getEscalaSprite() {
        return sprite == null ? 1f : sprite.getScaleX();
    }

    public AtaquesExtraRetumbar getAtaquesExtra() {
        return ataquesExtra;
    }

    public ArrayList<Fueguito> getFueguitosRadialesNuevos() {
        ArrayList<Fueguito> nuevos = new ArrayList<>(FUEGUITOS_RADIALES_NUEVOS);
        FUEGUITOS_RADIALES_NUEVOS.clear();
        return nuevos;
    }

    public List<Fueguito> getFueguitosRadialesActivos() {
        return FUEGUITOS_DIRECCIONADOS;
    }

    public ArrayList<Integer> getFueguitosRadialesEliminados() {
        ArrayList<Integer> eliminados = new ArrayList<>(FUEGUITOS_RADIALES_ELIMINADOS);
        FUEGUITOS_RADIALES_ELIMINADOS.clear();
        return eliminados;
    }

    public void dibujarAtaquesExtra() {
        if (!muerto && ataquesExtra != null) {
            ataquesExtra.dibujar();
        }
    }

    public void dibujarHitboxesAtaquesExtra(ShapeRenderer shapeRenderer) {
        if (muerto) {
            return;
        }

        MANO_IZQUIERDA.dibujarHitbox(shapeRenderer);
        MANO_DERECHA.dibujarHitbox(shapeRenderer);
        FUEGO_SUELO.dibujarHitbox(shapeRenderer);

        if (ataquesExtra != null) {
            ataquesExtra.dibujarHitboxes(shapeRenderer);
        }

        shapeRenderer.setColor(Color.MAGENTA);
        for (Fueguito fueguito : FUEGUITOS_DIRECCIONADOS) {
            shapeRenderer.circle(
                fueguito.getHitbox().x, fueguito.getHitbox().y, fueguito.getHitbox().radius);
        }

        if (laserVisible() || mostrarAvisoLaser()) {
            actualizarHitboxLaser();
            float[] vertices = HITBOX_LASER.getTransformedVertices();
            for (int i = 0; i < vertices.length; i += 2) {
                int siguiente = (i + 2) % vertices.length;
                shapeRenderer.line(
                    vertices[i], vertices[i + 1], vertices[siguiente], vertices[siguiente + 1]);
            }
        }
    }

    public String getFaseEspecial() {
        return faseEspecial.name();
    }

    @Override
    protected void morir() {
        super.morir();
        detenerSonidoLaser();
        reproducirSonido("RETUMBAR_MUERTE");

        for (Fueguito fueguito : FUEGUITOS_DIRECCIONADOS) {
            FUEGUITOS_RADIALES_ELIMINADOS.add(fueguito.getId());
        }

        FUEGUITOS_DIRECCIONADOS.clear();
        FUEGO_SUELO.desactivar();
        ataqueSegundaFase = AtaqueSegundaFase.NINGUNO;
        faseAtaqueSegundaFase = null;
        estadoTeletransporteAtaque = EstadoTeletransporteAtaque.NINGUNO;
        if (ataquesExtra != null) {
            ataquesExtra.cancelarAtaques();
        }
        MANO_IZQUIERDA.desactivarPorMuerteJefe();
        MANO_DERECHA.desactivarPorMuerteJefe();
    }
}
