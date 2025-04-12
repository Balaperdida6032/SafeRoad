package com.absdev.saferoad.core.navigation.BottomNavigation.Admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.absdev.saferoad.core.navigation.BottomNavigation.BottomNavItem

sealed class AdminBottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object AdminHome : AdminBottomNavItem("AdminHome", "Home", Icons.Default.Home)
    object Profile : AdminBottomNavItem("profile", "Profile", Icons.Default.Person)
}