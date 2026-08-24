package com.repforge.app

import android.animation.ValueAnimator
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.stateDescription
import com.repforge.core.designsystem.R as DesignSystemR
import com.repforge.core.designsystem.token.FitnessVisualTokens
import com.repforge.core.designsystem.token.RepForgeBrandTokens
import com.repforge.core.designsystem.token.RepForgeMotion
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.datastore.PreferencesDataSource
import com.repforge.feature.onboarding.OnboardingScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
internal fun RepForgeRoot(preferencesDataSource: PreferencesDataSource) {
    val onboardingDone by produceState<Boolean?>(initialValue = null, preferencesDataSource) {
        preferencesDataSource.onboardingDone.collect { value = it }
    }
    val scope = rememberCoroutineScope()

    when (onboardingDone) {
        null -> ForgeLoadingScreen()
        false -> OnboardingScreen(
            onFinish = {
                scope.launch { preferencesDataSource.setOnboardingDone(true) }
            },
        )
        true -> RepForgeNavHost()
    }
}

@Composable
internal fun ForgeLoadingScreen(modifier: Modifier = Modifier) {
    val motionEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    val loadingDescription = stringResource(R.string.launch_loading_description)
    val loadingState = stringResource(R.string.launch_loading_message)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = loadingDescription
                    stateDescription = loadingState
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FitnessVisualTokens.FitnessSpacing.md),
            ) {
                ForgeLoadingMark(motionEnabled = motionEnabled)
                Text(
                    text = stringResource(R.string.launch_loading_eyebrow),
                    style = RepForgeTypeRoles.LabelExpressive,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = loadingState,
                    style = RepForgeTypeRoles.BodySupport,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ForgeLoadingMark(motionEnabled: Boolean) {
    if (motionEnabled) {
        val transition = rememberInfiniteTransition(label = "forgeLoading")
        val scale by transition.animateFloat(
            initialValue = RepForgeBrandTokens.LoadingStartScale,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = RepForgeMotion.DurationEmphasized,
                    easing = RepForgeMotion.EmphasizedEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "forgeLoadingScale",
        )
        val alpha by transition.animateFloat(
            initialValue = RepForgeBrandTokens.LoadingStartAlpha,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = RepForgeMotion.DurationEmphasized,
                    easing = RepForgeMotion.EmphasizedEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "forgeLoadingAlpha",
        )
        ForgeMark(scale = scale, alpha = alpha)
    } else {
        ForgeMark(scale = 1f, alpha = 1f)
    }
}

@Composable
private fun ForgeMark(scale: Float, alpha: Float) {
    Surface(
        modifier = Modifier
            .size(RepForgeBrandTokens.MarkLoading)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha),
        shape = RepForgeShapes.Hero,
        color = colorResource(DesignSystemR.color.repforge_forge),
    ) {
        Image(
            painter = painterResource(DesignSystemR.drawable.ic_repforge_mark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
