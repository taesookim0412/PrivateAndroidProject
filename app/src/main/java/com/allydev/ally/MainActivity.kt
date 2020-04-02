package com.allydev.ally

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.api.TriviaViewModel
import com.allydev.ally.schemas.trivia.TriviaDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()

        /*if (!addAlarmViewModel.isTimeHooked()) {

            registerReceiver(addAlarmViewModel.timeChangeReceiver, minuteTickIntent)
            addAlarmViewModel.intentHooked.value = true
        }*/
    }

    override fun onPause() {
        super.onPause()
    }

    private val minuteTickIntent = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        AlarmDatabase.getAlarmDao(applicationContext)
        val navController = findNavController(R.id.mainFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)



        TriviaDatabase.getTriviaDao(application)
        val triviaViewModel = ViewModelProvider(this).get(TriviaViewModel::class.java)
        /*triviaViewModel.repository.api.switchToken_NewBuild("d5f9566082f19b4f8cb7ade00ba07212000e1c09bec57f870b0f1eef7d49ab4e")*/


        triviaViewModel.getCategories.observe(this, Observer{

        })

        triviaViewModel.putCustom50Questions("", "", "multiple")
//        triviaViewModel.allTrivia?.observe(this, Observer{
//            Log.d("it size: ", it.size.toString())
//        })

        /*triviaViewModel.test()*/


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings_difficulty -> true
            R.id.action_settings_category -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClickSettings(item: MenuItem) {

    }
}
