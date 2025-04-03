package com.absdev.saferoad.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.absdev.saferoad.HomeScreen
import com.absdev.saferoad.LoginScreen
import com.absdev.saferoad.SingScreen
import com.absdev.saferoad.SplashScreen
import com.absdev.saferoad.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth

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
            LoginScreen (auth)
                /*username -> navController.navigate(Home(username = username))*/
        }

        composable<Home> {
            HomeScreen { navController.navigate(Welcome) }
        }
    }
}

