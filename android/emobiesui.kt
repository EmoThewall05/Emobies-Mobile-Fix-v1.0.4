package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.BiometricHelper
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Repair
import com.example.data.UserStats
import com.example.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EmobiesAppContent(viewModel: MainViewModel) {
    val isAuthed by viewModel.isAuthed.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = isAuthed,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
        },
        label = "auth_screen_switch"
    ) { authed ->
        if (authed) {
            MainShellScreen(viewModel = viewModel)
        } else {
            LoginScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context as FragmentActivity) }
    var passwordText by remember { mutableStateOf("") }
    // ...

    var obscureText by remember { mutableStateOf(true) }
    
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val lockUntil by viewModel.lockUntil.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.secondsLeft.collectAsStateWithLifecycle()

    val isLocked = lockUntil > System.currentTimeMillis()

    // Shake animation state for password on error
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmobiesBg)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.value.dp)
        ) {
            // Hexagon icon
            Text(
                text = "⬡",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    color = EmobiesOrange,
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.testTag("app_logo_icon")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rich Title
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = EmobiesOrange, fontWeight = FontWeight.Black)) {
                        append("E")
                    }
                    withStyle(style = SpanStyle(color = EmobiesText, fontWeight = FontWeight.Bold)) {
                        append("mobies")
                    }
                },
                fontSize = 38.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.testTag("app_title")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryPill(label = "📱 Mobile Repair", color = EmobiesGreen)
                CategoryPill(label = "🔐 TheWall", color = EmobiesOrange)
                CategoryPill(label = "🤖 Emowall AI", color = EmobiesPurple)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle metadata
            Text(
                text = "KANNUR · DUBAI · DIVIN K.K.",
                color = EmobiesMuted,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Password Input Box
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                enabled = !isLocked && !authLoading,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = EmobiesText,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                ),
                placeholder = {
                    Text(
                        text = if (isLocked) "Locked — wait ${secondsLeft}s" else "Enter Password",
                        color = EmobiesMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                visualTransformation = if (obscureText) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { obscureText = !obscureText }) {
                        Icon(
                            imageVector = if (obscureText) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = EmobiesMuted
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = EmobiesSurface,
                    unfocusedContainerColor = EmobiesSurface,
                    disabledContainerColor = EmobiesSurface.copy(alpha = 0.5f),
                    focusedBorderColor = EmobiesOrange,
                    unfocusedBorderColor = EmobiesBorder,
                    disabledBorderColor = EmobiesBorder
                ),
                shape = RoundedCornerShape(13.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (!isLocked && !authLoading) {
                        viewModel.login(passwordText)
                    }
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Unlock Button
            Button(
                onClick = { viewModel.login(passwordText) },
                enabled = !isLocked && !authLoading && passwordText.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmobiesOrange,
                    contentColor = Color.White,
                    disabledContainerColor = EmobiesOrange.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("unlock_button")
            ) {
                if (authLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (isLocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LockClock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Locked · ${secondsLeft}s", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("⬡  Unlock Emobies", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fingerprint / Biometric Login Button
            OutlinedButton(
                onClick = {
                    biometricHelper.showBiometricPrompt(
                        title = "Authenticate",
                        subtitle = "Log in with biometric",
                        negativeButtonText = "Use Password",
                        onSuccess = { viewModel.loginWithBiometric() },
                        onError = { /* Handle error if needed */ }
                    )
                },
                enabled = !isLocked && !authLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmobiesText2
                ),
                border = BorderStroke(1.dp, EmobiesBorder),
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("fingerprint_login_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = EmobiesText2
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("☝  Fingerprint Login", fontWeight = FontWeight.Medium)
                }
            }

            // Error display / locks
            authError?.let { err ->
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLocked) {
                    LockBanner(secondsLeft = secondsLeft)
                } else {
                    Text(
                        text = err,
                        color = EmobiesRed,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("error_message_text")
                    )
                }
            }
        }
    }
}

@Composable
fun LockBanner(secondsLeft: Int) {
    val fraction = secondsLeft / 30f
    
    Card(
        colors = CardDefaults.cardColors(containerColor = EmobiesRed.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, EmobiesRed.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmobiesRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Too many attempts — wait ${secondsLeft}s",
                    color = EmobiesRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            LinearProgressIndicator(
                progress = { fraction },
                color = EmobiesRed,
                trackColor = EmobiesRed.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun CategoryPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MainShellScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = EmobiesSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(BorderStroke(1.dp, EmobiesBorder), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmobiesOrange,
                        selectedTextColor = EmobiesOrange,
                        indicatorColor = EmobiesOrange.copy(alpha = 0.15f),
                        unselectedIconColor = EmobiesText2,
                        unselectedTextColor = EmobiesText2
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.Build else Icons.Outlined.Build, contentDescription = "Repairs") },
                    label = { Text("Repairs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmobiesOrange,
                        selectedTextColor = EmobiesOrange,
                        indicatorColor = EmobiesOrange.copy(alpha = 0.15f),
                        unselectedIconColor = EmobiesText2,
                        unselectedTextColor = EmobiesText2
                    ),
                    modifier = Modifier.testTag("nav_tab_repairs")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Filled.Toll else Icons.Outlined.Toll, contentDescription = "EmoCoins") },
                    label = { Text("EmoCoins") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmobiesOrange,
                        selectedTextColor = EmobiesOrange,
                        indicatorColor = EmobiesOrange.copy(alpha = 0.15f),
                        unselectedIconColor = EmobiesText2,
                        unselectedTextColor = EmobiesText2
                    ),
                    modifier = Modifier.testTag("nav_tab_coins")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet, contentDescription = "TheWall") },
                    label = { Text("TheWall") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmobiesOrange,
                        selectedTextColor = EmobiesOrange,
                        indicatorColor = EmobiesOrange.copy(alpha = 0.15f),
                        unselectedIconColor = EmobiesText2,
                        unselectedTextColor = EmobiesText2
                    ),
                    modifier = Modifier.testTag("nav_tab_thewall")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmobiesBg)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(viewModel = viewModel)
                1 -> RepairsTab(viewModel = viewModel)
                2 -> EmoCoinsTab(viewModel = viewModel)
                3 -> TheWallTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DashboardTab(viewModel: MainViewModel) {
    val repairs by viewModel.repairs.collectAsStateWithLifecycle()
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    
    val geminiInsight by viewModel.geminiInsight.collectAsStateWithLifecycle()
    val geminiLoading by viewModel.geminiLoading.collectAsStateWithLifecycle()
    
    val activeRepairsCount = repairs.count { it.status != "Done ✓" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = EmobiesOrange, fontWeight = FontWeight.Black)) {
                            append("E")
                        }
                        withStyle(style = SpanStyle(color = EmobiesText, fontWeight = FontWeight.Bold)) {
                            append("mobies")
                        }
                    },
                    fontSize = 24.sp,
                )
                Text(
                    text = "Kannur → Dubai",
                    color = EmobiesMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(EmobiesGreen.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, EmobiesGreen.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "● STABLE",
                    color = EmobiesGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Three stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Active Repairs",
                value = activeRepairsCount.toString(),
                color = EmobiesOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "EmoCoins",
                value = stats?.coins?.toString() ?: "—",
                color = EmobiesPurple,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "APK Installs",
                value = "847",
                color = EmobiesGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Emowall AI Section
        Card(
            colors = CardDefaults.cardColors(containerColor = EmobiesCard),
            border = BorderStroke(1.dp, EmobiesBorder),
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("emowall_ai_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🤖 Emowall AI Insights",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = EmobiesPurple,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = EmobiesRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = geminiInsight ?: "Generate a real-time portfolio analysis of repairs, locations, and stashed assets using Gemini LLM reasoning.",
                    color = EmobiesText2,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.generateAiInsights() },
                    enabled = !geminiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmobiesPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (geminiLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Portfolio Analysis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(EmobiesCard, RoundedCornerShape(13.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.2f)), RoundedCornerShape(13.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = EmobiesMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun RepairsTab(viewModel: MainViewModel) {
    val repairs by viewModel.repairs.collectAsStateWithLifecycle()
    val apiRefreshing by viewModel.apiRefreshing.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmobiesOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_repair_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Repair")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Repair", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EmobiesBg)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Repairs",
                    color = EmobiesText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                IconButton(
                    onClick = { viewModel.refreshRepairs() },
                    enabled = !apiRefreshing
                ) {
                    if (apiRefreshing) {
                        CircularProgressIndicator(color = EmobiesOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh live repairs list", tint = EmobiesOrange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (repairs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active repairs", color = EmobiesMuted, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(repairs) { repair ->
                        RepairCard(repair = repair)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNewRepairDialog(
            onDismiss = { showAddDialog = false },
            onSave = { device, location, status ->
                viewModel.addNewRepair(device, location, status)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RepairCard(repair: Repair) {
    val statusColor = when (repair.status) {
        "Done ✓" -> EmobiesGreen
        "In Progress" -> EmobiesYellow
        else -> EmobiesBlue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmobiesCard, RoundedCornerShape(13.dp))
            .border(BorderStroke(1.dp, EmobiesBorder), RoundedCornerShape(13.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repair.device,
                    color = EmobiesText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmobiesMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = repair.customerLocation,
                        color = EmobiesText2,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = repair.status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AddNewRepairDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var device by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Kannur") }
    var status by remember { mutableStateOf("In Progress") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EmobiesSurface),
            border = BorderStroke(1.dp, EmobiesBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Live Repair",
                    color = EmobiesText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Device Name Field
                OutlinedTextField(
                    value = device,
                    onValueChange = { device = it },
                    label = { Text("Device Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmobiesOrange,
                        unfocusedBorderColor = EmobiesBorder,
                        focusedLabelColor = EmobiesOrange,
                        unfocusedLabelColor = EmobiesText2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Kannur", "Dubai").forEach { loc ->
                        val selected = location == loc
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) EmobiesOrange.copy(alpha = 0.15f) else EmobiesCard, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, if (selected) EmobiesOrange else EmobiesBorder), RoundedCornerShape(8.dp))
                                .clickable { location = loc }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(loc, color = if (selected) EmobiesOrange else EmobiesText2, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Pending", "In Progress", "Done ✓").forEach { s ->
                        val selected = status == s
                        val color = when (s) {
                            "Done ✓" -> EmobiesGreen
                            "In Progress" -> EmobiesYellow
                            else -> EmobiesBlue
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) color.copy(alpha = 0.15f) else EmobiesCard, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, if (selected) color else EmobiesBorder), RoundedCornerShape(8.dp))
                                .clickable { status = s }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s, color = if (selected) color else EmobiesText2, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, EmobiesBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = EmobiesText2)
                    }

                    Button(
                        onClick = { if (device.isNotEmpty()) onSave(device, location, status) },
                        enabled = device.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmobiesOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmoCoinsTab(viewModel: MainViewModel) {
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var scratchDialogOpen by remember { mutableStateOf(false) }
    var referDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Grand Coin Balance Display Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(EmobiesCard, EmobiesSurface)
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(BorderStroke(1.dp, EmobiesOrange.copy(alpha = 0.3f)), RoundedCornerShape(13.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${stats?.coins ?: "—"} EmoCoins",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = EmobiesOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1,000 coins → USDT / SOL / ETH",
                    color = EmobiesMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Activities List Items
        CoinActionItem(
            title = "🎰 Daily Scratch Card",
            subtitle = "Win 5–50 coins · Once per day",
            color = EmobiesPurple,
            onClick = {
                val now = System.currentTimeMillis()
                val lastScratch = stats?.lastScratch ?: 0L
                val oneDayMillis = 24 * 60 * 60 * 1000L
                if (now - lastScratch >= oneDayMillis) {
                    scratchDialogOpen = true
                } else {
                    Toast.makeText(context, "Checked scratch already today! Try again later.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CoinActionItem(
            title = "📅 Daily Check-in",
            subtitle = "+1 coin · First 10,000 users daily",
            color = EmobiesBlue,
            onClick = {
                val now = System.currentTimeMillis()
                val lastCheckIn = stats?.lastCheckIn ?: 0L
                val oneDayMillis = 24 * 60 * 60 * 1000L
                if (now - lastCheckIn >= oneDayMillis) {
                    viewModel.triggerCheckIn()
                    Toast.makeText(context, "Checked in! +1 EmoCoin!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Already checked in today!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CoinActionItem(
            title = "👥 Refer a Friend",
            subtitle = "100 coins per dynamic referral",
            color = EmobiesGreen,
            onClick = { referDialogOpen = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CoinActionItem(
            title = "💬 Join WhatsApp Circle",
            subtitle = "+50 coins · One-time reward",
            color = Color(0xFF25D366),
            onClick = {
                // Award 50 EmoCoins once
                viewModel.scratchCard(50)
                Toast.makeText(context, "Simulated joining WhatsApp! +50 EmoCoins rewarded!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (scratchDialogOpen) {
        ScratchCardDialog(
            onDismiss = { scratchDialogOpen = false },
            onScratchReveal = { reward ->
                viewModel.scratchCard(reward)
                scratchDialogOpen = false
                Toast.makeText(context, "Scratched! You won +$reward EmoCoins!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (referDialogOpen) {
        ReferFriendDialog(onDismiss = { referDialogOpen = false }) { reward ->
            viewModel.scratchCard(reward)
            referDialogOpen = false
            Toast.makeText(context, "Successfully referred! +$reward EmoCoins added!", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun CoinActionItem(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmobiesCard, RoundedCornerShape(13.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.2f)), RoundedCornerShape(13.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = EmobiesText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = EmobiesMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ScratchCardDialog(onDismiss: () -> Unit, onScratchReveal: (Int) -> Unit) {
    var revealed by remember { mutableStateOf(false) }
    val reward = remember { Random.nextInt(5, 51) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EmobiesSurface),
            border = BorderStroke(1.dp, EmobiesBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎰 Scratch Card",
                    color = EmobiesText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // The Scratch area
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            brush = if (revealed) {
                                Brush.verticalGradient(listOf(Color(0xFF2E1C0C), Color(0xFF1F1206)))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFF5E6573), Color(0xFF383C45)))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { revealed = true }
                        .drawBehind {
                            if (!revealed) {
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.1f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (revealed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉 REVEALED", color = EmobiesOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("+$reward", color = EmobiesOrange, fontSize = 38.sp, fontWeight = FontWeight.Black)
                            Text("EmoCoins", color = EmobiesText2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RUB / TAP HERE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("TO SCRATCH", color = Color.White.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, EmobiesBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = EmobiesText2)
                    }

                    Button(
                        onClick = { if (revealed) onScratchReveal(reward) else revealed = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmobiesOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (revealed) "Claim Now" else "Reveal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReferFriendDialog(onDismiss: () -> Unit, onReferSuccess: (Int) -> Unit) {
    var code by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EmobiesSurface),
            border = BorderStroke(1.dp, EmobiesBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👥 Refer a Friend",
                    color = EmobiesText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your referral code is EMOB-DIVIN-52C. Give friends 10 EmoCoins and get 100 EmoCoins when they join!",
                    color = EmobiesText2,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Friend's Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmobiesOrange,
                        unfocusedBorderColor = EmobiesBorder,
                        focusedLabelColor = EmobiesOrange,
                        unfocusedLabelColor = EmobiesText2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, EmobiesBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = EmobiesText2)
                    }

                    Button(
                        onClick = { if (code.isNotEmpty()) onReferSuccess(100) },
                        enabled = code.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmobiesOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TheWallTab(viewModel: MainViewModel) {
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentTrx = stats?.trxBalance ?: 12400.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "TheWall · ₹52 Crore",
            color = EmobiesText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Secure Crypto Stash and Yield Allocations",
            color = EmobiesMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                CryptoStakeCard(
                    symbol = "🔷",
                    platform = "Arbitrum · ETH",
                    allocation = "₹12.8 Crore · 842.1 ETH staked",
                    apy = "4.8% APY",
                    statusText = "Compounding"
                )
            }

            item {
                CryptoStakeCard(
                    symbol = "◎",
                    platform = "Solana",
                    allocation = "184 SOL",
                    apy = "+3.2% today",
                    statusText = "Staked"
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmobiesCard),
                    border = BorderStroke(1.dp, if (currentTrx > 0.0) EmobiesOrange.copy(alpha = 0.3f) else EmobiesBorder),
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔴", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TRON Stake",
                                color = EmobiesText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentTrx > 0.0) "${currentTrx.toInt()} TRX · Ready to Claim" else "Claimed",
                                color = EmobiesText2,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (currentTrx > 0.0) {
                            Button(
                                onClick = {
                                    viewModel.claimTrxStaked()
                                    Toast.makeText(context, "TRX Claim conversion successful! +124 EmoCoins awarded!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmobiesOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Claim ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(EmobiesGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Claimed ✓", color = EmobiesGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                CryptoStakeCard(
                    symbol = "👻",
                    platform = "AAVE · DeFi",
                    allocation = "Liquidity active",
                    apy = "Yield accumulating",
                    statusText = "Active"
                )
            }
        }
    }
}

@Composable
fun CryptoStakeCard(symbol: String, platform: String, allocation: String, apy: String, statusText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmobiesCard, RoundedCornerShape(13.dp))
            .border(BorderStroke(1.dp, EmobiesBorder), RoundedCornerShape(13.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform,
                    color = EmobiesText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$allocation · $apy",
                    color = EmobiesText2,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(EmobiesMuted.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    color = EmobiesText2,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
