package com.example.myapplication.pantallas

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R

data class User(val username: String, val password: String, val name: String, val age: Int, val poem: String)

val mockUsers = listOf(
    User("user1", "pass1", "Pablo Vallejos", 25, "Caminante, son tus huellas\nel camino y nada más;\ncaminante, no hay camino,\nse hace camino al andar."),
    User("user2", "pass2", "Miguel Puebla", 34, "Tus ojos son luceros,\ntus labios, de terciopelo,\ny un amor como el que siento,\nes imposible esconderlo."),
    User("user3", "pass3", "Carolina Martinez", 23, "Puedo escribir los versos más tristes esta noche.\nEscribir, por ejemplo: 'La noche está estrellada,\ny tiritan, azules, los astros, a lo lejos.'")
)

class TextSizeViewModel : ViewModel() {
    val _textSizeMultiplier = mutableStateOf(1f)
    val textSizeMultiplier: State<Float> = _textSizeMultiplier

    fun increaseTextSize() {
        _textSizeMultiplier.value = (_textSizeMultiplier.value * 1.1f).coerceAtMost(2f)
    }

    fun decreaseTextSize() {
        _textSizeMultiplier.value = (_textSizeMultiplier.value * 0.9f).coerceAtLeast(0.5f)
    }
}

@Composable
fun TextSizeIcon(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Int = 16
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: TextSizeViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var textSizeMultiplier by rememberSaveable { mutableStateOf(1.5f) }
    val passwordFocusRequester = remember { FocusRequester() } // Define passwordFocusRequester
    val isDarkTheme = isSystemInDarkTheme() // Detect theme

    // Update the ViewModel's state when the remembered state changes
    LaunchedEffect(textSizeMultiplier) {
        viewModel._textSizeMultiplier.value = textSizeMultiplier
    }

    // Update the remembered state when the ViewModel's state changes
    LaunchedEffect(viewModel.textSizeMultiplier.value) {
        textSizeMultiplier = viewModel.textSizeMultiplier.value
    }

    fun attemptLogin() {
        val user = mockUsers.find { it.username == username && it.password == password }
        if (user != null) {
            navController.navigate("services/${user.username}")
        } else {
            errorMessage = "Usuario o contraseña incorrecto"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regresar a Inicio", color = Color.White, fontSize = 16.sp * textSizeMultiplier) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home", tint = Color.White)
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
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Choose the correct logo based on the current theme
            val logoRes = if (isDarkTheme) R.drawable.logova2 else R.drawable.logova

            Icon(
                painter = painterResource(id = logoRes),
                contentDescription = "App Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(240.dp).padding(bottom = 32.dp)
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario", fontSize = 14.sp * textSizeMultiplier) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp * textSizeMultiplier),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", fontSize = 14.sp * textSizeMultiplier) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp * textSizeMultiplier),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { attemptLogin() })
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { attemptLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp * textSizeMultiplier),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, fontSize = 14.sp * textSizeMultiplier)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.navigate("recoverPassword") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp * textSizeMultiplier,
                        lineHeight = 20.sp * textSizeMultiplier
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

fun getUserData(username: String): User? {
    return mockUsers.find { it.username == username }
}