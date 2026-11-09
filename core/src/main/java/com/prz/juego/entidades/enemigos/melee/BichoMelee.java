package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.graphics.Texture;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoMelee extends EnemigoMelee {

    private static final float DURACION_FRAME_IDLE = 0.12f;

    public BichoMelee(float x, float y, Jugador jugador) {
        this(
                x,
                y,
                jugador,
                GestorRecursos.obtenerTextura("Enemigos/BichoMelee/bichoMelee.png"));
    }

    private BichoMelee(float x, float y, Jugador jugador, Texture spritesheet) {
        super(
                x,
                y,
                70,
                110,
                100,
                3,
                0.5,
                jugador,
                spritesheet);

        ANIMACION.configurar(
                spritesheet,
                spritesheet.getWidth(),
                spritesheet.getHeight(),
                new int[] {0},
                null,
                null,
                null,
                DURACION_FRAME_IDLE);
        ANIMACION.actualizar(sprite, 0f, false, false, false);
    }
}
