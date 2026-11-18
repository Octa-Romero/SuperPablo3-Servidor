package com.prz.juego.entidades.jugadores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.rango.Proyectil;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;

import java.util.ArrayList;

public class Pablo extends Jugador {

    private static final float ANCHO_ATAQUE = 80f;
    private static final float ALTO_ATAQUE = 10f;
    private static final float OFFSET_Y_ATAQUE = 10f;
    private static final float DURACION_ATAQUE = 0.3f;
    private static final float COOLDOWN_ATAQUE = 0.5f;
    private static final String SONIDO_ESPADA = "ESPADA";

    private final ArrayList<Proyectil> PROYECTILES_GOLPEADOS = new ArrayList<>();

    private ArrayList<Entidad> entidades;
    private ArrayList<Lanzable> proyectiles;
    private float tiempoAtaque = 0f;
    private boolean cooldownAtaque = false;
    private boolean yaDanio = false;

    public Pablo(float x, float y) {
        super(
            x,
            y,
            50,
            100,
            170,
            10,
            1,
            GestorRecursos.obtenerTextura("Personajes/Pablo/pablo_idle.png"));

        configurarSpriteVisual(100f, 100f, -5f, -45f, 0f);
        orientarSprite(direccion);
    }

    public void setEntidades(ArrayList<Entidad> entidades) {
        this.entidades = entidades;
    }

    public void setProyectiles(ArrayList<Lanzable> proyectiles) {
        this.proyectiles = proyectiles;
    }

    @Override
    public void update(float delta) {
        if (estadoRed == null) {
            return;
        }

        if (estadoRed.mueveDerecha()) {
            direccion = 1;
        }
        if (estadoRed.mueveIzquierda()) {
            direccion = -1;
        }

        super.update(delta);
        orientarSprite(direccion);
        actualizarAtaque(delta);
    }

    @Override
    public void atacar() {
        if (!accionesHabilitadas() || atacando || cooldownAtaque) {
            return;
        }

        atacando = true;
        tiempoAtaque = 0f;
        yaDanio = false;
        PROYECTILES_GOLPEADOS.clear();

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion(SONIDO_ESPADA, x, y);
        }
    }

    @Override
    public Personajes getPersonaje() {
        return Personajes.PABLO;
    }

    private void actualizarAtaque(float delta) {
        if (atacando) {
            tiempoAtaque += delta;

            if (!yaDanio) {
                comprobarDanio();
                yaDanio = true;
            }
            comprobarDanioProyectiles();

            if (tiempoAtaque >= DURACION_ATAQUE) {
                atacando = false;
                cooldownAtaque = true;
                tiempoAtaque = 0f;
            }
            return;
        }

        if (cooldownAtaque) {
            tiempoAtaque += delta;

            if (tiempoAtaque >= COOLDOWN_ATAQUE) {
                cooldownAtaque = false;
                tiempoAtaque = 0f;
            }
        }
    }

    private void comprobarDanio() {
        if (entidades == null) {
            return;
        }

        Rectangle ataque = getBoundsAtaque();

        for (Entidad entidad : entidades) {
            if (entidad == this || !(entidad instanceof Enemigo)) {
                continue;
            }

            if (((Enemigo) entidad).puedeRecibirDanio() && ataque.overlaps(entidad.getBounds())) {
                entidad.restarVida(danio);
            }
        }
    }

    private void comprobarDanioProyectiles() {
        if (proyectiles == null) {
            return;
        }

        Rectangle ataque = getBoundsAtaque();

        for (Lanzable lanzable : proyectiles) {
            if (!(lanzable instanceof Proyectil)) {
                continue;
            }

            Proyectil proyectil = (Proyectil) lanzable;

            if (proyectil.estaActiva()
                && !PROYECTILES_GOLPEADOS.contains(proyectil)
                && proyectil.chocaCon(ataque)) {
                proyectil.restarVida(danio);
                PROYECTILES_GOLPEADOS.add(proyectil);
            }
        }
    }

    public Rectangle getBoundsAtaque() {
        float ataqueY = y + alto / 2f - ALTO_ATAQUE / 2f + OFFSET_Y_ATAQUE;
        float centroJugador = x + ancho / 2f;
        float ataqueX = direccion > 0 ? centroJugador : centroJugador - ANCHO_ATAQUE;

        return new Rectangle(ataqueX, ataqueY, ANCHO_ATAQUE, ALTO_ATAQUE);
    }

    @Override
    public void reiniciar(float x, float y) {
        super.reiniciar(x, y);
        tiempoAtaque = 0f;
        cooldownAtaque = false;
        yaDanio = false;
        PROYECTILES_GOLPEADOS.clear();
    }

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {
        super.dibujarHitbox(shapeRenderer);

        if (atacando) {
            Rectangle ataque = getBoundsAtaque();
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(ataque.x, ataque.y, ataque.width, ataque.height);
        }
    }
}
