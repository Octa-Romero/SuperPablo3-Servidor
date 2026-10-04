# 🎮 Super Pablo 3: La Última Batalla

## 👥 Integrantes del grupo

- Santiago Papale
- Octavio Romero
- Matias Rodrigo Zurita

---

## 📖 Descripción

**Super Pablo 3: La Última Batalla** es un videojuego de acción y aventura en **2D** con vista lateral (*side-view*), desarrollado en **Java** utilizando el framework **LibGDX**. El juego representa la tercera y última entrega de la saga **Super Pablo**, donde dos jugadores deberán cooperar para recorrer distintos escenarios, enfrentarse a enemigos y jefes finales, obtener reliquias y detener definitivamente a **El Retumbar**, la entidad responsable de la corrupción del mundo.

<img width="480" height="480" alt="Gif de Felipe el Sabio" src="assets/README/readmegif.gif"/>

---

## 🎯 Características principales

- Videojuego de acción y aventura en 2D.
- Vista lateral (*side-view*).
- Modo cooperativo para dos jugadores mediante red local (LAN).
- Arquitectura Cliente-Servidor.
- Sistema de combate cuerpo a cuerpo y a distancia.
- Enemigos con distintos comportamientos.
- Jefe final.
- NPCs e historia original.
- Sistema de reliquias para progresar en la campaña.

---

## 🚀 Tecnologías utilizadas

- **Java 21 (JDK 21)**
- **LibGDX 1.14.2**
- **Gradle 9.6.1**
- **Git & GitHub**

### Plataformas objetivo

- 💻 Escritorio (Windows/Linux/macOS)

---

## 📌 Estado actual del proyecto

🚧 **Estructura actual del juego (Niveles, Gameplay, UI y Sistemas)**

- El proyecto actualmente cuenta con una estructura modular basada en niveles y pantallas, integrando la lógica principal del juego dentro de la clase Nivel. Se han implementado sistemas de gameplay como movimiento del jugador, colisiones con el mapa, combate básico, enemigos con comportamiento simple, sistema de vida y HUD. Además, el juego incluye sistemas de interfaz como menú de opciones, pausa, selección de personajes y game over, junto con un sistema de audio con música y efectos de sonido. La arquitectura general ha sido refactorizada para mejorar la organización del código y la integración de sistemas.

- [Video demostrativo](https://drive.google.com/file/d/10IqZd_WzuzKZK6zl2mERvlTAuxkcMpuN/view?usp=sharing)

---

## 📋 Historial de Cambios (Changelog)

[Ver Historial de Cambios](CHANGELOG.md)

---

## ⚙️ Cómo compilar y ejecutar

### 1. Clonar el repositorio

```bash
git clone https://github.com/Octa-Romero/SuperPablo3.git
```

### 2. Ingresar a la carpeta del juego

```bash
cd SuperPablo3
```

### 3. Abrir el proyecto

Abrir el proyecto con un IDE compatible con Gradle, por ejemplo:

- IntelliJ IDEA
- Eclipse
- Visual Studio Code (con soporte para Java)

### 4. Descargar las dependencias

Gradle descargará automáticamente todas las dependencias de LibGDX al abrir el proyecto.

### 5. Ejecutar el juego

Ejecutar la clase principal del módulo **lwjgl3** (`Lwjgl3Launcher.java`).

También es posible ejecutar desde la terminal:


En Windows:

```bash
gradlew.bat lwjgl3:run
```

Linux / macOS:

```
./gradlew lwjgl3:run
```

---

## ℹ️ Wiki del proyecto

La propuesta detallada del videojuego se encuentra disponible en la [Wiki del proyecto](https://github.com/Octa-Romero/SuperPablo3/wiki).

---

## 📄 Licencia

Proyecto desarrollado con fines educativos para la materia **Programación sobre Redes**.
