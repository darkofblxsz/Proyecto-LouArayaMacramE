# LouAraya: solicitudes, fotografías y QR

## Abrir el proyecto
1. Extraer todo el ZIP en una carpeta nueva.
2. Abrir la carpeta LouArayaMacrame desde Android Studio.
3. Esperar la sincronización de Gradle con conexión a Internet.
4. Conectar el teléfono con depuración USB, seleccionarlo y presionar Run.

Se mantienen las versiones de Gradle, SDK y Android Gradle Plugin del proyecto recibido. Se agregan ZXing Android Embedded 4.3.0 y Coil Compose 2.7.0, que Gradle debe descargar en la primera sincronización. No se incluye un APK compilado.

## Cuentas de demostración
| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@admin.com | Admin1234! |
| Cliente | usuario@gmail.com | Usuario1234! |

También se puede registrar un cliente desde la pantalla de registro.

## Crear una solicitud
1. Iniciar sesión como cliente.
2. Abrir un producto del catálogo y pulsar **Solicitar**.
3. Indicar cantidad, fecha requerida y un comentario opcional.
4. Pulsar **Crear solicitud**. Aparecerá en **Mis solicitudes**, en estado Pendiente.
5. El botón **Solicitudes** de la barra inferior permite consultar las solicitudes de esa cuenta.

Los productos sin stock no admiten solicitudes. Se comprueba nuevamente el stock al guardar y al confirmar. No se crean registros ficticios ni se reutilizan los ejemplos de la pantalla anterior.

## Gestionar solicitudes
1. En el mismo teléfono, entrar con la cuenta administradora.
2. Abrir **Ver solicitudes** en el dashboard.
3. Seleccionar **Actualizar estado**.

- Pendiente → Confirmado: reserva las unidades, descontándolas del stock.
- Pendiente → Cancelado: no modifica stock.
- Confirmado → Entregado: no vuelve a descontar unidades.
- Confirmado → Cancelado: devuelve las unidades si el producto todavía existe.
- Entregado y Cancelado son estados finales.

El estado y el stock se guardan juntos. No es un cobro ni una integración con pagos.

## Tomar y agregar fotos
1. Administrador → Productos → **+** o **Editar**.
2. Completar el formulario y bajar hasta **Fotografías**.
3. **Tomar foto** abre la aplicación de cámara. Aceptar el permiso cuando Android lo solicite.
4. **Elegir de la galería** permite seleccionar una o varias fotos, hasta cinco por producto.
5. Con **Portada** se elige la primera foto; con **Quitar** se retira del formulario.
6. Pulsar **Agregar** o **Guardar** para actualizar el producto.

Las fotos se copian al almacenamiento privado de la app: no dependen del permiso temporal de la galería. Se muestran en la gestión de productos y en el catálogo. El detalle permite recorrer las fotos adicionales. Si no se agregan fotos se conserva la imagen de categoría. Cada archivo debe ser una imagen de hasta 20 MB.

Si se cancela el formulario, el producto mantiene sus fotos anteriores. La copia privada de una foto importada puede permanecer en almacenamiento aunque se cancele o quite del formulario; no se borra el original de la galería. Desinstalar o borrar los datos de la app elimina estos datos locales.

## Lector QR
- **Escanear QR** está disponible en el catálogo y en la gestión de productos.
- En el catálogo abre el detalle del producto encontrado.
- En administración abre su edición.
- **Ver QR** muestra un código por producto. Ejemplo de contenido: `LOUARAYA:MAC-001`.
- También admite un QR cuyo texto sea directamente `MAC-001`.
- Un QR desconocido muestra un aviso; no abre páginas web ni ejecuta acciones externas.

Para probarlo, mostrar el QR en otra pantalla y escanearlo con el teléfono. Ambos catálogos deben contener el mismo código. Escanear el código no transfiere el producto ni sus fotos entre dispositivos.

## Alcance del almacenamiento
Es una aplicación local: cuentas, productos, fotos, campañas y solicitudes se guardan en el teléfono. Un administrador en otro teléfono no recibirá las solicitudes. Para eso se necesita una API o base de datos remota con autenticación. El catálogo sigue accesible sin iniciar sesión; para crear y consultar solicitudes propias se requiere una cuenta.

Se conservan la gestión de productos del archivo recibido, las pantallas y promociones de la versión anterior y el ícono LA con mandala blanco sobre verde esmeralda.

## Comprobaciones y pruebas pendientes
Se revisaron los XML, referencias de recursos, integración entre pantallas y la integridad del ZIP. No fue posible compilar ni ejecutar aquí: el entorno no tiene SDK de Android ni acceso para descargar Gradle. Las pruebas siguientes deben ejecutarse en Android Studio y en el teléfono.

Pruebas automatizadas incluidas en `app/src/androidTest/java/cl/louaraya/macrame/SolicitudesIntegrationTest.kt`: reserva y devolución del stock, entrega sin doble descuento, validación de solicitudes, persistencia de fotos y reconocimiento de QR. Se ejecutan con `gradlew.bat connectedDebugAndroidTest` con un dispositivo conectado. Usan preferencias separadas para no modificar los datos normales de la app. No se han ejecutado en este entorno.

Prueba manual recomendada:
- Crear y editar un producto con foto de cámara y otra de galería; guardar y reabrir la app.
- Cancelar cámara, denegar permiso, volver a permitirlo y repetir la captura.
- Rotar el teléfono con el formulario abierto y comprobar que conserva sus datos.
- Crear solicitudes desde dos cuentas y comprobar que cada cliente solo ve las suyas.
- Confirmar, entregar y cancelar solicitudes, verificando el stock.
- Escanear un QR válido, uno desconocido y salir del lector con Atrás.
