package com.prz.juego.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.prz.juego.principal.Navegable;
import com.prz.juego.recursos.Imagen;
import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.FuenteJuego;
import com.prz.juego.utilidades.Render;

public class PantallaFinal implements Screen {

    private Stage stage;
    private Imagen fondo;

    private BitmapFont fontTitulo;
    private BitmapFont fontSubtitulo;
    private BitmapFont fontRelato;
    private BitmapFont fontFin;
    private BitmapFont fontCreditos;

    private Label titulo;
    private Label subtitulo;
    private Label texto;

    private final Navegable NAV;

    private int escenaActual = 0;

    private final float SEPARACION_CREDITOS = 10f;

    private final String[] escenas = {
        "La batalla había llegado a su fin.",
        "El sacrificio de Pablo salvó al reino y selló para siempre la maldición de El Retumbar.",
        "El Retumbar fue derrotado, y con él desapareció la oscuridad que durante tanto tiempo había amenazado estas tierras.",
        "Sin embargo, la victoria tuvo un precio. Pablo entregó todo para que los demás pudieran vivir en paz.",
        "Su nombre jamás sería olvidado, porque incluso cuando los héroes desaparecen, sus leyendas permanecen."
    };

    public PantallaFinal(Navegable NAV) {
        this.NAV = NAV;
    }

    @Override
    public void show() {
        inicializarStage();
        inicializarFondo();
        inicializarFuentes();
        crearInterfaz();

        Gdx.input.setInputProcessor(null);
    }

    private void inicializarStage() {
        stage = new Stage(
            new ExtendViewport(Config.ANCHO_BASE, Config.ALTO_BASE)
        );
    }

    private void inicializarFondo() {
        fondo = new Imagen("Menu/fondoFinal.png");
        actualizarFondo();
    }

    private void actualizarFondo() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        fondo.setSize(w, h);
        fondo.setPosition(0, 0);
    }

    private void inicializarFuentes() {
        fontTitulo = FuenteJuego.generar(48);
        fontSubtitulo = FuenteJuego.generar(36);
        fontRelato = FuenteJuego.generar(40);
        fontFin = FuenteJuego.generar(64);
        fontCreditos = FuenteJuego.generar(26);
    }

    private void crearInterfaz() {

        titulo = new Label(
            "Super Pablo 3",
            new Label.LabelStyle(fontTitulo, Color.GOLD)
        );

        subtitulo = new Label(
            "~ La Última Batalla ~",
            new Label.LabelStyle(fontSubtitulo, Color.RED)
        );

        texto = new Label(
            "",
            new Label.LabelStyle(fontRelato, Color.WHITE)
        );

        texto.setAlignment(Align.center);
        texto.setWrap(true);

        stage.addActor(titulo);
        stage.addActor(subtitulo);
        stage.addActor(texto);

        titulo.getColor().a = 0f;
        subtitulo.getColor().a = 0f;
        texto.getColor().a = 0f;

        posicionarEncabezado();

        mostrarSiguienteEscena();
    }

    private void posicionarEncabezado() {

        float anchoPantalla = stage.getViewport().getWorldWidth();
        float altoPantalla = stage.getViewport().getWorldHeight();

        titulo.pack();
        subtitulo.pack();

        titulo.setPosition(
            (anchoPantalla - titulo.getWidth()) / 2f,
            altoPantalla - titulo.getHeight() - 50f
        );

        subtitulo.setPosition(
            (anchoPantalla - subtitulo.getWidth()) / 2f,
            titulo.getY() - subtitulo.getHeight() - 5f
        );
    }

    private void actualizarTexto() {

        float anchoPantalla = stage.getViewport().getWorldWidth();
        float altoPantalla = stage.getViewport().getWorldHeight();

        // Margen de 40 píxeles a cada lado
        float anchoDisponible = anchoPantalla - 80f;

        texto.setWrap(true);
        texto.setAlignment(Align.center);

        texto.setWidth(anchoDisponible);

        // Calcula la altura según el ancho establecido
        texto.invalidateHierarchy();
        texto.pack();

        // Vuelve a establecer el ancho después del pack
        texto.setWidth(anchoDisponible);

        texto.setPosition(
            (anchoPantalla - anchoDisponible) / 2f,
            (altoPantalla - texto.getHeight()) / 2f
        );
    }

    private void mostrarSiguienteEscena() {

        if (escenaActual >= escenas.length) {
            mostrarFin();
            return;
        }

        texto.setStyle(
            new Label.LabelStyle(fontRelato, Color.WHITE)
        );

        texto.setText(escenas[escenaActual]);

        actualizarTexto();

        texto.getColor().a = 0f;

        texto.addAction(
            Actions.sequence(
                Actions.fadeIn(2f),
                Actions.delay(4f),
                Actions.fadeOut(2f),
                Actions.run(() -> {
                    escenaActual++;
                    mostrarSiguienteEscena();
                })
            )
        );
    }

    private void mostrarFin() {

        texto.setStyle(
            new Label.LabelStyle(fontFin, Color.WHITE)
        );

        texto.setText("Fin.");

        actualizarTexto();

        texto.getColor().a = 0f;

        texto.addAction(
            Actions.sequence(
                Actions.fadeIn(3f),
                Actions.delay(5f),
                Actions.fadeOut(2f),
                Actions.run(this::mostrarCreditos)
            )
        );
    }

    private void mostrarCreditos() {

        titulo.setText("Super Pablo 3");
        subtitulo.setText("~ La Última Batalla ~");

        posicionarEncabezado();

        titulo.getColor().a = 0f;
        subtitulo.getColor().a = 0f;

        titulo.addAction(Actions.fadeIn(3f));
        subtitulo.addAction(Actions.fadeIn(3f));

        texto.setStyle(
            new Label.LabelStyle(fontCreditos, Color.WHITE)
        );

        texto.setText(
            "Desarrollo\n\n" +

                "Octavio Romero\n" +
                "Interfaz, sistema multijugador\n\n" +

                "Santiago Papale\n" +
                "Arte, sprites, mapas e interfaz\n\n" +

                "Matias Rodrigo Zurita\n" +
                "Enemigos, música y sonido\n\n\n" +

                "Agradecimientos especiales\n\n" +

                "Leandro Zelaya\n" +
                "Por sus diseños de los juegos anteriores,\n" +
                "reutilizados en esta entrega."
        );

        actualizarTexto();

        // Acercar el encabezado a los créditos
        float altoPantalla = stage.getViewport().getWorldHeight();

        float altoTotal = titulo.getHeight()
            + 5f
            + subtitulo.getHeight()
            + SEPARACION_CREDITOS
            + texto.getHeight();

        float inicio = (altoPantalla + altoTotal) / 2f;

        titulo.setPosition(
            (stage.getViewport().getWorldWidth() - titulo.getWidth()) / 2f,
            inicio - titulo.getHeight()
        );

        subtitulo.setPosition(
            (stage.getViewport().getWorldWidth() - subtitulo.getWidth()) / 2f,
            titulo.getY() - subtitulo.getHeight() - 5f
        );

        texto.setPosition(
            (stage.getViewport().getWorldWidth() - texto.getWidth()) / 2f,
            subtitulo.getY() - texto.getHeight() - SEPARACION_CREDITOS
        );

        texto.getColor().a = 0f;

        texto.addAction(
            Actions.sequence(
                Actions.fadeIn(3f),
                Actions.delay(18f),
                Actions.fadeOut(3f),
                Actions.run(() -> {
                    NAV.cambiarPantalla(new PantallaMenu(NAV));
                })
            )
        );
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
        posicionarEncabezado();

        if (texto.getText().length() > 0) {
            actualizarTexto();
        }
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

        if (fondo != null) {
            fondo.dispose();
        }

        if (fontTitulo != null) {
            fontTitulo.dispose();
        }

        if (fontSubtitulo != null) {
            fontSubtitulo.dispose();
        }

        if (fontRelato != null) {
            fontRelato.dispose();
        }

        if (fontFin != null) {
            fontFin.dispose();
        }

        if (fontCreditos != null) {
            fontCreditos.dispose();
        }
    }
}
