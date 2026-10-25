package com.prz.juego.principal;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.prz.juego.entidades.Personajes;
import com.prz.juego.pantallas.PantallaJuego;
import com.prz.juego.pantallas.PantallaMenu;
import com.prz.juego.pantallas.SeleccionPersonaje;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class Principal extends Game implements Navegable{

    private Servidor servidor;

    @Override
    public void create() {
        Render.batch = new SpriteBatch();
        servidor = new Servidor();
        servidor.iniciar();
        this.setScreen(new PantallaMenu(this));
    }

    @Override
    public void dispose() {

        Screen pantallaActual = getScreen();
        if (pantallaActual != null) {
            pantallaActual.dispose();
        }

        if (Render.batch != null) {
            Render.batch.dispose();
            Render.batch = null;
        }

        if(servidor != null)
        {
            servidor.cerrar();
        }

        Musica.dispose();
        Sonido.dispose();

        GestorRecursos.dispose();
    }

    @Override
    public void cambiarPantalla(Screen screen) {
        setScreen(screen);
    }

    @Override
    public void iniciarJuego() {
        setScreen(new SeleccionPersonaje(this, servidor));
    }
}
