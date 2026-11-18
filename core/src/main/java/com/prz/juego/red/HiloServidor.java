package com.prz.juego.red;

import com.prz.juego.utilidades.Config;
import com.prz.juego.utilidades.Global;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class HiloServidor extends Thread {

    private static final int PUERTO = 9990;
    private static final int TAMANO_BUFFER = 1024;
    private static final int MAX_JUGADORES = 2;
    private static final int MAX_NIVEL = 7;
    private static final int ENVIOS_EXPULSION = 3;
    private static final long TIEMPO_LIMITE_PING = 5000;
    private static final long INTERVALO_PONG = 1000;
    private static final long TIEMPO_ESPERA_RECONEXION = 60000;
    private static final String PERSONAJE_POR_DEFECTO = "PABLO";
    private static final long TIEMPO_LIMITE_INPUT = 750;
    private final long[] ULTIMO_INPUT = new long[MAX_JUGADORES];
    private final DatagramSocket SOCKET;
    private final JugadorRed[] JUGADORES = new JugadorRed[MAX_JUGADORES];
    private final boolean[] VUELVE_AL_HUB = new boolean[MAX_JUGADORES];

    private volatile boolean fin = false;
    private volatile boolean partidaIniciada = false;
    private volatile int cantClientes = 0;
    private long limiteReconexion = 0;

    public HiloServidor() {
        try {
            SOCKET = new DatagramSocket(PUERTO);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        iniciarHiloPingPong();
    }

    private void iniciarHiloPingPong() {
        Thread hiloPingPong = new Thread(() -> {
            while (!fin) {
                try {
                    Thread.sleep(INTERVALO_PONG);
                } catch (InterruptedException e) {
                    break;
                }
                try {
                    enviarPong();
                    detectarDesconexiones();
                    verificarReanudaciones();
                    verificarTemporizadorReconexion();
                    verificarYLimpiarJugadores();
                } catch (RuntimeException e) {
                    if (!fin) {
                        System.out.println("Error en el hilo de ping: " + e);
                    }
                }
            }
        });
        hiloPingPong.setDaemon(true);
        hiloPingPong.start();
    }

    private void enviarPong() {
        enviarMensajeATodos("PONG");
    }

    private synchronized void detectarDesconexiones() {
        long ahora = System.currentTimeMillis();

        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.estaConectado() && ahora - jugador.getUltimoPing() > TIEMPO_LIMITE_PING) {
                marcarDesconectado(i);
            }
        }
    }

    private synchronized void marcarDesconectado(int indice) {
        JUGADORES[indice].setConectado(false);
        VUELVE_AL_HUB[indice] = false;
        cancelarSeleccionNivel();
        Servidor.instancia.registrarDesconexion();

        JugadorRed otro = JUGADORES[1 - indice];
        if (otro != null && otro.estaConectado()) {
            enviarMensaje("JUGADOR_DESCONECTADO", otro.getIp(), otro.getPuerto());
            iniciarTemporizadorReconexion(otro);
        }
    }

    private synchronized void cancelarSeleccionNivel() {
        boolean habiaSeleccion = false;

        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && JugadorRed.ESTADO_SELECCION_NIVEL.equals(jugador.getEstadoActual())) {
                habiaSeleccion = true;
            }
        }

        if (!habiaSeleccion) {
            return;
        }

        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador == null) {
                continue;
            }

            jugador.setSolicitoSeleccionNivel(false);
            jugador.setNivelSeleccionado(0);
            if (JugadorRed.ESTADO_SELECCION_NIVEL.equals(jugador.getEstadoActual())) {
                jugador.setEstadoActual(JugadorRed.ESTADO_HUB);
            }
        }

        enviarMensajeATodos("CANCELAR_SELECCION");
    }

    private synchronized void iniciarTemporizadorReconexion(JugadorRed restante) {
        if (cantClientes < MAX_JUGADORES || limiteReconexion != 0) {
            return;
        }

        limiteReconexion = System.currentTimeMillis() + TIEMPO_ESPERA_RECONEXION;
        enviarTiempoReconexion(restante, TIEMPO_ESPERA_RECONEXION);
    }

    private synchronized void verificarTemporizadorReconexion() {
        if (limiteReconexion == 0) {
            return;
        }

        if (cantClientes < MAX_JUGADORES) {
            limiteReconexion = 0;
            return;
        }

        JugadorRed restante = buscarJugadorConectado();
        if (restante == null || todosConectados()) {
            limiteReconexion = 0;
            return;
        }

        long faltante = limiteReconexion - System.currentTimeMillis();
        if (faltante > 0) {
            enviarTiempoReconexion(restante, faltante);
            return;
        }

        limiteReconexion = 0;
        expulsar(restante);
    }

    private void enviarTiempoReconexion(JugadorRed destinatario, long milisegundos) {
        long segundos = (milisegundos + 999) / 1000;
        enviarMensaje("TIMER_RECONEXION;" + segundos, destinatario.getIp(), destinatario.getPuerto());
    }

    private void expulsar(JugadorRed jugador) {
        for (int i = 0; i < ENVIOS_EXPULSION; i++) {
            enviarMensaje("EXPULSADO", jugador.getIp(), jugador.getPuerto());
        }

        jugador.setConectado(false);
    }

    private JugadorRed buscarJugadorConectado() {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.estaConectado()) {
                return jugador;
            }
        }
        return null;
    }

    private synchronized void verificarReanudaciones() {
        for (int i = 0; i < cantClientes; i++) {
            notificarReanudacionSiCorresponde(i);
        }
    }

    private synchronized void notificarReanudacionSiCorresponde(int indice) {
        JugadorRed jugador = JUGADORES[indice];
        if (jugador == null || !jugador.isReanudacionPendiente() || !jugador.estaListoParaJugar()) {
            return;
        }

        jugador.setReanudacionPendiente(false);

        JugadorRed otro = JUGADORES[1 - indice];
        if (otro != null && otro.estaConectado()) {
            enviarMensaje("JUGADOR_RECONECTADO", otro.getIp(), otro.getPuerto());
        }
    }

    private synchronized void verificarYLimpiarJugadores() {
        if (cantClientes == 0) {
            return;
        }

        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.estaConectado()) {
                return;
            }
        }

        cantClientes = 0;
        limiteReconexion = 0;
        for (int i = 0; i < JUGADORES.length; i++) {
            JUGADORES[i] = null;
            VUELVE_AL_HUB[i] = false;
        }
        partidaIniciada = false;
        Global.empieza = false;
        Servidor.instancia.resetearNivelActual();
        Servidor.instancia.reiniciarProgreso();
    }

    public synchronized void resetearVotosVolverHub() {
        VUELVE_AL_HUB[0] = false;
        VUELVE_AL_HUB[1] = false;
    }

    public boolean hayJugadorNoListo() {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador == null || !jugador.estaListoParaJugar()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (!fin) {
            byte[] datos = new byte[TAMANO_BUFFER];
            DatagramPacket dp = new DatagramPacket(datos, datos.length);
            try {
                SOCKET.receive(dp);
                procesarMensaje(dp);
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                System.out.println("Error de recepción: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error procesando mensaje: " + e);
            }
        }
    }

    private void procesarMensaje(DatagramPacket dp) {
        String msj = new String(dp.getData(), 0, dp.getLength()).trim();

        int idDemo = 0;
        boolean tienePrefijoDemo = false;

        if (Config.DEMO_MODE && msj.startsWith("DEMO;")) {
            String[] partes = msj.split(";", 3);
            if (partes.length == 3) {
                try {
                    idDemo = Integer.parseInt(partes[1]);
                    msj = partes[2];
                    tienePrefijoDemo = true;
                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        int indice = tienePrefijoDemo
            ? buscarJugadorPorIdDemo(idDemo)
            : buscarJugador(dp.getAddress(), dp.getPort());

        if (tienePrefijoDemo && indice != -1 && !msj.startsWith("Conectar")) {
            JUGADORES[indice].setPuerto(dp.getPort());
        }

        if (msj.startsWith("Conectar")) {
            procesarConexion(dp, indice, idDemo, tienePrefijoDemo);
            return;
        }

        if (indice == -1 || !JUGADORES[indice].estaConectado()) {
            return;
        }

        JugadorRed jugador = JUGADORES[indice];

        if (msj.equals("PING")) {
            jugador.actualizarPing();
        } else if (msj.startsWith("INPUT;")) {
            procesarInput(msj, jugador, indice);
        } else if (msj.equals("PABLO") || msj.equals("WALTER")) {
            procesarSeleccionPersonaje(msj, indice);
        } else if (msj.equals("LISTO_COMENZAR")) {
            procesarListoComenzar(indice);
        } else if (msj.equals("CANCELAR_COMENZAR")) {
            procesarCancelarComenzar(indice);
        } else if (msj.startsWith("SELECCION_NIVEL;")) {
            procesarSeleccionNivel(msj, jugador);
        } else if (msj.startsWith("VOTAR_NIVEL;")) {
            procesarVotoNivel(msj, jugador);
        } else if (msj.equals("VOLVER_HUB")) {
            procesarVolverHub(indice);
        } else if (msj.equals("CANCELAR_HUB")) {
            procesarCancelarHub(indice);
        } else if (msj.equals("SOLICITAR_SNAPSHOT")) {
            procesarSolicitudSnapshot(indice);
        } else if (msj.equals("SNAPSHOT_OK")) {
            procesarSnapshotConfirmado(indice);
        } else if (msj.equals("CANCELAR_SELECCION_NIVEL")) {
            cancelarSeleccionNivel();
        } else if (msj.equals("DESCONECTAR")) {
            procesarDesconexion(indice);
        } else if (msj.equals("DEBUG_DESBLOQUEAR")) {
            Servidor.instancia.desbloquearTodo();
        }
    }

    private synchronized void procesarConexion(DatagramPacket dp, int indice, int idDemo, boolean esDemo) {
        int indiceExistente = esDemo
            ? buscarJugadorPorIdDemo(idDemo)
            : buscarJugadorPorIp(dp.getAddress());

        if (indiceExistente == -1) {
            indiceExistente = indice;
        }

        if (indiceExistente == -1) {
            registrarJugadorNuevo(dp, idDemo, esDemo);
            return;
        }

        if (esConexionDuplicada(JUGADORES[indiceExistente], dp)) {
            enviarMensaje("CONEXION_RECHAZADA", dp.getAddress(), dp.getPort());
            return;
        }

        reconectarJugador(indiceExistente, dp, idDemo, esDemo);
    }

    private boolean esConexionDuplicada(JugadorRed jugador, DatagramPacket dp) {
        return jugador.estaConectado() && jugador.getPuerto() != dp.getPort();
    }

    private void reconectarJugador(int indice, DatagramPacket dp, int idDemo, boolean esDemo) {
        JugadorRed jugador = JUGADORES[indice];
        JugadorRed otro = JUGADORES[1 - indice];
        boolean estabaConectado = jugador.estaConectado();
        boolean tieneProgreso = !JugadorRed.ESTADO_SELECCION_PERSONAJE.equals(jugador.getEstadoActual());

        jugador.setPuerto(dp.getPort());
        if (esDemo) {
            jugador.setIdDemo(idDemo);
        }

        enviarMensaje("Conexion recibida", dp.getAddress(), dp.getPort());

        if (estabaConectado && !tieneProgreso) {
            jugador.actualizarPing();
            if (cantClientes == JUGADORES.length) {
                enviarMensaje("Conexion establecida", dp.getAddress(), dp.getPort());
            }
            return;
        }

        if (estabaConectado && otro != null && otro.estaConectado()) {
            enviarMensaje("JUGADOR_DESCONECTADO", otro.getIp(), otro.getPuerto());
        }

        boolean requiereSnapshot = JugadorRed.ESTADO_JUGANDO.equals(jugador.getEstadoActual());
        jugador.marcarReconexion(requiereSnapshot);
        VUELVE_AL_HUB[indice] = false;

        enviarMensaje(armarMensajeReconexion(jugador, otro, requiereSnapshot), dp.getAddress(), dp.getPort());

        if (requiereSnapshot) {
            enviarMensaje("SNAPSHOT_REQUERIDO", dp.getAddress(), dp.getPort());
        }

        if (JugadorRed.ESTADO_HUB.equals(jugador.getEstadoActual())) {
            enviarMensaje("CANCELAR_SELECCION", dp.getAddress(), dp.getPort());
        }

        if (todosConectados()) {
            limiteReconexion = 0;
            enviarMensaje("Conexion establecida", dp.getAddress(), dp.getPort());
        }

        notificarReanudacionSiCorresponde(indice);
    }

    private String armarMensajeReconexion(JugadorRed jugador, JugadorRed otro, boolean requiereSnapshot) {
        String personajeOtro = otro != null && otro.getPersonaje() != null
            ? otro.getPersonaje()
            : PERSONAJE_POR_DEFECTO;

        StringBuilder mensaje = new StringBuilder("RECONEXION;")
            .append(jugador.getEstadoActual())
            .append(";")
            .append(personajeOtro);

        if (requiereSnapshot) {
            int numeroNivel = Servidor.instancia.getNumeroNivelActual();
            if (numeroNivel > 0) {
                mensaje.append(";").append(numeroNivel);
            }
        }

        return mensaje.toString();
    }

    private void registrarJugadorNuevo(DatagramPacket dp, int idDemo, boolean esDemo) {
        if (cantClientes >= JUGADORES.length) {
            return;
        }

        JugadorRed nuevo = new JugadorRed(dp.getAddress(), dp.getPort());
        if (esDemo) {
            nuevo.setIdDemo(idDemo);
        }

        JUGADORES[cantClientes] = nuevo;
        enviarMensaje("Conexion recibida", dp.getAddress(), dp.getPort());
        cantClientes++;

        if (cantClientes == JUGADORES.length) {
            for (int i = 0; i < cantClientes; i++) {
                enviarMensaje("Conexion establecida", JUGADORES[i].getIp(), JUGADORES[i].getPuerto());
            }
        }
    }

    private boolean todosConectados() {
        for (int i = 0; i < cantClientes; i++) {
            if (JUGADORES[i] == null || !JUGADORES[i].estaConectado()) {
                return false;
            }
        }
        return true;
    }

    private void procesarSolicitudSnapshot(int indice) {
        JugadorRed jugador = JUGADORES[indice];

        Servidor.instancia.enviarSnapshotEntidades(jugador.getIp(), jugador.getPuerto());
    }

    private synchronized void procesarSnapshotConfirmado(int indice) {
        JUGADORES[indice].marcarSincronizado();
        notificarReanudacionSiCorresponde(indice);
    }

    private void procesarDesconexion(int indice) {
        marcarDesconectado(indice);
    }

    private void procesarInput(String msj, JugadorRed jugador, int indice) {
        ULTIMO_INPUT[indice] = System.currentTimeMillis();
        String[] partes = msj.split(";");
        if (partes.length < 2) {
            return;
        }

        switch (partes[1]) {
            case "IZQUIERDA" -> jugador.setMovimientoHorizontal(true, jugador.mueveDerecha());
            case "NOIZQUIERDA" -> jugador.setMovimientoHorizontal(false, jugador.mueveDerecha());
            case "DERECHA" -> jugador.setMovimientoHorizontal(jugador.mueveIzquierda(), true);
            case "NODERECHA" -> jugador.setMovimientoHorizontal(jugador.mueveIzquierda(), false);
            case "ARRIBA" -> jugador.setMovimientoVertical(true, jugador.mueveAbajo());
            case "NOARRIBA" -> jugador.setMovimientoVertical(false, jugador.mueveAbajo());
            case "ABAJO" -> jugador.setMovimientoVertical(jugador.mueveArriba(), true);
            case "NOABAJO" -> jugador.setMovimientoVertical(jugador.mueveArriba(), false);
            case "ATACA" -> jugador.solicitarAtaque();
            case "E" -> jugador.solicitarInteraccion();
            default -> {
            }
        }
    }

    public void liberarInputsInactivos() {
        long ahora = System.currentTimeMillis();

        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador == null || ahora - ULTIMO_INPUT[i] <= TIEMPO_LIMITE_INPUT) {
                continue;
            }

            boolean seMueve = jugador.mueveIzquierda() || jugador.mueveDerecha()
                || jugador.mueveArriba() || jugador.mueveAbajo();
            if (seMueve) {
                jugador.setMovimientoHorizontal(false, false);
                jugador.setMovimientoVertical(false, false);
            }
        }
    }

    private void procesarSeleccionPersonaje(String msj, int indice) {
        if (cantClientes != JUGADORES.length || partidaIniciada) {
            return;
        }

        JugadorRed jugador = JUGADORES[indice];
        JugadorRed remoto = JUGADORES[1 - indice];
        if (remoto == null) {
            return;
        }

        jugador.setPersonaje(msj);

        enviarMensaje("PERSONAJE_REMOTO;" + msj, remoto.getIp(), remoto.getPuerto());
        jugador.setListoParaComenzar(false);
    }

    private void procesarListoComenzar(int indice) {
        if (cantClientes != JUGADORES.length || partidaIniciada) {
            return;
        }

        String personaje0 = JUGADORES[0].getPersonaje();
        String personaje1 = JUGADORES[1].getPersonaje();
        if (personaje0 == null || personaje1 == null || personaje0.equals(personaje1)) {
            return;
        }

        JugadorRed jugador = JUGADORES[indice];
        jugador.setListoParaComenzar(!jugador.isListoParaComenzar());
        enviarContadorComenzar();

        if (contarListos() >= MAX_JUGADORES) {
            iniciarJuego();
        }
    }

    private void procesarCancelarComenzar(int indice) {
        if (cantClientes != JUGADORES.length || partidaIniciada) {
            return;
        }

        JUGADORES[indice].setListoParaComenzar(false);
        enviarContadorComenzar();
    }

    private int contarListos() {
        int listos = 0;
        for (JugadorRed jugador : JUGADORES) {
            if (jugador != null && jugador.isListoParaComenzar()) {
                listos++;
            }
        }
        return listos;
    }

    private void enviarContadorComenzar() {
        enviarMensajeATodos("CONTADOR_COMENZAR;" + contarListos());
    }

    private void iniciarJuego() {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            JugadorRed remoto = JUGADORES[1 - i];

            enviarMensaje("Empieza juego", jugador.getIp(), jugador.getPuerto());
            enviarMensaje(remoto.getPersonaje(), jugador.getIp(), jugador.getPuerto());
            jugador.setEstadoActual(JugadorRed.ESTADO_HUB);
        }

        partidaIniciada = true;
        Global.empieza = true;
    }

    private void procesarSeleccionNivel(String msj, JugadorRed jugador) {
        String[] partes = msj.split(";");
        if (partes.length < 2) {
            return;
        }

        boolean solicitaSeleccion = Boolean.parseBoolean(partes[1]);
        jugador.setSolicitoSeleccionNivel(solicitaSeleccion);
        jugador.setEstadoActual(solicitaSeleccion
            ? JugadorRed.ESTADO_SELECCION_NIVEL
            : JugadorRed.ESTADO_HUB);
    }

    private void procesarVotoNivel(String msj, JugadorRed jugador) {
        String[] partes = msj.split(";");
        if (partes.length < 2) {
            return;
        }

        try {
            int nivel = Integer.parseInt(partes[1]);
            if (nivel == 0
                || (nivel > 0 && nivel <= MAX_NIVEL && Servidor.instancia.puedeEntrarANivel(nivel))) {
                jugador.setNivelSeleccionado(nivel);
            }
        } catch (NumberFormatException e) {
            System.out.println("Nivel recibido inválido");
        }
    }

    private synchronized void procesarVolverHub(int indice) {
        int indiceOtro = 1 - indice;
        JugadorRed jugador = JUGADORES[indice];
        JugadorRed otro = JUGADORES[indiceOtro];
        if (otro == null) {
            return;
        }

        if (VUELVE_AL_HUB[indice]) {
            VUELVE_AL_HUB[indice] = false;
            enviarMensaje("CANCELAR_HUB", otro.getIp(), otro.getPuerto());
            return;
        }

        VUELVE_AL_HUB[indice] = true;
        enviarMensaje("VOLVER_HUB;OTRO", otro.getIp(), otro.getPuerto());

        if (!VUELVE_AL_HUB[indiceOtro]) {
            return;
        }

        enviarMensaje("VOLVER_HUB;AMBOS", jugador.getIp(), jugador.getPuerto());
        enviarMensaje("VOLVER_HUB;AMBOS", otro.getIp(), otro.getPuerto());

        jugador.setEstadoActual(JugadorRed.ESTADO_HUB);
        otro.setEstadoActual(JugadorRed.ESTADO_HUB);

        VUELVE_AL_HUB[0] = false;
        VUELVE_AL_HUB[1] = false;

        Servidor.instancia.resetearNivelActual();
        Servidor.instancia.ejecutarCallbackVolverHub();
    }

    private synchronized void procesarCancelarHub(int indice) {
        VUELVE_AL_HUB[indice] = false;

        JugadorRed otro = JUGADORES[1 - indice];
        if (otro != null) {
            enviarMensaje("CANCELAR_HUB", otro.getIp(), otro.getPuerto());
        }
    }

    private int buscarJugador(InetAddress ip, int puerto) {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.getIp().equals(ip) && jugador.getPuerto() == puerto) {
                return i;
            }
        }
        return -1;
    }

    private int buscarJugadorPorIp(InetAddress ip) {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.getIp().equals(ip)) {
                return i;
            }
        }
        return -1;
    }

    private int buscarJugadorPorIdDemo(int idDemo) {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.getIdDemo() == idDemo) {
                return i;
            }
        }
        return -1;
    }

    public void enviarMensaje(String msj, InetAddress ipDestino, int puerto) {
        byte[] data = msj.getBytes();
        try {
            SOCKET.send(new DatagramPacket(data, data.length, ipDestino, puerto));
        } catch (IOException e) {
            if (!fin) {
                System.out.println("Error de envío: " + e.getMessage());
            }
        }
    }

    public void enviarMensajeATodos(String mensaje) {
        for (int i = 0; i < cantClientes; i++) {
            JugadorRed jugador = JUGADORES[i];
            if (jugador != null && jugador.estaConectado()) {
                enviarMensaje(mensaje, jugador.getIp(), jugador.getPuerto());
            }
        }
    }

    public JugadorRed[] getJugadores() {
        return JUGADORES;
    }

    public int getCantClientes() {
        return cantClientes;
    }

    public void cerrar() {
        fin = true;

        if (!SOCKET.isClosed()) {
            SOCKET.close();
        }
    }
}
