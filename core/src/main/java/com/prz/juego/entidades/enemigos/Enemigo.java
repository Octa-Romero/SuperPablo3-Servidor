package com.prz.juego.entidades.enemigos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.red.Servidor;

import java.util.ArrayList;

public abstract class Enemigo extends Entidad {

    protected static final int TIPO_REPOSO = 0;
    private static final float VELOCIDAD_SALTO = 300f;
    private static final float DURACION_CAMINATA_PATRULLA = 2f;
    private static final float ESPERA_PATRULLA = 5f;
    private static final double PROBABILIDAD_CAMBIO_DIRECCION = 0.70;
    private static final String SONIDO_SALTO = "SALTO";

    protected int id;
    protected TipoEnemigo tipo;
    protected String sonidoAtaque;
    protected float velocidad;
    protected float distanciaDeteccion = 500f;
    protected float distanciaDeteccionVertical = 150f;
    protected float tiempoPatrulla = 0f;
    protected boolean caminandoPatrulla = false;
    protected boolean atacando = false;
    protected Jugador jugador;
    protected Jugador[] jugadores;
    protected int tipoAnimacion = TIPO_REPOSO;
    protected float tiempoAnimacion = 0f;

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
        this.velocidad = velocidadX;
        this.duracionInvencibilidad = 1f;
        orientarSprite(direccion);
    }

    @Override
    public void update(float delta) {
        if (muerto) {
            return;
        }

        guardarPosicionAnterior();
        actualizarInvencibilidad(delta);

        Jugador jugadorCercano = obtenerJugadorMasCercano();
        if (jugadorCercano != null) {
            Rectangle boundsJugador = jugadorCercano.getBounds();

            if (detectaJugador(calcularDistanciaHorizontal(boundsJugador), boundsJugador)) {
                tiempoPatrulla = 0f;
                caminandoPatrulla = false;
            }

            actualizarComportamiento(delta, boundsJugador);
        } else {
            patrullar(delta);
        }

        actualizarFisica(delta);

        if (y + alto < 0f) {
            morir();
            return;
        }

        tiempoAnimacion += delta;
        actualizarOrientacionSprites();
        actualizarSpriteVisual();
        bounds.setPosition(x, y);
    }

    protected abstract void actualizarComportamiento(float delta, Rectangle boundsJugador);

    protected void reproducirSonidoAtaque() {
        if (sonidoAtaque != null && Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(sonidoAtaque, x, y);
        }
    }

    protected float calcularDistanciaHorizontal(Rectangle boundsJugador) {
        if (boundsJugador.x > x + ancho) {
            return boundsJugador.x - (x + ancho);
        }
        if (x > boundsJugador.x + boundsJugador.width) {
            return x - (boundsJugador.x + boundsJugador.width);
        }
        return 0f;
    }

    protected float calcularDistanciaVertical(Rectangle boundsJugador) {
        if (boundsJugador.y > y + alto) {
            return boundsJugador.y - (y + alto);
        }
        if (y > boundsJugador.y + boundsJugador.height) {
            return y - (boundsJugador.y + boundsJugador.height);
        }
        return 0f;
    }

    protected boolean detectaJugador(float distancia, Rectangle boundsJugador) {
        return distancia <= distanciaDeteccion
            && calcularDistanciaVertical(boundsJugador) <= distanciaDeteccionVertical;
    }

    protected Jugador obtenerJugadorMasCercano() {
        if (jugadores != null) {
            Jugador masCercano = null;
            float distanciaMinima = Float.MAX_VALUE;

            for (Jugador j : jugadores) {
                if (j == null || j.estaMuerto()) {
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

            if (masCercano != null) {
                return masCercano;
            }
        }

        if (jugador != null && !jugador.estaMuerto()) {
            return jugador;
        }
        return null;
    }

    protected Jugador obtenerJugadorAleatorio() {
        if (jugadores != null) {
            ArrayList<Jugador> vivos = new ArrayList<>();
            for (Jugador j : jugadores) {
                if (j != null && !j.estaMuerto()) {
                    vivos.add(j);
                }
            }
            if (!vivos.isEmpty()) {
                return vivos.get((int) (Math.random() * vivos.size()));
            }
        }

        if (jugador != null && !jugador.estaMuerto()) {
            return jugador;
        }
        return null;
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

        if (centroJugador == centroEnemigo) {
            return;
        }

        direccion = centroJugador > centroEnemigo ? 1 : -1;

        float movimiento = Math.min(velocidad * delta, distancia - distanciaMinima);
        if (movimiento > 0f) {
            x += direccion * movimiento;
        }
    }

    protected void patrullar(float delta) {
        tiempoPatrulla += delta;

        if (caminandoPatrulla) {
            x += direccion * velocidad * delta;

            if (tiempoPatrulla >= DURACION_CAMINATA_PATRULLA) {
                caminandoPatrulla = false;
                tiempoPatrulla = 0f;
            }
            return;
        }

        if (tiempoPatrulla >= ESPERA_PATRULLA) {
            tiempoPatrulla = 0f;
            if (Math.random() < PROBABILIDAD_CAMBIO_DIRECCION) {
                direccion *= -1;
            }
            caminandoPatrulla = true;
        }
    }

    protected void actualizarOrientacionSprites() {
        orientarSprite(direccion);
    }

    public boolean estaAtacando() {
        return atacando;
    }

    public boolean puedeRecibirDanio() {
        return true;
    }

    @Override
    public void saltar() {
        velocidadY = VELOCIDAD_SALTO;
        enSuelo = false;

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(SONIDO_SALTO, x, y);
        }
    }

    @Override
    public void dibujar() {
        dibujarSprite();
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public abstract TipoEnemigo getTipo();

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean debeSoltarReliquia() {
        return tipo == TipoEnemigo.MINIJEFE_MELEE || tipo == TipoEnemigo.MINIJEFE_RANGO;
    }

    public int getTipoAnimacion() {
        return tipoAnimacion;
    }

    public float getTiempoAnimacion() {
        return tiempoAnimacion;
    }
}
