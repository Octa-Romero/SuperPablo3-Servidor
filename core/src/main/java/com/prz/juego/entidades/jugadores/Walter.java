package com.prz.juego.entidades.jugadores;

import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.sistemas.Colisiones;

import java.util.ArrayList;

public class Walter extends Jugador {

    private static final float DURACION_ATAQUE = 0.35f;
    private static final float COOLDOWN_ATAQUE = 0.8f;
    private static final float DISTANCIA_ORBE = 50f;
    private static final float OFFSET_Y_ORBE = 10f;
    private static final double PROBABILIDAD_CRITICO = 0.25;
    private static final double MULTIPLICADOR_CRITICO = 2.0;
    private static final String SONIDO_BOLA_FUEGO = "BOLA_FUEGO";

    private ArrayList<Lanzable> proyectiles;
    private ArrayList<Entidad> entidades;
    private Colisiones colision;
    private float tiempoAtaque = 0f;
    private float tiempoCooldown = 0f;
    private boolean cooldownAtaque = false;

    public Walter(float x, float y) {
        super(
            x,
            y,
            50,
            80,
            140,
            5,
            0.5,
            GestorRecursos.obtenerTextura("Personajes/Walter/walter_idle.png"));

        configurarSpriteVisual(96f, 80f, -5f, -42f, 0f);
    }

    public void configurarAtaque(
        ArrayList<Entidad> entidades,
        Colisiones colision,
        ArrayList<Lanzable> proyectiles) {
        this.entidades = entidades;
        this.colision = colision;
        this.proyectiles = proyectiles;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (atacando) {
            tiempoAtaque += delta;

            if (tiempoAtaque >= DURACION_ATAQUE) {
                atacando = false;
                tiempoAtaque = 0f;
                cooldownAtaque = true;
                tiempoCooldown = 0f;
            }
        }

        if (cooldownAtaque) {
            tiempoCooldown += delta;

            if (tiempoCooldown >= COOLDOWN_ATAQUE) {
                cooldownAtaque = false;
                tiempoCooldown = 0f;
            }
        }
    }

    @Override
    public void atacar() {
        if (atacando || cooldownAtaque) {
            return;
        }

        atacando = true;
        tiempoAtaque = 0f;
        lanzarOrbe();
    }

    private void lanzarOrbe() {
        if (entidades == null || colision == null || proyectiles == null) {
            return;
        }

        float posicionX = direccion > 0 ? x + ancho + DISTANCIA_ORBE : x - DISTANCIA_ORBE;
        float posicionY = y + alto / 2f - OFFSET_Y_ORBE;

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(SONIDO_BOLA_FUEGO, x, y);
        }

        proyectiles.add(
            new BolaFuego(
                posicionX,
                posicionY,
                direccion,
                this,
                colision,
                entidades,
                proyectiles));
    }

    @Override
    public double getDanio() {
        if (Math.random() < PROBABILIDAD_CRITICO) {
            return danio * MULTIPLICADOR_CRITICO;
        }
        return danio;
    }

    @Override
    public Personajes getPersonaje() {
        return Personajes.WALTER;
    }

    @Override
    public void reiniciar(float x, float y) {
        super.reiniciar(x, y);
        tiempoAtaque = 0f;
        tiempoCooldown = 0f;
        cooldownAtaque = false;
    }
}
