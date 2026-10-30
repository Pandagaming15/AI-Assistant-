package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiTutorChatTab
import com.example.ui.components.FormQuestionsListTab
import com.example.ui.components.LiveSolverTab
import com.example.ui.components.SavedHistoryTab
import com.example.ui.components.TopNavBar
import com.example.ui.components.WebBrowserView

data class AssistantTabItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

@Composable
fun FormAssistantScreen(
    viewModel: FormAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val panelMode by viewModel.panelMode.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val detectedQuestions by viewModel.detectedQuestions.collectAsState()

    val tabs = listOf(
        AssistantTabItem("Live Solver", Icons.Default.AutoAwesome),
        AssistantTabItem("Questions", Icons.Default.FormatListNumbered, badgeCount = if (detectedQuestions.isNotEmpty()) detectedQuestions.size else null),
        AssistantTabItem("AI Tutor", Icons.Default.Chat),
        AssistantTabItem("History", Icons.Default.History)
    )

    Scaffold(
        topBar = {
            TopNavBar(viewModel = viewModel)
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                // Wide Screen: Side-by-side Dual Pane Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                    ) {
                        WebBrowserView(viewModel = viewModel)
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        AssistantPanelContent(
                            viewModel = viewModel,
                            selectedTab = selectedTab,
                            tabs = tabs,
                            onTabSelected = { viewModel.setSelectedTab(it) }
                        )
                    }
                }
            } else {
                // Mobile Portrait Layout
                when (panelMode) {
                    AssistantPanelMode.SPLIT_VIEW -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Upper: Web browser (48%)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.48f)
                            ) {
                                WebBrowserView(viewModel = viewModel)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Lower: Assistant Panel (52%)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.52f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                AssistantPanelContent(
                                    viewModel = viewModel,
                                    selectedTab = selectedTab,
                                    tabs = tabs,
                                    onTabSelected = { viewModel.setSelectedTab(it) }
                                )
                            }
                        }
                    }
                    AssistantPanelMode.BOTTOM_DRAWER -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.68f)
                            ) {
                                WebBrowserView(viewModel = viewModel)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.32f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                AssistantPanelContent(
                                    viewModel = viewModel,
                                    selectedTab = selectedTab,
                                    tabs = tabs,
                                    onTabSelected = { viewModel.setSelectedTab(it) }
                                )
                            }
                        }
                    }
                    AssistantPanelMode.FULL_ASSISTANT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            AssistantPanelContent(
                                viewModel = viewModel,
                                selectedTab = selectedTab,
                                tabs = tabs,
                                onTabSelected = { viewModel.setSelectedTab(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantPanelContent(
    viewModel: FormAssistantViewModel,
    selectedTab: Int,
    tabs: List<AssistantTabItem>,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth().testTag("assistant_tab_row")
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (tab.badgeCount != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${tab.badgeCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Active Tab Screen Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> LiveSolverTab(viewModel = viewModel)
                1 -> FormQuestionsListTab(viewModel = viewModel)
                2 -> AiTutorChatTab(viewModel = viewModel)
                3 -> SavedHistoryTab(viewModel = viewModel)
            }
        }
    }
}
