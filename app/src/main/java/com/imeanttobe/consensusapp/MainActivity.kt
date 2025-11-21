package com.imeanttobe.consensusapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.imeanttobe.consensusapp.data.idDataStore
import com.imeanttobe.consensusapp.seal.NativeLib
import com.imeanttobe.consensusapp.ui.theme.ConsensusTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ConsensusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
) {
    val messageFromCpp = NativeLib.stringFromJNI()
    val context = LocalContext.current
    val data = context.idDataStore.data.collectAsState(initial = Id.getDefaultInstance())
    val coroutineScope = rememberCoroutineScope()

    Column {
        Text(
            text = messageFromCpp,
            modifier = modifier,
        )

        Text(
            text = data.value.id,
            modifier = modifier,
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    context.idDataStore.updateData {
                        it.toBuilder().setId("Proto Datastore attached!").build()
                    }
                }
            },
        ) {
            Text(text = "Update")
        }
    }
}
