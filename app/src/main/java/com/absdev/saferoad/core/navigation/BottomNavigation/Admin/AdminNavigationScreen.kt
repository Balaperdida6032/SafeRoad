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
import com.absdev.saferoad.core.navigation.Profile.ProfileScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.lang.reflect.Modifier

@Composable
fun AdminNavigationScreen() {
    val navController = rememberNavController()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = true // esto es clave para que los íconos sean blancos
        )
    }

    Scaffold(
        bottomBar = {
            AdminBottomBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminBottomNavItem.AdminHome.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(AdminBottomNavItem.AdminHome.route) {
                AdminHomeScreen()
            }
            composable(AdminBottomNavItem.Profile.route) {
                ProfileScreen(navController)

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