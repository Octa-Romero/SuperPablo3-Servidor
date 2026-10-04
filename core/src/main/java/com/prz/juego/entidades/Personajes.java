package com.prz.juego.entidades;

public enum Personajes {
    PABLO,
    WALTER;

    public Jugador crear(int x, int y) {

        switch (this) {

            case PABLO:
                Pablo p = new Pablo(x, y);
                return p;

            case WALTER:
                Walter w = new Walter(x, y);
                return w;

            default:
                throw new IllegalArgumentException("Personaje inválido");
        }
    }
}
