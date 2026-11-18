package com.prz.juego.niveles;

import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel6 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel6.tmx";

    public Nivel6() {
        musica = "NIVEL6";
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
    }
}
