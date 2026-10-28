package com.prz.juego.red;

import com.prz.juego.entidades.Jugador;
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

    public void cerrar() {
        if (hilo != null) {
            hilo.cerrar();
            hilo = null;
        }
    }
}
