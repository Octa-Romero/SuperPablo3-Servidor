package com.prz.juego.entidades.jugadores;

import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;

public class Walter extends Jugador {

    private static final float DURACION_ATAQUE = 0.35f;
    private float tiempoAtaque;
    private static final float COOLDOWN_ATAQUE = 0.8f;
    private float tiempoCooldown;
    private boolean cooldownAtaque;
    private ArrayList<Lanzable> proyectiles;
    private ArrayList<Entidad> entidades;
    private Colisiones colision;

    public Walter(float x, float y) {
        super(
                x,
                y,
                50,
                80,
                140,
                5,
                0.5,
                GestorRecursos.obtenerTextura("Personajes/Walter/walter.png"),
                GestorRecursos.obtenerTextura("Hud/walter_hud.png"));
        ANIMACION.configurar(
                GestorRecursos.obtenerTextura("Personajes/Walter/walter_spritesheet.png"),
                540,
                450,
                new int[] {0},
                new int[] {1, 2, 3, 4, 5, 3},
                null,
                new int[] {6},
                0.12f);
        configurarSpriteVisual(96f, 80f, 0f, -46f, 0f);
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

        if (atacando && (tiempoAtaque += delta) >= DURACION_ATAQUE) {
            atacando = false;
            tiempoAtaque = 0f;
            cooldownAtaque = true;
            tiempoCooldown = 0f;
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
        if (!atacando && !cooldownAtaque) {
            atacando = true;
            tiempoAtaque = 0f;
            lanzarOrbe();
        }
    }

    private void lanzarOrbe() {
        if (entidades == null || colision == null || proyectiles == null) {
            return;
        }
        float direccion = mirandoDerecha ? 1f : -1f;
        float posicionX;
        if (direccion > 0) {
            posicionX = x + ancho + 50f;
        } else {
            posicionX = x - 50f;
        }
        float posicionY = y + alto / 2f - 10f;

        Sonido.BOLA_FUEGO.sonar();
        proyectiles.add(
                new BolaFuego(
                        posicionX, posicionY, direccion, this, colision, entidades, proyectiles));
    }

    @Override
    public double getDanio() {
        if (Math.random() < 0.25) {
            return danio * 2;
        }
        return danio;
    }
}
