package com.allydev.ally.viewmodels

import androidx.lifecycle.*

class TimeViewModel: ViewModel() {
    val hourStr = MutableLiveData<String>().apply{
        value = ""
    }
    val minuteStr = MutableLiveData<String>().apply{
        value = ""
    }
    val ampmStr = MutableLiveData<String>().apply{
        value = "AM"
    }
}