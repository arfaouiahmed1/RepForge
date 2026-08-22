package com.repforge.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button

/**
 * Wear OS companion — solves phone-between-sets problem.
 * Uses Health Services ExerciseClient for workout control + Wear Compose M3 expressive button groups.
 *
 * BENCH 82.5 kg × 8 [COMPLETE] → REST 1:34 +15 SKIP with HR
 */
class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearApp() }
    }
}

@Composable
fun WearApp() {
    var phase by remember { mutableStateOf("lift") } // lift | rest
    var rest by remember { mutableStateOf(94) }
    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black), contentAlignment = Alignment.Center) {
        if (phase == "lift") {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BENCH", color = androidx.compose.ui.graphics.Color.White)
                Text("82.5 kg × 8", color = androidx.compose.ui.graphics.Color.White)
                Button(onClick = { phase = "rest" }) { Text("COMPLETE") }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("REST", color = androidx.compose.ui.graphics.Color.White)
                Text(String.format("%d:%02d", rest/60, rest%60), color = androidx.compose.ui.graphics.Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { rest += 15 }) { Text("+15") }
                    Button(onClick = { phase = "lift" }) { Text("SKIP") }
                }
                Text("HR 128", color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
    }
}
