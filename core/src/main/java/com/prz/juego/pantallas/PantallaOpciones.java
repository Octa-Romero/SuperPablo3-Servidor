package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.Musica;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public class PantallaOpciones implements Screen {

    private Stage stage;
    private Imagen fondo;
    private BitmapFont fontTitulo;
    private BitmapFont fontBoton;
    private BitmapFont fontVolumen;
    private final Navegable nav;
    private final Screen pantallaAnterior;
    private final boolean VENIA_DE_JUEGO;
    private boolean mostrarResoluciones = false;
    private Table contenedor;
    private Table listaResoluciones;
    private Label titulo;
    private Label labelMusica;
    private Label labelSonido;
    private TextButton btnResolucion;
    private TextButton btnVolver;
    private TextButton btnToggleFullscreen;
    private Slider sliderMusica;
    private Slider sliderSonido;
    private Texture texturaSlider;
    private Texture texturaKnob;

    public PantallaOpciones(Navegable nav, Screen pantallaAnterior, boolean VENIA_DE_JUEGO) {
        this.nav = nav;
        this.pantallaAnterior = pantallaAnterior;
        this.VENIA_DE_JUEGO = VENIA_DE_JUEGO;
    }

    @Override
    public void show() {
        inicializarStage();
        inicializarFondo();
        inicializarFuentes();
        crearUI();

        Gdx.input.setInputProcessor(stage);
    }

    private void inicializarStage() {
        stage = new Stage(new FitViewport(Config.ANCHO_BASE, Config.ALTO_BASE));
    }

    private void inicializarFondo() {
        fondo = new Imagen("Menu/menuSP3.png");
        actualizarFondo();
    }

    private void actualizarFondo() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        fondo.setSize(w, h);
        fondo.setPosition(0, 0);
    }

    private void inicializarFuentes() {
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(3.5f);

        fontBoton = new BitmapFont();
        fontBoton.getData().setScale(2f);

        fontVolumen = new BitmapFont();
        fontVolumen.getData().setScale(1.5f);
    }

    private void crearUI() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = fontBoton;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.GOLD;
        style.downFontColor = Color.RED;

        titulo = new Label("OPCIONES", new Label.LabelStyle(fontTitulo, Color.GOLD));
        btnResolucion = new TextButton("RESOLUCIÓN: " + Config.getAncho() + "x" + Config.getAlto(), style);
        btnToggleFullscreen = new TextButton(Config.fullscreen ? "MODO VENTANA" : "PANTALLA COMPLETA", style);
        btnVolver = new TextButton("VOLVER", style);

        Slider.SliderStyle sliderStyle = crearEstiloSlider();
        sliderMusica = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        sliderSonido = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        sliderMusica.setValue(Musica.getVolumen());
        sliderSonido.setValue(Sonido.getVolumen());

        labelMusica = new Label(obtenerPorcentaje(Musica.getVolumen()), new Label.LabelStyle(fontVolumen, Color.WHITE));
        labelSonido = new Label(obtenerPorcentaje(Sonido.getVolumen()), new Label.LabelStyle(fontVolumen, Color.WHITE));

        sliderMusica.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    float volumen = sliderMusica.getValue();
                    Musica.setVolumen(volumen);
                    labelMusica.setText(obtenerPorcentaje(volumen));
                }
            }
        );


        sliderSonido.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    float volumen = sliderSonido.getValue();
                    Sonido.setVolumen(volumen);
                    labelSonido.setText(obtenerPorcentaje(volumen));
                }
            }
        );

        listaResoluciones = new Table();
        listaResoluciones.setVisible(false);

        agregarResolucion("800x450", 800, 450, style);
        agregarResolucion("1280x720", 1280, 720, style);
        agregarResolucion("1600x900", 1600, 900, style);
        agregarResolucion("1920x1080", 1920, 1080, style);

        btnResolucion.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    mostrarResoluciones = !mostrarResoluciones;
                    listaResoluciones.setVisible(mostrarResoluciones);
                    actualizarInterfaz();
                }
            }
        );

        btnToggleFullscreen.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    Config.toggleFullscreen();

                    btnToggleFullscreen.setText(Config.fullscreen ? "MODO VENTANA" : "PANTALLA COMPLETA");

                    mostrarResoluciones = false;
                    listaResoluciones.setVisible(false);

                    btnResolucion.setText("RESOLUCIÓN: " + Config.getAncho() + "x" + Config.getAlto());

                    stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

                    actualizarFondo();
                    actualizarInterfaz();
                }
            }
        );

        btnVolver.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();
                    if (VENIA_DE_JUEGO) {
                        nav.cambiarPantalla(pantallaAnterior);
                        PantallaJuego pantallaJuego = (PantallaJuego) pantallaAnterior;
                        pantallaJuego.restaurarPosicionCamara();
                        pantallaJuego.setPausa(true);
                    } else {
                        nav.cambiarPantalla(new PantallaMenu(nav));
                    }
                }
            }
        );

        contenedor = new Table();
        contenedor.setFillParent(true);

        actualizarInterfaz();

        stage.addActor(contenedor);
    }

    private void actualizarInterfaz() {
        contenedor.clearChildren();
        contenedor.add(titulo).padBottom(25).row();
        contenedor.add(new Label("VOLUMEN MÚSICA", new Label.LabelStyle(fontVolumen, Color.WHITE))).padBottom(5).row();

        Table filaMusica = new Table();
        filaMusica.add(sliderMusica).width(250).height(30);
        filaMusica.add(labelMusica).width(60).padLeft(10);

        contenedor.add(filaMusica).padBottom(15).row();

        contenedor.add(new Label("VOLUMEN EFECTOS", new Label.LabelStyle(fontVolumen, Color.WHITE))).padBottom(5).row();

        Table filaSonido = new Table();

        filaSonido.add(sliderSonido).width(250).height(30);
        filaSonido.add(labelSonido).width(60).padLeft(10);

        contenedor.add(filaSonido).padBottom(20).row();

        contenedor.add(btnToggleFullscreen).size(250, 50).padBottom(10).row();

        if (!Config.fullscreen) {
            contenedor.add(btnResolucion).size(250, 50).padBottom(10).row();
            if (mostrarResoluciones) {
                contenedor.add(listaResoluciones).row();
            }
        }

        contenedor.add(btnVolver).size(250, 50);
    }

    private void agregarResolucion(String texto, int ancho, int alto, TextButton.TextButtonStyle style) {
        TextButton btn = new TextButton(texto, style);

        btn.addListener(
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Sonido.CLICK.sonar();

                    Config.setWindowed(ancho, alto);

                    btnResolucion.setText("RESOLUCIÓN: " + texto);

                    mostrarResoluciones = false;

                    listaResoluciones.setVisible(false);

                    stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

                    actualizarFondo();
                    actualizarInterfaz();

                    btnToggleFullscreen.setText("PANTALLA COMPLETA");
                }
            }
        );

        listaResoluciones.add(btn).size(250, 40).row();
    }

    private Slider.SliderStyle crearEstiloSlider() {

        Slider.SliderStyle style = new Slider.SliderStyle();
        Pixmap pixmapFondo = new Pixmap(250, 10, Pixmap.Format.RGBA8888);
        pixmapFondo.setColor(Color.DARK_GRAY);
        pixmapFondo.fill();
        texturaSlider = new Texture(pixmapFondo);
        pixmapFondo.dispose();

        Pixmap pixmapKnob = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        pixmapKnob.setColor(Color.WHITE);
        pixmapKnob.fillCircle(10, 10, 10);
        texturaKnob = new Texture(pixmapKnob);
        pixmapKnob.dispose();

        style.background = new TextureRegionDrawable(new TextureRegion(texturaSlider));
        style.knob = new TextureRegionDrawable(new TextureRegion(texturaKnob));

        return style;
    }

    private String obtenerPorcentaje(float volumen) {
        int porcentaje = Math.round(volumen * 100);
        return porcentaje + "%";
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
        actualizarFondo();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();

        fondo.dispose();

        fontTitulo.dispose();
        fontBoton.dispose();
        fontVolumen.dispose();

        if (texturaSlider != null) {
            texturaSlider.dispose();
        }

        if (texturaKnob != null) {
            texturaKnob.dispose();
        }
    }
}
