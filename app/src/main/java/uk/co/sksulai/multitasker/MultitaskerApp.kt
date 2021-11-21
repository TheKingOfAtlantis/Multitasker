package uk.co.sksulai.multitasker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

abstract class BaseMultitaskerApp : Application()
@HiltAndroidApp class MultitaskerApp : BaseMultitaskerApp()
