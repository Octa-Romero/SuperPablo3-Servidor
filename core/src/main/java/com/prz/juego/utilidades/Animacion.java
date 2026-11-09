package com.prz.juego.utilidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Animacion {

    private Animation<TextureRegion> reposo;
    private Animation<TextureRegion> caminar;
    private Animation<TextureRegion> salto;
    private Animation<TextureRegion> ataque;
    private Animation<TextureRegion> actual;
    private float tiempo;

    public Animacion(Texture texturaReposo) {
        if (texturaReposo == null) {
            throw new IllegalArgumentException("La textura de reposo no puede ser null");
        }
        reposo = new Animation<>(1f, new TextureRegion(texturaReposo));
        reposo.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void configurar(
            Texture spritesheet,
            int anchoFrame,
            int altoFrame,
            int[] indicesReposo,
            int[] indicesCaminar,
            int[] indicesSalto,
            int[] indicesAtaque,
            float duracionFrame) {
        validarConfiguracion(spritesheet, anchoFrame, altoFrame, indicesReposo, duracionFrame);

        TextureRegion[][] grilla = TextureRegion.split(spritesheet, anchoFrame, altoFrame);
        TextureRegion[] frames = aplanar(grilla);
        validarIndices(frames.length, indicesReposo, indicesCaminar, indicesSalto, indicesAtaque);

        reposo = crearAnimacion(frames, indicesReposo, duracionFrame, Animation.PlayMode.LOOP);
        caminar =
                crearAnimacionSiConfigurada(
                        frames, indicesCaminar, duracionFrame, Animation.PlayMode.LOOP);
        salto =
                crearAnimacionSiConfigurada(
                        frames, indicesSalto, duracionFrame, Animation.PlayMode.NORMAL);
        ataque =
                crearAnimacionSiConfigurada(
                        frames, indicesAtaque, duracionFrame, Animation.PlayMode.NORMAL);

        actual = null;
        tiempo = 0f;
    }

    public void actualizar(
            Sprite sprite, float delta, boolean caminando, boolean saltando, boolean atacando) {
        Animation<TextureRegion> siguiente;

        if (atacando && ataque != null) {
            siguiente = ataque;
        } else if (saltando && salto != null) {
            siguiente = salto;
        } else if (caminando) {
            siguiente = caminar != null ? caminar : reposo;
        } else {
            siguiente = reposo;
        }

        if (siguiente == null) {
            return;
        }

        if (actual != siguiente) {
            actual = siguiente;
            tiempo = 0f;
        } else {
            tiempo += delta;
        }

        sprite.setRegion(actual.getKeyFrame(tiempo));
    }

    public void dispose() {
        reposo = null;
        caminar = null;
        salto = null;
        ataque = null;
        actual = null;
    }

    private void validarConfiguracion(
            Texture spritesheet,
            int anchoFrame,
            int altoFrame,
            int[] indicesReposo,
            float duracionFrame) {
        if (spritesheet == null
                || anchoFrame <= 0
                || altoFrame <= 0
                || duracionFrame <= 0
                || indicesReposo == null
                || indicesReposo.length == 0
                || spritesheet.getWidth() < anchoFrame
                || spritesheet.getHeight() < altoFrame) {
            throw new IllegalArgumentException(
                    "La spritesheet debe incluir una animacion de reposo valida");
        }
    }

    private Animation<TextureRegion> crearAnimacionSiConfigurada(
            TextureRegion[] frames, int[] indices, float duracionFrame, Animation.PlayMode modo) {
        if (indices == null || indices.length == 0) {
            return null;
        }
        return crearAnimacion(frames, indices, duracionFrame, modo);
    }

    private Animation<TextureRegion> crearAnimacion(
            TextureRegion[] frames, int[] indices, float duracionFrame, Animation.PlayMode modo) {
        TextureRegion[] regiones = new TextureRegion[indices.length];
        for (int i = 0; i < indices.length; i++) {
            regiones[i] = frames[indices[i]];
        }

        Animation<TextureRegion> animacion = new Animation<>(duracionFrame, regiones);
        animacion.setPlayMode(modo);
        return animacion;
    }

    private void validarIndices(int totalFrames, int[]... grupos) {
        for (int[] grupo : grupos) {
            if (grupo == null) {
                continue;
            }
            for (int indice : grupo) {
                if (indice < 0 || indice >= totalFrames) {
                    throw new IllegalArgumentException(
                            "El spritesheet no contiene el frame " + indice);
                }
            }
        }
    }

    private TextureRegion[] aplanar(TextureRegion[][] grilla) {
        int cantidad = 0;
        for (TextureRegion[] fila : grilla) {
            cantidad += fila.length;
        }

        TextureRegion[] frames = new TextureRegion[cantidad];
        int indice = 0;
        for (TextureRegion[] fila : grilla) {
            for (TextureRegion frame : fila) {
                frames[indice++] = frame;
            }
        }
        return frames;
    }
}
