package com.allydev.ally.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allydev.ally.objects.TimeStringify
import com.allydev.ally.schemas.Alarm

class AlarmHolderViewModel(alarm: Alarm): ViewModel(){
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


}