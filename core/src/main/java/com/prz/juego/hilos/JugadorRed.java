package com.prz.juego.hilos;

import java.net.InetAddress;

public class JugadorRed {

    private InetAddress ip;
    private int puerto;
    private boolean mueveIzquierda = false, mueveDerecha = false, mueveArriba = false, mueveAbajo = false, valorAtaca = false, solicitaSeleccion = false;
    private volatile int nivelSeleccionado = 0;
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

    public void setMovimientoHorizontal(boolean valorIzquierda, boolean valorDerecha)
    {
        this.mueveIzquierda = valorIzquierda;
        this.mueveDerecha = valorDerecha;
    }

    public void setMovimientoVertical(boolean valorArriba, boolean valorAbajo)
    {
        this.mueveArriba = valorArriba;
        this.mueveAbajo = valorAbajo;
    }


    public void setSolicitoSeleccionNivel(boolean solicitaSeleccion) {
        this.solicitaSeleccion = solicitaSeleccion;
    }

    public void setAtaca(boolean valorAtaca)
    {
        this.valorAtaca = valorAtaca;
    }

    public boolean mueveIzquierda()
    {
        return this.mueveIzquierda;
    }

    public boolean mueveDerecha()
    {
        return this.mueveDerecha;
    }

    public boolean mueveArriba() {
        return mueveArriba;
    }

    public boolean mueveAbajo() {
        return mueveAbajo;
    }

    public boolean solicitaSeleccion()
    {
        return this.solicitaSeleccion;
    }

    public boolean ataca()
    {
        return this.valorAtaca;
    }

    public void setPersonaje(String personaje)
    {
        this.personaje = personaje;
    }

    public String getPersonaje()
    {
        return this.personaje;
    }

    public void setNivelSeleccionado(int nivel) {
        nivelSeleccionado = nivel;
    }

    public int getNivelSeleccionado() {
        return nivelSeleccionado;
    }
}
