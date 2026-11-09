package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.entidades.jugadores.Personajes;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Global;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class SeleccionPersonaje implements Screen {

    private Servidor servidor;
    private final Navegable NAV;
    private final PantallaMenu PANTALLA_MENU;
    private Stage stage;
    private BitmapFont font;

    private BitmapFont fontBoton;
    private Imagen fondo;
    private Personajes personajeSeleccionado;
    private TextButton btnComenzar;

    public SeleccionPersonaje(Navegable NAV, PantallaMenu PANTALLA_MENU) {
        this.NAV = NAV;
        this.PANTALLA_MENU = PANTALLA_MENU;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));
        Gdx.input.setInputProcessor(stage);
        inicializarFuentes();
        inicializarFondo();

        crearInterfaz();
    }

    private void inicializarFuentes() {
        font = FuenteJuego.generar(48);
        fontBoton = FuenteJuego.generar(28);
    }

    private void inicializarFondo() {
        fondo = new Imagen("Menu/seleccionPersonaje.png");
        actualizarFondo();
    }

    private void actualizarFondo() {

        float ancho = stage.getViewport().getWorldWidth();
        float alto = stage.getViewport().getWorldHeight();
        fondo.setSize(ancho, alto);
        fondo.setPosition(0, 0);
    }

    private void crearInterfaz() {

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.defaults().pad(10);

        Label titulo = new Label("Selección de personaje", new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = fontBoton;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.GOLD;
        style.downFontColor = Color.RED;
        style.disabledFontColor = Color.GRAY;

        TextButton btnPablo = new TextButton("Pablo", style);
        TextButton btnWalter = new TextButton("Walter", style);
        TextButton btnCancelar = new TextButton("Cancelar", style);

        btnComenzar = new TextButton("Comenzar", style);
        btnComenzar.setDisabled(true);

        btnPablo.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        personajeSeleccionado = Personajes.PABLO;
                        btnComenzar.setDisabled(false);
                    }
                });

        btnWalter.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        personajeSeleccionado = Personajes.WALTER;
                        btnComenzar.setDisabled(false);
                    }
                });

        btnComenzar.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {

                if (personajeSeleccionado == null) return;

                //nav.iniciarJuego(personajeSeleccionado);
            }
        });

        btnCancelar.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Sonido.CLICK.sonar();
                        NAV.cambiarPantalla(PANTALLA_MENU);
                    }
                });

        table.add(titulo).colspan(2).center().padBottom(40).row();

        table.add(btnPablo).width(250).height(60).padRight(255).padBottom(20);
        table.add(btnWalter).width(250).height(60).padLeft(255).padBottom(20).row();

        table.add(btnComenzar).colspan(2).center().width(300).height(70).padBottom(12).row();
        table.add(btnCancelar).colspan(2).center().width(250).height(60);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla();
        Render.begin(stage.getCamera());
        fondo.dibujar(Render.batch);
        Render.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}
