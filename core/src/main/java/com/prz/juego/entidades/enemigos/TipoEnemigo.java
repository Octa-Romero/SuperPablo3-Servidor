package com.prz.juego.entidades.enemigos;

public enum TipoEnemigo {
    BICHO_RANGO,
    BICHO_MELEE,
    MINIJEFE_RANGO,
    MINIJEFE_MELEE,
    JEFE;

    public boolean esMinijefe() {
        return this == MINIJEFE_RANGO || this == MINIJEFE_MELEE;
    }
}
