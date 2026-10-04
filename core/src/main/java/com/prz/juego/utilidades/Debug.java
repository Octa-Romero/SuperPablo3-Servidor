package com.prz.juego.utilidades;

public class Debug {

    private static boolean mostrarHitboxes = false;

    public static void cambiarHitboxes() {
        mostrarHitboxes = !mostrarHitboxes;
    }

    public static boolean mostrarHitboxes() {
        return mostrarHitboxes;
    }
}
