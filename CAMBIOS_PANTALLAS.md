# Cambios realizados en la integración de pantallas

## Flujo cliente

- El botón **Comenzar** desde la bienvenida abre el catálogo.
- El inicio de sesión con rol `USER` abre el mismo catálogo.
- El catálogo tiene búsqueda, filtros por categoría, favoritos durante la sesión y una ventana con los detalles del producto.
- El catálogo lee los productos guardados en el almacenamiento local compartido con el panel de administración.

## Flujo administrador

- El inicio de sesión con rol `ADMIN` abre el dashboard.
- El resumen del dashboard muestra el total de productos y cuántos tienen stock.
- La opción **Gestión de productos** abre `PantallaProductosAdmin`.
- La gestión de productos permite buscar, filtrar por disponibilidad/stock bajo, agregar, editar y eliminar.
- Los campos gestionables son nombre, categoría, material, dimensiones, precio y stock. El código `MAC-###` se asigna automáticamente a los productos nuevos.
- Los datos se guardan en `SharedPreferences` mediante `AlmacenLocal`, de modo que las modificaciones se conservan en el dispositivo y se muestran en el catálogo cliente. Esto es almacenamiento local de prototipo, todavía no Room ni API REST.
- Las imágenes de muestra se eligen automáticamente según la categoría usando los recursos de imagen existentes.
- La opción **Gestionar campañas** sigue abriendo la pantalla de campañas existente. Solicitudes y promociones continúan como módulos pendientes.

## Verificación y límites

- Se revisó que la navegación tiene rutas para dashboard y gestión de productos, y que las llamadas a `PantallaCatalogo` y `PantallaDashboardAdmin` están actualizadas.
- No se pudo realizar una compilación Gradle completa en este entorno: el wrapper necesita descargar Gradle desde `services.gradle.org`, pero no hubo conexión de red. Ejecutar **Sync Project with Gradle Files** y luego **Run** en Android Studio antes de entregar.
- `local.properties` se excluye del ZIP porque contiene rutas del SDK específicas de un computador.
