package com.prz.juego.entidades.enemigos.rango;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.sistemas.Colisiones;

import java.util.ArrayList;

public abstract class EnemigoRango extends Enemigo {

    public enum Modo {
        ESTATICO,
        MOVIL
    }

    protected float distanciaDisparo = 400;
    protected float distanciaHuida = 300;
    protected float tiempoAntesDeDisparar = 0.5f;
    protected float tiempoEntreDisparos = 3f;
    protected float tiempoEstado = 0;
    protected boolean esperandoDisparo = false;
    protected boolean cooldownDisparo = false;
    protected float velocidadProyectil = 500;
    protected float distanciaMaximaProyectil = 1200;
    protected double vidaProyectil = 1;
    private final Modo MODO;
    private final Texture TEXTURA_PROYECTIL;
    private final float ANCHO_PROYECTIL;
    private final float ALTO_PROYECTIL;
    private ArrayList<Lanzable> proyectiles;
    private final ArrayList<Proyectil> proyectilesPropios = new ArrayList<>();
    private Colisiones colisiones;
    private boolean ataquesCancelados;
    private boolean huyendo = false;
    private boolean esperarAterrizajeAntesDeAtacar;
    private float retrasoInicialAntesDeAtacar;
    private float tiempoRetrasoInicial;

    protected EnemigoRango(
            float x,
            float y,
            float ancho,
            float alto,
            int velocidadX,
            double vida,
            double danio,
            Jugador jugador,
            Texture textura,
            Texture TEXTURA_PROYECTIL,
            float ANCHO_PROYECTIL,
            float ALTO_PROYECTIL,
            Modo MODO) {
        super(x, y, ancho, alto, velocidadX, vida, danio, jugador, textura);
        this.TEXTURA_PROYECTIL = TEXTURA_PROYECTIL;
        this.ANCHO_PROYECTIL = ANCHO_PROYECTIL;
        this.ALTO_PROYECTIL = ALTO_PROYECTIL;
        this.MODO = MODO;
        distanciaDeteccion = 700;
    }

    public void configurarDisparo(Colisiones colisiones, ArrayList<Lanzable> proyectiles) {
        this.colisiones = colisiones;
        this.proyectiles = proyectiles;
    }

    public void configurarRetrasoInicialAntesDeAtacar(float retraso) {
        if (retraso < 0f) {
            throw new IllegalArgumentException("El retraso inicial no puede ser negativo");
        }

        esperarAterrizajeAntesDeAtacar = true;
        retrasoInicialAntesDeAtacar = retraso;
        tiempoRetrasoInicial = 0f;
        enSuelo = false;
    }

    public void configurarDireccionInicial(float direccion) {
        if (direccion == 0f) {
            throw new IllegalArgumentException("La direccion inicial no puede ser cero");
        }

        this.direccion = Math.signum(direccion);
        orientarSprite(sprite, this.direccion);
    }

    @Override
    protected void actualizarComportamiento(float delta, Rectangle boundsJugador) {
        if (ataquesCancelados) {
            return;
        }

        if (esperarAterrizajeAntesDeAtacar) {
            if (!enSuelo) {
                return;
            }

            tiempoRetrasoInicial += delta;
            if (tiempoRetrasoInicial < retrasoInicialAntesDeAtacar) {
                return;
            }
            esperarAterrizajeAntesDeAtacar = false;
        }

        if (cooldownDisparo) {
            tiempoEstado += delta;
            if (tiempoEstado >= tiempoEntreDisparos) {
                cooldownDisparo = false;
                tiempoEstado = 0;
            }
        }

        if (esperandoDisparo) {
            tiempoEstado += delta;
            if (tiempoEstado >= tiempoAntesDeDisparar) {
                disparar();
                esperandoDisparo = false;
                atacando = false;
                cooldownDisparo = true;
                tiempoEstado = 0;
            }
            return;
        }

        float distancia = calcularDistanciaHorizontal(boundsJugador);

        if (!detectaJugador(distancia)) {
            huyendo = false;
            if (MODO == Modo.MOVIL) {
                patrullar(delta);
            }
            return;
        }

        actualizarDireccion(boundsJugador);

        if (!cooldownDisparo) {
            esperandoDisparo = true;
            atacando = true;
            tiempoEstado = 0;
            return;
        }

        if (MODO == Modo.ESTATICO) {
            return;
        }

        if (chocoPared) {
            huyendo = false;
            actualizarDireccion(boundsJugador);
            actualizarOrientacionSprites();
            return;
        }

        if (distancia <= distanciaHuida) {
            huyendo = true;
        }

        if (huyendo) {
            if (distancia >= distanciaDisparo) {
                huyendo = false;
            } else {
                direccion = -direccion;
                alejarse(delta, distancia);
            }
        } else if (distancia > distanciaDisparo) {
            perseguir(delta, boundsJugador, distanciaDisparo);
        }
    }

    private void alejarse(float delta, float distancia) {
        float movimiento = Math.min(velocidad * delta, distanciaDisparo - distancia);
        x += direccion * movimiento;
    }

    private void disparar() {
        if (proyectiles == null) {
            return;
        }

        Rectangle boundsJugador = jugador.getBounds();
        float origenX = x + ancho / 2f;
        float origenY = y + alto / 2f;
        float objetivoX = boundsJugador.x + boundsJugador.width / 2f;
        float objetivoY = boundsJugador.y + boundsJugador.height / 2f;

        Vector2 direccionProyectil = new Vector2(objetivoX - origenX, objetivoY - origenY);
        if (direccionProyectil.isZero()) {
            direccionProyectil.set(direccion, 0);
        }
        direccionProyectil.nor();

        float proyectilX = origenX + direccionProyectil.x * ancho / 2f;
        float proyectilY = origenY + direccionProyectil.y * ancho / 2f;

        proyectilesPropios.removeIf(proyectil -> !proyectil.estaActiva());
        Proyectil proyectil =
                new Proyectil(
                        proyectilX,
                        proyectilY,
                        direccionProyectil,
                        ANCHO_PROYECTIL,
                        ALTO_PROYECTIL,
                        velocidadProyectil,
                        distanciaMaximaProyectil,
                        danio,
                        vidaProyectil,
                        jugador,
                        colisiones,
                        TEXTURA_PROYECTIL);
        proyectiles.add(proyectil);
        proyectilesPropios.add(proyectil);
    }

    public void cancelarAtaques() {
        ataquesCancelados = true;
        esperandoDisparo = false;
        atacando = false;
        for (Proyectil proyectil : proyectilesPropios) {
            proyectil.desactivar();
        }
        proyectilesPropios.clear();
    }

    public Modo getModo() {
        return MODO;
    }
}
