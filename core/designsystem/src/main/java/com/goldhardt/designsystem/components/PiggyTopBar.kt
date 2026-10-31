package com.goldhardt.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class TopBarConfig(
    val title: String? = null,
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: @Composable RowScope.() -> Unit = {}
)

class TopBarController {
    var config by mutableStateOf(TopBarConfig())
        private set

    fun set(
        title: String? = null,
        navigationIcon: (@Composable () -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {}
    ) {
        config = TopBarConfig(title, navigationIcon, actions)
    }

    fun clear() {
        config = TopBarConfig()
    }
}

@Composable
fun ConfigureTopBar(
    title: String?,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val topBar = LocalTopBarController.current
    SideEffect {
        topBar.set(title, navigationIcon, actions)
    }
}

val LocalTopBarController = staticCompositionLocalOf<TopBarController> {
    TopBarController()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiggyTopBar(
    topBarController: TopBarController,
    navigationIcon: (@Composable () -> Unit)? = null,
    onNavigationIconClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                val configuredNavIcon = topBarController.config.navigationIcon
                when {
                    configuredNavIcon != null -> configuredNavIcon()
                    navigationIcon != null -> navigationIcon()
                    else -> FallbackAvatar(onClick = onNavigationIconClick)
                }
            }
        },
        title = {
            Text(
                text = topBarController.config.title ?: "",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        actions = topBarController.config.actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun FallbackAvatar(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val pink = MaterialTheme.colorScheme.secondaryContainer
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(pink)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = "🐷",
            style = MaterialTheme.typography.titleMedium
        )
    }
}


@Preview
@Composable
private fun FallbackAvatarPreview() {
    FallbackAvatar()
}


