package com.espol.transport.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.espol.transport.ui.components.PasswordTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espol.transport.AppContainer
import com.espol.transport.BuildConfig
import com.espol.transport.data.model.UserMe
import com.espol.transport.ui.util.PASSWORD_HINT
import com.espol.transport.ui.util.RECOVERY_PHRASE_HINT
import com.espol.transport.ui.util.isStrongPassword
import com.espol.transport.ui.util.isValidRecoveryPhrase
import com.espol.transport.ui.util.toUserMessage
import com.espol.transport.ui.driver.DriverScheduleScreen
import com.espol.transport.ui.inbox.InboxScreen
import com.espol.transport.ui.passenger.PassengerResultsScreen
import com.espol.transport.ui.passenger.PassengerSearchScreen
import com.espol.transport.ui.viewmodel.AuthViewModel
import com.espol.transport.ui.viewmodel.TransportViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val Bootstrap = "bootstrap"
    const val Welcome = "welcome"
    const val LoginRolePick = "login_role"
    const val RegisterRolePick = "register_role"
    const val Login = "login"
    const val ForgotPassword = "forgot_password"
    const val ResetPassword = "reset_password"
    const val PassengerOtp = "passenger_otp"
    const val PassengerRegister = "passenger_register"
    const val DriverRegister = "driver_register"
    const val RoleGate = "role_gate"
    const val Inbox = "inbox"
    const val PassengerSearch = "passenger_search"
    const val PassengerResults = "passenger_results"
    const val DriverSchedule = "driver_schedule"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(container: AppContainer) {
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val authVmFactory = remember(container) { AuthViewModel.Factory(container) }
    val transportVmFactory = remember(container) { TransportViewModel.Factory(container) }
    val authVm: AuthViewModel = viewModel(factory = authVmFactory)
    val transportVm: TransportViewModel = viewModel(factory = transportVmFactory)

    var pendingPassengerEmail by rememberSaveable { mutableStateOf("") }
    var pendingPassengerOtp by rememberSaveable { mutableStateOf("") }
    var loginExpectedRole by rememberSaveable { mutableStateOf<String?>(null) }
    var isDriverInbox by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.Bootstrap,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.Bootstrap) {
                SessionBootstrapScreen(
                    authVm = authVm,
                    onContinue = { destination ->
                        nav.navigate(destination) {
                            popUpTo(Routes.Bootstrap) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.Welcome) {
                WelcomeScreen(
                    onLogin = { nav.navigate(Routes.LoginRolePick) },
                    onRegister = { nav.navigate(Routes.RegisterRolePick) },
                )
            }
            composable(Routes.LoginRolePick) {
                LoginRolePickScreen(
                    onBack = { nav.popBackStack() },
                    onPassenger = {
                        loginExpectedRole = "student"
                        nav.navigate(Routes.Login)
                    },
                    onDriver = {
                        loginExpectedRole = "driver"
                        nav.navigate(Routes.Login)
                    },
                )
            }
            composable(Routes.RegisterRolePick) {
                RegisterRolePickScreen(
                    onBack = { nav.popBackStack() },
                    onPassenger = { nav.navigate(Routes.PassengerOtp) },
                    onDriver = { nav.navigate(Routes.DriverRegister) },
                )
            }
            composable(Routes.Login) {
                LoginScreen(
                    authVm = authVm,
                    expectedRole = loginExpectedRole,
                    onBack = { nav.popBackStack() },
                    onForgotPassword = { nav.navigate(Routes.ForgotPassword) },
                    onLoggedIn = {
                        nav.navigate(Routes.RoleGate) {
                            popUpTo(Routes.Welcome) { inclusive = false }
                        }
                    },
                )
            }
            composable(Routes.ForgotPassword) {
                ForgotPasswordScreen(
                    authVm = authVm,
                    onBack = { nav.popBackStack() },
                    onVerified = { nav.navigate(Routes.ResetPassword) },
                )
            }
            composable(Routes.ResetPassword) {
                ResetPasswordScreen(
                    authVm = authVm,
                    onBack = { nav.popBackStack() },
                    onReset = {
                        scope.launch { snack.showSnackbar("Contraseña actualizada. Inicia sesión.") }
                        nav.navigate(Routes.Login) {
                            popUpTo(Routes.Welcome) { inclusive = false }
                        }
                    },
                )
            }
            composable(Routes.PassengerOtp) {
                PassengerOtpScreen(
                    authVm = authVm,
                    onBack = { nav.popBackStack() },
                    onCodeSent = { email, devCode ->
                        pendingPassengerEmail = email
                        pendingPassengerOtp = devCode.orEmpty()
                        nav.navigate(Routes.PassengerRegister)
                    },
                )
            }
            composable(Routes.PassengerRegister) {
                PassengerRegisterScreen(
                    authVm = authVm,
                    initialEmail = pendingPassengerEmail,
                    initialOtp = pendingPassengerOtp,
                    onBack = { nav.popBackStack() },
                    onRegistered = {
                        scope.launch {
                            snack.showSnackbar("Cuenta creada. Inicia sesión con tu correo @espol.edu.ec")
                        }
                        nav.navigate(Routes.Login) {
                            popUpTo(Routes.Welcome) { inclusive = false }
                        }
                    },
                )
            }
            composable(Routes.DriverRegister) {
                DriverRegisterScreen(
                    authVm = authVm,
                    onBack = { nav.popBackStack() },
                    onRegistered = {
                        scope.launch { snack.showSnackbar("Conductor registrado. Inicia sesión.") }
                        nav.navigate(Routes.Login) {
                            popUpTo(Routes.Welcome) { inclusive = false }
                        }
                    },
                )
            }
            composable(Routes.RoleGate) {
                RoleGateScreen(
                    authVm = authVm,
                    transportVm = transportVm,
                    onPassenger = {
                        isDriverInbox = false
                        nav.navigate(Routes.Inbox) {
                            popUpTo(Routes.RoleGate) { inclusive = true }
                        }
                    },
                    onDriver = {
                        isDriverInbox = true
                        nav.navigate(Routes.Inbox) {
                            popUpTo(Routes.RoleGate) { inclusive = true }
                        }
                    },
                    onError = { nav.popBackStack() },
                )
            }
            composable(Routes.Inbox) {
                LaunchedEffect(Unit) {
                    if (transportVm.currentUser == null) {
                        try {
                            transportVm.currentUser = authVm.fetchMe()
                        } catch (_: Exception) { }
                    }
                    transportVm.loadInbox()
                }
                val name = transportVm.currentUser?.full_name ?: "Usuario"
                InboxScreen(
                    transportVm = transportVm,
                    userName = name,
                    isDriver = isDriverInbox,
                    onPrimaryAction = {
                        if (isDriverInbox) nav.navigate(Routes.DriverSchedule)
                        else nav.navigate(Routes.PassengerSearch)
                    },
                    onLogout = { logout(authVm, nav) },
                )
            }
            composable(Routes.PassengerSearch) {
                PassengerSearchScreen(
                    transportVm = transportVm,
                    onBack = { nav.popBackStack() },
                    onResults = {
                        nav.navigate(Routes.PassengerResults)
                    },
                )
            }
            composable(Routes.PassengerResults) {
                val response = transportVm.lastSearchResponse
                if (response != null) {
                    PassengerResultsScreen(
                        response = response,
                        homeSector = transportVm.lastSearchHomeSector,
                        transportVm = transportVm,
                        onBack = { nav.popBackStack() },
                        onBooked = { msg ->
                            if (msg.isNotBlank()) {
                                scope.launch { snack.showSnackbar(msg) }
                                nav.popBackStack(Routes.PassengerSearch, inclusive = true)
                                transportVm.loadInbox()
                            }
                        },
                    )
                } else {
                    LaunchedEffect(Unit) { nav.popBackStack() }
                }
            }
            composable(Routes.DriverSchedule) {
                val name = transportVm.currentUser?.full_name ?: "Conductor"
                DriverScheduleScreen(
                    transportVm = transportVm,
                    userName = name,
                    onBack = { nav.popBackStack() },
                    onLogout = { logout(authVm, nav) },
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ESPOL Transporte", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Transporte compartido hacia ESPOL",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Crear una cuenta")
        }
    }
}

@Composable
private fun SessionBootstrapScreen(
    authVm: AuthViewModel,
    onContinue: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        val dest = if (authVm.hasPersistedSession()) Routes.RoleGate else Routes.Welcome
        onContinue(dest)
    }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginRolePickScreen(
    onBack: () -> Unit,
    onPassenger: () -> Unit,
    onDriver: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text("¿Eres pasajero o conductor?", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onPassenger, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pasajero (estudiante)")
                }
            }
            Button(onClick = onDriver, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Conductor")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterRolePickScreen(
    onBack: () -> Unit,
    onPassenger: () -> Unit,
    onDriver: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text("¿Qué tipo de cuenta quieres crear?", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onPassenger, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Estudiante (pasajero)")
                }
            }
            Button(onClick = onDriver, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Conductor")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            authVm.errorMessage?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = email,
                onValueChange = { authVm.clearError(); email = it },
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            PasswordTextField(
                value = password,
                onValueChange = { authVm.clearError(); password = it.filter { ch -> !ch.isWhitespace() } },
                label = "Contraseña",
                supportingText = PASSWORD_HINT,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(checked = stayLoggedIn, onCheckedChange = { stayLoggedIn = it })
                Text("Mantener sesión iniciada")
            }
            TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                Text("¿Olvidaste tu contraseña?")
            }
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
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
                    enabled = email.isNotBlank() && isStrongPassword(password),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Entrar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerOtpScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onCodeSent: (email: String, devCode: String?) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificación ESPOL") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Usa tu correo institucional (@espol.edu.ec). Te enviaremos un código de 6 dígitos.",
                style = MaterialTheme.typography.bodyLarge,
            )
            authVm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(
                value = email,
                onValueChange = { authVm.clearError(); email = it },
                label = { Text("Correo @espol.edu.ec") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (!email.endsWith("@espol.edu.ec", ignoreCase = true)) {
                            return@Button
                        }
                        authVm.sendPassengerOtp(email) { devCode ->
                            onCodeSent(email.trim().lowercase(), devCode)
                        }
                    },
                    enabled = email.endsWith("@espol.edu.ec", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Enviar código")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerRegisterScreen(
    authVm: AuthViewModel,
    initialEmail: String,
    initialOtp: String,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialEmail, initialOtp) {
        if (initialEmail.isNotBlank()) {
            email = initialEmail
        }
        if (initialOtp.isNotBlank()) {
            otp = initialOtp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro pasajero") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            authVm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (BuildConfig.DEBUG && initialOtp.isNotBlank()) {
                Text(
                    "Código de prueba aplicado (solo compilación debug). En producción revisa tu correo @espol.edu.ec.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            OutlinedTextField(
                value = first,
                onValueChange = { authVm.clearError(); first = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = last,
                onValueChange = { authVm.clearError(); last = it },
                label = { Text("Apellido") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { authVm.clearError(); phone = it },
                label = { Text("Celular") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { },
                readOnly = true,
                label = { Text("Correo institucional") },
                modifier = Modifier.fillMaxWidth(),
            )
            PasswordTextField(
                value = password,
                onValueChange = { authVm.clearError(); password = it.filter { ch -> !ch.isWhitespace() } },
                label = "Contraseña",
                supportingText = PASSWORD_HINT,
            )
            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = { authVm.clearError(); recoveryPhrase = it.filter { ch -> !ch.isWhitespace() } },
                label = { Text("Palabra clave de recuperación") },
                supportingText = { Text(RECOVERY_PHRASE_HINT) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it.filter { ch -> ch.isDigit() } },
                label = { Text("Código de 6 dígitos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        authVm.registerPassenger(first, last, phone, email, password, recoveryPhrase, otp, onRegistered)
                    },
                    enabled = first.isNotBlank() && last.isNotBlank() && phone.length >= 8 &&
                        isStrongPassword(password) && isValidRecoveryPhrase(recoveryPhrase) &&
                        otp.length == 6 && email.endsWith("@espol.edu.ec", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverRegisterScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro conductor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Completa tus datos y los del vehículo. Podrás publicar rutas después de iniciar sesión.",
                style = MaterialTheme.typography.bodyMedium,
            )
            authVm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(first, { authVm.clearError(); first = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(last, { authVm.clearError(); last = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = phone,
                onValueChange = { authVm.clearError(); phone = it },
                label = { Text("Celular") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { authVm.clearError(); email = it },
                label = { Text("Correo (inicio de sesión)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            PasswordTextField(
                value = password,
                onValueChange = { authVm.clearError(); password = it.filter { ch -> !ch.isWhitespace() } },
                label = "Contraseña",
                supportingText = PASSWORD_HINT,
            )
            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = { authVm.clearError(); recoveryPhrase = it.filter { ch -> !ch.isWhitespace() } },
                label = { Text("Palabra clave de recuperación") },
                supportingText = { Text(RECOVERY_PHRASE_HINT) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(plate, { authVm.clearError(); plate = it }, label = { Text("Placa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(brand, { authVm.clearError(); brand = it }, label = { Text("Marca del auto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(model, { authVm.clearError(); model = it }, label = { Text("Modelo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        authVm.registerDriver(first, last, phone, email, password, recoveryPhrase, plate, brand, model, onRegistered)
                    },
                    enabled = listOf(first, last, phone, email, password, recoveryPhrase, plate, brand, model).all { it.isNotBlank() } &&
                        isStrongPassword(password) && isValidRecoveryPhrase(recoveryPhrase) && phone.length >= 8,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Registrar conductor")
                }
            }
        }
    }
}

@Composable
private fun RoleGateScreen(
    authVm: AuthViewModel,
    transportVm: TransportViewModel,
    onPassenger: () -> Unit,
    onDriver: () -> Unit,
    onError: () -> Unit,
) {
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val me = authVm.fetchMe()
            transportVm.currentUser = me
            when (me.role) {
                "student" -> onPassenger()
                "driver" -> onDriver()
                else -> loadError = "Rol de usuario no reconocido."
            }
        } catch (e: Exception) {
            authVm.logout { }
            loadError = e.toUserMessage()
        }
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loadError != null) {
            Text(loadError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
            Button(onClick = onError) { Text("Ir a inicio de sesión") }
        } else {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var recoveryPhrase by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Ingresa el correo de tu cuenta y la palabra clave que definiste al registrarte.",
                style = MaterialTheme.typography.bodyLarge,
            )
            authVm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(
                value = email,
                onValueChange = { authVm.clearError(); email = it },
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = { authVm.clearError(); recoveryPhrase = it.filter { ch -> !ch.isWhitespace() } },
                label = { Text("Palabra clave de recuperación") },
                supportingText = { Text("Debe coincidir con la que guardaste al crear la cuenta.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { authVm.verifyRecoveryPhrase(email, recoveryPhrase, onVerified) },
                    enabled = email.isNotBlank() && isValidRecoveryPhrase(recoveryPhrase),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResetPasswordScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onReset: () -> Unit,
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val passwordsMatch = newPassword == confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Elige una nueva contraseña para tu cuenta.", style = MaterialTheme.typography.bodyLarge)
            authVm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
            )
            if (!passwordsMatch && confirmPassword.isNotEmpty()) {
                Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error)
            }
            if (authVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { authVm.resetPassword(newPassword, onReset) },
                    enabled = isStrongPassword(newPassword) && passwordsMatch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Guardar contraseña")
                }
            }
        }
    }
}

private fun logout(authVm: AuthViewModel, nav: androidx.navigation.NavHostController) {
    authVm.logout {
        nav.navigate(Routes.Welcome) {
            popUpTo(Routes.Welcome) { inclusive = true }
            launchSingleTop = true
        }
    }
}
