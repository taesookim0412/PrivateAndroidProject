package com.allydev.ally

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import com.allydev.ally.databinding.ActivityAlarmBinding
import com.allydev.ally.objects.Days
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.AlarmRepository
import com.allydev.ally.schemas.AlarmViewModel
import com.allydev.ally.viewmodels.AddAlarmViewModel
import kotlinx.android.synthetic.main.content_alarm.*
import java.util.*

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var txtQuestion = "q"
    private var txtCategory = "c"
    private var ansChoice1 = "a"
    private var ansChoice2 = "a"
    private var ansChoice3 = "a"
    private var ansChoice4 = "2"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        binding.layout.questionCategory.text = txtCategory
        binding.layout.questionTxt.text = txtQuestion
        binding.layout.ansChoice1.text = ansChoice1
        binding.layout.ansChoice2.text = ansChoice2
        binding.layout.ansChoice3.text = ansChoice3
        binding.layout.ansChoice4.text = ansChoice4


        val view = binding.root
        setContentView(view)



        //recreateAlarm(view)
        val alarmProvider = ViewModelProviders.of(this).get(AlarmViewModel::class.java)
        val addAlarmViewModel = ViewModelProviders.of(this).get(AddAlarmViewModel::class.java)
        addAlarmViewModel.reCreateAlarms(alarmProvider, view.context)


    }

    public fun showAnswers(view: View){
        view.findViewById<Button>(R.id.showButton)!!.visibility = View.GONE

    }

    public fun recreateAlarm(view: View){
        //get closest alarm in the viewmodel
        //if the time is the closest alarm with a threshhold of one minute then dont reschedule it
        //otherwise recreate the same alarm with second 0 in 7 days
//Actually all alarms get deleted per reboot
        // Thus you have to recreate every alarm
        // then in the alarm trigger just create a method for determining the Single use of the alarm vs Weekly use.

        //Recreate each alarm
        // base: alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, alarmPendingIntent)
        val AlarmProvider = ViewModelProviders.of(this).get(AlarmViewModel::class.java)
        var arrList: List<Alarm> = emptyList()



        //TODO! ReCreate Each Alarm.
        val calendarIns: Calendar = Calendar.getInstance().apply{
            timeInMillis = System.currentTimeMillis()
        }

        var times = 0
        arrList = AlarmProvider.allAlarms
            //This creates an infinite loop because you're updating arrlist after each alarm.
            val size = arrList.size
            view.findViewById<EditText>(R.id.testText).setText(size.toString())
            arrList.forEach {
                val daysSet: MutableSet<Days.Day> = HashSet<Days.Day>()
                if (it.sun == true) daysSet.add(Days().sun)
                if (it.mon == true) daysSet.add(Days().mon)
                if (it.tue == true) daysSet.add(Days().tue)
                if (it.wed == true) daysSet.add(Days().wed)
                if (it.thurs == true) daysSet.add(Days().thurs)
                if (it.fri == true) daysSet.add(Days().fri)
                if (it.sat == true) daysSet.add(Days().sat)
                //for each day now add each alarm
                addAction(AlarmProvider, daysSet, true, it.min, it.hour, view.context)
                times++
                Log.d("Ran", times++.toString())
            }
    }


    fun addAction(alarmViewModel:AlarmViewModel, daysSet:MutableSet<Days.Day>, isBootActivity:Boolean, isBootMinute:Int?, isBootHour:Int?, bootContext: Context) {
        val daysSize = daysSet.size/*
        //Add just one day
        if (daysSize == 0 && isBootActivity == false) {
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
        }*/
        //create instance of NOW, THEN make it the time that was set. (Same calendar block)

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, isBootHour!!)
            set(Calendar.MINUTE, isBootMinute!!)
            set(Calendar.SECOND, 0)
        }


        val dTime = System.currentTimeMillis()
        var newCalendar: Calendar


        //Set new calendar to new time
        //Create alarmmanager service per iteration
        //Also add to the array
        val daysBoolArr = Array<Boolean>(7) { false }

        for (day: Days.Day in daysSet) {
            daysBoolArr[day.num - 1] = true
            newCalendar = calendar.clone() as Calendar
//                Log.d("should be old calendar", newCalendar.time.toString())
            newCalendar.set(Calendar.DAY_OF_WEEK, day.num)
            //Calendar day is less.. Therefore add a week.
            if (newCalendar.timeInMillis <= dTime) {
                newCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            //Create the alarm in the alarm manager
            if (isBootActivity == true){
                createAlarm(newCalendar.timeInMillis, bootContext)

            }
        }

        // Add to schema.
        /*val newAlarm = Alarm(
            isBootHour,
            isBootMinute,
            daysBoolArr
        )
        alarmViewModel.createAlarm(newAlarm)*/
    }

    public fun createAlarm(calendarMillis: Long, context: Context) {
        val alarmIntent = Intent(context, AlarmActivity::class.java)
        val alarmPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 1, alarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendarMillis, alarmPendingIntent)
    }
    }