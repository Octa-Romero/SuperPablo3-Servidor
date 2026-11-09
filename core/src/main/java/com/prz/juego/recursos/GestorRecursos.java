package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class GestorRecursos {

    private static final Map<String, Texture> TEXTURAS = new HashMap<>();
    private static final String PIXEL = "pixel";

    public static Texture obtenerTextura(String ruta) {
        if (!TEXTURAS.containsKey(ruta)) {
            TEXTURAS.put(ruta, new Texture(ruta));
        }
        return TEXTURAS.get(ruta);
    }

    public static Texture obtenerPixel() {
        if (!TEXTURAS.containsKey(PIXEL)) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            TEXTURAS.put(PIXEL, new Texture(pixmap));
            pixmap.dispose();
        }
        return TEXTURAS.get(PIXEL);
    }

    public static void dispose() {
        for (Texture textura : TEXTURAS.values()) {
            textura.dispose();
        }
        TEXTURAS.clear();
    }
}
