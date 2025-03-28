package it.polito.teamlounge

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import it.polito.teamlounge.model.User
import it.polito.teamlounge.model.Team
import it.polito.teamlounge.model.MyModel
import it.polito.teamlounge.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class Factory(context: Context): ViewModelProvider.Factory {
    val model: MyModel = (context.applicationContext as? MyApplication)?.model ?: throw IllegalArgumentException("Bad application")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FormViewModel::class.java))
            FormViewModel(model) as T
        else if (modelClass.isAssignableFrom(SignInViewModel::class.java))
            SignInViewModel(model) as T
        else throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class SignInState {
    data class Success(val user: FirebaseUser, val newAccount: Boolean) : SignInState()
    data class Failure(val error: Throwable) : SignInState()
    data object Idle : SignInState()
}

class SignInViewModel(val model: MyModel): ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState

    fun setSignInState(signInState: SignInState) {
        _signInState.value = signInState
    }

    fun addUser(email: String, phone: String) {
        model.addUser("", "", email, phone, "", "", "", 0)
    }

}

class FormViewModel(val model: MyModel): ViewModel() {
    val teams = MutableStateFlow<List<Team>>(emptyList())
    val users = MutableStateFlow<List<User>>(emptyList())
    val me = model.me
    var checkFirebase = false
    suspend fun getUsers() = model.fetchUsers()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTeams() = model.fetchTeams()


    fun addTask(
        teamId: String,
        title: String,
        desc: String,
        dueDate: String,
        tag: String,
        category: String,
        us: String,
        recur: String
    ) = model.addTask(teamId, title, desc, dueDate, tag, category, us, recur)

    fun setMe(id: String) = model.setMeValue(id)
    fun addUser(
        name: String,
        nick: String,
        email: String,
        phone: String,
        loc: String,
        desc: String,
        role: String,
        t: Int
    ) = model.addUser(name, nick, email, phone, loc, desc, role, t)

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTeam(
        name: String,
        desc: String,
        category: String,
        users: MutableList<String>,
        date: String
    ) = model.addTeam(name, desc, category, users, date)

    fun deleteTeam(id: String) = model.deleteTeam(id)

    fun addUserTeam(email: String, team: Team) = model.addUserTeam(email, team)
    fun removeUserTeam(email: String, team: Team) = model.removeUserTeam(email, team)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendMessage(team: Team, user: User, message: String) {
        val tagPattern = Regex("""@\w+@+\w+.com""")
        val taggedUserNames = tagPattern.findAll(message)
            .map { it.value.substring(1).trim() } // remove the @ and trim whitespace
            .toSet()
            .toList()
        val taggedUsers = taggedUserNames
            .mapNotNull { user -> getUsers()!!.find { u -> u.Email == user } }
        val taggedUserString = taggedUsers.mapNotNull { user -> user.Email }
        Log.d("tagged", taggedUserString.toString())
        model.addMessage(user.Email, message, taggedUserString, team, this)
    }

    fun validateTeam(team: Team): Boolean {
        if (team.validate()) {
            model.validateTeam(team)
            return true
        }
        return false
    }

    fun removeNotSeen(email: String, team: Team) = model.removeUserNotSeen(email, team)

    suspend fun validateTeamNew(team: Team): Boolean {
        return team.validateNew(getUsers())
    }

    var newMember by mutableStateOf("")
    var newMemberError by mutableStateOf("")
    suspend fun validateTeamMember(team: Team): Boolean {
        if (getUsers()!!.any { Log.d("name", it.Email);it.Email == newMember }) {
            if (newMember in team.users) {
                newMemberError = "Member is already in the team"
                return false
            }
            newMemberError = ""
            return true
        } else {
            newMemberError = "Member not found"
            return false
        }
    }

    var newTaskMember by mutableStateOf("")
    var newTaskMemberError by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun validateTaskMember(task: Task, id: String): Boolean {
        val team = getTeams()!!.findLast { it.id == id }
        if (team != null) {
            if (team.users.any { Log.d("name", it);it.compareTo(newTaskMember) == 0 }) {
                if (newTaskMember in task.users) {
                    newTaskMemberError = "Member is already in the team"
                    return false
                }
                newTaskMemberError = ""
                return true
            } else {
                newTaskMemberError = "Member not found"
                return false
            }
        } else {
            newTaskMemberError = "Team doesn't exist"
            return false
        }
    }

    fun setPhotoUriValue(photo: File) {
        model.changeImage(photo)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCommentToTask(task: Task, teamId: String, userName: String, text: String, file: File?) {
        model.addCommentToTask(task, teamId, userName, text, file)
    }


    fun getTeambyId(Name: String): Team? {
        return teams.value.find { it.id == Name }
    }

    fun addUserTask(task: Task, user: String, teamId: String) {
        val currentUsers = task.users
        val newUsers = currentUsers.toMutableList().apply { add(user) }
        task.setUsersValue(newUsers)
        model.addUserTask(task, user, teamId)
    }

    fun removeUserTask(task: Task, user: String, teamId: String) {
        val currentUsers = task.users
        val newUsers = currentUsers.toMutableList().apply { remove(user) }
        task.setUsersValue(newUsers)
        model.removeUserTask(task, user, teamId)
    }

    fun editTask(task: Task, teamId: String) = model.editTask(task, teamId)

    fun incrementTask(user: User) {
        user.taskCompleted += 1
        model.incrementTask(user)
    }

    fun retrieveImage(photoUri: String, listener: MyModel.OnImageUriRetrievedListener) =
        model.retrieveImage(photoUri, listener)

    fun retrieveFileComment(uri: String, listener: MyModel.OnImageUriRetrievedListener) =
        model.retrieveFileComment(uri, listener)

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "tempFile")
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
