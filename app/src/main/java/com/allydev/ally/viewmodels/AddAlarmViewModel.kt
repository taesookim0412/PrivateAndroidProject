package com.allydev.ally.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allydev.ally.AlarmActivity
import com.allydev.ally.objects.Days
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.services.TimeChangeReceiver
import com.allydev.ally.utils.CalendarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddAlarmViewModel(alarmRepository: AlarmRepository) : ViewModel() {
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
        var lastReqId: Int = 0

        arrList.forEach{
            var newDaysSet: MutableSet<Days.Day> = Days.createDaysSet(it)
            addAction(context, true, newDaysSet, it.hour!!, it.min!!, lastReqId)
            lastReqId += newDaysSet.size
        }

    }

    public fun recreateAlarm_AtThisTime(context:Context) = viewModelScope.launch(Dispatchers.IO){
        val instCalendar = Calendar.getInstance()
        val currHour = instCalendar.get(Calendar.HOUR_OF_DAY)
        val currMinute = instCalendar.get(Calendar.MINUTE)
        val currDayIdx = instCalendar.get(Calendar.DAY_OF_WEEK) - 1
        //Room query to obtain alarms scheduled at this hour and minute
        //What if there are duplicates? Ignore this problem, prevent duplicate alarms at creation instead
        val instAlarm:Alarm = alarmRepository.findByHourMinuteAndDay(currHour, currMinute, currDayIdx)
        //We have the target alarm now retrieve its request code and recreate its one alarm
        val newDaysSet: MutableSet<Days.Day> = Days.createDaysSet(instAlarm)
        val daysBoolArr: Array<Boolean> = Days.createDaysBoolArr(newDaysSet)
        var requestCodes: Array<Int> = createRequestCodes(instAlarm.requestId!!, daysBoolArr)
        //Cant do this, we want to recreate One alarm
        //TODO CREATE ONE ALARM
        //createOneAlarm(newDaysSet, context, requestCodes, baseCalendar)
        //Iterate through requestCodes, default is -1. If != -1, that idx - 1 = the day Idx.
        // We have the day idx we have to increment a new requestcodeid, scan through the days set,
        // the order stays the same so its index returns the request code.
        //However we need to create one alarm thus we must find the request code for that day if day.num == currdayidx
        val newBaseCalendar: Calendar = CalendarUtil.getBaseCalendar(currHour, currMinute)
        recreateOneAlarm(newDaysSet, context, requestCodes, newBaseCalendar, currDayIdx)


        //createAlarm(newDaysSet, context, requestCodes, baseCalendar)





    }


    public fun recreateOneAlarm(newDaysSet: MutableSet<Days.Day>? = daysSet.value, context: Context, requestCodeArr: Array<Int>, baseCalendar: Calendar, currDayIdx: Int) {
        val targetReqCode = requestCodeArr[currDayIdx]
        val targetDay:Days.Day = Days().calIntToDay(currDayIdx + 1)


            //retrieve the calendar object
            val newCalendar: Calendar = CalendarUtil.calDaysToAlarmMillis(baseCalendar, System.currentTimeMillis(), targetDay)
            val alarmIntent = Intent(context, AlarmActivity::class.java)
            val alarmPendingIntent: PendingIntent = PendingIntent.getActivity(
                context, targetReqCode, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newCalendar.timeInMillis,
                alarmPendingIntent
            )

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


    fun addAction(context: Context, isOnWake:Boolean, newDaysSet: MutableSet<Days.Day>? = daysSet.value, fHour: Int? = hour.value, fMinute: Int? = minute.value, lastReqId: Int = -1) {
        val daysSize = newDaysSet?.size
        Log.d("Days Size: ", newDaysSet?.size.toString())
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


        val baseCalendar: Calendar = CalendarUtil.getBaseCalendar(fHour, fMinute)


        //Set new calendar to new time
        //Create alarmmanager service per iteration
        //Also add to the array
        val daysBoolArr = Days.createDaysBoolArr(newDaysSet)
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
        else {
/*
                // We create alarm entities, then obtain the request code, then forloop the days!
            }
*/
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
    fun createAlarmEntities(hour:Int?, minute: Int?, boolArr: Array<Boolean>, context: Context, newDaysSet: MutableSet<Days.Day>?, baseCalendar:Calendar) = viewModelScope.launch(Dispatchers.IO){
        val currRequestId:Int = alarmRepository.createAlarm(hour, minute, boolArr)
        //Request codes are obtained here
        val requestIdCodes:Array<Int> = createRequestCodes(currRequestId, boolArr)

        createAlarm(newDaysSet, context, requestIdCodes, baseCalendar)

    }

    fun createRequestCodes(currRequestId: Int, boolArr: Array<Boolean>): Array<Int>{
        var requestCodeArr: Array<Int> = Array<Int>(7) { -1 }
        var ct = currRequestId;
        for ((idx, bool) in boolArr.withIndex()){
            if (bool == true){
                requestCodeArr[idx] = ct
                ct++
            }
        }
        return requestCodeArr
    }

    //This is called when setting the alarms

    public fun createAlarm(newDaysSet: MutableSet<Days.Day>? = daysSet.value, context: Context, requestCodeArr: Array<Int>, baseCalendar: Calendar) {
        for (day in newDaysSet.orEmpty()) {
            //retrieve the calendar object
            val newCalendar: Calendar = CalendarUtil.calDaysToAlarmMillis(baseCalendar, System.currentTimeMillis(), day)
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