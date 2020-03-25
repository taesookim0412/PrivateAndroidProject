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

        val addAlarmViewModel = ViewModelProviders.of(this).get(AddAlarmViewModel::class.java)
        //
        addAlarmViewModel.recreateAlarm_AtThisTime(view.context)


    }

    public fun showAnswers(view: View) {
        view.findViewById<Button>(R.id.showButton)!!.visibility = View.GONE
    }


}