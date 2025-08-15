package com.goldhardt.piggy.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldhardt.designsystem.components.LocalTopBarController
import com.goldhardt.designsystem.components.PiggyTopBar
import com.goldhardt.designsystem.components.TopBarController
import com.goldhardt.piggy.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    current: Screen,
    onItemClick: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val topBar = remember { TopBarController() }

    CompositionLocalProvider(LocalTopBarController provides topBar) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val photoUrl = user?.photoUrl
                PiggyTopBar(
                    topBarController = topBar,
                    navigationIcon = {
                        if (!photoUrl.isNullOrBlank()) {
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
                        } else null
                    }
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
