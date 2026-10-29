# Caja de Enmanuel - Kotlin

Proyecto Android nativo de Enmanuel Chavez, matrícula 2025-1099. Incluye predicción de género, persona aleatoria, universidades, clima, Pokémon con sonido, noticias y contacto.

## Compilación

Abrir la carpeta en Android Studio, configurar el SDK de Android y usar `./gradlew assembleDebug`. Se usa Android SDK 35 y JDK compatible con Gradle 9.6. El APK queda en `app/build/outputs/apk/debug/`.

## Fuentes y atribuciones

- Fotografía de caja de herramientas: Per Erik Strandberg, [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:20060513_toolbox.jpg), [CC BY-SA 2.5](https://creativecommons.org/licenses/by-sa/2.5/); archivo original sin modificar, mostrado ajustado al espacio.
- Noticias: [WordPress.org News](https://wordpress.org/news/wp-json/wp/v2/posts?per_page=3).
- Servicios: Genderize, Random User, Adamix, Open-Meteo y PokéAPI. La aplicación requiere conexión a Internet. El clima corresponde a Santo Domingo y usa la fecha y zona horaria de República Dominicana.
