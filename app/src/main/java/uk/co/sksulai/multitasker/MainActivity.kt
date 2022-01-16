package uk.co.sksulai.multitasker

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets

import uk.co.sksulai.multitasker.ui.*
import uk.co.sksulai.multitasker.util.ProvideActivity

@AndroidEntryPoint class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MultitaskerApp(this) }
    }
}

@Composable fun MultitaskerApp(activity: AppCompatActivity) = ProvideActivity(activity) {
    ProvideWindowSizeClass {
        ProvideWindowInsets {
            MultitaskerTheme { EntryPoint() }
        }
    }
}
