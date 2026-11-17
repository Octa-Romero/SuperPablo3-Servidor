package com.prz.juego.entidades.enemigos.melee;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoMelee extends EnemigoMelee {

    public BichoMelee(float x, float y, Jugador jugador, Jugador[] jugadores) {
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
            GestorRecursos.obtenerTextura("Enemigos/BichoMelee/bichoMelee.png"));
        this.tipo = TipoEnemigo.BICHO_MELEE;
    }

    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.BICHO_MELEE;
    }
}
