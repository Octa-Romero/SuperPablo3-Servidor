package com.prz.juego.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.Sonido;

public class MenuPausa {

    private final Stage stage;
    private final Navegable nav;
    private boolean activo = false;
    private final PantallaJuego pantallaJuego;
    private final BitmapFont fontTitulo;
    private final BitmapFont fontBotones;
    private Texture texturaFondoOscuro;
    private Texture texturaPanelOscuro;
    private Image fondoOscuro;
    private Image panelOscuro;

    public MenuPausa(Navegable nav, PantallaJuego anteriorPantalla) {
        this.nav = nav;
        this.pantallaJuego = anteriorPantalla;

        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));

        stage.setKeyboardFocus(null);
        stage.setScrollFocus(null);

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(3.0f);

        fontBotones = new BitmapFont();
        fontBotones.getData().setScale(2.0f);

        mostrar();

        // El menú empieza desactivado
        desactivar();
    }

    private void mostrar() {
        crearFondos();

        Table contenedor = new Table();

        Label titulo = new Label("PAUSA", new Label.LabelStyle(fontTitulo, Color.GOLD));

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = fontBotones;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.YELLOW;
        style.downFontColor = Color.RED;

        TextButton reanudar = new TextButton("REANUDAR", style);
        TextButton opciones = new TextButton("OPCIONES", style);
        TextButton menu = new TextButton("SALIR AL MENÚ", style);

        reanudar.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    pantallaJuego.setPausa(false);
                    desactivar();
                }
            }
        );

        opciones.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    pantallaJuego.guardarPosicionCamara();
                    nav.cambiarPantalla(new PantallaOpciones(nav, pantallaJuego, true));
                }
            }
        );

        menu.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    nav.cambiarPantalla(new PantallaMenu(nav));
                }
            }
        );

        contenedor.add(titulo).padBottom(40).row();
        contenedor.add(reanudar).width(250).height(50).padBottom(20).row();
        contenedor.add(opciones).width(250).height(50).padBottom(20).row();
        contenedor.add(menu).width(250).height(50).padBottom(20).row();
        contenedor.pack();

        float anchoPanel = contenedor.getWidth() + 100;
        float altoPanel = contenedor.getHeight() + 80;

        panelOscuro.setSize(anchoPanel, altoPanel);

        panelOscuro.setPosition((Config.ANCHO_BASE - anchoPanel) / 2f, (Config.ALTO_BASE - altoPanel) / 2f);

        contenedor.setPosition(Config.ANCHO_BASE / 2f, Config.ALTO_BASE / 2f, Align.center);

        stage.addActor(fondoOscuro);
        stage.addActor(panelOscuro);
        stage.addActor(contenedor);
    }

    private void crearFondos() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.45f));
        pixmap.fill();

        texturaFondoOscuro = new Texture(pixmap);
        fondoOscuro = new Image(new TextureRegionDrawable(new TextureRegion(texturaFondoOscuro)));
        fondoOscuro.setFillParent(true);

        pixmap.setColor(new Color(0, 0, 0, 0.65f));
        pixmap.fill();

        texturaPanelOscuro = new Texture(pixmap);
        panelOscuro = new Image(new TextureRegionDrawable(new TextureRegion(texturaPanelOscuro)));

        pixmap.dispose();
    }

    public void activar() {
        activo = true;
        stage.getRoot().setVisible(true);
        stage.getRoot().setTouchable(Touchable.enabled);
    }

    public void desactivar() {
        activo = false;
        stage.getRoot().setVisible(false);
        stage.getRoot().setTouchable(Touchable.disabled);
    }

    public void update(float delta) {
        if (activo) {
            stage.act(delta);
        }
    }

    public void render() {
        if (activo) {
            stage.draw();
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();

        fontTitulo.dispose();
        fontBotones.dispose();

        if (texturaFondoOscuro != null) {
            texturaFondoOscuro.dispose();
        }

        if (texturaPanelOscuro != null) {
            texturaPanelOscuro.dispose();
        }
    }

    public Stage getStage() {
        return stage;
    }
}
