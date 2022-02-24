package uk.co.sksulai.multitasker.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

import dagger.hilt.android.testing.CustomTestApplication

import uk.co.sksulai.multitasker.BaseMultitaskerApp

class Runner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(cl, TestApplication_Application::class.java.name, context)
}
