package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.Cultista;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel5 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel5.tmx";

    public Nivel5() {
        musica = "NIVEL5";
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
        agregarEntidad(new Cultista(500, 500, jugadores[0], jugadores));
        agregarEntidad(new Cultista(1000, 500, jugadores[0], jugadores, true));
    }
}
