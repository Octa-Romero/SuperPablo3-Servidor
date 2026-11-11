package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class SeleccionNivel implements Screen {

    private final Navegable NAV;
    private Servidor servidor;
    private JugadorRed[] estadosRed;
    private boolean nivelIniciado = false;

    private Stage stage;
    private BitmapFont font;
    private Imagen fondo;

    private Texture texturaPortal1;
    private Texture texturaPortal2;
    private Texture texturaPortal3;
    private Texture texturaPortal4;
    private Texture texturaPortal5;
    private Texture texturaPortal6;
    private Texture texturaPortal7;

    public SeleccionNivel(Navegable nav, Servidor servidor) {
        this.NAV = nav;
        this.servidor = servidor;
        this.estadosRed = servidor.getJugadoresRed();
    }

    @Override
    public void show() {

        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));

        Gdx.input.setInputProcessor(stage);

        inicializarFuentes();
        inicializarFondo();
        inicializarPortales();
        crearInterfaz();
    }

    private void inicializarFuentes() {
        font = FuenteJuego.generar(48);
    }

    private void inicializarFondo() {

        fondo = new Imagen("Menu/seleccionNivel.png");

        float ancho = stage.getViewport().getWorldWidth();
        float alto = stage.getViewport().getWorldHeight();

        fondo.setSize(ancho, alto);
        fondo.setPosition(0, 0);
    }

    private void inicializarPortales() {
        texturaPortal1 = GestorRecursos.obtenerTextura("Menu/Portales/portal1.png");
        texturaPortal2 = GestorRecursos.obtenerTextura("Menu/Portales/portal2.png");
        texturaPortal3 = GestorRecursos.obtenerTextura("Menu/Portales/portal3.png");
        texturaPortal4 = GestorRecursos.obtenerTextura("Menu/Portales/portal4.png");
        texturaPortal5 = GestorRecursos.obtenerTextura("Menu/Portales/portal5.png");
        texturaPortal6 = GestorRecursos.obtenerTextura("Menu/Portales/portal6.png");
        texturaPortal7 = GestorRecursos.obtenerTextura("Menu/Portales/portal7.png");
    }

    private ImageButton crearPortal(Texture textura) {

        ImageButton.ImageButtonStyle style =
            new ImageButton.ImageButtonStyle();

        style.imageUp =
            new TextureRegionDrawable(new TextureRegion(textura));

        return new ImageButton(style);
    }

    private void iniciarNivel(int numeroNivel) {

        String rutaNivel;

        switch (numeroNivel) {
            case 1:
                rutaNivel = "Niveles/Niveles/Level1.tmx";
                break;
            case 2:
                rutaNivel = "Niveles/Niveles/Nivel2.1.tmx";
                break;
            case 3:
                rutaNivel = "Niveles/Niveles/Nivel3.tmx";
                break;
            case 4:
                rutaNivel = "Niveles/Niveles/nivel4.tmx";
                break;
            case 5:
                rutaNivel = "Niveles/Niveles/Nivel5.tmx";
                break;
            case 6:
                rutaNivel = "Niveles/Niveles/Nivel6.tmx";
                break;
            case 7:
                rutaNivel = "Niveles/Niveles/Nivel7.tmx";
                break;
            default:
                return;
        }

        NAV.iniciarJuego(rutaNivel);
    }

    private void animarPortal(ImageButton portal, float retraso) {

        portal.getColor().a = 0f;

        portal.addAction(Actions.sequence(Actions.delay(retraso), Actions.fadeIn(0.6f)));
    }

    private void crearInterfaz() {

        Table principal = new Table();
        principal.setFillParent(true);

        Label titulo = new Label("Selección de nivel", new Label.LabelStyle(font, Color.WHITE));

        titulo.getColor().a = 0f;

        titulo.addAction(Actions.sequence(Actions.delay(0.2f), Actions.fadeIn(0.6f)));

        ImageButton portal1 = crearPortal(texturaPortal1);
        ImageButton portal2 = crearPortal(texturaPortal2);
        ImageButton portal3 = crearPortal(texturaPortal3);
        ImageButton portal4 = crearPortal(texturaPortal4);
        ImageButton portal5 = crearPortal(texturaPortal5);
        ImageButton portal6 = crearPortal(texturaPortal6);
        ImageButton portal7 = crearPortal(texturaPortal7);


        TextButton.TextButtonStyle estiloCancelar = new TextButton.TextButtonStyle();
        estiloCancelar.font = font;
        estiloCancelar.fontColor = Color.WHITE;
        estiloCancelar.overFontColor = Color.GOLD;
        estiloCancelar.downFontColor = Color.RED;

        TextButton botonCancelar = new TextButton("Cancelar", estiloCancelar);
        botonCancelar.getLabel().setFontScale(0.65f);
        /*botonCancelar.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Sonido.CLICK.sonar();
                        PANTALLA_HUB.restaurarPosicionCamara();
                        NAV.cambiarPantalla(PANTALLA_HUB);
                    }
                });

         */

        animarPortal(portal1, 0.5f);
        animarPortal(portal2, 0.7f);
        animarPortal(portal3, 0.9f);
        animarPortal(portal4, 1.1f);
        animarPortal(portal5, 1.3f);
        animarPortal(portal6, 1.5f);
        animarPortal(portal7, 1.7f);

        principal.add(titulo).colspan(3).padBottom(20).row();
        principal.add(portal1).width(170).height(170).pad(5);
        principal.add(portal2).width(170).height(170).pad(5);
        principal.add(portal3).width(170).height(170).pad(5).row();
        principal.add(portal4).width(170).height(170).pad(5);
        principal.add(portal5).width(170).height(170).pad(5);
        principal.add(portal6).width(170).height(170).pad(5).row();
        principal.add(portal7).width(170).height(170).pad(5);

        principal.add(botonCancelar).colspan(3).width(200).height(48).padTop(12);

        stage.addActor(principal);
    }

    @Override
    public void render(float delta) {

        Render.limpiarPantalla();

        int voto1 = estadosRed[0].getNivelSeleccionado();
        int voto2 = estadosRed[1].getNivelSeleccionado();

        servidor.enviarVotosNivel(estadosRed[0], voto1, voto2);

        servidor.enviarVotosNivel(estadosRed[1], voto2, voto1);

        if (voto1 != 0 && voto1 == voto2 && !nivelIniciado) {
            nivelIniciado = true;

            for (JugadorRed jugador : estadosRed) {
                jugador.setSolicitoSeleccionNivel(false);
                jugador.setNivelSeleccionado(0);
            }

            servidor.enviarInicioNivel(voto1);

            iniciarNivel(voto1);
            return;
        }

        // Dibujar el fondo.
        Render.begin(stage.getCamera());
        fondo.dibujar(Render.batch);
        Render.end();

        // Actualizar y dibujar las animaciones de la interfaz.
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

        if (fondo != null) {
            fondo.dispose();
        }
    }
}
