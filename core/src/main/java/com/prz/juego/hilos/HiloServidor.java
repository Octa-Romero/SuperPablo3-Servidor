package com.prz.juego.hilos;


import com.prz.juego.utilidades.Global;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class HiloServidor extends Thread {

    private DatagramSocket socket;
    private boolean fin = false;
    private int cantClientes = 0;
    private JugadorRed[] jugadores = new JugadorRed[2];
    private String[] mensajeParametrizado;

    public HiloServidor() {
        try {
            socket = new DatagramSocket(9990);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!fin) {
            byte[] datos = new byte[1024];
            DatagramPacket dp = new DatagramPacket(datos, datos.length);
            try {
                socket.receive(dp);
                procesarMensaje(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void procesarMensaje(DatagramPacket dp) {
        String msj = new String(dp.getData()).trim();

        int indice = buscarJugador(dp.getAddress(), dp.getPort());

        if (msj.equals("Conectar")) {
            procesarConexion(dp, indice);
            return;
        }

        if (indice == -1) {
            return;
        }

        if (msj.startsWith("INPUT;")) {
            procesarInput(msj, indice);
            return;
        }

        if (msj.equals("PABLO") || msj.equals("WALTER")) {
            procesarSeleccionPersonaje(msj, indice);
            return;
        }

        if (msj.startsWith("SELECCION_NIVEL;")) {
            procesarSeleccionNivel(msj, indice);
            return;
        }

        if (msj.startsWith("VOTAR_NIVEL;")) {
            procesarVotoNivel(msj, indice);
        }
    }

    private void procesarConexion(DatagramPacket dp, int indice) {

        if (indice != -1) {
            enviarMensaje("Conexion recibida", dp.getAddress(), dp.getPort());
            return;
        }

        if (cantClientes >= jugadores.length) {
            return;
        }

        jugadores[cantClientes] = new JugadorRed(dp.getAddress(), dp.getPort());

        enviarMensaje("Conexion recibida",
            dp.getAddress(), dp.getPort());

        cantClientes++;

        if (cantClientes == jugadores.length) {
            for (int i = 0; i < cantClientes; i++) {
                enviarMensaje("Conexion establecida",
                    jugadores[i].getIp(),
                    jugadores[i].getPuerto());
            }
        }
    }

    private void procesarInput(String msj, int indice) {
        mensajeParametrizado = msj.split(";");

        if (mensajeParametrizado[0].equals("INPUT")) {
            if (mensajeParametrizado[1].equals("IZQUIERDA")) {
                jugadores[indice].setMovimientoHorizontal(true, jugadores[indice].mueveDerecha());
            } else if (mensajeParametrizado[1].equals("NOIZQUIERDA")) {
                jugadores[indice].setMovimientoHorizontal(false, jugadores[indice].mueveDerecha());
            } else if (mensajeParametrizado[1].equals("DERECHA")) {
                jugadores[indice].setMovimientoHorizontal(jugadores[indice].mueveIzquierda(), true);
            } else if (mensajeParametrizado[1].equals("NODERECHA")) {
                jugadores[indice].setMovimientoHorizontal(jugadores[indice].mueveIzquierda(), false);
            } else if (mensajeParametrizado[1].equals("ARRIBA")) {
                jugadores[indice].setMovimientoVertical(true, jugadores[indice].mueveAbajo());
            } else if (mensajeParametrizado[1].equals("NOARRIBA")) {
                jugadores[indice].setMovimientoVertical(false, jugadores[indice].mueveAbajo());
            } else if (mensajeParametrizado[1].equals("ABAJO")) {
                jugadores[indice].setMovimientoVertical(jugadores[indice].mueveArriba(), true);
            } else if (mensajeParametrizado[1].equals("NOABAJO")) {
                jugadores[indice].setMovimientoVertical(jugadores[indice].mueveArriba(), false);
            } else if (mensajeParametrizado[1].equals("ATACA")) {
                jugadores[indice].setAtaca(true);
            } else if (mensajeParametrizado[1].equals("NOATACA")) {
                jugadores[indice].setAtaca(false);
            }
        }
    }

    private void procesarSeleccionPersonaje(String msj, int indice) {

        if (cantClientes != jugadores.length || Global.empieza) {
            return;
        }

        int indiceRemoto = 1 - indice;

        String personajeRemoto = jugadores[indiceRemoto].getPersonaje();

        if (msj.equals(personajeRemoto)) {
            return;
        }

        jugadores[indice].setPersonaje(msj);

        System.out.println("Jugador " + indice + " eligió " + msj);

        if (jugadores[0].getPersonaje() == null ||
            jugadores[1].getPersonaje() == null) {
            return;
        }

        System.out.println("Ambos eligieron. Enviando inicio.");

        for (int i = 0; i < cantClientes; i++) {
            enviarMensaje(
                "Empieza juego",
                jugadores[i].getIp(),
                jugadores[i].getPuerto()
            );
        }

        for (int i = 0; i < cantClientes; i++) {
            int remoto = 1 - i;

            enviarMensaje(
                jugadores[remoto].getPersonaje(),
                jugadores[i].getIp(),
                jugadores[i].getPuerto()
            );
        }

        Global.empieza = true;
    }
    private void procesarSeleccionNivel(String msj, int indice) {

        String[] mensajeParametrizado = msj.split(";");

        boolean solicitaSeleccion = Boolean.parseBoolean(mensajeParametrizado[1]);

        jugadores[indice].setSolicitoSeleccionNivel(solicitaSeleccion);
    }

    private void procesarVotoNivel(String msj, int indice) {
        String[] mensajeParametrizados = msj.split(";");

        try {
            int nivel = Integer.parseInt(mensajeParametrizados[1]);

            if (nivel >= 0 && nivel <= 7) {
                jugadores[indice].setNivelSeleccionado(nivel);
            }
        } catch (NumberFormatException e) {
            System.out.println("Nivel recibido inválido");
        }
    }

    private int buscarJugador (InetAddress ip,int puerto){
        for (int i = 0; i < cantClientes; i++) {
            if (jugadores[i].getIp().equals(ip) &&
                jugadores[i].getPuerto() == puerto) {
                return i;
            }
        }

        return -1;
    }


    public void enviarMensaje (String msj, InetAddress ipDestino,int puerto)
    {
        byte[] data = msj.getBytes();
        try {
            DatagramPacket dp = new DatagramPacket(data, data.length, ipDestino, puerto);
            socket.send(dp);
            System.out.println("PAQUETE ENVIADO: " + msj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarMensajeATodos(String mensaje) {
        for (int i = 0; i < cantClientes; i++) {
            enviarMensaje(
                mensaje,
                jugadores[i].getIp(),
                jugadores[i].getPuerto()
            );
        }
    }

    public JugadorRed[] getJugadores () {
        return jugadores;
    }

    public void cerrar () {
        fin = true;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}

