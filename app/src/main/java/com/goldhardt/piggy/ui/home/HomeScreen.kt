package com.goldhardt.piggy.ui.home

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldhardt.designsystem.components.LocalTopBarController
import com.goldhardt.designsystem.components.PiggyTopBar
import com.goldhardt.designsystem.components.TopBarController
import com.goldhardt.piggy.R
import com.goldhardt.piggy.navigation.Screen
import com.goldhardt.piggy.notifications.ExpenseNotificationHelper
import com.goldhardt.piggy.notifications.ExpenseNotificationListener

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
            val contxt = LocalContext.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {


                content()

                Button(onClick = {
                    val title = "Wallet"
                    val body = "You spent R$ 8.90 at Uber Eats"
                    val extras = Bundle().apply {
                        putBoolean(ExpenseNotificationListener.TEST_EXTRA_KEY, true)
                    }
                    val notif = NotificationCompat.Builder(contxt, ExpenseNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setExtras(extras)
                        .build()
                    val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                    try {
                        if (ContextCompat.checkSelfPermission(
                                contxt,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            NotificationManagerCompat.from(contxt).notify(id, notif)
                        }
                    } catch (_: SecurityException) {
                        // ignore when notifications are denied
                    }

                }) {
                    Text("Test Notification")
                }
            }
        }
    }
}
