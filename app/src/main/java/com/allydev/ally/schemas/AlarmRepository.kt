package com.allydev.ally.schemas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.allydev.ally.AlarmActivity
import com.allydev.ally.utils.AddAlarmUtil
import com.allydev.ally.utils.CalendarUtil
import com.allydev.ally.utils.Days
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AlarmRepository() {
    public val addAlarmUtil:AddAlarmUtil = AddAlarmUtil(this)
    private val alarmDao: AlarmDao?
    val allAlarmsSorted: LiveData<List<Alarm>>?
    init{
        alarmDao = AlarmDatabase.alarmDao
        allAlarmsSorted = alarmDao?.findAllSorted()
    }
    @WorkerThread
    fun retrieveAlarmList(): List<Alarm>?{
        return alarmDao?.findAll()
    }

    @WorkerThread
    fun createAlarm(hour: Int?, minute: Int?, boolArr: Array<Boolean>): Int{
        //refresh allAlarms
        val allAlarms = alarmDao?.findAll()

        //Retrieve boolcount
        var boolCount:Int = 0
        for (elem in boolArr) {
            if (elem == true) boolCount++
        }
        var newRequestId = 0;
        if (allAlarms?.size != 0) {
            //Retrieve requestcode
            val lastAlarm: Alarm = allAlarms!![allAlarms.lastIndex]
            val lastDaysElems = lastAlarm.daysElems
            newRequestId = lastAlarm.requestId!! + lastDaysElems!!
        }

        val newAlarm:Alarm = Alarm(hour, minute, boolArr, boolCount, newRequestId)

        alarmDao?.createAlarm(newAlarm)
        return newRequestId
    }

    @WorkerThread
    fun findByHourMinuteAndDay(pHour: Int?, pMinute: Int?, dayIdx: Int?):Alarm{
        val alarmList: List<Alarm>? = alarmDao?.findByHourMinute(pHour, pMinute)

        for (alarm in alarmList.orEmpty()){
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

    /*@WorkerThread
    fun findAlarmByRequestCode(requestCode: Int){
        val alarmList:
    }*/
}
