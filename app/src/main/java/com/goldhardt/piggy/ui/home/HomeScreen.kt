package com.goldhardt.piggy.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldhardt.core.auth.model.User
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.LocalTopBarController
import com.goldhardt.designsystem.components.PiggyTopBar
import com.goldhardt.designsystem.components.TopBarController
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.piggy.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User?,
    current: Screen,
    onItemClick: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val topBar = remember { TopBarController() }

    CompositionLocalProvider(LocalTopBarController provides topBar) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val photoUrl = user?.photoUrl
                PiggyTopBar(
                    topBarController = topBar,
                    navigationIcon = if (!photoUrl.isNullOrBlank()) {
                        {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User avatar",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        }
                    } else null
                )
            },
            bottomBar = {
                PiggyBottomBar(
                    current,
                    onItemClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}

@Preview(name = "Home Screen Shell - Light", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PiggyTheme {
        HomeScreen(
            user = User(
                id = "1",
                email = "alex@piggy.app",
                displayName = "Alex",
                photoUrl = null
            ),
            current = Screen.Expenses,
            onItemClick = {}
        ) {
            ConfigureTopBar(title = "Expenses")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Expenses Screen Content",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(
    name = "Home Screen Shell - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        HomeScreen(
            user = User(
                id = "1",
                email = "alex@piggy.app",
                displayName = "Alex",
                photoUrl = null
            ),
            current = Screen.Trends,
            onItemClick = {}
        ) {
            ConfigureTopBar(title = "Trends")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Trends Screen Content",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
