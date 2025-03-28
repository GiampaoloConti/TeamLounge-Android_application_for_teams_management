package it.polito.teamlounge

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.teamlounge.model.MyModel
import it.polito.teamlounge.ui.theme.TeamLoungeTheme
import java.io.File
import kotlin.reflect.KSuspendFunction0

class MainActivity : ComponentActivity() {
    val webClientId: String = "375866406421-etj4b3g5m1hb86h05rqmn2msrigr4422.apps.googleusercontent.com"

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var credentialManager: CredentialManager

    companion object {
        private const val CAMERA_PERMISSION_CODE: Int = 1
    }

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val formViewModel: FormViewModel by viewModels { Factory(this) }
    private val signInViewModel: SignInViewModel by viewModels { Factory(this) }
    private lateinit var model: MyModel
    private lateinit var photoUri: Uri
    private lateinit var imageFile: File

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MyApplication
        model = app.model
        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        setContent {
            AppNavigation(formViewModel, signInViewModel, openCamera = ::checkCameraPermissionsAndOpenCamera, openGallery = ::pickImage, handleSignIn = ::handleSignIn)
        }
        photoUri = createUri()
        registerPictureLauncher()
        registerGalleryLauncher()
    }
    private fun createUri(): Uri {
        imageFile = File(applicationContext.filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            applicationContext,
            "it.polito.teamlounge.fileProvider",
            imageFile
        )
    }

    private fun registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()) {
                result ->
            try {
                if (result) {
                    Toast.makeText(this@MainActivity.applicationContext, "Photo taken", Toast.LENGTH_SHORT).show()
                    formViewModel.setPhotoUriValue(imageFile)
                }
            } catch (exception : Exception) {
                exception.printStackTrace()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        galleryLauncher.launch(intent)
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var res : String? = null

        var proj = arrayOf(MediaStore.Images.Media.DATA)
        var cursor : Cursor? = contentResolver.query(contentUri, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                var columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                res = cursor.getString(columnIndex)
            }
            cursor.close()
        }

        return res
    }

    private fun registerGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result ->
            try {
                val imageUri = result.data?.data
                Log.i("Gallery", imageUri.toString())
                val path = imageUri?.let { getPathFromUri(it) }
                if (path != null) {
                    Log.i("Gallery", path)
                    val f = File(path)
                    val imageFile = File(applicationContext.filesDir, "gallery_photo.jpg");
                    formViewModel.setPhotoUriValue(imageFile)
                }
            } catch (exception : Exception) {
                exception.printStackTrace()
            }
        }
    }

    private fun checkCameraPermissionsAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            getPermissionsRequest.launch(android.Manifest.permission.CAMERA)
        } else {
            takePictureLauncher.launch(photoUri)
        }
    }

    private val getPermissionsRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(photoUri)
        }
        else {
            Toast.makeText(this@MainActivity, "Camera permission denied, please allow permission to take picture", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun handleSignIn() {
        val googleIdOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        Log.d("TeamLoungeAuth", request.credentialOptions.map { it -> it.toString() }.toString())

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = this
            )
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken

                            // Authenticate with Firebase using the Google ID token
                            firebaseAuthWithGoogle(idToken)
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e("TeamLoungeAuth", "Received an invalid google id token response", e)
                            signInViewModel.setSignInState(SignInState.Failure(e))
                        }
                    } else {
                        Log.e("TeamLoungeAuth", "Unexpected type of credential")
                        signInViewModel.setSignInState(SignInState.Failure(Exception("Unexpected type of credential")))
                    }
                }
                else -> {
                    Log.e("TeamLoungeAuth", "Unexpected type of credential")
                    signInViewModel.setSignInState(SignInState.Failure(Exception("Unexpected type of credential")))
                }
            }
        } catch (e: GetCredentialException) {
            Log.e("TeamLoungeAuth", "GetCredentialException")
            e.printStackTrace()
            signInViewModel.setSignInState(SignInState.Failure(e))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val firebase = FirebaseFirestore.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.email != null) {
                        Log.d("TeamLoungeAuth", "User Email: ${user.email}")
                        Log.d("TeamLoungeAuth", "User Phone Number: ${user.phoneNumber}")
                        firebase.collection("users")
                            .whereEqualTo("email", user.email)
                            .get()
                            .addOnCompleteListener { query ->
                                Log.i("TeamLoungeQuery", "Query start")
                                if (query.isSuccessful) {
                                    Log.i("TeamLoungeQuery", "Query successful")
                                    val documents = query.result
                                    if (documents != null && !documents.isEmpty) {
                                        Log.i("TeamLoungeQuery", "Documents != null")
                                        val document = documents.documents[0]
                                        val userId = document.get("email")?.toString()
                                        if (userId != null) {
                                            Log.i("TeamLoungeQuery", "UserId != null")
                                            model.setMeValue(userId)
                                            signInViewModel.setSignInState(SignInState.Success(user, false))
                                        }
                                        else {
                                            signInViewModel.setSignInState(SignInState.Failure(Exception("Email in use but has no user id")))
                                        }
                                    } else {
                                        Log.i("TeamLoungeQuery", "New account")
                                        signInViewModel.setSignInState(SignInState.Success(user, true))
                                    }
                                } else {
                                    Log.w("TeamLoungeAuth", "Error checking user in Firestore", query.exception)
                                    signInViewModel.setSignInState(SignInState.Failure(query.exception ?: Exception("Error checking Firestore")))
                                }
                            }
                    } else {
                        signInViewModel.setSignInState(SignInState.Failure(Exception("User is null or has no email")))
                    }
                } else {
                    Log.w("TeamLoungeAuth", "signInWithCredential:failure", task.exception)
                    signInViewModel.setSignInState(SignInState.Failure(task.exception ?: Exception("Firebase Auth failed")))
                }
            }
    }
}

@Composable
fun SignInScreen(signInViewModel: SignInViewModel, handleSignIn: KSuspendFunction0<Unit>, navController: NavController, id: String?) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val signInResult by signInViewModel.signInState.collectAsState()

    LaunchedEffect(signInResult) {
        Log.i("LaunchedEffect", "Launched Effect Start")
        when (signInResult) {
            is SignInState.Success -> {
                Log.i("LaunchedEffect", "Success")
                val success = signInResult as SignInState.Success
                if (success.newAccount) {
                    Log.i("LaunchedEffect", "Success new account")
                    navController.navigate("NewProfile/${success.user.email}")
                } else {
                    Log.i("LaunchedEffect", "Success existing account")
                    if(id == null){
                        navController.navigate("teamCard")
                    }else{
                        navController.navigate("teamDetails/${id}")
                    }

                }
            }

            is SignInState.Failure -> {
                Log.i("LaunchedEffect", "Failure")
                Toast.makeText(
                    context,
                    "Sign-in failed: ${(signInResult as SignInState.Failure).error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            SignInState.Idle -> {
                Log.i("LaunchedEffect", "Idle")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                text = "Welcome to TeamLounge!",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(0.8f))
            Button(modifier = Modifier
                    .height(100.dp)
                    .width(210.dp),
                onClick = {
                coroutineScope.launch {
                    handleSignIn()
                }
            }) {
                Text(
                    text = "Sign in with Google",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(formVm: FormViewModel, signInVm: SignInViewModel, openCamera: () -> Unit, openGallery: () -> Unit, handleSignIn: KSuspendFunction0<Unit>) {

    val uri = "https://team"
    val navController = rememberNavController()
    val reloadTrigger = remember{mutableStateOf(0)}  // Add this line

    TeamLoungeTheme {
        NavHost(navController = navController, startDestination = "signIn") {
            composable("signIn") { SignInScreen(signInVm, handleSignIn = handleSignIn, navController = navController, null) }
            composable("teamCard") { TeamListUI(formVm, navController, reloadTrigger) }
            composable("teamDetails/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                ShowTeamDetails(formVm, id = backStackEntry.arguments?.getString("id") ?: "404", navController)
            }
            composable(
                "team/{id}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "$uri/{id}"
                    action = Intent.ACTION_VIEW
                }),
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        defaultValue = "-1"
                    }
                )
            ) { backStackEntry ->
                SignInScreen(signInVm, handleSignIn = handleSignIn, navController = navController, id = backStackEntry.arguments?.getString("id") ?: "404")
            }
            composable("AddTeam") { AddTeamPage(navController, formVm, reloadTrigger) }
            composable("EditTeam/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                EditTeamPage(navController, formVm, id = backStackEntry.arguments?.getString("id") ?: "404")
            }
            composable("Chat/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                ChatScreen(formVm, id = backStackEntry.arguments?.getString("id") ?: "404", navController)
            }
            composable("Profile/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                PresentationPanel(formVm, email = (backStackEntry.arguments?.getString("email") ?: ""), navController)
            }
            composable("EditProfile/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                ModificationPanel(formVm, email = backStackEntry.arguments?.getString("email") ?: "", navController, openCamera, openGallery)
            }
            composable("NewProfile/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                NewProfile(formVm, email = backStackEntry.arguments?.getString("email") ?: "", navController, openCamera, openGallery)
            }
            composable("TaskList/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                TaskListUI(formVm, navController, id = backStackEntry.arguments?.getString("id") ?: "404")
            }
            composable("taskDetails/{idTask}/{idTeam}",
                arguments = listOf(navArgument("idTask") { type = NavType.StringType },
                    navArgument("idTeam") { type = NavType.StringType })
            ) { backStackEntry ->
                TaskDetailsPage(formVm, idTask = backStackEntry.arguments?.getString("idTask") ?: "", idteam = backStackEntry.arguments?.getString("idTeam") ?: "404", navController)
            }
            composable("EditTask/{idTask}/{teamId}",
                arguments = listOf(navArgument("idTask") { type = NavType.StringType },
                    navArgument("teamId") { type = NavType.StringType })
            ) { backStackEntry ->
                EditTaskPage(formVm, idTask = backStackEntry.arguments?.getString("idTask") ?: "", teamId = backStackEntry.arguments?.getString("teamId") ?: "", navController)
            }
            composable("AddTask/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
            ) { backStackEntry ->
                AddTaskPage(formVm, navController, teamId = backStackEntry.arguments?.getString("teamId") ?: "404")
            }
        }
    }
}
