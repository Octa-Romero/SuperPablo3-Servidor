package com.prz.juego.utilidades;

public class Dialogo {

    private final String[] MENSAJES;
    private int indice;
    private boolean activo;

    public Dialogo(String... MENSAJES) {
        this.MENSAJES = MENSAJES;
        indice = 0;
        activo = false;
    }

    public void iniciar() {
        if (MENSAJES.length > 0) {
            indice = 0;
            activo = true;
        }
    }

    public void avanzar() {
        if (!activo) {
            return;
        }

        indice++;

        if (indice >= MENSAJES.length) {
            activo = false;
        }
    }

    public String getMensajeActual() {
        if (!activo) {
            return null;
        }

        return MENSAJES[indice];
    }

    public boolean estaActivo() {
        return activo;
    }

    public void cerrar() {
        activo = false;
    }
}
