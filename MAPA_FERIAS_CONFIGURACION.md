# Mapa de Ferias y Showrooms – Lou Araya Macramé

## Configuración obligatoria de la API
En `app/src/main/java/cl/louaraya/macrame/ui/screens/PantallaMapaFerias.kt` cambia `FERIAS_API_URL = ""` por la URL HTTPS de tu servicio REST. No se incluye un backend real: sin URL, la app mostrará un aviso y los eventos previamente almacenados, si existen.

Respuesta esperada (array JSON):
```json
[{"id":1,"nombre":"Feria confirmada","tipo":"Feria","fecha":"2026-11-20","lugar":"Santiago, Chile","lat":-33.4489,"lon":-70.6693}]
```
Usa únicamente eventos reales de la tienda. Los tipos admitidos son `Feria`, `Showroom`, `Evento`.

## Funcionalidad
- Mapa nativo OpenStreetMap con gestos y zoom, sin WebView ni JavaScript CDN. Las teselas del mapa requieren internet y dependen del proveedor OpenStreetMap.
- Marcadores que abren tarjetas con nombre, fecha y lugar, más botón Cómo llegar.
- Filtros y vista de lista.
- Consulta HTTP REST y caché SQLite (`SQLiteOpenHelper`), conservando datos cuando falla la red.
- Acceso cliente/admin desde navegación existente.

## Comprobación
Abrir en Android Studio, sincronizar Gradle (requiere descargar osmdroid 6.1.20), configurar API, compilar y probar en emulador con internet. Prueba desconectando internet tras la primera descarga: los eventos seguirán en SQLite, aunque las teselas del mapa podrían no estar disponibles sin caché previa.

No se ha compilado este proyecto en este entorno; comprobar en Android Studio.
