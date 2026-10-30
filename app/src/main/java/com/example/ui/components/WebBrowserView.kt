package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.AnalysisState
import com.example.ui.FormAssistantViewModel
import com.example.web.FormJavaScriptBridge
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebBrowserView(
    viewModel: FormAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val progress by viewModel.pageLoadingProgress.collectAsState()
    val selectedText by viewModel.selectedWebText.collectAsState()
    val detectedQuestions by viewModel.detectedQuestions.collectAsState()
    val activeIndex by viewModel.activeQuestionIndex.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    val jsBridge = remember {
        FormJavaScriptBridge(
            coroutineScope = coroutineScope,
            onQuestionsDetectedCallback = { json ->
                viewModel.onQuestionsDetected(json)
            },
            onActiveQuestionChangedCallback = { json ->
                // Handled if needed
            },
            onTextSelectedCallback = { text ->
                viewModel.onTextSelectedInWeb(text)
            }
        )
    }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                allowFileAccess = true
                allowContentAccess = true
                userAgentString = settings.userAgentString + " FormAssistant/1.0"
            }

            addJavascriptInterface(jsBridge, "AndroidFormBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.updateWebState(
                        url = view?.url ?: currentUrl,
                        title = view?.title ?: "",
                        progress = newProgress,
                        canBack = view?.canGoBack() == true,
                        canForward = view?.canGoForward() == true
                    )
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    viewModel.updateWebState(
                        url = url ?: "",
                        title = view?.title ?: "",
                        progress = 10,
                        canBack = view?.canGoBack() == true,
                        canForward = view?.canGoForward() == true
                    )
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewModel.updateWebState(
                        url = url ?: "",
                        title = view?.title ?: "",
                        progress = 100,
                        canBack = view?.canGoBack() == true,
                        canForward = view?.canGoForward() == true
                    )
                    // Inject scanning script
                    view?.evaluateJavascript(FormJavaScriptBridge.INJECTION_SCRIPT, null)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }
        }
    }

    // Load URL when currentUrl changes
    LaunchedEffect(currentUrl) {
        if (webView.url != currentUrl) {
            webView.loadUrl(currentUrl)
        }
    }

    // Listen to JS commands emitted from ViewModel
    LaunchedEffect(Unit) {
        viewModel.jsCommandEvents.collectLatest { jsCode ->
            webView.evaluateJavascript(jsCode, null)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        // Web Loading progress indicator
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
            )
        }

        // Floating Quick Assistant Action Pill when questions are detected
        AnimatedVisibility(
            visible = detectedQuestions.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.solveCurrentQuestion()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Solve with AI"
                    )
                },
                text = {
                    val statusText = when (analysisState) {
                        is AnalysisState.Analyzing -> "Analyzing Q${activeIndex + 1}..."
                        is AnalysisState.Success -> "Q${activeIndex + 1} Solved ✓"
                        else -> "Solve Q${activeIndex + 1}"
                    }
                    Text(
                        text = statusText,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("floating_solve_button")
            )
        }

        // Floating Prompt when user highlights text on the webview
        AnimatedVisibility(
            visible = !selectedText.isNullOrBlank(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Ask AI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "\"${selectedText?.take(25)}...\"",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        FilledTonalButton(
                            onClick = { viewModel.askAboutSelectedText() },
                            modifier = Modifier.padding(start = 8.dp).height(36.dp)
                        ) {
                            Text("Ask AI", style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(
                            onClick = { viewModel.clearSelectedWebText() },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss"
                            )
                        }
                    }
                }
            }
        }
    }
}
