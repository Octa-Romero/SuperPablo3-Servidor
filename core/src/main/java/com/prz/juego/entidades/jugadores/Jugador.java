package com.prz.juego.entidades.jugadores;

import com.badlogic.gdx.graphics.Texture;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Entrada;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public abstract class Jugador extends Entidad {

    protected JugadorRed estadoRed;
    private final Texture TEXTURA_HUD;
    protected boolean atacando;
    protected float respawnY = 1000;
    private Colisiones colisiones;
    private boolean trepando = false;
    private boolean accionesHabilitadas = true;
    private float cooldownSaltoRestante = 0f;
    private final float VELOCIDAD_ESCALERA = 150f;
    private final float TIEMPO_COOLDOWN_SALTO = 1f;

    protected Jugador(
            float x,
            float y,
            float ancho,
            float alto,
            int velocidadX,
            double vida,
            double danio,
            Texture textura,
            Texture TEXTURA_HUD) {
        super(x, y, ancho, alto, velocidadX, vida, danio, textura);
        this.TEXTURA_HUD = TEXTURA_HUD;
    }

    @Override
    public void update(float delta) {
        guardarPosicionAnterior();
        cooldownSaltoRestante = Math.max(0f, cooldownSaltoRestante - delta);

        boolean dentroEscalera = colisiones != null && colisiones.estaEnEscalera(this);

        if (!dentroEscalera) {
            trepando = false;
        } else if (estadoRed.mueveArriba() || estadoRed.mueveAbajo()) {
            trepando = true;
        }

        if (trepando && dentroEscalera) {
            enSuelo = false;

            if (estadoRed.mueveArriba()) {
                velocidadY = VELOCIDAD_ESCALERA;
                y += VELOCIDAD_ESCALERA * delta;
            } else if (estadoRed.mueveAbajo()) {
                velocidadY = -VELOCIDAD_ESCALERA;
                y -= VELOCIDAD_ESCALERA * delta;
            } else {
                velocidadY = 0;
            }

            bounds.setPosition(x, y);

        } else {
            if (accionesHabilitadas && estadoRed.mueveArriba() && enSuelo) {
                saltar();
            }

            enSuelo = false;
            actualizarFisica(delta);
        }

        if (y + alto < 0) {
            restarVida(1);
            setY(respawnY);
            velocidadY = 0;
            trepando = false;
        }

        if (estadoRed.mueveIzquierda()) {
            x -= velocidadX * delta;
        }

        if (estadoRed.mueveDerecha()) {
            x += velocidadX * delta;
        }

        if (accionesHabilitadas && estadoRed.ataca()) {
            atacar();
        }

        ANIMACION.actualizar(
                sprite,
                delta,
                x != xAnterior || (trepando && y != yAnterior),
                !enSuelo && !trepando,
                atacando);

        actualizarOrientacionSegunMovimiento();
        actualizarSpriteVisual();

        bounds.setPosition(x, y);

        actualizarInvencibilidad(delta);
    }

    public abstract void atacar();

    public void setAccionesHabilitadas(boolean habilitadas) {
        accionesHabilitadas = habilitadas;
        if (!habilitadas) {
            atacando = false;
        }
    }

    protected boolean accionesHabilitadas() {
        return accionesHabilitadas;
    }

    public void saltar() {
        if (!accionesHabilitadas || cooldownSaltoRestante > 0f) {
            return;
        }
        velocidadY = 300;
        enSuelo = false;
        cooldownSaltoRestante = TIEMPO_COOLDOWN_SALTO;
        Sonido.SALTO.sonar();
    }

    public void dibujar() {
        if (spriteVisible && sprite != null) {
            sprite.draw(Render.batch);
        }
    }

    public Texture getTEXTURA_HUD() {
        return TEXTURA_HUD;
    }

    public boolean estaAtacando() {
        return atacando;
    }

    public abstract Personajes getPersonaje();

    public void setEstadoRed(JugadorRed estado)
    {
        this.estadoRed = estado;
    }

    public JugadorRed getEstadoRed()
    {
        return this.estadoRed;
    }

    public void setColisiones(Colisiones colisiones) {
        this.colisiones = colisiones;
    }

    public boolean estaTrepando() {
        return trepando;
    }
}
