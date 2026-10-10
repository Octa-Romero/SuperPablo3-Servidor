package com.prz.juego.hilos;

import java.net.InetAddress;

public class JugadorRed {

    private InetAddress ip;
    private int puerto;
    private int numero;
    private long ultimoMensaje;
    private boolean desconectado;

    public JugadorRed(InetAddress ip, int puerto, int numero) {
        this.ip = ip;
        this.puerto = puerto;
        this.numero = numero;
        this.ultimoMensaje = System.currentTimeMillis();
        this.desconectado = false;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public int getNumero() {
        return numero;
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }

    public boolean isDesconectado() {
        return desconectado;
    }

    public void actualizarConexion(int puerto) {
        this.puerto = puerto;
        this.ultimoMensaje = System.currentTimeMillis();
        this.desconectado = false;
    }

    public void actualizarMensaje() {
        this.ultimoMensaje = System.currentTimeMillis();
        this.desconectado = false;
    }

    public void setDesconectado(boolean desconectado) {
        this.desconectado = desconectado;
    }
}
