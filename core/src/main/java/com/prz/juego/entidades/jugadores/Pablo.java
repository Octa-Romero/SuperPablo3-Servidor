package com.prz.juego.entidades.jugadores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.Lanzable;
import com.prz.juego.entidades.enemigos.Enemigo;
import com.prz.juego.entidades.enemigos.rango.Proyectil;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;

public class Pablo extends Jugador {

    private float anchoAtaque = 100;
    private float altoAtaque = 10;
    private float direccion = 1; // 1 derecha, -1 izquierda
    private float tiempoAtaque = 0;
    private float duracionAtaque = 0.3f;
    private float tiempoCooldown = 0.5f;
    private boolean cooldownAtaque = false;
    private boolean yaDanio = false;
    private ArrayList<Entidad> entidades;
    private ArrayList<Lanzable> proyectiles;
    private final ArrayList<Proyectil> PROYECTILES_GOLPEADOS = new ArrayList<>();

    public Pablo(float x, float y) {
        super(
                x,
                y,
                50,
                80,
                170,
                10,
                1,
                GestorRecursos.obtenerTextura("Personajes/Pablo/pablo.png"),
                GestorRecursos.obtenerTextura("Hud/pablo_hud.png"));
        actualizarOrientacionSprites();
    }

    public void setEntidades(ArrayList<Entidad> entidades) {
        this.entidades = entidades;
    }

    public void setProyectiles(ArrayList<Lanzable> proyectiles) {
        this.proyectiles = proyectiles;
    }

    @Override
    public void update(float delta) {
        if (estadoRed.mueveDerecha()) {
            direccion = 1;
        }
        if (estadoRed.mueveIzquierda()) {
            direccion = -1;
        }

        super.update(delta);
        actualizarOrientacionSprites();

        actualizarAtaque(delta);
    }

    @Override
    public void atacar() {
        if (accionesHabilitadas() && !atacando && !cooldownAtaque) {
            atacando = true;
            tiempoAtaque = 0;
            yaDanio = false;
            PROYECTILES_GOLPEADOS.clear();
            Sonido.ESPADA.sonar();
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
            if (tiempoAtaque >= duracionAtaque) {
                atacando = false;
                cooldownAtaque = true;
                tiempoAtaque = 0;
            }
            return;
        }

        if (cooldownAtaque) {
            tiempoAtaque += delta;
            if (tiempoAtaque >= tiempoCooldown) {
                cooldownAtaque = false;
                tiempoAtaque = 0;
            }
        }
    }

    private void comprobarDanio() {
        if (entidades == null) {
            return;
        }

        Rectangle ataque = getBoundsAtaque();

        for (Entidad entidad : entidades) {
            if (entidad == this) {
                continue;
            }
            if (entidad instanceof Enemigo && ((Enemigo) entidad).puedeRecibirDanio()) {
                if (ataque.overlaps(entidad.getBounds())) {
                    entidad.restarVida(danio);
                }
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

    private void actualizarOrientacionSprites() {
        orientarSprite(sprite, direccion);
    }

    public Rectangle getBoundsAtaque() {
        float ataqueY = y + (alto / 2f) - (altoAtaque / 2f);
        float centroJugador = x + (ancho / 2f);
        float ataqueX;

        if (direccion > 0) {
            ataqueX = centroJugador;
        } else {
            ataqueX = centroJugador - anchoAtaque;
        }

        return new Rectangle(ataqueX, ataqueY, anchoAtaque, altoAtaque);
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

    @Override
    public void dispose() {
        super.dispose();
    }
}
