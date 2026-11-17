package com.prz.juego.entidades.retumbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.prz.juego.entidades.Entidad;
import com.prz.juego.entidades.enemigos.rango.BichoRango;
import com.prz.juego.entidades.enemigos.rango.EnemigoRango;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.recursos.GestorRecursos;
import com.prz.juego.red.Servidor;
import com.prz.juego.utilidades.Frames;
import com.prz.juego.utilidades.Render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AtaquesExtraRetumbar {

    private static final float COOLDOWN_PRIMERA_FASE = 10f;
    private static final float COOLDOWN_SEGUNDA_FASE = 7f;
    private static final float INTERVALO_METEORITOS = 0.5f;
    private static final float TIEMPO_ENTRE_METEORITOS_GRANDES = 2f;
    private static final float DURACION_LLUVIA_INTERFASE = 15f;
    private static final float INTERVALO_FUEGUITOS_INTERFASE = 0.08f;
    private static final float DURACION_LLUVIA_SEGUNDA_FASE = 10f;
    private static final float INTERVALO_FUEGUITOS_SEGUNDA_FASE = 0.12f;
    private static final float TIEMPO_DESPEJE_FUEGUITOS = 2f;
    private static final float DURACION_AVISO_RAYO = 1.5f;
    private static final float DURACION_AVISO_LASERES = 2.2f;
    private static final float DURACION_IMPACTO_OLEADA = 0.3f;
    private static final float PAUSA_ENTRE_OLEADAS = 0.6f;
    private static final float INTERVALO_PARPADEO = 0.15f;
    private static final int CANTIDAD_METEORITOS = 3;
    private static final int CANTIDAD_METEORITOS_GRANDES = 3;
    private static final int CANTIDAD_FRAMES = 2;
    private static final int CANTIDAD_RAYOS = 5;
    private static final int CANTIDAD_OLEADAS = 3;
    private static final int CANTIDAD_LASERES = 15;
    private static final int CANTIDAD_LASERES_DESACTIVADOS = 4;
    private static final float ANCHO_RAYO = 95f;
    private static final float ANCHO_LASER = 60f;
    private static final float ANCHO_SPRITE_LASER = 90f;
    private static final float DANIO = 1f;
    private static final float MARGEN_METEORITO = 45f;
    private static final float MARGEN_METEORITO_GRANDE = 100f;
    private static final float MARGEN_BICHO = 50f;
    private static final float ANCHO_BICHO = 70f;
    private static final float DESPLAZAMIENTO_APARICION = 30f;
    private static final float TAMANO_MINIMO_FUEGUITO = 28f;
    private static final float VARIACION_TAMANO_FUEGUITO = 24f;
    private static final float VELOCIDAD_MINIMA_FUEGUITO = 300f;
    private static final float VARIACION_VELOCIDAD_FUEGUITO = 250f;
    private static final float VARIACION_ALTURA_FUEGUITO = 250f;

    private enum TipoAtaque {
        NINGUNO,
        METEORITOS,
        BICHOS,
        RAYOS,
        METEORITOS_GRANDES,
        LLUVIA_SEGUNDA_FASE,
        LASERES
    }

    private enum FaseOleada {
        AVISO,
        IMPACTO,
        PAUSA
    }

    private static final TipoAtaque[] ATAQUES_PRIMERA_FASE = {
        TipoAtaque.METEORITOS, TipoAtaque.BICHOS, TipoAtaque.RAYOS
    };
    private static final TipoAtaque[] ATAQUES_SEGUNDA_FASE = {
        TipoAtaque.METEORITOS_GRANDES, TipoAtaque.LLUVIA_SEGUNDA_FASE, TipoAtaque.LASERES
    };

    private final Jugador JUGADOR;
    private final Jugador[] JUGADORES;
    private final float ANCHO_MAPA;
    private final float ALTO_MAPA;
    private final float SUELO_Y;
    private final Consumer<Entidad> AGREGAR_ENTIDAD;
    private final Consumer<Entidad> ELIMINAR_ENTIDAD;
    private final Texture TEXTURA_METEORITO;
    private final TextureRegion REGION_PIXEL;
    private final TextureRegion REGION_RAYO;
    private final TextureRegion REGION_LASER;
    private final TextureRegion REGION_FUEGUITO;
    private final TextureRegion REGION_FUEGO_METEORITO;
    private final Rectangle RECTANGULO_TEMPORAL = new Rectangle();
    private final float[] POSICIONES_RAYOS = new float[CANTIDAD_RAYOS];
    private final float[] POSICIONES_LASERES = new float[CANTIDAD_LASERES];
    private final boolean[] LASERES_ACTIVOS = new boolean[CANTIDAD_LASERES];
    private final List<Meteorito> METEORITOS = new ArrayList<>();
    private final List<MeteoritoGrande> METEORITOS_GRANDES = new ArrayList<>();
    private final List<Meteorito> METEORITOS_NUEVOS = new ArrayList<>();
    private final List<MeteoritoGrande> METEORITOS_GRANDES_NUEVOS = new ArrayList<>();
    private final List<Integer> METEORITOS_ELIMINADOS = new ArrayList<>();
    private final List<Fueguito> FUEGUITOS = new ArrayList<>();
    private final ArrayList<Fueguito> FUEGUITOS_NUEVOS = new ArrayList<>();
    private final ArrayList<Integer> FUEGUITOS_ELIMINADOS = new ArrayList<>();
    private final List<BichoRango> BICHOS_INVOCADOS = new ArrayList<>();

    private TipoAtaque ataqueActual = TipoAtaque.NINGUNO;
    private float tiempoCooldown = 0f;
    private float tiempoEntreMeteoritos = 0f;
    private int meteoritosInvocados = 0;
    private float tiempoEntreMeteoritosGrandes = 0f;
    private int meteoritosGrandesInvocados = 0;
    private int siguienteIdMeteorito = 1;
    private int siguienteIdFueguito = 1;
    private FaseOleada faseOleada;
    private float tiempoFaseOleada = 0f;
    private float tiempoParpadeoOleada = 0f;
    private int oleadaActual = 0;
    private boolean mostrarAvisoOleada = false;
    private boolean lluviaFueguitosActiva = false;
    private boolean lluviaEsAtaque = false;
    private float tiempoLluviaFueguitos = 0f;
    private float tiempoSiguienteFueguito = 0f;
    private float intervaloFueguitos = 0f;

    public AtaquesExtraRetumbar(
        Jugador jugador,
        Jugador[] jugadores,
        float anchoMapa,
        float altoMapa,
        float sueloY,
        Consumer<Entidad> agregarEntidad,
        Consumer<Entidad> eliminarEntidad) {
        this.JUGADOR = jugador;
        this.JUGADORES = jugadores;
        this.ANCHO_MAPA = anchoMapa;
        this.ALTO_MAPA = altoMapa;
        this.SUELO_Y = sueloY;
        this.AGREGAR_ENTIDAD = agregarEntidad;
        this.ELIMINAR_ENTIDAD = eliminarEntidad;

        TEXTURA_METEORITO = GestorRecursos.obtenerTextura("Jefe/meteorito.png");
        REGION_PIXEL = new TextureRegion(GestorRecursos.obtenerPixel());
        REGION_RAYO = new TextureRegion(GestorRecursos.obtenerTextura("Jefe/rayo.png"));
        REGION_LASER =
            Frames.primero(GestorRecursos.obtenerTextura("Jefe/laser.png"), CANTIDAD_FRAMES);
        REGION_FUEGUITO =
            Frames.primero(GestorRecursos.obtenerTextura("Jefe/fueguito.png"), CANTIDAD_FRAMES);
        REGION_FUEGO_METEORITO =
            Frames.primero(
                GestorRecursos.obtenerTextura("Jefe/fuegoMeteorito.png"), CANTIDAD_FRAMES);

        inicializarPosicionesLaseres();
    }

    public void update(float delta, boolean segundaFase) {
        if (lluviaFueguitosActiva) {
            actualizarLluviaFueguitos(delta);
            return;
        }

        METEORITOS.removeIf(m -> registrarSiTermino(m.update(delta), m.getId(), METEORITOS_ELIMINADOS));
        METEORITOS_GRANDES.removeIf(
            m -> registrarSiTermino(m.update(delta), m.getId(), METEORITOS_ELIMINADOS));

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
            case METEORITOS_GRANDES:
                actualizarAtaqueMeteoritosGrandes(delta);
                break;
            case RAYOS:
            case LASERES:
                actualizarOleada(delta);
                break;
            default:
                break;
        }
    }

    private boolean registrarSiTermino(boolean sigueActivo, int id, List<Integer> eliminados) {
        if (!sigueActivo) {
            eliminados.add(id);
        }
        return !sigueActivo;
    }

    private void iniciarAtaqueAleatorio(boolean segundaFase) {
        TipoAtaque[] ataques = segundaFase ? ATAQUES_SEGUNDA_FASE : ATAQUES_PRIMERA_FASE;
        ataqueActual = ataques[MathUtils.random(ataques.length - 1)];
        tiempoCooldown = 0f;

        switch (ataqueActual) {
            case METEORITOS:
                meteoritosInvocados = 0;
                tiempoEntreMeteoritos = 0f;
                break;
            case BICHOS:
                invocarBichos();
                terminarAtaque();
                break;
            case RAYOS:
            case LASERES:
                oleadaActual = 0;
                iniciarOleada();
                break;
            case METEORITOS_GRANDES:
                meteoritosGrandesInvocados = 0;
                tiempoEntreMeteoritosGrandes = 0f;
                break;
            case LLUVIA_SEGUNDA_FASE:
                iniciarLluviaFueguitos(
                    DURACION_LLUVIA_SEGUNDA_FASE, INTERVALO_FUEGUITOS_SEGUNDA_FASE, true);
                break;
            default:
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

        if (meteoritosInvocados >= CANTIDAD_METEORITOS) {
            terminarAtaque();
        } else {
            tiempoEntreMeteoritos = INTERVALO_METEORITOS;
        }
    }

    private void actualizarAtaqueMeteoritosGrandes(float delta) {
        tiempoEntreMeteoritosGrandes -= delta;
        if (tiempoEntreMeteoritosGrandes > 0f) {
            return;
        }

        invocarMeteoritoGrande();
        meteoritosGrandesInvocados++;

        if (meteoritosGrandesInvocados >= CANTIDAD_METEORITOS_GRANDES) {
            terminarAtaque();
        } else {
            tiempoEntreMeteoritosGrandes = TIEMPO_ENTRE_METEORITOS_GRANDES;
        }
    }

    private float posicionXAleatoria(float margen) {
        return margen + MathUtils.random(Math.max(0f, ANCHO_MAPA - 2f * margen));
    }

    private void invocarMeteorito() {
        Meteorito meteorito =
            new Meteorito(
                posicionXAleatoria(MARGEN_METEORITO),
                ALTO_MAPA + DESPLAZAMIENTO_APARICION,
                SUELO_Y,
                JUGADORES,
                TEXTURA_METEORITO,
                REGION_FUEGO_METEORITO);
        meteorito.setId(siguienteIdMeteorito++);

        METEORITOS.add(meteorito);
        METEORITOS_NUEVOS.add(meteorito);
    }

    private void invocarMeteoritoGrande() {
        MeteoritoGrande meteorito =
            new MeteoritoGrande(
                posicionXAleatoria(MARGEN_METEORITO_GRANDE),
                ALTO_MAPA + DESPLAZAMIENTO_APARICION,
                SUELO_Y,
                ANCHO_MAPA,
                JUGADORES,
                TEXTURA_METEORITO,
                REGION_FUEGO_METEORITO);
        meteorito.setId(siguienteIdMeteorito++);

        METEORITOS_GRANDES.add(meteorito);
        METEORITOS_GRANDES_NUEVOS.add(meteorito);
    }

    private void invocarBichos() {
        float y = ALTO_MAPA + DESPLAZAMIENTO_APARICION;
        BichoRango izquierdo =
            new BichoRango(MARGEN_BICHO, y, JUGADOR, JUGADORES, EnemigoRango.Modo.MOVIL);
        BichoRango derecho =
            new BichoRango(
                Math.max(MARGEN_BICHO, ANCHO_MAPA - MARGEN_BICHO - ANCHO_BICHO),
                y,
                JUGADOR,
                JUGADORES,
                EnemigoRango.Modo.MOVIL);

        izquierdo.configurarRetrasoInicialAntesDeAtacar(1f);
        derecho.configurarRetrasoInicialAntesDeAtacar(1f);
        derecho.configurarDireccionInicial(-1);

        BICHOS_INVOCADOS.add(izquierdo);
        BICHOS_INVOCADOS.add(derecho);
        AGREGAR_ENTIDAD.accept(izquierdo);
        AGREGAR_ENTIDAD.accept(derecho);
    }

    public void cancelarAtaques() {
        ataqueActual = TipoAtaque.NINGUNO;
        tiempoCooldown = 0f;

        for (Meteorito meteorito : METEORITOS) {
            METEORITOS_ELIMINADOS.add(meteorito.getId());
        }
        for (MeteoritoGrande meteorito : METEORITOS_GRANDES) {
            METEORITOS_ELIMINADOS.add(meteorito.getId());
        }
        METEORITOS.clear();
        METEORITOS_GRANDES.clear();

        limpiarFueguitos();
        lluviaFueguitosActiva = false;
        lluviaEsAtaque = false;
        faseOleada = null;
        mostrarAvisoOleada = false;

        for (BichoRango bicho : BICHOS_INVOCADOS) {
            ELIMINAR_ENTIDAD.accept(bicho);
        }
        BICHOS_INVOCADOS.clear();
    }

    private void terminarAtaque() {
        ataqueActual = TipoAtaque.NINGUNO;
        tiempoCooldown = 0f;
        faseOleada = null;
    }

    public void iniciarLluviaFueguitos() {
        iniciarLluviaFueguitos(DURACION_LLUVIA_INTERFASE, INTERVALO_FUEGUITOS_INTERFASE, false);
    }

    private void iniciarLluviaFueguitos(float duracion, float intervalo, boolean esAtaque) {
        lluviaFueguitosActiva = true;
        lluviaEsAtaque = esAtaque;
        intervaloFueguitos = intervalo;
        tiempoLluviaFueguitos = duracion + TIEMPO_DESPEJE_FUEGUITOS;
        tiempoSiguienteFueguito = 0f;
        limpiarFueguitos();
    }

    public boolean estaLluviaFueguitosActiva() {
        return lluviaFueguitosActiva;
    }

    private void actualizarLluviaFueguitos(float delta) {
        tiempoLluviaFueguitos = Math.max(0f, tiempoLluviaFueguitos - delta);
        tiempoSiguienteFueguito -= delta;

        while (tiempoLluviaFueguitos > TIEMPO_DESPEJE_FUEGUITOS && tiempoSiguienteFueguito <= 0f) {
            invocarFueguito();
            tiempoSiguienteFueguito += intervaloFueguitos;
        }

        FUEGUITOS.removeIf(
            f -> registrarSiTermino(f.update(delta), f.getId(), FUEGUITOS_ELIMINADOS));

        if (tiempoLluviaFueguitos <= 0f) {
            lluviaFueguitosActiva = false;
            limpiarFueguitos();

            if (lluviaEsAtaque) {
                terminarAtaque();
            }
        }
    }

    private void invocarFueguito() {
        Fueguito fueguito =
            Fueguito.crearVertical(
                MathUtils.random(ANCHO_MAPA),
                ALTO_MAPA + MathUtils.random(VARIACION_ALTURA_FUEGUITO),
                TAMANO_MINIMO_FUEGUITO + MathUtils.random(VARIACION_TAMANO_FUEGUITO),
                VELOCIDAD_MINIMA_FUEGUITO + MathUtils.random(VARIACION_VELOCIDAD_FUEGUITO),
                SUELO_Y,
                JUGADORES,
                REGION_FUEGUITO);
        fueguito.setId(siguienteIdFueguito++);

        FUEGUITOS.add(fueguito);
        FUEGUITOS_NUEVOS.add(fueguito);
    }

    private void limpiarFueguitos() {
        for (Fueguito fueguito : FUEGUITOS) {
            FUEGUITOS_ELIMINADOS.add(fueguito.getId());
        }
        FUEGUITOS.clear();
    }

    private void inicializarPosicionesLaseres() {
        float margen = ANCHO_SPRITE_LASER / 2f;
        float anchoUtil = Math.max(0f, ANCHO_MAPA - 2f * margen);

        for (int i = 0; i < POSICIONES_LASERES.length; i++) {
            POSICIONES_LASERES[i] = margen + i * anchoUtil / (POSICIONES_LASERES.length - 1f);
            LASERES_ACTIVOS[i] = true;
        }
    }

    private void iniciarOleada() {
        if (ataqueActual == TipoAtaque.RAYOS) {
            for (int i = 0; i < POSICIONES_RAYOS.length; i++) {
                POSICIONES_RAYOS[i] = posicionXAleatoria(ANCHO_RAYO / 2f);
            }
        } else {
            Arrays.fill(LASERES_ACTIVOS, true);
            int desactivados = 0;
            while (desactivados < CANTIDAD_LASERES_DESACTIVADOS) {
                int indice = MathUtils.random(LASERES_ACTIVOS.length - 1);
                if (LASERES_ACTIVOS[indice]) {
                    LASERES_ACTIVOS[indice] = false;
                    desactivados++;
                }
            }
        }

        faseOleada = FaseOleada.AVISO;
        tiempoFaseOleada = 0f;
        tiempoParpadeoOleada = 0f;
        mostrarAvisoOleada = true;
    }

    private void actualizarOleada(float delta) {
        if (faseOleada == null) {
            return;
        }

        tiempoFaseOleada += delta;

        switch (faseOleada) {
            case AVISO:
                tiempoParpadeoOleada += delta;
                if (tiempoParpadeoOleada >= INTERVALO_PARPADEO) {
                    tiempoParpadeoOleada %= INTERVALO_PARPADEO;
                    mostrarAvisoOleada = !mostrarAvisoOleada;
                }

                float duracionAviso =
                    ataqueActual == TipoAtaque.RAYOS
                        ? DURACION_AVISO_RAYO
                        : DURACION_AVISO_LASERES;
                if (tiempoFaseOleada >= duracionAviso) {
                    faseOleada = FaseOleada.IMPACTO;
                    tiempoFaseOleada = 0f;
                    impactarOleada();
                }
                break;

            case IMPACTO:
                if (tiempoFaseOleada >= DURACION_IMPACTO_OLEADA) {
                    oleadaActual++;
                    if (oleadaActual >= CANTIDAD_OLEADAS) {
                        terminarAtaque();
                    } else {
                        faseOleada = FaseOleada.PAUSA;
                        tiempoFaseOleada = 0f;
                    }
                }
                break;

            case PAUSA:
                if (tiempoFaseOleada >= PAUSA_ENTRE_OLEADAS) {
                    iniciarOleada();
                }
                break;
        }
    }

    private void impactarOleada() {
        if (ataqueActual == TipoAtaque.RAYOS) {
            daniarEnColumnas(POSICIONES_RAYOS, null, ANCHO_RAYO);
        } else {
            daniarEnColumnas(POSICIONES_LASERES, LASERES_ACTIVOS, ANCHO_LASER);
        }

        if (Servidor.instancia != null) {
            Servidor.instancia.enviarSonidoConPosicion("RELAMPAGO", ANCHO_MAPA / 2f, SUELO_Y);
        }
    }

    private void daniarEnColumnas(float[] posiciones, boolean[] activos, float ancho) {
        float altura = ALTO_MAPA - SUELO_Y;

        for (Jugador j : JUGADORES) {
            if (j.estaMuerto()) {
                continue;
            }

            for (int i = 0; i < posiciones.length; i++) {
                if (activos != null && !activos[i]) {
                    continue;
                }

                RECTANGULO_TEMPORAL.set(posiciones[i] - ancho / 2f, SUELO_Y, ancho, altura);
                if (j.getBounds().overlaps(RECTANGULO_TEMPORAL)) {
                    j.restarVida(DANIO);
                }
            }
        }
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
        for (MeteoritoGrande meteorito : METEORITOS_GRANDES) {
            meteorito.dibujar();
        }

        if (faseOleada == FaseOleada.AVISO) {
            dibujarAvisoOleada();
        } else if (faseOleada == FaseOleada.IMPACTO) {
            if (ataqueActual == TipoAtaque.RAYOS) {
                dibujarColumnas(REGION_RAYO, POSICIONES_RAYOS, null, ANCHO_RAYO);
            } else {
                dibujarColumnas(REGION_LASER, POSICIONES_LASERES, LASERES_ACTIVOS, ANCHO_SPRITE_LASER);
            }
        }
    }

    private void dibujarAvisoOleada() {
        if (!mostrarAvisoOleada) {
            return;
        }

        float colorAnterior = Render.batch.getPackedColor();
        Render.batch.setColor(1f, 0f, 0f, 0.35f);

        if (ataqueActual == TipoAtaque.RAYOS) {
            dibujarColumnas(REGION_PIXEL, POSICIONES_RAYOS, null, ANCHO_RAYO);
        } else {
            dibujarColumnas(REGION_PIXEL, POSICIONES_LASERES, LASERES_ACTIVOS, ANCHO_LASER);
        }

        Render.batch.setPackedColor(colorAnterior);
    }

    private void dibujarColumnas(
        TextureRegion region, float[] posiciones, boolean[] activos, float ancho) {
        float altura = ALTO_MAPA - SUELO_Y;

        for (int i = 0; i < posiciones.length; i++) {
            if (activos == null || activos[i]) {
                Render.batch.draw(region, posiciones[i] - ancho / 2f, SUELO_Y, ancho, altura);
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
            dibujarRectangulo(shapeRenderer, meteorito.getBounds());
        }

        for (MeteoritoGrande meteorito : METEORITOS_GRANDES) {
            dibujarRectangulo(shapeRenderer, meteorito.getBounds());
            for (Rectangle ola : meteorito.getBoundsOla()) {
                dibujarRectangulo(shapeRenderer, ola);
            }
        }

        if (faseOleada == FaseOleada.IMPACTO) {
            if (ataqueActual == TipoAtaque.RAYOS) {
                dibujarHitboxColumnas(shapeRenderer, POSICIONES_RAYOS, null, ANCHO_RAYO);
            } else {
                dibujarHitboxColumnas(shapeRenderer, POSICIONES_LASERES, LASERES_ACTIVOS, ANCHO_LASER);
            }
        }
    }

    private void dibujarRectangulo(ShapeRenderer shapeRenderer, Rectangle rectangulo) {
        shapeRenderer.rect(rectangulo.x, rectangulo.y, rectangulo.width, rectangulo.height);
    }

    private void dibujarHitboxColumnas(
        ShapeRenderer shapeRenderer, float[] posiciones, boolean[] activos, float ancho) {
        float altura = ALTO_MAPA - SUELO_Y;

        for (int i = 0; i < posiciones.length; i++) {
            if (activos == null || activos[i]) {
                shapeRenderer.rect(posiciones[i] - ancho / 2f, SUELO_Y, ancho, altura);
            }
        }
    }

    public String getFaseRayos() {
        return ataqueActual == TipoAtaque.RAYOS && faseOleada != null
            ? faseOleada.name()
            : "NINGUNO";
    }

    public boolean mostrarAvisoRayos() {
        return ataqueActual == TipoAtaque.RAYOS && mostrarAvisoOleada;
    }

    public float[] getPosicionesRayos() {
        return POSICIONES_RAYOS;
    }

    public String getFaseLasers() {
        return ataqueActual == TipoAtaque.LASERES && faseOleada != null
            ? faseOleada.name()
            : "NINGUNO";
    }

    public boolean mostrarAvisoLaser() {
        return ataqueActual == TipoAtaque.LASERES && mostrarAvisoOleada;
    }

    public boolean[] getLasersActivos() {
        return LASERES_ACTIVOS;
    }

    public List<Meteorito> getMeteoritosActivos() {
        return METEORITOS;
    }

    public List<MeteoritoGrande> getMeteoritosGrandesActivos() {
        return METEORITOS_GRANDES;
    }

    public List<Meteorito> getMeteoritosNuevos() {
        List<Meteorito> nuevos = new ArrayList<>(METEORITOS_NUEVOS);
        METEORITOS_NUEVOS.clear();
        return nuevos;
    }

    public List<MeteoritoGrande> getMeteoritosGrandesNuevos() {
        List<MeteoritoGrande> nuevos = new ArrayList<>(METEORITOS_GRANDES_NUEVOS);
        METEORITOS_GRANDES_NUEVOS.clear();
        return nuevos;
    }

    public List<Integer> getMeteoritosEliminados() {
        List<Integer> eliminados = new ArrayList<>(METEORITOS_ELIMINADOS);
        METEORITOS_ELIMINADOS.clear();
        return eliminados;
    }

    public ArrayList<Fueguito> getFueguitosNuevos() {
        ArrayList<Fueguito> nuevos = new ArrayList<>(FUEGUITOS_NUEVOS);
        FUEGUITOS_NUEVOS.clear();
        return nuevos;
    }

    public ArrayList<Fueguito> getFueguitosActivos() {
        return new ArrayList<>(FUEGUITOS);
    }

    public ArrayList<Integer> getFueguitosEliminados() {
        ArrayList<Integer> eliminados = new ArrayList<>(FUEGUITOS_ELIMINADOS);
        FUEGUITOS_ELIMINADOS.clear();
        return eliminados;
    }
}
