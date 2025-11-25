package com.imeanttobe.consensusapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.imeanttobe.consensusapp.BuildConfig
import com.imeanttobe.consensusapp.navigation.Route
import com.imeanttobe.consensusapp.ui.create_poll.CreatePollScreen
import com.imeanttobe.consensusapp.ui.dev.DevScreen
import com.imeanttobe.consensusapp.ui.home.HomeScreen
import com.imeanttobe.consensusapp.ui.host_dashboard.HostDashboardScreen
import com.imeanttobe.consensusapp.ui.result.ResultScreen
import com.imeanttobe.consensusapp.ui.splash.SplashScreen
import com.imeanttobe.consensusapp.ui.vote.VoteScreen
import com.imeanttobe.consensusapp.ui.waiting.WaitingScreen

@Composable
fun ConsensusNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.SplashScreen,
    ) {
        if (BuildConfig.IS_DEV_MODE_ENABLED) {
            composable<Route.DevRoute> {
                DevScreen(navController = navController)
            }
        }

        composable<Route.SplashScreen> {
            SplashScreen(navController = navController)
        }

        composable<Route.HomeRoute> {
            HomeScreen(navController = navController)
        }

        composable<Route.CreatePollScreen> {
            CreatePollScreen()
        }

        composable<Route.VoteScreen>(
            deepLinks =
                listOf(
                    navDeepLink {
                        uriPattern = "consensus://poll/{pollId}"
                    },
                ),
        ) { backStackEntry ->
            val route: Route.VoteScreen = backStackEntry.toRoute()
            VoteScreen(pollId = route.pollId)
        }

        composable<Route.ResultScreen> { backStackEntry ->
            val route: Route.ResultScreen = backStackEntry.toRoute()
            ResultScreen(pollId = route.pollId)
        }

        composable<Route.HostDashboardScreen> { backStackEntry ->
            val route: Route.HostDashboardScreen = backStackEntry.toRoute()
            HostDashboardScreen(pollId = route.pollId)
        }

        composable<Route.WaitingScreen> { backStackEntry ->
            val route: Route.WaitingScreen = backStackEntry.toRoute()
            WaitingScreen(pollId = route.pollId)
        }
    }
}
