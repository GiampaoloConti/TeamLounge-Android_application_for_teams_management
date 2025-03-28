package it.polito.teamlounge.model

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Comment() {
    var user by mutableStateOf<String>("")
        private set
    fun setUserValue(s: String){
        user = s
    }
    var text by mutableStateOf<String>("")
        private set
    fun setTextValue(s: String){
        text = s
    }
    var textError by mutableStateOf("")
        private set
    var time: String? = LocalDateTime.now().toString()
    fun setTimeValue(value: LocalDateTime){
        time = value.toString()
    }
    var isModification by mutableStateOf<Boolean>(false)
        private set
    fun setIsModificationValue(s: Boolean){
        isModification = s
    }

    var attachmentUri by mutableStateOf<String?>(null)
        private set
    fun setAttachmentUriValue(uri: String?){
        attachmentUri = uri
    }

}
