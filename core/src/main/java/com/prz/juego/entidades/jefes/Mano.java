package com.prz.juego.entidades.jefes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Animacion;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class Mano extends Enemigo {

    public enum Lado {
        IZQUIERDA,
        DERECHA
    }

    private static final float ANCHO = 120;
    private static final float ALTO = 100;
    private static final float SEPARACION = 10;
    private static final double VIDA = 20;
    private static final double DANIO = 1;

    private static final float ALTURA_LEVANTADA = 300;
    private static final float VELOCIDAD_LEVANTAR = 500;
    private static final float VELOCIDAD_BAJAR = 1800;
    private static final float VELOCIDAD_VOLVER = 600;
    private static final float VELOCIDAD_RAPIDO_VOLVER = 1200;
    private static final float TIEMPO_DESPUES_DESAPARECER = 0.5f;
    private static final float TIEMPO_RAPIDO_DESPUES_DESAPARECER = 0.2f;
    private static final float TIEMPO_RETORNO_IDLE_DESAPARECIDO = 0.65f;
    private static final float DURACION_ESCALA_TELETRANSPORTE = 0.15f;
    private static final float ALTURA_TELETRANSPORTE = 600;
    private static final float ALTURA_REPOSO = 120;

    private static final float TIEMPO_SEGUIR_JUGADOR = 1f;
    private static final float TIEMPO_RAPIDO_SEGUIR_JUGADOR = 0.4f;
    private static final float TIEMPO_ESPERA_GOLPE = 0.8f;
    private static final float TIEMPO_RAPIDO_ESPERA_GOLPE = 0.3f;
    private static final float TIEMPO_RECUPERACION = 0.4f;
    private static final float TIEMPO_RAPIDO_RECUPERACION = 0.15f;
    private static final float DURACION_HITBOX_EXTENDIDA = 0.2f;
    private static final float ANCHO_FUEGO_GOLPE = 220;
    private static final float ALTO_FUEGO_GOLPE = ALTO;
    private static final float ANCHO_SPRITE = 170;
    private static final float ALTO_SPRITE = 170;
    private static final float ANCHO_SPRITE_EXTENDIDO = 200;
    private static final float ALTO_SPRITE_EXTENDIDO = 200;
    private static final float AMPLITUD_MOVIMIENTO_IDLE = 6f;
    private static final float FRECUENCIA_MOVIMIENTO_IDLE = 2.5f;
    private static final int CANTIDAD_FRAMES = 3;
    private static final int CANTIDAD_FRAMES_FUEGO = 2;
    private static final float DURACION_FRAME = 0.12f;

    private enum EstadoAtaque {
        NINGUNO,
        LEVANTANDO,
        BAJANDO,
        RECUPERANDO,
        DESAPARECIENDO,
        ESPERANDO_REAPARICION,
        REAPARECIENDO,
        SIGUIENDO_JUGADOR,
        ESPERANDO_GOLPE,
        MOVIENDO,
        EN_POSICION,
        VOLVIENDO
    }

    private enum EstiloAtaque {
        NORMAL,
        PUNIO,
        PALMA
    }

    private final Retumbar RETUMBAR;
    private final Lado LADO;
    private final Animacion ANIMACION_PUNIO;
    private final Texture TEXTURA_ABIERTA_ARRIBA;
    private final Texture TEXTURA_MANO_ABAJO;
    private final Texture TEXTURA_PUNIO;
    private final Animacion ANIMACION_ABIERTA_ARRIBA;
    private final Sprite SPRITE_FUEGO_GOLPE;
    private final Animacion ANIMACION_FUEGO_GOLPE;
    private final Rectangle BOUNDS_FUEGO_GOLPE = new Rectangle();

    private EstadoAtaque estadoAtaque = EstadoAtaque.NINGUNO;
    private boolean haciendoDanio = false;
    private boolean teletransportada = false;
    private boolean teletransporteRapido = false;
    private boolean volverAIdle = false;
    private EstiloAtaque estiloAtaque = EstiloAtaque.NORMAL;
    private float tiempoHitboxExtendida = 0f;
    private float tiempoEstado = 0f;
    private float escalaTeletransporte = 1f;
    private boolean visible = true;
    private float yAtaque;
    private float yObjetivo;
    private float yDestino;
    private float velocidadMovimiento;
    private float tiempoMovimientoIdle;
    private float desfaseMovimientoVertical;

    public Mano(Retumbar RETUMBAR, Lado LADO, Jugador jugador) {
        super(
            0,
            RETUMBAR.getY(),
            ANCHO,
            ALTO,
            0,
            VIDA,
            DANIO,
            jugador,
            GestorRecursos.obtenerTextura("Jefe/manoAbajo.png"));
        this.RETUMBAR = RETUMBAR;
        this.LADO = LADO;
        TEXTURA_ABIERTA_ARRIBA = GestorRecursos.obtenerTextura("Jefe/manoArriba.png");
        TEXTURA_MANO_ABAJO = GestorRecursos.obtenerTextura("Jefe/manoAbajo.png");
        TEXTURA_PUNIO = GestorRecursos.obtenerTextura("Jefe/punio.png");
        configurarAnimacion(ANIMACION, TEXTURA_MANO_ABAJO);
        ANIMACION_PUNIO = new Animacion(TEXTURA_PUNIO);
        configurarAnimacion(ANIMACION_PUNIO, TEXTURA_PUNIO);
        ANIMACION_ABIERTA_ARRIBA = new Animacion(TEXTURA_ABIERTA_ARRIBA);
        configurarAnimacion(ANIMACION_ABIERTA_ARRIBA, TEXTURA_ABIERTA_ARRIBA);
        Texture texturaFuego = GestorRecursos.obtenerTextura("Jefe/fuegoSuelo.png");
        SPRITE_FUEGO_GOLPE = new Sprite(texturaFuego);
        ANIMACION_FUEGO_GOLPE = new Animacion(texturaFuego);
        configurarAnimacion(ANIMACION_FUEGO_GOLPE, texturaFuego, CANTIDAD_FRAMES_FUEGO);
        SPRITE_FUEGO_GOLPE.setFlip(LADO == Lado.IZQUIERDA, false);
        duracionInvencibilidad = 0f;
        configurarSpriteVisual(
            ANCHO_SPRITE,
            ALTO_SPRITE,
            (ANCHO - ANCHO_SPRITE) / 2f,
            (ANCHO - ANCHO_SPRITE) / 2f,
            (ALTO - ALTO_SPRITE) / 2f);
        seguirJefe();
        actualizarTexturaSegunEstado(0f);
    }

    private void configurarAnimacion(Animacion animacion, Texture spritesheet) {
        configurarAnimacion(animacion, spritesheet, CANTIDAD_FRAMES);
    }

    private void configurarAnimacion(
        Animacion animacion, Texture spritesheet, int cantidadFrames) {
        int[] indicesFrames = new int[cantidadFrames];
        for (int i = 0; i < cantidadFrames; i++) {
            indicesFrames[i] = i;
        }
        animacion.configurar(
            spritesheet,
            spritesheet.getWidth() / cantidadFrames,
            spritesheet.getHeight(),
            indicesFrames,
            null,
            null,
            null,
            DURACION_FRAME);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);
        tiempoMovimientoIdle += delta;
        actualizarComportamiento(delta, jugador.getBounds());
        if (estadoAtaque == EstadoAtaque.NINGUNO) {
            actualizarDesfaseMovimientoVertical();
            y = RETUMBAR.getY() + ALTURA_REPOSO + desfaseMovimientoVertical;
        } else {
            desfaseMovimientoVertical = 0f;
        }
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
        actualizarTexturaSegunEstado(delta);
        ANIMACION_FUEGO_GOLPE.actualizar(
            SPRITE_FUEGO_GOLPE, delta, false, false, false);

        if (tiempoHitboxExtendida > 0) {
            tiempoHitboxExtendida -= delta;
        }
        actualizarBoundsFuegoGolpe();

        aplicarEscalaTeletransporte();

        if (haciendoDanio || tiempoHitboxExtendida > 0) {
            verificarColisionJugador();
        }

        sprite.setAlpha(visible ? 1f : 0f);
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        if (estadoAtaque == EstadoAtaque.NINGUNO) {
            seguirJefe();
        } else {
            ejecutarAtaque(delta);
        }
    }

    private void actualizarDesfaseMovimientoVertical() {
        desfaseMovimientoVertical =
            MathUtils.sin(tiempoMovimientoIdle * FRECUENCIA_MOVIMIENTO_IDLE)
                * AMPLITUD_MOVIMIENTO_IDLE;
    }

    private void ejecutarAtaque(float delta) {
        switch (estadoAtaque) {
            case LEVANTANDO:
                y += VELOCIDAD_LEVANTAR * delta;
                if (y >= yAtaque) {
                    y = yAtaque;
                    estadoAtaque = EstadoAtaque.BAJANDO;
                }
                break;
            case BAJANDO:
                y -= VELOCIDAD_BAJAR * delta;
                haciendoDanio = true;
                if (y <= yObjetivo) {
                    y = yObjetivo;
                    haciendoDanio = false;
                    tiempoHitboxExtendida = DURACION_HITBOX_EXTENDIDA;
                    Sonido.GOLPE.sonar();
                    estadoAtaque = EstadoAtaque.RECUPERANDO;
                    tiempoEstado = teletransporteRapido
                        ? TIEMPO_RAPIDO_RECUPERACION
                        : TIEMPO_RECUPERACION;
                }
                break;
            case RECUPERANDO:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0) {
                    if (teletransportada) {
                        volverAIdle = true;
                        teletransporteRapido = true;
                        estadoAtaque = EstadoAtaque.DESAPARECIENDO;
                    } else {
                        estadoAtaque = EstadoAtaque.VOLVIENDO;
                    }
                }
                break;
            case DESAPARECIENDO:
                escalaTeletransporte = Math.max(
                    0f,
                    escalaTeletransporte - delta / DURACION_ESCALA_TELETRANSPORTE);
                if (escalaTeletransporte <= 0f) {
                    visible = false;
                    estadoAtaque = EstadoAtaque.ESPERANDO_REAPARICION;
                    Sonido.TP1.sonar();
                    tiempoEstado = teletransporteRapido
                        ? (volverAIdle
                        ? TIEMPO_RETORNO_IDLE_DESAPARECIDO
                        : TIEMPO_RAPIDO_DESPUES_DESAPARECER)
                        : TIEMPO_DESPUES_DESAPARECER;
                }
                break;
            case ESPERANDO_REAPARICION:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0) {
                    if (volverAIdle) {
                        seguirJefeEnX();
                        y = RETUMBAR.getY() + ALTURA_REPOSO;
                        estiloAtaque = EstiloAtaque.NORMAL;
                    } else {
                        x = jugador.getX() + jugador.getAncho() / 2 - ANCHO / 2;
                        y = ALTURA_TELETRANSPORTE;
                        estiloAtaque = EstiloAtaque.PALMA;
                    }
                    escalaTeletransporte = 0f;
                    visible = true;
                    Sonido.TP2.sonar();
                    estadoAtaque = EstadoAtaque.REAPARECIENDO;
                }
                break;
            case REAPARECIENDO:
                if (!volverAIdle) {
                    x = jugador.getX() + jugador.getAncho() / 2 - ANCHO / 2;
                    y = ALTURA_TELETRANSPORTE;
                }
                escalaTeletransporte = Math.min(
                    1f,
                    escalaTeletransporte + delta / DURACION_ESCALA_TELETRANSPORTE);
                if (escalaTeletransporte >= 1f) {
                    if (volverAIdle) {
                        volverAIdle = false;
                        teletransportada = false;
                        teletransporteRapido = false;
                        estiloAtaque = EstiloAtaque.NORMAL;
                        estadoAtaque = EstadoAtaque.NINGUNO;
                    } else {
                        estadoAtaque = EstadoAtaque.SIGUIENDO_JUGADOR;
                        tiempoEstado = teletransporteRapido
                            ? TIEMPO_RAPIDO_SEGUIR_JUGADOR
                            : TIEMPO_SEGUIR_JUGADOR;
                    }
                }
                break;
            case SIGUIENDO_JUGADOR:
                tiempoEstado -= delta;
                x = jugador.getX() + jugador.getAncho() / 2 - ANCHO / 2;
                if (tiempoEstado <= 0) {
                    estadoAtaque = EstadoAtaque.ESPERANDO_GOLPE;
                    tiempoEstado = teletransporteRapido
                        ? TIEMPO_RAPIDO_ESPERA_GOLPE
                        : TIEMPO_ESPERA_GOLPE;
                }
                break;
            case ESPERANDO_GOLPE:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0) {
                    yObjetivo = RETUMBAR.getY();
                    estadoAtaque = EstadoAtaque.BAJANDO;
                }
                break;
            case MOVIENDO:
                velocidadY = 1f;
                y += velocidadMovimiento * delta;
                if (y >= yDestino) {
                    y = yDestino;
                    velocidadY = 0f;
                    estadoAtaque = EstadoAtaque.EN_POSICION;
                }
                break;
            case EN_POSICION:
                // Quieta en el aire hasta que Retumbar ordene otra cosa
                break;
            case VOLVIENDO:
                y -= (teletransporteRapido ? VELOCIDAD_RAPIDO_VOLVER : VELOCIDAD_VOLVER) * delta;
                if (y <= RETUMBAR.getY() + ALTURA_REPOSO) {
                    y = RETUMBAR.getY() + ALTURA_REPOSO;
                    teletransporteRapido = false;
                    estadoAtaque = EstadoAtaque.NINGUNO;
                }
                break;
            default:
                break;
        }

        if (debeSeguirEnX()) {
            seguirJefeEnX();
        }
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
    }

    private boolean debeSeguirEnX() {
        switch (estadoAtaque) {
            case DESAPARECIENDO:
            case ESPERANDO_REAPARICION:
            case REAPARECIENDO:
            case SIGUIENDO_JUGADOR:
            case ESPERANDO_GOLPE:
                return false;
            case BAJANDO:
            case RECUPERANDO:
                return !teletransportada;
            default:
                return true;
        }
    }

    private void seguirJefeEnX() {
        if (LADO == Lado.IZQUIERDA) {
            x = RETUMBAR.getX() - SEPARACION - ancho;
        } else {
            x = RETUMBAR.getX() + RETUMBAR.getAncho() + SEPARACION;
        }
    }

    private void seguirJefe() {
        seguirJefeEnX();
        y = RETUMBAR.getY() + ALTURA_REPOSO;

        bounds.setPosition(x, y);
        actualizarSpriteVisual();
    }

    private void actualizarTexturaSegunEstado(float delta) {
        if (estadoAtaque == EstadoAtaque.NINGUNO) {
            estiloAtaque = EstiloAtaque.NORMAL;
        }

        if (estiloAtaque == EstiloAtaque.PUNIO
            || (estiloAtaque == EstiloAtaque.NORMAL && tiempoHitboxExtendida > 0)) {
            ANIMACION_PUNIO.actualizar(sprite, delta, false, false, false);
            sprite.setFlip(LADO == Lado.DERECHA, false);
            ajustarTamanoSprite(ANCHO_SPRITE, ALTO_SPRITE);
            return;
        }

        if (estiloAtaque == EstiloAtaque.PALMA) {
            ANIMACION.actualizar(sprite, delta, false, false, false);
            sprite.setFlip(LADO == Lado.DERECHA, false);
            ajustarTamanoSprite(ANCHO_SPRITE_EXTENDIDO, ALTO_SPRITE_EXTENDIDO);
            return;
        }

        switch (estadoAtaque) {
            case LEVANTANDO:
            case MOVIENDO:
            case EN_POSICION:
                ANIMACION_ABIERTA_ARRIBA.actualizar(sprite, delta, false, false, false);
                sprite.setFlip(LADO == Lado.DERECHA, false);
                ajustarTamanoSprite(ANCHO_SPRITE_EXTENDIDO, ALTO_SPRITE_EXTENDIDO);
                return;
            case BAJANDO:
                ANIMACION.actualizar(sprite, delta, false, false, false);
                sprite.setFlip(LADO == Lado.DERECHA, false);
                ajustarTamanoSprite(ANCHO_SPRITE_EXTENDIDO, ALTO_SPRITE_EXTENDIDO);
                return;
            case RECUPERANDO:
                ANIMACION_PUNIO.actualizar(sprite, delta, false, false, false);
                sprite.setFlip(LADO == Lado.DERECHA, false);
                ajustarTamanoSprite(ANCHO_SPRITE, ALTO_SPRITE);
                return;
            case SIGUIENDO_JUGADOR:
            case ESPERANDO_GOLPE:
                ANIMACION.actualizar(sprite, delta, false, false, false);
                sprite.setFlip(LADO == Lado.DERECHA, false);
                ajustarTamanoSprite(ANCHO_SPRITE_EXTENDIDO, ALTO_SPRITE_EXTENDIDO);
                return;
            default:
                ANIMACION.actualizar(sprite, delta, false, false, false);
                sprite.setFlip(LADO == Lado.DERECHA, false);
                ajustarTamanoSprite(ANCHO_SPRITE_EXTENDIDO, ALTO_SPRITE_EXTENDIDO);
                return;
        }
    }

    private void ajustarTamanoSprite(float anchoSprite, float altoSprite) {
        if (this.anchoSprite != anchoSprite || this.altoSprite != altoSprite) {
            configurarSpriteVisual(
                anchoSprite,
                altoSprite,
                (ANCHO - anchoSprite) / 2f,
                (ANCHO - anchoSprite) / 2f,
                (ALTO - ALTO_SPRITE) / 2f);
        }
    }

    private void aplicarEscalaTeletransporte() {
        actualizarSpriteVisual();
    }

    @Override
    protected void actualizarSpriteVisual() {
        super.actualizarSpriteVisual();
        if (sprite != null) {
            sprite.setOriginCenter();
            sprite.setScale(escalaTeletransporte);
        }
    }

    @Override
    public void dibujar() {
        if (spriteVisible && visible && tiempoHitboxExtendida > 0) {
            SPRITE_FUEGO_GOLPE.setBounds(
                BOUNDS_FUEGO_GOLPE.x,
                BOUNDS_FUEGO_GOLPE.y,
                BOUNDS_FUEGO_GOLPE.width,
                BOUNDS_FUEGO_GOLPE.height);
            SPRITE_FUEGO_GOLPE.draw(Render.batch);
        }
        super.dibujar();
    }

    private void verificarColisionJugador() {
        boolean golpeaConMano = haciendoDanio && bounds.overlaps(jugador.getBounds());
        boolean golpeaConFuego =
            tiempoHitboxExtendida > 0
                && BOUNDS_FUEGO_GOLPE.overlaps(jugador.getBounds());
        if (golpeaConMano || golpeaConFuego) {
            jugador.restarVida(danio);
        }
    }

    private void actualizarBoundsFuegoGolpe() {
        BOUNDS_FUEGO_GOLPE.set(
            x + (ANCHO - ANCHO_FUEGO_GOLPE) / 2f,
            y + (ALTO - ALTO_FUEGO_GOLPE) / 2f,
            ANCHO_FUEGO_GOLPE,
            ALTO_FUEGO_GOLPE);
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        if (muerto) {
            return;
        }
        if (tiempoHitboxExtendida > 0) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(
                BOUNDS_FUEGO_GOLPE.x,
                BOUNDS_FUEGO_GOLPE.y,
                BOUNDS_FUEGO_GOLPE.width,
                BOUNDS_FUEGO_GOLPE.height);
        } else {
            super.dibujarHitbox(shapeRenderer);
        }
    }

    void desactivarPorMuerteJefe() {
        muerto = true;
        visible = false;
        spriteVisible = false;
        haciendoDanio = false;
        tiempoHitboxExtendida = 0f;
        estadoAtaque = EstadoAtaque.NINGUNO;
        bounds.set(0f, 0f, 0f, 0f);
        BOUNDS_FUEGO_GOLPE.set(0f, 0f, 0f, 0f);
    }

    @Override
    public void restarVida(double cantidad) {
        if (!visible) {
            return;
        }
        if (vida - cantidad > 0) {
            Sonido.RETUMBAR_GOLPE_RECIBIDO.sonar();
        }
        super.restarVida(cantidad);
    }

    public void iniciarLevantarBajar() {
        if (muerto) {
            return;
        }
        restablecerPosicionInicial();
        teletransportada = false;
        estiloAtaque = EstiloAtaque.PUNIO;
        estadoAtaque = EstadoAtaque.LEVANTANDO;
        yAtaque = RETUMBAR.getY() + ALTURA_REPOSO + ALTURA_LEVANTADA;
        yObjetivo = RETUMBAR.getY();
    }

    public void iniciarTeletransporte() {
        iniciarTeletransporte(false);
    }

    public void iniciarTeletransporte(boolean rapido) {
        if (muerto) {
            return;
        }
        restablecerPosicionInicial();
        teletransportada = true;
        teletransporteRapido = rapido;
        volverAIdle = false;
        escalaTeletransporte = 1f;
        estadoAtaque = EstadoAtaque.DESAPARECIENDO;
    }

    public void restablecerPosicionInicial() {
        if (muerto) {
            return;
        }
        desfaseMovimientoVertical = 0f;
        seguirJefeEnX();
        y = RETUMBAR.getY() + ALTURA_REPOSO;
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
    }

    public void iniciarSubida(float alturaSobreSuelo, float velocidad) {
        if (muerto) {
            return;
        }
        teletransportada = false;
        estiloAtaque = EstiloAtaque.NORMAL;
        estadoAtaque = EstadoAtaque.MOVIENDO;
        yDestino = RETUMBAR.getY() + ALTURA_REPOSO + alturaSobreSuelo;
        velocidadMovimiento = velocidad;
    }

    public void iniciarRegreso() {
        if (muerto) {
            return;
        }
        velocidadY = 0f;
        estadoAtaque = EstadoAtaque.VOLVIENDO;
    }

    public boolean estaAtaqueCompletado() {
        // Una mano muerta ya no puede atacar: se considera completado para no trabar al jefe
        return muerto || estadoAtaque == EstadoAtaque.NINGUNO;
    }

    public boolean estaEnAtaque() {
        return !muerto && estadoAtaque != EstadoAtaque.NINGUNO;
    }

    @Override
    public void saltar() {
    }

    @Override
    protected void morir() {
        super.morir();
        Sonido.RETUMBAR_MANO_ROTA.sonar();
    }

    @Override
    public boolean debeVerificarColisionesMapa() {
        return false;
    }

    public Lado getLado() {
        return LADO;
    }

    @Override
    public void dispose() {
        ANIMACION.dispose();
        ANIMACION_PUNIO.dispose();
        ANIMACION_ABIERTA_ARRIBA.dispose();
        ANIMACION_FUEGO_GOLPE.dispose();
        super.dispose();
    }
}
