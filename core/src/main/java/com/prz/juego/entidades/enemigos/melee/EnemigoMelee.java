package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;

public abstract class EnemigoMelee extends Enemigo {

    private static final float DURACION_ATAQUE = 1f;
    private final Ataque HITBOX_ATAQUE;
    protected float distanciaAtaque = 15f;
    protected float tiempoEstado = 0f;
    protected float tiempoAntesDeAtacar;
    protected float tiempoEntreAtaques;
    protected boolean esperandoAtaque = false;
    protected boolean cooldownAtaque = false;
    protected boolean yaDanio = false;

    protected EnemigoMelee(
        float x,
        float y,
        float ancho,
        float alto,
        int velocidadX,
        double vida,
        double danio,
        Ataque hitboxAtaque,
        float tiempoAntesDeAtacar,
        float tiempoEntreAtaques,
        Jugador jugador,
        Jugador[] jugadores,
        Texture textura) {
        super(x, y, ancho, alto, velocidadX, vida, danio, jugador, jugadores, textura);
        this.HITBOX_ATAQUE = hitboxAtaque;
        this.tiempoAntesDeAtacar = tiempoAntesDeAtacar;
        this.tiempoEntreAtaques = tiempoEntreAtaques;
    }

    @Override
    protected boolean estaPreparandoAtaque() {
        return esperandoAtaque && !atacando;
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        float distancia = calcularDistanciaHorizontal(boundsJugador);
        boolean detectado = detectaJugador(distancia, boundsJugador);

        if (atacando) {
            actualizarAtaque(delta);
            return;
        }

        if (esperandoAtaque) {
            tiempoEstado += delta;

            if (tiempoEstado >= tiempoAntesDeAtacar) {
                esperandoAtaque = false;
                atacando = true;
                tiempoEstado = 0f;
                yaDanio = false;
                reproducirSonidoAtaque();
            }
            return;
        }

        if (cooldownAtaque) {
            tiempoEstado += delta;

            if (tiempoEstado >= tiempoEntreAtaques) {
                cooldownAtaque = false;
                tiempoEstado = 0f;
            }

            perseguirOPatrullar(delta, boundsJugador, detectado);
            return;
        }

        if (detectado && distancia <= distanciaAtaque) {
            actualizarDireccion(boundsJugador);
            esperandoAtaque = true;
            tiempoEstado = 0f;
        } else {
            perseguirOPatrullar(delta, boundsJugador, detectado);
        }
    }

    private void perseguirOPatrullar(float delta, Rectangle boundsJugador, boolean detectado) {
        if (detectado) {
            perseguir(delta, boundsJugador, distanciaAtaque);
        } else {
            patrullar(delta);
        }
    }

    private void actualizarAtaque(float delta) {
        tiempoEstado += delta;

        if (!yaDanio) {
            Rectangle ataque = getBoundsAtaque();
            boolean golpeo = false;

            if (jugadores != null) {
                for (Jugador j : jugadores) {
                    golpeo |= golpear(j, ataque);
                }
            } else {
                golpeo = golpear(jugador, ataque);
            }

            yaDanio = golpeo;
        }

        if (tiempoEstado >= DURACION_ATAQUE) {
            atacando = false;
            cooldownAtaque = true;
            tiempoEstado = 0f;
        }
    }

    private boolean golpear(Jugador objetivo, Rectangle ataque) {
        if (objetivo == null || objetivo.estaMuerto() || !ataque.overlaps(objetivo.getBounds())) {
            return false;
        }

        objetivo.restarVida(danio);
        return true;
    }

    @Override
    public Rectangle getBoundsAtaque() {
        return HITBOX_ATAQUE.calcular(x, y, ancho, direccion);
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        super.dibujarHitbox(shapeRenderer);

        if (atacando) {
            Rectangle ataque = getBoundsAtaque();
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(ataque.x, ataque.y, ataque.width, ataque.height);
        }
    }
}
