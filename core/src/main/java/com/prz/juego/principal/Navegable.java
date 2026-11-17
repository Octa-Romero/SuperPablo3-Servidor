package com.prz.juego.principal;

import com.badlogic.gdx.Screen;

public interface Navegable {
    void cambiarPantalla(Screen SCREEN);
    void iniciarJuego(String RUTA_NIVEL);
    void iniciarPartida();
}
