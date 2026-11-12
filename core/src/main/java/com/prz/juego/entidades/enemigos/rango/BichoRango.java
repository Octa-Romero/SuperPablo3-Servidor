package com.prz.juego.entidades.enemigos.rango;

import com.badlogic.gdx.graphics.Texture;
import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;

public class BichoRango extends EnemigoRango {

    private static final float DURACION_FRAME_IDLE = 0.12f;

    public BichoRango(int id, float x, float y, Jugador jugador, Modo modo) {
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
                GestorRecursos.obtenerTextura("Enemigos/BichoRango/bichoRango.png"),
                GestorRecursos.obtenerTextura("Enemigos/BichoRango/flecha.png"),
                50,
                10,
                modo);

        Texture spritesheet = GestorRecursos.obtenerTextura("Enemigos/BichoRango/bichoRango.png");
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
        vidaProyectil = 0.5;
        this.tipo = TipoEnemigo.BICHO_RANGO;
    }

    @Override
    public TipoEnemigo getTipo() {
        return TipoEnemigo.BICHO_RANGO;
    }
}
