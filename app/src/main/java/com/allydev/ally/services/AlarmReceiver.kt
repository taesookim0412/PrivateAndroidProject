package com.allydev.ally.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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