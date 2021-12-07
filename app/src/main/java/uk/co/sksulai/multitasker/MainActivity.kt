package uk.co.sksulai.multitasker

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.util.ProvideActivity

@AndroidEntryPoint class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProvideActivity(this) {
                MultitaskerTheme { EntryPoint() }
            }
        }
    }
}
