package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class GestorRecursos {

    private static final Map<String, Texture> TEXTURAS = new HashMap<>();
    private static Texture pixel;

    private GestorRecursos() {}

    public static synchronized Texture obtenerTextura(String ruta) {
        Texture textura = TEXTURAS.get(ruta);
        if (textura == null) {
            textura = new Texture(ruta);
            TEXTURAS.put(ruta, textura);
        }
        return textura;
    }

    public static synchronized Texture obtenerPixel() {
        if (pixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            try {
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                pixel = new Texture(pixmap);
            } finally {
                pixmap.dispose();
            }
        }
        return pixel;
    }

    public static synchronized void dispose() {
        for (Texture textura : TEXTURAS.values()) {
            textura.dispose();
        }
        TEXTURAS.clear();

        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
    }
}
