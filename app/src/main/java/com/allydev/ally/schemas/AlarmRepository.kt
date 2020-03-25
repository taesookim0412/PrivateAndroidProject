package com.allydev.ally.schemas

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarmsSorted: LiveData<List<Alarm>> = alarmDao.findAllSorted()

    @WorkerThread
    fun retrieveAlarmList(): List<Alarm>{
        return alarmDao.findAll()
    }

    @WorkerThread
    fun createAlarm(hour: Int?, minute: Int?, boolArr: Array<Boolean?>): Int{
        //refresh allAlarms
        val allAlarms = alarmDao.findAll()

        //Retrieve boolcount
        var boolCount:Int = 0
        for (elem in boolArr) {
            if (elem == true) boolCount++
        }
        var newRequestId = 0;
        if (allAlarms.size != 0) {
            //Retrieve requestcode
            val lastAlarm: Alarm = allAlarms[allAlarms.lastIndex]
            val lastDaysElems = lastAlarm.daysElems
            newRequestId = lastAlarm.requestId!! + lastDaysElems!!
        }

        val newAlarm:Alarm = Alarm(hour, minute, boolArr, boolCount, newRequestId)

        alarmDao.createAlarm(newAlarm)
        return newRequestId
    }

    @WorkerThread
    fun findByHourMinuteAndDay(pHour: Int, pMinute: Int, dayIdx: Int):Alarm{
        val alarmList: List<Alarm> = alarmDao.findByHourMinute(pHour, pMinute)
        for (alarm in alarmList){
            if (dayIdx == 0) { if (alarm.sun == true) return alarm }
            else if (dayIdx == 1) { if (alarm.mon == true) return alarm }
            else if (dayIdx == 2) { if (alarm.tue == true) return alarm }
            else if (dayIdx == 3) { if (alarm.wed == true) return alarm }
            else if (dayIdx == 4) { if (alarm.thurs == true) return alarm }
            else if (dayIdx == 5) { if (alarm.fri == true) return alarm }
            else if (dayIdx == 6) { if (alarm.sat == true) return alarm }
        }
        return Alarm(null, null, emptyArray(), null, null)
    }
}