package com.allydev.ally.schemas

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application): AndroidViewModel(application) {
    private val repository:AlarmRepository
    val allAlarmsSorted: LiveData<List<Alarm>>?


    init{
        repository = AlarmRepository()
        repository.addAlarmUtil.reCreateAllAlarms(application, viewModelScope)
        Log.d("Alarmviewmodel", "initted")
        allAlarmsSorted = repository.allAlarmsSorted
    }
}