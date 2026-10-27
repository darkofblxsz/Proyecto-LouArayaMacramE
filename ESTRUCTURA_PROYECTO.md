# Organización del proyecto LouAraya Macramé

La estructura solicitada está en `app/src/main/java/cl/louaraya/macrame/`.

| Carpeta | Responsabilidad | Archivos principales |
| --- | --- | --- |
| ui/screens | Pantallas Compose | Catálogo, login, registro, productos, campañas, solicitudes, bienvenida y términos |
| ui/components | Componentes reutilizables y acceso visual a cámara/galería/QR | BannerOferta, LectorQr, MultimediaProductos |
| ui/navigation | Navegación y conexión de eventos con el ViewModel | LouArayaApp |
| ui/theme | Colores, tipografías y tema | Color, Theme, Type |
| viewmodel | Estado y acciones de presentación | TiendaViewModel, SolicitudesViewModel, PromocionesViewModel |
| model | Clases de datos | ProductoLocal, Campana, SolicitudLocal, OfertaLocal |
| repository | Lectura y escritura local | AlmacenLocal, SolicitudesLocal, PromocionesLocal, ImagenesCampanasRepository |

`MainActivity.kt` permanece en el paquete principal como punto de entrada de Android.

## Cómo se conecta

La Activity obtiene TiendaViewModel con ViewModelProvider. LouArayaApp muestra la pantalla seleccionada y envía los eventos al ViewModel. Los ViewModels usan los repositorios, y estos guardan los datos mediante SharedPreferences. Los modelos representan productos, campañas, solicitudes y ofertas.

TiendaViewModel conserva el estado del registro, la sesión y la navegación con SavedStateHandle. Las pantallas conservan estado visual local, como diálogos, selección de fotos y campos de edición. Solicitudes y promociones obtienen sus ViewModels con el propietario de ciclo de vida de Compose. La cámara y los selectores siguen en componentes porque requieren los lanzadores de resultados de Android.

## Abrir en Android Studio

1. Descomprime el ZIP en una carpeta nueva para no mezclar archivos con la versión anterior.
2. Abre la carpeta que contiene `settings.gradle.kts`.
3. Espera la sincronización de Gradle (necesita Internet para descargar dependencias).
4. Ejecuta `app` en tu teléfono o emulador.

Se conservan recursos, identificador de aplicación, claves de almacenamiento y configuración original de SDK/Gradle. Se añaden las dependencias de ViewModel Compose y SavedState de la misma versión Lifecycle existente (2.6.1). No desinstales la aplicación si quieres conservar los datos locales.

## Verificación

Se revisaron paquetes, imports, separación de modelos, referencias de las pruebas y conservación de recursos. Se intentó ejecutar `:app:compileDebugKotlin :app:testDebugUnitTest`, pero el entorno no pudo descargar Gradle 9.8.0 por falta de acceso de red. Por ello no se certifica compilación ni ejecución en dispositivo.

Después de sincronizar, ejecuta las pruebas locales en Android Studio y `SolicitudesIntegrationTest` en un dispositivo. Revisa además registro/login, catálogo, crear/editar productos, cámara/galería, QR, promociones, campañas y el cambio de estado de solicitudes con su stock.
