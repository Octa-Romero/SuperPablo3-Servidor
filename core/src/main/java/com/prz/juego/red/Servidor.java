package com.prz.juego.red;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.retumbar.Mano;
import com.prz.juego.entidades.jugadores.Jugador;

import java.net.InetAddress;
import java.util.function.BiConsumer;

public class Servidor {

    public static volatile Servidor instancia;

    private static final int CANTIDAD_JUGADORES = 2;
    private static final String[] RUTAS_NIVELES = {
        "Niveles/Niveles/Nivel1.tmx",
        "Niveles/Niveles/Nivel2.tmx",
        "Niveles/Niveles/Nivel3.tmx",
        "Niveles/Niveles/Nivel4.tmx",
        "Niveles/Niveles/Nivel5.tmx",
        "Niveles/Niveles/Nivel6.tmx",
        "Niveles/Niveles/Nivel7.tmx"
    };

    private volatile HiloServidor hilo;
    private volatile Runnable callbackVolverHub;
    private volatile BiConsumer<InetAddress, Integer> callbackSnapshotEntidades;
    private volatile String nivelActual;

    public void iniciar() {
        if (hilo == null) {
            HiloServidor nuevo = new HiloServidor();
            nuevo.start();
            hilo = nuevo;
        }
    }

    private void enviarATodos(String mensaje) {
        HiloServidor hiloActual = hilo;
        if (hiloActual != null) {
            hiloActual.enviarMensajeATodos(mensaje);
        }
    }

    private void enviarA(JugadorRed destinatario, String mensaje) {
        HiloServidor hiloActual = hilo;
        if (hiloActual != null && destinatario != null) {
            hiloActual.enviarMensaje(mensaje, destinatario.getIp(), destinatario.getPuerto());
        }
    }

    private void establecerEstadoJugadores(String estado) {
        HiloServidor hiloActual = hilo;
        if (hiloActual == null) {
            return;
        }

        for (JugadorRed jugador : hiloActual.getJugadores()) {
            if (jugador != null) {
                jugador.setEstadoActual(estado);
            }
        }
    }

    public boolean isJuegoPausado() {
        HiloServidor hiloActual = hilo;
        return hiloActual != null && hiloActual.hayJugadorNoListo();
    }

    public void enviarPosicion(Jugador jugador) {
        enviarATodos("POS;" + jugador.getPersonaje().name() + ";" + jugador.getX() + ";" + jugador.getY() + ";"
            + jugador.getDireccion() + ";" + jugador.estaEnSuelo() + ";" + jugador.estaTrepando());
    }

    public void enviarEstadoAtaque(Jugador jugador) {
        enviarATodos("ATAQUE;" + jugador.getPersonaje().name() + ";" + jugador.estaAtacando() + ";"
            + jugador.getTipoAnimacion() + ";" + jugador.getTiempoAnimacion());
    }

    public void enviarEstadoVida(Jugador jugador) {
        enviarATodos("VIDA;" + jugador.getPersonaje().name() + ";" + jugador.getVida());
    }

    public void enviarEstadoInvencibilidad(Jugador jugador) {
        enviarATodos("INVENCIBILIDAD;" + jugador.getPersonaje().name() + ";" + jugador.estaInvencible() + ";"
            + jugador.estaSpriteVisible());
    }

    public void enviarEstadoTumba(String personaje, float centroX, float y) {
        enviarATodos("ESTADO_TUMBA;" + personaje + ";" + centroX + ";" + y);
    }

    public void enviarCreacionEnemigos(int id, TipoEnemigo tipo, float x, float y, double vida) {
        enviarATodos("CREAR_ENEMIGO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + vida);
    }

    public void enviarEstadoEnemigo(int id, float x, float y, int direccion, boolean estaAtacando, double vida,
                                    boolean spriteVisible, int tipoAnimacion, float tiempoAnimacion) {
        enviarATodos("ESTADO_ENEMIGO;" + id + ";" + x + ";" + y + ";" + direccion + ";" + estaAtacando + ";"
            + vida + ";" + spriteVisible + ";" + tipoAnimacion + ";" + tiempoAnimacion);
    }

    public void enviarEliminacionJefe(int id) {
        enviarATodos("ELIMINAR_ENEMIGO;" + id);
    }

    public void enviarCreacionProyectil(int id, String origen, float x, float y, float direccion) {
        enviarATodos("CREAR_PROYECTIL;" + id + ";" + origen + ";" + x + ";" + y + ";" + direccion);
    }

    public void enviarEstadoProyectil(int id, float x, float y) {
        enviarATodos("ESTADO_PROYECTIL;" + id + ";" + x + ";" + y);
    }

    public void enviarEliminacionProyectil(int id) {
        enviarATodos("ELIMINAR_PROYECTIL;" + id);
    }

    public void enviarEstadoCentroHub(JugadorRed estadoRed, boolean valor) {
        enviarA(estadoRed, "CENTRO_HUB;" + valor);
    }

    public void enviarEstadoSeleccion(JugadorRed estadoRed) {
        enviarA(estadoRed, "ESTADO_SELECCION;" + estadoRed.solicitaSeleccion());
    }

    public void enviarInicioSeleccionNivel() {
        establecerEstadoJugadores(JugadorRed.ESTADO_SELECCION_NIVEL);
        enviarATodos("ABRIR_SELECCION_NIVEL");
    }

    public void enviarVotosNivel(JugadorRed destinatario, int votoLocal, int votoRemoto) {
        enviarA(destinatario, "VOTOS_NIVEL;" + votoLocal + ";" + votoRemoto);
    }

    public void enviarInicioNivel(int voto) {
        nivelActual = voto >= 1 && voto <= RUTAS_NIVELES.length ? RUTAS_NIVELES[voto - 1] : null;
        establecerEstadoJugadores(JugadorRed.ESTADO_JUGANDO);
        enviarATodos("INICIAR_NIVEL;" + voto);
    }

    public String getNivelActual() {
        return nivelActual;
    }

    public void resetearNivelActual() {
        nivelActual = null;
    }

    public void enviarCreacionJefe(float x, float y, double vida) {
        enviarATodos("CREAR_JEFE;" + x + ";" + y + ";" + vida);
    }

    public void enviarEstadoJefe(float x, float y, double vida, boolean visible, float escala, String faseEspecial) {
        enviarATodos("ESTADO_JEFE;" + x + ";" + y + ";" + vida + ";" + visible + ";" + escala + ";" + faseEspecial);
    }

    public void enviarEliminacionJefe() {
        enviarATodos("ELIMINAR_JEFE");
    }

    public void enviarEstadoMano(Mano.Lado lado, float x, float y, double vida, boolean visible, float escala,
                                 Mano.EstadoAtaque estadoAtaque, Mano.EstiloAtaque estiloAtaque,
                                 boolean mostrarFuegoGolpe) {
        enviarATodos("ESTADO_MANO;" + lado + ";" + x + ";" + y + ";" + vida + ";" + visible + ";" + escala + ";"
            + estadoAtaque + ";" + estiloAtaque + ";" + mostrarFuegoGolpe);
    }

    public void enviarEstadoFuegoSuelo(float x, float y, float alto, boolean activa) {
        enviarATodos("ESTADO_FUEGO_SUELO;" + x + ";" + y + ";" + alto + ";" + activa);
    }

    public void enviarEstadoRayos(String fase, boolean mostrarAviso, float[] posiciones) {
        StringBuilder mensaje = new StringBuilder("ESTADO_RAYOS;").append(fase).append(";").append(mostrarAviso);

        for (float posicion : posiciones) {
            mensaje.append(";").append(posicion);
        }

        enviarATodos(mensaje.toString());
    }

    public void enviarEstadoLasers(String fase, boolean mostrarAviso, boolean[] activos) {
        StringBuilder mensaje = new StringBuilder("ESTADO_LASERES;").append(fase).append(";").append(mostrarAviso);

        for (boolean activo : activos) {
            mensaje.append(";").append(activo);
        }

        enviarATodos(mensaje.toString());
    }

    public void enviarCreacionMeteorito(int id, String tipo, float x, float y) {
        enviarATodos("CREAR_METEORITO;" + id + ";" + tipo + ";" + x + ";" + y);
    }

    public void enviarEstadoMeteorito(int id, String tipo, float x, float y, boolean impacto, float rotacion,
                                      float tiempoEfecto) {
        enviarATodos("ESTADO_METEORITO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + impacto + ";" + rotacion
            + ";" + tiempoEfecto);
    }

    public void enviarEliminacionMeteorito(int id) {
        enviarATodos("ELIMINAR_METEORITO;" + id);
    }

    public void enviarCreacionFueguito(int id, String tipo, float x, float y, float tamano, float rotacion) {
        enviarATodos("CREAR_FUEGUITO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + tamano + ";" + rotacion);
    }

    public void enviarEstadoFueguito(int id, String tipo, float x, float y, float tamano, float rotacion) {
        enviarATodos("ESTADO_FUEGUITO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + tamano + ";" + rotacion);
    }

    public void enviarEliminacionFueguito(int id, String tipo) {
        enviarATodos("ELIMINAR_FUEGUITO;" + id + ";" + tipo);
    }

    public void enviarEstadoLaserJefe(boolean visible, boolean aviso, float angulo, float tiempoAnimacion) {
        enviarATodos("ESTADO_LASER_JEFE;" + visible + ";" + aviso + ";" + angulo + ";" + tiempoAnimacion);
    }

    public void enviarSonido(String nombreSonido) {
        enviarATodos("SONIDO;" + nombreSonido);
    }

    public void enviarSonidoConPosicion(String nombreSonido, float x, float y) {
        enviarATodos("SONIDO;" + nombreSonido + ";" + x + ";" + y);
    }

    public void enviarSonidoLoopConPosicion(String nombreSonido, float x, float y) {
        enviarATodos("SONIDO;" + nombreSonido + ";LOOP;" + x + ";" + y);
    }

    public void enviarDetenerSonidoLoop(String nombreSonido) {
        enviarATodos("SONIDO;" + nombreSonido + ";DETENER_LOOP");
    }

    public void enviarDetenerSonidoLoopConPosicion(String nombreSonido, float x, float y) {
        enviarATodos("SONIDO;" + nombreSonido + ";DETENER_LOOP;" + x + ";" + y);
    }

    public void enviarMusica(String nombreMusica) {
        enviarATodos("MUSICA;" + nombreMusica);
    }

    public void enviarDetenerMusica() {
        enviarATodos("MUSICA;PARAR");
    }

    public void enviarEliminacionAtaquesJefe() {
        enviarATodos("ELIMINAR_ATAQUES_JEFE");
    }

    public void enviarCreditos() {
        establecerEstadoJugadores(JugadorRed.ESTADO_CREDITOS);
        enviarATodos("MOSTRAR_CREDITOS");
    }

    public void enviarCreacionReliquia(float x, float y) {
        enviarATodos("RELIQUIA;APARECER;" + x + ";" + y);
    }

    public void enviarEliminacionReliquia() {
        enviarATodos("RELIQUIA;RECOGIDA");
    }

    public JugadorRed[] getJugadoresRed() {
        HiloServidor hiloActual = hilo;
        return hiloActual != null ? hiloActual.getJugadores() : new JugadorRed[CANTIDAD_JUGADORES];
    }

    public int getCantClientes() {
        HiloServidor hiloActual = hilo;
        return hiloActual != null ? hiloActual.getCantClientes() : 0;
    }

    public void cerrar() {
        HiloServidor hiloActual = hilo;
        hilo = null;

        if (hiloActual != null) {
            hiloActual.cerrar();
        }
    }

    public void registrarCallbackVolverHub(Runnable callback) {
        callbackVolverHub = callback;
    }

    public void ejecutarCallbackVolverHub() {
        Runnable callback = callbackVolverHub;
        if (callback != null) {
            callback.run();
        }
    }

    public void registrarCallbackSnapshotEntidades(BiConsumer<InetAddress, Integer> callback) {
        callbackSnapshotEntidades = callback;
    }

    public void enviarSnapshotEntidades(InetAddress ip, int puerto) {
        BiConsumer<InetAddress, Integer> callback = callbackSnapshotEntidades;
        if (callback != null) {
            callback.accept(ip, puerto);
        }
    }

    public void enviarMensajeASpecifico(String msj, InetAddress ip, int puerto) {
        HiloServidor hiloActual = hilo;
        if (hiloActual != null) {
            hiloActual.enviarMensaje(msj, ip, puerto);
        }
    }
}
