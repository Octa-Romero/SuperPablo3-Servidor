package com.prz.juego.niveles;

import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel3 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel3.tmx";

    public Nivel3() {
        musica = null;
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
        // Este mapa todavía no define entidades propias del nivel.
    }
}
