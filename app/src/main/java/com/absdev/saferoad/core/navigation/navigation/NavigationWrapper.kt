package com.absdev.saferoad.core.navigation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.absdev.saferoad.SplashScreen
import com.absdev.saferoad.WelcomeScreen
import com.absdev.saferoad.core.navigation.BottomNavigation.Admin.AdminNavigationScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.CarreraDetailScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.DefinirRutaCarreraScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.EditarCarreraScreen
import com.absdev.saferoad.core.navigation.Home.CarreraForm.CarreraFormScreen
import com.absdev.saferoad.core.navigation.Home.CarreraForm.UploadImageScreen
import com.absdev.saferoad.core.navigation.HomeScreen.HomeScreen
import com.absdev.saferoad.core.navigation.LoginScreen.LoginScreen
import com.absdev.saferoad.core.navigation.Profile.Edit.EditarPerfilScreen
import com.absdev.saferoad.core.navigation.Profile.ProfileScreen
import com.absdev.saferoad.core.navigation.SingUp.SingScreen
import com.absdev.saferoad.core.navigation.model.Carrera
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.absdev.saferoad.core.navigation.maps.CarreraMapaScreen
import com.absdev.saferoad.core.navigation.maps.ParticipanteCarreraStartScreen
import com.absdev.saferoad.core.navigation.navigation.ParticipanteCarreraStart
import com.absdev.saferoad.core.navigation.maps.ParticipanteCarreraStartScreen


@Composable
fun NavigationWrapper(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var startDestination by remember { mutableStateOf<String?>(null) }

    if (startDestination == null) {
        if (user == null) {
            startDestination = Welcome::class.qualifiedName
        } else {
            val uid = user.uid
            db.collection("profile").document(uid).get().addOnSuccessListener { document ->
                val role = document.getString("role")
                startDestination = when (role) {
                    "admin" -> AdminHome::class.qualifiedName
                    else -> Home::class.qualifiedName
                }
            }.addOnFailureListener {
                startDestination = Home::class.qualifiedName
            }
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable<Splash> {
                SplashScreen(
                    navigateToWelcome = { navController.navigate(Welcome) }
                )
            }

            composable<Welcome> {
                WelcomeScreen(
                    navigateToLogin = { navController.navigate(Login) },
                    navigateToSign = { navController.navigate(Sign) }
                )
            }

            composable<Sign> {
                SingScreen(
                    auth,
                    navController
                )
            }

            composable<Login> {
                LoginScreen(
                    auth,
                    navigateToHome = { navController.navigate(Home) },
                    navigateToAdminHome = { navController.navigate(AdminHome) },
                    navController
                )
            }

            composable<Home> {
                MainNavigationScreen()
            }

            composable<AdminHome> {
                AdminNavigationScreen()
            }

            composable<Profile> {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        auth.signOut()
                        navController.navigate(Welcome) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }


            composable<CarreraForm> {
                CarreraFormScreen(navController)
            }

            composable<CarreraDetailScreen> {
                val carrera = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<Carrera>("carrera")

                carrera?.let {
                    CarreraDetailScreen(it, navController)
                }
            }

            composable<UploadImage> {
                UploadImageScreen()
            }

            composable<EditarCarrera> {
                val carrera = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<Carrera>("carrera")
                carrera?.let {
                    EditarCarreraScreen(it, navController)
                }
            }

            composable<EditarPerfil> {
                EditarPerfilScreen(navController)
            }

            composable<CarreraMapa> { backStackEntry ->
                val carreraId = backStackEntry.arguments?.getString("carreraId") ?: return@composable
                CarreraMapaScreen(idCarrera = carreraId)
            }

            composable<ParticipanteCarreraStart> { backStackEntry ->
                val carreraId = backStackEntry.arguments?.getString("carreraId") ?: return@composable
                ParticipanteCarreraStartScreen(
                    idCarrera = carreraId,
                    navController = navController)
            }

            composable<DefinirRutaCarrera> { backStackEntry ->
                val carreraId = backStackEntry.arguments?.getString("carreraId") ?: return@composable
                DefinirRutaCarreraScreen(carreraId = carreraId, navController = navController)
            }
            //fin :)
        }
    }
}
