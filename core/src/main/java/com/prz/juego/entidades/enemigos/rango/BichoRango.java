package com.prz.juego.entidades.enemigos.rango;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoRango extends EnemigoRango {

    private static final double VIDA_PROYECTIL = 0.5;

    public BichoRango(float x, float y, Jugador jugador, Jugador[] jugadores, Modo modo) {
        super(
            x,
            y,
            70,
            110,
            100,
            3,
            0.5,
            jugador,
            jugadores,
            GestorRecursos.obtenerTextura("Enemigos/BichoRango/bichoRango.png"),
            GestorRecursos.obtenerTextura("Enemigos/BichoRango/flecha.png"),
            50,
            10,
            modo);
        this.vidaProyectil = VIDA_PROYECTIL;
        this.tipo = TipoEnemigo.BICHO_RANGO;
    }

    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.BICHO_RANGO;
    }
}
