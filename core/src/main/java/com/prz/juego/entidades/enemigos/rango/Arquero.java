package com.prz.juego.entidades.enemigos.rango;

import com.prz.juego.entidades.enemigos.Enemigos;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class Arquero extends EnemigoRango {

    private static final float ANCHO = 70f;
    private static final float ALTO = 110f;
    private static final int VELOCIDAD = 100;
    private static final double VIDA = 3;
    private static final double DANIO = 0.5;
    private static final float ANCHO_PROYECTIL = 50f;
    private static final float ALTO_PROYECTIL = 10f;
    private static final double VIDA_PROYECTIL = 0.5;
    private static final float ESCALA_MINIJEFE = 1.5f;
    private static final double VIDA_MINIJEFE = 12;
    private static final double DANIO_MINIJEFE = 1;
    private static final String SONIDO_ATAQUE = "ESPADA";

    public Arquero(float x, float y, Jugador jugador, Jugador[] jugadores, Modo modo) {
        this(x, y, jugador, jugadores, modo, false);
    }

    public Arquero(
        float x, float y, Jugador jugador, Jugador[] jugadores, Modo modo, boolean minijefe) {
        super(
            x,
            y,
            minijefe ? ANCHO * ESCALA_MINIJEFE : ANCHO,
            minijefe ? ALTO * ESCALA_MINIJEFE : ALTO,
            VELOCIDAD,
            minijefe ? VIDA_MINIJEFE : VIDA,
            minijefe ? DANIO_MINIJEFE : DANIO,
            jugador,
            jugadores,
            GestorRecursos.obtenerTextura("Enemigos/arquero_idle.png"),
            GestorRecursos.obtenerTextura("Enemigos/flecha.png"),
            minijefe ? ANCHO_PROYECTIL * ESCALA_MINIJEFE : ANCHO_PROYECTIL,
            minijefe ? ALTO_PROYECTIL * ESCALA_MINIJEFE : ALTO_PROYECTIL,
            modo);
        this.vidaProyectil = VIDA_PROYECTIL;
        this.tipo = minijefe ? Enemigos.MINIJEFE_ARQUERO : Enemigos.ARQUERO;
        this.sonidoAtaque = SONIDO_ATAQUE;
    }

    @Override
    public Enemigos getTipo() {
        return tipo;
    }
}
