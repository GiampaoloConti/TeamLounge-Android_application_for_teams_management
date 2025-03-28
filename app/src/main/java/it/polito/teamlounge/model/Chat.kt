package it.polito.teamlounge.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.polito.teamlounge.FormViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Message() {
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(user: String, text: String):this(){
        this.user = user
        this.text = text
    }

    var timeString: String? = null
    var user by mutableStateOf<String?>(null)
        private set
    fun setUserValue(s: String){
        user = s;
    }
    var text by mutableStateOf<String>("")
        private set
    fun setTextValue(s: String){
        text = s;
    }
    var textError by mutableStateOf("")
        private set

    var time: String? = LocalDateTime.now().toString()
     fun setTimeValue(value: LocalDateTime){
         time = value.toString()
     }
    var taggedUsers by mutableStateOf<List<String>>(emptyList())
        private set
    fun setTaggedUsersValue(taggedUsersList: List<String>) {
        taggedUsers = taggedUsersList
    }
}

class Chat {
    var messages by mutableStateOf(mutableListOf<Message>())
}