package com.absdev.saferoad.core.navigation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.absdev.saferoad.core.navigation.SingUp.SingScreen
import com.absdev.saferoad.SplashScreen
import com.absdev.saferoad.WelcomeScreen
import com.absdev.saferoad.core.navigation.HomeScreen.HomeScreen
import com.absdev.saferoad.core.navigation.LoginScreen.LoginScreen
import com.absdev.saferoad.core.navigation.Profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(auth: FirebaseAuth,isUserLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (isUserLoggedIn) Home else Splash

    NavHost(navController = navController, startDestination = startDestination ) {
        composable<Splash> {
            SplashScreen (
                navigateToWelcome = { navController.navigate(Welcome) })
        }

        composable<Welcome> {
            WelcomeScreen (
                navigateToLogin = { navController.navigate(Login) },
                navigateToSign = { navController.navigate(Sign) })
        }

        composable<Sign> {
            SingScreen (auth)
        }

        composable<Login> {
            LoginScreen (auth) {
                navController.navigate(Home) {
                    popUpTo(Login) { inclusive = true }
                }
            }
        }

        composable<Home> {
            MainNavigationScreen()
        }

        composable<Profile> {
            ProfileScreen(navController)
        }
    }
}

