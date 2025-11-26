package com.feelme.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository by lazy { PingRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, PingRelayService::class.java))

        setContent {
            var partner by remember { mutableStateOf("") }
            var status by remember { mutableStateOf("Not paired") }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Feel Me", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = partner,
                            onValueChange = { partner = it },
                            label = { Text("Partner email/ID") }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            lifecycleScope.launch {
                                status = repository.pairWithPartner(partner)
                            }
                        }) { Text("Pair") }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            lifecycleScope.launch {
                                status = repository.sendPing(partner)
                            }
                        }) { Text("Send ping") }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = status)
                    }
                }
            }
        }
    }
}
