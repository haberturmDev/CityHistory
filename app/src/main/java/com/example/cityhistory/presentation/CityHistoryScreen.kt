package com.example.cityhistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cityhistory.ui.theme.CityHistoryTheme

private const val DEFAULT_MAX_TOKENS = "250"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityHistoryScreen(
    viewModel: CityHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var apiKey by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var maxTokensText by rememberSaveable { mutableStateOf(DEFAULT_MAX_TOKENS) }
    var stopSequencesText by rememberSaveable { mutableStateOf("") }
    var modelSettingsExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("City History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Groq API Key") },
                placeholder = { Text("gsk_...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            imageVector = if (apiKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (apiKeyVisible) "Hide API key" else "Show API key",
                        )
                    }
                },
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City Name") },
                placeholder = { Text("e.g. Rome, Tokyo, Cairo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ModelSettingsAccordion(
                expanded = modelSettingsExpanded,
                onToggle = { modelSettingsExpanded = !modelSettingsExpanded },
                maxTokensText = maxTokensText,
                onMaxTokensChange = { maxTokensText = it },
                stopSequencesText = stopSequencesText,
                onStopSequencesChange = { stopSequencesText = it },
            )

            Button(
                onClick = {
                    val maxTokens = maxTokensText.trim().toIntOrNull()
                        ?.coerceIn(1, 32_768) ?: DEFAULT_MAX_TOKENS.toInt()
                    val stopSequences = stopSequencesText
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    viewModel.fetchHistory(apiKey, city, maxTokens, stopSequences)
                },
                enabled = uiState !is CityHistoryUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Get History")
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (val state = uiState) {
                is CityHistoryUiState.Idle -> Unit

                is CityHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is CityHistoryUiState.Success -> {
                    CityHistoryResult(history = state.history)
                }

                is CityHistoryUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private val SECTION_LABELS = listOf("Brief History", "Modern state of city", "Random fact")

private fun parseHistorySections(raw: String): List<Pair<String, String>> {
    return SECTION_LABELS.mapNotNull { label ->
        val prefix = "##$label:"
        val start = raw.indexOf(prefix).takeIf { it >= 0 } ?: return@mapNotNull null
        val contentStart = start + prefix.length
        val nextSection = SECTION_LABELS
            .filter { it != label }
            .mapNotNull { other ->
                val idx = raw.indexOf("##$other:", contentStart)
                if (idx >= 0) idx else null
            }
            .minOrNull() ?: raw.length
        val content = raw.substring(contentStart, nextSection).trim()
        label to content
    }
}

@Composable
private fun CityHistoryResult(history: String) {
    val sections = parseHistorySections(history)
    if (sections.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(
                text = history,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEach { (label, content) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSettingsAccordion(
    expanded: Boolean,
    onToggle: () -> Unit,
    maxTokensText: String,
    onMaxTokensChange: (String) -> Unit,
    stopSequencesText: String,
    onStopSequencesChange: (String) -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Model Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse model settings" else "Expand model settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = maxTokensText,
                    onValueChange = onMaxTokensChange,
                    label = { Text("Max Tokens") },
                    placeholder = { Text(DEFAULT_MAX_TOKENS) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Controls the maximum length of the response") },
                )

                OutlinedTextField(
                    value = stopSequencesText,
                    onValueChange = onStopSequencesChange,
                    label = { Text("Stop Sequences") },
                    placeholder = { Text("e.g. END, \\n\\n") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Comma-separated strings where the model will stop generating") },
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CityHistoryScreenIdlePreview() {
    CityHistoryTheme {
        CityHistoryScreenContent(uiState = CityHistoryUiState.Idle)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CityHistoryScreenSuccessPreview() {
    CityHistoryTheme {
        CityHistoryScreenContent(
            uiState = CityHistoryUiState.Success(
                history = "##Brief History: Rome, founded in 753 BC according to tradition, grew from a pastoral community into the capital of one of the largest empires the world has ever seen. The Roman Republic expanded across the Mediterranean through military prowess and sophisticated governance.\n##Modern state of city: Today Rome is Italy's capital and largest city, a vibrant metropolis of nearly 3 million people blending ancient monuments with a lively modern culture.\n##Random fact: Rome has more CCTV cameras per capita than any other city in Europe, many of them hidden inside replicas of ancient statues.",
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CityHistoryScreenErrorPreview() {
    CityHistoryTheme {
        CityHistoryScreenContent(uiState = CityHistoryUiState.Error("Invalid API key. Please check and try again."))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityHistoryScreenContent(uiState: CityHistoryUiState) {
    var apiKeyVisible by remember { mutableStateOf(false) }
    var modelSettingsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("City History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Groq API Key") },
                placeholder = { Text("gsk_...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            imageVector = if (apiKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (apiKeyVisible) "Hide API key" else "Show API key",
                        )
                    }
                },
            )

            OutlinedTextField(
                value = if (uiState is CityHistoryUiState.Success) "Rome" else "",
                onValueChange = {},
                label = { Text("City Name") },
                placeholder = { Text("e.g. Rome, Tokyo, Cairo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ModelSettingsAccordion(
                expanded = modelSettingsExpanded,
                onToggle = { modelSettingsExpanded = !modelSettingsExpanded },
                maxTokensText = DEFAULT_MAX_TOKENS,
                onMaxTokensChange = {},
                stopSequencesText = "",
                onStopSequencesChange = {},
            )

            Button(
                onClick = {},
                enabled = uiState !is CityHistoryUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Get History")
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (uiState) {
                is CityHistoryUiState.Idle -> Unit

                is CityHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is CityHistoryUiState.Success -> {
                    CityHistoryResult(history = uiState.history)
                }

                is CityHistoryUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
