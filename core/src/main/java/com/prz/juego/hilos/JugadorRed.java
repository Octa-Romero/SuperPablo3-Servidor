package com.prz.juego.hilos;

import java.net.InetAddress;

public class JugadorRed {

    private InetAddress ip;
    private int puerto;
    private boolean mueveIzquierda = false, mueveDerecha = false, valorSalto = false;
    private String personaje;

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

    public void setMovimiento(boolean valorIzquierda, boolean valorDerecha)
    {
        this.mueveIzquierda = valorIzquierda;
        this.mueveDerecha = valorDerecha;
    }

    public void setSalto(boolean valorSalto)
    {
        this.valorSalto = valorSalto;
    }

    public boolean mueveIzquierda()
    {
        return this.mueveIzquierda;
    }

    public boolean mueveDerecha()
    {
        return this.mueveDerecha;
    }

    public boolean solicitaSaltar()
    {
        return this.valorSalto;
    }

    public void setPersonaje(String personaje)
    {
        this.personaje = personaje;
    }

    public String getPersonaje()
    {
        return this.personaje;
    }
}
