package uk.co.sksulai.multitasker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme

import uk.co.sksulai.multitasker.ui.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(MultitaskerTheme.currentTheme) {

            }
        }
    }
}
