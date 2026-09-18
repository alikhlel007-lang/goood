package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Strings
import com.example.ui.viewmodel.CafeViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onLoginSuccess: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var email by remember { mutableStateOf("admin@cafe.com") }
    var password by remember { mutableStateOf("123456") }
    var ownerName by remember { mutableStateOf("") }
    var cafeName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // OTP Verification State
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var resendCooldown by remember { mutableIntStateOf(60) }

    // Cooldown timer for OTP resend
    LaunchedEffect(isVerifyingOtp, resendCooldown) {
        if (isVerifyingOtp && resendCooldown > 0) {
            delay(1000L)
            resendCooldown -= 1
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App Branding - App Icon
        Image(
            painter = painterResource(id = R.drawable.cafe_qr_app_icon),
            contentDescription = Strings.get("app_name", lang),
            modifier = Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5A93C).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // If verifying OTP step
                if (isVerifyingOtp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                isVerifyingOtp = false
                                errorMessage = null
                                successMessage = null
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.get("verification_code", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Strings.get("enter_code_sent_to", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email.trim(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6-digit OTP code input
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otpInput = it },
                        label = { Text(Strings.get("verification_code", lang)) },
                        placeholder = { Text("000000") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = successMessage ?: "",
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (otpInput.length != 6) {
                                errorMessage = "يرجى إدخال الرمز المكون من 6 أرقام كاملاً"
                                return@Button
                            }
                            errorMessage = null
                            isLoading = true
                            viewModel.verifyOtpAndCompleteRegistration(
                                enteredCode = otpInput,
                                onSuccess = {
                                    isLoading = false
                                    onLoginSuccess()
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage = it
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("otp_verify_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && otpInput.length == 6
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = Strings.get("verify_and_continue", lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend Button with Cooldown
                    OutlinedButton(
                        onClick = {
                            if (resendCooldown == 0 && !isLoading) {
                                errorMessage = null
                                isLoading = true
                                viewModel.resendOtp(
                                    onCodeSent = {
                                        isLoading = false
                                        resendCooldown = 60
                                        successMessage = Strings.get("code_sent_success", lang)
                                    },
                                    onError = {
                                        isLoading = false
                                        errorMessage = it
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = resendCooldown == 0 && !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (resendCooldown > 0) {
                            Text(
                                text = "${Strings.get("resend_in", lang)} $resendCooldown ${Strings.get("seconds", lang)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = Strings.get("resend_code", lang),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                } else {
                    // Normal Login / Register tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; errorMessage = null; successMessage = null },
                            text = { Text(Strings.get("login", lang), fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; errorMessage = null; successMessage = null },
                            text = { Text(Strings.get("register", lang), fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(Strings.get("owner_name", lang)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_name_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cafeName,
                            onValueChange = { cafeName = it },
                            label = { Text(Strings.get("cafe_name", lang)) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cafe_name_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Strings.get("email", lang)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Strings.get("password", lang)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            if (selectedTab == 0) {
                                isLoading = true
                                viewModel.login(
                                    email = email,
                                    pass = password,
                                    onSuccess = {
                                        isLoading = false
                                        onLoginSuccess()
                                    },
                                    onError = {
                                        isLoading = false
                                        errorMessage = it
                                    }
                                )
                            } else {
                                if (ownerName.isBlank() || cafeName.isBlank() || email.isBlank() || password.isBlank()) {
                                    errorMessage = "يرجى ملء جميع الحقول المطلوبة"
                                    return@Button
                                }
                                isLoading = true
                                viewModel.sendRegistrationOtp(
                                    name = ownerName,
                                    email = email,
                                    pass = password,
                                    cafeName = cafeName,
                                    onDirectSuccess = {
                                        isLoading = false
                                        onLoginSuccess()
                                    },
                                    onCodeSent = {
                                        isLoading = false
                                        isVerifyingOtp = true
                                        otpInput = ""
                                        resendCooldown = 60
                                        successMessage = Strings.get("code_sent_success", lang)
                                    },
                                    onError = {
                                        isLoading = false
                                        errorMessage = it
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) Strings.get("login", lang) else Strings.get("register", lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
