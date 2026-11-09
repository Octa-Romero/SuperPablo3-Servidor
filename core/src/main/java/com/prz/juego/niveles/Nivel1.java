package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.BichoMelee;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel1 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Level1.tmx";

    @Override
    protected String getRutaMapa() {
        return RUTA;
    }

    @Override
    protected void configurarContenidoEspecifico(Jugador jugador) {
        BichoMelee enemigoConReliquia =
            new BichoMelee(2000, 300, jugador);

        BichoMelee enemigoNormal =
            new BichoMelee(2200, 300, jugador);

        enemigoConReliquia.setSueltaReliquia(true);

        agregarEntidad(enemigoConReliquia);
        agregarEntidad(enemigoNormal);
    }
}
