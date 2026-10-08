package com.prz.juego.hilos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class HiloServidor extends Thread{

    private DatagramSocket socket;
    private boolean fin = false;

    public HiloServidor() {
        try {
            socket = new DatagramSocket(9992);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        while(!fin)
        {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
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
        System.out.println(msj);
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


}
