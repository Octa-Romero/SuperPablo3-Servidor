package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.utilidades.Animacion;

public abstract class Entidad {

    protected Sprite sprite;
    protected float x, y;
    protected float ancho, alto;
    protected float anchoSprite, altoSprite;
    protected float offsetSpriteXDerecha, offsetSpriteXIzquierda, offsetSpriteY;
    protected float xAnterior, yAnterior;
    protected int direccion = 1;
    protected int velocidadX;
    protected float velocidadY = 0;
    protected final float GRAVEDAD = 400;
    protected boolean enSuelo = true;
    protected boolean chocoPared = false;
    protected Rectangle bounds;
    protected double vida;
    protected double vidaMaxima;
    protected boolean muerto = false;
    protected double danio;
    protected float tiempoInvencibilidad = 0;
    protected float duracionInvencibilidad = 2f;
    protected float tiempoParpadeo = 0;
    protected boolean spriteVisible = true;
    protected boolean mirandoDerecha = true;
    protected final Animacion ANIMACION;

    protected Entidad(
            float x,
            float y,
            float ancho,
            float alto,
            int velocidadX,
            double vida,
            double danio,
            Texture textura) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.anchoSprite = ancho;
        this.altoSprite = alto;
        this.velocidadX = velocidadX;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.danio = danio;
        xAnterior = x;
        yAnterior = y;
        bounds = new Rectangle(x, y, ancho, alto);
        sprite = new Sprite(textura);
        ANIMACION = new Animacion(textura);
        actualizarSpriteVisual();
    }

    protected void configurarSpriteVisual(float anchoSprite, float altoSprite) {
        validarTamanoSprite(anchoSprite, altoSprite);

        this.anchoSprite = anchoSprite;
        this.altoSprite = altoSprite;
        offsetSpriteXDerecha = (ancho - anchoSprite) / 2f;
        offsetSpriteXIzquierda = offsetSpriteXDerecha;
        offsetSpriteY = 0f;

        actualizarSpriteVisual();
    }

    protected void configurarSpriteVisual(
            float anchoSprite,
            float altoSprite,
            float offsetDerecha,
            float offsetIzquierda,
            float offsetY) {

        validarTamanoSprite(anchoSprite, altoSprite);

        this.anchoSprite = anchoSprite;
        this.altoSprite = altoSprite;
        offsetSpriteXDerecha = offsetDerecha;
        offsetSpriteXIzquierda = offsetIzquierda;
        offsetSpriteY = offsetY;

        actualizarSpriteVisual();
    }

    public void escalarSpriteVisual(float escala) {
        if (escala <= 0) {
            throw new IllegalArgumentException("La escala visual debe ser positiva");
        }

        configurarSpriteVisual(
                anchoSprite * escala,
                altoSprite * escala,
                offsetSpriteXDerecha * escala,
                offsetSpriteXIzquierda * escala,
                offsetSpriteY * escala);
    }

    public void escalar(float escala) {
        if (escala <= 0) {
            throw new IllegalArgumentException("La escala debe ser positiva");
        }

        ancho *= escala;
        alto *= escala;
        bounds.setSize(ancho, alto);
        escalarSpriteVisual(escala);
    }

    private void validarTamanoSprite(float anchoSprite, float altoSprite) {
        if (anchoSprite <= 0 || altoSprite <= 0) {
            throw new IllegalArgumentException("El tamano visual del sprite debe ser positivo");
        }
    }

    protected void actualizarSpriteVisual() {
        if (sprite != null) {
            sprite.setSize(anchoSprite, altoSprite);

            float offsetX = sprite.isFlipX() ? offsetSpriteXIzquierda : offsetSpriteXDerecha;

            sprite.setPosition(x + offsetX, y + offsetSpriteY);
        }
    }

    public abstract void update(float delta);

    public boolean debeVerificarColisionesMapa() {
        return true;
    }

    protected void actualizarFisica(float delta) {

        if (!enSuelo) {
            velocidadY -= GRAVEDAD * delta;
        }

        y += velocidadY * delta;

        bounds.setPosition(x, y);
    }

    protected void actualizarInvencibilidad(float delta) {

        if (tiempoInvencibilidad > 0) {

            tiempoInvencibilidad -= delta;
            tiempoParpadeo += delta;

            if (tiempoParpadeo >= 0.1f) {
                spriteVisible = !spriteVisible;
                tiempoParpadeo = 0;
            }

        } else {

            tiempoInvencibilidad = 0;
            tiempoParpadeo = 0;
            spriteVisible = true;
        }
    }

    public void restarVida(double cantidad) {

        if (tiempoInvencibilidad > 0) {
            return;
        }

        vida -= cantidad;

        tiempoInvencibilidad = duracionInvencibilidad;
        tiempoParpadeo = 0;
        spriteVisible = true;

        if (vida <= 0) {
            vida = 0;
            morir();
        }
    }

    protected void guardarPosicionAnterior() {
        xAnterior = x;
        yAnterior = y;
    }

    public void limitarMovimientoHorizontal(float limiteIzquierdo, float limiteDerecho) {
        float maximoX = limiteDerecho - bounds.width;
        if (maximoX < limiteIzquierdo) {
            maximoX = limiteIzquierdo;
        }

        float xLimitado = MathUtils.clamp(x, limiteIzquierdo, maximoX);
        chocoPared =
            chocoPared
                || xLimitado != x
                || xLimitado <= limiteIzquierdo
                || xLimitado >= maximoX;
        x = xLimitado;
        bounds.setPosition(x, y);
        actualizarSpriteVisual();
    }

    protected void orientarSprite(Sprite sprite, float direccion) {

        if (sprite == null) {
            return;
        }

        if (direccion != 0) {
            mirandoDerecha = direccion > 0;
        }

        boolean debeInvertirse = !mirandoDerecha;

        if (sprite.isFlipX() != debeInvertirse) {
            sprite.flip(true, false);
        }
    }

    public abstract void saltar();

    protected void actualizarOrientacionSegunMovimiento() {
        orientarSprite(sprite, x - xAnterior);
    }

    protected void morir() {
        muerto = true;
    }

    public boolean estaMuerto() {
        return muerto;
    }

    public abstract void dibujar();

    public void dibujarHitbox(ShapeRenderer shapeRenderer) {

        shapeRenderer.setColor(Color.GREEN);

        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getXAnterior() {
        return xAnterior;
    }

    public float getYAnterior() {
        return yAnterior;
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }

    public int getDireccion()
    {
        return this.direccion;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getVelocidadX() {
        return velocidadX;
    }

    public float getVelocidadY() {
        return velocidadY;
    }

    public double getVida() {
        return vida;
    }

    public double getVidaMaxima() {
        return vidaMaxima;
    }

    public double getDanio() {
        return danio;
    }

    public boolean estaEnSuelo() {
        return enSuelo;
    }

    public boolean chocoPared() {
        return chocoPared;
    }

    public void setChocoPared(boolean valor) {
        chocoPared = valor;
    }

    public void setVelocidadX(int valor) {
        velocidadX = valor;
    }

    public void setVelocidadY(float valor) {
        velocidadY = valor;
    }

    public void setEnSuelo(boolean valor) {
        enSuelo = valor;
    }

    public void setX(float x) {
        this.x = x;
        bounds.setX(x);
        actualizarSpriteVisual();
    }

    public void setY(float y) {
        this.y = y;
        bounds.setY(y);
        actualizarSpriteVisual();
    }

    public void dispose() {
        sprite = null;
        ANIMACION.dispose();
    }
}
