package com.prz.juego.entidades.enemigos.melee;

import com.badlogic.gdx.math.Rectangle;

public record Ataque(float ancho, float alto, float offsetX, float offsetY) {

    public Rectangle calcular(float x, float y, float anchoCuerpo, int direccion) {
        float ataqueX = direccion > 0 ? x + anchoCuerpo + offsetX : x - offsetX - ancho;
        return new Rectangle(ataqueX, y + offsetY, ancho, alto);
    }
}
