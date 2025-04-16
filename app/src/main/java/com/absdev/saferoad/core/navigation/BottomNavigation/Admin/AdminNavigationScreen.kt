package com.absdev.saferoad.core.navigation.BottomNavigation.Admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.absdev.saferoad.core.navigation.BottomNavigation.BottomNavItem
import com.absdev.saferoad.core.navigation.Home.Admin.AdminHomeScreen.AdminHomeScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.CarreraDetailScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.DefinirRutaCarreraScreen
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.EditarCarreraScreen
import com.absdev.saferoad.core.navigation.Home.CarreraForm.CarreraFormScreen
import com.absdev.saferoad.core.navigation.Profile.Edit.EditarPerfilScreen
import com.absdev.saferoad.core.navigation.Profile.ProfileScreen
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.navigation.CarreraDetailScreen
import com.absdev.saferoad.core.navigation.navigation.CarreraForm
import com.absdev.saferoad.core.navigation.navigation.EditarCarrera
import com.absdev.saferoad.core.navigation.navigation.EditarPerfil
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.lang.reflect.Modifier
import com.absdev.saferoad.core.navigation.navigation.CarreraMapa
import com.absdev.saferoad.core.navigation.maps.CarreraMapaScreen
import com.absdev.saferoad.core.navigation.maps.ParticipanteCarreraStartScreen
import com.absdev.saferoad.core.navigation.navigation.DefinirRutaCarrera
import com.absdev.saferoad.core.navigation.navigation.ParticipanteCarreraStart


@Composable
fun AdminNavigationScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = true // esto es clave para que los íconos sean blancos
        )
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != CarreraForm::class.qualifiedName) {
                AdminBottomBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminBottomNavItem.AdminHome.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(AdminBottomNavItem.AdminHome.route) {
                AdminHomeScreen(navController)
            }
            composable(AdminBottomNavItem.Profile.route) {
                ProfileScreen(navController)
            }
            composable(CarreraForm::class.qualifiedName!!) {
                CarreraFormScreen(navController)
            }

            composable<CarreraDetailScreen> {
                val carrera = navController.previousBackStackEntry?.savedStateHandle?.get<Carrera>("carrera")
                carrera?.let {
                    CarreraDetailScreen(it, navController)
                }
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

        }
    }
}

@Composable
fun AdminBottomBar(navController: NavHostController) {
    val items = listOf(AdminBottomNavItem.AdminHome, AdminBottomNavItem.Profile)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(containerColor = Color.Black) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}