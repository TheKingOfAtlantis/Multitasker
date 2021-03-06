package uk.co.sksulai.multitasker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.material.*

import uk.co.sksulai.multitasker.ui.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(MultitaskerTheme.currentTheme) {
                EntryPoint()
            }
        }
    }
}
