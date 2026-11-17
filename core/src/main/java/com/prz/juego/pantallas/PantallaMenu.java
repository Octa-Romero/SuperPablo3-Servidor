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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Global;
import com.prz.juego.utilidades.Render;

public class PantallaMenu implements Screen {

    private static final float ANCHO_BOTON = 250;
    private static final float ALTO_BOTON = 50;
    private static final float ANCHO_TEXTO_ESPERA = 600;
    private static final float ALPHA_FONDO_ESPERA = 0.45f;
    private static final float ALPHA_PANEL_ESPERA = 0.65f;

    private final Navegable NAV;
    private final Servidor servidor;

    private Stage stage;
    private Stage stageEspera;
    private Imagen fondo;
    private BitmapFont fontTitulo;
    private BitmapFont fontSubtitulo;
    private BitmapFont fontBoton;
    private TextButton.TextButtonStyle estiloBoton;
    private Texture texturaBlanca;
    private Table panelEspera;
    private Label lblEspera;

    private boolean esperandoJugadores = false;
    private boolean seleccionandoPersonajes = false;

    public PantallaMenu(Navegable NAV, Servidor servidor) {
        this.NAV = NAV;
        this.servidor = servidor;
    }

    @Override
    public void show() {
        stage = new Stage(new ExtendViewport(Config.ANCHO_BASE, Config.ALTO_BASE));
        inicializarFondo();
        inicializarFuentes();
        crearTexturaBlanca();
        crearInterfaz();

        Gdx.input.setInputProcessor(stage);
    }

    private void inicializarFondo() {
        fondo = new Imagen("Menu/menuSP3.png");
        actualizarFondo();
    }

    private void actualizarFondo() {
        fondo.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        fondo.setPosition(0, 0);
    }

    private void inicializarFuentes() {
        fontTitulo = FuenteJuego.generar(64);
        fontSubtitulo = FuenteJuego.generar(36);
        fontBoton = FuenteJuego.generar(28);

        estiloBoton = new TextButton.TextButtonStyle();
        estiloBoton.font = fontBoton;
        estiloBoton.fontColor = Color.WHITE;
        estiloBoton.overFontColor = Color.GOLD;
        estiloBoton.downFontColor = Color.RED;
    }

    private void crearTexturaBlanca() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texturaBlanca = new Texture(pixmap);
        pixmap.dispose();
    }

    private TextButton crearBoton(String texto, Runnable accion) {
        TextButton boton = new TextButton(texto, estiloBoton);
        boton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                accion.run();
            }
        });
        return boton;
    }

    private void crearInterfaz() {
        Label titulo = new Label("Super Pablo 3", new Label.LabelStyle(fontTitulo, Color.GOLD));
        Label subtitulo = new Label("~ La Última Batalla ~", new Label.LabelStyle(fontSubtitulo, Color.RED));

        TextButton btnIniciarServidor = crearBoton("Iniciar servidor", this::iniciarServidor);
        TextButton btnSalir = crearBoton("Salir", () -> Gdx.app.exit());

        Table contenedor = new Table();
        contenedor.setFillParent(true);

        contenedor.add(titulo).padBottom(5).row();
        contenedor.add(subtitulo).padBottom(60).row();
        contenedor.add(btnIniciarServidor).width(ANCHO_BOTON).height(ALTO_BOTON).padBottom(25).row();
        contenedor.add(btnSalir).width(ANCHO_BOTON).height(ALTO_BOTON);

        stage.addActor(contenedor);
    }

    private void iniciarServidor() {
        servidor.iniciar();
        esperandoJugadores = true;
        mostrarCartelEspera();
    }

    private void cancelarEspera() {
        servidor.cerrar();
        finalizarEspera();
    }

    private void finalizarEspera() {
        esperandoJugadores = false;
        seleccionandoPersonajes = false;
        cerrarCartelEspera();
    }

    @Override
    public void render(float delta) {
        if (Global.empieza) {
            Global.empieza = false;
            NAV.cambiarPantalla(new PantallaJuego(NAV, servidor));
            return;
        }

        Render.limpiarPantalla();

        Render.begin(stage.getCamera());
        fondo.dibujar(Render.batch);
        Render.end();

        stage.act(delta);
        stage.draw();

        if (esperandoJugadores) {
            verificarJugadores();
            if (stageEspera != null) {
                stageEspera.act(delta);
                stageEspera.draw();
            }
        }
    }

    private void verificarJugadores() {
        int cant = servidor.getCantClientes();

        if (cant == 0) {
            if (seleccionandoPersonajes) {
                finalizarEspera();
            } else {
                setTextoEspera("Esperando jugadores (0/2)");
            }
            return;
        }

        if (cant >= 2) {
            if (!seleccionandoPersonajes) {
                seleccionandoPersonajes = true;
                setTextoEspera("Seleccionando personajes...");
                ocultarBotonCancelar();
            }
        } else {
            setTextoEspera("Esperando jugadores (" + cant + "/2)");
        }
    }

    private void setTextoEspera(String texto) {
        if (lblEspera != null) {
            lblEspera.setText(texto);
        }
    }

    private void ocultarBotonCancelar() {
        if (panelEspera == null) {
            return;
        }
        panelEspera.clearChildren();
        panelEspera.add(lblEspera).width(ANCHO_TEXTO_ESPERA);
    }

    private void mostrarCartelEspera() {
        liberarCartelEspera();

        stageEspera = new Stage(new ExtendViewport(Config.ANCHO_BASE, Config.ALTO_BASE));

        TextureRegionDrawable base = new TextureRegionDrawable(new TextureRegion(texturaBlanca));

        Table raiz = new Table();
        raiz.setFillParent(true);
        raiz.setBackground(base.tint(new Color(0, 0, 0, ALPHA_FONDO_ESPERA)));

        panelEspera = new Table();
        panelEspera.setBackground(base.tint(new Color(0, 0, 0, ALPHA_PANEL_ESPERA)));
        panelEspera.pad(40, 100, 40, 100);

        lblEspera = new Label("Esperando jugadores (0/2)", new Label.LabelStyle(fontSubtitulo, Color.WHITE));
        lblEspera.setAlignment(Align.center);

        TextButton btnCancelar = crearBoton("Cancelar", this::cancelarEspera);

        panelEspera.add(lblEspera).width(ANCHO_TEXTO_ESPERA).padBottom(30).row();
        panelEspera.add(btnCancelar).width(ANCHO_BOTON).height(ALTO_BOTON);

        raiz.add(panelEspera);
        stageEspera.addActor(raiz);

        Gdx.input.setInputProcessor(stageEspera);
    }

    private void liberarCartelEspera() {
        if (stageEspera != null) {
            stageEspera.dispose();
            stageEspera = null;
        }
        panelEspera = null;
        lblEspera = null;
    }

    private void cerrarCartelEspera() {
        liberarCartelEspera();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (stageEspera != null) {
            stageEspera.getViewport().update(width, height, true);
        }
        actualizarFondo();
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
        liberarCartelEspera();

        if (stage != null) {
            stage.dispose();
        }
        if (fondo != null) {
            fondo.dispose();
        }
        if (fontTitulo != null) {
            fontTitulo.dispose();
            fontSubtitulo.dispose();
            fontBoton.dispose();
        }
        if (texturaBlanca != null) {
            texturaBlanca.dispose();
        }
    }
}
