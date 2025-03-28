package it.polito.teamlounge.model

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Team() {

    constructor(
        id: String,
        name: String,
        desc: String,
        category: String,
        users: List<String>,
        date: String
    ) : this() {
        this.id = id
        this.Name = name
        this.description = desc
        this.category = category
        this.users = users.toMutableList()
        this.CreationDate = date
        this.setMonogramValue(name)
        this.not_seen_list = users.toMutableList()
    }

    constructor(
        id: String,
        name: String,
        desc: String,
        category: String,
        users: List<String>,
        date: String,
        tasks: List<Task>
    ) : this(id, name, desc, category, users, date) {
        this.tasks = tasks.toMutableList()
    }

    fun validate(): Boolean {
        CheckName()
        Checkcat()
        Checkdesc()
        if (NameError.isBlank() && catError.isBlank() && descError.isBlank()) {
            setMonogramValue(Name)
            return true
        }
        return false
    }

    fun validateNew(possibleUsers: List<User>): Boolean {
        CheckName()
        Checkcat()
        Checkdesc()
        Checkteam(possibleUsers)
        if (NameError.isBlank() && catError.isBlank() && descError.isBlank() && userListError.isBlank()) {
            setMonogramValue(Name)
            return true
        }
        return false
    }

    var id by mutableStateOf("")
        private set

    fun setIdValue(s: String) {
        id = s
    }

    fun getIdValue(): String {
        return id
    }

    var monogram by mutableStateOf("")
        private set

    fun setMonogramValue(name: String) {
        monogram = ""
        for (n in name.split(" ")) {
            monogram += n[0]
        }
    }

    var Name by mutableStateOf("")
        private set
    var NameError by mutableStateOf("")
        private set

    fun setNameValue(name: String) {
        Name = name
    }

    fun getNameValue(): String {
        return Name
    }

    private fun CheckName() {
        if (Name.isBlank()) {
            NameError = "Name cannot be blank"
        } else if (Name.contains("  ")) {
            NameError = "Name cannot contain multiple spaces"
        } else if (Name.endsWith(" ") || Name.startsWith(" ")) {
            NameError = "Name cannot start or end with a space"
        } else NameError = ""
    }

    var description by mutableStateOf("")
        private set

    fun setDescriptionValue(s: String) {
        description = s
    }

    var descError by mutableStateOf("")
        private set

    private fun Checkdesc() {
        if (description.isBlank()) {
            descError = "description cannot be blank"
        } else descError = ""
    }

    fun getDescValue(): String {
        return description
    }

    var users by mutableStateOf(mutableListOf<String>())
        private set


    var userList by mutableStateOf("")
        private set

    fun setUserListValue(s: String) {
        userList = s
    }

    var not_seen_list by mutableStateOf(mutableListOf<String>())
        private set

    fun setNotseenlistValue(s: MutableList<String>) {
        this.not_seen_list = s.toMutableList() // Create a new list instance
        Log.d("Not_seen", this.not_seen_list.toString())
    }


    fun checkUser(user: String): Boolean {
        Log.d("Not_seen_list_check", this.not_seen_list.toString())
        return this.not_seen_list.contains(user)
    }


    var userListError by mutableStateOf("")
        private set

    private fun Checkteam(possibleUsers: List<User>) {
        if (userList.isBlank()) {
            userListError = "Team members cannot be blank"
            return
        } else {
            userList.split(", ").forEach { userName ->
                if (!possibleUsers.any { user -> user.Email == userName }) {
                    userListError = "Member not found"
                    Log.d("Lista", possibleUsers.toString())
                    return
                }
            }
        }
        userListError = ""
    }

    var CreationDate by mutableStateOf("")
        private set

    fun setdateValue(s: String) {
        CreationDate = s;
    }

    fun getDateValue(): String {
        return CreationDate
    }

    var DateError by mutableStateOf("")
        private set

    private fun Checkdate() {
        if (CreationDate.isBlank()) {
            DateError = " Date cannot be blank"
        } else if (!CreationDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) && !CreationDate.matches(
                Regex("\\d{1}/\\d{2}/\\d{4}")
            )
            && !CreationDate.matches(Regex("\\d{2}/\\d{1}/\\d{4}"))
            && !CreationDate.matches(Regex("\\d{1}/\\d{1}/\\d{4}"))
        ) {
            DateError = "insert a valid date"
        } else DateError = ""
    }

    var category by mutableStateOf("")
        private set

    fun setCategoryValue(s: String) {
        category = s;
    }

    fun getCatValue(): String {
        return category
    }

    var catError by mutableStateOf("")
        private set

    private fun Checkcat() {
        if (category.isBlank()) {
            catError = "category cannot be blank"
        } else catError = ""
    }

    var photoUri by mutableStateOf<Uri?>(null)
        private set

    fun setPhotoUriValue(photo_uri: Uri?) {
        photoUri = photo_uri
    }

    var chat by mutableStateOf<Chat>(Chat())

    var tasks by mutableStateOf(mutableListOf<Task>())
}
