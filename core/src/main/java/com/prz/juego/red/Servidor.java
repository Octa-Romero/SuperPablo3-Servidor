package com.prz.juego.red;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jefes.Mano;
import com.prz.juego.entidades.jugadores.Jugador;
import com.prz.juego.hilos.HiloServidor;
import com.prz.juego.hilos.JugadorRed;

public class Servidor {

    public static Servidor instancia;

    private HiloServidor hilo;

    public void iniciar() {
        if (hilo == null) {
            hilo = new HiloServidor();
            hilo.start();
        }
    }

    public void enviarPosicion(Jugador jugador)
    {
        String msj = "POS;" + jugador.getPersonaje().name() + ";" + jugador.getX() + ";" + jugador.getY() + ";" + jugador.getDireccion() + ";" + jugador.estaEnSuelo() + ";" + jugador.estaTrepando();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoAtaque(Jugador jugador)
    {
        String msj = "ATAQUE;" + jugador.getPersonaje().name() + ";" + jugador.estaAtacando() + ";" + jugador.getTipoAnimacion() + ";" + jugador.getTiempoAnimacion();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoVida(Jugador jugador)
    {
        String msj = "VIDA;" + jugador.getPersonaje().name() + ";" + jugador.getVida();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoInvencibilidad(Jugador jugador)
    {
        String msj = "INVENCIBILIDAD;" + jugador.getPersonaje().name() + ";" + jugador.estaInvencible() + ";" + jugador.estaSpriteVisible();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionEnemigos(int id, TipoEnemigo tipo, float x, float y, double vida)
    {
        String msj = "CREAR_ENEMIGO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + vida;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoEnemigo(int id, float x, float y, int direccion, boolean estaAtacando, double vida, boolean spriteVisible, int tipoAnimacion, float tiempoAnimacion)
    {
        String msj = "ESTADO_ENEMIGO;" + id + ";" + x + ";" + y + ";" + direccion + ";" + estaAtacando + ";" + vida + ";" + spriteVisible + ";" + tipoAnimacion + ";" + tiempoAnimacion;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionJefe(int id)
    {
        String msj = "ELIMINAR_ENEMIGO;" + id;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionProyectil(int id, String origen, float x, float y, float direccion) {

        String msj = "CREAR_PROYECTIL;" + id + ";" + origen + ";" + x + ";" + y + ";" + direccion;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoProyectil(int id, float x, float y)
    {
        String msj = "ESTADO_PROYECTIL;" + id + ";" + x + ";" + y ;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionProyectil(int id)
    {
        String msj = "ELIMINAR_PROYECTIL;" + id;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoCentroHub(JugadorRed estadoRed, boolean valor) {
        String msj = "CENTRO_HUB;" + valor;

        hilo.enviarMensaje(msj, estadoRed.getIp(), estadoRed.getPuerto());
    }

    public void enviarEstadoSeleccion(JugadorRed estadoRed) {
        String msj = "ESTADO_SELECCION;" + estadoRed.solicitaSeleccion();

        hilo.enviarMensaje(msj, estadoRed.getIp(), estadoRed.getPuerto());
    }

    public void enviarInicioSeleccionNivel() {
        String msj = "ABRIR_SELECCION_NIVEL";
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarVotosNivel(JugadorRed destinatario, int votoLocal, int votoRemoto) {
        String msj = "VOTOS_NIVEL;" + votoLocal + ";" + votoRemoto;

        hilo.enviarMensaje(
            msj,
            destinatario.getIp(),
            destinatario.getPuerto()
        );
    }

    public void enviarInicioNivel(int voto)
    {
        String msj = "INICIAR_NIVEL;" + voto;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionJefe(float x, float y, double vida) {
        String mensaje = "CREAR_JEFE;" + x + ";" + y + ";" + vida;
        hilo.enviarMensajeATodos(mensaje);
    }

    public void enviarEstadoJefe(float x, float y, double vida, boolean visible, float escala, String faseEspecial) {

        String mensaje = "ESTADO_JEFE;" + x + ";" + y + ";" + vida + ";" + visible + ";" + escala + ";" + faseEspecial;

        hilo.enviarMensajeATodos(mensaje);
    }

    public void enviarEliminacionJefe()
    {
        String mensaje = "ELIMINAR_JEFE";

        hilo.enviarMensajeATodos(mensaje);
    }

    public void enviarEstadoMano(
        Mano.Lado lado,
        float x,
        float y,
        double vida,
        boolean visible,
        float escala,
        Mano.EstadoAtaque estadoAtaque,
        Mano.EstiloAtaque estiloAtaque,
        boolean mostrarFuegoGolpe) {

        String mensaje = "ESTADO_MANO;" + lado + ";" + x + ";" + y + ";"
            + vida + ";" + visible + ";" + escala + ";"
            + estadoAtaque + ";" + estiloAtaque + ";" + mostrarFuegoGolpe;

        hilo.enviarMensajeATodos(mensaje);
    }

    public void enviarEstadoFuegoSuelo(float x, float y, float alto, boolean activa)
    {
        String msj = "ESTADO_FUEGO_SUELO;" + x + ";" + y + ";" + alto + ";" + activa;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoRayos(String fase, boolean mostrarAviso, float[] posiciones) {

        String msj = "ESTADO_RAYOS;" + fase + ";" + mostrarAviso;

        for (float posicion : posiciones) {
            msj += ";" + posicion;
        }

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoLasers(String fase, boolean mostrarAviso, boolean[] activos) {

        String msj = "ESTADO_LASERES;" + fase + ";" + mostrarAviso;

        for (boolean activo : activos) {
            msj += ";" + activo;
        }

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionMeteorito(
        int id, String tipo, float x, float y) {

        String msj = "CREAR_METEORITO;" + id + ";"
            + tipo + ";" + x + ";" + y;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoMeteorito(
        int id, String tipo, float x, float y,
        boolean impacto, float rotacion, float tiempoEfecto) {

        String msj = "ESTADO_METEORITO;" + id
            + ";" + tipo
            + ";" + x
            + ";" + y
            + ";" + impacto
            + ";" + rotacion
            + ";" + tiempoEfecto;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionMeteorito(int id) {
        hilo.enviarMensajeATodos("ELIMINAR_METEORITO;" + id);
    }

    public void enviarCreacionFueguito(int id, String tipo, float x, float y, float tamano, float rotacion) {

        String msj = "CREAR_FUEGUITO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + tamano + ";" + rotacion;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoFueguito(int id, String tipo, float x, float y, float tamano, float rotacion) {

        String msj = "ESTADO_FUEGUITO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + tamano + ";" + rotacion;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionFueguito(int id, String tipo) {
        String msj = "ELIMINAR_FUEGUITO;" + id + ";" + tipo;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoLaserJefe(boolean visible, boolean aviso, float angulo, float tiempoAnimacion) {

        String msj = "ESTADO_LASER_JEFE;" + visible + ";" + aviso + ";" + angulo + ";" + tiempoAnimacion;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarSonido(String nombreSonido) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido);
    }

    public void enviarSonidoConPosicion(String nombreSonido, float x, float y) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido + ";" + x + ";" + y);
    }

    public void enviarSonidoLoop(String nombreSonido) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido + ";LOOP");
    }

    public void enviarSonidoLoopConPosicion(String nombreSonido, float x, float y) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido + ";LOOP;" + x + ";" + y);
    }

    public void enviarDetenerSonidoLoop(String nombreSonido) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido + ";DETENER_LOOP");
    }

    public void enviarDetenerSonidoLoopConPosicion(String nombreSonido, float x, float y) {
        hilo.enviarMensajeATodos("SONIDO;" + nombreSonido + ";DETENER_LOOP;" + x + ";" + y);
    }

    public void enviarMusica(String nombreMusica) {
        hilo.enviarMensajeATodos("MUSICA;" + nombreMusica);
    }

    public void enviarDetenerMusica() {
        hilo.enviarMensajeATodos("MUSICA;PARAR");
    }

    public JugadorRed[] getJugadoresRed()
    {
        return hilo.getJugadores();
    }

    public void cerrar() {
        if (hilo != null) {
            hilo.cerrar();
            hilo = null;
        }
    }
}
