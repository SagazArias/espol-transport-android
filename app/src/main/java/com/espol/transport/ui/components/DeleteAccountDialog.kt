package com.espol.transport.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    busy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Eliminar cuenta") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Se borrarán tu perfil, horarios, viajes agendados y todos los datos asociados. " +
                        "Esta acción no se puede deshacer. El mismo correo podrá usarse de nuevo para una cuenta nueva.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                PasswordTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Confirma con tu contraseña",
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = password.isNotBlank() && !busy) {
                Text("Eliminar definitivamente", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") }
        },
    )
}
