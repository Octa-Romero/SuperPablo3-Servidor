package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.utilidades.Sonido;

public abstract class EnemigoMelee extends Enemigo {

    protected float anchoAtaque = 100;
    protected float altoAtaque = 10;
    protected float distanciaAtaque = 15;
    protected float tiempoEstado = 0;
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
            Texture textura) {
        super(x, y, ancho, alto, velocidadX, vida, danio, jugador, textura);
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        float distancia = calcularDistanciaHorizontal(boundsJugador);

        if (atacando) {
            actualizarAtaque(delta);
        } else if (esperandoAtaque) {
            tiempoEstado += delta;
            if (tiempoEstado >= tiempoAntesDeAtacar) {
                esperandoAtaque = false;
                atacando = true;
                tiempoEstado = 0;
                yaDanio = false;
                Sonido.ESPADA.sonar();
            }
        } else if (cooldownAtaque) {
            tiempoEstado += delta;
            if (tiempoEstado >= tiempoEntreAtaques) {
                cooldownAtaque = false;
                tiempoEstado = 0;
            }

            if (detectaJugador(distancia)) {
                perseguir(delta, boundsJugador, distanciaAtaque);
            } else {
                patrullar(delta);
            }
        } else {
            if (distancia <= distanciaAtaque && detectaJugador(distancia)) {
                actualizarDireccion(boundsJugador);
                esperandoAtaque = true;
                tiempoEstado = 0;
            } else if (detectaJugador(distancia)) {
                perseguir(delta, boundsJugador, distanciaAtaque);
            } else {
                patrullar(delta);
            }
        }
    }

    private void actualizarAtaque(float delta) {
        tiempoEstado += delta;
        if (!yaDanio && ataqueTocaJugador()) {
            jugador.restarVida(danio);
            yaDanio = true;
        }

        if (tiempoEstado >= 1f) {
            atacando = false;
            cooldownAtaque = true;
            tiempoEstado = 0;
        }
    }

    protected Rectangle getBoundsAtaque() {
        float ataqueY = y + (alto / 2f) - (altoAtaque / 2f);
        float centroEnemigo = x + ancho / 2f;
        float ataqueX;

        if (direccion > 0) {
            ataqueX = centroEnemigo;
        } else {
            ataqueX = centroEnemigo - anchoAtaque;
        }

        return new Rectangle(ataqueX, ataqueY, anchoAtaque, altoAtaque);
    }

    private boolean ataqueTocaJugador() {
        Rectangle ataque = getBoundsAtaque();
        return ataque.overlaps(jugador.getBounds());
    }

    @Override
    protected void actualizarOrientacionSprites() {
        super.actualizarOrientacionSprites();
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

    public Rectangle getBoundsAtaquePublico() {
        return getBoundsAtaque();
    }
}
