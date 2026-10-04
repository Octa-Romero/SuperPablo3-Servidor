# Changelog

Todos los cambios importantes de este proyecto serán documentados en este archivo.

[Volver al repositorio](https://github.com/Octa-Romero/SuperPablo3)

## [0.3.1] - 2026-09-19

### Cambiado
- Desacoplada la navegación de pantallas mediante la interfaz `Navegable`, eliminando la dependencia directa con `Principal`.
- Unificación del uso de `GestorRecursos` para la carga de texturas reutilizables.
- Optimización del HUD para evitar reconstrucción innecesaria de corazones en cada frame.
- Reorganización del manejo de entrada eliminando asignaciones redundantes de `InputProcessor`.
- Limpieza del repositorio eliminando archivos generados (`build/`) y ajuste de `.gitignore`.
- Unificación del versionado entre `CHANGELOG` y `gradle.properties`.

### Corregido
- Corrección en el ciclo de vida de pantallas: gestión adecuada de `dispose()` en Screens.
- Ajuste en `InputMultiplexer` para evitar conflictos entre el juego y el menú de pausa.
- Corrección del respawn del jugador desacoplándolo de la resolución (`Gdx.graphics.getHeight()`).
- Actualización del HUD en `resize()` mediante `actualizarTamano()`.
- Liberación completa de entidades en `Nivel.dispose()`.
- Corrección en la gestión de recursos compartidos (ownership de texturas).



## [0.3.0] - 2026-09-04

### Agregado
- Pantalla de Game Over para finalización de partida.
- Pantalla de selección de personajes.
- Sistema de menú de opciones con configuración de resolución y fullscreen.
- Integración del menú de opciones dentro del menú de pausa.
- Sistema de ataque para Walter con animaciones mediante spritesheets.
- Ajustes visuales en botones y estilo general del menú.
- Sistema de pausa completo en PantallaJuego.

### Cambiado
- Refactor del sistema de configuración de resolución y fullscreen.
- Mejora en la arquitectura del menú principal usando ExtendViewport.
- Reorganización de la lógica de PantallaMenu para mejor inicialización.
- Optimización de carga de texturas de entidades.
- Refactor general del sistema de UI (HUD, menúes y pantallas).
- Centralización de lógica de gameplay en Nivel.

### Corregido
- Fix de error al spawnear al jugador (Pablo).
- Correcciones en conflictos de merge en múltiples pantallas y sistemas.
- Liberación de texturas para evitar memory leaks.
- Eliminación de imports innecesarios.
- Ajustes en variables locales y limpieza de código.



## [0.2.4] - 2026-09-01

### Agregado
- Sistema de pausa en la pantalla de juego (PantallaJuego), permitiendo detener la lógica del gameplay.

### Cambiado
- Refactor de la creación y estructura de PantallaMenu para mejorar la organización del código y su inicialización.



## [0.2.3] - 2026-09-01

### Agregado
- Sistema de audio con música de fondo y efectos de sonido.
- Sistema de ataque del jugador (Pablo).
- Sprites placeholder para prototipado visual.
- Clase reutilizable para manejo de imágenes/sprites.

### Cambiado
- Mejora en la cámara del menú con renderizado independiente.
- Reorganización de estructura de carpetas del proyecto(texturas y niveles).

### Corregido
- Liberación de texturas para evitar memory leaks.
- Eliminación de imports innecesarios en HUD.
- Correcciones generales de gestión de recursos.



## [0.2.2] - 2026-08-31

### Agregado
- Sistema de ataque para el jugador (Pablo), incluyendo animaciones y lógica de combate.
- Implementación de HUD para visualización de estado del jugador.

### Cambiado
- Refactor general del sistema de entidades aplicando herencia para unificar comportamiento base.



## [0.2.1] - 2026-08-30

### Agregado
- Sistema de enemigos con lógica de persecución hacia el jugador.
- Implementación de ataques básicos y patrones de comportamiento enemigo.
- Sistema de vida para el jugador con gestión de daño.

### Cambiado
- Refactor de la clase Nivel para centralizar la lógica de update: 
movimiento del jugador, colisiones, enemigos y cámara del nivel.



## [0.2.0] - 2026-08-29

### Agregado
- Se integraron dos ramas principales del proyecto, una orientada al sistema de mapa (TiledMap, camara, viewport y menu de inicio) y otra orientada al jugador (movimiento y colisiones).
- Implementación de colisiones basicas del jugador con el mapa utilizando el layer de TiledMap.
- Mejoras en el sistema de movimiento del jugador, incluyendo la mecanica de salto.
- Incorporación del sistema de camara y viewport para el seguimiento del jugador.
- Clases como Camara, Colisiones y Entrada.
- Estructura basica para sonidos y musicas.



## [0.1.4] - 2026-08-16

### Agregado
- Se modifico la version de Java a 21.
- build.gradle y gradle.properties fueron configurado correctamente para correr esta version.
- Se realizo un merge minimo para modificar lo ya establecido en el repositorio remoto.



## [0.1.3] - 2026-08-04

### Agregado
- Paquetes como entidades, recursos, utilidades y pantallas.
- Pantallas para el juego en general, el menu y pausa pero sin una logica significativamente importante aún.
- Clase Render para manejar el batch de forma static.
- Logica basica de movimiento para Jugador.



## [0.1.2] - 2026-07-15

### Agregado
- Creación del archivo `CHANGELOG.md`.



## [0.1.1] - 2026-07-14

### Agregado
- Activación de la Wiki del proyecto en GitHub.
- Incorporación de la propuesta inicial del videojuego en la Wiki.



## [0.1.0] - 2026-07-13

### Agregado
- Inicialización del proyecto con LibGDX.
- Creación del repositorio en GitHub.
- Configuración del archivo `.gitignore`.
- Creación del archivo `README.md`.
- Estructura base del proyecto preparada para el desarrollo del videojuego.
