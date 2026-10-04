package com.prz.juego.sistemas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.prz.juego.entidades.Jugador;
import com.prz.juego.recursos.GestorRecursos;

import java.util.ArrayList;

public class Hud {

    private final Jugador jugador;
    private final Stage stage;
    private final FitViewport viewport = new FitViewport(1280, 720, new OrthographicCamera());

    private final Texture corazonLleno = GestorRecursos.obtenerTextura("Hud/corazon_lleno.png");
    private final Texture corazonMitad = GestorRecursos.obtenerTextura("Hud/corazon_mitad.png");
    private final Texture corazonVacio = GestorRecursos.obtenerTextura("Hud/corazon_vacio.png");

    private final TextureRegionDrawable llenoDrawable;
    private final TextureRegionDrawable mitadDrawable;
    private final TextureRegionDrawable vacioDrawable;

    private final ArrayList<Image> corazones = new ArrayList<>();

    private Table tablaPrincipal;
    private Table contenedorCorazones;

    private final float TAMANO_CARA = 120;
    private final float TAMANO_CORAZON = 50;
    private final float ESPACIO_CORAZONES = 5;

    private double ultimaVida = -1;

    public Hud(Jugador jugador, SpriteBatch batch) {
        this.jugador = jugador;

        stage = new Stage(viewport, batch);

        // cache de drawables (se crea una sola vez para ser reutilizado)
        llenoDrawable = new TextureRegionDrawable(new TextureRegion(corazonLleno));
        mitadDrawable = new TextureRegionDrawable(new TextureRegion(corazonMitad));
        vacioDrawable = new TextureRegionDrawable(new TextureRegion(corazonVacio));

        tablaPrincipal = new Table();
        tablaPrincipal.top().left();
        tablaPrincipal.setFillParent(true);
        tablaPrincipal.pad(20);

        Image imagenCara = new Image(jugador.getTEXTURA_HUD());

        contenedorCorazones = new Table();

        tablaPrincipal.add(imagenCara).size(TAMANO_CARA, TAMANO_CARA);
        tablaPrincipal.add(contenedorCorazones).padLeft(10).center();

        stage.addActor(tablaPrincipal);

        crearCorazones();
        actualizarCorazones();
    }

    private void crearCorazones() {
        int max = (int) Math.ceil(jugador.getVidaMaxima());

        for (int i = 0; i < max; i++) {
            Image corazon = new Image(vacioDrawable);

            corazones.add(corazon);

            contenedorCorazones.add(corazon)
                .size(TAMANO_CORAZON, TAMANO_CORAZON)
                .padRight(ESPACIO_CORAZONES);
        }
    }

    private void actualizarCorazones() {

        double vida = jugador.getVida();

        for (int i = 0; i < corazones.size(); i++) {

            double vidaRestante = vida - i;

            if (vidaRestante >= 1.0) {
                corazones.get(i).setDrawable(llenoDrawable);
            } else if (vidaRestante >= 0.5) {
                corazones.get(i).setDrawable(mitadDrawable);
            } else {
                corazones.get(i).setDrawable(vacioDrawable);
            }
        }
    }

    public void dibujar() {

        double vidaActual = jugador.getVida();

        if (vidaActual != ultimaVida) {
            actualizarCorazones();
            ultimaVida = vidaActual;
        }

        stage.act();
        stage.draw();
    }

    public void actualizarTamano(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
