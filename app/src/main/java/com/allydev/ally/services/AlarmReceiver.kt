package com.allydev.ally.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProviders
import com.allydev.ally.AlarmActivity
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.schemas.AlarmViewModel

class AlarmReceiver: BroadcastReceiver() {
    private lateinit var allAlarms: Array<Alarm>;
    override fun onReceive(context: Context?, intent: Intent?){

    var alarmMgr: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmActivity::class.java)
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(alarmIntent)

        //TODO: Move the activity function to this receiver (then there's no activity) and remove the additionally created alarms

    }
}