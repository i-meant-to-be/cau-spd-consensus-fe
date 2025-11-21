package com.imeanttobe.consensusapp.navigation

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
