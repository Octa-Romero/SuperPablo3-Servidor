package com.prz.juego.entidades.enemigos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.utilidades.Render;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;

public abstract class Enemigo extends Entidad {

    protected int id;
    protected TipoEnemigo tipo;
    protected float velocidad;
    protected float distanciaDeteccion = 500;
    protected float distanciaDeteccionVertical = 150;
    protected float tiempoPatrulla = 0;
    protected boolean caminandoPatrulla = false;
    protected boolean atacando = false;
    protected Jugador jugador;
    protected Jugador[] jugadores;
    protected boolean sueltaReliquia = false;

    protected Enemigo(
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
        super(x, y, ancho, alto, velocidadX, vida, danio, textura);
        this.jugador = jugador;
        this.jugadores = jugadores;
        velocidad = velocidadX;
        duracionInvencibilidad = 1f;
        orientarSprite(sprite, direccion);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        guardarPosicionAnterior();

        actualizarInvencibilidad(delta);

        Jugador jugadorCercano = obtenerJugadorMasCercano();
        Rectangle boundsJugador = jugadorCercano.getBounds();

        if (detectaJugador(calcularDistanciaHorizontal(boundsJugador), boundsJugador)) {
            tiempoPatrulla = 0;
            caminandoPatrulla = false;
        }

        actualizarComportamiento(delta, boundsJugador);

        actualizarFisica(delta);

        if (y + alto < 0) {
            morir();
            return;
        }

        boolean saltandoTemp = !enSuelo;
        boolean saltando = saltandoTemp && velocidadY > 0;
        boolean cayendo = saltandoTemp && velocidadY <= 0;
        ANIMACION.actualizar(sprite, delta, x != xAnterior, saltando, cayendo, atacando);
        actualizarOrientacionSprites();
        actualizarSpriteVisual();
        bounds.setPosition(x, y);
    }

    protected abstract void actualizarComportamiento(float delta, Rectangle boundsJugador);

    protected float calcularDistanciaHorizontal(Rectangle boundsJugador) {
        // Jugador a la derecha
        if (boundsJugador.x > x + ancho) {
            return boundsJugador.x - (x + ancho);
        }

        // Jugador a la izquierda
        if (x > boundsJugador.x + boundsJugador.width) {
            return x - (boundsJugador.x + boundsJugador.width);
        }

        // Se superponen
        return 0;
    }

    protected float calcularDistanciaVertical(Rectangle boundsJugador) {
        if (boundsJugador.y > y + alto) {
            return boundsJugador.y - (y + alto);
        }

        if (y > boundsJugador.y + boundsJugador.height) {
            return y - (boundsJugador.y + boundsJugador.height);
        }

        return 0;
    }

    protected boolean detectaJugador(float distancia, Rectangle boundsJugador) {
        return distancia <= distanciaDeteccion
                && calcularDistanciaVertical(boundsJugador) <= distanciaDeteccionVertical;
    }

    protected Jugador obtenerJugadorMasCercano() {
        if (jugadores == null || jugadores.length == 0) {
            return jugador;
        }
        Jugador masCercano = null;
        float distanciaMinima = Float.MAX_VALUE;
        for (Jugador j : jugadores) {
            if (j.estaMuerto()) {
                continue;
            }
            float dx = j.getX() + j.getAncho() / 2f - (x + ancho / 2f);
            float dy = j.getY() + j.getAlto() / 2f - (y + alto / 2f);
            float distancia = dx * dx + dy * dy;
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercano = j;
            }
        }
        if (masCercano == null) {
            return jugador;
        }
        return masCercano;
    }

    protected Jugador obtenerJugadorAleatorio() {
        if (jugadores == null || jugadores.length == 0) {
            return jugador;
        }
        ArrayList<Jugador> vivos = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (!j.estaMuerto()) {
                vivos.add(j);
            }
        }
        if (vivos.isEmpty()) {
            return jugador;
        }
        return vivos.get((int) (Math.random() * vivos.size()));
    }

    protected void actualizarDireccion(Rectangle boundsJugador) {
        float centroJugador = boundsJugador.x + boundsJugador.width / 2f;
        float centroEnemigo = x + ancho / 2f;
        if (centroJugador > centroEnemigo) {
            direccion = 1;
        } else if (centroJugador < centroEnemigo) {
            direccion = -1;
        }
    }

    protected void perseguir(float delta, Rectangle boundsJugador, float distanciaMinima) {
        float distancia = calcularDistanciaHorizontal(boundsJugador);
        if (distancia <= distanciaMinima) {
            return;
        }
        float centroJugador = boundsJugador.x + boundsJugador.width / 2f;
        float centroEnemigo = x + ancho / 2f;
        float movimiento = velocidad * delta;

        // Jugador a la derecha
        if (centroJugador > centroEnemigo) {
            direccion = 1;
            if (distancia - movimiento < distanciaMinima) {
                movimiento = distancia - distanciaMinima;
            }
            if (movimiento > 0) {
                x += movimiento;
            }
        }

        // Jugador a la izquierda
        else if (centroJugador < centroEnemigo) {
            direccion = -1;
            if (distancia - movimiento < distanciaMinima) {
                movimiento = distancia - distanciaMinima;
            }
            if (movimiento > 0) {
                x -= movimiento;
            }
        }
    }

    protected void patrullar(float delta) {
        tiempoPatrulla += delta;
        if (caminandoPatrulla) {
            x += direccion * velocidad * delta;
            if (tiempoPatrulla >= 2f) {
                caminandoPatrulla = false;
                tiempoPatrulla = 0;
            }
            return;
        }

        if (tiempoPatrulla >= 5f) {
            tiempoPatrulla = 0;
            if (Math.random() < 0.70) { // 70% cambia de dirección, 30% continúa
                direccion *= -1;
            }
            caminandoPatrulla = true;
        }
    }

    protected void actualizarOrientacionSprites() {
        orientarSprite(sprite, direccion);
    }

    public boolean estaAtacando() {
        return atacando;
    }

    public boolean puedeRecibirDanio() {
        return true;
    }

    public void saltar() {
        velocidadY = 300;
        enSuelo = false;
        Servidor.instancia.enviarSonidoConPosicion("SALTO", x, y);
    }

    @Override
    public void dibujar() {
        if (spriteVisible) {
            sprite.draw(Render.batch);
        }
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public abstract TipoEnemigo getTipo();


    public void setSueltaReliquia(boolean valor) {
        this.sueltaReliquia = valor;
    }

    public boolean sueltaReliquia() {
        return sueltaReliquia;
    }

    public int getTipoAnimacion() {
        return ANIMACION.getTipoActual();
    }

    public float getTiempoAnimacion() {
        return ANIMACION.getTiempo();
    }
}
