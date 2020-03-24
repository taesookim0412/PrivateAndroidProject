package com.allydev.ally.schemas

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: List<Alarm> = alarmDao.findAll()
    val allAlarmsSorted: LiveData<List<Alarm>> = alarmDao.findAllSorted()

    @WorkerThread
    suspend fun createAlarm(hour: Int?, minute: Int?, boolArr: Array<Boolean?>){
        /*if (allAlarms.size != 0) {

            //logic here for next lastDayElems
        }
        else {
            newId = 0
        }*/

        //Retrieve boolcount
        var boolCount:Int = 0
        for (elem in boolArr) {
            if (elem == true) boolCount++
        }
        var newRequestId = 0;
        Log.d("Size of all alarms: ", allAlarms.size.toString())
        if (allAlarms.size != 0) {
            //Retrieve requestcode
            val lastAlarm: Alarm = allAlarms[allAlarms.lastIndex]
            val lastDaysElems = lastAlarm.daysElems
            Log.d(lastAlarm.requestId.toString(), lastDaysElems.toString())
            newRequestId = lastAlarm.requestId!! + lastDaysElems!!
        }

        val newAlarm:Alarm = Alarm(hour, minute, boolArr, boolCount, newRequestId)

        alarmDao.createAlarm(newAlarm)
    }
}