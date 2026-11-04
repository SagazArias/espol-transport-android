package com.espol.transport.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espol.transport.AppContainer
import com.espol.transport.ui.auth.DriverRegisterScreen
import com.espol.transport.ui.auth.ForgotPasswordScreen
import com.espol.transport.ui.auth.LoginRolePickScreen
import com.espol.transport.ui.auth.LoginScreen
import com.espol.transport.ui.auth.PassengerRegisterScreen
import com.espol.transport.ui.auth.RegisterRolePickScreen
import com.espol.transport.ui.auth.ResetPasswordScreen
import com.espol.transport.ui.auth.WelcomeScreen
import com.espol.transport.ui.components.InlineMessage
import com.espol.transport.ui.components.PrimaryButton
import com.espol.transport.ui.components.ScreenPadding
import com.espol.transport.ui.driver.DriverScheduleScreen
import com.espol.transport.ui.inbox.InboxScreen
import com.espol.transport.ui.passenger.PassengerResultsScreen
import com.espol.transport.ui.passenger.PassengerSearchScreen
import com.espol.transport.ui.util.toUserMessage
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
    const val PassengerRegister = "passenger_register"
    const val DriverRegister = "driver_register"
    const val RoleGate = "role_gate"
    const val Inbox = "inbox"
    const val PassengerSearch = "passenger_search"
    const val PassengerResults = "passenger_results"
    const val DriverSchedule = "driver_schedule"
}

@Composable
fun AppNav(container: AppContainer) {
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val authVmFactory = remember(container) { AuthViewModel.Factory(container) }
    val transportVmFactory = remember(container) { TransportViewModel.Factory(container) }
    val authVm: AuthViewModel = viewModel(factory = authVmFactory)
    val transportVm: TransportViewModel = viewModel(factory = transportVmFactory)

    var loginExpectedRole by rememberSaveable { mutableStateOf<String?>(null) }
    var isDriverInbox by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snack, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0), // cada pantalla maneja sus barras (la barra superior se extiende bajo la hora)
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
                    onPassenger = { nav.navigate(Routes.PassengerRegister) },
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
            composable(Routes.PassengerRegister) {
                PassengerRegisterScreen(
                    authVm = authVm,
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
                    authVm = authVm,
                    userName = name,
                    isDriver = isDriverInbox,
                    onPrimaryAction = {
                        if (isDriverInbox) nav.navigate(Routes.DriverSchedule)
                        else nav.navigate(Routes.PassengerSearch)
                    },
                    onLogout = { logout(authVm, nav) },
                    onAccountDeleted = {
                        scope.launch { snack.showSnackbar("Cuenta eliminada.") }
                        logout(authVm, nav)
                    },
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
                DriverScheduleScreen(
                    transportVm = transportVm,
                    onBack = { nav.popBackStack() },
                )
            }
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loadError != null) {
            InlineMessage(loadError)
            PrimaryButton("Ir a inicio de sesión", onClick = onError)
        } else {
            CircularProgressIndicator()
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
