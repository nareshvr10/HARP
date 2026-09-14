package com.vrntechnology.harpedge.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("admin@harpedge.gov") }
    var password by remember { mutableStateOf("Admin@1234") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HarpNavyDark)
            .testTag("screen_login")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblem & App Identity
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(HarpTeal.copy(alpha = 0.2f), HarpNavyElevated)
                        )
                    )
                    .border(1.2.dp, HarpTeal.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_harp_logo),
                    contentDescription = "HARP-Edge Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HARP-Edge",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarpTextPrimary,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Hazard-Aware Resonant Pattern Edge Intelligence Network",
                fontSize = 12.sp,
                color = HarpTeal,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\"Detect the Cause. Validate the Risk. Act Early.\"",
                fontSize = 11.sp,
                color = HarpTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error banner if any
            uiState.errorMessage?.let { errorMsg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RiskCritical.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RiskCritical.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RiskCritical,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            color = HarpTextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Credentials Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                border = BorderStroke(1.dp, HarpBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ENVIRONMENTAL ACCESS PORTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearError()
                        },
                        label = { Text("Authorized Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = HarpTeal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedLabelColor = HarpTeal,
                            unfocusedLabelColor = HarpTextSecondary,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.clearError()
                        },
                        label = { Text("Security Passphrase") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = HarpTeal)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = HarpTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                authViewModel.signIn(email, password, onNavigateToDashboard)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedLabelColor = HarpTeal,
                            unfocusedLabelColor = HarpTextSecondary,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                color = HarpCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            authViewModel.signIn(email, password, onNavigateToDashboard)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_sign_in"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HarpTeal,
                            contentColor = HarpNavyDark
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = HarpNavyDark,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTHENTICATE & ENTER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Role Access (For SIH Presentations / Evaluator Demonstrations)
            Text(
                text = "DEMONSTRATION ROLE PRESETS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HarpTextMuted,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        authViewModel.quickRoleSignIn(UserRole.ADMIN, onNavigateToDashboard)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_role_admin"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        authViewModel.quickRoleSignIn(UserRole.AUTHORITY, onNavigateToDashboard)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_role_authority"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HarpCyan.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpCyan),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("AUTHORITY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        authViewModel.quickRoleSignIn(UserRole.VIEWER, onNavigateToDashboard)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_role_viewer"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HarpPurple.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpPurple),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("VIEWER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Password Recovery",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextPrimary
                )
            },
            text = {
                Text(
                    text = "A password recovery token will be dispatched to your agency administrator at $email.",
                    fontSize = 13.sp,
                    color = HarpTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showForgotPasswordDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                ) {
                    Text("OK")
                }
            },
            containerColor = HarpNavyElevated
        )
    }
}
