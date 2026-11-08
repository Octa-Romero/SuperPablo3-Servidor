package com.prz.juego.red;

import com.prz.juego.entidades.Jugador;
import com.prz.juego.entidades.TipoEnemigo;
import com.prz.juego.hilos.HiloServidor;
import com.prz.juego.hilos.JugadorRed;

public class Servidor {

    private HiloServidor hilo;

    public void iniciar() {
        if (hilo == null) {
            hilo = new HiloServidor();
            hilo.start();
        }
    }

    public JugadorRed[] getJugadoresRed()
    {
        return hilo.getJugadores();
    }

    public void enviarPosicion(Jugador jugador)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "POS;" + jugador.getX() + ";" + jugador.getY() + ";" + jugador.getDireccion();

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEstadoAtaque(Jugador jugador)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "ATAQUE;" + jugador.estaAtacando();

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEstadoVida(Jugador jugador)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "VIDA;" + jugador.getVida();

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarCreacionEnemigos(int id, TipoEnemigo tipo, float x, float y, double vida)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "CREAR_ENEMIGO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + vida;

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEstadoEnemigo(int id, float x, float y, int direccion, boolean estaAtacando, double vida)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "ESTADO_ENEMIGO;" + id + ";" + x + ";" + y + ";" + direccion + ";" + estaAtacando + ";" + vida;
        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEliminacionEnemigo(int id)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "ELIMINAR_ENEMIGO;" + id;
        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarCreacionOrbes(int id, float x, float y, int direccion)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "CREAR_ORBE;" + id + ";" + x + ";" + y + ";" + direccion;

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEstadoOrbes(int id, float x, float y)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "ESTADO_ORBE;" + id + ";" + x + ";" + y ;

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void enviarEliminacionOrbes(int id)
    {
        JugadorRed jugadorRed = hilo.getJugadores()[0];
        String msj = "ELIMINAR_ORBE;" + id;

        hilo.enviarMensaje(msj, jugadorRed.getIp(), jugadorRed.getPuerto());
    }

    public void cerrar() {
        if (hilo != null) {
            hilo.cerrar();
            hilo = null;
        }
    }
}
