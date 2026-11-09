package com.prz.juego.sistemas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;
import com.prz.juego.recursos.GestorRecursos;

public class BarraVida {

    private static final Color COLOR_FONDO = new Color(0.15f, 0.15f, 0.15f, 1f);
    private static final float BORDE = 3;
    private static final float ESPACIO_NOMBRE = 8;

    public static void dibujar(
            Batch batch,
            BitmapFont fuente,
            String nombre,
            float x,
            float y,
            float ancho,
            float alto,
            double vida,
            double vidaMaxima,
            Color color) {
        Texture pixel = GestorRecursos.obtenerPixel();
        float porcentaje = (float) Math.max(0, Math.min(1, vida / vidaMaxima));

        batch.setColor(Color.BLACK);
        batch.draw(pixel, x - BORDE, y - BORDE, ancho + BORDE * 2, alto + BORDE * 2);

        batch.setColor(COLOR_FONDO);
        batch.draw(pixel, x, y, ancho, alto);

        batch.setColor(color);
        batch.draw(pixel, x, y, ancho * porcentaje, alto);

        batch.setColor(Color.WHITE);

        fuente.draw(
                batch,
                nombre,
                x,
                y + alto + ESPACIO_NOMBRE + fuente.getCapHeight(),
                ancho,
                Align.center,
                false);
    }
}
