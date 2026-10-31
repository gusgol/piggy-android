package com.goldhardt.auth

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.errorContainerLight
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.onSecondaryContainerLight
import com.goldhardt.designsystem.theme.onSurfaceLight
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryContainerLight
import com.goldhardt.designsystem.theme.primaryFixedDim
import com.goldhardt.designsystem.theme.primaryLight
import com.goldhardt.designsystem.theme.secondaryContainerLight
import com.goldhardt.designsystem.theme.secondaryFixed

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = null)

    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState != null) {
            onSignInSuccess()
        }
    }

    LoginScreenContent(
        modifier = modifier,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onGoogleSignInClick = {
            (context as? Activity)?.let { activity ->
                viewModel.signIn(activity)
            }
        },
        onErrorDismiss = { viewModel.clearError() }
    )
}

@Composable
internal fun LoginScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    error: String? = null,
    onGoogleSignInClick: () -> Unit = {},
    onErrorDismiss: () -> Unit = {}
) {
    val isDark = isPiggyDarkTheme()
    val cardContainerColor = piggyCardContainerColor()
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = if (isDark) 0.45f else 0.30f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
            .padding(horizontal = PiggySpacing.margin + PiggySpacing.spaceXs),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 384.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = PiggyShapeTokens.Xl,
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
            shape = PiggyShapeTokens.Xl,
            color = cardContainerColor,
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PiggyShapeTokens.Xl)
                    .drawBehind {
                        // Top-right ambient emerald flare (-top-12 -right-12 w-36 h-36 bg-primary-fixed-dim/20 blur-2xl)
                        val topRightRadius = 132.dp.toPx()
                        val topRightCenter = Offset(
                            x = size.width - 12.dp.toPx(),
                            y = 12.dp.toPx()
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryFixedDim.copy(alpha = if (isDark) 0.14f else 0.22f),
                                    Color.Transparent
                                ),
                                center = topRightCenter,
                                radius = topRightRadius
                            ),
                            radius = topRightRadius,
                            center = topRightCenter
                        )

                        // Bottom-left ambient rose flare (-bottom-10 -left-10 w-32 h-32 bg-secondary-fixed/30 blur-2xl)
                        val bottomLeftRadius = 116.dp.toPx()
                        val bottomLeftCenter = Offset(
                            x = 12.dp.toPx(),
                            y = size.height - 12.dp.toPx()
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryFixed.copy(alpha = if (isDark) 0.10f else 0.32f),
                                    Color.Transparent
                                ),
                                center = bottomLeftCenter,
                                radius = bottomLeftRadius
                            ),
                            radius = bottomLeftRadius,
                            center = bottomLeftCenter
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PiggySpacing.space2xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Piggy Icon Avatar Hero Badge
                    PiggyHeroAvatar()

                    Spacer(modifier = Modifier.height(PiggySpacing.spaceXl))

                    // App Title
                    Text(
                        text = stringResource(R.string.login_app_title),
                        style = PiggyTypography.headlineLg,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(PiggySpacing.spaceXs))

                    // App Subtitle / Tagline
                    Text(
                        text = stringResource(R.string.login_app_subtitle),
                        style = PiggyTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 260.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(
                            if (error != null) PiggySpacing.spaceXl else PiggySpacing.space2xl
                        )
                    )

                    // Error State Alert Banner
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (error != null) {
                            LoginErrorBanner(
                                message = error,
                                modifier = Modifier.padding(bottom = PiggySpacing.spaceLg)
                            )
                        }
                    }

                    // Primary Auth: Google Sign In
                    GoogleSignInButton(
                        isLoading = isLoading,
                        onClick = onGoogleSignInClick
                    )
                }
            }
        }
    }
}

@Composable
internal fun PiggyHeroAvatar(
    modifier: Modifier = Modifier,
    badgeSize: Dp = 80.dp
) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = primaryLight.copy(alpha = 0.25f),
                spotColor = primaryLight.copy(alpha = 0.30f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(primaryLight, primaryContainerLight),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(badgeSize * 0.6f)) {
            val scaleX = size.width / 64f
            val scaleY = size.height / 64f

            // Left ear: M15 22C13 14 20 13 22 17L23 23
            val leftEar = Path().apply {
                moveTo(15f * scaleX, 22f * scaleY)
                cubicTo(
                    13f * scaleX, 14f * scaleY,
                    20f * scaleX, 13f * scaleY,
                    22f * scaleX, 17f * scaleY
                )
                lineTo(23f * scaleX, 23f * scaleY)
                close()
            }
            drawPath(path = leftEar, color = secondaryContainerLight)

            // Right ear: M49 22C51 14 44 13 42 17L41 23
            val rightEar = Path().apply {
                moveTo(49f * scaleX, 22f * scaleY)
                cubicTo(
                    51f * scaleX, 14f * scaleY,
                    44f * scaleX, 13f * scaleY,
                    42f * scaleX, 17f * scaleY
                )
                lineTo(41f * scaleX, 23f * scaleY)
                close()
            }
            drawPath(path = rightEar, color = secondaryContainerLight)

            // Head Body: ellipse cx="32" cy="35" rx="22" ry="18" fill="#FFDAD6"
            drawOval(
                color = errorContainerLight,
                topLeft = Offset((32f - 22f) * scaleX, (35f - 18f) * scaleY),
                size = Size(44f * scaleX, 36f * scaleY)
            )

            // Cheeks: circle cx="18"/"46" cy="38" r="3.5" fill="#FE7488" fill-opacity="0.6"
            val cheekColor = secondaryContainerLight.copy(alpha = 0.6f)
            drawCircle(
                color = cheekColor,
                radius = 3.5f * scaleX,
                center = Offset(18f * scaleX, 38f * scaleY)
            )
            drawCircle(
                color = cheekColor,
                radius = 3.5f * scaleX,
                center = Offset(46f * scaleX, 38f * scaleY)
            )

            // Eyes: ellipse cx="25"/"39" cy="32" rx="2.2" ry="3" fill="#131B2E"
            drawOval(
                color = onSurfaceLight,
                topLeft = Offset((25f - 2.2f) * scaleX, (32f - 3f) * scaleY),
                size = Size(4.4f * scaleX, 6f * scaleY)
            )
            drawOval(
                color = onSurfaceLight,
                topLeft = Offset((39f - 2.2f) * scaleX, (32f - 3f) * scaleY),
                size = Size(4.4f * scaleX, 6f * scaleY)
            )

            // Eye highlights: circle cx="26"/"40" cy="31" r="0.8" fill="#FFFFFF"
            drawCircle(
                color = Color.White,
                radius = 0.8f * scaleX,
                center = Offset(26f * scaleX, 31f * scaleY)
            )
            drawCircle(
                color = Color.White,
                radius = 0.8f * scaleX,
                center = Offset(40f * scaleX, 31f * scaleY)
            )

            // Snout: ellipse cx="32" cy="40" rx="9" ry="6.5" fill="#FE7488"
            drawOval(
                color = secondaryContainerLight,
                topLeft = Offset((32f - 9f) * scaleX, (40f - 6.5f) * scaleY),
                size = Size(18f * scaleX, 13f * scaleY)
            )

            // Nostrils: ellipse cx="29"/"35" cy="40" rx="1.6" ry="2.2" fill="#730425"
            drawOval(
                color = onSecondaryContainerLight,
                topLeft = Offset((29f - 1.6f) * scaleX, (40f - 2.2f) * scaleY),
                size = Size(3.2f * scaleX, 4.4f * scaleY)
            )
            drawOval(
                color = onSecondaryContainerLight,
                topLeft = Offset((35f - 1.6f) * scaleX, (40f - 2.2f) * scaleY),
                size = Size(3.2f * scaleX, 4.4f * scaleY)
            )
        }
    }
}

@Composable
internal fun LoginErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Lg,
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error_outline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(20.dp)
            )
            Text(
                text = message,
                style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isLoading) 0.98f else 1f,
        label = "googleAuthBtnScale"
    )
    val isDark = isPiggyDarkTheme()
    val containerColor = piggySubtleContainerColor()
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = if (isDark) 0.50f else 0.30f
    )

    Surface(
        onClick = onClick,
        enabled = !isLoading,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = PiggyShapeTokens.Pill,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PiggySpacing.spaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(PiggySpacing.spaceMd))

            Text(
                text = stringResource(R.string.login_continue_with_google),
                style = PiggyTypography.titleSm,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(name = "Piggy Login - Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoginScreenPreview() {
    PiggyTheme {
        LoginScreenContent()
    }
}

@Preview(
    name = "Piggy Login - Dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        LoginScreenContent()
    }
}

@Preview(name = "Piggy Login - Error State", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoginScreenErrorPreview() {
    PiggyTheme {
        LoginScreenContent(
            error = "Sign-in was cancelled or failed. Please try again."
        )
    }
}
