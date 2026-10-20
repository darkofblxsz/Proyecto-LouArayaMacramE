
package cl.louaraya.macrame

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject

private val VerdeCampana = Color(0xFF073D35)
private val FondoCampana = Color(0xFFFAF8F4)
private val VerdeSuave = Color(0xFFD7F4E2)
private val RojoCampana = Color(0xFFD01827)
private val RojoSuave = Color(0xFFFFDFDF)

private val fotosBase = listOf(
    "matrimonios",
    "festivales",
    "hogar",
    "viajes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCampanas(
    almacen: AlmacenLocal,
    onSalir: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preferencias = remember {
        context.getSharedPreferences(
            "imagenes_campanas",
            Context.MODE_PRIVATE
        )
    }

    var campanas by remember {
        mutableStateOf(almacen.campanas())
    }

    var imagenes by remember {
        mutableStateOf(
            JSONObject(
                preferencias.getString("imagenes", "{}") ?: "{}"
            )
        )
    }

    fun guardarImagenes(nuevas: JSONObject) {
        imagenes = nuevas
        preferencias.edit()
            .putString("imagenes", nuevas.toString())
            .apply()
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var mostrarEliminar by remember {
        mutableStateOf(false)
    }

    var indiceEditar by remember {
        mutableIntStateOf(-1)
    }

    var indiceEliminar by remember {
        mutableIntStateOf(-1)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    var activa by remember {
        mutableStateOf(true)
    }

    var imagenSeleccionada by remember {
        mutableStateOf("")
    }

    var errorFormulario by remember {
        mutableStateOf("")
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    fun abrirCrear() {
        indiceEditar = -1
        nombre = ""
        descripcion = ""
        activa = true
        imagenSeleccionada = "matrimonios"
        errorFormulario = ""
        mostrarFormulario = true
    }

    fun abrirEditar(index: Int) {
        val campana = campanas[index]
        indiceEditar = index
        nombre = campana.nombre
        descripcion = campana.descripcion
        activa = campana.activa
        imagenSeleccionada = imagenes.optString(
            campana.nombre,
            campana.nombre.lowercase()
        )
        errorFormulario = ""
        mostrarFormulario = true
    }

    fun cerrarFormulario() {
        scope.launch {
            sheetState.hide()
            mostrarFormulario = false
        }
    }

    val selectorImagen = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Algunos proveedores no permiten persistencia.
            }

            imagenSeleccionada = uri.toString()
        }
    }

    // ======================================
    // PANTALLA PRINCIPAL DE CAMPAÑAS
    // ======================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCampana)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "‹",
                fontSize = 32.sp,
                color = VerdeCampana,
                modifier = Modifier.clickable(onClick = onSalir)
            )

            Spacer(Modifier.width(22.dp))

            Text(
                text = "Campañas",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeCampana
            )
        }

        HorizontalDivider(
            color = Color(0xFFE7E4DF)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            itemsIndexed(campanas) { index, campana ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 186.dp)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FotoCampana(
                            referencia = imagenes.optString(
                                campana.nombre,
                                campana.nombre.lowercase()
                            ),
                            modifier = Modifier
                                .width(105.dp)
                                .height(160.dp)
                        )

                        // Dos zonas: información arriba y controles abajo.
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 160.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = campana.nombre,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp,
                                        lineHeight = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeCampana,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    EtiquetaEstado(campana.activa)
                                }
                                Text(
                                    text = campana.descripcion,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFF65716D),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    TextButton(
                                        onClick = { abrirEditar(index) },
                                        contentPadding = PaddingValues(horizontal = 0.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("✎ Editar", fontSize = 14.sp, color = VerdeCampana)
                                    }
                                    TextButton(
                                        onClick = {
                                            indiceEliminar = index
                                            mostrarEliminar = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 0.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("Eliminar", fontSize = 14.sp, color = RojoCampana)
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Switch(
                                        checked = campana.activa,
                                        onCheckedChange = { nuevoEstado ->
                                            campanas = campanas.toMutableList().also {
                                                it[index] = campana.copy(activa = nuevoEstado)
                                            }
                                            almacen.guardarCampanas(campanas)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedTrackColor = VerdeCampana
                                        )
                                    )
                                    Text(
                                        text = if (campana.activa) "Visible" else "Oculta",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { abrirCrear() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(55.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeCampana
            )
        ) {
            Text(
                text = "Crear campaña",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    // ======================================
    // PANEL DESLIZANTE ANIMADO
    // ======================================

    if (mostrarFormulario) {

        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
            },
            sheetState = sheetState,
            containerColor = FondoCampana,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp)
                        .width(38.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFADB8B8))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 18.dp)
            ) {

                Text(
                    text = if (indiceEditar == -1)
                        "Crear campaña"
                    else
                        "Editar campaña",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeCampana,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )

                Spacer(Modifier.height(18.dp))

                // El contenido puede desplazarse en
                // pantallas pequeñas o con teclado abierto.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    item {

                        CampoFormulario(
                            titulo = "Nombre de la campaña",
                            valor = nombre,
                            placeholder = "Ej: Matrimonios",
                            onCambio = {
                                nombre = it
                                errorFormulario = ""
                            }
                        )
                    }

                    item {

                        CampoFormulario(
                            titulo = "Descripción",
                            valor = descripcion,
                            placeholder = "Ej: Colección especial para un día único",
                            multilinea = true,
                            onCambio = {
                                descripcion = it
                                errorFormulario = ""
                            }
                        )
                    }

                    item {

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = "Imagen de la campaña",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF182B32)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                fotosBase.forEach { foto ->

                                    val seleccionada =
                                        imagenSeleccionada == foto

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(94.dp)
                                            .border(
                                                width = if (seleccionada)
                                                    2.dp
                                                else
                                                    0.dp,
                                                color = if (seleccionada)
                                                    VerdeCampana
                                                else
                                                    Color.Transparent,
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                            .padding(3.dp)
                                            .clickable {
                                                imagenSeleccionada = foto
                                            }
                                    ) {

                                        FotoCampana(
                                            referencia = foto,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    selectorImagen.launch(
                                        arrayOf("image/*")
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp),
                                shape = RoundedCornerShape(7.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    VerdeCampana
                                )
                            ) {

                                Text(
                                    text = "▣  Seleccionar imagen",
                                    color = VerdeCampana,
                                    fontSize = 13.sp
                                )
                            }

                            if (
                                imagenSeleccionada.startsWith("content://")
                            ) {

                                FotoCampana(
                                    referencia = imagenSeleccionada,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                )
                            }
                        }
                    }

                    item {

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = "Estado de la campaña",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF182B32)
                            )

                            FilaEstado(
                                titulo = "Activa",
                                subtitulo = "La campaña será visible",
                                seleccionada = activa,
                                color = VerdeCampana,
                                onClick = { activa = true }
                            )

                            FilaEstado(
                                titulo = "Inactiva",
                                subtitulo = "La campaña no será visible",
                                seleccionada = !activa,
                                color = RojoCampana,
                                onClick = { activa = false }
                            )
                        }
                    }

                    if (errorFormulario.isNotBlank()) {
                        item {
                            Text(
                                text = errorFormulario,
                                color = RojoCampana,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = {
                            cerrarFormulario()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(49.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {

                        Text(
                            text = "Cancelar",
                            color = Color(0xFF26343C)
                        )
                    }

                    Button(
                        onClick = {

                            val nuevoNombre = nombre.trim()
                            val nuevaDescripcion = descripcion.trim()

                            val duplicada = campanas
                                .withIndex()
                                .any { (index, item) ->
                                    index != indiceEditar &&
                                            item.nombre.equals(
                                                nuevoNombre,
                                                ignoreCase = true
                                            )
                                }

                            when {
                                nuevoNombre.isBlank() ||
                                        nuevaDescripcion.isBlank() -> {
                                    errorFormulario =
                                        "Completa nombre y descripción"
                                }

                                duplicada -> {
                                    errorFormulario =
                                        "Ya existe una campaña con ese nombre"
                                }

                                else -> {

                                    val anterior =
                                        if (indiceEditar >= 0)
                                            campanas[indiceEditar].nombre
                                        else
                                            null

                                    val nuevaCampana = Campana(
                                        nombre = nuevoNombre,
                                        descripcion = nuevaDescripcion,
                                        activa = activa
                                    )

                                    campanas = if (indiceEditar == -1) {
                                        campanas + nuevaCampana
                                    } else {
                                        campanas.toMutableList().also {
                                            it[indiceEditar] = nuevaCampana
                                        }
                                    }

                                    almacen.guardarCampanas(campanas)

                                    val nuevasImagenes = JSONObject(
                                        imagenes.toString()
                                    )

                                    if (anterior != null) {
                                        nuevasImagenes.remove(anterior)
                                    }

                                    nuevasImagenes.put(
                                        nuevoNombre,
                                        imagenSeleccionada
                                    )

                                    guardarImagenes(nuevasImagenes)

                                    cerrarFormulario()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(49.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeCampana
                        )
                    ) {

                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ======================================
    // CONFIRMACIÓN DE ELIMINACIÓN
    // ======================================

    if (mostrarEliminar && indiceEliminar in campanas.indices) {

        val campana = campanas[indiceEliminar]

        AlertDialog(
            onDismissRequest = {
                mostrarEliminar = false
            },
            title = {
                Text("Eliminar campaña")
            },
            text = {
                Text(
                    "¿Deseas eliminar \"${campana.nombre}\"? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val nuevasImagenes = JSONObject(
                            imagenes.toString()
                        )

                        nuevasImagenes.remove(campana.nombre)
                        guardarImagenes(nuevasImagenes)

                        campanas = campanas.filterIndexed {
                                index, _ -> index != indiceEliminar
                        }

                        almacen.guardarCampanas(campanas)
                        mostrarEliminar = false
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = RojoCampana
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarEliminar = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ======================================
// CAMPO DE TEXTO DEL FORMULARIO
// ======================================

@Composable
private fun CampoFormulario(
    titulo: String,
    valor: String,
    placeholder: String,
    multilinea: Boolean = false,
    onCambio: (String) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        Text(
            text = titulo,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF182B32)
        )

        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            },
            singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1,
            maxLines = if (multilinea) 4 else 1,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(7.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeCampana,
                unfocusedBorderColor = Color(0xFFC5C9CA)
            )
        )
    }
}

// ======================================
// OPCIÓN ACTIVA / INACTIVA
// ======================================

@Composable
private fun FilaEstado(
    titulo: String,
    subtitulo: String,
    seleccionada: Boolean,
    color: Color,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = seleccionada,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = color
            )
        )

        Spacer(Modifier.width(5.dp))

        Column {

            Text(
                text = titulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF192C32)
            )

            Text(
                text = subtitulo,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// ======================================
// ETIQUETA DE ESTADO
// ======================================

@Composable
private fun EtiquetaEstado(activa: Boolean) {

    Row(
        modifier = Modifier
            .background(
                if (activa) VerdeSuave else RojoSuave,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (activa) Color(0xFF008D53)
                    else RojoCampana,
                    RoundedCornerShape(50)
                )
        )

        Text(
            text = if (activa) "Activa" else "Inactiva",
            fontSize = 12.sp,
            color = if (activa) VerdeCampana
            else RojoCampana,
            fontWeight = FontWeight.Medium
        )
    }
}

// ======================================
// FOTOGRAFÍAS
// ======================================

@Composable
private fun FotoCampana(
    referencia: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val recurso = remember(referencia) {
        if (referencia in fotosBase) {
            context.resources.getIdentifier(
                referencia,
                "drawable",
                context.packageName
            )
        } else {
            0
        }
    }

    val bitmap = remember(referencia) {
        if (!referencia.startsWith("content://")) {
            null
        } else {
            try {
                val opciones = BitmapFactory.Options().apply {
                    inSampleSize = 4
                }

                context.contentResolver.openInputStream(
                    Uri.parse(referencia)
                )?.use {
                    BitmapFactory.decodeStream(
                        it,
                        null,
                        opciones
                    )
                }?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFE8D9C5)),
        contentAlignment = Alignment.Center
    ) {

        when {

            bitmap != null -> {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Imagen de campaña",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            recurso != 0 -> {
                Image(
                    painter = painterResource(recurso),
                    contentDescription = "Imagen de campaña",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Text(
                    text = "✦",
                    fontSize = 32.sp,
                    color = VerdeCampana
                )
            }
        }
    }
}

// ======================================
// PANTALLA PROVISIONAL DE USUARIO
// ======================================

@Composable
fun PantallaUsuarioVacia(
    onSalir: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        TextButton(onClick = onSalir) {
            Text("← Cerrar sesión")
        }
    }
}
