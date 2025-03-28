package it.polito.teamlounge.model

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class User() {

    constructor(name:String, nick: String, email: String, phone: String, loc: String, desc: String, role: String, t: Int) : this() {
        this.FullName = name
        this.Nickname = nick
        this.Role = role
        this.Email = email
        this.Phone = phone
        this.Location = loc
        this.Description = desc
        this.taskCompleted = t
        this.photoUri = "${email}/Profile.jpg"
        this.setMonogramValue(name)
    }

    var id by mutableStateOf(-1)
        private set

    var monogram by mutableStateOf("")
        private set
    fun setMonogramValue(name: String) {
        monogram = ""
        for(n in name.split(" ")){
            if (n.isNotEmpty())
                monogram += n[0]
        }
    }

    fun GetMonogramValue(): String {
        return monogram
    }

    var FullName by mutableStateOf("")
        private set
    var FullNameError by mutableStateOf("")
        private set
    fun setNameValue(name: String) {
        FullName = name
    }
    private fun CheckName() {
        if(FullName.isBlank()) {
            FullNameError = "Name cannot be blank"
        }
        else if(FullName.contains("[0-9]".toRegex())) {
            FullNameError = "Name contains digits"
        }
        else if(FullName.contains("  ")){
            FullNameError = "Name cannot contain multiple spaces"
        }
        else {
            FullName.trim()
            FullNameError = ""
        }
    }

    var Nickname by mutableStateOf("")
        private set
    var NicknameError by mutableStateOf("")
        private set
    fun setNickValue(name: String) {
        Nickname = name
    }

    private fun CheckNickname() {
        if(Nickname.isBlank()) {
            NicknameError = "Nickname cannot be blank"
        }
        else  NicknameError = ""
    }

    var Role by mutableStateOf("")
        private set
    var RoleError by mutableStateOf("")
        private set
    fun setRoleValue(role: String) {
        Role = role
    }

    private fun CheckRole() {
        if(Role.isBlank()) {
            RoleError = "Role cannot be blank"
        }
        else  RoleError = ""
    }

    var Email by mutableStateOf("")
        private set
    var EmailError by mutableStateOf("")
        private set
    fun setEmailValue(word: String) {
        Email = word
    }

    fun GetEmailValue(): String {
        return Email
    }

    private fun CheckEmail() {
        if(Email.isBlank()) {
            EmailError = "Nickname cannot be blank"
        }
        else if(!Email.contains("@") || Email.contains("[A-Z]".toRegex())){
            EmailError = "Invalid email"
        }
        else  EmailError = ""
    }

    var Phone by mutableStateOf("")
        private set
    var PhoneError by mutableStateOf("")
        private set
    fun setPhoneValue(number: String) {
        Phone = number
    }
    fun GetPhoneValue(): String {
        return Phone
    }

    private fun CheckPhone() {
        if(Phone.isBlank()) {
            PhoneError = "Phone Number cannot be blank"
        }
        else if(Phone.contains("[A-Z]".toRegex()) || Phone.contains("[a-z]".toRegex())){
            PhoneError = "Invalid phone number"
        }
        else if(Phone.length != 10){
            PhoneError = "phone number must be 10 digits long"
        }
        else  PhoneError = ""
    }



    var Location by mutableStateOf("")
        private set
    var LocationError by mutableStateOf("")
        private set
    fun setLocationValue(loc: String) {
        Location = loc
    }

    fun GetLocationValue(): String {
        return Location
    }

    private fun CheckLocation() {
        if(Location.isBlank()) {
            LocationError = "Location cannot be blank"
        }
        else  LocationError = ""
    }

    var Description by mutableStateOf("")
        private set
    var DescriptionError by mutableStateOf("")
        private set
    fun setDescriptionValue(desc: String) {
        Description = desc
    }

    fun GetDescriptionValue(): String {
        return Description
    }

    private fun CheckDescription() {
        if(Description.isBlank()) {
            DescriptionError = "Description cannot be blank"
        }
        else  DescriptionError = ""
    }

    var taskCompleted by mutableStateOf(0)


    var photoUri by mutableStateOf("")
        private set
    fun setPhotoUriValue(photo: String) {
        photoUri = photo
    }
    fun validate():Boolean {
        CheckName()
        CheckNickname()
        CheckEmail()
        CheckPhone()
        CheckLocation()
        CheckDescription()
        CheckRole()
        if(FullNameError.isBlank() && NicknameError.isBlank() && EmailError.isBlank()
            && LocationError.isBlank() && DescriptionError.isBlank() && PhoneError.isBlank()
            && RoleError.isBlank()) {
            setMonogramValue(FullName)
            return true
        }
        return false
    }
}