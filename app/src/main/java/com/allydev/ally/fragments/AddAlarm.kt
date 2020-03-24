package com.allydev.ally.fragments


import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.allydev.ally.AlarmActivity
import com.allydev.ally.R
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.viewmodels.TimeViewModel
import com.allydev.ally.databinding.FragmentAddAlarmBinding
import com.allydev.ally.fragments.dialogfragments.SleepGenius
import com.allydev.ally.fragments.dialogfragments.SleepGeniusAbout
import com.allydev.ally.objects.Days
import com.allydev.ally.objects.TimeStringify
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.schemas.AlarmViewModel
import com.allydev.ally.utils.FragmentUtil
import kotlinx.android.synthetic.main.fragment_add_alarm.*
import kotlinx.android.synthetic.main.fragment_all_alarms.*
import java.util.*

class AddAlarm : Fragment() {
    private lateinit var alarmMgr: AlarmManager
    private lateinit var days: Days
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var timeViewModel: TimeViewModel
    private lateinit var addAlarmViewModel: AddAlarmViewModel
    private val version: Int = android.os.Build.VERSION.SDK_INT

    private var hour: Int = 0
    private var minute: Int = 0
    private var currentHour: Int = 0
    private var currentMinute: Int = 0
    private lateinit var daysSet: MutableSet<Days.Day>
    private var singleDayStr: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentAddAlarmBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_alarm, container, false)
        Log.d("Binding", "Binding")
        addAlarmViewModel = ViewModelProviders.of(activity!!).get(AddAlarmViewModel::class.java)
        alarmViewModel = ViewModelProviders.of(activity!!).get(AlarmViewModel::class.java)
        timeViewModel = ViewModelProviders.of(activity!!).get(TimeViewModel::class.java)

        binding.addAlarmViewModel = addAlarmViewModel
        binding.setLifecycleOwner(viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //necessary:
        days = addAlarmViewModel.days
        daysSet = addAlarmViewModel.daysSet.value!!


        hour = addAlarmViewModel.hour.value!!
        minute = addAlarmViewModel.minute.value!!
//        currentHour = addAlarmViewModel.currentHour.value!!
//        currentMinute = addAlarmViewModel.currentMinute.value!!

        setDaysText()
        setDay()


        addAlarmViewModel.apply {
//            hour.observe(viewLifecycleOwner, Observer { hour -> this@AddAlarm.hour = hour })
//            minute.observe(viewLifecycleOwner, Observer { minute -> this@AddAlarm.minute = minute })
//            currentHour.observe(viewLifecycleOwner, Observer { currentHour -> this@AddAlarm.currentHour = currentHour })
//            currentMinute.observe(
//                viewLifecycleOwner,
//                Observer { currentMinute -> this@AddAlarm.currentMinute = currentMinute })
            singleDayStr.observe(viewLifecycleOwner, Observer { singleDayStr ->
                this@AddAlarm.singleDayStr = singleDayStr
                setDay()
            })
//            daysSet.observe(viewLifecycleOwner, Observer { daysSet -> this@AddAlarm.daysSet = daysSet })
        }
        setPicker()

        //TODO - add date creator

//        dateTextVar.text = getResourcesStr(R.string.tomorrow)

        //********========================= Hooks=========================********//
        alarmTimePicker.setOnTimeChangedListener { alarmTimePicker, hour, minute ->
            onTimeChanged(alarmTimePicker, hour, minute)
        }


        mon.setOnClickListener { view -> onClickDay(mon, days.mon) }
        tue.setOnClickListener { view -> onClickDay(tue, days.tue) }
        wed.setOnClickListener { view -> onClickDay(wed, days.wed) }
        thurs.setOnClickListener { view -> onClickDay(thurs, days.thurs) }
        fri.setOnClickListener { view -> onClickDay(fri, days.fri) }
        sat.setOnClickListener { view -> onClickDay(sat, days.sat) }
        sun.setOnClickListener { view -> onClickDay(sun, days.sun) }
        add.setOnClickListener { view -> addAction(view) }
        bgGenius.setOnClickListener { view -> showGenius(view) }
        genius_about.setOnClickListener { view -> showAboutGenius(view) }
    }


    private fun showGenius(view: View?) {
        timeViewModel.hourStr.value = TimeStringify.getHour(hour)
        timeViewModel.minuteStr.value = TimeStringify.getMinute(minute)
        timeViewModel.ampmStr.value = TimeStringify.getAmpm(hour)

        val ft = fragmentManager!!.beginTransaction()

        val sleepGenius = SleepGenius.newInstance()
        sleepGenius.show(ft, "dialog")
    }

    private fun showAboutGenius(view: View?) {
        val ft = fragmentManager?.beginTransaction()
        val sleepGeniusAbout = SleepGeniusAbout.newInstance()
        sleepGeniusAbout.show(ft!!, "dialog")
    }


    // add alarm
    private fun addAction(view: View) {
        setTimeInViewModel()
        addAlarmViewModel.addAction(alarmViewModel, view.context, false)
        view.findNavController().navigate(R.id.allAlarms)
    }



    //************************************************** On time change, only change the day text **************************************************//

    private fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        addAlarmViewModel.getDayStrFromTxtValue(hourOfDay, minute)
        setDay()
    }

    private fun setDay() {
        if (singleDayStr == "Today") dateText.text = FragmentUtil.getResourceStr(R.string.today, resources)
        else if (singleDayStr == "Tomorrow") dateText.text = FragmentUtil.getResourceStr(R.string.tomorrow, resources)
    }

    //************************************************** End time change **************************************************//

    private fun setPicker() {
        if (version >= 23) {
            alarmTimePicker.hour = hour
            alarmTimePicker.minute = minute
        } else {
            alarmTimePicker.currentHour = hour
            alarmTimePicker.currentMinute = minute
        }
    }



    //TODO - Data binding. Not Worth it? ~30 extra lines, BaseObservable class, extra memory.
    //set em all
    private fun setDaysText(daysSet: MutableSet<Days.Day> = this.daysSet) {
        for (day in daysSet) {
            view?.findViewById<EditText>(day.id)!!.setTextColor(Color.BLUE)
        }
    }

    //TODO - Data binding. Not Worth it? ~30 extra lines, BaseObservable class, extra memory.
    //set one
    private fun onClickDay(day: EditText, vDay: Days.Day) {
        val result = addAlarmViewModel.evalDaySet(vDay)
        setDayText(result, day)
    }

    private fun setDayText(result: String, editText: EditText) {
        if (result == "Add") {
            editText.setTextColor(Color.BLUE)
        } else {
            editText.setTextColor(Color.BLACK)
        }
    }

    //************************************************** Manually set the view model's time. onPause here, too. **************************************************//

    private fun setTimeInViewModel() {
        if (version >= 23) {
            addAlarmViewModel.setTime(alarmTimePicker.hour, alarmTimePicker.minute)
        } else {
            addAlarmViewModel.setTime(alarmTimePicker.currentHour, alarmTimePicker.currentMinute)
        }
    }
    //set viewmodel time on pause
    override fun onPause() {
        super.onPause()
        setTimeInViewModel()
    }

}
