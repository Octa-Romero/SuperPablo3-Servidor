package com.prz.juego.entidades.jefes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Retumbar extends Enemigo {

    private static final float ANCHO = 300;
    private static final float ANCHO_SPRITE = 370;
    private static final float ALTO = 400;
    private static final float ALTURA_PECHO = ALTO * 0.77f;
    private static final int VELOCIDAD = 45;
    private static final float RECORRIDO = 250;
    private static final double VIDA = 40;
    private static final double DANIO = 1;
    private static final float TIEMPO_ENTRE_ATAQUES = 3f;
    private static final int REPETICIONES_LEVANTAR_BAJAR = 5;
    private static final int REPETICIONES_TELETRANSPORTE = 3;
    private static final float TIEMPO_ENTRE_TELETRANSPORTES = 1f;

    private static final float ALTURA_SUBIDA_LENTA = 100;
    private static final float DURACION_SUBIDA_LENTA = 2f;
    private static final float ALTURA_SUBIDA_RAPIDA = 200;
    private static final float VELOCIDAD_SUBIDA_RAPIDA = 2500;
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
    private static final float LARGO_SPRITE_LASER_JEFE = 1000f;
    private static final int CANTIDAD_FRAMES = 3;
    private static final float DURACION_FRAME = 0.12f;
    private static final int CANTIDAD_FRAMES_LASER = 2;
    private static final int CANTIDAD_DIRECCIONES_FUEGUITOS = 8;
    private static final int CANTIDAD_DISPAROS_LASER_SEGUIDOR = 3;

    private final float X_INICIAL;
    private final float Y_INICIAL;
    private final Mano MANO_IZQUIERDA;
    private final Mano MANO_DERECHA;
    private final FuegoSuelo FUEGO_SUELO;
    private final TextureRegion REGION_PIXEL;
    private final Animation<TextureRegion> ANIMACION_LASER;
    private final Animation<TextureRegion> ANIMACION_FUEGUITO;
    private final List<Fueguito> FUEGUITOS_DIRECCIONADOS = new ArrayList<>();
    private final Polygon HITBOX_LASER =
        new Polygon(
            new float[]{
                0f,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                1f,
                0f,
                1f
            });
    private final Polygon HITBOX_JUGADOR_LASER = new Polygon(new float[8]);
    private AtaquesExtraRetumbar ataquesExtra;

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
        LASER_GIRATORIO_INICIAL,
        LASER_APUNTANDO_CENTRO,
        LASER_CENTRO_ESPERA,
        ESPERA_LASER_OPUESTO,
        LASER_GIRATORIO_OPUESTO,
        LASER_OPUESTO_APUNTANDO_CENTRO,
        LASER_OPUESTO_CENTRO_ESPERA,
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

    private TipoAtaque ataqueActual;
    private boolean atacando = false;
    private float tiempoAtaque = 0f;
    private int repeticionesAtaque = 0;
    private boolean manoDerechaAtacando = false;
    private Mano manoTeletransporte;
    private FaseSuelo faseSuelo;
    private float tiempoFase = 0f;
    private boolean esperandoTeletransporte = false;
    private float tiempoEsperaTeletransporte = 0f;
    private FaseEspecial faseEspecial = FaseEspecial.NORMAL;
    private float tiempoFaseEspecial;
    private float tiempoMovimientoIdle;
    private float anchoMapa;
    private float altoMapa;
    private float tiempoAtaqueSegundaFase;
    private float tiempoFaseAtaqueSegundaFase;
    private float tiempoTeletransporteAtaque;
    private float xDestinoTeletransporteAtaque;
    private AtaqueSegundaFase ataqueSegundaFase = AtaqueSegundaFase.NINGUNO;
    private EstadoTeletransporteAtaque estadoTeletransporteAtaque =
        EstadoTeletransporteAtaque.NINGUNO;
    private FaseAtaqueSegundaFase faseAtaqueSegundaFase;
    private float anguloLaserSegundaFase;
    private float anguloLaserInicial;
    private float anguloLaserObjetivo;
    private float direccionLaserInicial;
    private boolean laserDaniadoJugador;
    private int posicionFueguitos;
    private int disparosLaserSeguidor;

    public Retumbar(
        float centroX,
        float y,
        Jugador jugador,
        float sueloXMin,
        float sueloXMax
    ) {
        super(
            centroX - ANCHO / 2f,
            y,
            ANCHO,
            ALTO,
            VELOCIDAD,
            VIDA,
            DANIO,
            jugador,
            GestorRecursos.obtenerTextura("Jefe/retumbar.png")
        );

        X_INICIAL = x;
        Y_INICIAL = y;
        duracionInvencibilidad = 0f;
        float offsetSpriteX = (ANCHO - ANCHO_SPRITE) / 2f;
        configurarSpriteVisual(ANCHO_SPRITE, ALTO + 100, offsetSpriteX, offsetSpriteX, 0);
        sprite.setOriginCenter();
        Texture spritesheet = GestorRecursos.obtenerTextura("Jefe/retumbar.png");
        ANIMACION.configurar(
            spritesheet,
            spritesheet.getWidth() / CANTIDAD_FRAMES,
            spritesheet.getHeight(),
            new int[]{0, 1, 2},
            null,
            null,
            null,
            DURACION_FRAME);
        ANIMACION.actualizar(sprite, 0f, false, false, false);

        Texture texturaLaser = GestorRecursos.obtenerTextura("Jefe/laser.png");
        REGION_PIXEL = new TextureRegion(GestorRecursos.obtenerPixel());
        TextureRegion[] framesLaser =
            TextureRegion.split(
                texturaLaser,
                texturaLaser.getWidth() / CANTIDAD_FRAMES_LASER,
                texturaLaser.getHeight())[0];
        ANIMACION_LASER = new Animation<>(DURACION_FRAME, framesLaser);
        ANIMACION_LASER.setPlayMode(Animation.PlayMode.LOOP);

        Texture texturaFueguito = GestorRecursos.obtenerTextura("Jefe/fueguito.png");
        TextureRegion[] framesFueguito =
            TextureRegion.split(
                texturaFueguito,
                texturaFueguito.getWidth() / 2,
                texturaFueguito.getHeight())[0];
        ANIMACION_FUEGUITO = new Animation<>(0.08f, framesFueguito);
        ANIMACION_FUEGUITO.setPlayMode(Animation.PlayMode.LOOP);

        MANO_IZQUIERDA = new Mano(this, Mano.Lado.IZQUIERDA, jugador);
        MANO_DERECHA = new Mano(this, Mano.Lado.DERECHA, jugador);
        FUEGO_SUELO = new FuegoSuelo(sueloXMin, sueloXMax, y, jugador);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        if (faseEspecial == FaseEspecial.NORMAL
            && MANO_IZQUIERDA.estaMuerto()
            && MANO_DERECHA.estaMuerto()) {
            iniciarFaseEspecial();
        }
        if (faseEspecial != FaseEspecial.NORMAL
            && faseEspecial != FaseEspecial.COMPLETADA) {
            actualizarMovimientoIdle(delta);
            actualizarMovimientoIdle(delta);
            guardarPosicionAnterior();
            moverBoss(delta);
            actualizarFaseEspecial(delta);
            bounds.setPosition(x, y);
            actualizarSpriteVisual();
            return;
        }

        if (faseEspecial == FaseEspecial.COMPLETADA) {
            FUEGUITOS_DIRECCIONADOS.removeIf(fueguito -> !fueguito.update(delta));
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
            bounds.setPosition(x, y);
            ANIMACION.actualizar(sprite, delta, false, false, false);
            actualizarSpriteVisual();
            return;
        }

        actualizarMovimientoIdle(delta);
        actualizarMovimientoIdle(delta);

        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);
        actualizarComportamiento(delta, jugador.getBounds());

        if (ataquesExtra != null) {
            ataquesExtra.update(delta, false);
        }

        bounds.setPosition(x, y);
        ANIMACION.actualizar(sprite, delta, false, false, false);
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
        int ataqueElegido = MathUtils.random(2);
        if (ataqueElegido == 0) {
            iniciarLaserLateral();
        } else if (ataqueElegido == 1) {
            iniciarRafagaRadial();
        } else {
            iniciarLaserApuntado();
        }
        ejecutarAtaqueSegundaFase(0f);
    }

    private void iniciarLaserLateral() {
        ataqueSegundaFase = AtaqueSegundaFase.LASER_GIRATORIO;
        faseAtaqueSegundaFase = FaseAtaqueSegundaFase.LASER_GIRATORIO_INICIAL;
        tiempoFaseAtaqueSegundaFase = 0f;
        iniciarTeletransporteAtaque(X_INICIAL);
        direccionLaserInicial = Math.random() < 0.5 ? 1f : -1f;
        anguloLaserSegundaFase = direccionLaserInicial > 0f ? 0f : 180f;
        laserDaniadoJugador = false;
    }

    private void iniciarRafagaRadial() {
        ataqueSegundaFase = AtaqueSegundaFase.FUEGUITOS_DIRECCIONES;
        faseAtaqueSegundaFase = FaseAtaqueSegundaFase.FUEGUITOS_ESPERA;
        tiempoFaseAtaqueSegundaFase = 0f;
        posicionFueguitos = 0;
        iniciarTeletransporteAtaque(X_INICIAL);
    }

    private void iniciarLaserApuntado() {
        ataqueSegundaFase = AtaqueSegundaFase.LASER_SEGUIDOR;
        faseAtaqueSegundaFase = FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO;
        tiempoFaseAtaqueSegundaFase = 0f;
        disparosLaserSeguidor = 0;
        laserDaniadoJugador = false;
        actualizarAnguloHaciaJugador();
    }

    private void ejecutarAtaqueSegundaFase(float delta) {
        if (ataqueSegundaFase == AtaqueSegundaFase.NINGUNO) {
            return;
        }

        if (actualizarTeletransporteAtaque(delta)) {
            return;
        }

        tiempoFaseAtaqueSegundaFase += delta;
        switch (faseAtaqueSegundaFase) {
            case LASER_GIRATORIO_INICIAL:
                if (!Sonido.FUEGO_SOSTENIDO.estaSonandoLoop()) {
                    Sonido.FUEGO_SOSTENIDO.sonarConLoop();
                }
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_APUNTANDO_CENTRO;
                    tiempoFaseAtaqueSegundaFase = 0f;
                    anguloLaserInicial = anguloLaserSegundaFase;
                    anguloLaserObjetivo = -90f;
                }
                break;

            case LASER_APUNTANDO_CENTRO:
                anguloLaserSegundaFase =
                    MathUtils.lerpAngleDeg(
                        anguloLaserInicial,
                        anguloLaserObjetivo,
                        Math.min(
                            1f,
                            tiempoFaseAtaqueSegundaFase
                                / DURACION_APUNTAR_CENTRO));
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_APUNTAR_CENTRO) {
                    faseAtaqueSegundaFase = FaseAtaqueSegundaFase.LASER_CENTRO_ESPERA;
                    tiempoFaseAtaqueSegundaFase = 0f;
                }
                break;

            case LASER_CENTRO_ESPERA:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    Sonido.FUEGO_SOSTENIDO.detenerLoop();
                    faseAtaqueSegundaFase = FaseAtaqueSegundaFase.ESPERA_LASER_OPUESTO;
                    tiempoFaseAtaqueSegundaFase = 0f;
                }
                break;

            case ESPERA_LASER_OPUESTO:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_LASER_GIRATORIO) {
                    if (!Sonido.FUEGO_SOSTENIDO.estaSonandoLoop()) {
                        Sonido.FUEGO_SOSTENIDO.sonarConLoop();
                    }
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_GIRATORIO_OPUESTO;
                    tiempoFaseAtaqueSegundaFase = 0f;
                    anguloLaserSegundaFase = direccionLaserInicial > 0f ? 180f : 0f;
                    laserDaniadoJugador = false;
                }
                break;

            case LASER_GIRATORIO_OPUESTO:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_OPUESTO_APUNTANDO_CENTRO;
                    tiempoFaseAtaqueSegundaFase = 0f;
                    anguloLaserInicial = anguloLaserSegundaFase;
                    anguloLaserObjetivo = -90f;
                }
                break;

            case LASER_OPUESTO_APUNTANDO_CENTRO:
                anguloLaserSegundaFase =
                    MathUtils.lerpAngleDeg(
                        anguloLaserInicial,
                        anguloLaserObjetivo,
                        Math.min(
                            1f,
                            tiempoFaseAtaqueSegundaFase
                                / DURACION_APUNTAR_CENTRO));
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_APUNTAR_CENTRO) {
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_OPUESTO_CENTRO_ESPERA;
                    tiempoFaseAtaqueSegundaFase = 0f;
                }
                break;

            case LASER_OPUESTO_CENTRO_ESPERA:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_GIRATORIO) {
                    Sonido.FUEGO_SOSTENIDO.detenerLoop();
                    terminarAtaqueSegundaFase();
                }
                break;

            case FUEGUITOS_ESPERA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_FUEGUITOS_DIRECCIONES) {
                    dispararRafagaRadial();
                    if (posicionFueguitos >= 2) {
                        terminarAtaqueSegundaFase();
                    } else {
                        faseAtaqueSegundaFase = FaseAtaqueSegundaFase.FUEGUITOS_PAUSA;
                        tiempoFaseAtaqueSegundaFase = 0f;
                    }
                }
                break;

            case FUEGUITOS_PAUSA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_FUEGUITOS_DIRECCIONES) {
                    posicionFueguitos++;
                    iniciarTeletransporteAtaque(obtenerPosicionRafaga());
                    faseAtaqueSegundaFase = FaseAtaqueSegundaFase.FUEGUITOS_ESPERA;
                    tiempoFaseAtaqueSegundaFase = 0f;
                }
                break;

            case LASER_SEGUIDOR_AVISO:
                actualizarAnguloHaciaJugador();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_SEGUIR_JUGADOR_LASER) {
                    faseAtaqueSegundaFase = FaseAtaqueSegundaFase.LASER_SEGUIDOR_ESPERA;
                    tiempoFaseAtaqueSegundaFase = 0f;
                }
                break;

            case LASER_SEGUIDOR_ESPERA:
                if (tiempoFaseAtaqueSegundaFase >= DURACION_ESPERA_LASER_SEGUIDOR) {
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_SEGUIDOR_DISPARO;
                    tiempoFaseAtaqueSegundaFase = 0f;
                    laserDaniadoJugador = false;
                    Sonido.RELAMPAGO.sonar();
                }
                break;

            case LASER_SEGUIDOR_DISPARO:
                daniarJugadorPorLaser();
                if (tiempoFaseAtaqueSegundaFase >= DURACION_LASER_SEGUIDOR) {
                    disparosLaserSeguidor++;
                    if (disparosLaserSeguidor >= CANTIDAD_DISPAROS_LASER_SEGUIDOR) {
                        terminarAtaqueSegundaFase();
                    } else {
                        faseAtaqueSegundaFase =
                            FaseAtaqueSegundaFase.LASER_SEGUIDOR_PAUSA;
                        tiempoFaseAtaqueSegundaFase = 0f;
                    }
                }
                break;

            case LASER_SEGUIDOR_PAUSA:
                if (tiempoFaseAtaqueSegundaFase >= PAUSA_LASER_SEGUIDOR) {
                    faseAtaqueSegundaFase =
                        FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO;
                    tiempoFaseAtaqueSegundaFase = 0f;
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
        Sonido.TP1.sonar();
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
                        1f - tiempoTeletransporteAtaque
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
                    Sonido.TP2.sonar();
                }
                break;

            case AGRANDANDO:
                sprite.setScale(
                    Math.min(
                        1f,
                        tiempoTeletransporteAtaque
                            / DURACION_AGRANDAR_TELETRANSPORTE_ATAQUE));
                if (tiempoTeletransporteAtaque
                    >= DURACION_AGRANDAR_TELETRANSPORTE_ATAQUE) {
                    sprite.setScale(1f);
                    estadoTeletransporteAtaque = EstadoTeletransporteAtaque.NINGUNO;
                    tiempoTeletransporteAtaque = 0f;
                }
                break;

            case NINGUNO:
                return false;
        }
        return true;
    }

    private void dispararRafagaRadial() {
        float centroX = x + ancho / 2f;
        float pechoY = obtenerPechoY();
        for (int i = 0; i < CANTIDAD_DIRECCIONES_FUEGUITOS; i++) {
            float angulo = i * MathUtils.PI2 / CANTIDAD_DIRECCIONES_FUEGUITOS;
            FUEGUITOS_DIRECCIONADOS.add(
                new Fueguito(
                    centroX,
                    pechoY,
                    angulo,
                    anchoMapa,
                    altoMapa,
                    jugador,
                    ANIMACION_FUEGUITO));
        }
        Sonido.FUEGUITO.sonar();
    }

    private float obtenerPosicionRafaga() {
        if (posicionFueguitos == 1) {
            return Math.max(0f, X_INICIAL - RECORRIDO);
        }
        return Math.min(anchoMapa - ancho, X_INICIAL + RECORRIDO);
    }

    private void actualizarAnguloHaciaJugador() {
        float dx = jugador.getBounds().x + jugador.getBounds().width / 2f - (x + ancho / 2f);
        float dy =
            jugador.getBounds().y
                + jugador.getBounds().height / 2f
                - obtenerPechoY();
        anguloLaserSegundaFase = (float) Math.toDegrees(Math.atan2(dy, dx));
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

    private void daniarJugadorPorLaser() {
        if (laserDaniadoJugador) {
            return;
        }

        Rectangle boundsJugador = jugador.getBounds();
        actualizarHitboxLaser();
        HITBOX_JUGADOR_LASER.setVertices(
            new float[]{
                0f,
                0f,
                boundsJugador.width,
                0f,
                boundsJugador.width,
                boundsJugador.height,
                0f,
                boundsJugador.height
            });
        HITBOX_JUGADOR_LASER.setPosition(boundsJugador.x, boundsJugador.y);

        if (Intersector.overlapConvexPolygons(HITBOX_LASER, HITBOX_JUGADOR_LASER)) {
            jugador.restarVida(DANIO);
            laserDaniadoJugador = true;
        }
    }

    private void actualizarHitboxLaser() {
        float origenX = x + ancho / 2f;
        float origenY = obtenerPechoY();
        float largo = obtenerLargoLaser();
        HITBOX_LASER.setVertices(
            new float[]{
                0f,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                largo,
                0f,
                largo
            });
        HITBOX_LASER.setOrigin(ANCHO_HITBOX_LASER_JEFE / 2f, 0f);
        HITBOX_LASER.setPosition(origenX - ANCHO_HITBOX_LASER_JEFE / 2f, origenY);
        HITBOX_LASER.setRotation(anguloLaserSegundaFase - 90f);
    }

    private float obtenerLargoLaser() {
        return LARGO_LASER_JEFE;
    }

    private boolean laserVisible() {
        return estadoTeletransporteAtaque == EstadoTeletransporteAtaque.NINGUNO
            && (faseAtaqueSegundaFase == FaseAtaqueSegundaFase.LASER_GIRATORIO_INICIAL
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_APUNTANDO_CENTRO
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_CENTRO_ESPERA
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_GIRATORIO_OPUESTO
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_OPUESTO_APUNTANDO_CENTRO
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_OPUESTO_CENTRO_ESPERA
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_SEGUIDOR_DISPARO);
    }

    private boolean mostrarAvisoLaser() {
        return estadoTeletransporteAtaque == EstadoTeletransporteAtaque.NINGUNO
            && (faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_SEGUIDOR_AVISO
            || faseAtaqueSegundaFase
            == FaseAtaqueSegundaFase.LASER_SEGUIDOR_ESPERA);
    }

    private void dibujarLaserSegundaFase() {
        if (!laserVisible() && !mostrarAvisoLaser()) {
            return;
        }

        float origenX = x + ancho / 2f;
        float origenY = obtenerPechoY();
        float largo = LARGO_SPRITE_LASER_JEFE;

        if (mostrarAvisoLaser()) {
            Color colorAnterior = Render.batch.getColor().cpy();
            Render.batch.setColor(1f, 0f, 0f, 0.45f);
            Render.batch.draw(
                REGION_PIXEL,
                origenX - ANCHO_HITBOX_LASER_JEFE / 2f,
                origenY,
                ANCHO_HITBOX_LASER_JEFE / 2f,
                0f,
                ANCHO_HITBOX_LASER_JEFE,
                largo,
                1f,
                1f,
                anguloLaserSegundaFase - 90f);
            Render.batch.setColor(colorAnterior);
            return;
        }

        TextureRegion frame =
            new TextureRegion(ANIMACION_LASER.getKeyFrame(tiempoFaseAtaqueSegundaFase));
        frame.flip(false, true);
        Render.batch.draw(
            frame,
            origenX - ANCHO_SPRITE_LASER_JEFE / 2f,
            origenY,
            ANCHO_SPRITE_LASER_JEFE / 2f,
            0f,
            ANCHO_SPRITE_LASER_JEFE,
            largo,
            1f,
            1f,
            anguloLaserSegundaFase - 90f);
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
        atacando = false;
        tiempoAtaque = 0f;
        FUEGO_SUELO.desactivar();
        if (ataquesExtra != null) {
            ataquesExtra.cancelarAtaques();
        }
        spriteVisible = true;
        sprite.setScale(1f);
        Sonido.TP1.sonar();
    }

    private void actualizarFaseEspecial(float delta) {
        tiempoFaseEspecial += delta;

        switch (faseEspecial) {
            case DESAPARECIENDO:
                sprite.setScale(
                    Math.max(0f, 1f - tiempoFaseEspecial / DURACION_TELETRANSPORTE));
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
                sprite.setScale(
                    Math.min(1f, tiempoFaseEspecial / DURACION_TELETRANSPORTE));
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
        Sonido.TP2.sonar();
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        if (!atacando) {
            tiempoAtaque += delta;

            if (tiempoAtaque >= TIEMPO_ENTRE_ATAQUES) {
                iniciarAtaqueAleatorio();
            }
        }

        if (!atacando || ataqueActual != TipoAtaque.ATAQUE_SUELO) {
            moverBoss(delta);
        }

        if (atacando) {
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

    private void iniciarAtaqueAleatorio() {
        boolean izquierdaViva = !MANO_IZQUIERDA.estaMuerto();
        boolean derechaViva = !MANO_DERECHA.estaMuerto();

        if (!izquierdaViva && !derechaViva) {
            tiempoAtaque = 0f;
            return;
        }

        TipoAtaque[] ataques = TipoAtaque.values();
        ataqueActual = ataques[(int) (Math.random() * ataques.length)];

        atacando = true;
        tiempoAtaque = 0f;
        repeticionesAtaque = 0;
        manoDerechaAtacando = true;
        esperandoTeletransporte = false;
        tiempoEsperaTeletransporte = 0f;
        manoTeletransporte = null;
        MANO_IZQUIERDA.restablecerPosicionInicial();
        MANO_DERECHA.restablecerPosicionInicial();

        if (faseEspecial == FaseEspecial.NORMAL) {
            Sonido.RETUMBAR_ATAQUE.sonar();
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
        boolean manoDerechaLista = MANO_DERECHA.estaAtaqueCompletado();
        boolean manoIzquierdaLista = MANO_IZQUIERDA.estaAtaqueCompletado();

        if (manoDerechaAtacando && manoDerechaLista) {
            manoDerechaAtacando = false;
            MANO_IZQUIERDA.iniciarLevantarBajar();
        } else if (!manoDerechaAtacando && manoIzquierdaLista) {
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

        if (izquierdaViva && !derechaViva) {
            return MANO_IZQUIERDA;
        }

        if (derechaViva && !izquierdaViva) {
            return MANO_DERECHA;
        }

        if (manoAnterior == MANO_IZQUIERDA) {
            return MANO_DERECHA;
        }

        if (manoAnterior == MANO_DERECHA) {
            return MANO_IZQUIERDA;
        }

        return Math.random() < 0.5 ? MANO_IZQUIERDA : MANO_DERECHA;
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

                    MANO_IZQUIERDA.iniciarSubida(
                        ALTURA_SUBIDA_RAPIDA,
                        VELOCIDAD_SUBIDA_RAPIDA
                    );
                    MANO_DERECHA.iniciarSubida(
                        ALTURA_SUBIDA_RAPIDA,
                        VELOCIDAD_SUBIDA_RAPIDA
                    );

                    FUEGO_SUELO.activar(DURACION_FUEGO_SUELO);
                    Sonido.FUEGO_SUELO.sonar();
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
                if (MANO_IZQUIERDA.estaAtaqueCompletado()
                    && MANO_DERECHA.estaAtaqueCompletado()) {
                    terminarAtaque();
                }
                break;
        }
    }

    private void terminarAtaque() {
        atacando = false;
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

    public boolean debeDibujarBarraVida() {
        return MANO_IZQUIERDA.estaMuerto()
            && MANO_DERECHA.estaMuerto()
            && faseEspecial == FaseEspecial.COMPLETADA;
    }

    @Override
    public void restarVida(double cantidad) {
        if (puedeRecibirDanio()) {
            Sonido.RETUMBAR_GOLPE_RECIBIDO.sonar();
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

    public void configurarAtaquesExtra(
        float anchoMapa,
        float altoMapa,
        Consumer<Entidad> agregarEntidad,
        Consumer<Entidad> eliminarEntidad) {
        this.anchoMapa = anchoMapa;
        this.altoMapa = altoMapa;
        ataquesExtra =
            new AtaquesExtraRetumbar(
                jugador, anchoMapa, altoMapa, y, agregarEntidad, eliminarEntidad);
    }

    @Override
    public void dibujar() {
        if (muerto) {
            return;
        }
        super.dibujar();
        dibujarLaserSegundaFase();
        for (Fueguito fueguito : FUEGUITOS_DIRECCIONADOS) {
            fueguito.dibujar();
        }
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
                fueguito.getHitbox().x,
                fueguito.getHitbox().y,
                fueguito.getHitbox().radius);
        }

        if (laserVisible() || mostrarAvisoLaser()) {
            actualizarHitboxLaser();
            float[] vertices = HITBOX_LASER.getTransformedVertices();
            for (int i = 0; i < vertices.length; i += 2) {
                int siguiente = (i + 2) % vertices.length;
                shapeRenderer.line(
                    vertices[i],
                    vertices[i + 1],
                    vertices[siguiente],
                    vertices[siguiente + 1]);
            }
        }
    }

    @Override
    protected void morir() {
        super.morir();
        Sonido.FUEGO_SOSTENIDO.detenerLoop();
        Sonido.RETUMBAR_MUERTE.sonar();
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
