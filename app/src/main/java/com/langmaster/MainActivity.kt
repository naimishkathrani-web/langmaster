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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
                            onClick = { selectedTab = tab },
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
                    onRegister = { phone, email, pin, confirmPin, native, others ->
                        vm.register(phone, email, pin, confirmPin, native, others)
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
    onRegister: (String, String, String, String, String, List<String>) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(countries[0]) }
    var phone by remember { mutableStateOf("") }
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
                onClick = { onRegister("${selectedCountry.code}$phone", email, pin, confirmPin, nativeLanguage, otherLanguages.toList()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = phone.length == selectedCountry.numberLength && pin.isNotEmpty() && pin == confirmPin && email.contains("@")
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
    val activeConversationTitle by vm.activeConversationTitle.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var translationEnabled by remember { mutableStateOf(false) }
    var preferredLanguage by remember { mutableStateOf("English") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Active Chat Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(activeConversationTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Chat + Voice + Video", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick Conversation Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            conversations.take(3).forEach { conversation ->
                val isSelected = activeConversationTitle == (conversation.title ?: conversation.id)
                OutlinedButton(
                    onClick = { vm.selectConversation(conversation.id) },
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(conversation.title ?: "Group", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Translation Controls
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Translation", fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = translationEnabled,
                        onCheckedChange = {
                            translationEnabled = it
                            vm.setListenerPreference(it, preferredLanguage)
                        }
                    )
                }
                if (translationEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Text("Listening in:", style = MaterialTheme.typography.labelSmall)
                    LanguageChipRow(
                        selected = preferredLanguage,
                        onSelect = {
                            preferredLanguage = it
                            vm.setListenerPreference(translationEnabled, preferredLanguage)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Messages Area
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onDeleteForEveryone = { vm.deleteForEveryone(msg) },
                    onDeleteForMe = { vm.deleteForMe(msg.id) },
                    onForward = { vm.forwardMessage(msg, "conv-group-1") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Input Area
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            vm.sendConnectMessage(input, preferredLanguage)
                            input = ""
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onDeleteForEveryone: () -> Unit,
    onDeleteForMe: () -> Unit,
    onForward: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(message.senderPhoneE164, fontWeight = FontWeight.Bold)
                Text("▼", modifier = Modifier.clickable { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Reply") }, onClick = { expanded = false })
                    DropdownMenuItem(
                        text = { Text("Forward") },
                        onClick = {
                            onForward()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete for me") },
                        onClick = {
                            onDeleteForMe()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete for everyone (24h)") },
                        onClick = {
                            onDeleteForEveryone()
                            expanded = false
                        }
                    )
                }
            }
            Text(message.body ?: "")
        }
    }
}

@Composable
private fun AgentTranslateScreen(vm: LangMasterViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessions by vm.translationSessions.collectAsStateWithLifecycle()
    var source by remember { mutableStateOf("English") }
    var target by remember { mutableStateOf("Hindi") }
    var inputText by remember { mutableStateOf("") }
    
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized successfully
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // AI Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AI Agent Studio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Instant cross-app translation", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Language Config
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Source Language", style = MaterialTheme.typography.labelMedium)
                LanguageChipRow(selected = source, onSelect = { source = it })
                
                Spacer(Modifier.height(12.dp))
                
                Text("Target Language", style = MaterialTheme.typography.labelMedium)
                LanguageChipRow(selected = target, onSelect = { target = it })
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Image", "Voice", "Video").forEach { type ->
                OutlinedButton(
                    onClick = {
                        if (type == "Voice") {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            runCatching { speechRecognizerLauncher.launch(intent) }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(type, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Input Field
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Enter text to translate via AI...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        vm.saveTranslation(source = source, target = target, input = inputText)
                        inputText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Translate")
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        // Recent Translations
        Text("Recent History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sessions.take(10), key = { it.id }) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(session.sourceLang, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(12.dp).padding(horizontal = 4.dp))
                            Text(session.targetLang, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(session.inputText.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(session.outputText.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { 
                                val loc = when (session.targetLang.lowercase()) {
                                    "english" -> Locale.US
                                    "hindi" -> Locale("hi", "IN")
                                    "gujarati" -> Locale("gu", "IN")
                                    "marathi" -> Locale("mr", "IN")
                                    "tamil" -> Locale("ta", "IN")
                                    else -> Locale.US
                                }
                                tts?.language = loc
                                tts?.speak(session.outputText.orEmpty(), TextToSpeech.QUEUE_FLUSH, null, null)
                            }) {
                                Text("🔊 Listen", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { shareText(context, session.outputText.orEmpty(), null) }) {
                                Text("Share", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { shareText(context, session.outputText.orEmpty(), "com.whatsapp") }) {
                                Text("WhatsApp", style = MaterialTheme.typography.labelSmall)
                            }
                        }
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
    val chooser = Intent.createChooser(targetIntent, "Share translation")
    context.startActivity(chooser)
}

@Composable
private fun LearningScreen(vm: LangMasterViewModel, modifier: Modifier = Modifier) {
    val tracks by vm.learningTracks.collectAsStateWithLifecycle()
    val modules by vm.learningModules.collectAsStateWithLifecycle()
    val learningLang by vm.learningLanguage.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Learning Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Learning Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Master a new language", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Language Selector
        Text("I want to learn:", style = MaterialTheme.typography.labelLarge)
        LanguageChipRow(selected = learningLang, onSelect = { vm.setLearningLanguage(it) })

        Spacer(Modifier.height(24.dp))

        if (tracks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No tracks available for $learningLang", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tracks) { track ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(track.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text("Modules", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            
                            // Nested list of modules for this track
                            modules.filter { it.trackId == track.id }.forEach { module ->
                                LearningModuleCard(module = module, onComplete = { vm.markModuleProgress(module.id, 100) })
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningModuleCard(module: LearningModuleEntity, onComplete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(module.phaseOrder.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(module.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(module.goal, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Checkbox(checked = false, onCheckedChange = { if (it) onComplete() })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        // Mocked preview would need a mock VM or specialized preview content
        Text("Onboarding Preview")
    }
}

@Preview(showBackground = true)
@Composable
fun LanguageChipRowPreview() {
    MaterialTheme {
        LanguageChipRow(selected = "English", onSelect = {})
    }
}
