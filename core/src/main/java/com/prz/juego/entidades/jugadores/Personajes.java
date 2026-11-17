package com.prz.juego.entidades.jugadores;

public enum Personajes {
    PABLO,
    WALTER;

    public Jugador crear(float x, float y) {
        switch (this) {
            case PABLO:
                return new Pablo(x, y);
            case WALTER:
                return new Walter(x, y);
            default:
                throw new IllegalArgumentException("Personaje inválido");
        }
    }
}
