package com.imeanttobe.consensusapp.ui.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.navigation.Route
import java.util.UUID

@Composable
fun DevScreen(
    navController: NavHostController,
    viewModel: DevViewModel = hiltViewModel(),
) {
    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Text(text = viewModel.userId.value)

            Button(
                onClick = {
                    viewModel.setId(UUID.randomUUID().toString())
                },
            ) {
                Text(text = "Set ID")
            }

            Button(onClick = { navController.navigate(Route.CreatePollScreen) }) {
                Text(text = "Create Poll")
            }

            Button(onClick = { navController.navigate(Route.HomeRoute) }) {
                Text(text = "Home")
            }

            Button(onClick = { navController.navigate(Route.VoteScreen(id = 0)) }) {
                Text(text = "Vote")
            }

            Button(onClick = { navController.navigate(Route.PollResultScreen(id = 0)) }) {
                Text(text = "Result")
            }

            Button(onClick = { navController.navigate(Route.WaitingScreen(id = 0)) }) {
                Text(text = "Waiting")
            }

            Button(onClick = { navController.navigate(Route.HostDashboardScreen(id = 0)) }) {
                Text(text = "Host Dashboard")
            }
        }
    }
}
