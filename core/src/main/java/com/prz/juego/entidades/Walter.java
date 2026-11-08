package com.prz.juego.entidades;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.sistemas.Colisiones;
import com.prz.juego.utilidades.Sonido;

public class Walter extends Jugador {

    private static final float DURACION_ATAQUE = 0.35f;
    private float tiempoAtaque;
    private static final float COOLDOWN_ATAQUE = 0.8f;
    private float tiempoCooldown;
    private boolean cooldownAtaque;
    private int idOrbes = 1;
    private final ArrayList<OrbeMagica> ORBES_NUEVOS = new ArrayList<>();
    private final ArrayList<OrbeMagica> ORBES = new ArrayList<>();
    private final ArrayList<Integer> ID_ORBES_ELIMINADOS = new ArrayList<>();
    private ArrayList<Entidad> entidades;
    private Colisiones colision;

    public Walter(float x, float y) {
        super(x, y, 50, 80, 140, 5, 0.5, GestorRecursos.obtenerTextura("Personajes/Walter/walter.png"), GestorRecursos.obtenerTextura("Hud/walter_hud.png"));
        configurarAnimaciones(GestorRecursos.obtenerTextura("Personajes/Walter/walter_spritesheet2.png"), 540, 450, new int[]{0}, new int[]{2, 3, 4, 5, 6, 7}, new int[]{1}, 0.12f);
        configurarSpriteVisual(96f, 80f, 0f, -46f, 0f);
    }

    public void configurarAtaque(ArrayList<Entidad> entidades, Colisiones colision) {
        this.entidades = entidades;
        this.colision = colision;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (atacando && (tiempoAtaque += delta) >= DURACION_ATAQUE) {
            atacando = false;
            tiempoAtaque = 0f;
            cooldownAtaque = true;
            tiempoCooldown = 0f;
        }

        if (cooldownAtaque) {
            tiempoCooldown += delta;
            if (tiempoCooldown >= COOLDOWN_ATAQUE) {
                cooldownAtaque = false;
                tiempoCooldown = 0f;
            }
        }

        actualizarOrbes(delta);
    }

    private void actualizarOrbes(float delta) {
        for (int i = ORBES.size() - 1; i >= 0; i--) {
            OrbeMagica orbe = ORBES.get(i);
            orbe.update(delta);
            if (!orbe.estaActiva()) {
                ID_ORBES_ELIMINADOS.add(orbe.getId());
                orbe.dispose();
                ORBES.remove(i);
            }
        }
    }

    @Override
    public void atacar() {
        if (!atacando && !cooldownAtaque) {
            atacando = true;
            tiempoAtaque = 0f;
            lanzarOrbe();
        }
    }

    private void lanzarOrbe() {
        if (entidades == null || colision == null) {
            return;
        }
        int direccion = mirandoDerecha ? 1 : -1;
        float posicionX;
        if (direccion > 0) {
            posicionX = x + ancho + 50f;
        } else {
            posicionX = x - 50f;
        }
        float posicionY = y + alto / 2f - 10f;

        Sonido.BOLA_FUEGO.sonar();
        agregarOrbes(new OrbeMagica(idOrbes++, posicionX, posicionY, direccion, this, colision, entidades));
    }

    private void agregarOrbes(OrbeMagica orbe)
    {
        ORBES.add(orbe);
        ORBES_NUEVOS.add(orbe);
    }

    public void dibujarOrbes() {
        for (OrbeMagica orbe : ORBES) {
            orbe.dibujar();
        }
    }

    public void dibujarOrbesHitbox(ShapeRenderer shapeRenderer) {
        for (OrbeMagica orbe : ORBES) {
            orbe.dibujarHitbox(shapeRenderer);
        }
    }

    public ArrayList<OrbeMagica> getOrbesNuevos() {
        ArrayList<OrbeMagica> copia = new ArrayList<>(ORBES_NUEVOS);
        ORBES_NUEVOS.clear();
        return copia;
    }

    public ArrayList<OrbeMagica> getOrbesActivos()
    {
        return new ArrayList<>(ORBES);
    }

    public ArrayList<Integer> getOrbesEliminados() {
        ArrayList<Integer> copia = new ArrayList<>(ID_ORBES_ELIMINADOS);
        ID_ORBES_ELIMINADOS.clear();
        return copia;
    }

    @Override
    public void dispose() {
        super.dispose();

        for (OrbeMagica o : ORBES) {
            o.dispose();
        }

        ORBES.clear();
    }
}
