package com.allydev.ally.schemas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application): AndroidViewModel(application) {
    private val repository:AlarmRepository
    val allAlarmsSorted: LiveData<List<Alarm>>?


    init{
        val alarmDao = AlarmDatabase.getAlarmDao(application)
        repository = AlarmRepository()
        allAlarmsSorted = repository.allAlarmsSorted
    }
}