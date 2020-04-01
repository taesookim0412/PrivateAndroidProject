package com.allydev.ally.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import com.allydev.ally.AlarmActivity
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.schemas.AlarmViewModel
import com.allydev.ally.utils.Days
import com.allydev.ally.viewmodels.AddAlarmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver: BroadcastReceiver() {
    private val serviceScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var alarmRepository:AlarmRepository

    override fun onReceive(context: Context, intent: Intent?){
        AlarmDatabase.getInstance(context)
        AlarmDatabase.getAlarmDao(context)
        alarmRepository = AlarmRepository()
        alarmRepository.addAlarmUtil.reCreateAllAlarms(context, serviceScope)

    }


}