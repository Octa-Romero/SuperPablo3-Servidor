package com.prz.juego.entidades.enemigos.melee;

import com.prz.juego.entidades.enemigos.Enemigos;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class Cultista extends EnemigoMelee {

    private static final float ANCHO = 90f;
    private static final float ALTO = 150f;
    private static final int VELOCIDAD = 50;
    private static final double VIDA = 10;
    private static final double DANIO = 1;
    private static final Ataque ATAQUE = new Ataque(80f, 80f, -50f, 0f);
    private static final float TIEMPO_ANTES_DE_ATACAR = 0.8f;
    private static final float TIEMPO_ENTRE_ATAQUES = 2f;
    private static final float ESCALA_MINIJEFE = 1.5f;
    private static final double VIDA_MINIJEFE = 25;
    private static final double DANIO_MINIJEFE = 2;
    private static final Ataque ATAQUE_MINIJEFE = new Ataque(80f*ESCALA_MINIJEFE, 80f*ESCALA_MINIJEFE, -50f*ESCALA_MINIJEFE, 0f*ESCALA_MINIJEFE);
    private static final String SONIDO_ATAQUE = "GOLPE";

    public Cultista(float x, float y, Jugador jugador, Jugador[] jugadores) {
        this(x, y, jugador, jugadores, false);
    }

    public Cultista(float x, float y, Jugador jugador, Jugador[] jugadores, boolean minijefe) {
        super(
            x,
            y,
            minijefe ? ANCHO * ESCALA_MINIJEFE : ANCHO,
            minijefe ? ALTO * ESCALA_MINIJEFE : ALTO,
            VELOCIDAD,
            minijefe ? VIDA_MINIJEFE : VIDA,
            minijefe ? DANIO_MINIJEFE : DANIO,
            minijefe ? ATAQUE_MINIJEFE : ATAQUE,
            TIEMPO_ANTES_DE_ATACAR,
            TIEMPO_ENTRE_ATAQUES,
            jugador,
            jugadores,
            GestorRecursos.obtenerTextura("Enemigos/cultista_idle.png"));
        this.tipo = minijefe ? Enemigos.MINIJEFE_CULTISTA : Enemigos.CULTISTA;
        this.sonidoAtaque = SONIDO_ATAQUE;
    }

    @Override
    public Enemigos getTipo() {
        return tipo;
    }
}
