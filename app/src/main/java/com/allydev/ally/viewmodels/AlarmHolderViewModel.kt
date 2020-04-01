package com.allydev.ally.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allydev.ally.utils.TimeStringify
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmHolderViewModel(alarm: Alarm, val context: Context): ViewModel(){
    private val alarm = checkNotNull(alarm)

    val hourStr = MutableLiveData<String>().apply{
        value = TimeStringify.getHour(alarm.hour!!)
    }
    val minuteStr = MutableLiveData<String>().apply{
        value = TimeStringify.getMinute(alarm.min!!)
    }
    val amPm = MutableLiveData<String>().apply{
        value = TimeStringify.getAmpm(alarm.hour!!)
    }
    val daysArr = MutableLiveData<Array<Boolean?>>().apply{
        value = arrayOf(alarm.sun, alarm.mon, alarm.tue, alarm.wed, alarm.thurs, alarm.fri, alarm.sat)
    }

    fun deleteAlarm() = viewModelScope.launch(Dispatchers.IO){
        val alarmRepository = AlarmRepository()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmRepository.deleteAlarmEntityAndCancelAlarms(alarm, context, alarmManager)
    }




}