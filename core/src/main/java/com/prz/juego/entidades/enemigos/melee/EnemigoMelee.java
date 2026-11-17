package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.red.Servidor;

public abstract class EnemigoMelee extends Enemigo {

    private static final float DURACION_ATAQUE = 1f;
    private static final String SONIDO_ESPADA = "ESPADA";

    protected float anchoAtaque = 100f;
    protected float altoAtaque = 10f;
    protected float distanciaAtaque = 15f;
    protected float tiempoEstado = 0f;
    protected float tiempoAntesDeAtacar = 0.3f;
    protected float tiempoEntreAtaques = 2f;
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
        Jugador jugador,
        Jugador[] jugadores,
        Texture textura) {
        super(x, y, ancho, alto, velocidadX, vida, danio, jugador, jugadores, textura);
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

                if (Servidor.instancia != null) {
                    Servidor.instancia.enviarSonido(SONIDO_ESPADA);
                }
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

    protected Rectangle getBoundsAtaque() {
        float ataqueY = y + alto / 2f - altoAtaque / 2f;
        float centroEnemigo = x + ancho / 2f;
        float ataqueX = direccion > 0 ? centroEnemigo : centroEnemigo - anchoAtaque;

        return new Rectangle(ataqueX, ataqueY, anchoAtaque, altoAtaque);
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
