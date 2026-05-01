package com.example.chefmateapp.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefmateapp.R
import com.example.chefmateapp.ui.theme.*
import com.example.chefmateapp.viewmodel.AuthViewModel
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState

@Composable
fun AuthScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) { page ->
        if (page == 0) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1.7f).fillMaxWidth()) {
                    LoginContent(viewModel)
                }
                Box(modifier = Modifier.weight(0.3f).fillMaxWidth()) {
                    SidePanel(
                        title = "¡Hola, amigo!",
                        subtitle = "Desliza hacia arriba para registrarte",
                        isTop = false
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.3f).fillMaxWidth()) {
                    SidePanel(
                        title = "¡Bienvenido!",
                        subtitle = "Desliza hacia abajo para iniciar sesión",
                        isTop = true
                    )
                }
                Box(modifier = Modifier.weight(1.7f).fillMaxWidth()) {
                    RegisterContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginContent(viewModel: AuthViewModel) {
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val captcha by viewModel.captchaResponse.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val generalError by viewModel.loginGeneralError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isValid by viewModel.isLoginValid.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo + Brand
        Image(
            painter = painterResource(id = R.drawable.logo_chef_app),
            contentDescription = "ChefMate Logo",
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ChefMate",
            fontFamily = Poppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Primary
        )
        Text(
            text = "Iniciar Sesión",
            style = AppTypography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email field
        AppTextField(
            value = email,
            onValueChange = { viewModel.onLoginEmailChange(it) },
            label = "Correo Electrónico",
            isEmail = true,
            isError = emailError != null,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password field
        AppTextField(
            value = password,
            onValueChange = { viewModel.onLoginPasswordChange(it) },
            label = "Contraseña",
            isError = passwordError != null,
            errorMessage = passwordError,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(20.dp))

        CaptchaBox(
            isVerified = captcha.isNotEmpty(),
            onClick = { viewModel.onCaptchaChange("verified") }
        )

        AnimatedVisibility(visible = generalError != null) {
            generalError?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = Danger,
                    style = AppTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = BorderMedium
            ),
            enabled = isValid && !isLoading,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Iniciando…", color = Color.White, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    "INICIAR SESIÓN",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun RegisterContent(viewModel: AuthViewModel) {
    val name by viewModel.registerName.collectAsState()
    val email by viewModel.registerEmail.collectAsState()
    val password by viewModel.registerPassword.collectAsState()
    val strength by viewModel.passwordStrength.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val generalError by viewModel.registerGeneralError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isValid by viewModel.isRegisterValid.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_chef_app),
            contentDescription = "ChefMate Logo",
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ChefMate",
            fontFamily = Poppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Primary
        )
        Text(
            text = "Crear Cuenta",
            style = AppTypography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = name,
            onValueChange = { viewModel.onRegisterNameChange(it) },
            label = "Nombre Completo"
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppTextField(
            value = email,
            onValueChange = { viewModel.onRegisterEmailChange(it) },
            label = "Correo Electrónico",
            isEmail = true,
            isError = emailError != null,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppTextField(
            value = password,
            onValueChange = { viewModel.onRegisterPasswordChange(it) },
            label = "Contraseña",
            isError = passwordError != null,
            errorMessage = passwordError,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(10.dp))
        PasswordStrengthMeter(strength)

        AnimatedVisibility(visible = generalError != null) {
            generalError?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = Danger,
                    style = AppTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = BorderMedium
            ),
            enabled = isValid && !isLoading,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Registrando…", color = Color.White, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    "REGISTRARSE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    isEmail: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    val keyboardOptions = when {
        isPassword -> KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
        isEmail -> KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        else -> KeyboardOptions.Default
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = AppTypography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            isError = isError,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onTogglePassword?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (isError) Danger else TextMuted
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderMedium,
                errorBorderColor = Danger,
                focusedLabelColor = Primary,
                errorLabelColor = Danger,
                focusedContainerColor = Surface,
                unfocusedContainerColor = SurfaceSecondary
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = Danger,
                    style = AppTypography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp, top = 3.dp)
                )
            }
        }
    }
}

@Composable
fun SidePanel(title: String, subtitle: String, isTop: Boolean) {
    val shape = if (isTop) {
        RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
    } else {
        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Primary, Color(0xFF059669))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                style = AppTypography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CaptchaBox(isVerified: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isVerified) Primary else BorderMedium,
        animationSpec = tween(300),
        label = "captcha_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isVerified) PrimaryLight else Surface,
        animationSpec = tween(300),
        label = "captcha_bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(targetState = isVerified, label = "check_anim") { verified ->
                if (verified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, BorderMedium, RoundedCornerShape(6.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "No soy un robot",
                style = AppTypography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("reCAPTCHA", fontSize = 8.sp, color = TextMuted)
                Text("Privacidad", fontSize = 6.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun PasswordStrengthMeter(strength: Float) {
    val color by animateColorAsState(
        targetValue = when {
            strength < 0.3f -> Danger
            strength < 0.7f -> Warning
            else -> Primary
        },
        animationSpec = tween(400),
        label = "strength_color"
    )
    val label = when {
        strength < 0.3f -> "Débil"
        strength < 0.7f -> "Media"
        else -> "Fuerte"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seguridad de contraseña",
                style = AppTypography.labelMedium,
                color = TextMuted
            )
            Text(
                text = label,
                style = AppTypography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = BorderLight,
        )
    }
}