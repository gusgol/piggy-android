package com.goldhardt.feature.trends

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.goldhardt.designsystem.components.ConfigureTopBar

@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
) {
    ConfigureTopBar(title = stringResource(R.string.title_trends))
    var selectedTab by rememberSaveable { mutableStateOf(TrendsTab.Current) }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth.coerceAtMost(680.dp)
        Column(
            modifier = Modifier.widthIn(max = contentWidth).fillMaxHeight().align(Alignment.TopCenter),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                TrendsTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(stringResource(tab.titleRes)) },
                    )
                }
            }
            when (selectedTab) {
                TrendsTab.Current -> CurrentTrendsScreen(Modifier.weight(1f))
                TrendsTab.Year -> YearTrendsScreen(Modifier.weight(1f))
            }
        }
    }
}

private enum class TrendsTab(val titleRes: Int) {
    Current(R.string.tab_current),
    Year(R.string.tab_year),
}
