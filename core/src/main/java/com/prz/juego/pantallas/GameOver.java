package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.Render;

public class GameOver implements Screen {

    private final Navegable nav;
    private Stage stage;
    private BitmapFont font;

    public GameOver(Navegable nav) {
        this.nav = nav;
    }

    @Override
    public void show() {
        inicializarStage();
        inicializarFuentes();
        crearInterfaz();

        Gdx.input.setInputProcessor(stage);
    }

    private void inicializarStage() {
        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));
    }

    private void inicializarFuentes() {
        font = new BitmapFont();
    }

    private void crearInterfaz() {

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.defaults().pad(8);

        Label titulo = new Label("GAME OVER", new Label.LabelStyle(font, Color.RED));
        titulo.setFontScale(3f);

        Label mensaje = new Label("Has perdido la partida", new Label.LabelStyle(font, Color.LIGHT_GRAY));
        mensaje.setFontScale(1.2f);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.GOLD;
        style.downFontColor = Color.RED;

        TextButton volverMenu = new TextButton("VOLVER AL MENU", style);

        volverMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nav.cambiarPantalla(new PantallaMenu(nav));
            }
        });

        table.add(titulo).padBottom(20).row();
        table.add(mensaje).padBottom(30).row();
        table.add(volverMenu).width(300).height(60);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}

    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
    }
}
