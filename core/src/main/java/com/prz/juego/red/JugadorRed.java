package com.prz.juego.red;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class JugadorRed {

    public static final String ESTADO_SELECCION_PERSONAJE = "SELECCION_PERSONAJE";
    public static final String ESTADO_HUB = "HUB";
    public static final String ESTADO_SELECCION_NIVEL = "SELECCION_NIVEL";
    public static final String ESTADO_JUGANDO = "JUGANDO";
    public static final String ESTADO_CREDITOS = "CREDITOS";

    private static final long TIEMPO_MAX_SINCRONIZACION = 10000;

    private final InetAddress IP;
    private final AtomicBoolean ATAQUE_PENDIENTE = new AtomicBoolean(false);
    private final AtomicBoolean INTERACCION_PENDIENTE = new AtomicBoolean(false);

    private volatile int puerto;
    private volatile boolean mueveIzquierda = false;
    private volatile boolean mueveDerecha = false;
    private volatile boolean mueveArriba = false;
    private volatile boolean mueveAbajo = false;
    private volatile boolean solicitaSeleccion = false;
    private volatile int nivelSeleccionado = 0;
    private volatile long ultimoPing = System.currentTimeMillis();
    private volatile boolean conectado = true;
    private volatile boolean sincronizado = true;
    private volatile boolean reanudacionPendiente = false;
    private volatile long momentoReconexion = 0;
    private volatile String personaje;
    private volatile int idDemo;
    private volatile boolean listoParaComenzar = false;
    private volatile String estadoActual = ESTADO_SELECCION_PERSONAJE;

    public JugadorRed(InetAddress ip, int puerto) {
        IP = ip;
        this.puerto = puerto;
    }

    public void setIdDemo(int idDemo) {
        this.idDemo = idDemo;
    }

    public int getIdDemo() {
        return idDemo;
    }

    public InetAddress getIp() {
        return IP;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public void setMovimientoHorizontal(boolean valorIzquierda, boolean valorDerecha) {
        this.mueveIzquierda = valorIzquierda;
        this.mueveDerecha = valorDerecha;
    }

    public void setMovimientoVertical(boolean valorArriba, boolean valorAbajo) {
        this.mueveArriba = valorArriba;
        this.mueveAbajo = valorAbajo;
    }

    public void setSolicitoSeleccionNivel(boolean solicitaSeleccion) {
        this.solicitaSeleccion = solicitaSeleccion;
    }

    public void solicitarAtaque() {
        ATAQUE_PENDIENTE.set(true);
    }

    public void solicitarInteraccion() {
        INTERACCION_PENDIENTE.set(true);
    }

    public boolean mueveIzquierda() {
        return mueveIzquierda;
    }

    public boolean mueveDerecha() {
        return mueveDerecha;
    }

    public boolean mueveArriba() {
        return mueveArriba;
    }

    public boolean mueveAbajo() {
        return mueveAbajo;
    }

    public boolean solicitaSeleccion() {
        return solicitaSeleccion;
    }

    public boolean ataca() {
        return ATAQUE_PENDIENTE.getAndSet(false);
    }

    public boolean interactua() {
        return INTERACCION_PENDIENTE.getAndSet(false);
    }

    public void setPersonaje(String personaje) {
        this.personaje = personaje;
    }

    public String getPersonaje() {
        return personaje;
    }

    public void setNivelSeleccionado(int nivel) {
        nivelSeleccionado = nivel;
    }

    public int getNivelSeleccionado() {
        return nivelSeleccionado;
    }

    public void actualizarPing() {
        ultimoPing = System.currentTimeMillis();
    }

    public long getUltimoPing() {
        return ultimoPing;
    }

    public boolean estaConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
        if (!conectado) {
            resetearInputs();
        }
    }

    public void resetearInputs() {
        mueveIzquierda = false;
        mueveDerecha = false;
        mueveArriba = false;
        mueveAbajo = false;
        ATAQUE_PENDIENTE.set(false);
        INTERACCION_PENDIENTE.set(false);
    }

    public void descartarAccionesPendientes() {
        ATAQUE_PENDIENTE.set(false);
        INTERACCION_PENDIENTE.set(false);
    }

    public void marcarReconexion(boolean requiereSnapshot) {
        resetearInputs();
        sincronizado = !requiereSnapshot;
        momentoReconexion = System.currentTimeMillis();
        reanudacionPendiente = true;
        actualizarPing();
        conectado = true;
    }

    public void marcarSincronizado() {
        sincronizado = true;
    }

    public boolean estaListoParaJugar() {
        if (!conectado) {
            return false;
        }
        return sincronizado || System.currentTimeMillis() - momentoReconexion > TIEMPO_MAX_SINCRONIZACION;
    }

    public boolean isReanudacionPendiente() {
        return reanudacionPendiente;
    }

    public void setReanudacionPendiente(boolean pendiente) {
        reanudacionPendiente = pendiente;
    }

    public void setListoParaComenzar(boolean listo) {
        listoParaComenzar = listo;
    }

    public boolean isListoParaComenzar() {
        return listoParaComenzar;
    }

    public void setEstadoActual(String estado) {
        estadoActual = estado;
    }

    public String getEstadoActual() {
        return estadoActual;
    }
}
