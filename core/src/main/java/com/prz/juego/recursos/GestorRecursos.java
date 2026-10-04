package com.prz.juego.recursos;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class GestorRecursos {

    private static final Map<String, Texture> texturas = new HashMap<>();

    public static Texture obtenerTextura(String ruta) {
        if (!texturas.containsKey(ruta)) {
            texturas.put(ruta, new Texture(ruta));
        }
        return texturas.get(ruta);
    }

    public static void dispose() {
        for (Texture textura : texturas.values()) {
            textura.dispose();
        }
        texturas.clear();
    }
}
