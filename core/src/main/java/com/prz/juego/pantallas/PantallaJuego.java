package com.prz.juego.pantallas;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.prz.juego.entidades.Jugador;
import com.prz.juego.entidades.Personajes;
import com.prz.juego.principal.Navegable;
import com.prz.juego.niveles.Nivel;
import com.prz.juego.utilidades.Entrada;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;
import com.prz.juego.sistemas.Hud;

public class PantallaJuego implements Screen {

    private final Navegable nav;
    private Jugador jugador;
    private final Nivel nivel;
    private final Entrada entrada;
    private final MenuPausa menuPausa;
    private boolean pausado = false;
    private final Hud hud;

    public PantallaJuego(Navegable nav, Personajes personajeElegido) {
        this.nav = nav;
        this.entrada = new Entrada();
        this.nivel = new Nivel();
        this.menuPausa = new MenuPausa(nav, this);

        jugador = personajeElegido.crear(50, 150);

        jugador.setEntrada(entrada);

        nivel.cargar("Niveles/Niveles/Level1.tmx", jugador);

        hud = new Hud(jugador, Render.batch);
    }

    @Override
    public void show() {
        InputMultiplexer mux = new InputMultiplexer();

        mux.addProcessor(menuPausa.getStage());
        mux.addProcessor(entrada);

        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla();

        if (entrada.aprietaEscape()) {
            pausado = !pausado;
            if (pausado) {
                menuPausa.activar();
            } else {
                menuPausa.desactivar();
            }
        }

        if (!pausado) {
            nivel.update(delta, entrada);
        }

        if (pausado) {
            Gdx.input.setInputProcessor(menuPausa.getStage());
        } else {
            Gdx.input.setInputProcessor(entrada);
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
