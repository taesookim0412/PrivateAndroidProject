package com.allydev.ally.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.databinding.BaseObservable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.allydev.ally.AlarmActivity
import com.allydev.ally.R
import com.allydev.ally.objects.Days
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmViewModel
import com.allydev.ally.services.TimeChangeReceiver
import java.util.*

class AddAlarmViewModel() : ViewModel() {
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
    var testText: MutableLiveData<Boolean>

    init{
        currentHour = MutableLiveData<Int>()
        currentMinute = MutableLiveData<Int>()
        hour = MutableLiveData<Int>()
        minute = MutableLiveData<Int>()

         singleDayStr = MutableLiveData<String>()
         intentHooked = MutableLiveData<Boolean>()
         calendar = Calendar.getInstance()
         timeChangeReceiver = TimeChangeReceiver()
         days = Days()
         daysSet = MutableLiveData<MutableSet<Days.Day>>().apply {
            value = HashSet<Days.Day>()
        }
         testText = MutableLiveData<Boolean>().apply{
            value = false
        }
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


    fun reCreateAlarms(alarmViewModel: AlarmViewModel, context: Context){
        var arrList:List<Alarm> = alarmViewModel.allAlarms
        Log.d(arrList.size.toString(), "arrlistsize")
        arrList.forEach{
            hour.value = it.hour
            minute.value = it.min
            if (it.sun == true) daysSet.value?.add(Days().sun)
            if (it.mon == true) daysSet.value?.add(Days().mon)
            if (it.tue == true) daysSet.value?.add(Days().tue)
            if (it.wed == true) daysSet.value?.add(Days().wed)
            if (it.thurs == true) daysSet.value?.add(Days().thurs)
            if (it.fri == true) daysSet.value?.add(Days().fri)
            if (it.sat == true) daysSet.value?.add(Days().sat)
            addAction(alarmViewModel, context, true)

        }

    }




    //
    /*  !* means these have to be altered for use in the service
    * Add !*
    * Check for Tomorrow/today !*
    * Create Calendar instance
    * Create calendar clone shadow
    * read days set values and create each alarm !* Need to set the time for each alarm
    * add to database !*
    * register alarms
    *
    *
    * */



    fun addAction(alarmViewModel:AlarmViewModel, context: Context, isOnWake:Boolean) {
        val daysSize = daysSet.value?.size
        //Add just one day
        if (daysSize == 0 && isOnWake == false) {
            //get now. Calendar is required here for the following function: get(Calendar.DAY_OF_WEEK)
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
            }
            //if tomorrow, add one day
            if (singleDayStr.value === "Tomorrow") {
                calendar.add(Calendar.DAY_OF_WEEK, 1)
            }
            //add the day to the set.
            val calDate: Int = calendar.get(Calendar.DAY_OF_WEEK)
            daysSet.value?.add(days.calIntToDay(calDate))
            //add nextday alarm default
        }
        //create instance of NOW, THEN make it the time that was set. (Same calendar block)

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour.value!!)
                set(Calendar.MINUTE, minute.value!!)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
        }


        val dTime = System.currentTimeMillis()
        var newCalendar: Calendar


        //Set new calendar to new time
        //Create alarmmanager service per iteration
        //Also add to the array
        val daysBoolArr = Array<Boolean?>(7) { false }

        for (day: Days.Day in daysSet.value!!) {
            daysBoolArr[day.num - 1] = true
            newCalendar = calendar.clone() as Calendar
//                Log.d("should be old calendar", newCalendar.time.toString())
            newCalendar.set(Calendar.DAY_OF_WEEK, day.num)
            //Calendar day is less.. Therefore add a week.
            if (newCalendar.timeInMillis <= dTime) {
                newCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            createAlarm(newCalendar.timeInMillis, context.applicationContext)
            }

        // Add to schema.
        if (isOnWake == false) {
        alarmViewModel.createAlarm(hour.value, minute.value, daysBoolArr)
        }
    }

    public fun createAlarm(calendarMillis: Long, context: Context) {
        val alarmIntent = Intent(context, AlarmActivity::class.java)
        val alarmPendingIntent: PendingIntent = PendingIntent.getActivity(
            //TODO: CHANGE REQUEST CODE FROM RANDOM
            context, (0..10).random(), alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarMillis, alarmPendingIntent)
    }

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
        testText.value=true
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