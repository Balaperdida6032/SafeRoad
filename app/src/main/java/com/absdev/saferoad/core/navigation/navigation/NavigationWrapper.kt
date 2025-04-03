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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Splash) {
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
        //{  /*username -> navController.navigate(Home(username = username))*/ }
        }

        composable<Login> {
            LoginScreen (auth) {
                navController.navigate(Home)
            }
        }

        composable<Home> {
            HomeScreen ()
        }
    }
}

