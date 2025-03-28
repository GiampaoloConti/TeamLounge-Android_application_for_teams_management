package it.polito.teamlounge

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import it.polito.teamlounge.model.Comment
import it.polito.teamlounge.model.MyModel
import it.polito.teamlounge.model.Task
import it.polito.teamlounge.model.Team
import it.polito.teamlounge.model.User
import it.polito.teamlounge.ui.theme.BackgroundColor
import it.polito.teamlounge.ui.theme.LightPrimaryColor
import it.polito.teamlounge.ui.theme.Poppins
import it.polito.teamlounge.ui.theme.PrimaryColor
import it.polito.teamlounge.ui.theme.PrimaryTextColor
import it.polito.teamlounge.ui.theme.SecondaryColor
import it.polito.teamlounge.ui.theme.Shapes
import it.polito.teamlounge.ui.theme.SubTextColor
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskListUI(vm: FormViewModel, navController: NavController, id: String) {
    val scope = rememberCoroutineScope()
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val teams = vm.getTeams()
            teamsState.value = teams
        }
    }

    val teams by teamsState
    var t = teams.find { it.id == id } ?: Team()

    val switchState = remember { mutableStateOf(false) }
    val sortState = remember { mutableStateOf("ascending") }  // State to track sorting order
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Sort team based on the sortState
    val sortedTasks = remember(t.tasks, sortState.value) {
        if (sortState.value == "ascending") {
            t.tasks.sortedBy { dateFormatter.parse(it.dueDate) }
        } else {
            t.tasks.sortedByDescending { dateFormatter.parse(it.dueDate) }
        }
    }

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderUI(navController, vm)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, ${vm.me.collectAsState().value.FullName}",
                            fontFamily = Poppins,
                            fontSize = 18.sp,
                            color = PrimaryTextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Task List:",
                            fontFamily = Poppins,
                            fontSize = 16.sp,
                            color = PrimaryTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Show Only My Tasks:  ",
                            fontFamily = Poppins,
                            fontSize = 13.sp,  // Smaller font size
                            color = Color.Gray
                        )
                        Switch(
                            checked = switchState.value,
                            onCheckedChange = { switchState.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryColor,
                                uncheckedThumbColor = Color.Gray
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { sortState.value = "ascending" },
                            colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Ascend", color = Color.Gray, fontFamily = Poppins, fontSize = 13.sp)
                        }

                        TextButton(
                            onClick = { sortState.value = "descending" },
                            colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Descend", color = Color.Gray, fontFamily = Poppins, fontSize = 13.sp)
                        }
                    }
                }
                StatisticCardUI(t)
                if (sortedTasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No task yet",
                            fontFamily = Poppins,
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    }
                }
                if (switchState.value) {
                    sortedTasks.forEach {
                        if (it.users.contains(vm.me.value.Email)) {
                            TaskCardUI(navController, it, id)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    sortedTasks.forEach {
                        TaskCardUI(navController, it, id)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
            FloatingActionButton(
                onClick = { navController.navigate("AddTask/${id}") },
                containerColor = PrimaryColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.padding(all = 4.dp),
                    text = "New Task +",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


@SuppressLint("NewApi")
@Composable
fun TaskCardUI(navController: NavController, t: Task, teamid: String) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val now = LocalDateTime.now().format(dateTimeFormatter)
    if(t.recurring.compareTo("Only Once")!=0){
        if(t.recurring.compareTo("Daily")==0 && dateFormatter.parse(t.dueDate).before(dateFormatter.parse(now))){
            t.setDuedateValue(now)
            t.setStatusValue("To do")
            navController.navigate("TaskList/${teamid}")
        }
        else if (t.recurring.compareTo("Monthly")==0 && dateFormatter.parse(t.dueDate).before(dateFormatter.parse(now))){
            val s = t.dueDate.split("/").toMutableList()
            var m = now.split("/")[1].toInt()
            var y = now.split("/")[2].toInt()
            var d = ""
            if(m != 12){
                m += 1
                if(m>10){
                    d = "${m}"
                }else{
                    d = "0${m}"
                }
                s[1] = d
                s[2] = "${y}"
            }else{
                m=1
                y += 1
                s[1] = "${m}"
                s[2] = "${y}"
            }
            val h = "${s[0]}/${s[1]}/${s[2]}"
            t.setDuedateValue(h)

            t.setStatusValue("To do")
            navController.navigate("TaskList/${teamid}")
        }else if(t.recurring.compareTo("Every Year")==0 && dateFormatter.parse(t.dueDate).before(dateFormatter.parse(now))){
            val s = t.dueDate.split("/").toMutableList()
            var y = now.split("/")[2].toInt()
            y += 1
            s[2] = "${y}"
            val h = "${s[0]}/${s[1]}/${s[2]}"
            t.setDuedateValue(h)
            t.setStatusValue("To do")
            navController.navigate("TaskList/${teamid}")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 40.dp)
    ) {
        Button(
            onClick = {navController.navigate("taskDetails/${t.id}/${teamid}")},
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = Shapes.large
        ) {
            Row(
                modifier = Modifier.padding(20.dp)
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        if (t.status == "Finished") {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_tick_circle),
                                contentDescription = "",
                                tint = PrimaryColor,
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(25.dp),
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = "",
                                tint = PrimaryColor,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = t.title,
                            fontFamily = Poppins,
                            fontSize = 18.sp,
                            color = PrimaryTextColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t.description,
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = t.status,
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = t.dueDate,
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Recurrence: ${t.recurring}",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FilePreview(uri: Uri, context: Context) {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri)

    if (mimeType != null && mimeType.startsWith("image")) {
        // Display image preview
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray)
                .padding(4.dp)
        )
    } else {
        // Display file name for non-image files
        val fileName = remember { mutableStateOf("") }

        LaunchedEffect(uri) {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName.value = cursor.getString(nameIndex)
                }
            }
        }

        Text(
            text = fileName.value,
            modifier = Modifier
                .padding(4.dp)
                .background(Color.LightGray)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun FilePicker(
    onFilePicked: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFilePicked(it) }
    }

    IconButton(
        onClick = {
            launcher.launch("*/*") // Open the file picker
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Add Attachment",
            tint = PrimaryTextColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun makeLinksClickable(text: String): AnnotatedString {
    val linkPattern = Pattern.compile(
        "(https?://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = linkPattern.matcher(text)
    val annotatedString = buildAnnotatedString {
        append(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            addStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                ), start = start, end = end
            )
            addStringAnnotation(
                tag = "URL",
                annotation = text.substring(start, end),
                start = start,
                end = end
            )
        }
    }
    return annotatedString
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskDetailsPage(vm: FormViewModel, idTask: String, idteam: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }
    val refreshState = remember { mutableStateOf(false) }

    LaunchedEffect(refreshState.value, vm.checkFirebase) {
        scope.launch {
            val teams = vm.getTeams()
            teamsState.value = teams
        }
    }

    val teams by teamsState
    val team = teams.find { it.id == idteam } ?: Team()
    val task = team.tasks.find { it.id == idTask } ?: Task()

    Log.d("Task", idTask)

    val context = LocalContext.current
    val currentUser = vm.me.collectAsState().value.Email

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            HeaderUI(navController, vm)
            StatisticUI(vm, navController, task)
            if (task.users.contains(currentUser) && task.status != "Finished") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = vm.newTaskMember,
                        onValueChange = { newValue -> vm.newTaskMember = newValue },
                        label = { Text("New member") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (vm.validateTaskMember(task, idteam)) {
                                    vm.addUserTask(task, vm.newTaskMember, idteam)
                                    refreshState.value = !refreshState.value  // Trigger recomposition
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add User",
                            tint = PrimaryTextColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Button(
                    onClick = { navController.navigate("EditTask/${idTask}/${idteam}") },
                    colors = ButtonDefaults.buttonColors(PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 40.dp),
                    shape = Shapes.large
                ) {
                    Row {
                        Text(
                            text = "Edit Task",
                            fontFamily = Poppins,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            DescriptionUI(task)
            val c = remember { mutableStateOf(TextFieldValue()) }
            val selectedFileUri = remember { mutableStateOf<Uri?>(null) }
            val selectedFile = remember { mutableStateOf<File?>(null) }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Comments",
                fontFamily = Poppins,
                fontSize = 20.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 22.dp)
                    .padding(top = 20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp)
            ) {
                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                    val (inputField, filePicker, addButton) = createRefs()

                    TextField(
                        value = c.value,
                        onValueChange = { c.value = it },
                        label = { Text("Add your comment") },
                        modifier = Modifier
                            .constrainAs(inputField) {
                                start.linkTo(parent.start)
                                end.linkTo(filePicker.start)
                                width = Dimension.fillToConstraints
                            }
                            .padding(end = 8.dp)
                    )

                    FilePicker(
                        onFilePicked = { uri ->
                            selectedFileUri.value = uri
                            selectedFile.value = uri.let { vm.getFileFromUri(context, it) }
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .constrainAs(filePicker) {
                                end.linkTo(addButton.start)
                                top.linkTo(inputField.top)
                                bottom.linkTo(inputField.bottom)
                                start.linkTo(inputField.end)
                            }
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                if (c.value.text.isNotBlank() || selectedFileUri.value != null) {
                                    vm.addCommentToTask(
                                        task,
                                        idteam,
                                        currentUser,
                                        c.value.text,
                                        selectedFile.value
                                    )
                                    // Trigger recomposition
                                    refreshState.value = !refreshState.value
                                    c.value = TextFieldValue("")
                                    selectedFileUri.value = null
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .constrainAs(addButton) {
                                end.linkTo(parent.end)
                                top.linkTo(inputField.top)
                                bottom.linkTo(inputField.bottom)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                if (selectedFileUri.value != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilePreview(uri = selectedFileUri.value!!, context = context)
                }
            }

            task.comments.reversed().forEach {
                CommentCardUI(it, vm)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun StatisticUI(vm: FormViewModel, navController: NavController, task: Task) {
    val coroutineScope = rememberCoroutineScope()
    val (users, setUsers) = remember { mutableStateOf(emptyList<User>()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        setUsers(vm.getUsers())
        Log.d("task", task.title)
    }

    Column(
        modifier = Modifier.padding(30.dp)
    ) {
        Text(
            text = task.title,
            fontFamily = Poppins,
            fontSize = 25.sp,
            color = PrimaryTextColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = task.category,
            fontFamily = Poppins,
            fontSize = 16.sp,
            color = PrimaryTextColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tag: ${task.tag}",
            fontFamily = Poppins,
            fontSize = 16.sp,
            color = PrimaryTextColor,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                if (task.status == "Finished") {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tick_circle),
                        contentDescription = "",
                        tint = Color(0xFF818181),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = "",
                        tint = Color(0xFF818181),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.dueDate,
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = Color(0xFF818181),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Recurrence: ${task.recurring}",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = Color(0xFF818181),
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .clip(Shapes.large)
                    .background(Color(0xFFE1E3FA))
                    .border(width = 0.dp, color = Color.Transparent, shape = Shapes.large)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = task.status,
                    fontSize = 10.sp,
                    fontFamily = Poppins,
                    color = Color(0xFF7885B9)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        for (user in task.users) {
            var idu = -1
            var userObj: User? = null
            users.forEach {
                if (user.compareTo(it.Email) == 0) {
                    idu = it.id
                    userObj = it
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { navController.navigate("Profile/${userObj?.Email}") },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userObj != null) {
                    if (userObj!!.photoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userObj!!.photoUri.toString())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo taken",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(24.dp)
                        )
                    } else {
                        Text(
                            userObj!!.monogram,
                            modifier = Modifier
                                .padding(0.dp, 0.dp),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = user,
                    fontFamily = Poppins,
                    fontSize = 15.sp,
                    color = PrimaryTextColor,
                    modifier = Modifier.weight(1f)
                )
                if (task.users.contains(vm.me.value.Email) && task.status.compareTo("Finished") != 0) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                vm.removeUserTask(task,user, task.teamId)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove User",
                            tint = PrimaryTextColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Work Status",
                    tint = PrimaryColor, // Change to Color.Red for work not done
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


@Composable
fun DescriptionUI( task: Task) {
    Box(
        modifier = Modifier
            .padding(horizontal = 30.dp)
            .shadow(10.dp, Shapes.medium)
            .fillMaxWidth()
            .background(color = Color.White, Shapes.medium)
            .padding(16.dp),
        contentAlignment = Alignment.Center

    ) {
        Column{
            Text(
                text = "Description",
                fontFamily = Poppins,
                fontSize = 20.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = task.description,
                fontFamily = Poppins,
                fontSize = 14.sp,
                color = SubTextColor,
                lineHeight = 24.sp, // Increasing line height for better readability
                textAlign = TextAlign.Justify
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentCardUI(c: Comment, vm: FormViewModel) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    LaunchedEffect(c.attachmentUri) {
        c.let {
            c.attachmentUri?.let { it1 ->
                vm.retrieveFileComment(it1, object : MyModel.OnImageUriRetrievedListener {
                    override fun onImageUriRetrieved(uri: Uri?) {
                        imageUri.value = uri
                    }
                })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 20.dp)
            .background(
                color = if (c.isModification) Color.White else LightPrimaryColor,
                shape = RoundedCornerShape(8.dp)
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val annotatedString = makeLinksClickable(c.text)

            ClickableText(
                text = annotatedString,
                style = LocalTextStyle.current.copy(color = Color.Black),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val url = annotation.item
                            openUrl(context, url)
                        }
                }
            )


            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${c.user} - ${c.time}", fontSize = 10.sp)
            if (c.attachmentUri != null) {
                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri.value)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Photo taken",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
            }
        }
    }
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun openFile(context: Context, uri: Uri, mimeType: String? = null) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType ?: context.contentResolver.getType(uri))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No application found to open this file.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun StatisticCardUI(t: Team) {
    Column(
        modifier = Modifier.padding(30.dp)
    ) {
        Text(
            text = "Statistic",
            fontFamily = Poppins,
            fontSize = 16.sp,
            color = SubTextColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            StatisticProgressUI(t)
            Spacer(modifier = Modifier.width(12.dp))
            StatisticIndicatorUI()
        }
    }
}

@Composable
fun StatisticProgressUI(t: Team) {
    Box(
        modifier = Modifier
            .size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalTasks = t.tasks.size.toFloat()  // Convert to float to avoid integer division issues
        val finishedTasksPercentage = if (totalTasks > 0) (t.tasks.count { it.status == "Finished" } / totalTasks) * 100 else 0f
        val ongoingTasksPercentage = if (totalTasks > 0) (t.tasks.count { it.status == "On Going" } / totalTasks) * 100 else 0f

        val convertedPrimaryValue = finishedTasksPercentage * 360 / 100
        val convertedSecondaryValue = ongoingTasksPercentage * 360 / 100 + convertedPrimaryValue

        Canvas(
            modifier = Modifier
                .size(100.dp)
        ) {
            drawCircle(
                brush = SolidColor(Color(0xFFE3E5E7)),
                radius = size.width / 2,
                style = Stroke(width = 34f)
            )

            drawArc(
                brush = SolidColor(SecondaryColor),
                startAngle = -90f,
                sweepAngle = convertedSecondaryValue,
                useCenter = false,
                style = Stroke(width = 34f, cap = StrokeCap.Round)
            )
            drawArc(
                brush = SolidColor(PrimaryColor),
                startAngle = -90f,
                sweepAngle = convertedPrimaryValue,
                useCenter = false,
                style = Stroke(width = 34f, cap = StrokeCap.Round)
            )
        }

        val percentageText = "${finishedTasksPercentage.toInt()}% Done"
        val annotatedString = AnnotatedString.Builder(percentageText).apply {
            addStyle(
                style = SpanStyle(
                    color = SubTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                ),
                start = percentageText.length - 4,  // "Done" begins 4 characters back from the end
                end = percentageText.length
            )
        }

        Text(
            text = annotatedString.toAnnotatedString(),
            fontFamily = Poppins,
            fontSize = 20.sp,
            color = PrimaryTextColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StatisticIndicatorUI() {
    Column(
        modifier = Modifier
            .height(120.dp) ,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IndicatorItemUI(text = "Finished")
        IndicatorItemUI(color = SecondaryColor, text = "On Going")
        IndicatorItemUI(color = Color(0xFFE3E5E7), text = "To do")
    }
}

@Composable
fun IndicatorItemUI(color: Color = PrimaryColor, text:String) {
    Row {
        Icon(
            painter = painterResource(id = R.drawable.ic_circle),
            contentDescription = "",
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = Poppins,
            fontSize = 12.sp,
            color = Color(0xFF818181),
            fontWeight = FontWeight.Normal
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditTaskPage(vm: FormViewModel, idTask: String, teamId: String, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val (teams, setTeams) = remember { mutableStateOf(emptyList<Team>()) }
    val team = remember { mutableStateOf(Team()) }
    val task = remember { mutableStateOf(Task()) }
    val (users, setUsers) = remember { mutableStateOf(emptyList<User>()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        setTeams(vm.getTeams())
        setUsers(vm.getUsers())
    }

    val team_used = vm.getTeambyId(teamId)

    LaunchedEffect(teams) {
        teams.forEach {
            if (team_used != null) {
                if (it.id == team_used.id) {
                    team.value = it
                }
            }
        }
        team.value.tasks.forEach {
            if (it.id == idTask) {
                task.value = it
            }
        }
    }

    val expanded = remember { mutableStateOf(false) }
    val statusOptions = listOf("Finished", "On Going", "To do")
    val selectedStatus = remember { mutableStateOf(task.value.status) }
    val expandedrec = remember { mutableStateOf(false) }
    val recOptions = listOf("Only Once", "Daily", "Monthly", "Every Year")
    val selectedRec = remember { mutableStateOf(task.value.recurring) }

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Insert the information you want to modify",
                    fontFamily = Poppins,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .padding(top = 20.dp)
            ) {
                UserInputField("title", task.value.title, task.value.titleError, task.value::setTitleValue)
                UserInputField("category", task.value.category, task.value.catError, task.value::setCategoryValue)
                UserInputField("tag", task.value.tag, task.value.tagError, task.value::setTagValue)
                UserInputField("due date", task.value.dueDate, task.value.dueDateError, task.value::setDuedateValue)
                UserInputField("description", task.value.description, task.value.descError, task.value::setDescriptionValue)
                Text("Status")
                Box {
                    Button(
                        onClick = { expanded.value = !expanded.value }
                    ) {
                        Text(selectedStatus.value)
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                onClick = {
                                    val oldStatus = selectedStatus.value
                                    selectedStatus.value = status
                                    task.value.setStatusValue(status)
                                    task.value.addModificationValue(vm.me.value.Email, "status", oldStatus, status)
                                    expanded.value = false
                                },
                                text = { Text(text = status) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Recurrence")
                Box {
                    Button(
                        onClick = { expandedrec.value = !expandedrec.value }
                    ) {
                        Text(selectedRec.value)
                    }
                    DropdownMenu(
                        expanded = expandedrec.value,
                        onDismissRequest = { expandedrec.value = false }
                    ) {
                        recOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    val oldRec = selectedRec.value
                                    task.value.setrecurValue(option)
                                    selectedRec.value = option
                                    task.value.addModificationValue(vm.me.value.Email, "recurrence", oldRec, option)
                                    expandedrec.value = false
                                },
                                text = { Text(text = option) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (team_used != null) {
                                vm.editTask(task.value, teamId)
                                if (task.value.validate(vm.me.value.Email, vm.getUsers())) {
                                    if (task.value.status.compareTo("Finished") == 0) {
                                        users.forEach {
                                            if (task.value.users.contains(it.Email)) {
                                                vm.incrementTask(it)
                                            }
                                        }
                                    }
                                    navController.navigate("taskDetails/${idTask}/${teamId}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 40.dp),
                    shape = Shapes.large
                ) {
                    Text(
                        text = "Edit Task",
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskPage(vm: FormViewModel, navController: NavController, teamId: String) {
    val coroutineScope = rememberCoroutineScope()
    val (teams, setTeams) = remember { mutableStateOf(emptyList<Team>()) }
    val task = remember { mutableStateOf(Task()) }
    val expanded = remember { mutableStateOf(false) }
    val recOptions = listOf("Only Once", "Daily", "Monthly", "Every Year")
    val selectedRec = remember { mutableStateOf(task.value.recurring) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        setTeams(vm.getTeams())
    }

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            HeaderUI(navController, vm)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Insert all information to add a new Task",
                    fontFamily = Poppins,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .padding(top = 20.dp)
            ) {
                UserInputField("title", task.value.title, task.value.titleError, task.value::setTitleValue)
                UserInputField("category", task.value.category, task.value.catError, task.value::setCategoryValue)
                UserInputField("tag", task.value.tag, task.value.tagError, task.value::setTagValue)
                UserInputField("due date", task.value.dueDate, task.value.dueDateError, task.value::setDuedateValue)
                UserInputField("description", task.value.description, task.value.descError, task.value::setDescriptionValue)
                UserInputField("Team members assigned", task.value.team, task.value.teamError, task.value::setTeamValue)
                Text("Recurrence")
                Box {
                    Button(
                        onClick = { expanded.value = !expanded.value }
                    ) {
                        Text(selectedRec.value)
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        recOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    task.value.setrecurValue(option)
                                    selectedRec.value = option
                                    expanded.value = false
                                },
                                text = { Text(text = option) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (task.value.validateNew(teams.findLast { it.id == teamId }!!.users)) {
                                vm.addTask(
                                    teams.findLast { it.id == teamId }!!.id,
                                    task.value.title,
                                    task.value.description,
                                    task.value.dueDate,
                                    task.value.tag,
                                    task.value.category,
                                    task.value.team,
                                    task.value.recurring
                                )
                                navController.navigate("taskList/${teamId}")

                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 40.dp),
                    shape = Shapes.large
                ) {
                    Text(
                        text = "Add Task",
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
