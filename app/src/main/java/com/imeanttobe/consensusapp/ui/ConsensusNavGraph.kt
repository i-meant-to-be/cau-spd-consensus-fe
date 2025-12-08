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
import com.imeanttobe.consensusapp.ui.poll_finish.PollFinishScreen
import com.imeanttobe.consensusapp.ui.poll_result.PollResultScreen
import com.imeanttobe.consensusapp.ui.splash.SplashScreen
import com.imeanttobe.consensusapp.ui.vote.VoteScreen
import com.imeanttobe.consensusapp.ui.vote_result.VoteResultScreen
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
            CreatePollScreen(navController = navController)
        }

        composable<Route.VoteScreen>(
            deepLinks =
                listOf(
                    navDeepLink {
                        uriPattern = "consensus://poll/{id}"
                    },
                ),
        ) { backStackEntry ->
            val route: Route.VoteScreen = backStackEntry.toRoute()
            VoteScreen(id = route.id, navController = navController)
        }

        composable<Route.PollResultScreen> { backStackEntry ->
            val route: Route.PollResultScreen = backStackEntry.toRoute()
            PollResultScreen(id = route.id)
        }

        composable<Route.VoteResultScreen> { backStackEntry ->
            val route: Route.VoteResultScreen = backStackEntry.toRoute()
            VoteResultScreen(isVoteSuccess = route.isVoteSuccess, errorMessage = route.errorMessage)
        }

        composable<Route.HostDashboardScreen> { backStackEntry ->
            val route: Route.HostDashboardScreen = backStackEntry.toRoute()
            HostDashboardScreen(id = route.id)
        }

        composable<Route.WaitingScreen> { backStackEntry ->
            val route: Route.WaitingScreen = backStackEntry.toRoute()
            WaitingScreen(id = route.id)
        }

        composable<Route.PollFinishScreen> { backStackEntry ->
            val route: Route.PollFinishScreen = backStackEntry.toRoute()
            PollFinishScreen(id = route.id)
        }
    }
}
