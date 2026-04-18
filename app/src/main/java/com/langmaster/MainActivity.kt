package com.langmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.InsertDriveFile
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TextFieldDefaults
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.ConversationEntity
import com.langmaster.data.local.entity.LearningTrackEntity
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.langmaster.data.local.entity.TranslationSessionEntity
import com.langmaster.data.local.entity.MessageEntity
import com.langmaster.ui.LangMasterViewModel
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect

data class Country(
    val name: String,
    val code: String,
    val flag: String,
    val numberLength: Int
)

val countries = listOf(
    Country("India", "+91", "🇮🇳", 10),
    Country("United States", "+1", "🇺🇸", 10),
    Country("United Kingdom", "+44", "🇬🇧", 10),
    Country("Australia", "+61", "🇦🇺", 9),
    Country("Canada", "+1", "🇨🇦", 10),
    Country("Germany", "+49", "🇩🇪", 11),
    Country("France", "+33", "🇫🇷", 9),
    Country("Japan", "+81", "🇯🇵", 10),
    Country("China", "+86", "🇨🇳", 11),
    Country("Brazil", "+55", "🇧🇷", 11)
)

private val supportedLanguages = listOf("English", "Hindi", "Gujarati", "Marathi", "Tamil")

private val PrimaryBlue = Color(0xFF1565C0)
private val SecondaryRed = Color(0xFFC62828)
private val TextBlack = Color(0xFF1A1A2E)

private val LangMasterColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryRed,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextBlack,
    onSurface = TextBlack,
    surfaceVariant = Color(0xFFE3F2FD),
    onSurfaceVariant = TextBlack
)

@Composable
fun LangMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LangMasterColorScheme,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangMasterTheme {
                LangMasterApp()
            }
        }
    }
}

private enum class AppTab { CONNECT, AGENT, LEARN }

@Composable
fun LangMasterApp(vm: LangMasterViewModel = viewModel()) {
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AppTab.CONNECT) }

    LaunchedEffect(isLoggedIn) {
        android.util.Log.d("LangMasterUI", "isLoggedIn changed: $isLoggedIn")
    }

    if (!isLoggedIn) {
        OnboardingScreen(vm = vm)
        return
    }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    AppTab.entries.forEach { tab ->
                        val (label, icon) = when (tab) {
                            AppTab.CONNECT -> "Connect" to Icons.Default.Chat
                            AppTab.AGENT -> "Agent" to Icons.Default.Translate
                            AppTab.LEARN -> "Learn" to Icons.Default.School
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { 
                                selectedTab = tab 
                                if (tab == AppTab.CONNECT) {
                                    vm.selectConversation(null)
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                AppTab.CONNECT -> ConnectScreen(vm = vm)
                AppTab.AGENT -> AgentTranslateScreen(vm = vm)
                AppTab.LEARN -> LearningScreen(vm = vm)
            }
        }
    }
}

private enum class Screen { LOGIN, REGISTER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(vm: LangMasterViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    val authStatus by vm.authStatus.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            
            // Logo Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "LangMaster",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.weight(0.5f))

            if (currentScreen == Screen.LOGIN) {
                LoginContent(
                    vm = vm,
                    onLogin = vm::loginWithPin,
                    onNavigateToRegister = { currentScreen = Screen.REGISTER }
                )
            } else {
                RegisterContent(
                    vm = vm,
                    onRegister = { phone, email, pin, confirmPin, firstName, lastName, native, others ->
                        vm.register(phone, email, pin, confirmPin, firstName, lastName, native, others)
                    },
                    onNavigateToLogin = { currentScreen = Screen.LOGIN }
                )
            }

            authStatus?.let {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun LoginContent(
    vm: LangMasterViewModel,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(countries[0]) }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val authStatus by vm.authStatus.collectAsStateWithLifecycle()
    
    // Auto-clear PIN on login failure
    LaunchedEffect(authStatus) {
        if (authStatus != null && (authStatus!!.contains("Invalid", ignoreCase = true) || authStatus!!.contains("not found", ignoreCase = true))) {
            pin = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome Back", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Login to your account", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(0.25f)) {
                OutlinedTextField(
                    value = selectedCountry.flag,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = Color.Transparent
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.flag} ${country.name}") },
                            onClick = {
                                selectedCountry = country
                                expanded = false
                                if (phone.length > country.numberLength) {
                                    phone = phone.take(country.numberLength)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = selectedCountry.code,
                onValueChange = {},
                readOnly = true,
                label = { Text("Code") },
                modifier = Modifier.weight(0.25f),
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() } && it.length <= selectedCountry.numberLength) {
                        phone = it
                        vm.clearAuthStatus()
                    }
                },
                label = { Text("Mobile Number") },
                modifier = Modifier.weight(0.5f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(24.dp))

        Text("Enter 4-Digit PIN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        PinInputRow(
            pin = pin,
            onPinChange = { 
                pin = it
                vm.clearAuthStatus()
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = { onLogin("${selectedCountry.code}$phone", pin) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            enabled = phone.length == selectedCountry.numberLength && pin.length == 4
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text(
                "Don't have an account? Register",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RegisterContent(
    vm: LangMasterViewModel,
    onRegister: (String, String, String, String, String, String, String, List<String>) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(countries[0]) }
    var phone by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var nativeLanguage by remember { mutableStateOf("English") }
    var otherLanguages by remember { mutableStateOf(setOf<String>()) }
    var expanded by remember { mutableStateOf(false) }

    val authStatus by vm.authStatus.collectAsStateWithLifecycle()

    // Auto-clear PINs on registration failure (e.g. mismatch)
    LaunchedEffect(authStatus) {
        if (authStatus != null && (authStatus!!.contains("mismatch", ignoreCase = true) || authStatus!!.contains("failed", ignoreCase = true))) {
            pin = ""
            confirmPin = ""
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Join LangMaster today", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(0.25f)) {
                    OutlinedTextField(
                        value = selectedCountry.flag,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${country.flag} ${country.name}") },
                                onClick = {
                                    selectedCountry = country
                                    expanded = false
                                    if (phone.length > country.numberLength) {
                                        phone = phone.take(country.numberLength)
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = selectedCountry.code,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Code") },
                    modifier = Modifier.weight(0.25f),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= selectedCountry.numberLength) {
                            phone = it
                            vm.clearAuthStatus()
                        }
                    },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.weight(0.5f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it; vm.clearAuthStatus() },
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; vm.clearAuthStatus() },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    vm.clearAuthStatus()
                },
                label = { Text("Email ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create 4-Digit PIN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                PinInputRow(pin = pin, onPinChange = { 
                    pin = it
                    vm.clearAuthStatus()
                })
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Confirm 4-Digit PIN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                PinInputRow(pin = confirmPin, onPinChange = { 
                    confirmPin = it
                    vm.clearAuthStatus()
                })
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Your Native Language", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LanguageChipRow(selected = nativeLanguage, onSelect = { nativeLanguage = it })
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Languages you understand", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OtherLanguagesSelection(selected = otherLanguages, onToggle = { lang ->
                    val current = otherLanguages.toMutableSet()
                    if (current.contains(lang)) current.remove(lang) else current.add(lang)
                    otherLanguages = current
                })
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onRegister("${selectedCountry.code}$phone", email, pin, confirmPin, firstName, lastName, nativeLanguage, otherLanguages.toList()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = phone.length == selectedCountry.numberLength && pin.isNotEmpty() && pin == confirmPin && email.contains("@") && firstName.isNotBlank() && lastName.isNotBlank()
            ) {
                Text("Register", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PinInputRow(pin: String, onPinChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Hidden TextField to capture input
        TextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                    onPinChange(it)
                }
            },
            modifier = Modifier
                .size(width = 230.dp, height = 50.dp)
                .focusRequester(focusRequester)
                .alpha(0.01f), // Minimal alpha to avoid visibility but keep focusable
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { focusRequester.requestFocus() }
        ) {
            repeat(4) { index ->
                val char = pin.getOrNull(index)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = if (index == pin.length) 2.dp else 1.dp,
                            color = if (index == pin.length) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (char.isEmpty()) "" else "●",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Removed auto-focus LaunchedEffect to prevent infinite IME/Focus loops during recomposition.
    // Focus can be requested manually by tapping the PinInputRow.
}

@Composable
private fun OtherLanguagesSelection(selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        supportedLanguages.forEach { lang ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggle(lang) }) {
                Checkbox(checked = selected.contains(lang), onCheckedChange = { onToggle(lang) })
                Text(lang)
            }
        }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color(0xFF64B5F6) else Color.White,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun LanguageChipRow(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        supportedLanguages.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { language ->
                    val isSelected = language == selected
                    OutlinedButton(onClick = { onSelect(language) }) {
                        Text(if (isSelected) "✓ $language" else language)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectScreen(vm: LangMasterViewModel, modifier: Modifier = Modifier) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    val activeConvId by vm.activeConversationId.collectAsStateWithLifecycle()
    val activeConversationTitle by vm.activeConversationTitle.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val currentUserPhone by vm.currentUserPhone.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (activeConvId == null) {
        // ── CONTACT LIST VIEW ──
        Scaffold(
            topBar = {
                Surface(color = PrimaryBlue, shadowElevation = 4.dp) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LangMaster", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Open WhatsApp invite for a sample number
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://api.whatsapp.com/send?phone=&text=Hey! Chat with me on LangMaster — the multilingual messaging app!"))
                        context.startActivity(intent)
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Invite")
                }
            }
        ) { padding ->
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("No conversations yet", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap + to invite friends", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationListItem(
                            conversation = conversation,
                            onClick = { vm.selectConversation(conversation.id) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                    }
                }
            }
        }
    } else {
        // ── CHAT WINDOW VIEW ──
        var input by remember { mutableStateOf("") }

        Column(modifier = modifier.fillMaxSize()) {
            // Chat top bar
            Surface(color = PrimaryBlue, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.selectConversation(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            activeConversationTitle.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(activeConversationTitle, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("online", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Messages
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFECE5DD))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isMe = msg.senderPhoneE164 == currentUserPhone,
                        onDeleteForEveryone = { vm.deleteForEveryone(msg) },
                        onDeleteForMe = { vm.deleteForMe(msg.id) }
                    )
                }
            }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    vm.sendMediaMessage(uri.toString(), "IMAGE")
                }
            }

            // Input bar
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap ->
                // TODO: Save bitmap to temp file and send URI
            }

            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.Gray)
                    }
                    IconButton(onClick = { cameraLauncher.launch(null) }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.Gray)
                    }
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Type a message") },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    FloatingActionButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                vm.sendConnectMessage(input, "English")
                                input = ""
                            }
                        },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (input.isNotBlank()) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        } else {
                            Icon(Icons.Default.Mic, contentDescription = "Record")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationListItem(conversation: ConversationEntity, onClick: () -> Unit) {
    val name = conversation.title ?: "Unknown"
    val initials = name.split(" ").take(2).joinToString("") { it.take(1).uppercase() }
    val avatarColors = listOf(
        Color(0xFF1565C0), Color(0xFFC62828), Color(0xFF2E7D32),
        Color(0xFFEF6C00), Color(0xFF6A1B9A), Color(0xFF00838F)
    )
    val avatarColor = avatarColors[name.hashCode().mod(avatarColors.size).let { if (it < 0) it + avatarColors.size else it }]
    
    val timeDiff = System.currentTimeMillis() - conversation.updatedAt
    val timeLabel = when {
        timeDiff < 60_000 -> "now"
        timeDiff < 3_600_000 -> "${timeDiff / 60_000}m"
        timeDiff < 86_400_000 -> "${timeDiff / 3_600_000}h"
        else -> "${timeDiff / 86_400_000}d"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(avatarColor, shape = RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(timeLabel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                if (conversation.type == "GROUP") "Group conversation" else "Tap to open chat",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: MessageEntity,
    isMe: Boolean,
    onDeleteForEveryone: () -> Unit,
    onDeleteForMe: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val bubbleColor = if (isMe) Color(0xFFDCF8C6) else Color.White
    val alignment = if (isMe) Arrangement.End else Arrangement.Start

    val timeDiff = System.currentTimeMillis() - message.createdAt
    val timeLabel = when {
        timeDiff < 60_000 -> "just now"
        timeDiff < 3_600_000 -> "${timeDiff / 60_000}m ago"
        timeDiff < 86_400_000 -> "${timeDiff / 3_600_000}h ago"
        else -> "${timeDiff / 86_400_000}d ago"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable { expanded = true },
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (isMe) 12.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 12.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.messageType == "IMAGE" && message.mediaUri != null) {
                    AsyncImage(
                        model = message.mediaUri,
                        contentDescription = "Image attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                } else if (message.messageType == "VOICE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, contentDescription = "Play Voice", tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Voice Note", style = MaterialTheme.typography.bodyMedium, color = TextBlack)
                    }
                    Spacer(Modifier.height(4.dp))
                } else if (message.messageType == "VIDEO") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = "Play Video", tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Video Attachment", style = MaterialTheme.typography.bodyMedium, color = TextBlack)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                
                if (!message.body.isNullOrBlank()) {
                    Text(
                        message.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextBlack
                    )
                    Spacer(Modifier.height(2.dp))
                }
                
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Delete for me") }, onClick = { onDeleteForMe(); expanded = false })
                DropdownMenuItem(text = { Text("Delete for everyone") }, onClick = { onDeleteForEveryone(); expanded = false })
            }
        }
    }
}

// ─────────── AGENT TAB ───────────

@Composable
private fun AgentTranslateScreen(vm: LangMasterViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessions by vm.translationSessions.collectAsStateWithLifecycle()
    var source by remember { mutableStateOf("English") }
    var target by remember { mutableStateOf("Hindi") }
    var inputText by remember { mutableStateOf("") }
    var showLanguageConfig by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { /* init */ }
        tts = textToSpeech
        onDispose { textToSpeech.stop(); textToSpeech.shutdown() }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) inputText = spoken
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        Surface(color = PrimaryBlue, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Agent", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("$source → $target", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { showLanguageConfig = !showLanguageConfig }) {
                    Text("⚙", color = Color.White, fontSize = 20.sp)
                }
            }
        }

        // Language config (collapsible)
        if (showLanguageConfig) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Source Language", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LanguageChipRow(selected = source, onSelect = { source = it })
                    Spacer(Modifier.height(8.dp))
                    Text("Target Language", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LanguageChipRow(selected = target, onSelect = { target = it })
                }
            }
        }

        // Chat-style translations (Agent Window)
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        
        LaunchedEffect(sessions.size) {
            if (sessions.isNotEmpty()) {
                listState.animateScrollToItem(sessions.size - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFECE5DD))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            reverseLayout = false
        ) {
            items(sessions, key = { it.id }) { session ->
                // User message (right)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Card(
                        modifier = Modifier.widthIn(max = 300.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light blue for user in Agent mode
                        shape = RoundedCornerShape(
                            topStart = 12.dp, topEnd = 12.dp,
                            bottomStart = 12.dp, bottomEnd = 2.dp
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(session.inputText.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = TextBlack)
                            Spacer(Modifier.height(2.dp))
                            Text(session.sourceLang, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                // AI response (left)
                if (!session.outputText.isNullOrBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Card(
                            modifier = Modifier.widthIn(max = 300.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(
                                topStart = 12.dp, topEnd = 12.dp,
                                bottomStart = 2.dp, bottomEnd = 12.dp
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(session.outputText.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(session.targetLang, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("•", color = Color.Gray)
                                    Icon(
                                        Icons.Default.PlayCircle, 
                                        contentDescription = "Read aloud",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp).clickable {
                                            val loc = when (session.targetLang.lowercase()) {
                                                "hindi" -> Locale("hi", "IN")
                                                "gujarati" -> Locale("gu", "IN")
                                                "marathi" -> Locale("mr", "IN")
                                                "tamil" -> Locale("ta", "IN")
                                                else -> Locale.US
                                            }
                                            tts?.language = loc
                                            tts?.speak(session.outputText.orEmpty(), TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // WhatsApp Style Input bar
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview()
                ) { bitmap ->
                    // TODO: process agent image
                }

                IconButton(onClick = {
                    // TODO: Implement Agent Attachment
                }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.Gray)
                }

                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.Gray)
                }
                
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask the Agent...") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            vm.saveTranslation(source = source, target = target, input = inputText)
                            inputText = ""
                        }
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    if (inputText.isNotBlank()) {
                        Icon(Icons.Default.Send, contentDescription = "Translate")
                    } else {
                        Icon(Icons.Default.Mic, contentDescription = "Record")
                    }
                }
            }
        }
    }
}

private fun shareText(context: android.content.Context, text: String, packageName: String?) {
    val baseIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val targetIntent = if (packageName != null) {
        baseIntent.apply { setPackage(packageName) }.takeIf {
            it.resolveActivity(context.packageManager) != null
        } ?: baseIntent
    } else {
        baseIntent
    }
    context.startActivity(Intent.createChooser(targetIntent, "Share translation"))
}

// ─────────── LEARN TAB (Coming Soon) ───────────

@Composable
private fun LearningScreen(vm: LangMasterViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryBlue, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Learning Center", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextBlack)
        Spacer(Modifier.height(8.dp))
        Text("Coming Soon", style = MaterialTheme.typography.titleMedium, color = SecondaryRed, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Interactive language lessons, quizzes,\nand certification prep — all offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    LangMasterTheme {
        Text("Onboarding Preview")
    }
}

@Preview(showBackground = true)
@Composable
fun LanguageChipRowPreview() {
    LangMasterTheme {
        LanguageChipRow(selected = "English", onSelect = {})
    }
}
