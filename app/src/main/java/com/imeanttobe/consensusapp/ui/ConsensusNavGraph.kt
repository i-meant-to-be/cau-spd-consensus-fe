package com.imeanttobe.consensusapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.imeanttobe.consensusapp.navigation.Route
import com.imeanttobe.consensusapp.ui.dev.DevScreen
import com.imeanttobe.consensusapp.ui.home.HomeScreen
import com.imeanttobe.consensusapp.ui.splash.SplashScreen
import com.imeanttobe.consensusapp.ui.vote.VoteScreen

@Composable
fun ConsensusNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.SplashScreen
    ) {
        composable<Route.SplashScreen> {
            SplashScreen(navController = navController)
        }

        composable<Route.HomeRoute> {
            HomeScreen()
        }

        composable<Route.DevRoute> {
            DevScreen()
        }

        composable<Route.VoteScreen>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "consensus://poll/{pollId}"
                }
            )
        ) { backStackEntry ->
            val route: Route.VoteScreen = backStackEntry.toRoute()

            VoteScreen(pollId = route.pollId)
        }

    }
}