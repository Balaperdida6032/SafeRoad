package com.absdev.saferoad.core.navigation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.absdev.saferoad.core.navigation.BottomNavigation.BottomNavItem
import com.absdev.saferoad.core.navigation.Home.CarreraDetail.CarreraDetailScreen
import com.absdev.saferoad.core.navigation.HomeScreen.HomeScreen
import com.absdev.saferoad.core.navigation.Profile.Edit.EditarPerfilScreen
import com.absdev.saferoad.core.navigation.Profile.ProfileScreen
import com.absdev.saferoad.core.navigation.maps.CarreraMapaScreen
import com.absdev.saferoad.core.navigation.maps.ParticipanteCarreraStartScreen
import com.absdev.saferoad.core.navigation.model.Carrera
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.ktx.Firebase

@Composable
fun MainNavigationScreen() {
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
            if (currentRoute?.startsWith("ParticipanteCarreraStartScreen") != true) {
                BottomBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(navController)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController)
            }

            composable<CarreraMapa> { backStackEntry ->
                val carreraId = backStackEntry.arguments?.getString("carreraId") ?: return@composable
                CarreraMapaScreen(idCarrera = carreraId)
            }

            composable<EditarPerfil> {
                EditarPerfilScreen(navController)
            }

            composable<CarreraDetailScreen> {
                val carrera = navController.previousBackStackEntry?.savedStateHandle?.get<Carrera>("carrera")
                carrera?.let {
                    CarreraDetailScreen(it, navController)
                }
            }

            composable<ParticipanteCarreraStart> { backStackEntry ->
                val carreraId = backStackEntry.arguments?.getString("carreraId") ?: return@composable
                ParticipanteCarreraStartScreen(
                    idCarrera = carreraId,
                    navController = navController // si lo necesita
                )
            }

        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Profile)
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

