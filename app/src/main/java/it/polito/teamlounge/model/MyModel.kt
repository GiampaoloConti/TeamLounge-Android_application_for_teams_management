package it.polito.teamlounge.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import it.polito.teamlounge.FormViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime

class MyModel(context: Context, val applicationScope: CoroutineScope) {
    init {
        FirebaseApp.initializeApp(context)
    }

    val db = Firebase.firestore
    val storage = Firebase.storage
    var storageRef = storage.reference
    var me = MutableStateFlow(User())
        private set

    var users = MutableStateFlow(mutableListOf<User>())
    var teams = MutableStateFlow(mutableListOf<Team>())

    suspend fun fetchUsers(): List<User> {
        return try {
            val result = db.collection("users").get().await()
            result.toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("TeamLoungeModel", "Error fetching users", e)
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchTeams(): List<Team> {
        return try {
            val result = db.collection("teams").get().await()
            val teams = result.toObjects(Team::class.java)
            teams
        } catch (e: Exception) {
            Log.e("TeamLoungeModel", "Error fetching teams", e)
            emptyList()
        }
    }

    fun setMeValue(id: String) {
        applicationScope.launch {
            val currentUsers = fetchUsers()
            currentUsers.forEach { it ->
                if (id == it.Email) {
                    me.tryEmit(it)
                }
            }
        }
    }

    fun changeImage(photo: File){
        applicationScope.launch {
            val profileRef = storageRef.child("${me.value.GetEmailValue()}/Profile.jpg")
            val stream = FileInputStream(photo)
            val uploadTask = profileRef.putStream(stream)
            uploadTask.addOnFailureListener { e->
                Log.e("TeamLoungeModel", "Error adding document", e)
            }.addOnSuccessListener {
                Log.i("TeamLoungeModel", "Image uploaded")
            }
        }
    }

    fun setAttachment(file: File, teamId: String, path: String){
        applicationScope.launch {
            val profileRef = storageRef.child(path)
            val stream = FileInputStream(file)
            val uploadTask = profileRef.putStream(stream)
            uploadTask.addOnFailureListener { e->
                Log.e("TeamLoungeModel", "Error adding document to comment", e)
            }.addOnSuccessListener {
                Log.i("TeamLoungeModel", "file uploaded to comment")
            }
        }
    }

    fun addUser(
        name: String,
        nick: String,
        email: String,
        phone: String,
        loc: String,
        desc: String,
        role: String,
        t: Int
    ) {
        applicationScope.launch {
            Log.i("TeamLoungeModel", email)
            val newU = User(name, nick, email, phone, loc, desc, role, t)
            db.collection("users").document(newU.GetEmailValue()).get()
                .addOnSuccessListener {
                    Log.i("TeamLoungeModel", "Added user ${newU.GetEmailValue()})")
                    val userUpdates = mapOf(
                        "fullName" to name,
                        "nickname" to nick,
                        "role" to email,
                        "phone" to phone,
                        "location" to loc,
                        "description" to desc,
                        "role" to role,
                        "t" to t
                    )
                    db.collection("users").document(newU.GetEmailValue()).update(userUpdates)
                        .addOnSuccessListener {
                            Log.d("Firestore", "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating document", e)
                        }

                }
                .addOnFailureListener { e ->
                    db.collection("users").document(newU.GetEmailValue()).set(newU)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Added new user!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error adding document", e)
                        }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTeam(
        name: String,
        desc: String,
        category: String,
        users: MutableList<String>,
        date: String
    ) {
        applicationScope.launch {
            val currentTeams = fetchTeams()
            val id = generateRandomId()
            val newT = Team(id, name, desc, category, users, date)
            db.collection("teams").document(id).set(newT)
                .addOnSuccessListener {
                    Log.i("TeamLoungeModel", "Added team ${newT.Name})")
                }
                .addOnFailureListener { e ->
                    Log.e("TeamLoungeModel", "Error adding document", e)
                }
        }
    }

    fun addTask(
        teamId: String,
        title: String,
        desc: String,
        dueDate: String,
        tag: String,
        category: String,
        us: String,
        recur: String
    ) {
        applicationScope.launch {
            Log.d("TeamName", teamId)
            val teamRef = db.collection("teams").document(teamId)
            teamRef.get().addOnSuccessListener {
                if (it.data != null) {
                    val newTask = Task(
                        generateRandomId(),
                        title,
                        desc,
                        dueDate,
                        tag,
                        category,
                        us,
                        recur,
                        teamId,
                        comments = emptyList()
                    )
                    teamRef.update("tasks", FieldValue.arrayUnion(newTask))
                        .addOnSuccessListener {
                            Log.i("TeamLoungeModel", "Task added to team with id $teamId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TeamLoungeModel", "Error adding task", e)
                        }
                } else {
                    Log.i("TeamLoungeModel", "Team with id $teamId doesn't exist in firebase")
                }
            }
        }
    }

    fun deleteTeam(id: String) {
        applicationScope.launch {
            db.collection("teams").document(id)
                .delete()
                .addOnSuccessListener {
                    Log.i("TeamLoungeModel", "Deleted team with id $id")
                }
                .addOnFailureListener { e ->
                    Log.e("TeamLoungeModel", "Error deleting team", e)
                }
        }
    }

    fun addUserTeam(email: String, team: Team) {
        applicationScope.launch {
            val teamRef = db.collection("teams").document(team.id)
            teamRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val users =
                        documentSnapshot.get("users") as? MutableList<String> ?: mutableListOf()
                    if (!users.contains(email)) {
                        users.add(email)
                        teamRef.update("users", users)
                    }
                }
            }.addOnFailureListener {
                Log.e("addUserTeam", "Failed to fetch team data: ${it.message}")
            }
        }
    }

    fun removeUserTeam(email: String, team: Team) {
        applicationScope.launch {
            val teamRef = db.collection("teams").document(team.id)
            teamRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val users =
                        documentSnapshot.get("users") as? MutableList<String> ?: mutableListOf()
                    users.remove(email)
                    teamRef.update("users", users)
                }
            }.addOnFailureListener {
                Log.e("removeUserTeam", "Failed to fetch team data: ${it.message}")
            }
        }
    }

    fun removeUserNotSeen(email: String, team: Team) {
        applicationScope.launch {
            val teamRef = db.collection("teams").document(team.id)
            teamRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val not_seen =
                        documentSnapshot.get("not_seen_list") as? MutableList<String>
                            ?: mutableListOf()
                    not_seen.remove(email)
                    teamRef.update("not_seen_list", not_seen)
                    team.not_seen_list.remove(email)
                }
            }.addOnFailureListener {
                Log.e("not_seen_remove", "Failed to fetch team data: ${it.message}")
            }
        }
    }

    fun validateTeam(team: Team) {
        applicationScope.launch {
            val teamRef = db.collection("teams").document(team.id)

            teamRef.set(team)
                .addOnSuccessListener {
                    Log.d(
                        "validateTeam",
                        "Team document successfully updated with name: ${team.Name}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("validateTeam", "Error updating team document with id: ${team.id}", e)
                }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun addMessage(
        user: String,
        text: String,
        taggedUsers: List<String> = emptyList(),
        team: Team,
        vm: FormViewModel
    ) {
        applicationScope.launch {

            val message = Message()
            message.setUserValue(user)
            message.setTextValue(text)
            val time = LocalDateTime.now()
            message.setTimeValue(time)
            message.setTaggedUsersValue(taggedUsers)

            // Add the message to the local list
            val currentMessages = team.chat.messages
            val newMessages = currentMessages.toMutableList().apply { add(message) }
            team.chat.messages = newMessages

            // Create a map representation of the message for Firestore
            val messageMap = mapOf(
                "user" to user,  // Assuming User has a toMap function
                "text" to text,
                "time" to time.toString(),
                "taggedUsers" to taggedUsers
            )

            // Fetch the team document to ensure it exists and then update it
            val teamDocRef = db.collection("teams").document(team.id)

            teamDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Document exists, update the messages array
                    teamDocRef.update("chat.messages", FieldValue.arrayUnion(messageMap))
                        .addOnSuccessListener {
                            // Successfully added message to Firestore
                            println("Message added successfully")
                        }
                        .addOnFailureListener { e ->
                            // Failed to add message to Firestore
                            println("Error adding message: $e")
                        }
                } else {
                    // Document does not exist, handle the error
                    println("Team document does not exist")
                }
            }.addOnFailureListener { e ->
                // Failed to fetch the team document
                println("Error fetching team document: $e")
            }

            // Update the not seen list
            team.setNotseenlistValue(team.users)
            vm.removeNotSeen(user, team)
        }
    }


    fun generateRandomId(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun addUserTask(task: Task, user: String, teamId: String) {
        applicationScope.launch {
            val taskId = task.id
            val teamRef = db.collection("teams").document(teamId)

            teamRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the current tasks array
                    val tasks = document.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Find the specific task by ID and update the users array
                    val updatedTasks = tasks.map { task ->
                        if (task["id"] == taskId) {
                            val users = (task["users"] as? MutableList<String>)?.toMutableList() ?: mutableListOf()
                            if (!users.contains(user)) {
                                users.add(user)
                            }
                            task.toMutableMap().apply { this["users"] = users }
                        } else {
                            task
                        }
                    }

                    // Update the Firestore document
                    teamRef.update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            println("User added successfully")
                        }
                        .addOnFailureListener { e ->
                            println("Error adding user: $e")
                        }
                } else {
                    println("Team with id $teamId doesn't exist in Firebase")
                }
            }.addOnFailureListener { e ->
                println("Error fetching team document: $e")
            }
        }
    }


    fun removeUserTask(task: Task, user: String, teamId: String) {
        applicationScope.launch {
            val taskId = task.id
            val teamRef = db.collection("teams").document(teamId)

            teamRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the current tasks array
                    val tasks = document.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Find the specific task by ID and update the users array
                    val updatedTasks = tasks.map { task ->
                        if (task["id"] == taskId) {
                            val users = (task["users"] as? MutableList<String>)?.toMutableList() ?: mutableListOf()
                            users.remove(user)
                            task.toMutableMap().apply { this["users"] = users }
                        } else {
                            task
                        }
                    }

                    // Update the Firestore document
                    teamRef.update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            println("User removed successfully")
                        }
                        .addOnFailureListener { e ->
                            println("Error removing user: $e")
                        }
                } else {
                    println("Team with id $teamId doesn't exist in Firebase")
                }
            }.addOnFailureListener { e ->
                println("Error fetching team document: $e")
            }
        }
    }

    fun editTask(task: Task, teamId: String) {
        applicationScope.launch {
            val taskId = task.id
            val teamRef = db.collection("teams").document(teamId)

            teamRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the current tasks array
                    val tasks = document.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Convert the new task to a map
                    val newTaskMap = mapOf(
                        "id" to task.id,
                        "title" to task.title,
                        "description" to task.description,
                        "dueDate" to task.dueDate,
                        "tag" to task.tag,
                        "category" to task.category,
                        "team" to task.team,
                        "recurring" to task.recurring,
                        "status" to task.status,
                        "users" to task.users,
                        "comments" to task.comments,
                        "modifications" to task.modifications,
                        "oldTitle" to task.oldTitle,
                        "oldCategory" to task.oldCategory,
                        "oldTag" to task.oldTag,
                        "oldDate" to task.oldDate,
                        "oldDescription" to task.oldDescription,
                        "oldTeam" to task.oldTeam
                    )

                    // Map the tasks, replacing the task with the matching ID
                    val updatedTasks = tasks.map { element ->
                        if (element["id"] == taskId) {
                            newTaskMap
                        } else {
                            element
                        }
                    }

                    // Update the Firestore document
                    teamRef.update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            println("Task updated successfully")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating task: $e")
                        }
                } else {
                    println("Team with id $teamId doesn't exist in Firebase")
                }
            }.addOnFailureListener { e ->
                println("Error fetching team document: $e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCommentToTask(task: Task, teamId: String, userName: String, text: String, file: File?) {
        applicationScope.launch {

            var path: String? = null

            if(file!= null){
                path = "${teamId}/${generateRandomId()}"
            }
            val comment = Comment()
            comment.setUserValue(userName)
            comment.setTextValue(text)
            val time = LocalDateTime.now()
            comment.setTimeValue(time)
            comment.setAttachmentUriValue(path)

            // Add the comment to the local list
            val currentMessages = task.comments
            val newComments = currentMessages.toMutableList().apply { add(comment) }
            task.comments = newComments

            // Create a map representation of the comment for Firestore
            val commentMap = mapOf<String, Any?>(
                "user" to userName,  // Assuming User has a toMap function
                "text" to text,
                "time" to time.toString(),
                "attachmentUri" to path
            )

            // Fetch the team document to ensure it exists and then update it
            val teamDocRef = db.collection("teams").document(teamId)

            teamDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the current tasks array
                    val tasks = document.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    val updatedTasks = tasks.map { element ->
                        if (element["id"] == task.id) {
                            val comments = element["comments"] as? MutableList<Map<String, Any?>> ?: mutableListOf()
                            comments.add(commentMap)
                            if(file!= null){
                                if (path != null) {
                                    setAttachment(file, teamId, path)
                                }
                            }
                            element.toMutableMap().apply { this["comments"] = comments }
                        } else {
                            element
                        }
                    }

                    // Update the Firestore document
                    teamDocRef.update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            println("User removed successfully")
                        }
                        .addOnFailureListener { e ->
                            println("Error removing user: $e")
                        }
                } else {
                    // Document does not exist, handle the error
                    println("Team document does not exist")
                }
            }.addOnFailureListener { e ->
                // Failed to fetch the team document
                println("Error fetching team document: $e")
            }
        }
    }

    fun incrementTask(user: User){
        applicationScope.launch {
            db.collection("users").document(user.Email).update("taskCompleted", user.taskCompleted)
                .addOnSuccessListener {
                    Log.i("TeamLoungeModel", "Updated user ${user.Email})")
                }
                .addOnFailureListener { e ->
                    Log.e("TeamLoungeModel", "Error adding document", e)
                }
        }
    }



    interface OnImageUriRetrievedListener {
        fun onImageUriRetrieved(uri: Uri?)
    }

    fun retrieveImage(photoUri: String, listener: OnImageUriRetrievedListener) {
        if (photoUri.isEmpty() || photoUri == "null") {
            listener.onImageUriRetrieved(null)
            return
        }

        val storageReference = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://teamlounge.appspot.com/$photoUri")
        storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
            listener.onImageUriRetrieved(uri)
        }.addOnFailureListener { exception ->
            listener.onImageUriRetrieved(null)
            Log.e("Firebase", "Failed to retrieve image: ${exception.message}")
        }
    }

    fun retrieveFileComment(uri: String, listener: OnImageUriRetrievedListener){
        if (uri.isEmpty() || uri == "null") {
            listener.onImageUriRetrieved(null)
            return
        }
        Log.d("URI", "gs://teamlounge.appspot.com/$uri")
        val storageReference = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://teamlounge.appspot.com/$uri")
        storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
            listener.onImageUriRetrieved(uri)
        }.addOnFailureListener { exception ->
            listener.onImageUriRetrieved(null)
            Log.e("Firebase", "Failed to retrieve image: ${exception.message}")
        }
    }
}