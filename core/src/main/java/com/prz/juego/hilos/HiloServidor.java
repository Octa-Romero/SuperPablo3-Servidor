package com.prz.juego.hilos;


import com.prz.juego.entidades.Personajes;
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
    private boolean izquierda, derecha;

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
        //System.out.println("Mensaje recibido: " + msj);

        if(cantClientes < jugadores.length) {
            if(msj.equals("Conectar"))
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
        } else {
            int indice = buscarJugador(dp.getAddress(), dp.getPort());
            if(indice != -1)
            {
                mensajeParametrizado = msj.split(";");

                if(mensajeParametrizado[0].equals("INPUT"))
                {
                    if(mensajeParametrizado[1].equals("IZQUIERDA"))
                    {
                        jugadores[indice].setMovimiento(true, false);
                    }else if(mensajeParametrizado[1].equals("NOIZQUIERDA"))
                    {
                        jugadores[indice].setMovimiento(false, false);
                    }else if(mensajeParametrizado[1].equals("DERECHA"))
                    {
                        jugadores[indice].setMovimiento(false, true);
                    }else if(mensajeParametrizado[1].equals("NODERECHA"))
                    {
                        jugadores[indice].setMovimiento(false, false);
                    }
                }
            }
        }

        if (cantClientes == 2) {
            if (msj.equals("PABLO") || msj.equals("WALTER")) {

                int indice = buscarJugador(dp.getAddress(), dp.getPort());

                if (indice != -1) {
                    jugadores[indice].setPersonaje(msj);

                    System.out.println(
                        "Jugador " + indice + " eligió " + msj
                    );
                }

                if (jugadores[0].getPersonaje() != null && jugadores[1].getPersonaje() != null) {
                    System.out.println("Ambos eligieron. Enviando inicio.");

                    for (int i = 0; i < cantClientes; i++) {
                        enviarMensaje(
                            "Empieza juego",
                            jugadores[i].getIp(),
                            jugadores[i].getPuerto()
                        );
                    }

                    for (int i = 0; i < cantClientes; i++) {
                        int indiceRemoto = 1 - i;
                        enviarMensaje(
                            jugadores[indiceRemoto].getPersonaje(),
                            jugadores[i].getIp(),
                            jugadores[i].getPuerto()
                        );
                    }
                    Global.empieza = true;
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

    public JugadorRed[] getJugadores() {
        return jugadores;
    }

    public void cerrar() {
        fin = true;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
