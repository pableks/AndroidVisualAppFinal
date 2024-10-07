package com.example.myapplication.pantallas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToTextScreen(
    navController: NavController,
    viewModel: TextSizeViewModel = viewModel()
) {
    var textSizeMultiplier by remember { mutableFloatStateOf(viewModel.textSizeMultiplier.value) }
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecordPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.textSizeMultiplier.value) {
        textSizeMultiplier = viewModel.textSizeMultiplier.value
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context))
            SpeechRecognizer.createSpeechRecognizer(context)
        else null
    }
    val intent = remember { Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora")
    } }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasRecordPermission = isGranted
        if (!isGranted) {
            errorMessage = "Se requiere permiso para grabar audio."
        }
    }

    LaunchedEffect(Unit) {
        hasRecordPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voz a Texto",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = recognizedText.ifEmpty { "El texto convertido aparecerá aquí" },
                    color = if (recognizedText.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp * textSizeMultiplier,
                    lineHeight = 20.sp * textSizeMultiplier,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = {
                    if (speechRecognizer == null) {
                        errorMessage = "El reconocimiento de voz no está disponible en este dispositivo."
                    } else if (hasRecordPermission) {
                        isRecording = !isRecording
                        if (isRecording) {
                            try {
                                errorMessage = null
                                speechRecognizer.startListening(intent)
                            } catch (e: Exception) {
                                errorMessage = "Error al iniciar la escucha: ${e.message}"
                                isRecording = false
                            }
                        } else {
                            speechRecognizer.stopListening()
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Alternar grabación",
                    tint = if (isRecording) Color.Red else Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp * textSizeMultiplier,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
                isRecording = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
            }

            override fun onError(error: Int) {
                isRecording = false
                errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se pudo reconocer el habla. Intente de nuevo."
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red. Verifique su conexión."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado."
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio. Verifique su micrófono."
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor. Intente más tarde."
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente. Intente reiniciar la app."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó habla. Intente de nuevo."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado. Intente de nuevo."
                    else -> "Error de reconocimiento de voz: $error"
                }
            }

            // Implement other RecognitionListener methods
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            speechRecognizer?.destroy()
        }
    }
}