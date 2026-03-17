package com.example.cityhistory.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cityhistory.ui.theme.CityHistoryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityHistoryScreen(
    viewModel: CityHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var apiKey by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }

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

            Button(
                onClick = { viewModel.fetchHistory(apiKey, city) },
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Text(
                            text = state.history,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
                history = "Rome, founded in 753 BC according to tradition, grew from a pastoral community on the Tiber River into the capital of one of the largest empires the world has ever seen. The Roman Republic, established in 509 BC, expanded across the Mediterranean through military prowess and sophisticated governance. At its height, the Roman Empire stretched from Britain to Mesopotamia, spreading Latin language, Roman law, and Greco-Roman culture across Europe and beyond.",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Text(
                            text = uiState.history,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
