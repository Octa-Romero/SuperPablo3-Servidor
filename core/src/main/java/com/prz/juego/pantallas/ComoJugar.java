package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Render;

public class ComoJugar implements Screen {

    private Imagen fondo;
    private final Navegable NAV;
    private Stage stage;
    private BitmapFont font;

    public ComoJugar(Navegable NAV) {
        this.NAV = NAV;
    }

    @Override
    public void show() {
        inicializarStage();
        inicializarFondo();
        inicializarFuentes();
        crearInterfaz();

        Gdx.input.setInputProcessor(stage);
    }

    private void inicializarFondo() {
        fondo = new Imagen("Menu/fondoComoJugar.png");
        actualizarFondo();
    }

    private void actualizarFondo() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        fondo.setSize(w, h);
        fondo.setPosition(0, 0);
    }

    private void inicializarStage() {
        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));
    }

    private void inicializarFuentes() {
        font = FuenteJuego.generar(32);
    }

    private void crearInterfaz() {

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.defaults().pad(6);

        Label titulo = new Label("Cómo jugar", new Label.LabelStyle(font, Color.WHITE));
        titulo.setFontScale(1.6f);

        Label movTitulo = new Label("Movimiento", new Label.LabelStyle(font, Color.WHITE));
        movTitulo.setFontScale(1.2f);

        Label mov =
                new Label(
                        "A/D o Flecha Izquierda/Flecha Derecha",
                        new Label.LabelStyle(font, Color.WHITE));

        Label saltoTitulo = new Label("Salto", new Label.LabelStyle(font, Color.WHITE));
        saltoTitulo.setFontScale(1.2f);

        Label salto = new Label("W, Z o Flecha Arriba", new Label.LabelStyle(font, Color.WHITE));

        Label ataqueTitulo = new Label("Ataque", new Label.LabelStyle(font, Color.WHITE));
        ataqueTitulo.setFontScale(1.2f);

        Label ataque = new Label("X, J o Espacio", new Label.LabelStyle(font, Color.WHITE));

        Label pausaTitulo = new Label("Pausa", new Label.LabelStyle(font, Color.WHITE));
        pausaTitulo.setFontScale(1.2f);

        Label pausa = new Label("Esc", new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.GOLD;
        style.downFontColor = Color.RED;

        TextButton volver = new TextButton("VOLVER", style);

        volver.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        NAV.cambiarPantalla(new PantallaMenu(NAV));
                    }
                });

        table.add(titulo).padBottom(10).row();

        table.add(movTitulo).padTop(10).row();
        table.add(mov).padBottom(10).row();

        table.add(saltoTitulo).row();
        table.add(salto).padBottom(10).row();

        table.add(ataqueTitulo).row();
        table.add(ataque).padBottom(10).row();

        table.add(pausaTitulo).row();
        table.add(pausa).padBottom(20).row();

        table.add(volver).width(250).height(50);

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
        if (stage != null) {
            stage.dispose();
        }

        if (font != null) {
            font.dispose();
        }
    }
}
