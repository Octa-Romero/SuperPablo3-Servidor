package com.prz.juego.entidades.enemigos;

public enum Enemigos {
    CULTISTA(false),
    MINIJEFE_CULTISTA(true),
    DEMONIO(false),
    MINIJEFE_DEMONIO(true),

    ARQUERO(false),
    MINIJEFE_ARQUERO(true),

    RETUMBAR(false);

    private final boolean MINIJEFE;

    Enemigos(boolean minijefe) {
        MINIJEFE = minijefe;
    }

    public boolean esMinijefe() {
        return MINIJEFE;
    }
}
