package com.prz.juego.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.utilidades.Sonido;

public abstract class Entidad {

    protected Sprite sprite;
    protected float x, y;
    protected float ancho, alto;
    protected float anchoSprite, altoSprite;
    protected float offsetSpriteXDerecha, offsetSpriteXIzquierda, offsetSpriteY;
    protected float xAnterior, yAnterior;
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

    private TextureRegion regionReposo;
    private Animation<TextureRegion> animacionReposo;
    private Animation<TextureRegion> animacionCaminar;
    private Animation<TextureRegion> animacionAtaque;
    private Animation<TextureRegion> animacionActual;
    private float tiempoAnimacion;


    protected Entidad(float x, float y, float ancho, float alto, int velocidadX, double vida, double danio, Texture textura) {
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
        regionReposo = new TextureRegion(textura);
        actualizarSpriteVisual();
    }


    protected final void configurarSpriteVisual(float anchoSprite, float altoSprite) {
        validarTamanoSprite(anchoSprite, altoSprite);

        this.anchoSprite = anchoSprite;
        this.altoSprite = altoSprite;
        offsetSpriteXDerecha = (ancho - anchoSprite) / 2f;
        offsetSpriteXIzquierda = offsetSpriteXDerecha;
        offsetSpriteY = 0f;

        actualizarSpriteVisual();
    }


    protected final void configurarSpriteVisual(float anchoSprite, float altoSprite,
                                                float offsetDerecha, float offsetIzquierda, float offsetY) {

        validarTamanoSprite(anchoSprite, altoSprite);

        this.anchoSprite = anchoSprite;
        this.altoSprite = altoSprite;
        offsetSpriteXDerecha = offsetDerecha;
        offsetSpriteXIzquierda = offsetIzquierda;
        offsetSpriteY = offsetY;

        actualizarSpriteVisual();
    }


    private void validarTamanoSprite(float anchoSprite, float altoSprite) {
        if (anchoSprite <= 0 || altoSprite <= 0) {
            throw new IllegalArgumentException("El tamano visual del sprite debe ser positivo");
        }
    }


    protected final void actualizarSpriteVisual() {
        if (sprite != null) {
            sprite.setSize(anchoSprite, altoSprite);

            float offsetX = sprite.isFlipX()
                ? offsetSpriteXIzquierda
                : offsetSpriteXDerecha;

            sprite.setPosition(
                x + offsetX,
                y + offsetSpriteY
            );
        }
    }


    protected final void configurarAnimaciones(Texture spritesheet, int anchoFrame, int altoFrame, int framesReposo, int framesCaminar, float duracionFrame) {

        int[] indicesReposo = crearIndices(0, framesReposo);
        int[] indicesCaminar = crearIndices(framesReposo, framesCaminar);

        configurarAnimaciones(
            spritesheet,
            anchoFrame,
            altoFrame,
            indicesReposo,
            indicesCaminar,
            null,
            duracionFrame
        );
    }


    protected final void configurarAnimaciones(Texture spritesheet, int anchoFrame, int altoFrame, int[] indicesReposo, int[] indicesCaminar, int[] indicesAtaque, float duracionFrame) {

        validarConfiguracionAnimaciones(
            spritesheet,
            anchoFrame,
            altoFrame,
            indicesCaminar,
            duracionFrame
        );

        TextureRegion[][] grilla = TextureRegion.split(
            spritesheet,
            anchoFrame,
            altoFrame
        );

        TextureRegion[] frames = aplanar(grilla);

        validarIndices(
            frames.length,
            indicesReposo,
            indicesCaminar,
            indicesAtaque
        );

        if (indicesReposo != null && indicesReposo.length > 0) {
            animacionReposo = crearAnimacion(
                frames,
                indicesReposo,
                duracionFrame,
                Animation.PlayMode.LOOP
            );
        }

        animacionCaminar = crearAnimacion(
            frames,
            indicesCaminar,
            duracionFrame,
            Animation.PlayMode.LOOP
        );

        if (indicesAtaque != null && indicesAtaque.length > 0) {
            animacionAtaque = crearAnimacion(
                frames,
                indicesAtaque,
                duracionFrame,
                Animation.PlayMode.NORMAL
            );
        }

        animacionActual = null;
        tiempoAnimacion = 0f;
    }


    private void validarConfiguracionAnimaciones(Texture spritesheet, int anchoFrame, int altoFrame,
                                                 int[] indicesCaminar, float duracionFrame) {

        if (spritesheet == null ||
            anchoFrame <= 0 ||
            altoFrame <= 0 ||
            duracionFrame <= 0 ||
            indicesCaminar == null ||
            indicesCaminar.length == 0 ||
            spritesheet.getWidth() % anchoFrame != 0 ||
            spritesheet.getHeight() % altoFrame != 0) {

            throw new IllegalArgumentException("Spritesheet o configuracion de frames invalida");
        }
    }


    private Animation<TextureRegion> crearAnimacion(TextureRegion[] frames, int[] indices,
                                                    float duracionFrame, Animation.PlayMode modo) {

        TextureRegion[] regiones = new TextureRegion[indices.length];

        for (int i = 0; i < indices.length; i++) {
            regiones[i] = frames[indices[i]];
        }

        Animation<TextureRegion> animacion = new Animation<>(
            duracionFrame,
            regiones
        );

        animacion.setPlayMode(modo);

        return animacion;
    }


    private int[] crearIndices(int desde, int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad de frames no puede ser negativa");
        }

        int[] indices = new int[cantidad];

        for (int i = 0; i < cantidad; i++) {
            indices[i] = desde + i;
        }

        return indices;
    }


    private void validarIndices(int totalFrames, int[]... grupos) {
        for (int[] grupo : grupos) {
            if (grupo == null) {
                continue;
            }

            for (int indice : grupo) {
                if (indice < 0 || indice >= totalFrames) {
                    throw new IllegalArgumentException("El spritesheet no contiene el frame " + indice);
                }
            }
        }
    }


    private TextureRegion[] aplanar(TextureRegion[][] grilla) {
        int cantidad = 0;

        for (TextureRegion[] fila : grilla) {
            cantidad += fila.length;
        }

        TextureRegion[] frames = new TextureRegion[cantidad];

        int indice = 0;

        for (TextureRegion[] fila : grilla) {
            for (TextureRegion frame : fila) {
                frames[indice++] = frame;
            }
        }

        return frames;
    }


    protected final void actualizarAnimacion(float delta, boolean caminando) {
        actualizarAnimacion(delta, caminando, false);
    }


    protected final void actualizarAnimacion(float delta, boolean caminando, boolean atacando) {

        Animation<TextureRegion> siguiente;

        if (atacando && animacionAtaque != null) {
            siguiente = animacionAtaque;
        } else if (caminando) {
            siguiente = animacionCaminar;
        } else {
            siguiente = animacionReposo;
        }

        if (siguiente == null) {
            if (!caminando && regionReposo != null) {
                sprite.setRegion(regionReposo);
            }
            return;
        }

        if (animacionActual != siguiente) {
            animacionActual = siguiente;
            tiempoAnimacion = 0f;
        } else {
            tiempoAnimacion += delta;
        }

        sprite.setRegion(
            animacionActual.getKeyFrame(tiempoAnimacion)
        );
    }


    public abstract void update(float delta);


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


    public void saltar() {
        velocidadY = 300;
        enSuelo = false;
        Sonido.SALTO.sonar();
    }


    protected void actualizarOrientacionSegunMovimiento() {
        orientarSprite(sprite, x - xAnterior);
    }

    protected void morir() {muerto = true;}

    public boolean estaMuerto() {
        return muerto;
    }

    public abstract void dibujar();


    public void dibujarHitbox(ShapeRenderer shapeRenderer) {

        shapeRenderer.setColor(Color.GREEN);

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        );
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
        regionReposo = null;

        animacionReposo = null;
        animacionCaminar = null;
        animacionAtaque = null;
        animacionActual = null;
    }
}
