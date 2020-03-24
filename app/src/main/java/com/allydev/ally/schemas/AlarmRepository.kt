package com.allydev.ally.schemas

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: List<Alarm> = alarmDao.findAll()
    val allAlarmsSorted: LiveData<List<Alarm>> = alarmDao.findAllSorted()

    @WorkerThread
    suspend fun createAlarm(alarm: Alarm){
        alarmDao.createAlarm(alarm)
    }
}