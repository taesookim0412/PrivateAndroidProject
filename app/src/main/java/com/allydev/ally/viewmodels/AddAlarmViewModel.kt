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
import com.allydev.ally.utils.Day
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {
    // Objects to instantiate
    val alarmRepository = AlarmRepository()
    val addAlarmUtil = alarmRepository.addAlarmUtil
    val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val days:Days.Companion = Days
    val calendar = Calendar.getInstance()

    //Controller values
    val currentHour = MutableLiveData<Int>()
    val currentMinute = MutableLiveData<Int>()
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()
    val singleDayStr = MutableLiveData<String>()
    val daysSet = MutableLiveData<MutableSet<Day>>().apply {
            value = HashSet<Day>()
    }
    init{
        hardReset()
    }
/*
    fun recreateAlarm_AtThisTime() = viewModelScope.launch{
        alarmRepository.addAlarmUtil.recreateAlarm_AtThisTime(getApplication())
    }*/

    fun recreateOneAlarm(hour:Int?, minute:Int?, dayIdx: Int?){
        alarmRepository.addAlarmUtil.recreateOneAlarm_ExtraHourAndMinute(getApplication(), hour, minute, dayIdx?:0, viewModelScope, alarmManager)
    }


    fun addAction(isOnWake: Boolean){
        alarmRepository.addAlarmUtil.addAction(isOnWake, daysSet.value, hour.value, minute.value, -1, getApplication(), alarmManager, singleDayStr.value, viewModelScope)
    }









    //Activity Controllers
    fun setTime(hourOfDay: Int, minute: Int) {
        hour.value = hourOfDay
        this.minute.value = minute
        getDayStrFromTxtValue()
    }

    fun getDayStrFromTxtValue(hourOfDay: Int = hour.value?:6, minute: Int = this.minute.value?:0) {
        //if time is after now (by minute)
        val currentHour:Int = this.currentHour.value?:6
        val currentMinute:Int = this.currentMinute.value?:0
        val singleDayStr = this.singleDayStr.value!!
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

    fun evalDaySet(day: Day):String {
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
        daysSet.value = HashSet<Day>()
        singleDayStr.value = "Tomorrow"
    }


}