package com.prz.juego.entidades.jefes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.rango.BichoRango;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.utilidades.Render;
import com.prz.juego.utilidades.Sonido;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class AtaquesExtraRetumbar {

    private static final float COOLDOWN_PRIMERA_FASE = 10f;
    private static final float COOLDOWN_SEGUNDA_FASE = 7f;
    private static final float INTERVALO_METEORITOS = 0.5f;
    private static final float DURACION_LLUVIA_INTERFASE = 15f;
    private static final float INTERVALO_FUEGUITOS_INTERFASE = 0.08f;
    private static final float DURACION_LLUVIA_SEGUNDA_FASE = 10f;
    private static final float INTERVALO_FUEGUITOS_SEGUNDA_FASE = 0.12f;
    private static final float TIEMPO_DESPEJE_FUEGUITOS = 2f;
    private static final float DURACION_AVISO_RAYO = 1.5f;
    private static final float INTERVALO_PARPADEO_RAYO = 0.15f;
    private static final float DURACION_RAYO = 0.3f;
    private static final float PAUSA_ENTRE_OLEADAS = 0.6f;
    private static final float DURACION_AVISO_LASERES = 2.2f;
    private static final float DURACION_LASERES = 0.3f;
    private static final float DURACION_FRAME_LASER = 0.12f;
    private static final float INTERVALO_PARPADEO_LASER = 0.15f;
    private static final float TIEMPO_ENTRE_METEORITOS_GRANDES = 2f;
    private static final int CANTIDAD_METEORITOS = 3;
    private static final int CANTIDAD_METEORITOS_GRANDES = 3;
    private static final int CANTIDAD_FRAMES_FUEGO_METEORITO = 2;
    private static final int CANTIDAD_RAYOS = 5;
    private static final int CANTIDAD_OLEADAS = 3;
    private static final int CANTIDAD_LASERES = 15;
    private static final int CANTIDAD_LASERES_DESACTIVADOS = 4;
    private static final int CANTIDAD_OLEADAS_LASER = 3;
    private static final float ANCHO_RAYO = 95f;
    private static final float ANCHO_LASER = 60f;
    private static final float ANCHO_SPRITE_LASER = 90f;
    private static final float DANIO = 1;

    private enum TipoAtaque {
        NINGUNO,
        METEORITOS,
        BICHOS,
        RAYOS,
        METEORITOS_GRANDES,
        LLUVIA_SEGUNDA_FASE,
        LASERES
    }

    private enum FaseRayos {
        AVISO,
        IMPACTO,
        PAUSA
    }

    private enum FaseLasers {
        AVISO,
        IMPACTO,
        PAUSA
    }

    private final Jugador JUGADOR;
    private final float ANCHO_MAPA;
    private final float ALTO_MAPA;
    private final float SUELO_Y;
    private final Consumer<Entidad> AGREGAR_ENTIDAD;
    private final Consumer<Entidad> ELIMINAR_ENTIDAD;
    private final Random RANDOM = new Random();
    private final List<Meteorito> METEORITOS = new ArrayList<>();
    private final List<MeteoritoGrande> METEORITOS_GRANDES = new ArrayList<>();
    private final List<Fueguito> FUEGUITOS = new ArrayList<>();
    private final List<BichoRango> BICHOS_INVOCADOS = new ArrayList<>();
    private final Texture TEXTURA_METEORITO;
    private final Texture TEXTURA_RAYO;
    private final Texture TEXTURA_PIXEL;
    private final Texture TEXTURA_FUEGUITO;
    private final Animation<TextureRegion> ANIMACION_FUEGO;
    private final Animation<TextureRegion> ANIMACION_LASER;

    private TipoAtaque ataqueActual = TipoAtaque.NINGUNO;
    private float tiempoCooldown;
    private float tiempoEntreMeteoritos;
    private int meteoritosInvocados;
    private float tiempoEntreMeteoritosGrandes;
    private int meteoritosGrandesInvocados;
    private float tiempoFaseRayos;
    private float tiempoParpadeoRayo;
    private int oleadaRayo;
    private FaseRayos faseRayos;
    private boolean mostrarAvisoRayo;
    private boolean lluviaFueguitosActiva;
    private boolean lluviaEsAtaque;
    private float tiempoLluviaFueguitos;
    private float tiempoSiguienteFueguito;
    private float intervaloFueguitos;
    private final float[] POSICIONES_RAYOS = new float[CANTIDAD_RAYOS];
    private final float[] POSICIONES_LASERES = new float[CANTIDAD_LASERES];
    private final boolean[] LASERES_ACTIVOS = new boolean[CANTIDAD_LASERES];
    private float tiempoFaseLaser;
    private float tiempoParpadeoLaser;
    private float tiempoAnimacionLaser;
    private int oleadaLaser;
    private FaseLasers faseLasers;
    private boolean mostrarAvisoLaser;

    public AtaquesExtraRetumbar(
            Jugador jugador,
            float anchoMapa,
            float altoMapa,
            float sueloY,
            Consumer<Entidad> agregarEntidad,
            Consumer<Entidad> eliminarEntidad) {
        this.JUGADOR = jugador;
        this.ANCHO_MAPA = anchoMapa;
        this.ALTO_MAPA = altoMapa;
        this.SUELO_Y = sueloY;
        this.AGREGAR_ENTIDAD = agregarEntidad;
        this.ELIMINAR_ENTIDAD = eliminarEntidad;

        TEXTURA_METEORITO = GestorRecursos.obtenerTextura("Jefe/meteorito.png");
        Texture texturaFuegoMeteorito = GestorRecursos.obtenerTextura("Jefe/fuegoMeteorito.png");
        TEXTURA_RAYO = GestorRecursos.obtenerTextura("Jefe/rayo.png");
        Texture texturaLaser = GestorRecursos.obtenerTextura("Jefe/laser.png");
        TEXTURA_PIXEL = GestorRecursos.obtenerPixel();
        TEXTURA_FUEGUITO = GestorRecursos.obtenerTextura("Jefe/fueguito.png");

        TextureRegion[] frames =
                TextureRegion.split(
                                texturaFuegoMeteorito,
                                texturaFuegoMeteorito.getWidth() / CANTIDAD_FRAMES_FUEGO_METEORITO,
                                texturaFuegoMeteorito.getHeight())[0];
        ANIMACION_FUEGO = new Animation<>(0.14f, frames);
        ANIMACION_FUEGO.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[][] framesLaser =
                TextureRegion.split(
                        texturaLaser,
                        texturaLaser.getWidth() / 2,
                        texturaLaser.getHeight());
        ANIMACION_LASER = new Animation<>(DURACION_FRAME_LASER, framesLaser[0]);
        ANIMACION_LASER.setPlayMode(Animation.PlayMode.LOOP);
        inicializarPosicionesLasers();
    }

    public void update(float delta, boolean segundaFase) {
        if (lluviaFueguitosActiva) {
            actualizarLluviaFueguitos(delta);
            return;
        }

        METEORITOS.removeIf(m -> !m.update(delta));
        METEORITOS_GRANDES.removeIf(m -> !m.update(delta));

        if (ataqueActual == TipoAtaque.NINGUNO) {
            tiempoCooldown += delta;
            float cooldown = segundaFase ? COOLDOWN_SEGUNDA_FASE : COOLDOWN_PRIMERA_FASE;
            if (tiempoCooldown >= cooldown) {
                iniciarAtaqueAleatorio(segundaFase);
            }
        }

        switch (ataqueActual) {
            case METEORITOS:
                actualizarAtaqueMeteoritos(delta);
                break;
            case RAYOS:
                actualizarAtaqueRayos(delta);
                break;
            case METEORITOS_GRANDES:
                actualizarAtaqueMeteoritosGrandes(delta);
                break;
            case LASERES:
                actualizarAtaqueLasers(delta);
                break;
            case BICHOS:
            case LLUVIA_SEGUNDA_FASE:
            case NINGUNO:
                break;
        }
    }

    private void iniciarAtaqueAleatorio(boolean segundaFase) {
        TipoAtaque[] ataques =
                segundaFase
                        ? new TipoAtaque[] {
                            TipoAtaque.METEORITOS_GRANDES,
                            TipoAtaque.LLUVIA_SEGUNDA_FASE,
                            TipoAtaque.LASERES
                        }
                        : new TipoAtaque[] {
                            TipoAtaque.METEORITOS, TipoAtaque.BICHOS, TipoAtaque.RAYOS
                        };
        ataqueActual = ataques[RANDOM.nextInt(ataques.length)];
        tiempoCooldown = 0f;

        switch (ataqueActual) {
            case NINGUNO:
                break;
            case METEORITOS:
                meteoritosInvocados = 0;
                tiempoEntreMeteoritos = 0f;
                break;
            case BICHOS:
                invocarBichos();
                terminarAtaque();
                break;
            case RAYOS:
                oleadaRayo = 0;
                iniciarOleadaRayos();
                break;
            case METEORITOS_GRANDES:
                meteoritosGrandesInvocados = 0;
                tiempoEntreMeteoritosGrandes = 0f;
                break;
            case LLUVIA_SEGUNDA_FASE:
                iniciarLluviaFueguitos(
                        DURACION_LLUVIA_SEGUNDA_FASE,
                        INTERVALO_FUEGUITOS_SEGUNDA_FASE,
                        true);
                break;
            case LASERES:
                iniciarAtaqueLasers();
                break;
        }
    }

    private void actualizarAtaqueMeteoritos(float delta) {
        tiempoEntreMeteoritos -= delta;
        if (tiempoEntreMeteoritos > 0f) {
            return;
        }

        invocarMeteorito();
        meteoritosInvocados++;

        if (meteoritosInvocados == CANTIDAD_METEORITOS) {
            terminarAtaque();
        } else {
            tiempoEntreMeteoritos = INTERVALO_METEORITOS;
        }
    }

    private void invocarMeteorito() {
        float margen = 45f;
        float x = margen + RANDOM.nextFloat() * Math.max(0f, ANCHO_MAPA - 2f * margen);
        METEORITOS.add(
            new Meteorito(
                x,
                ALTO_MAPA + 30f,
                SUELO_Y,
                JUGADOR,
                TEXTURA_METEORITO,
                ANIMACION_FUEGO));
    }

    private void actualizarAtaqueMeteoritosGrandes(float delta) {
        tiempoEntreMeteoritosGrandes -= delta;
        if (tiempoEntreMeteoritosGrandes > 0f) {
            return;
        }

        invocarMeteoritoGrande();
        meteoritosGrandesInvocados++;

        if (meteoritosGrandesInvocados == CANTIDAD_METEORITOS_GRANDES) {
            terminarAtaque();
        } else {
            tiempoEntreMeteoritosGrandes = TIEMPO_ENTRE_METEORITOS_GRANDES;
        }
    }

    private void invocarMeteoritoGrande() {
        float margen = 100f;
        float x =
                margen
                        + RANDOM.nextFloat()
                                * Math.max(0f, ANCHO_MAPA - 2f * margen);
        METEORITOS_GRANDES.add(
                new MeteoritoGrande(
                        x,
                        ALTO_MAPA + 30f,
                        SUELO_Y,
                        ANCHO_MAPA,
                        JUGADOR,
                        TEXTURA_METEORITO,
                        ANIMACION_FUEGO));
    }

    private void invocarBichos() {
        float anchoBicho = 70f;
        BichoRango izquierdo =
                new BichoRango(50f, ALTO_MAPA + 30f, JUGADOR, EnemigoRango.Modo.MOVIL);
        BichoRango derecho =
                new BichoRango(
                        Math.max(50f, ANCHO_MAPA - 50f - anchoBicho),
                        ALTO_MAPA + 30f,
                        JUGADOR,
                        EnemigoRango.Modo.MOVIL);
        izquierdo.configurarRetrasoInicialAntesDeAtacar(1f);
        derecho.configurarRetrasoInicialAntesDeAtacar(1f);
        derecho.configurarDireccionInicial(-1f);
        BICHOS_INVOCADOS.add(izquierdo);
        BICHOS_INVOCADOS.add(derecho);
        AGREGAR_ENTIDAD.accept(izquierdo);
        AGREGAR_ENTIDAD.accept(derecho);
    }

    public void cancelarAtaques() {
        ataqueActual = TipoAtaque.NINGUNO;
        tiempoCooldown = 0f;
        METEORITOS.clear();
        METEORITOS_GRANDES.clear();
        FUEGUITOS.clear();
        lluviaFueguitosActiva = false;
        lluviaEsAtaque = false;
        faseRayos = null;
        mostrarAvisoRayo = false;
        faseLasers = null;
        mostrarAvisoLaser = false;

        for (BichoRango bicho : BICHOS_INVOCADOS) {
            ELIMINAR_ENTIDAD.accept(bicho);
        }
        BICHOS_INVOCADOS.clear();
    }

    public void iniciarLluviaFueguitos() {
        iniciarLluviaFueguitos(
                DURACION_LLUVIA_INTERFASE, INTERVALO_FUEGUITOS_INTERFASE, false);
    }

    private void iniciarLluviaFueguitos(
            float duracion, float intervalo, boolean esAtaque) {
        lluviaFueguitosActiva = true;
        lluviaEsAtaque = esAtaque;
        intervaloFueguitos = intervalo;
        tiempoLluviaFueguitos = duracion + TIEMPO_DESPEJE_FUEGUITOS;
        tiempoSiguienteFueguito = 0f;
        FUEGUITOS.clear();
    }

    public boolean estaLluviaFueguitosActiva() {
        return lluviaFueguitosActiva;
    }

    private void actualizarLluviaFueguitos(float delta) {
        tiempoLluviaFueguitos = Math.max(0f, tiempoLluviaFueguitos - delta);
        tiempoSiguienteFueguito -= delta;

        while (tiempoLluviaFueguitos > TIEMPO_DESPEJE_FUEGUITOS
                && tiempoSiguienteFueguito <= 0f) {
            invocarFueguito();
            tiempoSiguienteFueguito += intervaloFueguitos;
        }

        FUEGUITOS.removeIf(fueguito -> !fueguito.update(delta));
        if (tiempoLluviaFueguitos <= 0f) {
            lluviaFueguitosActiva = false;
            FUEGUITOS.clear();
            if (lluviaEsAtaque) {
                terminarAtaque();
            }
        }
    }

    private void invocarFueguito() {
        float x = RANDOM.nextFloat() * Math.max(0f, ANCHO_MAPA);
        float y = ALTO_MAPA + RANDOM.nextFloat() * 250f;
        float tamano = 28f + RANDOM.nextFloat() * 24f;
        float velocidad = 300f + RANDOM.nextFloat() * 250f;
        FUEGUITOS.add(
            new Fueguito(x, y, tamano, velocidad, SUELO_Y, JUGADOR, TEXTURA_FUEGUITO));
    }

    private void iniciarOleadaRayos() {
        for (int i = 0; i < POSICIONES_RAYOS.length; i++) {
            float margen = ANCHO_RAYO / 2f;
            POSICIONES_RAYOS[i] =
                    margen + RANDOM.nextFloat() * Math.max(0f, ANCHO_MAPA - 2f * margen);
        }

        faseRayos = FaseRayos.AVISO;
        tiempoFaseRayos = 0f;
        tiempoParpadeoRayo = 0f;
        mostrarAvisoRayo = true;
    }

    private void inicializarPosicionesLasers() {
        float margen = ANCHO_SPRITE_LASER / 2f;
        float anchoUtil = Math.max(0f, ANCHO_MAPA - 2f * margen);
        for (int i = 0; i < POSICIONES_LASERES.length; i++) {
            POSICIONES_LASERES[i] =
                    margen + i * anchoUtil / (POSICIONES_LASERES.length - 1f);
            LASERES_ACTIVOS[i] = true;
        }
    }

    private void iniciarAtaqueLasers() {
        oleadaLaser = 0;
        tiempoAnimacionLaser = 0f;
        iniciarOleadaLasers();
    }

    private void iniciarOleadaLasers() {
        Arrays.fill(LASERES_ACTIVOS, true);
        int desactivados = 0;
        while (desactivados < CANTIDAD_LASERES_DESACTIVADOS) {
            int indice = RANDOM.nextInt(LASERES_ACTIVOS.length);
            if (LASERES_ACTIVOS[indice]) {
                LASERES_ACTIVOS[indice] = false;
                desactivados++;
            }
        }

        faseLasers = FaseLasers.AVISO;
        tiempoFaseLaser = 0f;
        tiempoParpadeoLaser = 0f;
        mostrarAvisoLaser = true;
    }

    private void actualizarAtaqueLasers(float delta) {
        tiempoAnimacionLaser += delta;
        tiempoFaseLaser += delta;

        if (faseLasers == FaseLasers.AVISO) {
            tiempoParpadeoLaser += delta;
            if (tiempoParpadeoLaser >= INTERVALO_PARPADEO_LASER) {
                tiempoParpadeoLaser %= INTERVALO_PARPADEO_LASER;
                mostrarAvisoLaser = !mostrarAvisoLaser;
            }
        }

        if (faseLasers == FaseLasers.AVISO
                && tiempoFaseLaser >= DURACION_AVISO_LASERES) {
            faseLasers = FaseLasers.IMPACTO;
            tiempoFaseLaser = 0f;
            daniarJugadorPorLasers();
        } else if (faseLasers == FaseLasers.IMPACTO
                && tiempoFaseLaser >= DURACION_LASERES) {
            oleadaLaser++;
            if (oleadaLaser >= CANTIDAD_OLEADAS_LASER) {
                terminarAtaque();
            } else {
                faseLasers = FaseLasers.PAUSA;
                tiempoFaseLaser = 0f;
            }
        } else if (faseLasers == FaseLasers.PAUSA
                && tiempoFaseLaser >= PAUSA_ENTRE_OLEADAS) {
            iniciarOleadaLasers();
        }
    }

    private void daniarJugadorPorLasers() {
        Rectangle boundsJugador = JUGADOR.getBounds();
        float alturaLaser = ALTO_MAPA - SUELO_Y;
        for (int i = 0; i < POSICIONES_LASERES.length; i++) {
            if (!LASERES_ACTIVOS[i]) {
                continue;
            }

            Rectangle boundsLaser =
                    new Rectangle(
                            POSICIONES_LASERES[i] - ANCHO_LASER / 2f,
                            SUELO_Y,
                            ANCHO_LASER,
                            alturaLaser);
            if (boundsJugador.overlaps(boundsLaser)) {
                JUGADOR.restarVida(DANIO);
            }
        }
        Sonido.RELAMPAGO.sonar();
    }

    private void actualizarAtaqueRayos(float delta) {
        tiempoFaseRayos += delta;

        if (faseRayos == FaseRayos.AVISO) {
            tiempoParpadeoRayo += delta;
            if (tiempoParpadeoRayo >= INTERVALO_PARPADEO_RAYO) {
                tiempoParpadeoRayo %= INTERVALO_PARPADEO_RAYO;
                mostrarAvisoRayo = !mostrarAvisoRayo;
            }
        }

        if (faseRayos == FaseRayos.AVISO && tiempoFaseRayos >= DURACION_AVISO_RAYO) {
            faseRayos = FaseRayos.IMPACTO;
            tiempoFaseRayos = 0f;
            daniarJugadorPorRayos();
        } else if (faseRayos == FaseRayos.IMPACTO
                && tiempoFaseRayos >= DURACION_RAYO) {
            oleadaRayo++;
            if (oleadaRayo >= CANTIDAD_OLEADAS) {
                terminarAtaque();
            } else {
                faseRayos = FaseRayos.PAUSA;
                tiempoFaseRayos = 0f;
            }
        } else if (faseRayos == FaseRayos.PAUSA
                && tiempoFaseRayos >= PAUSA_ENTRE_OLEADAS) {
            iniciarOleadaRayos();
        }
    }

    private void daniarJugadorPorRayos() {
        Rectangle boundsJugador = JUGADOR.getBounds();
        float alturaRayo = ALTO_MAPA - SUELO_Y;
        for (float x : POSICIONES_RAYOS) {
            Rectangle boundsRayo =
                    new Rectangle(x - ANCHO_RAYO / 2f, SUELO_Y, ANCHO_RAYO, alturaRayo);
            if (boundsJugador.overlaps(boundsRayo)) {
                JUGADOR.restarVida(DANIO);
            }
        }
        Sonido.RELAMPAGO.sonar();
    }

    private void terminarAtaque() {
        ataqueActual = TipoAtaque.NINGUNO;
        tiempoCooldown = 0f;
        faseRayos = null;
        faseLasers = null;
    }


    public void dibujar() {
        if (lluviaFueguitosActiva) {
            for (Fueguito fueguito : FUEGUITOS) {
                fueguito.dibujar();
            }
            return;
        }

        for (Meteorito meteorito : METEORITOS) {
            meteorito.dibujar();
        }
        for (MeteoritoGrande meteoritoGrande : METEORITOS_GRANDES) {
            meteoritoGrande.dibujar();
        }

        if (faseRayos == FaseRayos.AVISO) {
            dibujarAvisosRayos();
        } else if (faseRayos == FaseRayos.IMPACTO) {
            dibujarRayos();
        }

        if (ataqueActual == TipoAtaque.LASERES) {
            if (faseLasers == FaseLasers.AVISO) {
                dibujarAvisosLasers();
            } else if (faseLasers == FaseLasers.IMPACTO) {
                dibujarLasers();
            }
        }
    }

    public void dibujarHitboxes(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.MAGENTA);
        if (lluviaFueguitosActiva) {
            for (Fueguito fueguito : FUEGUITOS) {
                shapeRenderer.circle(
                    fueguito.getHitbox().x,
                    fueguito.getHitbox().y,
                    fueguito.getHitbox().radius);
            }
        }

        for (Meteorito meteorito : METEORITOS) {
            Rectangle bounds = meteorito.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        for (MeteoritoGrande meteoritoGrande : METEORITOS_GRANDES) {
            Rectangle bounds = meteoritoGrande.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            for (Rectangle boundsOla : meteoritoGrande.getBoundsOla()) {
                shapeRenderer.rect(boundsOla.x, boundsOla.y, boundsOla.width, boundsOla.height);
            }
        }

        if (ataqueActual == TipoAtaque.RAYOS && faseRayos == FaseRayos.IMPACTO) {
            float alturaRayo = ALTO_MAPA - SUELO_Y;
            for (float x : POSICIONES_RAYOS) {
                shapeRenderer.rect(
                        x - ANCHO_RAYO / 2f, SUELO_Y, ANCHO_RAYO, alturaRayo);
            }
        }

        if (ataqueActual == TipoAtaque.LASERES && faseLasers == FaseLasers.IMPACTO) {
            float alturaLaser = ALTO_MAPA - SUELO_Y;
            for (int i = 0; i < POSICIONES_LASERES.length; i++) {
                if (LASERES_ACTIVOS[i]) {
                    shapeRenderer.rect(
                            POSICIONES_LASERES[i] - ANCHO_LASER / 2f,
                            SUELO_Y,
                            ANCHO_LASER,
                            alturaLaser);
                }
            }
        }
    }

    private void dibujarAvisosRayos() {
        if (!mostrarAvisoRayo) {
            return;
        }

        Color colorAnterior = Render.batch.getColor().cpy();
        Render.batch.setColor(1f, 0f, 0f, 0.35f);
        for (float x : POSICIONES_RAYOS) {
            Render.batch.draw(
                    TEXTURA_PIXEL,
                    x - ANCHO_RAYO / 2f,
                    SUELO_Y,
                    ANCHO_RAYO,
                    ALTO_MAPA - SUELO_Y);
        }
        Render.batch.setColor(colorAnterior);
    }

    private void dibujarRayos() {
        for (float x : POSICIONES_RAYOS) {
            Render.batch.draw(
                    TEXTURA_RAYO,
                    x - ANCHO_RAYO / 2f,
                    SUELO_Y,
                    ANCHO_RAYO,
                    ALTO_MAPA - SUELO_Y);
        }
    }

    private void dibujarAvisosLasers() {
        if (!mostrarAvisoLaser) {
            return;
        }

        Color colorAnterior = Render.batch.getColor().cpy();
        Render.batch.setColor(1f, 0f, 0f, 0.35f);
        for (int i = 0; i < POSICIONES_LASERES.length; i++) {
            if (!LASERES_ACTIVOS[i]) {
                continue;
            }
            Render.batch.draw(
                    TEXTURA_PIXEL,
                    POSICIONES_LASERES[i] - ANCHO_LASER / 2f,
                    SUELO_Y,
                    ANCHO_LASER,
                    ALTO_MAPA - SUELO_Y);
        }
        Render.batch.setColor(colorAnterior);
    }

    private void dibujarLasers() {
        TextureRegion frame = ANIMACION_LASER.getKeyFrame(tiempoAnimacionLaser);
        float alturaLaser = ALTO_MAPA - SUELO_Y;
        for (int i = 0; i < POSICIONES_LASERES.length; i++) {
            if (!LASERES_ACTIVOS[i]) {
                continue;
            }
            Render.batch.draw(
                    frame,
                    POSICIONES_LASERES[i] - ANCHO_SPRITE_LASER / 2f,
                    SUELO_Y,
                    ANCHO_SPRITE_LASER,
                    alturaLaser);
        }
    }

}
