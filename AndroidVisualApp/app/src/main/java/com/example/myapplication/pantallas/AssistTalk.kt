package com.example.myapplication.pantallas

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistTalkScreen(
    navController: NavController,
    viewModel: TextSizeViewModel = viewModel()
) {
    var textSizeMultiplier by remember { mutableFloatStateOf(viewModel.textSizeMultiplier.value) }
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(viewModel.textSizeMultiplier.value) {
        textSizeMultiplier = viewModel.textSizeMultiplier.value
    }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lectura Asistida",
                        fontSize = 18.sp * textSizeMultiplier,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextSizeIcon(
                        text = "A",
                        onClick = {
                            viewModel.decreaseTextSize()
                            textSizeMultiplier = viewModel.textSizeMultiplier.value
                        },
                        fontSize = 14
                    )
                    TextSizeIcon(
                        text = "A",
                        onClick = {
                            viewModel.increaseTextSize()
                            textSizeMultiplier = viewModel.textSizeMultiplier.value
                        },
                        fontSize = 18
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Ingrese el texto a leer", fontSize = 16.sp * textSizeMultiplier) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp * textSizeMultiplier)
            )

            Button(
                onClick = {
                    tts?.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Leer texto",
                    fontSize = 16.sp * textSizeMultiplier
                )
            }
        }
    }
}