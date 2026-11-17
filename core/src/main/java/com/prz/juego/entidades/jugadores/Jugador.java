package com.prz.juego.entidades.jugadores;

import com.badlogic.gdx.graphics.Texture;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.red.JugadorRed;
import com.prz.juego.red.Servidor;
import com.prz.juego.sistemas.Colisiones;

public abstract class Jugador extends Entidad {

    public static final int TIPO_REPOSO = 0;
    public static final int TIPO_CAMINAR = 1;
    public static final int TIPO_SALTO = 2;
    public static final int TIPO_ATAQUE = 3;
    public static final int TIPO_CAIDA = 4;

    private static final float VELOCIDAD_ESCALERA = 150f;
    private static final float VELOCIDAD_SALTO = 300f;
    private static final float TIEMPO_COOLDOWN_SALTO = 1f;
    private static final String SONIDO_SALTO = "SALTO";

    protected JugadorRed estadoRed;
    protected boolean atacando;
    protected float respawnY = 1000f;

    private Colisiones colisiones;
    private boolean trepando = false;
    private boolean accionesHabilitadas = true;
    private float cooldownSaltoRestante = 0f;
    private int tipoAnimacion = TIPO_REPOSO;
    private float tiempoAnimacion = 0f;

    protected Jugador(
        float x,
        float y,
        float ancho,
        float alto,
        int velocidadX,
        double vida,
        double danio,
        Texture textura) {
        super(x, y, ancho, alto, velocidadX, vida, danio, textura);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            guardarPosicionAnterior();
            actualizarAnimacion(delta, false, false, false, false);
            actualizarSpriteVisual();
            bounds.setPosition(x, y);
            actualizarInvencibilidad(delta);
            return;
        }

        if (estadoRed == null) {
            return;
        }

        if (estadoRed.solicitaSeleccion()) {
            velocidadY = 0f;
            guardarPosicionAnterior();
            forzarIdle();
            actualizarOrientacionSegunMovimiento();
            actualizarSpriteVisual();
            return;
        }

        guardarPosicionAnterior();
        cooldownSaltoRestante = Math.max(0f, cooldownSaltoRestante - delta);

        boolean fueEnSuelo = enSuelo;
        boolean dentroEscalera = colisiones != null && colisiones.estaEnEscalera(this);

        if (!dentroEscalera) {
            trepando = false;
        } else if (estadoRed.mueveArriba() || estadoRed.mueveAbajo()) {
            trepando = true;
        }

        if (trepando) {
            actualizarTrepado(delta);
        } else {
            if (accionesHabilitadas && estadoRed.mueveArriba() && enSuelo) {
                saltar();
            }
            enSuelo = false;
            actualizarFisica(delta);
        }

        verificarCaida();

        if (estadoRed.mueveIzquierda()) {
            x -= velocidadX * delta;
        }
        if (estadoRed.mueveDerecha()) {
            x += velocidadX * delta;
        }

        if (accionesHabilitadas && estadoRed.ataca()) {
            atacar();
        }

        boolean enAire = !fueEnSuelo && !trepando;
        boolean caminando = x != xAnterior || (trepando && y != yAnterior);
        actualizarAnimacion(
            delta, caminando, enAire && velocidadY > 0f, enAire && velocidadY <= 0f, atacando);

        actualizarOrientacionSegunMovimiento();
        actualizarSpriteVisual();
        bounds.setPosition(x, y);
        actualizarInvencibilidad(delta);
    }

    private void actualizarTrepado(float delta) {
        enSuelo = false;

        if (estadoRed.mueveArriba()) {
            velocidadY = VELOCIDAD_ESCALERA;
        } else if (estadoRed.mueveAbajo()) {
            velocidadY = -VELOCIDAD_ESCALERA;
        } else {
            velocidadY = 0f;
        }

        y += velocidadY * delta;
        bounds.setPosition(x, y);
    }

    private void verificarCaida() {
        if (y + alto >= 0f) {
            return;
        }

        restarVida(1);
        velocidadY = 0f;
        trepando = false;

        if (!muerto) {
            setY(respawnY);
        }
    }

    private void actualizarAnimacion(
        float delta,
        boolean caminando,
        boolean saltando,
        boolean cayendo,
        boolean atacandoAhora) {
        int siguiente;

        if (atacandoAhora) {
            siguiente = TIPO_ATAQUE;
        } else if (saltando) {
            siguiente = TIPO_SALTO;
        } else if (cayendo) {
            siguiente = TIPO_CAIDA;
        } else if (caminando) {
            siguiente = TIPO_CAMINAR;
        } else {
            siguiente = TIPO_REPOSO;
        }

        if (siguiente != tipoAnimacion) {
            tipoAnimacion = siguiente;
            tiempoAnimacion = 0f;
        } else {
            tiempoAnimacion += delta;
        }
    }

    public abstract void atacar();

    public abstract Personajes getPersonaje();

    public void setAccionesHabilitadas(boolean habilitadas) {
        accionesHabilitadas = habilitadas;
        if (!habilitadas) {
            atacando = false;
        }
    }

    protected boolean accionesHabilitadas() {
        return accionesHabilitadas;
    }

    @Override
    public void saltar() {
        if (!accionesHabilitadas || cooldownSaltoRestante > 0f) {
            return;
        }

        velocidadY = VELOCIDAD_SALTO;
        enSuelo = false;
        cooldownSaltoRestante = TIEMPO_COOLDOWN_SALTO;

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(SONIDO_SALTO, x, y);
        }
    }

    @Override
    public void dibujar() {
        if (!muerto) {
            dibujarSprite();
        }
    }

    public boolean estaAtacando() {
        return atacando;
    }

    public void setEstadoRed(JugadorRed estado) {
        this.estadoRed = estado;
    }

    public JugadorRed getEstadoRed() {
        return estadoRed;
    }

    public void setColisiones(Colisiones colisiones) {
        this.colisiones = colisiones;
    }

    public boolean estaTrepando() {
        return trepando;
    }

    public int getTipoAnimacion() {
        return tipoAnimacion;
    }

    public float getTiempoAnimacion() {
        return tiempoAnimacion;
    }

    public void forzarIdle() {
        tipoAnimacion = TIPO_REPOSO;
        tiempoAnimacion = 0f;
    }

    public void reiniciar(float x, float y) {
        muerto = false;
        spriteVisible = true;
        vida = vidaMaxima;
        velocidadY = 0f;
        enSuelo = true;
        trepando = false;
        atacando = false;
        accionesHabilitadas = true;
        tiempoInvencibilidad = 0f;
        tiempoParpadeo = 0f;
        cooldownSaltoRestante = 0f;
        this.x = x;
        this.y = y;
        xAnterior = x;
        yAnterior = y;
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
        forzarIdle();
    }
}
