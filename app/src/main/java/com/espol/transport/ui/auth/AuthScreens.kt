package com.espol.transport.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.espol.transport.ui.components.AppScreen
import com.espol.transport.ui.components.AppTextField
import com.espol.transport.ui.components.InlineMessage
import com.espol.transport.ui.components.PasswordTextField
import com.espol.transport.ui.components.PrimaryButton
import com.espol.transport.ui.components.ScreenPadding
import com.espol.transport.ui.components.SecondaryButton
import com.espol.transport.ui.components.SectionHeader
import com.espol.transport.ui.util.PASSWORD_HINT
import com.espol.transport.ui.util.RECOVERY_PHRASE_HINT
import com.espol.transport.ui.util.isStrongPassword
import com.espol.transport.ui.util.isValidRecoveryPhrase
import com.espol.transport.ui.util.toUserMessage
import com.espol.transport.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = ScreenPadding, vertical = 24.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("ESPOL\nTransporte", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Transporte compartido hacia ESPOL",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1.2f))
        PrimaryButton("Iniciar sesión", onClick = onLogin)
        Spacer(Modifier.height(12.dp))
        SecondaryButton("Crear una cuenta", onClick = onRegister)
    }
}

/** Fila seleccionable grande: icono, título, descripción y chevron. */
@Composable
private fun ChoiceRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier
                .clickable(role = Role.Button, onClick = onClick)
                .heightIn(min = 72.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun RolePickScreen(
    title: String,
    question: String,
    passengerTitle: String,
    onBack: () -> Unit,
    onPassenger: () -> Unit,
    onDriver: () -> Unit,
) {
    AppScreen(title = title, onBack = onBack) {
        Text(
            question,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ChoiceRow(Icons.Filled.School, passengerTitle, "Con tu correo @espol.edu.ec", onPassenger)
        ChoiceRow(Icons.Filled.DirectionsCar, "Conductor", "Publica tu horario y lleva pasajeros", onDriver)
    }
}

@Composable
fun LoginRolePickScreen(onBack: () -> Unit, onPassenger: () -> Unit, onDriver: () -> Unit) =
    RolePickScreen("Iniciar sesión", "¿Eres pasajero o conductor?", "Pasajero (estudiante)", onBack, onPassenger, onDriver)

@Composable
fun RegisterRolePickScreen(onBack: () -> Unit, onPassenger: () -> Unit, onDriver: () -> Unit) =
    RolePickScreen("Crear cuenta", "¿Qué tipo de cuenta quieres crear?", "Estudiante (pasajero)", onBack, onPassenger, onDriver)

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    expectedRole: String?,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var stayLoggedIn by rememberSaveable { mutableStateOf(true) }

    AppScreen(title = "Iniciar sesión", onBack = onBack) {
        when (expectedRole) {
            "student" -> "Como pasajero"
            "driver" -> "Como conductor"
            else -> null
        }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AppTextField(
            value = email,
            onValueChange = { authVm.clearError(); email = it },
            label = "Correo",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        PasswordTextField(
            value = password,
            onValueChange = { authVm.clearError(); password = it.filter { ch -> !ch.isWhitespace() } },
            label = "Contraseña",
            supportingText = PASSWORD_HINT,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Switch) { stayLoggedIn = !stayLoggedIn }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Mantener sesión iniciada", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = stayLoggedIn, onCheckedChange = null)
        }
        InlineMessage(authVm.errorMessage)
        PrimaryButton(
            text = "Entrar",
            loading = authVm.busy,
            enabled = email.isNotBlank() && isStrongPassword(password),
            onClick = {
                authVm.login(email, password, stayLoggedIn) {
                    scope.launch {
                        try {
                            val me = authVm.fetchMe()
                            if (expectedRole != null && me.role != expectedRole) {
                                authVm.logout { }
                                authVm.errorMessage = when (expectedRole) {
                                    "student" -> "Esta cuenta no es de pasajero. Elige «Conductor» al iniciar sesión."
                                    else -> "Esta cuenta no es de conductor. Elige «Pasajero» al iniciar sesión."
                                }
                            } else {
                                onLoggedIn()
                            }
                        } catch (e: Exception) {
                            authVm.errorMessage = e.toUserMessage()
                        }
                    }
                }
            },
        )
        TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Olvidaste tu contraseña?")
        }
    }
}

/** Contraseña + confirmación + palabra clave: compartido por ambos registros. */
@Composable
private fun AccountSecurityFields(
    authVm: AuthViewModel,
    password: String,
    onPassword: (String) -> Unit,
    confirmPassword: String,
    onConfirmPassword: (String) -> Unit,
    recoveryPhrase: String,
    onRecoveryPhrase: (String) -> Unit,
) {
    val mismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    PasswordTextField(
        value = password,
        onValueChange = { authVm.clearError(); onPassword(it.filter { ch -> !ch.isWhitespace() }) },
        label = "Contraseña",
        supportingText = PASSWORD_HINT,
    )
    PasswordTextField(
        value = confirmPassword,
        onValueChange = { authVm.clearError(); onConfirmPassword(it.filter { ch -> !ch.isWhitespace() }) },
        label = "Vuelve a escribir tu contraseña",
        isError = mismatch,
        supportingText = if (mismatch) "Las contraseñas no coinciden." else null,
    )
    SectionHeader("Recuperación")
    AppTextField(
        value = recoveryPhrase,
        onValueChange = { authVm.clearError(); onRecoveryPhrase(it.filter { ch -> !ch.isWhitespace() }) },
        label = "Palabra clave de recuperación",
        supportingText = RECOVERY_PHRASE_HINT,
    )
}

@Composable
fun PassengerRegisterScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }
    val passwordsMatch = password == confirmPassword

    AppScreen(title = "Registro pasajero", onBack = onBack) {
        Text(
            "Registro solo con correo institucional @espol.edu.ec. Un correo = una cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionHeader("Datos personales")
        AppTextField(first, { authVm.clearError(); first = it }, label = "Nombre")
        AppTextField(last, { authVm.clearError(); last = it }, label = "Apellido")
        AppTextField(
            phone, { authVm.clearError(); phone = it }, label = "Celular",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        SectionHeader("Cuenta")
        AppTextField(
            email, { authVm.clearError(); email = it.trim().lowercase() }, label = "Correo @espol.edu.ec",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        AccountSecurityFields(
            authVm, password, { password = it }, confirmPassword, { confirmPassword = it },
            recoveryPhrase, { recoveryPhrase = it },
        )
        InlineMessage(authVm.errorMessage)
        PrimaryButton(
            text = "Crear cuenta",
            loading = authVm.busy,
            onClick = {
                authVm.registerPassenger(first, last, phone, email, password, recoveryPhrase, onRegistered)
            },
            enabled = first.isNotBlank() && last.isNotBlank() && phone.length >= 8 &&
                isStrongPassword(password) && passwordsMatch && confirmPassword.isNotBlank() &&
                isValidRecoveryPhrase(recoveryPhrase) &&
                email.endsWith("@espol.edu.ec", ignoreCase = true),
        )
    }
}

@Composable
fun DriverRegisterScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    val passwordsMatch = password == confirmPassword

    AppScreen(title = "Registro conductor", onBack = onBack) {
        Text(
            "Completa tus datos y los del vehículo. Un correo = una cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionHeader("Datos personales")
        AppTextField(first, { authVm.clearError(); first = it }, label = "Nombre")
        AppTextField(last, { authVm.clearError(); last = it }, label = "Apellido")
        AppTextField(
            phone, { authVm.clearError(); phone = it }, label = "Celular",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        SectionHeader("Vehículo")
        AppTextField(plate, { authVm.clearError(); plate = it }, label = "Placa")
        AppTextField(brand, { authVm.clearError(); brand = it }, label = "Marca del auto")
        AppTextField(model, { authVm.clearError(); model = it }, label = "Modelo")
        SectionHeader("Cuenta")
        AppTextField(
            email, { authVm.clearError(); email = it }, label = "Correo (inicio de sesión)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        AccountSecurityFields(
            authVm, password, { password = it }, confirmPassword, { confirmPassword = it },
            recoveryPhrase, { recoveryPhrase = it },
        )
        InlineMessage(authVm.errorMessage)
        PrimaryButton(
            text = "Registrar conductor",
            loading = authVm.busy,
            onClick = {
                authVm.registerDriver(first, last, phone, email, password, recoveryPhrase, plate, brand, model, onRegistered)
            },
            enabled = listOf(first, last, phone, email, password, confirmPassword, recoveryPhrase, plate, brand, model).all { it.isNotBlank() } &&
                isStrongPassword(password) && passwordsMatch && isValidRecoveryPhrase(recoveryPhrase) && phone.length >= 8,
        )
    }
}

@Composable
fun ForgotPasswordScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }

    AppScreen(title = "Recuperar contraseña", onBack = onBack) {
        Text(
            "Ingresa el correo de tu cuenta y la palabra clave que definiste al registrarte.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppTextField(
            value = email,
            onValueChange = { authVm.clearError(); email = it },
            label = "Correo",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        AppTextField(
            value = recoveryPhrase,
            onValueChange = { authVm.clearError(); recoveryPhrase = it.filter { ch -> !ch.isWhitespace() } },
            label = "Palabra clave de recuperación",
            supportingText = "Debe coincidir con la que guardaste al crear la cuenta.",
        )
        InlineMessage(authVm.errorMessage)
        PrimaryButton(
            text = "Continuar",
            loading = authVm.busy,
            onClick = { authVm.verifyRecoveryPhrase(email, recoveryPhrase, onVerified) },
            enabled = email.isNotBlank() && isValidRecoveryPhrase(recoveryPhrase),
        )
    }
}

@Composable
fun ResetPasswordScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onReset: () -> Unit,
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val mismatch = confirmPassword.isNotEmpty() && newPassword != confirmPassword

    AppScreen(title = "Nueva contraseña", onBack = onBack) {
        Text(
            "Elige una nueva contraseña para tu cuenta.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PasswordTextField(
            value = newPassword,
            onValueChange = { authVm.clearError(); newPassword = it.filter { ch -> !ch.isWhitespace() } },
            label = "Nueva contraseña",
            supportingText = PASSWORD_HINT,
        )
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { authVm.clearError(); confirmPassword = it.filter { ch -> !ch.isWhitespace() } },
            label = "Confirmar contraseña",
            isError = mismatch,
            supportingText = if (mismatch) "Las contraseñas no coinciden." else null,
        )
        InlineMessage(authVm.errorMessage)
        PrimaryButton(
            text = "Guardar contraseña",
            loading = authVm.busy,
            onClick = { authVm.resetPassword(newPassword, onReset) },
            enabled = isStrongPassword(newPassword) && newPassword == confirmPassword,
        )
    }
}
