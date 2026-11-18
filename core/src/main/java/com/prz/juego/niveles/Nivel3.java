package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.BichoMelee;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel3 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel3.tmx";

    public Nivel3() {
        musica = "NIVEL3";
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
