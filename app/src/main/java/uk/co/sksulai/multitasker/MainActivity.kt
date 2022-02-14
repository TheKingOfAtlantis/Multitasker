package uk.co.sksulai.multitasker

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.util.ProvideActivity

@AndroidEntryPoint class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MultitaskerApp(this) }
    }
}

@Composable fun MultitaskerApp(activity: ComponentActivity) = ProvideActivity(activity) {
    ProvideWindowSizeClass {
        ProvideWindowInsets {
            MultitaskerTheme { EntryPoint() }
        }
    }
}
