package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.BichoMelee;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel1 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel1.tmx";

    public Nivel1() {
        musica = "NIVEL1";
    }

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador[] jugadores) {
        BichoMelee enemigoConReliquia =
            new BichoMelee(2000, 300, jugadores[0], jugadores);

        BichoMelee enemigoNormal =
            new BichoMelee(2200, 300, jugadores[0], jugadores);



        agregarEntidad(enemigoConReliquia);
        agregarEntidad(enemigoNormal);
    }
}
