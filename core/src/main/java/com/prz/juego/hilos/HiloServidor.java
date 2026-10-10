package com.prz.juego.hilos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class HiloServidor extends Thread {

    private DatagramSocket socket;
    private volatile boolean fin = false;

    private HashMap<String, JugadorRed> jugadores = new HashMap<>();

    private static final int PUERTO = 9993;
    private static final long TIEMPO_LIMITE = 5000;

    private boolean partidaIniciada = false;

    public HiloServidor() {
        try {
            socket = new DatagramSocket(PUERTO);
            socket.setSoTimeout(100);
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
            } catch (SocketTimeoutException e) {
                // El bucle continúa si no llegan paquetes.
            } catch (IOException e) {
                if (!fin) {
                    e.printStackTrace();
                }
                break;
            }

            comprobarDesconexiones();
        }
    }

    private void procesarMensaje(DatagramPacket dp) {
        String msj = new String(
            dp.getData(), dp.getOffset(), dp.getLength()
        );

        InetAddress ip = dp.getAddress();
        int puerto = dp.getPort();

        String clave = ip.getHostAddress();

        System.out.println("Mensaje recibido: " + msj);

        if (msj.equals("Conectar")) {
            registrarCliente(ip, puerto);
            return;
        }

        JugadorRed jugador = jugadores.get(clave);

        if (jugador == null || jugador.getPuerto() != puerto) {
            enviarMensaje("Acceso denegado", ip, puerto);
            return;
        }

        jugador.actualizarMensaje();

        if (msj.equals("PING")) {
            enviarMensaje("PONG", ip, puerto);
        }
    }

    private void registrarCliente(InetAddress ip, int puerto) {
        String clave = ip.getHostAddress();

        JugadorRed jugador = jugadores.get(clave);

        // El jugador ya estaba registrado.
        if (jugador != null) {
            boolean reconexion = jugador.isDesconectado()
                || jugador.getPuerto() != puerto;

            jugador.actualizarConexion(puerto);

            enviarMensaje("Conexion recibida", ip, puerto);

            if (reconexion && partidaIniciada) {
                enviarMensaje("Empezarchat", ip, puerto);

                JugadorRed otro = obtenerOponente(jugador);

                if (otro != null && !otro.isDesconectado()) {
                    enviarMensaje(
                        "Jugador " + jugador.getNumero() + " reconectado",
                        otro.getIp(),
                        otro.getPuerto()
                    );
                }

                System.out.println(
                    "Jugador " + jugador.getNumero() + " reconectado"
                );
            }

            return;
        }

        // La sala admite solamente dos jugadores.
        if (jugadores.size() >= 2) {
            enviarMensaje("Sala llena", ip, puerto);
            return;
        }

        int numero = jugadores.size() + 1;

        jugador = new JugadorRed(ip, puerto, numero);
        jugadores.put(clave, jugador);

        enviarMensaje("Conexion recibida", ip, puerto);

        System.out.println("Cliente " + numero + " conectado");

        // Al conectarse el segundo jugador, comienza la partida.
        if (jugadores.size() == 2) {
            partidaIniciada = true;

            for (JugadorRed j : jugadores.values()) {
                enviarMensaje("Empezarchat", j.getIp(), j.getPuerto());
            }

            System.out.println("Ambos clientes conectados");
        }
    }

    private void comprobarDesconexiones() {
        long ahora = System.currentTimeMillis();

        for (JugadorRed jugador : jugadores.values()) {
            if (!jugador.isDesconectado()
                && ahora - jugador.getUltimoMensaje() >= TIEMPO_LIMITE) {

                jugador.setDesconectado(true);

                System.out.println(
                    "Cliente " + jugador.getNumero()
                        + " desconectado por timeout"
                );

                JugadorRed otro = obtenerOponente(jugador);

                if (otro != null && !otro.isDesconectado()) {
                    enviarMensaje(
                        "Jugador " + jugador.getNumero() + " desconectado",
                        otro.getIp(),
                        otro.getPuerto()
                    );
                }
            }
        }
    }

    private JugadorRed obtenerOponente(JugadorRed jugador) {
        for (JugadorRed otro : jugadores.values()) {
            if (otro.getNumero() != jugador.getNumero()) {
                return otro;
            }
        }

        return null;
    }

    public void enviarMensaje(
        String msj, InetAddress ipDestino, int puerto) {

        byte[] datos = msj.getBytes();

        try {
            DatagramPacket dp = new DatagramPacket(
                datos, datos.length, ipDestino, puerto
            );

            socket.send(dp);
        } catch (IOException e) {
            if (!fin) {
                e.printStackTrace();
            }
        }
    }

    public void detener() {
        fin = true;
        socket.close();
    }
}
