package uk.co.sksulai.multitasker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import uk.co.sksulai.multitasker.ui.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultitaskerTheme { EntryPoint() }
        }
    }
}
