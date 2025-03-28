package it.polito.teamlounge

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.polito.teamlounge.model.Message
import it.polito.teamlounge.model.Team
import it.polito.teamlounge.model.User
import it.polito.teamlounge.ui.theme.PrimaryColor
import it.polito.teamlounge.ui.theme.PrimaryTextColor
import it.polito.teamlounge.ui.theme.TaggedColor
import it.polito.teamlounge.ui.theme.TaggedTextColor
import kotlinx.coroutines.launch

fun getAnnotatedMessageText(message: String, taggedUsers: List<String>): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var currentIndex = 0

    for (user in taggedUsers) {
        val pattern = "@${user}"
        val regex = Regex(Regex.escape(pattern))
        val matches = regex.findAll(message)
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            // add text before the match
            if (start > currentIndex) {
                builder.append(message.substring(currentIndex, start))
            }
            // add the matched tag with bold style
            builder.withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold, color = TaggedTextColor)
            ) {
                builder.append(match.value)
            }
            currentIndex = end
        }
    }
    // add the remaining text
    if (currentIndex < message.length) {
        builder.append(message.substring(currentIndex))
    }
    return builder.toAnnotatedString()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessage(vm: FormViewModel, message: Message) {
    val isMyMessage = message.user == vm.me.collectAsState().value.Email
    val isTagged = vm.me.collectAsState().value.Email in message.taggedUsers
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start) {
            if (!isMyMessage) {
                Text(
                    text = message.user ?: "Unknown User",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (isTagged) TaggedColor else if (isMyMessage) PrimaryColor else PrimaryTextColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .then(
                        if (isTagged) Modifier.border(
                            2.dp,
                            Color.Black,
                            RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = getAnnotatedMessageText(message.text, message.taggedUsers),
                    color = Color.White
                )
            }
            Text(
                text = message.time.toString().substring(11, 16),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(vm: FormViewModel, id: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }
    val teamState = remember { mutableStateOf(Team()) }
    val usersState = remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val teams = vm.getTeams()
            val users = vm.getUsers()
            teamsState.value = teams
            usersState.value = users

            teams.forEach {
                if (it.id == id) {
                    teamState.value = it
                }
            }
            vm.removeNotSeen(vm.me.value.Email, teamState.value)
        }
    }

    val users by usersState
    val team by teamState


    var newMessageState by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }

    Column {
        HeaderUI(navController, vm)
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 20.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = team.Name, style = MaterialTheme.typography.titleLarge)
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Adjust bottom padding for input field
            ) {
                items(team.chat.messages) { message ->
                    ChatMessage(vm, message)
                }
            }

            // Input and Suggestions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .imePadding()
            ) {
                // Suggestions
                suggestions.forEach { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newText =
                                    newMessageState.text.substringBeforeLast("@") + "@$suggestion "
                                newMessageState = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newText.length)
                                )
                                suggestions = emptyList()
                            }
                            .background(Color.White)
                            .padding(8.dp)
                    )
                }

                // Text Field Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newMessageState,
                        onValueChange = { value ->
                            /* TODO change to double @ for email */
                            newMessageState = value
                            val lastWord = value.text.substringAfterLast("@", "")
                            suggestions = if (lastWord.isNotEmpty()) {
                                users
                                    .map { it.Email }
                                    .filter { it.contains(lastWord, ignoreCase = true) }
                            } else {
                                emptyList()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Type a message") }
                    )

                    if (newMessageState.text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    vm.sendMessage(team, vm.me.value, newMessageState.text.trim())
                                    newMessageState = TextFieldValue("")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}