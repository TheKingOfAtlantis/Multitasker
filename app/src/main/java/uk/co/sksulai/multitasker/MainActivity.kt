package uk.co.sksulai.multitasker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.Providers
import androidx.compose.staticAmbientOf

import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme

import com.github.zsoltk.compose.backpress.AmbientBackPressHandler
import com.github.zsoltk.compose.backpress.BackPressHandler

import uk.co.sksulai.multitasker.ui.*

val CurrentActivityAmbient = staticAmbientOf<AppCompatActivity>()

class MainActivity : AppCompatActivity() {

    private var initialState: MainState? = null
    private val backPressHandler: BackPressHandler = BackPressHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(MultitaskerTheme.currentTheme) {
                Providers(
                    CurrentActivityAmbient  provides this@MainActivity,
                    AmbientBackPressHandler provides backPressHandler
                ) { EntryPoint(initialState) }
            }
        }
    }

    override fun onBackPressed() {
        if(!backPressHandler.handle())
            super.onBackPressed()
    }

}
