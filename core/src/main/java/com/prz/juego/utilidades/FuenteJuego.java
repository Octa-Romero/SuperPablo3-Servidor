package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FuenteJuego {

    private static final String RUTA_FUENTE = "Fuentes/Augusta.ttf";
    private static final float GROSOR_BORDE = 2f;

    private FuenteJuego() {
    }

    public static BitmapFont generar(int tamano) {
        return generar(tamano, Color.WHITE, Color.BLACK);
    }

    public static BitmapFont generar(int tamano, Color colorTexto, Color colorBorde) {
        FreeTypeFontGenerator generador = new FreeTypeFontGenerator(Gdx.files.internal(RUTA_FUENTE));
        try {
            FreeTypeFontParameter parametro = new FreeTypeFontParameter();
            parametro.size = tamano;
            parametro.color = colorTexto;
            parametro.borderWidth = GROSOR_BORDE;
            parametro.borderColor = colorBorde;
            return generador.generateFont(parametro);
        } finally {
            generador.dispose();
        }
    }
}
