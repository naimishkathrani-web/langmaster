package com.langmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.MessageEntity
import com.langmaster.ui.LangMasterViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LangMasterApp(vm: LangMasterViewModel = viewModel()) {
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()
    if (!isLoggedIn) {
        OnboardingScreen(
            authStatus = vm.authStatus.collectAsStateWithLifecycle().value,
            onRegister = vm::registerPin,
            onLogin = vm::loginWithPin
        )
        return
    }
    var selectedTab by remember { mutableStateOf(AppTab.CONNECT) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("LangMaster") }) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem("Connect", selectedTab == AppTab.CONNECT) { selectedTab = AppTab.CONNECT }
                TabItem("AI Translate", selectedTab == AppTab.AGENT) { selectedTab = AppTab.AGENT }
                TabItem("Learn", selectedTab == AppTab.LEARN) { selectedTab = AppTab.LEARN }
            }
        }
    ) { padding ->
        when (selectedTab) {
            AppTab.CONNECT -> ConnectScreen(vm = vm, modifier = Modifier.padding(padding))
            AppTab.AGENT -> AgentTranslateScreen(vm = vm, modifier = Modifier.padding(padding))
            AppTab.LEARN -> LearningScreen(vm = vm, modifier = Modifier.padding(padding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(
    authStatus: String?,
    onRegister: (String, String, String) -> Unit,
    onLogin: (String, String) -> Unit
) {
    var phone by remember { mutableStateOf("+91") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("LangMaster Setup") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Step 1: Register mobile + 4-digit PIN")
            TextField(value = phone, onValueChange = { phone = it }, placeholder = { Text("Phone number") })
            TextField(value = pin, onValueChange = { pin = it }, placeholder = { Text("Create 4-digit PIN") })
            TextField(value = confirmPin, onValueChange = { confirmPin = it }, placeholder = { Text("Confirm PIN") })
            Button(onClick = { onRegister(phone, pin, confirmPin) }) { Text("Register PIN") }
            Text("Step 2: Login with mobile + PIN")
            Button(onClick = { onLogin(phone, pin) }) { Text("Login & Continue") }
            authStatus?.let { Text(it) }
            Text("Step 3+: Permissions, contacts sync, Google backup, and profile setup will be added next.")
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

    Column(modifier = modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Chat + Voice Note + Voice Call + Video Call", fontWeight = FontWeight.Bold)
        Text("Current: $activeConversationTitle")
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(conversations, key = { it.id }) { conversation ->
                OutlinedButton(onClick = { vm.selectConversation(conversation.id) }) {
                    Text(conversation.title ?: conversation.id)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Translation", fontWeight = FontWeight.SemiBold)
            Switch(
                checked = translationEnabled,
                onCheckedChange = {
                    translationEnabled = it
                    vm.setListenerPreference(it, preferredLanguage)
                }
            )
        }
        if (translationEnabled) {
            Text("Listen in")
            LanguageChipRow(
                selected = preferredLanguage,
                onSelect = {
                    preferredLanguage = it
                    vm.setListenerPreference(translationEnabled, preferredLanguage)
                }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { }) { Text("Voice Call") }
            Button(onClick = { }) { Text("Video Call") }
            Button(onClick = { }) { Text("Voice Record") }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onDeleteForEveryone = { vm.deleteForEveryone(msg) },
                    onDeleteForMe = { vm.deleteForMe(msg.id) },
                    onForward = { vm.forwardMessage(msg, "conv-group-1") }
                )
            }
        }

        val aiUsed = vm.shouldUseAi(
            translationEnabled = translationEnabled,
            listenerLanguage = preferredLanguage,
            sourceLanguage = "Hindi"
        )
        Text(
            text = if (aiUsed) "AI translation active for listener" else "AI idle (same language or translation off)",
            color = if (aiUsed) Color(0xFFBBDEFB) else Color(0xFFFFCDD2)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier.weight(1f),
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Type message") }
            )
            Button(onClick = {
                vm.sendConnectMessage(input, preferredLanguage)
                input = ""
            }) {
                Text("Send")
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

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI Agent Translation Studio", fontWeight = FontWeight.Bold)
        Text("Same UX style as Connect tab, but talking to AI agent instead of contacts.")
        Text("Source language")
        LanguageChipRow(selected = source, onSelect = { source = it })
        Text("Target language")
        LanguageChipRow(selected = target, onSelect = { target = it })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { }) { Text("Translate Image") }
            Button(onClick = { }) { Text("Translate Voice") }
            Button(onClick = { }) { Text("Translate Video") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val output = sessions.firstOrNull()?.outputText.orEmpty()
                if (output.isNotBlank()) {
                    shareText(context, output, null)
                }
            }) { Text("Share to App") }
            Button(onClick = {
                val output = sessions.firstOrNull()?.outputText.orEmpty()
                if (output.isNotBlank()) {
                    shareText(context, output, "com.whatsapp")
                }
            }) { Text("Share to WhatsApp") }
        }

        TextField(value = inputText, onValueChange = { inputText = it }, placeholder = { Text("Type text to translate") })
        Button(onClick = {
            vm.saveTranslation(source = source, target = target, input = inputText)
            inputText = ""
        }) { Text("Ask AI Agent") }

        Text("Recent AI translations")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sessions.take(5), key = { it.id }) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("${session.sourceLang} → ${session.targetLang}", fontWeight = FontWeight.SemiBold)
                        Text("In: ${session.inputText.orEmpty()}")
                        Text("Out: ${session.outputText.orEmpty()}")
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
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI Language Learning", fontWeight = FontWeight.Bold)
        Text("Guided modules to become expert and certification-ready.")
        Text("Choose language")
        LanguageChipRow(
            selected = selectedLanguage,
            onSelect = {
                selectedLanguage = it
                vm.setLearningLanguage(it)
            }
        )

        tracks.forEach { track ->
            Text("• ${track.title}: ${track.description}")
        }

        modules.forEach { module ->
            LearningModuleCard(module = module, onComplete = { vm.markModuleProgress(module.id, 80) })
        }
    }
}

@Composable
private fun LearningModuleCard(module: LearningModuleEntity, onComplete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(module.title, fontWeight = FontWeight.SemiBold)
            Text(module.goal)
            Button(onClick = onComplete) { Text("Mark progress") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        OnboardingScreen(
            authStatus = "Sample Status Message",
            onRegister = { _, _, _ -> },
            onLogin = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LanguageChipRowPreview() {
    MaterialTheme {
        LanguageChipRow(selected = "English", onSelect = {})
    }
}
