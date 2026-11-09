package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FuenteJuego {

    private FuenteJuego() {}

    public static BitmapFont generar(int tamano) {
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("Fuentes/Augusta.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parametro =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametro.size = tamano;
        parametro.color = Color.WHITE;
        parametro.borderWidth = 2f;
        parametro.borderColor = Color.BLACK;
        BitmapFont fuente = generator.generateFont(parametro);
        generator.dispose();
        return fuente;
    }
}
