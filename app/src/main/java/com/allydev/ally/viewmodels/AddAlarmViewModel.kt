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
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.allydev.ally.AlarmActivity
import com.allydev.ally.R
import com.allydev.ally.objects.Days
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.schemas.AlarmViewModel
import com.allydev.ally.services.TimeChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    var testText: MutableLiveData<Boolean>
    private var alarmRepository: AlarmRepository

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
        alarmRepository = AlarmRepository(AlarmDatabase.getAlarmDao(application))
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


    fun reCreateAlarms(context: Context) = viewModelScope.launch(
        Dispatchers.IO){
        var arrList:List<Alarm> = alarmRepository.retrieveAlarmList()
        Log.d(arrList.size.toString(), "arrlistsize")
        var newDaysSet: MutableSet<Days.Day> = HashSet<Days.Day>()
        var lastReqId: Int = 0

        arrList.forEach{
            if (it.sun == true) newDaysSet.add(Days().sun)
            if (it.mon == true) newDaysSet.add(Days().mon)
            if (it.tue == true) newDaysSet.add(Days().tue)
            if (it.wed == true) newDaysSet.add(Days().wed)
            if (it.thurs == true) newDaysSet.add(Days().thurs)
            if (it.fri == true) newDaysSet.add(Days().fri)
            if (it.sat == true) newDaysSet.add(Days().sat)
            addAction(context, true, newDaysSet, it.hour!!, it.min!!, lastReqId)
            lastReqId += newDaysSet.size
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


    fun addAction(context: Context, isOnWake:Boolean, newDaysSet: MutableSet<Days.Day> = daysSet.value!!, fHour: Int = hour.value!!, fMinute: Int = minute.value!!, lastReqId: Int = -1) {
        val daysSize = newDaysSet.size
        Log.d("Days Size: ", newDaysSet.size.toString())
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
            newDaysSet.add(days.calIntToDay(calDate))
            //add nextday alarm default
        }
        //create instance of NOW, THEN make it the time that was set. (Same calendar block)


        val baseCalendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, fHour)
            set(Calendar.MINUTE, fMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        //Set new calendar to new time
        //Create alarmmanager service per iteration
        //Also add to the array
        val daysBoolArr = Array<Boolean?>(7) { false }

        for (day: Days.Day in newDaysSet) {
            daysBoolArr[day.num - 1] = true
        }
        if (isOnWake == false) {
            createAlarmEntities(
                hour.value,
                minute.value,
                daysBoolArr,
                context.applicationContext,
                newDaysSet,
                baseCalendar
            )
        }
/*
            // Add to schema.
            if (isOnWake == false) {
                //This will be a coroutine therefore we cant call here therefore pass the necessary params for createAlarm(1,2 )
                //Certainly pass [day.num - 1]
                /// WOOPS this creates ONE alarm.. This is for each day!
                // We have to refactor our code to create alarm entities, then obtain the request code, then forloop the days!
                createAlarmEntities(hour.value, minute.value, daysBoolArr, newCalendar.timeInMillis, context.applicationContext, day.num - 1)
            }
*/
        else {
            // First we have to obtain the request codes
            //One way we can do this is to get every alarm,
            // Create alarms for each alarm ent (WE ARE ALREADY HERE) but keep track of the previous req code
            // alarm 0: req code 0, we have a days array so create a requestcodeArr,
            // !!keep track of the requestcodeid, next iteration add on top of that

            var requestCodes: Array<Int> = createRequestCodes(lastReqId, daysBoolArr)
            createAlarm(newDaysSet, context, requestCodes, baseCalendar)
        }
    }




    // Only used when initially creating the alarm
    fun createAlarmEntities(hour:Int?, minute: Int?, boolArr: Array<Boolean?>, context: Context, newDaysSet: MutableSet<Days.Day>, baseCalendar:Calendar) = viewModelScope.launch(Dispatchers.IO){
        val lastRequestId:Int = alarmRepository.createAlarm(hour, minute, boolArr)
        //Request codes are obtained here
        val requestIdCodes:Array<Int> = createRequestCodes(lastRequestId, boolArr)

        createAlarm(newDaysSet, context, requestIdCodes, baseCalendar)

    }

    fun createRequestCodes(lastRequestId: Int, boolArr: Array<Boolean?>): Array<Int>{
        var requestCodeArr: Array<Int> = Array<Int>(7) { -1 }
        var ct = lastRequestId;
        for ((idx, bool) in boolArr.withIndex()){
            if (bool == true){
                requestCodeArr[idx] = ct
                ct++
            }
        }
        return requestCodeArr
    }

    //This is called when setting the alarms

    public fun createAlarm(newDaysSet: MutableSet<Days.Day> = daysSet.value!!, context: Context, requestCodeArr: Array<Int>, baseCalendar: Calendar) {
        for (day in newDaysSet) {
            //retrieve the calendar object
            val newCalendar: Calendar = calDaysToMillis(baseCalendar, System.currentTimeMillis(), day)
            val alarmIntent = Intent(context, AlarmActivity::class.java)
            val requestCode: Int = requestCodeArr[day.num - 1]
            val alarmPendingIntent: PendingIntent = PendingIntent.getActivity(
                context, requestCode, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newCalendar.timeInMillis,
                alarmPendingIntent
            )
        }
    }

    private fun calDaysToMillis(
        baseCalendar: Calendar,
        dTime: Long,
        day: Days.Day
    ): Calendar {
        var newCalendar1: Calendar
        newCalendar1 = baseCalendar.clone() as Calendar
        //                Log.d("should be old calendar", newCalendar.time.toString())
        newCalendar1.set(Calendar.DAY_OF_WEEK, day.num)
        //Calendar day is less.. Therefore add a week.
        if (newCalendar1.timeInMillis <= dTime) {
            newCalendar1.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return newCalendar1
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