package com.prz.juego.red;

import com.prz.juego.hilos.HiloServidor;

public class Servidor {

    private HiloServidor hs;

    public Servidor()
    {
        hs = new HiloServidor();
        hs.start();
    }
}
