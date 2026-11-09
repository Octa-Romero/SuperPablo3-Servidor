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
        String msj = "POS;" + jugador.getPersonaje().name() + ";" + jugador.getX() + ";" + jugador.getY() + ";" + jugador.getDireccion();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoAtaque(Jugador jugador)
    {
        String msj = "ATAQUE;" + jugador.getPersonaje().name() + ";" + jugador.estaAtacando();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoVida(Jugador jugador)
    {
        String msj = "VIDA;" + jugador.getPersonaje().name() + ";" + jugador.getVida();

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionEnemigos(int id, TipoEnemigo tipo, float x, float y, double vida)
    {
        String msj = "CREAR_ENEMIGO;" + id + ";" + tipo + ";" + x + ";" + y + ";" + vida;

        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoEnemigo(int id, float x, float y, int direccion, boolean estaAtacando, double vida)
    {
        String msj = "ESTADO_ENEMIGO;" + id + ";" + x + ";" + y + ";" + direccion + ";" + estaAtacando + ";" + vida;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionEnemigo(int id)
    {
        String msj = "ELIMINAR_ENEMIGO;" + id;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarCreacionOrbes(int id, float x, float y, int direccion)
    {
        String msj = "CREAR_ORBE;" + id + ";" + x + ";" + y + ";" + direccion;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEstadoOrbes(int id, float x, float y)
    {
        String msj = "ESTADO_ORBE;" + id + ";" + x + ";" + y ;
        hilo.enviarMensajeATodos(msj);
    }

    public void enviarEliminacionOrbes(int id)
    {
        String msj = "ELIMINAR_ORBE;" + id;
        hilo.enviarMensajeATodos(msj);
    }

    public void cerrar() {
        if (hilo != null) {
            hilo.cerrar();
            hilo = null;
        }
    }
}
