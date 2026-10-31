package com.goldhardt.auth

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldhardt.core.auth.model.User
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryFixed
import com.goldhardt.designsystem.theme.primaryFixedDim

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val title = stringResource(R.string.settings_title)
    val backDescription = stringResource(R.string.settings_navigate_back)

    ConfigureTopBar(
        title = title,
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(piggySubtleContainerColor())
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = backDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )

    SettingsScreenContent(
        user = user,
        isSigningOut = uiState.isSigningOut,
        error = uiState.error,
        onSignOutClick = { viewModel.signOut() },
        modifier = modifier
    )
}

@Composable
internal fun SettingsScreenContent(
    user: User?,
    isSigningOut: Boolean = false,
    error: String? = null,
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(
                horizontal = PiggySpacing.margin,
                vertical = PiggySpacing.spaceLg
            )
    ) {
        // Profile Summary Hero Card
        ProfileSummaryCard(user = user)

        Spacer(modifier = Modifier.height(PiggySpacing.space2xl))

        // Error Alert Banner (if sign-out fails)
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

        // Account Section Label
        Text(
            text = stringResource(R.string.settings_section_account_caps),
            style = PiggyTypography.labelCaps,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = PiggySpacing.space2xs,
                bottom = PiggySpacing.spaceSm
            )
        )

        // Logout Action Card
        SignOutActionCard(
            isSigningOut = isSigningOut,
            onClick = onSignOutClick
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(PiggySpacing.space3xl))

        // App Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PiggySpacing.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.settings_footer_app_version),
                style = PiggyTypography.badgeLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.settings_footer_tagline),
                style = PiggyTypography.bodySm,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    user: User?,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val containerColor = piggyCardContainerColor()
    val borderColor = if (isDark) {
        PiggyColors.Emerald500.copy(alpha = 0.22f)
    } else {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PiggyShapeTokens.Card)
                .drawBehind {
                    val radius = 140.dp.toPx()
                    val center = Offset(x = size.width - 16.dp.toPx(), y = 8.dp.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryFixedDim.copy(alpha = if (isDark) 0.14f else 0.20f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
                .padding(PiggySpacing.spaceXl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceLg)
            ) {
                UserAvatarBadge(photoUrl = user?.photoUrl)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.space2xs)
                ) {
                    AccountProviderBadge()

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = user?.displayName?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.settings_default_user_name),
                        style = PiggyTypography.headlineMd,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!user?.email.isNullOrBlank()) {
                        Text(
                            text = user.email.orEmpty(),
                            style = PiggyTypography.bodyMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAvatarBadge(
    photoUrl: String?,
    modifier: Modifier = Modifier,
) {
    val ringColor = if (isPiggyDarkTheme()) {
        PiggyColors.Emerald500.copy(alpha = 0.45f)
    } else {
        primaryFixed
    }

    Box(
        modifier = modifier
            .size(68.dp)
            .border(width = 2.dp, color = ringColor, shape = CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            PiggyHeroAvatar(badgeSize = 62.dp)
        }
    }
}

@Composable
private fun AccountProviderBadge(
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val badgeBg = if (isDark) {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    } else {
        PiggyColors.Emerald50
    }
    val badgeText = if (isDark) {
        primaryFixed
    } else {
        PiggyColors.Emerald700
    }

    Row(
        modifier = modifier
            .clip(PiggyShapeTokens.Pill)
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(PiggyColors.Emerald500)
        )
        Text(
            text = stringResource(R.string.settings_account_badge_caps),
            style = PiggyTypography.labelCaps,
            color = badgeText
        )
    }
}

@Composable
private fun SignOutActionCard(
    isSigningOut: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isSigningOut) 0.985f else 1f,
        label = "signOutCardScale"
    )
    val isDark = isPiggyDarkTheme()
    val iconContainerColor = if (isDark) {
        PiggyColors.CategoryHousing.copy(alpha = 0.18f)
    } else {
        PiggyColors.CategoryHousingTint
    }
    val accentRoseColor = if (isDark) {
        MaterialTheme.colorScheme.error
    } else {
        PiggyColors.Rose600
    }

    Surface(
        onClick = onClick,
        enabled = !isSigningOut,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd)
        ) {
            // 44.dp circular category-style icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = accentRoseColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_logout_title),
                    style = PiggyTypography.titleSm,
                    color = accentRoseColor
                )
                Text(
                    text = stringResource(R.string.settings_logout_subtitle),
                    style = PiggyTypography.bodySm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSigningOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = accentRoseColor
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(name = "Settings Screen - Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    PiggyTheme {
        SettingsScreenContent(
            user = User(
                id = "1",
                email = "alex.rivera@piggy.app",
                displayName = "Alex Rivera",
                photoUrl = null
            )
        )
    }
}

@Preview(
    name = "Settings Screen - Dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        SettingsScreenContent(
            user = User(
                id = "1",
                email = "alex.rivera@piggy.app",
                displayName = "Alex Rivera",
                photoUrl = null
            )
        )
    }
}
