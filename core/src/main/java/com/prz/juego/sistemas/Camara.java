package com.prz.juego.sistemas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prz.juego.utilidades.Config;

public class Camara {

    private final OrthographicCamera CAMERA;
    private final Viewport VIEWPORT;
    public static final float V_WIDTH = Config.ANCHO_BASE;
    public static final float V_HEIGHT = Config.ALTO_BASE;
    private float limiteMinX, limiteMaxX;
    private float limiteMinY, limiteMaxY;
    private float posicionGuardadaX;
    private float posicionGuardadaY;

    public Camara() {
        CAMERA = new OrthographicCamera();
        VIEWPORT = new FitViewport(V_WIDTH, V_HEIGHT, CAMERA);
        CAMERA.position.set(V_WIDTH / 2f, V_HEIGHT / 2f, 0);
        CAMERA.update();
    }

    public void setLimitesMapa(float anchoMapaPx, float altoMapaPx) {
        this.limiteMinX = V_WIDTH / 2f;
        this.limiteMaxX = anchoMapaPx - (V_WIDTH / 2f);

        this.limiteMinY = V_HEIGHT / 2f;
        this.limiteMaxY = altoMapaPx - (V_HEIGHT / 2f);
    }

    public void seguirPersonaje(float targetX, float targetY, float delta) {
        float lerp = Math.min(1f, 10f * delta);

        CAMERA.position.x += (targetX - CAMERA.position.x) * lerp;
        CAMERA.position.y += (targetY - CAMERA.position.y) * lerp;

        if (limiteMaxX <= limiteMinX) {
            CAMERA.position.x = limiteMinX;
        } else {
            CAMERA.position.x = MathUtils.clamp(CAMERA.position.x, limiteMinX, limiteMaxX);
        }

        if (limiteMaxY <= limiteMinY) {
            CAMERA.position.y = limiteMinY;
        } else {
            CAMERA.position.y = MathUtils.clamp(CAMERA.position.y, limiteMinY, limiteMaxY);
        }

        CAMERA.update();
    }

    public void actualizarTamano(int width, int height) {
        VIEWPORT.update(width, height, false);
        CAMERA.update();
    }

    public OrthographicCamera getCamera() {
        return CAMERA;
    }

    public void guardarPosicion() {
        posicionGuardadaX = CAMERA.position.x;
        posicionGuardadaY = CAMERA.position.y;
    }

    public void restaurarPosicion() {
        CAMERA.position.x = posicionGuardadaX;
        CAMERA.position.y = posicionGuardadaY;
        CAMERA.update();
    }
}
