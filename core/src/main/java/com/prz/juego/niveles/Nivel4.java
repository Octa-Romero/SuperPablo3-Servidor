package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.BichoMelee;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel4 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel4.tmx";

    public Nivel4() {
        musica = "NIVEL4";
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
        agregarEntidad(new BichoMelee(500, 500, jugadores[0], jugadores, true));
    }
}
