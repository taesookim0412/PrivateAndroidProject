package com.allydev.ally

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allydev.ally.adapters.AlarmAdapter
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmDao
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.services.AlarmReceiver
import com.allydev.ally.viewmodels.AddAlarmViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_all_alarms.*
import java.util.*

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
/*
        unregisterReceiver(addAlarmViewModel.timeChangeReceiver)
        addAlarmViewModel.intentHooked.value = false*/
    }

    private val minuteTickIntent = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }
    private lateinit var addAlarmViewModel: AddAlarmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        AlarmDatabase.getInstance(applicationContext)
        AlarmDatabase.getAlarmDao(applicationContext)
        Log.d("Created" , "databases")
        val navController = findNavController(R.id.mainFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
