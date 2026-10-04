package com.prz.juego.entidades;

import com.prz.juego.recursos.GestorRecursos;

public class Bicho1 extends Enemigo {

    public Bicho1(float x, float y, Jugador jugador) {
        super(x, y, 70, 110, 100, 3, 0.5, jugador, GestorRecursos.obtenerTextura("Enemigos/Bicho1/bicho1.png"), GestorRecursos.obtenerTextura("Enemigos/Bicho1/ataque_bicho1.png"));
    }
}
