package com.prz.juego.sistemas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prz.juego.utilidades.Config;

public class Camara {

    private final OrthographicCamera camera;
    private final Viewport viewport;
    public static final float V_WIDTH = Config.ANCHO_BASE;
    public static final float V_HEIGHT = Config.ALTO_BASE;
    private float limiteMinX, limiteMaxX;
    private float limiteMinY, limiteMaxY;
    private float posicionGuardadaX;
    private float posicionGuardadaY;

    public Camara() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);
        camera.position.set(V_WIDTH / 2f, V_HEIGHT / 2f, 0);
        camera.update();
    }

    public void setLimitesMapa(float anchoMapaPx, float altoMapaPx) {
        this.limiteMinX = V_WIDTH / 2f;
        this.limiteMaxX = anchoMapaPx - (V_WIDTH / 2f);

        this.limiteMinY = V_HEIGHT / 2f;
        this.limiteMaxY = altoMapaPx - (V_HEIGHT / 2f);
    }

    public void seguirPersonaje(float targetX, float targetY, float delta) {
        float lerp = 10f * delta;
        camera.position.x += (targetX - camera.position.x) * lerp;
        camera.position.y += (targetY - camera.position.y) * lerp;

        if (limiteMaxX > limiteMinX) {
            camera.position.x = MathUtils.clamp(camera.position.x, limiteMinX, limiteMaxX);
        }
        if (limiteMaxY > limiteMinY) {
            camera.position.y = MathUtils.clamp(camera.position.y, limiteMinY, limiteMaxY);
        }

        camera.update();
    }

    public void actualizarTamano(int width, int height) {
        viewport.update(width, height, true);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void guardarPosicion() {
        posicionGuardadaX = camera.position.x;
        posicionGuardadaY = camera.position.y;
    }

    public void restaurarPosicion() {
        camera.position.x = posicionGuardadaX;
        camera.position.y = posicionGuardadaY;
        camera.update();
    }
}
