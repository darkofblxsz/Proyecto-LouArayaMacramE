# Mapa de ferias y showrooms — Pantalla 5

Integrado en el proyecto original: clientes desde la barra inferior del catálogo (Mapa); administradores desde Accesos rápidos (Mapa de ferias).

## API REST
En `PantallaMapaFerias.kt`, asigna a `FERIAS_API_URL` una URL HTTPS válida. El endpoint debe devolver un array JSON:

```json
[{"id":1,"nombre":"Feria de Artesanía","tipo":"Feria","fecha":"2026-11-15","lugar":"Vitacura, Santiago","lat":-33.3987,"lon":-70.6013}]
```

Tipos admitidos por los filtros: Feria, Showroom y Evento. Al recibir datos válidos se guardan en SQLite; sin internet se reutiliza el último caché. Mientras no se configure una API, se muestran tres ubicaciones DEMOSTRATIVAS que no representan eventos confirmados.

## Mapa
Usa Leaflet y teselas OpenStreetMap en WebView: requiere conexión a Internet para cargar el mapa visual. Los datos de eventos almacenados en SQLite siguen disponibles sin conexión.

## Importante
No se ha compilado el APK en este entorno; abrir el proyecto con Android Studio, sincronizar Gradle y probar en emulador/dispositivo.
