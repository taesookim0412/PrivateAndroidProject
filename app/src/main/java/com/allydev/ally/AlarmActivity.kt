package com.allydev.ally

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.allydev.ally.databinding.ActivityAlarmBinding
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.viewmodels.AddAlarmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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

        AlarmDatabase.getInstance(applicationContext)
        AlarmDatabase.getAlarmDao(applicationContext)

        val addAlarmViewModel = ViewModelProviders.of(this).get(AddAlarmViewModel::class.java)
        val hour:Int? = intent.extras!!.get("hour") as Int
        val minute:Int? = intent.extras!!.get("minute") as Int
        val day:Int? = intent.extras!!.get("day") as Int
        Log.d("hour:", hour.toString())
        Log.d("minute:", minute.toString())
        Log.d("day:", day.toString())


        addAlarmViewModel.recreateOneAlarm(hour, minute, day?:0)


    }

    public fun showAnswers(view: View) {
        view.findViewById<Button>(R.id.showButton)!!.visibility = View.GONE
    }


}