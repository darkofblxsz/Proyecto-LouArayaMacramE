# Lou Araya · Inicio visual, notificaciones y administración de cuentas

## Abrir en Android Studio

1. Extrae el ZIP y abre la carpeta que contiene `settings.gradle.kts`.
2. Espera la sincronización de Gradle. Se conserva la configuración de compilación del proyecto recibido (SDK 37 y herramientas declaradas en Gradle).
3. Selecciona tu teléfono y pulsa Run. No desinstales la aplicación ni borres sus datos si quieres conservar tus cuentas y pedidos locales.

## Nuevo inicio y administración

- Inicio inspirado en una tienda: banner visual, iconos circulares de categorías y productos en tarjetas horizontales. Se mantienen los colores de Lou Araya.
- Corazón y campana juntos en la cabecera. La campana muestra el número de avisos sin leer y abre la bandeja.
- Una sola barra inferior compartida por Inicio, Catálogo, Favoritos, Mapa, Pedidos, Perfil y Notificaciones del cliente. Cada destino mantiene siempre su icono; cambia únicamente la selección.
- Las categorías del inicio abren el catálogo filtrado. Favoritos muestra los favoritos de todas las categorías.
- Promociones activas del administrador en el inicio, con tiempo restante y detalle al tocar. Al vencer dejan de mostrarse. No se crean porcentajes ficticios: el título y condiciones son los publicados por el administrador. Esta función presenta la promoción; no añade un cálculo automático de descuento al pedido.
- Cada aviso de la bandeja se abre para leerlo completo y se marca individualmente como leído. Tocar el aviso del sistema abre su detalle después de iniciar sesión en la cuenta destinataria.
- Panel → Administrar cuentas: búsqueda y filtros para clientes y administradores; edición de nombre, apellido, usuario, teléfono y contraseña opcional. Se mantiene el correo por ser la identidad del historial.
- Panel → Crear administrador: registro validado con rol ADMIN, independientemente del dominio del correo.
- Eliminar cuenta pide confirmación; no permite eliminar la cuenta activa ni cuentas con pedidos pendientes/confirmados. Los pedidos cerrados permanecen en el historial. Las cuentas de prueba eliminadas no vuelven a crearse. No se reutilizan correos eliminados para evitar mezclar historiales.

## Comprobar esta versión

1. Abre el inicio y toca cada categoría; comprueba su filtro en el catálogo y la barra inferior uniforme.
2. Como administrador, publica una promoción con fecha futura. Como cliente, abre el inicio y toca la tarjeta para ver sus condiciones y productos.
3. Genera dos notificaciones. Abre solo una: debe bajar el contador en una unidad. Prueba también tocar el aviso en la barra de notificaciones de Android.
4. Crea un administrador con correo Gmail, cierra sesión e inicia con su nueva clave: debe abrir el panel y permitir gestionar ferias y cuentas.
5. Edita un cliente y comprueba su nuevo nombre de usuario/contraseña. Cancela una eliminación y comprueba que sigue existiendo; después confirma la eliminación de una cuenta sin pedidos activos.

Se agregaron seis pruebas instrumentadas en `CuentasNotificacionesTest.kt` para permisos, creación, edición, eliminación, pedidos activos y lectura individual de avisos. No pudieron ejecutarse en este entorno; requieren un dispositivo/emulador Android.

```bat
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:connectedDebugAndroidTest
```

## Funciones conservadas

- Logo con animación de crecimiento al entrar, también en el inicio del cliente.
- Inicio con bienvenida por nombre, accesos al catálogo, favoritos, mapa, pedidos, perfil y notificaciones, y menú inferior.
- Advertencias rojas debajo de cada dato de registro inválido. Pulsar Crear cuenta muestra también los errores de campos vacíos. Correos y usuarios repetidos se indican en su campo.
- Foto de perfil en lugar de LA. Sin foto se usa un icono de persona. Desde Perfil se puede seleccionar una foto, editar nombre, apellido y teléfono, y cambiar la contraseña comprobando la contraseña actual. El correo conserva la asociación con los pedidos.
- Favoritos persistentes por cuenta y filtro por material en catálogo.
- Personalización opcional, desactivada al crear un pedido: paleta de colores y selector de materiales con búsqueda. Desactivarla conserva el diseño original, aunque antes se hayan elegido otros colores.
- Revisar pedido muestra un resumen. Solo Confirmar guarda; volver o cancelar no envía. La personalización está sujeta a aprobación del administrador y no calcula un precio nuevo.
- Mis pedidos permite editar cantidad, fecha, comentario, color y material de solicitudes pendientes. Pedidos confirmados o cerrados no se pueden modificar desde el cliente.
- Dashboard del administrador con nombre y foto, recuentos reales de productos disponibles, solicitudes pendientes y confirmadas, aviso de stock bajo (2 unidades o menos), y accesos a gestión, perfil y notificaciones.
- Bandeja persistente para nuevas solicitudes, cambios de estado, cambios del cliente y promociones. Tiene estado leído/no leído.

## Notificaciones: alcance

Se conservó el almacenamiento local del ZIP original (SharedPreferences y JSON). No hay servidor ni sincronización entre teléfonos. Para demostrar el flujo, crea una solicitud como cliente, entra como administrador EN EL MISMO TELÉFONO, confirma la solicitud y vuelve a entrar como cliente.

En Android 13 o superior, abre Notificaciones y pulsa Activar avisos en el teléfono. Sin permiso puedes leer la bandeja igualmente. Con permiso, la sesión activa recibe avisos locales y al entrar se avisa de novedades no mostradas. No se muestran contenidos privados en el aviso de la pantalla de bloqueo. Una app cerrada no recibe cambios procedentes de otros dispositivos; eso requiere un servicio remoto.

## Cuentas de demostración

Solo se crean si no existen: administrador `admin@admin.com`, contraseña `Admin1234!`; cliente `usuario@gmail.com`, contraseña `Usuario1234!`. Si cambias sus contraseñas desde Perfil, se conserva tu nueva contraseña y ya no se restablece la de demostración al abrir la app.

## Comprobaciones

Se revisaron rutas, llamadas y persistencia de los campos nuevos, y se verificaron los XML y la integridad del ZIP. No fue posible compilar ni ejecutar la aplicación aquí: falta la distribución Gradle y el entorno no permite descargarla. Tampoco se pudo hacer una prueba visual en emulador. No se incluye un APK compilado.

Se añadieron cinco pruebas unitarias de registro (datos válidos, errores simultáneos, fechas imposibles/futuras, dominio reservado y campos vacíos). Están pendientes de ejecutar. En la terminal de Android Studio en Windows:

```bat
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Prueba manual sugerida:

1. Pulsa Crear cuenta con campos vacíos; revisa cada advertencia roja. Corrige los datos y registra una cuenta.
2. Inicia sesión, cambia la foto, vuelve al inicio y comprueba la cabecera. Cierra y vuelve a abrir para comprobar la persistencia.
3. Solicita un producto sin activar personalización; revisa y vuelve a editar. Cancela: no debe aparecer un pedido nuevo.
4. Crea otro, activa personalización, elige un color y filtra materiales. Confirma y revisa los valores desde Mis pedidos.
5. Edita el pedido pendiente. Entra como administrador y comprueba la bandeja y el resumen. Confirma el pedido: se reserva el stock una vez; el cliente ya no puede editarlo.
6. Publica una promoción como administrador. Vuelve a la cuenta cliente y revisa la bandeja. Marca como leídas y comprueba el contador al volver al inicio.
7. Con notificaciones denegadas, comprueba que los pedidos siguen funcionando. Con permiso concedido, prueba los avisos locales.
8. Cambia la contraseña de una cuenta de prueba y comprueba que la antigua no funciona tras volver a abrir la app.
