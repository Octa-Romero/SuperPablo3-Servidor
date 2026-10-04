package com.prz.juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public enum Musica {
	MENU("Musicas/musica_menu.mp3"),
	NIVEL1("Musicas/musica_nivel1.mp3");

	private final String NOMBRE_ARCHIVO;
	private Music music;
	private static Musica actual;
	private static float volumenGlobal = 0.5f;

	private Musica(String NOMBRE_ARCHIVO) {
		this.NOMBRE_ARCHIVO = NOMBRE_ARCHIVO;
	}

	public void sonar() {
		// Volver si ya está sonando la misma música o si es null
		if (actual == this && music != null && music.isPlaying()) {
			return;
		}
		parar();
		// Cargar si todavía no fue cargada
		if (music == null) {
			music = Gdx.audio.newMusic(Gdx.files.internal(NOMBRE_ARCHIVO));
		}
		music.setLooping(true);
		music.setVolume(volumenGlobal);
		music.play();
		actual = this;
	}

	public static void parar() {
		if (actual != null && actual.music != null) {
			actual.music.stop();
			actual = null;
		}
	}

	public static void setVolumen(float volumen) {
		volumenGlobal = Math.max(0f, Math.min(1f, volumen));
		if (actual != null && actual.music != null) {
			actual.music.setVolume(volumenGlobal);
		}
	}

	public static float getVolumen() {
		return volumenGlobal;
	}

	public static void dispose() {
		for(Musica musica : values()) {
			if(musica.music != null) {
				musica.music.dispose();
				musica.music = null;
			}
		}
		actual = null;
	}
}
