package com.allydev.ally.schemas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(): ViewModel() {
    private val repository:AlarmRepository
    val allAlarmsSorted: LiveData<List<Alarm>>?


    init{
        repository = AlarmRepository()
        allAlarmsSorted = repository.allAlarmsSorted
    }
}