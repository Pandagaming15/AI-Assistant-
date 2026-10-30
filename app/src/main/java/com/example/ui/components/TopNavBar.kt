package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SolverMode
import com.example.ui.AssistantPanelMode
import com.example.ui.FormAssistantViewModel
import com.example.web.PracticeForms

@Composable
fun TopNavBar(
    viewModel: FormAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val canBack by viewModel.canGoBack.collectAsState()
    val canForward by viewModel.canGoForward.collectAsState()
    val solverMode by viewModel.solverMode.collectAsState()
    val panelMode by viewModel.panelMode.collectAsState()
    val savedForms by viewModel.savedForms.collectAsState()

    var urlInputText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var showMenu by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isBookmarked = savedForms.any { it.url == currentUrl && it.isFavorite }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Row 1: Back, Forward, URL bar, Reload, Presets Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.loadUrl("javascript:window.history.back()") },
                    enabled = canBack,
                    modifier = Modifier.size(38.dp).testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.loadUrl("javascript:window.history.forward()") },
                    enabled = canForward,
                    modifier = Modifier.size(38.dp).testTag("nav_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // URL Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = urlInputText,
                            onValueChange = { urlInputText = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                viewModel.loadUrl(urlInputText)
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("url_input_field")
                        )

                        if (urlInputText.isNotEmpty()) {
                            IconButton(
                                onClick = { urlInputText = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear URL",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { viewModel.loadUrl(currentUrl) },
                    modifier = Modifier.size(38.dp).testTag("nav_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Presets & Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(38.dp).testTag("nav_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Science & Tech Quiz (Preset)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_stem.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History & Geography Quiz (Preset)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_history.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logic Reasoning Assessment (Preset)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_logic.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Google Forms Gallery") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("https://docs.google.com/forms/u/0/")
                            }
                        )
                    }
                }
            }

            // Row 2: Mode Chips & Layout controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Solver Mode Selector
                AssistChip(
                    onClick = {
                        val nextMode = if (solverMode == SolverMode.EXAM_MODE) {
                            SolverMode.STUDY_MODE
                        } else {
                            SolverMode.EXAM_MODE
                        }
                        viewModel.setSolverMode(nextMode)
                    },
                    label = {
                        Text(
                            text = solverMode.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (solverMode == SolverMode.EXAM_MODE) Icons.Default.Speed else Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("solver_mode_chip")
                )

                // Layout toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val next = when (panelMode) {
                                AssistantPanelMode.SPLIT_VIEW -> AssistantPanelMode.BOTTOM_DRAWER
                                AssistantPanelMode.BOTTOM_DRAWER -> AssistantPanelMode.FULL_ASSISTANT
                                AssistantPanelMode.FULL_ASSISTANT -> AssistantPanelMode.SPLIT_VIEW
                            }
                            viewModel.setPanelMode(next)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle View Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(currentUrl, !isBookmarked)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Form",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
