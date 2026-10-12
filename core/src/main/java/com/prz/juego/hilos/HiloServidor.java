package com.prz.juego.hilos;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class HiloServidor extends Thread{

    private DatagramSocket socket;
    private boolean fin = false;
    private int cantClientes = 0;
    private JugadorRed[] jugadores = new JugadorRed[2];
    private String[] personajes = new String[2];

    public HiloServidor()
    {
        try {
            socket = new DatagramSocket(9990);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        while(!fin)
        {
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

    private void procesarMensaje(DatagramPacket dp)
    {
        String msj = new String(dp.getData()).trim();
        System.out.println("Mensaje recibido: " + msj);

        if(msj.equals("Conectar")) {
            if(cantClientes < 2)
            {
                jugadores[cantClientes] = new JugadorRed(dp.getAddress(), dp.getPort());
                enviarMensaje("Conexion recibida", jugadores[cantClientes].getIp(), jugadores[cantClientes].getPuerto());
                cantClientes++;
                if(cantClientes == 2)
                {
                    for(int i = 0 ; i < cantClientes ; i++)
                    {
                        enviarMensaje("Conexion establecida", jugadores[i].getIp(), jugadores[i].getPuerto());
                    }
                }
            }
        }

        if (cantClientes == 2) {
            if (msj.equals("PABLO") || msj.equals("WALTER")) {

                int indice = buscarJugador(dp.getAddress(), dp.getPort());

                if (indice != -1 && personajes[indice] == null) {
                    personajes[indice] = msj;

                    System.out.println(
                        "Jugador " + indice + " eligió " + msj
                    );
                }

                if (personajes[0] != null && personajes[1] != null) {
                    System.out.println("Ambos eligieron. Enviando inicio.");

                    for (int i = 0; i < cantClientes; i++) {
                        enviarMensaje(
                            "Empieza juego",
                            jugadores[i].getIp(),
                            jugadores[i].getPuerto()
                        );
                    }
                }
            }
        }
    }

    private int buscarJugador(InetAddress ip, int puerto) {
        for (int i = 0; i < cantClientes; i++) {
            if (jugadores[i].getIp().equals(ip) &&
                jugadores[i].getPuerto() == puerto) {
                return i;
            }
        }

        return -1;
    }


    public void enviarMensaje(String msj, InetAddress ipDestino, int puerto)
    {
        byte[] data = msj.getBytes();
        try {
            DatagramPacket dp = new DatagramPacket(data, data.length, ipDestino, puerto);
            socket.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cerrar() {
        fin = true;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
