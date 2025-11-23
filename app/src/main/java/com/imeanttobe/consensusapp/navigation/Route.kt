package com.imeanttobe.consensusapp.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object HomeRoute : Route

    @Serializable
    data object SplashScreen : Route

    @Serializable
    data object CreatePollScreen : Route

    @Serializable
    data class HostDashboardScreen(val pollId: String) : Route

    @Serializable
    data class VoteScreen(val pollId: String) : Route

    @Serializable
    data class WaitingScreen(val pollId: String) : Route

    @Serializable
    data class ResultScreen(val pollId: String) : Route
}

/*
sealed class Route(
    val name: String,
    val path: String,
) {
    object HomeRoute : Route(
        name = "Home",
        path = "/",
    )

    object SplashScreen : Route(
        name = "Splash",
        path = "/splash",
    )

    object CreatePollScreen : Route(
        name = "Create poll",
        path = "/poll/create",
    )

    object HostDashboardScreen : Route(
        name = "Host dashboard",
        path = "/poll/{pollId}/dashboard",
    ) {
        fun createRoute(pollId: String): String = this.path.replace("{pollId}", pollId)
    }

    object VoteScreen : Route(
        name = "Vote",
        path = "/poll/{pollId}/vote",
    ) {
        fun createRoute(pollId: String): String = this.path.replace("{pollId}", pollId)
    }

    object WaitingScreen : Route(
        name = "Waiting",
        path = "/poll/{pollId}/waiting",
    ) {
        fun createRoute(pollId: String): String = this.path.replace("{pollId}", pollId)
    }

    object ResultScreen : Route(
        name = "Result",
        path = "/poll/{pollId}/result",
    ) {
        fun createRoute(pollId: String): String = this.path.replace("{pollId}", pollId)
    }
}
 */