package com.prz.juego.entidades;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.hilos.JugadorRed;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Sonido;

public class Pablo extends Jugador {

    private float anchoAtaque = 100;
	private float altoAtaque = 10;
    private float tiempoAtaque = 0;
	private float duracionAtaque = 0.3f;
	private float tiempoCooldown = 0.5f;
	private boolean cooldownAtaque = false;
	private boolean yaDanio = false;
	private ArrayList<Entidad> entidades;
	private final Sprite SPRITE_ATAQUE;

	public Pablo(float x, float y) {
		super(x, y, 50, 80, 170, 10, 1, GestorRecursos.obtenerTextura("Personajes/Pablo/pablo.png"), GestorRecursos.obtenerTextura("Hud/pablo_hud.png"));
        SPRITE_ATAQUE = new Sprite(
            GestorRecursos.obtenerTextura("Personajes/Pablo/ataque_pablo.png")
        );
		SPRITE_ATAQUE.setSize(anchoAtaque, altoAtaque);
		actualizarOrientacionSprites();
	}

	public void setEntidades(ArrayList<Entidad> entidades) {
		this.entidades = entidades;
	}

	@Override
	public void update(float delta) {
        JugadorRed estadoRed = getEstadoRed();
		if (entrada != null) {
			if (estadoRed.mueveDerecha()) {
				direccion = 1;
			}
			if (estadoRed.mueveIzquierda()) {
				direccion = -1;
			}
		}

		super.update(delta);
		actualizarOrientacionSprites();

		actualizarAtaque(delta);

		if (atacando) {
			Rectangle ataque = getBoundsAtaque();
			SPRITE_ATAQUE.setPosition(ataque.x, ataque.y);
		}
	}

	@Override
	public void atacar() {
		if (!atacando && !cooldownAtaque) {
			atacando = true;
			tiempoAtaque = 0;
			yaDanio = false;
			Sonido.ESPADA.sonar();
		}
	}

	private void actualizarAtaque(float delta) {
		if (atacando) {
			tiempoAtaque += delta;
			if (!yaDanio) {
				comprobarDanio();
				yaDanio = true;
			}
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
			if (entidad instanceof Enemigo) {
				if (ataque.overlaps(
						entidad.getBounds())) {
					entidad.restarVida(danio);
				}
			}
		}
	}



	private void actualizarOrientacionSprites() {
		orientarSprite(sprite, direccion);
		orientarSprite(SPRITE_ATAQUE, direccion);
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

	public void dibujarAtaque() {
		if (!atacando) {
			return;
		}

		Rectangle ataque = getBoundsAtaque();
		SPRITE_ATAQUE.setPosition(ataque.x, ataque.y);
		SPRITE_ATAQUE.draw(com.prz.juego.utilidades.Render.batch);
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
