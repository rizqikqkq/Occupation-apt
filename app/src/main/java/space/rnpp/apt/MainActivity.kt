package space.rnpp.apt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import space.rnpp.apt.ui.OccupationFormScreen
import space.rnpp.apt.ui.theme.AptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AptTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OccupationFormScreen(systemPadding = innerPadding)
                }
            }
        }
    }
}