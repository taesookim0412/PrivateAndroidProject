package com.allydev.ally.fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.allydev.ally.R
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.viewmodels.TimeViewModel
import com.allydev.ally.databinding.FragmentAddAlarmBinding
import com.allydev.ally.fragments.dialogfragments.SleepGenius
import com.allydev.ally.fragments.dialogfragments.SleepGeniusAbout
import com.allydev.ally.utils.Days
import com.allydev.ally.utils.TimeStringify
import com.allydev.ally.utils.FragmentUtil
import kotlinx.android.synthetic.main.fragment_add_alarm.*

class AddAlarm : Fragment() {
    private lateinit var days: Days
    private val timeViewModel: TimeViewModel by activityViewModels<TimeViewModel>()
    private val addAlarmViewModel: AddAlarmViewModel by activityViewModels<AddAlarmViewModel>()
    private val version: Int = android.os.Build.VERSION.SDK_INT

    private var hour: Int = 0
    private var minute: Int = 0
    private var currentHour: Int = 0
    private var currentMinute: Int = 0
    private lateinit var daysSet: MutableSet<Day>
    private var singleDayStr: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentAddAlarmBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_alarm, container, false)
        Log.d("Binding", "Binding")

        binding.addAlarmViewModel = addAlarmViewModel
        binding.setLifecycleOwner(viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //necessary:
        days = addAlarmViewModel.days
        daysSet = addAlarmViewModel.daysSet.value?:HashSet<Days.Day>()
        hour = addAlarmViewModel.hour.value?:6
        minute = addAlarmViewModel.minute.value?:0
        currentHour = addAlarmViewModel.currentHour.value?:6
        currentMinute = addAlarmViewModel.currentMinute.value?:0

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


//        dateTextVar.text = getResourcesStr(R.string.tomorrow)

        //********========================= Hooks=========================********//
        alarmTimePicker.setOnTimeChangedListener { _, hour, minute ->
            onTimeChanged(hour, minute)
        }


        mon.setOnClickListener { _ -> onClickDay(mon, days.mon) }
        tue.setOnClickListener { _ -> onClickDay(tue, days.tue) }
        wed.setOnClickListener { _ -> onClickDay(wed, days.wed) }
        thurs.setOnClickListener { _ -> onClickDay(thurs, days.thurs) }
        fri.setOnClickListener { _ -> onClickDay(fri, days.fri) }
        sat.setOnClickListener { _ -> onClickDay(sat, days.sat) }
        sun.setOnClickListener { _ -> onClickDay(sun, days.sun) }
        add.setOnClickListener { _ -> addAction(view) }
        bgGenius.setOnClickListener { _ -> showGenius() }
        genius_about.setOnClickListener { _ -> showAboutGenius() }
    }


    private fun showGenius() {
        timeViewModel.hourStr.value = TimeStringify.getHour(hour)
        timeViewModel.minuteStr.value = TimeStringify.getMinute(minute)
        timeViewModel.ampmStr.value = TimeStringify.getAmpm(hour)

        val ft = childFragmentManager.beginTransaction()

        val sleepGenius = SleepGenius.newInstance()
        sleepGenius.show(ft, "dialog")
    }

    private fun showAboutGenius() {
        val ft = childFragmentManager.beginTransaction()
        val sleepGeniusAbout = SleepGeniusAbout.newInstance()
        sleepGeniusAbout.show(ft, "dialog")
    }


    // add alarm
    private fun addAction(view: View) {
        setTimeInViewModel()
        addAlarmViewModel.addAction(false)
        view.findNavController().navigate(R.id.allAlarms)
    }



    //************************************************** On time change, only change the day text **************************************************//

    private fun onTimeChanged(hourOfDay: Int, minute: Int) {
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
        }
        else {
            alarmTimePicker.currentHour = hour
            alarmTimePicker.currentMinute = minute

        }
    }



    //set em all
    private fun setDaysText(daysSet: MutableSet<Days.Day> = this.daysSet) {
        for (day in daysSet) {
            view?.findViewById<EditText>(day.id)!!.setTextColor(Color.BLUE)
        }
    }

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
