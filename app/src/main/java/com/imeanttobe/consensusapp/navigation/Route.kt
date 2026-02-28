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
    data object DevRoute : Route

    @Serializable
    data object BenchmarkScreen : Route

    @Serializable
    data class VoteResultScreen(val isVoteSuccess: Boolean, val errorMessage: String? = null) : Route

    @Serializable
    data class HostDashboardScreen(val id: Int) : Route

    @Serializable
    data class VoteScreen(val id: Int) : Route

    @Serializable
    data class PollResultScreen(val id: Int) : Route

    @Serializable
    data class PollFinishScreen(val id: Int) : Route
}