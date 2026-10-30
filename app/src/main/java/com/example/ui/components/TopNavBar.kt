package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SolverMode
import com.example.ui.AssistantPanelMode
import com.example.ui.FormAssistantViewModel

@Composable
fun TopNavBar(
    viewModel: FormAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUrl by viewModel.currentUrl.collectAsState()
    val canBack by viewModel.canGoBack.collectAsState()
    val canForward by viewModel.canGoForward.collectAsState()
    val solverMode by viewModel.solverMode.collectAsState()
    val splitRatio by viewModel.splitRatio.collectAsState()
    val savedForms by viewModel.savedForms.collectAsState()

    var urlInputText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var showMenu by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isBookmarked = savedForms.any { it.url == currentUrl && it.isFavorite }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding() // Ensures UI is not covered by the system status bar or camera notch!
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Row 1: Back, Forward, URL input bar, Google Sign In, Reload, Options Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.loadUrl("javascript:window.history.back()") },
                    enabled = canBack,
                    modifier = Modifier.size(36.dp).testTag("nav_back_button")
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
                    modifier = Modifier.size(36.dp).testTag("nav_forward_button")
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
                        .height(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(19.dp)
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
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear URL",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Direct Google Sign In / Account Button
                IconButton(
                    onClick = {
                        showGoogleAccountDialog = true
                    },
                    modifier = Modifier.size(36.dp).testTag("google_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Account Sign In",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.loadUrl(currentUrl) },
                    modifier = Modifier.size(36.dp).testTag("nav_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp).testTag("nav_menu_button")
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
                            text = { Text("Sign in to Google Account") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("https://accounts.google.com/signin")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Create Google Account") },
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("https://accounts.google.com/signup")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open in External Chrome") },
                            leadingIcon = {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Science & Tech Quiz (Simulator)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_stem.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("World History Quiz (Simulator)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_history.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logic Reasoning (Simulator)") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("file:///android_asset/practice_logic.html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Google Forms Web") },
                            onClick = {
                                showMenu = false
                                viewModel.loadUrl("https://docs.google.com/forms/u/0/")
                            }
                        )
                    }

                    // Google Account Helper Dropdown / Dialog
                    DropdownMenu(
                        expanded = showGoogleAccountDialog,
                        onDismissRequest = { showGoogleAccountDialog = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign in to Google Account") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                showGoogleAccountDialog = false
                                viewModel.loadUrl("https://accounts.google.com/signin")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Create Google Account") },
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                            },
                            onClick = {
                                showGoogleAccountDialog = false
                                viewModel.loadUrl("https://accounts.google.com/signup")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open in Device Chrome App") },
                            leadingIcon = {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                            },
                            onClick = {
                                showGoogleAccountDialog = false
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Row 2: Mode Chips & Split View Ratio controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 2.dp, end = 2.dp),
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
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (solverMode == SolverMode.EXAM_MODE) Icons.Default.Speed else Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("solver_mode_chip")
                )

                // Split Ratio selector: 50/50, 70/30 (Form focus), 30/70 (AI focus)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AssistChip(
                        onClick = { viewModel.setSplitRatio(0.5f) },
                        label = { Text("50/50", fontSize = 11.sp, fontWeight = if (splitRatio == 0.5f) FontWeight.Bold else FontWeight.Normal) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (splitRatio == 0.5f) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = if (splitRatio == 0.5f) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    AssistChip(
                        onClick = { viewModel.setSplitRatio(0.7f) },
                        label = { Text("Form 70%", fontSize = 11.sp, fontWeight = if (splitRatio == 0.7f) FontWeight.Bold else FontWeight.Normal) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (splitRatio == 0.7f) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = if (splitRatio == 0.7f) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    AssistChip(
                        onClick = { viewModel.setSplitRatio(0.3f) },
                        label = { Text("AI 70%", fontSize = 11.sp, fontWeight = if (splitRatio == 0.3f) FontWeight.Bold else FontWeight.Normal) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (splitRatio == 0.3f) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            labelColor = if (splitRatio == 0.3f) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(currentUrl, !isBookmarked)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Form",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
