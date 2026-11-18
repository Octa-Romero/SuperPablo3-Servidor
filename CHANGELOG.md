# Changelog

Todos los cambios importantes del módulo servidor de Super Pablo 3 serán documentados en este archivo.

Las primeras versiones registran el desarrollo del juego original, que sirvió como base para el módulo servidor creado el 4 de octubre de 2026. A partir de esa fecha, el historial distingue la incorporación de funcionalidades del juego de las adaptaciones necesarias para ejecutar una partida autoritativa y sincronizarla con los clientes.

[Volver al repositorio del servidor](https://github.com/Octa-Romero/SuperPablo3-Servidor)

## [0.6.0] - 2026-11-17

### Agregado
- Detección de desconexiones, conservación temporal de las sesiones y gestión de la reconexión de jugadores mediante PING/PONG.
- Recuperación del estado de la partida mediante snapshots de jugadores, enemigos, proyectiles y jefe, enviadas al cliente que se reconecta.
- Gestión de la muerte de los jugadores y del regreso al Hub desde el servidor.
- Integración del estado de las reliquias en la sincronización entre servidor y clientes.

### Cambiado
- Separación de responsabilidades entre la comunicación UDP y la lógica autoritativa del juego, con optimizaciones en el servidor.
- Desacoplamiento de la solicitud y el envío de snapshots mediante callbacks y una cola concurrente.
- Eliminación del procesamiento de las animaciones de reliquias del servidor, dejando su representación visual al cliente.

### Corregido
- Ajustes en la emisión de los sonidos de ataque de enemigos cuerpo a cuerpo.

## [0.5.1] - 2026-11-15

### Agregado
- Envío de estados de Retumbar y sus componentes: manos, fuego del suelo, rayos, láseres, meteoritos y fueguitos.
- Identificación del tipo de fueguito para sincronizar por separado los ataques de lluvia y los radiales.
- Restricciones de selección para impedir que ambos jugadores elijan el mismo personaje.

### Cambiado
- Reorganización de los mensajes e identificadores utilizados para comunicar las entidades y ataques del jefe.
- Ajustes en el seguimiento de enemigos y en la actualización de los estados de combate transmitidos por el servidor.

### Corregido
- Problemas de seguimiento y sincronización de Retumbar y sus ataques.
- Ajustes en la sincronización del estado de invencibilidad y visibilidad de los personajes.

## [0.5.0] - 2026-11-12

### Agregado
- Integración de las mecánicas del juego original en la ejecución autoritativa del servidor multijugador.
- Envío periódico de posiciones, estados de ataque y vida de ambos jugadores hacia los clientes.
- Mensajes para comunicar creación, actualización y eliminación de enemigos y proyectiles, incluidos los ataques de Walter.
- Gestión cooperativa de la selección de niveles, con validación de los votos de ambos jugadores antes de iniciar la partida.

### Cambiado
- Reorganización del procesamiento de mensajes UDP en métodos según su responsabilidad.
- Administración de los estados de enemigos mediante listas y mensajes parametrizados, incorporando su tipo para identificarlos.

### Corregido
- Comportamiento de enemigos sobre pendientes en la lógica de los niveles.

## [0.4.1] - 2026-11-06

### Agregado
- Incorporación de la lógica de combate de Retumbar, con ataques de meteoritos, rayos, enemigos y transiciones entre fases.
- Desarrollo de los ataques de la segunda fase del jefe y actualización de las animaciones utilizadas por las entidades del servidor.
- Integración del cierre de partida y del flujo de créditos en la progresión del juego.

### Cambiado
- Ampliación de las fases y patrones de ataque de Retumbar, incluyendo mejoras en su comportamiento.

### Corregido
- Ajustes en la vida de las manos del jefe y en los ataques adicionales de su primera fase.
- Correcciones de colisiones en las pendientes de los niveles 2 y 4 y del fondo del nivel 3.

## [0.4.0] - 2026-10-31

### Agregado
- Implementación de nuevos niveles, ampliando la progresión del juego hasta el nivel 7.
- Incorporación del NPC Pancrasio en el Hub, junto con su sistema de diálogo.
- Implementación del sistema de reliquias y su integración en los niveles.
- Incorporación de nuevos recursos y elementos para ampliar los escenarios del juego.
- Desarrollo de nuevos escenarios y mapas mediante Tiled.

### Cambiado
- Ampliación y reorganización de los niveles, incorporando nuevas áreas y mecánicas.
- Ajustes en las colisiones y el comportamiento de la cámara para mejorar la experiencia de juego.
- Integración de los nuevos niveles con el sistema de navegación y selección desde el Hub.
- Actualización de recursos gráficos y elementos visuales de los escenarios.

### Corregido
- Ajustes en las mecánicas de los niveles para mejorar su funcionamiento.
- Correcciones en la integración de recursos y escenarios.

## [0.3.4] - 2026-10-28

### Agregado
- Primeros mensajes de red para comunicar las posiciones y los estados de ataque de los jugadores.
- Incorporación inicial de Retumbar y de enemigos a distancia a la base de juego utilizada por el servidor.

### Cambiado
- Ajuste del procesamiento de movimientos recibidos mediante indicadores booleanos para representar las teclas presionadas.
- Reorganización de paquetes de entidades y separación de la lógica de animación reutilizable.

### Corregido
- Ajustes de funcionamiento del Hub durante la integración de las mecánicas existentes.

## [0.3.3] - 2026-10-12

### Agregado
- Creación del módulo servidor a partir de la estructura del juego original.
- Implementación inicial de un socket UDP para recibir mensajes desde los clientes.
- Incorporación de la lógica del Hub, selección de niveles y espera de dos jugadores durante la selección de personajes.

### Cambiado
- Organización inicial del registro y la administración de conexiones y jugadores mediante un `HashMap`.

## [0.3.2] - 2026-09-22

### Agregado
- Incorporación del nivel 2, nuevos recursos y escenarios al juego base posteriormente utilizado por el servidor.
- Primeras cinemáticas e integración de nuevos niveles e interfaces de menú en la base del proyecto.

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
- Refactor general del sistema de UI (HUD, menús y pantallas).
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
- Refactor de la clase Nivel para centralizar la lógica de update: movimiento del jugador, colisiones, enemigos y cámara del nivel.

## [0.2.0] - 2026-08-29

### Agregado
- Se integraron dos ramas principales del proyecto, una orientada al sistema de mapa (TiledMap, camara, viewport y menu de inicio) y otra orientada al jugador (movimiento y colisiones).
- Implementación de colisiones básicas del jugador con el mapa utilizando el layer de TiledMap.
- Mejoras en el sistema de movimiento del jugador, incluyendo la mecánica de salto.
- Incorporación del sistema de cámara y viewport para el seguimiento del jugador.
- Clases como Camara, Colisiones y Entrada.
- Estructura básica para sonidos y musicas.

## [0.1.4] - 2026-08-16

### Agregado
- Se modifico la version de Java a 21.
- build.gradle y gradle.properties fueron configurados correctamente para correr esta version.
- Se realizó un merge minimo para modificar lo ya establecido en el repositorio remoto.

## [0.1.3] - 2026-08-04

### Agregado
- Paquetes como entidades, recursos, utilidades y pantallas.
- Pantallas para el juego en general, el menu y pausa pero sin una lógica significativamente importante aún.
- Clase Render para manejar el batch de forma static.
- Lógica básica de movimiento para Jugador.

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
