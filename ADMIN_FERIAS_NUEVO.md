# Ferias y Showrooms — Gestión por roles

- **Administrador** (`@admin.com` según la autenticación existente): Lista de ferias, botón Nuevo, editar/eliminar eventos locales y formulario `ModalBottomSheet` animado. Tipos: Feria, Showroom, Exposición y Otro (campo adicional obligatorio).
- **Cliente**: lista, detalles y mapa sin herramientas de edición.
- **Mapa**: OpenStreetMap/osmdroid. La flecha circular regresa a Lista. Cada evento con coordenadas válidas aparece como marcador.
- **Caché**: SQLite local `ferias_lou_araya.db`. Al subir la versión de esquema a 2 se recrea la tabla anterior, por lo que la caché antigua se reinicia una vez.
- **API REST**: `FERIAS_API_URL` en `PantallaMapaFerias.kt`. La URL está vacía: los eventos creados en esta versión se guardan **localmente** hasta configurar un backend. Si se configura la URL, GET obtiene los eventos y POST/PUT/DELETE intentan sincronizar. El backend debe implementar estos endpoints, aceptar el JSON indicado y verificar la identidad y los permisos de administrador. Esta versión no implementa autenticación remota ni un servidor REST.
- **Evento de demostración**: aparece únicamente cuando no hay eventos reales y no se ha configurado API. No representa una participación confirmada de la tienda.
- **Pruebas**: iniciar sesión como admin, abrir Mapa de ferias, Nuevo, rellenar campos y coordenadas, guardar, ir a Mapa, volver a Lista, editar y eliminar. Comprobar que cliente no vea Nuevo/Editar/Eliminar.
- **Pendiente**: compilar y ejecutar en Android Studio; no se verificó en un emulador aquí.
