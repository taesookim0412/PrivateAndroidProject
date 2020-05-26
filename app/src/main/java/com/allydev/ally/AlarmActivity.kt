package com.allydev.ally

import android.animation.ObjectAnimator
import android.graphics.Path
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.allydev.ally.databinding.AlarmActivityBinding
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.viewmodels.AlarmTriviaViewModel
import com.allydev.ally.viewmodels.AlarmViewModel
import kotlinx.android.synthetic.main.alarm_activity.*

class AlarmActivity : AppCompatActivity() {
    private val alarmTriviaViewModel: AlarmTriviaViewModel by viewModels()
    private val addAlarmViewModel:AddAlarmViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        AlarmDatabase.getInstance(applicationContext)
        AlarmDatabase.getAlarmDao(applicationContext)
        super.onCreate(savedInstanceState)

        val binding: AlarmActivityBinding = DataBindingUtil.setContentView(this, R.layout.alarm_activity)
        binding.viewModel = alarmViewModel
        binding.setLifecycleOwner{lifecycle}
        binding.ansChoice1.setOnClickListener{ view -> onClickAnswer(view)}
        binding.ansChoice2.setOnClickListener{ view -> onClickAnswer(view)}
        binding.ansChoice3.setOnClickListener{ view -> onClickAnswer(view)}
        binding.ansChoice4.setOnClickListener{ view -> onClickAnswer(view)}
        binding.disable.setOnClickListener{ _ -> finishAndRemoveTask()}
        binding.snooze.setOnClickListener{ _ -> alarmViewModel.snooze()}
        initAlarmSpeak()




        val id: Long? = intent.extras!!.get("id") as Long
        val hour:Int? = intent.extras!!.get("hour") as Int
        val minute:Int? = intent.extras!!.get("minute") as Int
        val day:Int? = intent.extras!!.get("day") as Int
        val singleDay:Boolean = intent.extras!!.get("singleAlarm") == "true"
        //Move this onto viewmodel
        //
        if (addAlarmViewModel.alarmActivityAddProcessed == false) {
            addAlarmViewModel.processAlarmRenewal(hour, minute, day, singleDay, id)
            addAlarmViewModel.alarmActivityAddProcessed = true
        }
        alarmTriviaViewModel.triviaData.observe(this, Observer{ data ->
            if (data != null) {
                alarmTriviaViewModel.setDataAndRetrieveAnswersArray(data)
            }
        })
    }
    fun onClickAnswer(view: View){
        val id = view.id

        if (alarmViewModel.ansSelectable.value == true) {

            alarmViewModel.ansFeedbackVisibility.value = View.VISIBLE

            /*var alphaAnimation = AlphaAnimation(R.animator.alpha, )*/
            var alphaAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.alpha)
            answerFeedback.alpha = 1.0f
            answerFeedback.startAnimation(alphaAnim)
        }


        if (view.findViewById<AppCompatRadioButton>(id).text.equals(alarmViewModel.correct_answer.value) && alarmViewModel.ansSelectable.value == true){
            alarmViewModel.ansFeedback.value = "Correct Answer!"
            alarmViewModel.attempts.value = alarmViewModel.attempts.value?.plus(1)
            alarmViewModel.ansVisiblity0.value = View.INVISIBLE
            alarmViewModel.ansVisiblity1.value = View.INVISIBLE
            alarmViewModel.ansVisiblity2.value = View.INVISIBLE
            alarmViewModel.ansVisiblity3.value = View.INVISIBLE
            when (id){
                R.id.ansChoice1 -> alarmViewModel.ansVisiblity0.value = View.VISIBLE
                R.id.ansChoice2 -> alarmViewModel.ansVisiblity1.value = View.VISIBLE
                R.id.ansChoice3 -> alarmViewModel.ansVisiblity2.value = View.VISIBLE
                R.id.ansChoice4 -> alarmViewModel.ansVisiblity3.value = View.VISIBLE
            }
            val height = (view.parent as View).height
            val path = Path().apply{
                lineTo(0f, (height/2).toFloat())
            }
            val animator = ObjectAnimator.ofFloat(view, View.X, View.Y, path).apply{
                duration = 2000
                start()
            }
            alarmViewModel.ansSelectable.value = false
        }
        else if (alarmViewModel.ansSelectable.value == true){
            alarmViewModel.ansFeedback.value = "Incorrect Answer!"
        }

    }

    fun initAlarmSpeak(){
        alarmViewModel.get_tts_inst(applicationContext)
    }



}