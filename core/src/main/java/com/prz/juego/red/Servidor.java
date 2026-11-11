package com.prz.juego.red;

import com.prz.juego.entidades.enemigos.TipoEnemigo;
import com.prz.juego.entidades.jugadores.Jugador;
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

    public void enviarPosicion(Jugador jugador)
    {
        String msj = "POS;" + jugador.getPersonaje().name() + ";" + jugador.getX() + ";" + jugador.getY() + ";" + jugador.getDireccion() + ";" + jugador.estaEnSuelo() + ";" + jugador.estaTrepando();

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
