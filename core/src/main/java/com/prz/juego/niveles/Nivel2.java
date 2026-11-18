package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.Demonio;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel2 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel2.tmx";

    public Nivel2() {
        musica = "NIVEL2";
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    public float getPosicionInicialX() {
        return 500;
    }

    @Override
    public float getPosicionInicialY() {
        return 150;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
        agregarEntidad(new Demonio(500, 500, jugadores[0], jugadores));
        agregarEntidad(new Demonio(1000, 500, jugadores[0], jugadores, true));
    }
}
