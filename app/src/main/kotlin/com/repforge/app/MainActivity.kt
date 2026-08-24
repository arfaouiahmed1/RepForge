package com.repforge.app

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import com.repforge.core.designsystem.token.RepForgeBrandTokens
import com.repforge.core.designsystem.token.RepForgeMotion
import com.repforge.core.datastore.PreferencesDataSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var preferencesDataSource: PreferencesDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener(::animateSplashExit)
        enableEdgeToEdge()
        setContent {
            RepForgeRoot(preferencesDataSource = preferencesDataSource)
        }
    }

    private fun animateSplashExit(provider: SplashScreenViewProvider) {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            provider.remove()
            return
        }

        val lift = RepForgeBrandTokens.SplashLift.value * resources.displayMetrics.density
        provider.iconView.animate()
            .scaleX(RepForgeBrandTokens.SplashExitScale)
            .scaleY(RepForgeBrandTokens.SplashExitScale)
            .translationY(-lift)
            .setDuration(RepForgeMotion.DurationStandard.toLong())
            .setInterpolator(RepForgeBrandTokens.SplashExitInterpolator)
            .start()
        provider.view.animate()
            .alpha(0f)
            .setDuration(RepForgeMotion.DurationStandard.toLong())
            .setInterpolator(RepForgeBrandTokens.SplashExitInterpolator)
            .withEndAction { provider.remove() }
            .start()
    }
}
