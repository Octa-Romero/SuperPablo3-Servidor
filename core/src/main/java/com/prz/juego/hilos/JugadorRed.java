package com.prz.juego.hilos;

import java.net.InetAddress;

public class JugadorRed {

    private InetAddress ip;
    private int puerto;

    public JugadorRed(InetAddress ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

}
