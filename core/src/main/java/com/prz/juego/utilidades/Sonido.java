package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public enum Sonido {
    SALTO("Sonidos/salto.WAV"),
    ESPADA("Sonidos/espada.WAV"),
    BOLA_FUEGO("Sonidos/bolaFuego.WAV"),
    CLICK("Sonidos/click.WAV"),
    GOLPE("Sonidos/golpe.WAV"),
    TP1("Sonidos/tp1.WAV"),
    TP2("Sonidos/tp2.WAV"),
    METEORITO("Sonidos/meteorito.WAV"),
    RELAMPAGO("Sonidos/relampago.WAV"),
    FUEGO_SUELO("Sonidos/fuegoSuelo.WAV"),
    FUEGUITO("Sonidos/fueguito.WAV"),
    FUEGO_SOSTENIDO("Sonidos/fuegoSostenido.WAV"),
    RETUMBAR_ATAQUE("Sonidos/retumbarAtaque.WAV"),
    RETUMBAR_GOLPE_RECIBIDO("Sonidos/retumbarGolpeRecibido.WAV"),
    RETUMBAR_MANO_ROTA("Sonidos/retumbarManoRota.WAV"),
    RETUMBAR_MUERTE("Sonidos/retumbarMuerte.WAV");

    private final String NOMBRE_ARCHIVO;
    private Sound sound;
    private long sonidoLoopId = -1;
    private static float volumenGlobal = 0.5f;
    private final int RANGO_ESCUCHA = 700;

    private Sonido(String NOMBRE_ARCHIVO) {
        this.NOMBRE_ARCHIVO = NOMBRE_ARCHIVO;
    }

    public void sonar() {
        cargar();
        sound.play(volumenGlobal);
    }

    public void sonar(float x, float xJugador) {
        if ((xJugador - RANGO_ESCUCHA) < x && (xJugador + RANGO_ESCUCHA) > x) {
            sonar();
        }
    }

    public void sonarConLoop() {
        cargar();
        sonidoLoopId = sound.play(volumenGlobal);
    }

    public void detenerLoop() {
        if (sonidoLoopId != -1) {
            cargar();
            sound.stop(sonidoLoopId);
            sonidoLoopId = -1;
        }
    }

    public boolean estaSonandoLoop() {
        return sonidoLoopId != -1;
    }

    private void cargar() {
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(NOMBRE_ARCHIVO));
        }
    }

    public static void setVolumen(float volumen) {
        volumenGlobal = Math.max(0f, Math.min(1f, volumen));
    }

    public static float getVolumen() {
        return volumenGlobal;
    }

    public static void dispose() {
        for (Sonido sonido : values()) {
            sonido.detenerLoop();
            if (sonido.sound != null) {
                sonido.sound.dispose();
                sonido.sound = null;
            }
        }
    }
}
