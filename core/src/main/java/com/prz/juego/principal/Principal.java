package com.prz.juego.principal;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.prz.juego.pantallas.PantallaJuego;
import com.prz.juego.pantallas.PantallaMenu;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Render;

public class Principal extends Game implements Navegable {

    private Servidor servidor;
    private static final float DELTA_MAXIMO = 1f / 20f;

    @Override
    public void render() {
        Screen pantalla = getScreen();
        if (pantalla != null) {
            pantalla.render(Math.min(Gdx.graphics.getDeltaTime(), DELTA_MAXIMO));
        }
    }

    @Override
    public void create() {
        Render.batch = new SpriteBatch();

        servidor = new Servidor();
        Servidor.instancia = servidor;

        setScreen(new PantallaMenu(this, servidor));
    }

    @Override
    public void cambiarPantalla(Screen screen) {
        Gdx.app.postRunnable(() -> reemplazarPantalla(screen));
    }

    @Override
    public void iniciarJuego(String rutaNivel) {
        Gdx.app.postRunnable(() -> reemplazarPantalla(new PantallaJuego(this, servidor, rutaNivel)));
    }

    @Override
    public void iniciarPartida() {
        iniciarJuego(null);
    }

    private void reemplazarPantalla(Screen nueva) {
        Screen anterior = getScreen();
        setScreen(nueva);

        if (anterior != null && anterior != nueva) {
            anterior.dispose();
        }
    }

    @Override
    public void dispose() {
        try {
            if (servidor != null) {
                servidor.cerrar();
            }
        } finally {
            try {
                Screen pantallaActual = getScreen();
                setScreen(null);
                if (pantallaActual != null) {
                    pantallaActual.dispose();
                }
            } finally {
                if (Render.batch != null) {
                    Render.batch.dispose();
                    Render.batch = null;
                }
                GestorRecursos.dispose();

                Servidor.instancia = null;
                servidor = null;
            }
        }
    }
}
