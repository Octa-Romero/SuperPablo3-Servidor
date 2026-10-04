package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public enum Sonido {
	SALTO("Sonidos/salto.WAV"),
	ESPADA("Sonidos/espada.WAV"),
    BOLA_FUEGO("Sonidos/bola_fuego.WAV"),
	CLICK("Sonidos/click.WAV");

	private final String NOMBRE_ARCHIVO;
	private Sound sound;
	private static float volumenGlobal = 0.5f;

	private Sonido(String NOMBRE_ARCHIVO) {
		this.NOMBRE_ARCHIVO = NOMBRE_ARCHIVO;
	}

	public void sonar() {
		cargar();
		sound.play(volumenGlobal);
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
		for(Sonido sonido : values()) {
			if(sonido.sound != null) {
				sonido.sound.dispose();
				sonido.sound = null;
			}
		}
	}
}
