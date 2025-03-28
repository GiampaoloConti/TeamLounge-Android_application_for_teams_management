package it.polito.teamlounge

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import it.polito.teamlounge.model.MyModel
import it.polito.teamlounge.model.Team
import it.polito.teamlounge.model.User
import it.polito.teamlounge.ui.theme.BackgroundColor
import it.polito.teamlounge.ui.theme.Poppins
import it.polito.teamlounge.ui.theme.PrimaryColor
import it.polito.teamlounge.ui.theme.PrimaryTextColor
import it.polito.teamlounge.ui.theme.SecondaryColor
import it.polito.teamlounge.ui.theme.Shapes
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamListUI(vm: FormViewModel, navController: NavController, reloadTrigger: MutableState<Int>) {
    LaunchedEffect(reloadTrigger.value) {
        vm.teams.value = vm.getTeams().toMutableList()
        vm.users.value = vm.getUsers().toMutableList()
    }

    val user by vm.me.collectAsState()
    val teams by vm.teams.collectAsState()
    val switchState = remember { mutableStateOf(false) }
    val sortState = remember { mutableStateOf("ascending") }

    val sortedTeams = remember(teams, sortState.value) {
        if (sortState.value == "ascending") {
            teams.sortedBy { it.CreationDate }
        } else {
            teams.sortedByDescending { it.CreationDate }
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
                            text = "Hello, ${user.FullName}",
                            fontFamily = Poppins,
                            fontSize = 18.sp,
                            color = PrimaryTextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Team List:",
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
                        .padding(top = 16.dp, start = 10.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Show Only My Teams:  ",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        modifier = Modifier.weight(0.2f)
                    )
                    Switch(
                        checked = switchState.value,
                        onCheckedChange = { switchState.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryColor,
                            uncheckedThumbColor = Color.Gray
                        ),
                        modifier = Modifier.weight(0.2f)
                    )

                    TextButton(
                        onClick = { sortState.value = "ascending" },
                        colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent),
                        modifier = Modifier.weight(0.2f)
                    ) {
                        Text(
                            "Ascend",
                            color = Color.Gray,
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                        )
                    }
                    TextButton(
                        onClick = { sortState.value = "descending" },
                        colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent),
                        modifier = Modifier.weight(0.2f)
                    ) {
                        Text(
                            "Descend",
                            color = Color.Gray,
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                        )
                    }
                }
                if (sortedTeams.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No team yet",
                            fontFamily = Poppins,
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (switchState.value) {
                    sortedTeams.forEach {
                        if (it.users.contains(vm.me.value.Email)) {
                            TeamCardUI(navController, it, vm)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    sortedTeams.forEach {
                        TeamCardUI(navController, it, vm)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
            FloatingActionButton(
                onClick = { navController.navigate("AddTeam") },
                containerColor = PrimaryColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.padding(all = 4.dp),
                    text = "New Team +",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditTeamPage(navController: NavController, vm: FormViewModel, id: String) {
    val scope = rememberCoroutineScope()
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }
    val team = remember { mutableStateOf(Team()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val teams = vm.getTeams()
            teamsState.value = teams
            teams.forEach {
                if (it.id == id) {
                    team.value = it
                }
            }
        }
    }

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
                UserInputField("Name", team.value.Name, team.value.NameError, team.value::setNameValue)
                UserInputField("Category", team.value.category, team.value.catError, team.value::setCategoryValue)
                UserInputField("Description", team.value.description, team.value.descError, team.value::setDescriptionValue)

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (vm.validateTeam(team.value)) {
                            navController.navigate("teamDetails/${team.value.id}")
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
                        text = "Edit Team",
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun UserInputField(label: String, value: String, error: String, onValueChange: (String) -> Unit,
                   keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    Column() {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error.isNotBlank(),
            keyboardOptions = keyboardOptions
        )
        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTeamPage(navController: NavController, vm: FormViewModel, reloadTrigger: MutableState<Int>) {
    val scope = rememberCoroutineScope()
    val team = remember { mutableStateOf(Team()) }

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
                    text = "Insert all information to add a new Team",
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
                UserInputField("Name", team.value.Name, team.value.NameError, team.value::setNameValue)
                UserInputField("Category", team.value.category, team.value.catError, team.value::setCategoryValue)
                UserInputField("Description", team.value.description, team.value.descError, team.value::setDescriptionValue)
                UserInputField("Team members", team.value.userList, team.value.userListError, team.value::setUserListValue)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (vm.validateTeamNew(team.value)) {
                                val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                vm.addTeam(team.value.Name, team.value.description, team.value.category, team.value.userList.split(", ").toMutableList(), LocalDateTime.now().format(dateTimeFormatter))
                                navController.navigate("teamCard")
                                reloadTrigger.value++  // Increment the reload trigger to force recomposition
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
                        text = "Add Team",
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}




fun generateQRCode(content: String, width: Int, height: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else BackgroundColor.toArgb())
        }
    }
    return bitmap
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowTeamDetails(vm: FormViewModel, id: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val usersState = remember { mutableStateOf<List<User>>(emptyList()) }
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }
    val teamState = remember { mutableStateOf(Team()) }
    val reloadTrigger = remember { mutableStateOf(0) }  // State to trigger recomposition

    // Trigger LaunchedEffect when reloadTrigger changes
    LaunchedEffect(reloadTrigger.value, vm.checkFirebase) {
        scope.launch {
            val users = vm.getUsers()
            val teams = vm.getTeams()
            usersState.value = users
            teamsState.value = teams

            teams.forEach {
                if (it.id == id) {
                    teamState.value = it
                }
            }
        }
    }

    val users by usersState
    val team by teamState
    val me by vm.me.collectAsState()

    // Generate QR Code
    val url = "https://team/$id"
    val qrCodeBitmap = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        qrCodeBitmap.value = generateQRCode(url, 400, 400)
    }

    val completedTasks = team.users.associateWith { user ->
        var ntask = -1
        users.forEach {
            if (user.compareTo(it.Email) == 0) {
                ntask = it.taskCompleted
            }
        }
        ntask
    }
    val maxTasks = completedTasks.values.maxOrNull() ?: 0

    Surface(color = BackgroundColor, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp) // Add padding to prevent content overlap with the FAB
            ) {
                HeaderUI(navController, vm)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = team.Name,
                            fontFamily = Poppins,
                            fontSize = 25.sp,
                            color = PrimaryTextColor,
                            modifier = Modifier.padding(start = 10.dp),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = team.category,
                            fontFamily = Poppins,
                            fontSize = 15.sp,
                            color = PrimaryTextColor,
                            modifier = Modifier.padding(start = 15.dp),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = team.CreationDate,
                            fontFamily = Poppins,
                            fontSize = 15.sp,
                            color = PrimaryTextColor,
                            modifier = Modifier.padding(start = 15.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = team.description,
                            fontFamily = Poppins,
                            fontSize = 15.sp,
                            color = PrimaryTextColor,
                            modifier = Modifier.padding(start = 15.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "TeamIcon",
                                tint = Color.Gray,
                            )
                            Text(
                                text = "Team members:",
                                fontFamily = Poppins,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(start = 10.dp),
                                color = PrimaryTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp)
                ) {
                    for (user in team.users) {
                        val imageUri = remember { mutableStateOf<Uri?>(null) }
                        var userObj: User? = null
                        users.forEach {
                            if (user.compareTo(it.Email) == 0) {
                                userObj = it
                            }
                        }
                        LaunchedEffect(userObj?.photoUri) {
                            userObj?.let {
                                vm.retrieveImage(it.photoUri, object : MyModel.OnImageUriRetrievedListener {
                                    override fun onImageUriRetrieved(uri: Uri?) {
                                        imageUri.value = uri
                                    }
                                })
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
                                if (imageUri.value != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUri.value)
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
                            if (team.users.contains(me.Email)) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            vm.removeUserTeam(user, team)
                                            reloadTrigger.value++
                                            vm.checkFirebase = true
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
                    if (team.users.contains(me.Email)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserInputField("New member", vm.newMember, vm.newMemberError, { newValue -> vm.newMember = newValue })
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (vm.validateTeamMember(team)) {
                                            vm.addUserTeam(vm.newMember, team)
                                            reloadTrigger.value++
                                            vm.checkFirebase = true
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
                            onClick = { navController.navigate("EditTeam/${id}") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .padding(top = 40.dp),
                            shape = Shapes.large
                        ) {
                            Row {
                                Text(
                                    text = "Edit team",
                                    fontFamily = Poppins,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                vm.deleteTeam(team.id)
                                navController.navigate("teamCard")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .padding(top = 40.dp),
                            shape = Shapes.large
                        ) {
                            Row {
                                Text(
                                    text = "Delete Team",
                                    fontFamily = Poppins,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    vm.addUserTeam(vm.me.value.Email, team)
                                    reloadTrigger.value++
                                    vm.checkFirebase = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .padding(top = 40.dp),
                            shape = Shapes.large
                        ) {
                            Row {
                                Text(
                                    text = "Join team",
                                    fontFamily = Poppins,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { navController.navigate("TaskList/${id}") },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .padding(top = 40.dp),
                        shape = Shapes.large
                    ) {
                        Text(
                            text = "Go to assigned tasks",
                            fontFamily = Poppins,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    // Bar Graph
                    Text(
                        text = "Tasks Completed by Team Members",
                        fontFamily = Poppins,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor,
                        modifier = Modifier.padding(start = 10.dp, top = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    BarGraph(completedTasks = completedTasks, maxTasks = maxTasks)
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        qrCodeBitmap.value?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(200.dp) // Adjust the size as needed
                            )
                            Text(
                                text = "Share this QR Code to invite members to the team",
                                fontFamily = Poppins,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("Chat/${id}") },
                    containerColor = PrimaryColor
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (team.checkUser(me.Email)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, shape = CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun BarGraph(completedTasks: Map<String, Int>, maxTasks: Int) {
    val maxBarWidth = 240.dp // Maximum width for the bar
    val spacing = 8.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        completedTasks.forEach { (member, tasks) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Draw member name and number of completed tasks
                Text(
                    text = "$member ($tasks)",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = PrimaryTextColor,
                    modifier = Modifier.width(120.dp),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Calculate the width of the bar proportionally
                val barWidth =
                    (maxBarWidth * (tasks.toFloat() / maxTasks.toFloat())).coerceAtMost(maxBarWidth)
                // Draw the bar
                Box(
                    modifier = Modifier
                        .height(20.dp) // Adjust the height of the bar as needed
                        .width(barWidth)
                        .background(color = PrimaryColor)
                )
            }
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}

@Composable
fun TeamCardUI(navController: NavController, team: Team, vm: FormViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 40.dp)
    ) {
        Button(
            onClick = {navController.navigate("teamDetails/${team.id}")},
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Name: ${team.getNameValue()}",
                            fontFamily = Poppins,
                            fontSize = 18.sp,
                            color = PrimaryTextColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Category: ${team.getCatValue()}",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Description: ${team.getDescValue()}",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Creation Date: ${team.getDateValue()}",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = Color(0xFF292D32),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (team.checkUser(vm.me.collectAsState().value.Email)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Red, shape = CircleShape)
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }
        }
    }
}

@Composable
fun HeaderUI(navController: NavController, vm: FormViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {navController.navigate("teamCard")},
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
            )
        }
        IconButton(
            onClick = {navController.navigate("Profile/${vm.me.value.Email}")},
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
            )
        }
    }
}