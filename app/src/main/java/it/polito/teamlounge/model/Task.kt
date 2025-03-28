package it.polito.teamlounge.model

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

class Task() {
    constructor(
        id: String,
        title: String,
        desc: String,
        dueDate: String,
        tag: String,
        category: String,
        us: String,
        recur: String,
        teamId: String,
        comments: List<Comment>
    ) : this() {
        setIDValue(id)
        setTitleValue(title);
        oldTitle = title
        setCategoryValue(category);
        oldCategory = category
        setDescriptionValue(desc);
        oldDescription = desc
        setDuedateValue(dueDate);
        oldDate = dueDate
        setTagValue(tag);
        oldTag = tag
        setTeamValue(us);
        oldTeam = us
        recurring = recur
        setUsersValue(us.split(", ").toMutableList())
        setStatusValue("To do");
        setTeamIdValue(teamId)
        this.comments = comments.toMutableList()
    }

    var recurring by mutableStateOf("Only Once")
    fun setrecurValue(s: String){
        recurring = s;
    }

    var teamId by mutableStateOf("")
        private set
    fun setTeamIdValue(s: String){
        teamId = s;
    }

    var title by mutableStateOf("")
        private set
    fun setTitleValue(s: String){
        title = s;
    }

    var titleError by mutableStateOf("")
        private set
    private fun Checktitle() {
        if(title.isBlank()) {
            titleError = "title cannot be blank"
        }
        else  titleError = ""
    }

    var id by mutableStateOf("")
        private set
    fun setIDValue(word:String){
        id = word
    }
    var description by mutableStateOf("")
        private set
    fun setDescriptionValue(s: String){
        description = s;
    }
    var descError by mutableStateOf("")
        private set
    private fun Checkdesc() {
        if(description.isBlank()) {
            descError = "description cannot be blank"
        }
        else  descError = ""
    }

    var status by mutableStateOf("To do")
        private set
    fun setStatusValue(s: String){
        status = s;
    }
    var statusError by mutableStateOf("")
        private set
    private fun Checkstatus() {
        if(status.isBlank()) {
            statusError = "status cannot be blank"
        }
        else  statusError = ""
    }

    var dueDate by mutableStateOf("")
        private set
    fun setDuedateValue(s: String){
        dueDate = s;
    }
    var dueDateError by mutableStateOf("")
        private set
    private fun Checkdate() {
        if(dueDate.isBlank()) {
            dueDateError = "due date cannot be blank"
        }
        else if(!dueDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) && !dueDate.matches(Regex("\\d{1}/\\d{2}/\\d{4}"))
            && !dueDate.matches(Regex("\\d{2}/\\d{1}/\\d{4}"))
            && !dueDate.matches(Regex("\\d{1}/\\d{1}/\\d{4}"))){
            dueDateError = "insert a valid date"
        }
        else  dueDateError = ""
    }

    var category by mutableStateOf("")
        private set
    fun setCategoryValue(s: String){
        category = s;
    }
    var catError by mutableStateOf("")
        private set
    private fun Checkcat() {
        if(category.isBlank()) {
            catError = "category cannot be blank"
        }
        else  catError = ""
    }

    var tag by mutableStateOf("")
        private set
    fun setTagValue(s: String){
        tag = s;
    }
    var tagError by mutableStateOf("")
        private set
    private fun Checktag() {
        if(tag.isBlank()) {
            tagError = "tag cannot be blank"
        }
        else  tagError = ""
    }

    var users by mutableStateOf(mutableListOf<String>())
        private set
    fun setUsersValue(s: MutableList<String>){
        users = s;
    }
    fun removeUser(name: String) {
        val currentUsers = users
        val newUsers = currentUsers.toMutableList().apply { remove(name) }
        users = newUsers
    }

    var team by mutableStateOf("")
        private set
    fun setTeamValue(s: String){
        team = s;
    }
    var teamError by mutableStateOf("")
        private set
    private fun Checkteam(possibleUsers: List<User>) {
        if(team.isBlank()) {
            teamError = "Team members cannot be blank"
            return
        }
        else {
            team.split(", ").forEach{ userName ->
                if(!possibleUsers.any { user:User -> user.Email.compareTo(userName)==0}){
                    teamError = "Member not found"
                    return
                }
            }
        }
        teamError = ""
    }

    private fun CheckteamNew(possibleUsers: MutableList<String>) {
        if(team.isBlank()) {
            teamError = "Team members cannot be blank"
            return
        }
        else {
            team.split(", ").forEach{ userName ->
                if(!possibleUsers.any { user -> user.compareTo(userName)==0}){
                    teamError = "Member not found"
                    return
                }
            }
        }
        teamError = ""
    }

    var comments by mutableStateOf(mutableListOf<Comment>())
    @RequiresApi(Build.VERSION_CODES.O)
    fun addCommentValue(u: String, t: String, attachmentUri: Uri? = null, isModification: Boolean = false){
        val c = Comment()
        c.setUserValue(u)
        c.setTextValue(t)
        val time = LocalDateTime.now()
        c.setTimeValue(time)
        c.setIsModificationValue(isModification)
        c.setAttachmentUriValue(null)
        val currentComments = comments
        val newComments = currentComments.toMutableList().apply { add(c) }
        comments = newComments
    }


    var modifications by mutableStateOf(mutableListOf<Modification>())
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun addModificationValue(u: String, type: String, old: String, new: String){
        Log.d("Modification", type)
        val m = Modification();
        val time = LocalDateTime.now()
        m.setUserValue(u);
        m.setTypeValue(type);
        m.setOldValue(old)
        m.setNewValue(new)
        m.setTimeValue(time)
        val currentModifications = modifications
        val newModifications = currentModifications.toMutableList().apply { add(m) }
        modifications = newModifications
        addCommentValue("System", "$u changed $type from '$old' to '$new'", null, true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addModificationTeamValue(u: String, old: String, new: String) {
        val m = Modification();
        val time = LocalDateTime.now()
        m.setUserValue(u);
        m.setTypeValue("team");
        m.setOldValue(old)
        m.setNewValue(new)
        m.setTimeValue(time)
        val currentModifications = modifications
        val newModifications = currentModifications.toMutableList().apply { add(m) }
        modifications = newModifications

        val oldTeam = old.split(", ")
        val newTeam = new.split(", ")
        val notInNew = oldTeam.subtract(newTeam.toSet())
        val notInOld = newTeam.subtract(oldTeam.toSet())

        notInNew.forEach {
            addCommentValue("System", "$u removed $it from the team assigned to this task.",null, true)
        }
        notInOld.forEach {
            addCommentValue("System", "$u added $it to the team assigned to this task.",null, true)
        }
    }

    var oldTitle = this.title
    var oldCategory = this.category
    var oldTag = this.tag
    var oldDate = this.dueDate
    var oldDescription = this.description
    var oldTeam = this.team

    @RequiresApi(Build.VERSION_CODES.O)
    fun validate(user: String, possibleUsers: List<User>): Boolean {
        Checktitle()
        Checkcat()
        Checkdate()
        Checktag()
        Checkdesc()
        Checkteam(possibleUsers)
        if(titleError.isBlank() && catError.isBlank() && teamError.isBlank()
            && dueDateError.isBlank() && tagError.isBlank() && descError.isBlank()) {
            if (oldTitle != title) {
                addModificationValue(user, "title", oldTitle, title)
                oldTitle = title
            }
            if (oldCategory != category) {
                addModificationValue(user, "category", oldCategory, category)
                oldCategory = category
            }
            if (oldTag != tag) {
                addModificationValue(user, "tag", oldTag, tag)
                oldTag = tag
            }
            if (oldDate != dueDate) {
                addModificationValue(user, "due date", oldDate, dueDate)
                oldDate = dueDate
            }
            if (oldDescription != description) {
                addModificationValue(user, "description", oldDescription, description)
                oldDescription = description
            }
            if (oldTeam != team) {
                addModificationTeamValue(user, oldTeam, team)
                oldTeam = team
            }
            setUsersValue(team.split(", ").toMutableList())
            return true
        }
        return false
    }

    fun validateNew(possibleUsers: MutableList<String>): Boolean {
        Checktitle()
        Checkcat()
        Checkdate()
        Checktag()
        Checkdesc()
        CheckteamNew(possibleUsers)
        if(titleError.isBlank() && catError.isBlank() && teamError.isBlank()
            && dueDateError.isBlank() && tagError.isBlank() && descError.isBlank()) {
            return true
        }
        return false
    }
}