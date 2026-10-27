# Pantalla 5 — Lista y mapa

- La entrada ahora abre **Lista**, con tarjetas y botón **Ver en el mapa**.
- En la barra inferior, **Lista** precede a **Mapa**. Ambos accesos se mantienen visibles.
- En el mapa hay una flecha circular flotante para regresar a Lista.
- Al cambiar entre las vistas, el MapView se vuelve a adjuntar y se actualizan sus marcadores.
- Se muestra una feria **DEMOSTRACIÓN** en Parque Bicentenario (ubicación ilustrativa), solo cuando no existe API configurada ni caché. No se guarda como evento real en SQLite.
- API: configurar FERIAS_API_URL en PantallaMapaFerias.kt. Se espera JSON array con id, nombre, tipo, fecha, lugar, lat, lon.
- Si falla la API, se usa la última respuesta guardada en SQLite.
- La vista de mapa usa mosaicos OpenStreetMap y requiere internet para descargar calles nuevas.

No se ha compilado en Android Studio en este entorno. Verificar sincronización de Gradle y prueba en emulador.
