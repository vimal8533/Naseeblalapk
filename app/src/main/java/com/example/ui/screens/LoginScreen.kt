package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.MarketLogoMedallion
import com.example.ui.components.MoolStoneBrandingFooter
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPending

@Composable
fun LoginScreen(
    onLogin: (String, String, UserRole) -> Result<UserSession>,
    onOpenTenantPortal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val isAdmin = selectedRole == UserRole.ADMIN

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040810),
                        NavyDark,
                        Color(0xFF0C1322)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Refined Market Emblem
            MarketLogoMedallion(
                size = 78.dp,
                showSubtext = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Clean Typography Header
            Text(
                text = "Naseeb Lal Market",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sign in to manage shops & rent records",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 3. Elegant Glass-Style Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF1E2D4A),
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0B1324).copy(alpha = 0.94f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Role Segmented Pill Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070C18))
                            .border(1.dp, Color(0xFF1B2842), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Master Admin Tab
                        val adminBgColor by animateColorAsState(
                            targetValue = if (isAdmin) GoldAccent else Color.Transparent,
                            animationSpec = tween(220),
                            label = "admin_pill_bg"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(adminBgColor)
                                .clickable {
                                    if (selectedRole != UserRole.ADMIN) {
                                        selectedRole = UserRole.ADMIN
                                        errorMessage = null
                                        password = ""
                                    }
                                }
                                .padding(vertical = 10.dp)
                                .testTag("login_role_master_admin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = "Master Admin",
                                    tint = if (isAdmin) NavyDark else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Master Admin",
                                    color = if (isAdmin) NavyDark else Color.White.copy(alpha = 0.85f),
                                    fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Sub Admin Tab
                        val subAdminBgColor by animateColorAsState(
                            targetValue = if (!isAdmin) GoldAccent else Color.Transparent,
                            animationSpec = tween(220),
                            label = "subadmin_pill_bg"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(subAdminBgColor)
                                .clickable {
                                    if (selectedRole != UserRole.SUB_ADMIN) {
                                        selectedRole = UserRole.SUB_ADMIN
                                        errorMessage = null
                                        password = ""
                                    }
                                }
                                .padding(vertical = 10.dp)
                                .testTag("login_role_sub_admin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Sub Admin",
                                    tint = if (!isAdmin) NavyDark else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sub-Admin",
                                    color = if (!isAdmin) NavyDark else Color.White.copy(alpha = 0.85f),
                                    fontWeight = if (!isAdmin) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username Input Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = {
                            Text(if (isAdmin) "Master Admin Username" else "Sub-Admin Username")
                        },
                        placeholder = {
                            Text(
                                text = if (isAdmin) "Enter Master Admin username" else "Enter Sub-Admin username",
                                color = Color.White.copy(alpha = 0.35f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isAdmin) Icons.Filled.Shield else Icons.Filled.Person,
                                contentDescription = "User",
                                tint = if (username.isNotEmpty()) GoldAccent else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF1E2D4A),
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldAccent,
                            focusedContainerColor = Color(0xFF070C18).copy(alpha = 0.75f),
                            unfocusedContainerColor = Color(0xFF070C18).copy(alpha = 0.45f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = {
                            Text("Password")
                        },
                        placeholder = {
                            Text(
                                text = "Enter password",
                                color = Color.White.copy(alpha = 0.35f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password",
                                tint = if (password.isNotEmpty()) GoldAccent else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                val result = onLogin(username, password, selectedRole)
                                if (result.isFailure) {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF1E2D4A),
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldAccent,
                            focusedContainerColor = Color(0xFF070C18).copy(alpha = 0.75f),
                            unfocusedContainerColor = Color(0xFF070C18).copy(alpha = 0.45f)
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusPending.copy(alpha = 0.15f))
                                .border(1.dp, StatusPending.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFFB4AB),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Sign-In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val result = onLogin(username, password, selectedRole)
                            if (result.isFailure) {
                                errorMessage = result.exceptionOrNull()?.message ?: "Invalid credentials!"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = NavyDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. WhatsApp-style Branding Footer with MoolStone Logo
            MoolStoneBrandingFooter(
                logoSize = 22.dp,
                nameColor = GoldAccent,
                fromColor = Color.White.copy(alpha = 0.45f),
                testTagPrefix = "login_brand"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

