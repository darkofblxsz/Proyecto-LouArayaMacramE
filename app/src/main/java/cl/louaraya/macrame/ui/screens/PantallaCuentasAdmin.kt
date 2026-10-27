package cl.louaraya.macrame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.model.CuentaResumen
import cl.louaraya.macrame.viewmodel.TiendaViewModel

@Composable
fun PantallaCuentasAdmin(vm: TiendaViewModel, crear: Boolean, volver: () -> Unit, nuevoAdmin: () -> Unit) {
    var cuentas by remember { mutableStateOf(vm.cuentasAdmin()) }
    var buscar by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var editar by remember { mutableStateOf<CuentaResumen?>(null) }
    var eliminar by remember { mutableStateOf<CuentaResumen?>(null) }
    var error by remember { mutableStateOf("") }
    if (crear) {
        FormularioCuentaAdmin(vm, null, volver) { volver() }
        return
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = volver) { Text("‹ Volver al panel") }
        Text("Administrar cuentas", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = nuevoAdmin) { Icon(Icons.Outlined.PersonAdd, null); Text(" Crear administrador") }
        OutlinedTextField(buscar, { buscar = it }, label = { Text("Buscar por nombre, usuario o correo") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Clientes", "Admins").forEach { opcion -> FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion) }) }
        }
        val visibles = cuentas.filter { (filtro == "Todos" || it.rol == (if (filtro == "Admins") "ADMIN" else "USER")) &&
            (it.nombre.contains(buscar, true) || it.correo.contains(buscar, true) || it.usuario.contains(buscar, true)) }
        Text("${visibles.size} cuentas")
        if (visibles.isEmpty()) Text("No hay cuentas que coincidan con la búsqueda.")
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibles, key = { it.correo }) { cuenta ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${cuenta.nombre} ${cuenta.apellido}", style = MaterialTheme.typography.titleMedium)
                        Text(cuenta.correo)
                        Text("${cuenta.usuario} · ${if (cuenta.rol == "ADMIN") "Administrador" else "Cliente"}")
                        Row {
                            TextButton(onClick = { editar = cuenta }) { Icon(Icons.Outlined.Edit, null); Text(" Editar") }
                            TextButton(enabled = cuenta.correo != vm.correoSesion, onClick = { eliminar = cuenta; error = "" }) { Icon(Icons.Outlined.Delete, null); Text(" Eliminar") }
                        }
                    }
                }
            }
        }
    }
    editar?.let { cuenta ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { editar = null }) {
            Surface(shape = MaterialTheme.shapes.large) {
                FormularioCuentaAdmin(vm, cuenta, { editar = null }) { cuentas = vm.cuentasAdmin(); editar = null }
            }
        }
    }
    eliminar?.let { cuenta ->
        AlertDialog(onDismissRequest = { eliminar = null }, title = { Text("¿Eliminar esta cuenta?") },
            text = { Column { Text("${cuenta.nombre} · ${cuenta.correo}\nPerderá el acceso. Los pedidos cerrados se conservan en el historial."); if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error) } },
            confirmButton = { TextButton(onClick = { val resultado = vm.eliminarCuentaAdmin(cuenta.correo); if (resultado == null) { cuentas = vm.cuentasAdmin(); eliminar = null } else error = resultado }) { Text("Eliminar cuenta") } },
            dismissButton = { TextButton(onClick = { eliminar = null }) { Text("Cancelar") } })
    }
}

@Composable
private fun FormularioCuentaAdmin(vm: TiendaViewModel, cuenta: CuentaResumen?, volver: () -> Unit, guardada: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf(cuenta?.correo ?: "") }
    var usuario by rememberSaveable { mutableStateOf(cuenta?.usuario ?: "") }
    var nombre by rememberSaveable { mutableStateOf(cuenta?.nombre ?: "") }
    var apellido by rememberSaveable { mutableStateOf(cuenta?.apellido ?: "") }
    var telefono by rememberSaveable { mutableStateOf(cuenta?.telefono ?: "") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = volver) { Text("‹ Volver") }
        Text(if (cuenta == null) "Crear administrador" else "Editar cuenta", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(correo, { correo = it }, readOnly = cuenta != null, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (cuenta != null) Text("El correo se conserva para mantener su historial de pedidos.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(usuario, { usuario = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(apellido, { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(clave, { clave = it }, label = { Text(if (cuenta == null) "Contraseña (mínimo 8)" else "Nueva contraseña (opcional)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(confirmar, { confirmar = it }, label = { Text("Confirmar contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        Button(onClick = {
            val resultado = if (clave != confirmar) "Las contraseñas no coinciden." else vm.guardarCuentaAdmin(cuenta?.correo, correo, usuario, nombre, apellido, telefono, clave)
            if (resultado == null) guardada() else error = resultado
        }, modifier = Modifier.fillMaxWidth()) { Text(if (cuenta == null) "Crear administrador" else "Guardar cambios") }
    }
}
