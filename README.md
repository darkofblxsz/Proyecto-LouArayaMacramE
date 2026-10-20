# Lou Araya Macramé — prototipo Android

Proyecto Android en Kotlin y Jetpack Compose.

## Flujo integrado

- **Comenzar** desde la bienvenida abre el catálogo de cliente.
- **Iniciar sesión** conserva la autenticación local por rol: `USER` abre el catálogo y `ADMIN` abre el dashboard administrativo.
- En el catálogo se pueden buscar productos ficticios, filtrar por categoría, guardar favoritos durante la sesión y consultar los datos de un producto.
- Desde el dashboard se puede abrir la gestión de productos o la gestión de campañas.
- La gestión de productos permite buscar, filtrar por disponibilidad o stock bajo, agregar, editar y eliminar productos. Los cambios persisten localmente y aparecen en el catálogo cliente.
- Las secciones de solicitudes y promociones siguen pendientes.

## Cuentas de demostración

La aplicación crea estas cuentas locales de prueba si aún no hay cuentas guardadas:

- Administrador: `admin@admin.com` / `Admin1234!`
- Cliente: `usuario@gmail.com` / `Usuario1234!`

Son credenciales ficticias del prototipo, no deben utilizarse en un sistema real.

## Datos y almacenamiento

Los productos, precios, stock y campañas son datos de demostración. Productos y campañas se guardan localmente en `SharedPreferences`. Esta versión todavía no conecta el catálogo a Room ni a una API REST; esa integración puede hacerse después.

## Abrir en Android Studio

1. Descomprime el ZIP.
2. Abre la carpeta raíz que contiene `settings.gradle.kts`.
3. Permite que Android Studio configure `local.properties` con la ubicación del Android SDK de tu computador.
4. Sincroniza Gradle y ejecuta la configuración `app` en un emulador o dispositivo.


## Solicitudes, cámara, galería y QR
Consulta [GUIA_SOLICITUDES_FOTOS_QR.md](GUIA_SOLICITUDES_FOTOS_QR.md) para las cuentas, uso, limitaciones y pruebas de estas funciones.
