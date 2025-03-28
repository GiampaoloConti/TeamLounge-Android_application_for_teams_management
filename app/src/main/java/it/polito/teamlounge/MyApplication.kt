package it.polito.teamlounge

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import it.polito.teamlounge.model.MyModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MyApplication: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO + Job())
    lateinit var model : MyModel

    fun getApplicationScope(): CoroutineScope = applicationScope

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        model = MyModel(this, getApplicationScope())
    }

    // Only for initial debug purposes
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeModel() {
        model.addUser("Carlo Marco", "Ciccio", "ciccio@gmail.com", "0123456789", "Taranto", "Ciao", "Chef",2)
        model.addUser(
            "Giorgia Corona",
            "Giorgina",
            "giorgina@gmail.com",
            "0432156789",
            "Torino",
            "Hola",
            "Waiter",
            3
        )
        model.addUser("Matteo Rossi", "Matt", "matt@gmail.com", "01234569870", "Milano", "Sleepy", "Gardener",1)
        model.addTeam(
            "Team 1",
            "Cutting",
            "Kitchen",
            mutableListOf("Giorgia Corona", "Matteo Rossi"),
            "23/05/2023"
        )
        model.addTeam(
            "Team 2",
            "Washing",
            "Laundry",
            mutableListOf("Giorgia Corona", "Matteo Rossi", "Carlo Marco"),
            "25/05/2023"
        )
        model.addTeam(
            "Team 3",
            "Clean the pool",
            "Garden",
            mutableListOf("Giorgia Corona", "Matteo Rossi", "Carlo Marco"),
            "28/05/2023"
        )


        model.addTask("Team 1", "Clean the kitchen", "Clean the pots", "04/01/2024", "Urgent", "Kitchen", "Giorgia Corona, Matteo Rossi, Carlo Marco", "Never");
        model.addTask("Team 1", "Clean the bathroom","Clean the wc","03/01/2022", "Urgent", "Cleaning", "Giorgia Corona", "Daily");
        model.addTask("Team 1", "Cut vegetables","Cut potatoes and carrots","10/12/2056", "Urgent", "kitchen", "Giorgia Corona, Matteo Rossi", "Every Year");


    }
}