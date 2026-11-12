package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.graphics.Texture;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoMelee extends EnemigoMelee {

    private static final float DURACION_FRAME_IDLE = 0.12f;

    public BichoMelee(int id, float x, float y, Jugador jugador) {
        this(
                id,
                x,
                y,
                jugador,
                GestorRecursos.obtenerTextura("Enemigos/BichoMelee/bichoMelee.png"));
        this.tipo = TipoEnemigo.BICHO_MELEE;
    }

    private BichoMelee(int id, float x, float y, Jugador jugador, Texture spritesheet) {
        super(
                id,
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


    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.BICHO_MELEE;
    }
}
