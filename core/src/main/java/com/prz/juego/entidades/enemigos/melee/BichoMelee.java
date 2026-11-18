package com.prz.juego.entidades.enemigos.melee;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoMelee extends EnemigoMelee {

    private static final float ANCHO = 70f;
    private static final float ALTO = 110f;
    private static final int VELOCIDAD = 100;
    private static final double VIDA = 3;
    private static final double DANIO = 0.5;
    private static final float ESCALA_MINIJEFE = 1.5f;
    private static final double VIDA_MINIJEFE = 12;
    private static final double DANIO_MINIJEFE = 1;
    private static final String SONIDO_ATAQUE = "ESPADA";

    public BichoMelee(float x, float y, Jugador jugador, Jugador[] jugadores) {
        this(x, y, jugador, jugadores, false);
    }

    public BichoMelee(float x, float y, Jugador jugador, Jugador[] jugadores, boolean minijefe) {
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
            GestorRecursos.obtenerTextura("Enemigos/BichoMelee/bichoMelee.png"));
        this.tipo = minijefe ? TipoEnemigo.MINIJEFE_MELEE : TipoEnemigo.BICHO_MELEE;
        this.sonidoAtaque = SONIDO_ATAQUE;

        if (minijefe) {
            this.anchoAtaque *= ESCALA_MINIJEFE;
            this.altoAtaque *= ESCALA_MINIJEFE;
        }
    }

    @Override
    public TipoEnemigo getTipo() {
        return tipo;
    }
}
