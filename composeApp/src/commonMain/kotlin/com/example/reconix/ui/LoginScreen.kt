package com.example.reconix.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Design tokens (local — avoids import from theme package) ─
private val NavyDeep    = Color(0xFF060E20)
private val NavyCard    = Color(0xFF0D1B3E)
private val NavyBorder  = Color(0xFF1A3066)
private val RoyalBlue   = Color(0xFF4F7FFF)
private val RoyalLight  = Color(0xFF7EA9FF)
private val EmeraldAccent = Color(0xFF00C896)
private val WhiteLabel  = Color(0xFFFFFFFF)
private val SilverLabel = Color(0xFFB8C8E8)
private val MutedLabel  = Color(0xFF6B82B0)
private val ErrorCrimson = Color(0xFFFF2D5B)

/**
 * ═══════════════════════════════════════════════════════════════
 *  LoginScreen — Premium Fintech Glassmorphism
 *
 *  8dp Grid:
 *    All paddings: 8 / 16 / 24 / 32dp
 *    Card corner: 24dp
 *    Button height: 56dp
 *    Input height: 56dp
 *    Logo size: 80dp
 *    Icon size in logo: 32dp
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit) {
    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // ── Card slide-in animation ──────────────────────────────
    val cardOffset = remember { Animatable(60f) }
    val cardAlpha  = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            launch { cardAlpha.animateTo( 1f, tween(600, easing = FastOutSlowInEasing)) }
            launch { cardOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        }
    }

    // Ambient background glow pulse
    val glowAlpha by rememberInfiniteTransition(label = "bgGlow").animateFloat(
        initialValue = 0.08f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    // ── Shared field styling ─────────────────────────────────
    val fieldShape  = RoundedCornerShape(16.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor    = RoyalBlue,
        unfocusedBorderColor  = NavyBorder,
        focusedLabelColor     = RoyalBlue,
        unfocusedLabelColor   = MutedLabel,
        cursorColor           = RoyalBlue,
        focusedLeadingIconColor   = RoyalBlue,
        unfocusedLeadingIconColor = MutedLabel,
        focusedTrailingIconColor  = RoyalBlue,
        unfocusedTrailingIconColor= MutedLabel,
        focusedTextColor      = WhiteLabel,
        unfocusedTextColor    = SilverLabel,
        focusedContainerColor = NavyCard,
        unfocusedContainerColor = NavyCard
    )

    // ── Root ─────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to NavyDeep,
                        0.4f to Color(0xFF0A1530),
                        1.0f to NavyDeep
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Ambient glow orb (top)
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .alpha(glowAlpha)
                .blur(100.dp)
                .background(RoyalBlue, CircleShape)
        )
        // Ambient glow orb (bottom)
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .alpha(glowAlpha * 0.7f)
                .blur(80.dp)
                .background(EmeraldAccent, CircleShape)
        )

        // ── Login Card ──────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .alpha(cardAlpha.value)
                .offset(y = cardOffset.value.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0D1B3E).copy(alpha = 0.95f),
                                Color(0xFF060E20).copy(alpha = 0.98f)
                            )
                        )
                    )
                    // Luminous top border line effect
                    .then(
                        Modifier.background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x334F7FFF),
                                    Color.Transparent
                                ),
                                endY = 2f
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // ── Logo ──────────────────────────────
                    Box(contentAlignment = Alignment.Center) {
                        // Outer glow
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .alpha(0.20f)
                                .blur(20.dp)
                                .background(
                                    Brush.radialGradient(listOf(RoyalBlue, Color.Transparent)),
                                    CircleShape
                                )
                        )
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF122250),
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF1A3A80).copy(alpha = 0.7f),
                                                Color(0xFF0D1B3E)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "App Logo",
                                    modifier = Modifier.size(32.dp),
                                    tint = RoyalBlue
                                )
                            }
                        }
                        // Emerald accent dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Headline ──────────────────────────
                    Text(
                        text = "Invoice Validator",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteLabel,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "3-Way Match Validation System",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SilverLabel,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Username Field ────────────────────
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = fieldShape,
                        colors = fieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Password Field ────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = fieldShape,
                        colors = fieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    performLogin(username, password, onLoginSuccess,
                                        onLoading = { isLoading = it },
                                        onError = { errorMessage = it }
                                    )
                                }
                            }
                        ),
                        enabled = !isLoading
                    )

                    // ── Error Message (animated) ──────────
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ErrorCrimson.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ErrorCrimson)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        color = ErrorCrimson,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Login Button ──────────────────────
                    Button(
                        onClick = {
                            performLogin(username, password, onLoginSuccess,
                                onLoading = { isLoading = it },
                                onError = { errorMessage = it }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (username.isNotBlank() && password.isNotBlank() && !isLoading)
                                        Brush.linearGradient(listOf(RoyalBlue, Color(0xFF1E3A8A)))
                                    else
                                        Brush.linearGradient(listOf(RoyalBlue.copy(alpha = 0.35f), Color(0xFF1E3A8A).copy(alpha = 0.35f)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = WhiteLabel,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Authenticating...",
                                        color = WhiteLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "Sign In",
                                    color = WhiteLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Divider ───────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NavyBorder)
                        Text("DEMO", fontSize = 10.sp, color = MutedLabel, letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NavyBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Demo Credential Pills ─────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("vendor", "Vendor Portal"),
                            Pair("admin", "Finance Manager"),
                            Pair("demo", "Demo User")
                        ).forEach { (user, label) ->
                            DemoCredentialRow(
                                username = user,
                                label    = label,
                                onClick  = {
                                    username = user
                                    password = "password"
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Password: password  (for all accounts)",
                            fontSize = 11.sp,
                            color = MutedLabel,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// ── Demo credential row component ────────────────────────────
@Composable
private fun DemoCredentialRow(
    username: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF122250),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SilverLabel
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1A3066))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = username,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF7EA9FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Perform login validation against backend API
 */
private fun performLogin(
    username: String,
    password: String,
    onSuccess: (String) -> Unit,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)

    CoroutineScope(Dispatchers.Default).launch {
        try {
            val repository = com.example.reconix.repository.InvoiceRepository()
            val result = repository.login(username, password)

            withContext(Dispatchers.Main) {
                onLoading(false)

                result.onSuccess { loginResponse ->
                    if (loginResponse.success) {
                        onSuccess(username)
                    } else {
                        onError(loginResponse.message)
                    }
                }.onFailure { error ->
                    onError(error.message ?: "Network error. Please check your connection.")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onLoading(false)
                onError("Error: ${e.message}")
            }
        }
    }
}
