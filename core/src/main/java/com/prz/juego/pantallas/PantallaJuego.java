package com.prz.juego.pantallas;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.prz.juego.entidades.Enemigo;
import com.prz.juego.entidades.Jugador;
import com.prz.juego.entidades.OrbeMagica;
import com.prz.juego.entidades.Personajes;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.principal.Navegable;
import com.prz.juego.niveles.Nivel;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Global;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;
import com.prz.juego.sistemas.Hud;

import java.util.ArrayList;

public class PantallaJuego implements Screen {

    private Servidor servidor;
    private final Navegable nav;
    private Jugador[] jugadores = new Jugador[2];
    private final Nivel nivel;
    private final MenuPausa menuPausa;
    private boolean pausado = false;
    private final Hud hud;

    public PantallaJuego(Navegable nav, Personajes personajeElegido, Servidor servidor) {
        this.nav = nav;
        this.nivel = new Nivel();
        this.menuPausa = new MenuPausa(nav, this);
        this.servidor = servidor;

        JugadorRed[] jugadoresRed = servidor.getJugadoresRed();

        for (int i = 0; i < jugadores.length; i++) {
            JugadorRed estado = jugadoresRed[i];

            Personajes personaje = Personajes.valueOf(estado.getPersonaje());

            jugadores[i] = personaje.crear(50 + i * 100, 150);
            jugadores[i].setEstadoRed(estado);
        }

        nivel.cargar("Niveles/Niveles/Level1.tmx", jugadores);

        hud = new Hud(jugadores[0], Render.batch);
    }

    @Override
    public void show() {
        InputMultiplexer mux = new InputMultiplexer();

        mux.addProcessor(menuPausa.getStage());
        //mux.addProcessor(entrada);

        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void render(float delta) {
        if(!Global.empieza) {return;}
        Render.limpiarPantalla();

        //if (entrada.aprietaEscape()) {
        //    pausado = !pausado;
        //    if (pausado) {
        //        menuPausa.activar();
        //    } else {
        //        menuPausa.desactivar();
        //    }
        //}

        if (!pausado) {
            nivel.update(delta);

            for(Jugador jugador : jugadores) {
                servidor.enviarPosicion(jugador);
                servidor.enviarEstadoAtaque(jugador);
                servidor.enviarEstadoVida(jugador);
            }

            for(Enemigo e : nivel.getEnemigosNuevos())
            {
                servidor.enviarCreacionEnemigos(e.getId(), e.getTipo(), e.getX(), e.getY(), e.getVida());
            }

            for(Enemigo e : nivel.getEnemigosActivos())
            {
                servidor.enviarEstadoEnemigo(e.getId(), e.getX(), e.getY(), e.getDireccion(), e.estaAtacando(), e.getVida());
            }

            for(Integer id : nivel.getEnemigosEliminados())
            {
                servidor.enviarEliminacionEnemigo(id);
            }

            for(OrbeMagica o : nivel.getOrbesNuevos())
            {
                servidor.enviarCreacionOrbes(o.getId(), o.getX(), o.getY(), o.getDireccion());
            }

            for(OrbeMagica o : nivel.getOrbesActivos())
            {
                servidor.enviarEstadoOrbes(o.getId(), o.getX(), o.getY());
            }

            for(Integer id : nivel.getOrbesEliminados())
            {
                servidor.enviarEliminacionOrbes(id);
            }

        }

        if (pausado) {
            Gdx.input.setInputProcessor(menuPausa.getStage());
        } else {
            //Gdx.input.setInputProcessor(entrada);
        }

        nivel.renderMapa();

        Render.begin(nivel.getCamara());
        nivel.render();
        Render.end();

        nivel.renderDebug();

        hud.dibujar();

        if (pausado) {
            menuPausa.update(delta);
            menuPausa.render();
        }

        if(nivel.isGameOver())
        {
            Musica.parar();
            nav.cambiarPantalla(new GameOver(nav));
        }
    }

    @Override
    public void resize(int width, int height) {
        nivel.resize(width, height);
        menuPausa.resize(width, height);
        hud.actualizarTamano(width, height);
    }

    public void setPausa(boolean valor)
    {
        this.pausado = valor;
    }

    public void guardarPosicionCamara() {
        nivel.guardarPosicionCamara();
    }

    public void restaurarPosicionCamara() {
        nivel.restaurarPosicionCamara();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        nivel.dispose();
        menuPausa.dispose();
    }
}
