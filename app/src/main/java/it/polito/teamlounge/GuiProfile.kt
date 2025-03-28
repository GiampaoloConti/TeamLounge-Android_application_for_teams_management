package it.polito.teamlounge

import android.app.AlertDialog
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.polito.teamlounge.model.MyModel
import it.polito.teamlounge.model.Team
import it.polito.teamlounge.model.User
import it.polito.teamlounge.ui.theme.Poppins
import it.polito.teamlounge.ui.theme.PrimaryTextColor
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresentationPanel(vm: FormViewModel, email: String, navController: NavController){
    val scope = rememberCoroutineScope()
    val usersState = remember { mutableStateOf<List<User>>(emptyList()) }
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }
    val userState = remember { mutableStateOf(User()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val users = vm.getUsers()
            val teams = vm.getTeams()
            usersState.value = users
            teamsState.value = teams

            users.forEach {
                if (email == it.Email) {
                    userState.value = it
                }
            }

            teams.forEach {
                if (it.users.contains("*****")) {
                    vm.removeUserTeam("*****", it)
                    vm.addUserTeam(vm.me.value.Email, it)
                }
            }
        }
    }

    val users by usersState
    val teams by teamsState
    val u by userState

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { navController.navigate("teamCard") },
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
            if (vm.me.collectAsState().value.Email == u.Email) {
                IconButton(
                    onClick = { navController.navigate("EditProfile/${u.Email}") },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                    )
                }
            }
        }
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Icon box will be on the left in landscape mode
                ProfileIconBox(null, u.photoUri, null, u.monogram, vm)
                // Main content will be in a Column to the right of the icon box
                ContentColumn(u, Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon box will be on top in portrait mode
                ProfileIconBox(null, u.photoUri, null, u.monogram, vm)
                // Main content below the icon box
                ContentColumn(u)
            }
        }
    }
}


@Composable
fun ProfileIconBox(
    openCamera: (() -> Unit)?,
    photoUri: String,
    openGallery: (() -> Unit)?,
    monogram: String,
    vm:FormViewModel
) {
    var showOptions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }


    LaunchedEffect(photoUri) {
        vm.retrieveImage(photoUri, object : MyModel.OnImageUriRetrievedListener {
            override fun onImageUriRetrieved(uri: Uri?) {
                imageUri.value = uri
            }
        })
    }

    Box(
        modifier = Modifier
            .padding(48.dp)
            .size(240.dp)
            .border(3.dp, Color.Black, shape = CircleShape)
            .let {
                if (openCamera != null && openGallery != null) {
                    it.clickable {
                        showOptions = !showOptions
                    }
                } else {
                    it
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if ( imageUri.value != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri.value)
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo taken",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
            )
        } else {
            Text(
                monogram,
                modifier = Modifier
                    .padding(16.dp, 0.dp),
                fontSize = 65.sp
            )
        }
    }

    if (showOptions) {
        if (openCamera != null && openGallery != null) {
            ShowOptionsDialog(openCamera, openGallery) { showOptions = false }
        }
    }
}


@Composable
fun ShowOptionsDialog(
    openCamera: () -> Unit,
    openGallery: () -> Unit,
    onCloseDialog: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog.Builder(context)
        .setTitle("Choose an option")
        .setPositiveButton("Take a photo") { _, _ ->
            openCamera()
            onCloseDialog()
        }
        .setNegativeButton("Pick from gallery") { _, _ ->
            openGallery()
            onCloseDialog()
        }
        .setOnDismissListener {
            onCloseDialog()
        }
        .show()
}

@Composable
fun ContentColumn(u: User, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(32.dp))

        UserInfoRow(u.FullName, Icons.Default.Person, "name", false, emptyList(), emptyList())
        UserInfoRow(u.Nickname, Icons.Default.AccountCircle, "nickname", false, emptyList(), emptyList())
        UserInfoRow(u.Role, Icons.Default.Build, "Role", false, emptyList(), emptyList())
        UserInfoRow(u.Email, Icons.Default.Email, "email", false, emptyList(), emptyList())
        UserInfoRow(u.Phone, Icons.Default.Phone, "phone", false, emptyList(), emptyList())
        UserInfoRow(u.Location, Icons.Default.LocationOn, "location", false, emptyList(), emptyList())
        UserInfoRow(u.Description, Icons.Default.Edit, "description", false, emptyList(), emptyList())
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun UserInfoRow(info: String, icon: ImageVector, contentDescription: String, showBarChart: Boolean, barChartData: List<Float>, barData_: List<Int>) {
    Column(
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        if(!showBarChart) {
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    info,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp, 0.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModificationPanel(vm: FormViewModel, email: String, navController: NavController, openCamera: () -> Unit, openGallery: () -> Unit) {
    val scope = rememberCoroutineScope()
    val usersState = remember { mutableStateOf<List<User>>(emptyList()) }
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val users = vm.getUsers()
            val teams = vm.getTeams()
            usersState.value = users
            teamsState.value = teams

            val user = users.find { it.Email == email } ?: User()
            teams.forEach { team ->
                if (team.users.contains(user.Email)) {
                    Log.d("", "ciao")
                    vm.removeUserTeam(user.Email, team)
                    vm.addUserTeam("*****", team)
                }
            }
        }
    }

    val users by usersState
    val teams by teamsState
    val user = users.find { it.Email == email } ?: User()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { navController.navigate("teamCard") },
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
            Button(onClick = {
                if (user.validate()) {
                    scope.launch {
                        vm.model.addUser(user.FullName, user.Nickname, user.Email, user.Phone, user.Location, user.Description, user.Role, 0)
                        vm.setMe(user.Email)
                        navController.navigate("Profile/${user.Email}")
                        vm.checkFirebase = true
                    }
                }
            }, Modifier.padding(end = 12.dp)) {
                Text("Done")
            }
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Icon box will be on the left in landscape mode
                ProfileIconBox(openCamera, user.photoUri, openGallery, user.monogram, vm)
                // Main content will be in a Column to the right of the icon box
                EditContentColumn(user, Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ProfileIconBox(openCamera, user.photoUri, openGallery, user.monogram, vm)

                EditContentColumn(user)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewProfile(vm: FormViewModel, email: String, navController: NavController, openCamera: () -> Unit, openGallery: () -> Unit) {
    val scope = rememberCoroutineScope()
    val usersState = remember { mutableStateOf<List<User>>(emptyList()) }
    val teamsState = remember { mutableStateOf<List<Team>>(emptyList()) }

    LaunchedEffect(Unit, vm.checkFirebase) {
        scope.launch {
            val users = vm.getUsers()
            val teams = vm.getTeams()
            usersState.value = users
            teamsState.value = teams
        }
    }

    val user = User()
    user.setEmailValue(email)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Please fill in your new account's details",
                fontFamily = Poppins,
                fontSize = 16.sp,
                color = PrimaryTextColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(0.66f) // Allocate 2/3 of the row's width
                    .padding(end = 15.dp) // Add padding to the right
            )
            Button(onClick = {
                if (user.validate()) {
                    scope.launch {
                        vm.model.addUser(
                            user.FullName,
                            user.Nickname,
                            email,
                            user.Phone,
                            user.Location,
                            user.Description,
                            user.Role,
                            0
                        )
                        vm.setMe(email)
                        navController.navigate("teamCard")
                        vm.checkFirebase = true
                    }
                }
            }, Modifier
                .weight(0.33f)
                .padding(end = 12.dp)) {
                Text("Done")
            }
        }
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Icon box will be on the left in landscape mode
                ProfileIconBox(openCamera, user.photoUri, openGallery, user.monogram,vm)
                // Main content will be in a Column to the right of the icon box
                EditContentColumn(user, Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon box will be on top in portrait mode
                ProfileIconBox(openCamera, user.photoUri, openGallery, user.monogram, vm)
                // Main content below the icon box
                EditContentColumn(user)
            }
        }
    }
}


@Composable
fun EditContentColumn(u: User, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(32.dp))

        UserInputField("Name", u.FullName, u.FullNameError, u::setNameValue)
        UserInputField("Nickname", u.Nickname, u.NicknameError, u::setNickValue)
        UserInputField("Role", u.Role, u.RoleError, u::setRoleValue)
        UserInputField(
            "Phone Number", u.Phone, u.PhoneError, u::setPhoneValue,
            KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )
        UserInputField("Location", u.Location, u.LocationError, u::setLocationValue)
        UserInputField("Description", u.Description, u.DescriptionError, u::setDescriptionValue)

        Spacer(modifier = Modifier.height(16.dp))
    }
}