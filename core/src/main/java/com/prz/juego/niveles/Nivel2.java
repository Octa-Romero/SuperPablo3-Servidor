package com.prz.juego.niveles;

import com.prz.juego.entidades.enemigos.rango.BichoRango;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.jugadores.Jugador;

public class Nivel2 extends Nivel {

    public static final String RUTA = "Niveles/Niveles/Nivel2.1.tmx";

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
        agregarEntidad(new BichoRango(2650, 650, jugadores[0], jugadores, EnemigoRango.Modo.ESTATICO));
        agregarEntidad(new BichoRango(5000, 260, jugadores[0], jugadores, EnemigoRango.Modo.MOVIL));
    }
}
