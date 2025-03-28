package it.polito.teamlounge.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class Modification(){
    var user by mutableStateOf<String>("")
        private set
    fun setUserValue(s: String){
        user = s;
    }
    var time by mutableStateOf<String>(LocalDateTime.now().toString())
        private set
    fun setTimeValue(s: LocalDateTime){
        time = s.toString();
    }
    var type by mutableStateOf<String>("")
        private set
    fun setTypeValue(s: String){
        type = s;
    }
    var old by mutableStateOf<String>("")
        private set
    fun setOldValue(s: String){
        old = s;
    }
    var new by mutableStateOf<String>("")
        private set
    fun setNewValue(s: String){
        new = s;
    }
}