package com.allydev.ally.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allydev.ally.AlarmActivity
import com.allydev.ally.utils.Days
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.services.TimeChangeReceiver
import com.allydev.ally.utils.AddAlarmUtil
import com.allydev.ally.utils.CalendarUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {
    var currentHour: MutableLiveData<Int>
    var currentMinute: MutableLiveData<Int>
    var hour: MutableLiveData<Int>
    var minute: MutableLiveData<Int>

    var singleDayStr: MutableLiveData<String>
    var intentHooked: MutableLiveData<Boolean>
    var calendar: Calendar
    var timeChangeReceiver:TimeChangeReceiver
    var days: Days
    var daysSet: MutableLiveData<MutableSet<Days.Day>>
    var alarmRepository: AlarmRepository
    var addAlarmUtil:AddAlarmUtil

    init{
        currentHour = MutableLiveData<Int>()
        currentMinute = MutableLiveData<Int>()
        hour = MutableLiveData<Int>()
        minute = MutableLiveData<Int>()
        alarmRepository = AlarmRepository()
        addAlarmUtil = alarmRepository.addAlarmUtil
         singleDayStr = MutableLiveData<String>()
         intentHooked = MutableLiveData<Boolean>()
         calendar = Calendar.getInstance()
         timeChangeReceiver = TimeChangeReceiver()
         days = Days()
         daysSet = MutableLiveData<MutableSet<Days.Day>>().apply {
            value = HashSet<Days.Day>()
        }
    }
/*
    fun recreateAlarm_AtThisTime() = viewModelScope.launch{
        alarmRepository.addAlarmUtil.recreateAlarm_AtThisTime(getApplication())
    }*/

    fun recreateOneAlarm(hour:Int?, minute:Int?, dayIdx: Int?){
        alarmRepository.addAlarmUtil.recreateOneAlarm_ExtraHourAndMinute(getApplication(), hour, minute, dayIdx?:0, viewModelScope)
    }


    fun addAction(isOnWake: Boolean){
        alarmRepository.addAlarmUtil.addAction(isOnWake, daysSet.value, hour.value, minute.value, -1, getApplication(), singleDayStr.value, viewModelScope)
    }




    fun isTimeHooked(): Boolean {
        if (intentHooked.value != true) {
            return false
        }
        return true
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        hour.value = hourOfDay
        this.minute.value = minute
        getDayStrFromTxtValue()
    }








    //Activity Controllers
    fun getDayStrFromTxtValue(hourOfDay: Int = hour.value!!, minute: Int = this.minute.value!!) {
        //if time is after now (by minute)
        var currentHour = this.currentHour.value!!
        var currentMinute = this.currentMinute.value!!
        var singleDayStr = this.singleDayStr.value!!
        if (minute > currentMinute && hourOfDay >= currentHour) {
            //set to today
            if (singleDayStr != "Today") {
                this.singleDayStr.value = "Today"
            }
        }
        //else if going by hour (time is still after)
        else if (hourOfDay > currentHour) {
            //set to today
            if (singleDayStr != "Today") {
                this.singleDayStr.value = "Today"
            }
        }
        // anything else
        else {
            //set to tomorrow
            if (singleDayStr != "Tomorrow") {
                this.singleDayStr.value = "Tomorrow"
            }
        }
    }

    fun evalDaySet(day: Days.Day):String {
        val daysSet = daysSet.value
        if (!daysSet!!.contains(day)) {
            daysSet.add(day)
            return "Add"
        } else {
            daysSet.remove(day)
            return "Remove"
        }
    }


    fun hardReset() {
        calendar.timeInMillis = System.currentTimeMillis()
        val tHour = calendar.get(Calendar.HOUR_OF_DAY)
        val tMinute = calendar.get(Calendar.MINUTE)
        currentHour.value = tHour
        currentMinute.value = tMinute
        hour.value = 6
        minute.value = 0
        daysSet.value = HashSet<Days.Day>()
        singleDayStr.value = "Tomorrow"
    }


}