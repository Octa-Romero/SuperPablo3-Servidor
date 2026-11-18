package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Frames;
import com.prz.juego.utilidades.Render;

public class Mano extends Enemigo {

    public enum Lado {
        IZQUIERDA,
        DERECHA
    }

    public enum EstadoAtaque {
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

    public enum EstiloAtaque {
        NORMAL,
        PUNIO,
        PALMA
    }

    private static final float ANCHO = 120f;
    private static final float ALTO = 100f;
    private static final float SEPARACION = 10f;
    private static final double VIDA = 20;
    private static final double DANIO = 1;

    private static final float ALTURA_LEVANTADA = 300f;
    private static final float VELOCIDAD_LEVANTAR = 500f;
    private static final float VELOCIDAD_BAJAR = 1800f;
    private static final float VELOCIDAD_VOLVER = 600f;
    private static final float VELOCIDAD_RAPIDO_VOLVER = 1200f;
    private static final float TIEMPO_DESPUES_DESAPARECER = 0.5f;
    private static final float TIEMPO_RAPIDO_DESPUES_DESAPARECER = 0.2f;
    private static final float TIEMPO_RETORNO_IDLE_DESAPARECIDO = 0.65f;
    private static final float DURACION_ESCALA_TELETRANSPORTE = 0.15f;
    private static final float ALTURA_TELETRANSPORTE = 600f;
    private static final float ALTURA_REPOSO = 120f;

    private static final float TIEMPO_SEGUIR_JUGADOR = 1f;
    private static final float TIEMPO_RAPIDO_SEGUIR_JUGADOR = 0.4f;
    private static final float TIEMPO_ESPERA_GOLPE = 0.8f;
    private static final float TIEMPO_RAPIDO_ESPERA_GOLPE = 0.3f;
    private static final float TIEMPO_RECUPERACION = 0.4f;
    private static final float TIEMPO_RAPIDO_RECUPERACION = 0.15f;
    private static final float DURACION_HITBOX_EXTENDIDA = 0.2f;
    private static final float ANCHO_FUEGO_GOLPE = 220f;
    private static final float ALTO_FUEGO_GOLPE = ALTO;
    private static final float ANCHO_SPRITE = 170f;
    private static final float ALTO_SPRITE = 170f;
    private static final float ANCHO_SPRITE_EXTENDIDO = 200f;
    private static final float ALTO_SPRITE_EXTENDIDO = 200f;
    private static final float AMPLITUD_MOVIMIENTO_IDLE = 6f;
    private static final float FRECUENCIA_MOVIMIENTO_IDLE = 2.5f;
    private static final int CANTIDAD_FRAMES = 3;
    private static final int CANTIDAD_FRAMES_FUEGO = 2;

    private final Retumbar RETUMBAR;
    private final Lado LADO;
    private final TextureRegion REGION_ABAJO;
    private final TextureRegion REGION_ARRIBA;
    private final TextureRegion REGION_PUNIO;
    private final Sprite SPRITE_FUEGO_GOLPE;
    private final Rectangle BOUNDS_FUEGO_GOLPE = new Rectangle();

    private EstadoAtaque estadoAtaque = EstadoAtaque.NINGUNO;
    private EstiloAtaque estiloAtaque = EstiloAtaque.NORMAL;
    private boolean haciendoDanio = false;
    private boolean teletransportada = false;
    private boolean teletransporteRapido = false;
    private boolean volverAIdle = false;
    private boolean visible = true;
    private float tiempoHitboxExtendida = 0f;
    private float tiempoEstado = 0f;
    private float escalaTeletransporte = 1f;
    private float yAtaque;
    private float yObjetivo;
    private float yDestino;
    private float velocidadMovimiento;
    private float tiempoMovimientoIdle = 0f;
    private float desfaseMovimientoVertical = 0f;
    private Jugador objetivoMano;

    public Mano(Retumbar retumbar, Lado lado, Jugador jugador, Jugador[] jugadores) {
        super(
            0,
            retumbar.getY(),
            ANCHO,
            ALTO,
            0,
            VIDA,
            DANIO,
            jugador,
            jugadores,
            GestorRecursos.obtenerTextura("Jefe/manoAbajo.png"));
        this.RETUMBAR = retumbar;
        this.LADO = lado;

        REGION_ABAJO = Frames.primero(sprite.getTexture(), CANTIDAD_FRAMES);
        REGION_ARRIBA =
            Frames.primero(GestorRecursos.obtenerTextura("Jefe/manoArriba.png"), CANTIDAD_FRAMES);
        REGION_PUNIO =
            Frames.primero(GestorRecursos.obtenerTextura("Jefe/punio.png"), CANTIDAD_FRAMES);
        SPRITE_FUEGO_GOLPE =
            new Sprite(
                Frames.primero(
                    GestorRecursos.obtenerTextura("Jefe/fuegoSuelo.png"),
                    CANTIDAD_FRAMES_FUEGO));
        SPRITE_FUEGO_GOLPE.setFlip(lado == Lado.IZQUIERDA, false);

        duracionInvencibilidad = 0f;
        float offsetX = (ANCHO - ANCHO_SPRITE) / 2f;
        configurarSpriteVisual(
            ANCHO_SPRITE, ALTO_SPRITE, offsetX, offsetX, (ALTO - ALTO_SPRITE) / 2f);
        seguirJefe();
        actualizarTexturaSegunEstado();
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);
        tiempoMovimientoIdle += delta;
        tiempoAnimacion += delta;
        actualizarComportamiento(delta, null);

        if (estadoAtaque == EstadoAtaque.NINGUNO) {
            desfaseMovimientoVertical =
                MathUtils.sin(tiempoMovimientoIdle * FRECUENCIA_MOVIMIENTO_IDLE)
                    * AMPLITUD_MOVIMIENTO_IDLE;
            y = RETUMBAR.getY() + ALTURA_REPOSO + desfaseMovimientoVertical;
        } else {
            desfaseMovimientoVertical = 0f;
        }

        bounds.setPosition(x, y);
        actualizarTexturaSegunEstado();

        if (tiempoHitboxExtendida > 0f) {
            tiempoHitboxExtendida -= delta;
        }
        actualizarBoundsFuegoGolpe();
        actualizarSpriteVisual();

        if (haciendoDanio || tiempoHitboxExtendida > 0f) {
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
                    reproducirSonido("GOLPE");
                    estadoAtaque = EstadoAtaque.RECUPERANDO;
                    tiempoEstado =
                        teletransporteRapido
                            ? TIEMPO_RAPIDO_RECUPERACION
                            : TIEMPO_RECUPERACION;
                }
                break;

            case RECUPERANDO:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0f) {
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
                escalaTeletransporte =
                    Math.max(0f, escalaTeletransporte - delta / DURACION_ESCALA_TELETRANSPORTE);
                if (escalaTeletransporte <= 0f) {
                    visible = false;
                    estadoAtaque = EstadoAtaque.ESPERANDO_REAPARICION;
                    reproducirSonido("TP1");
                    tiempoEstado = obtenerTiempoDesaparecida();
                }
                break;

            case ESPERANDO_REAPARICION:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0f) {
                    reaparecer();
                }
                break;

            case REAPARECIENDO:
                if (!volverAIdle && objetivoMano != null) {
                    posicionarSobreObjetivo();
                    y = ALTURA_TELETRANSPORTE;
                }
                escalaTeletransporte =
                    Math.min(1f, escalaTeletransporte + delta / DURACION_ESCALA_TELETRANSPORTE);
                if (escalaTeletransporte >= 1f) {
                    terminarReaparicion();
                }
                break;

            case SIGUIENDO_JUGADOR:
                tiempoEstado -= delta;
                if (objetivoMano != null) {
                    posicionarSobreObjetivo();
                }
                if (tiempoEstado <= 0f) {
                    estadoAtaque = EstadoAtaque.ESPERANDO_GOLPE;
                    tiempoEstado =
                        teletransporteRapido
                            ? TIEMPO_RAPIDO_ESPERA_GOLPE
                            : TIEMPO_ESPERA_GOLPE;
                }
                break;

            case ESPERANDO_GOLPE:
                tiempoEstado -= delta;
                if (tiempoEstado <= 0f) {
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

    private float obtenerTiempoDesaparecida() {
        if (!teletransporteRapido) {
            return TIEMPO_DESPUES_DESAPARECER;
        }
        return volverAIdle ? TIEMPO_RETORNO_IDLE_DESAPARECIDO : TIEMPO_RAPIDO_DESPUES_DESAPARECER;
    }

    private void reaparecer() {
        objetivoMano = volverAIdle ? null : obtenerJugadorAleatorio();

        if (objetivoMano != null) {
            posicionarSobreObjetivo();
            y = ALTURA_TELETRANSPORTE;
            estiloAtaque = EstiloAtaque.PALMA;
        } else {
            seguirJefeEnX();
            y = RETUMBAR.getY() + ALTURA_REPOSO;
            estiloAtaque = EstiloAtaque.NORMAL;
        }

        escalaTeletransporte = 0f;
        visible = true;
        reproducirSonido("TP2");
        estadoAtaque = EstadoAtaque.REAPARECIENDO;
    }

    private void terminarReaparicion() {
        if (volverAIdle) {
            volverAIdle = false;
            teletransportada = false;
            teletransporteRapido = false;
            estiloAtaque = EstiloAtaque.NORMAL;
            estadoAtaque = EstadoAtaque.NINGUNO;
        } else {
            estadoAtaque = EstadoAtaque.SIGUIENDO_JUGADOR;
            tiempoEstado =
                teletransporteRapido ? TIEMPO_RAPIDO_SEGUIR_JUGADOR : TIEMPO_SEGUIR_JUGADOR;
        }
    }

    private void posicionarSobreObjetivo() {
        x = objetivoMano.getX() + objetivoMano.getAncho() / 2f - ANCHO / 2f;
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

    private void actualizarTexturaSegunEstado() {
        if (estadoAtaque == EstadoAtaque.NINGUNO) {
            estiloAtaque = EstiloAtaque.NORMAL;
        }

        boolean punio =
            estiloAtaque == EstiloAtaque.PUNIO
                || (estiloAtaque == EstiloAtaque.NORMAL && tiempoHitboxExtendida > 0f);

        if (punio) {
            mostrarRegion(REGION_PUNIO, false);
        } else if (estiloAtaque == EstiloAtaque.PALMA) {
            mostrarRegion(REGION_ABAJO, true);
        } else {
            switch (estadoAtaque) {
                case LEVANTANDO:
                case MOVIENDO:
                case EN_POSICION:
                    mostrarRegion(REGION_ARRIBA, true);
                    break;
                case RECUPERANDO:
                    mostrarRegion(REGION_PUNIO, false);
                    break;
                default:
                    mostrarRegion(REGION_ABAJO, true);
                    break;
            }
        }
    }

    private void mostrarRegion(TextureRegion region, boolean extendido) {
        sprite.setRegion(region);
        sprite.setFlip(LADO == Lado.DERECHA, false);

        float anchoNuevo = extendido ? ANCHO_SPRITE_EXTENDIDO : ANCHO_SPRITE;
        float altoNuevo = extendido ? ALTO_SPRITE_EXTENDIDO : ALTO_SPRITE;

        if (anchoSprite != anchoNuevo || altoSprite != altoNuevo) {
            float offsetX = (ANCHO - anchoNuevo) / 2f;
            configurarSpriteVisual(
                anchoNuevo, altoNuevo, offsetX, offsetX, (ALTO - ALTO_SPRITE) / 2f);
        }
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
        if (muerto) {
            return;
        }

        if (spriteVisible && visible && tiempoHitboxExtendida > 0f) {
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
        for (Jugador j : jugadores) {
            if (j.estaMuerto()) {
                continue;
            }

            boolean golpeaConMano = haciendoDanio && bounds.overlaps(j.getBounds());
            boolean golpeaConFuego =
                tiempoHitboxExtendida > 0f && BOUNDS_FUEGO_GOLPE.overlaps(j.getBounds());

            if (golpeaConMano || golpeaConFuego) {
                j.restarVida(danio);
            }
        }
    }

    private void actualizarBoundsFuegoGolpe() {
        BOUNDS_FUEGO_GOLPE.set(
            x + (ANCHO - ANCHO_FUEGO_GOLPE) / 2f,
            y + (ALTO - ALTO_FUEGO_GOLPE) / 2f,
            ANCHO_FUEGO_GOLPE,
            ALTO_FUEGO_GOLPE);
    }

    private void reproducirSonido(String nombre) {
        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(nombre, x, y);
        }
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        if (muerto) {
            return;
        }

        if (tiempoHitboxExtendida > 0f) {
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

    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.JEFE;
    }

    private void eliminarDelEscenario() {
        muerto = true;
        visible = false;
        spriteVisible = false;
        haciendoDanio = false;
        tiempoHitboxExtendida = 0f;
        estadoAtaque = EstadoAtaque.NINGUNO;
        estiloAtaque = EstiloAtaque.NORMAL;
        escalaTeletransporte = 0f;
        velocidadY = 0f;
        bounds.set(0f, 0f, 0f, 0f);
        BOUNDS_FUEGO_GOLPE.set(0f, 0f, 0f, 0f);
        if (sprite != null) {
            sprite.setAlpha(0f);
            sprite.setScale(0f);
        }
    }

    void desactivarPorMuerteJefe() {
        eliminarDelEscenario();
    }

    @Override
    public void restarVida(double cantidad) {
        if (muerto || !visible) {
            return;
        }
        if (vida - cantidad > 0) {
            reproducirSonido("RETUMBAR_GOLPE_RECIBIDO");
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
        return muerto || estadoAtaque == EstadoAtaque.NINGUNO;
    }

    public boolean estaEnAtaque() {
        return !muerto && estadoAtaque != EstadoAtaque.NINGUNO;
    }

    public EstadoAtaque getEstadoAtaque() {
        return estadoAtaque;
    }

    public EstiloAtaque getEstiloAtaque() {
        return estiloAtaque;
    }

    public boolean estaVisible() {
        return visible;
    }

    public float getEscalaSprite() {
        return escalaTeletransporte;
    }

    public boolean mostrarFuegoGolpe() {
        return tiempoHitboxExtendida > 0f;
    }

    public Lado getLado() {
        return LADO;
    }

    @Override
    public void saltar() {
    }

    @Override
    protected void morir() {
        super.morir();
        reproducirSonido("RETUMBAR_MANO_ROTA");
        eliminarDelEscenario();
    }

    @Override
    public boolean debeVerificarColisionesMapa() {
        return false;
    }
}
