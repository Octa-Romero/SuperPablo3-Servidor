package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.melee.Demonio;
import com.prz.juego.entidades.enemigos.rango.Arquero;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
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
        agregarEntidad(new Arquero(500, 500, jugadores[0], jugadores, EnemigoRango.Modo.ESTATICO));
        agregarEntidad(new Arquero(1000, 500, jugadores[0], jugadores, EnemigoRango.Modo.MOVIL, true));
    }
}
