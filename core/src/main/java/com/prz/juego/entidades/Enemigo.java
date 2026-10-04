package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

public abstract class Enemigo extends Entidad {

	protected float anchoAtaque = 100;
	protected float altoAtaque = 10;
	protected float velocidad;
	protected float distanciaDeteccion = 500;
	protected float distanciaAtaque = 15;
	protected float direccion = 1; // 1 derecha, -1 izquierda
	protected float tiempoPatrulla = 0;
	protected boolean caminandoPatrulla = false;
	protected float tiempoEstado = 0;
	protected float tiempoAntesDeAtacar = 0.3f;
	protected float tiempoEntreAtaques = 2f;
	protected boolean esperandoAtaque = false;
	protected boolean atacando = false;
	protected boolean cooldownAtaque = false;
	protected boolean yaDanio = false;
	protected boolean muerto = false;
	protected Jugador jugador;
	protected Sprite spriteAtaque;

	protected Enemigo( float x, float y, float ancho, float alto, int velocidadX, double vida, double danio, Jugador jugador, Texture textura, Texture texturaAtaque) {
		super(x, y, ancho, alto, velocidadX, vida, danio, textura);
		this.jugador = jugador;
		velocidad = velocidadX;
		duracionInvencibilidad = 1f;
		spriteAtaque = new Sprite(texturaAtaque);
		spriteAtaque.setSize(anchoAtaque, altoAtaque);
		actualizarOrientacionSprites();
	}

	@Override
	public void update(float delta) {
		if (muerto) {
			return;
		}

		guardarPosicionAnterior();

		actualizarInvencibilidad(delta);

		Rectangle boundsJugador = jugador.getBounds();

		float distancia = calcularDistanciaHorizontal(boundsJugador);

		if (atacando) {
			actualizarAtaque(delta);
		} else if (esperandoAtaque) {
			tiempoEstado += delta;
			if (tiempoEstado >= tiempoAntesDeAtacar) {
				esperandoAtaque = false;
				atacando = true;
				tiempoEstado = 0;
				yaDanio = false;
				Sonido.ESPADA.sonar();
			}
		} else if (cooldownAtaque) {
			tiempoEstado += delta;
			if (tiempoEstado >= tiempoEntreAtaques) {
				cooldownAtaque = false;
				tiempoEstado = 0;
			}

			if (distancia <= distanciaDeteccion) {
				perseguir(delta, boundsJugador);
			} else {
				patrullar(delta);
			}
		}

		else {
			if (distancia <= distanciaAtaque) {
				actualizarDireccion(boundsJugador);
				esperandoAtaque = true;
				tiempoEstado = 0;
			} else if (distancia <= distanciaDeteccion) {
				perseguir(delta, boundsJugador);
			} else {
				patrullar(delta);
			}
		}

		actualizarFisica(delta);

		if (y + alto < 0) {
			morir();
			return;
		}

		actualizarAnimacion(delta, x != xAnterior, atacando);
		actualizarOrientacionSprites();
		actualizarSpriteVisual();
		bounds.setPosition(x, y);
		if (atacando) {
			Rectangle ataque = getBoundsAtaque();
			spriteAtaque.setPosition(ataque.x, ataque.y);
		}
	}

	private float calcularDistanciaHorizontal(Rectangle boundsJugador) {
		// Jugador a la derecha
		if (boundsJugador.x > x + ancho) {
			return boundsJugador.x - (x + ancho);
		}

		// Jugador a la izquierda
		if (x > boundsJugador.x + boundsJugador.width) {
			return x - (boundsJugador.x + boundsJugador.width);
		}

		// Se superponen
		return 0;
	}

	private void actualizarDireccion(Rectangle boundsJugador) {
		float centroJugador = boundsJugador.x + boundsJugador.width / 2f;
		float centroEnemigo = x + ancho / 2f;
		if (centroJugador > centroEnemigo) {
			direccion = 1;
		} else if (centroJugador < centroEnemigo) {
			direccion = -1;
		}
	}

	private void perseguir(float delta, Rectangle boundsJugador) {
		float distancia = calcularDistanciaHorizontal(boundsJugador);
		if (distancia <= distanciaAtaque) {
			return;
		}
		float centroJugador = boundsJugador.x + boundsJugador.width / 2f;
		float centroEnemigo = x + ancho / 2f;
		float movimiento = velocidad * delta;

		// Jugador a la derecha
		if (centroJugador > centroEnemigo) {
			direccion = 1;
			if (distancia - movimiento < distanciaAtaque) {
				movimiento = distancia - distanciaAtaque;
			}
			if (movimiento > 0) {
				x += movimiento;
			}
		}

		// Jugador a la izquierda
		else if (centroJugador < centroEnemigo) {
			direccion = -1;
			if (distancia - movimiento < distanciaAtaque) {
				movimiento = distancia - distanciaAtaque;
			}
			if (movimiento > 0) {
				x -= movimiento;
			}
		}
	}

	private void patrullar(float delta) {
		tiempoPatrulla += delta;
		if (caminandoPatrulla) {
			x += direccion * velocidad * delta;
			if (tiempoPatrulla >= 2f) {
				caminandoPatrulla = false;
				tiempoPatrulla = 0;
			}
			return;
		}

		if (tiempoPatrulla >= 5f) {
			tiempoPatrulla = 0;
			if (Math.random() < 0.70) { // 70% cambia de dirección, 30% continúa
				direccion *= -1;
			}
			caminandoPatrulla = true;
		}
	}

	private void actualizarAtaque(float delta) {
		tiempoEstado += delta;
		if (!yaDanio && ataqueTocaJugador()) {
			jugador.restarVida(danio);
			yaDanio = true;
		}

		if (tiempoEstado >= 1f) {
			atacando = false;
			cooldownAtaque = true;
			tiempoEstado = 0;
		}
	}

	protected Rectangle getBoundsAtaque() {
		float ataqueY =	y + (alto / 2f) - (altoAtaque / 2f);
		float centroEnemigo = x + ancho / 2f;
		float ataqueX;

		if (direccion > 0) {
			ataqueX = centroEnemigo;
		} else {
			ataqueX = centroEnemigo - anchoAtaque;
		}

		return new Rectangle(ataqueX, ataqueY, anchoAtaque, altoAtaque);
	}

	private boolean ataqueTocaJugador() {
		Rectangle ataque = getBoundsAtaque();
		return ataque.overlaps(jugador.getBounds());
	}

	private void actualizarOrientacionSprites() {
		orientarSprite(sprite, direccion);
		orientarSprite(spriteAtaque, direccion);
	}

	public boolean estaAtacando() {
		return atacando;
	}

	@Override
	public void dibujar() {
		if (spriteVisible) {
			sprite.draw(Render.batch);
		}
	}

	public void dibujarAtaque() {
		if (!atacando) {
			return;
		}

		Rectangle ataque = getBoundsAtaque();
		spriteAtaque.setPosition(ataque.x, ataque.y);
		spriteAtaque.draw(Render.batch);
	}

    @Override
    public void dibujarHitbox(ShapeRenderer shapeRenderer) {

        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (atacando) {
            Rectangle ataque = getBoundsAtaque();
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(ataque.x, ataque.y, ataque.width, ataque.height);
        }
    }

	public float getDireccion() {
		return direccion;
	}

	public Rectangle getBoundsAtaquePublico() {
		return getBoundsAtaque();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
