package com.feelme.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyColumnDefaults
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val client by lazy { WearPingClient(this) }
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var status by remember { mutableStateOf("Ready") }
            MaterialTheme {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    scalingParams = ScalingLazyColumnDefaults.scalingParams()
                ) {
                    item { TimeText() }
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Feel Me")
                            Button(onClick = {
                                scope.launch {
                                    status = client.sendPingFromWatch()
                                }
                            }) { Text("Send ping") }
                            Text(text = status)
                        }
                    }
                }
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        client.register()
    }

    override fun onPause() {
        client.unregister()
        super.onPause()
    }
}
