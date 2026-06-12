package com.mobiledevpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.mobiledevpro.app.ui.MainApp
import com.mobiledevpro.ui.theme.AppTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Init PDFBox to extract the text from PDF files
        PDFBoxResourceLoader.init(this.applicationContext)

        //False - allows to drawing the content "edge-to-edge"
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            AppTheme {
                CompositionLocalProvider {
                    MainApp()
                }
            }
        }
    }
}