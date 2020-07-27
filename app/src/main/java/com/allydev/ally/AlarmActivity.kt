package com.allydev.ally

import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.app.AlarmManagerCompat.setAlarmClock
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.allydev.ally.api.TriviaViewModel
import com.allydev.ally.databinding.AlarmActivityBinding
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.viewmodels.AdViewModel
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.viewmodels.AlarmViewModel
import kotlinx.android.synthetic.main.alarm_activity.*
import kotlinx.coroutines.*

class AlarmActivity : AppCompatActivity() {
    private val addAlarmViewModel:AddAlarmViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val triviaViewModel: TriviaViewModel by viewModels()
    private val adsViewModel by viewModels<AdViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        AlarmDatabase.getInstance(applicationContext)
        AlarmDatabase.getAlarmDao(applicationContext)
        super.onCreate(savedInstanceState)

        val binding: AlarmActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.alarm_activity)
        binding.viewModel = alarmViewModel
        binding.setLifecycleOwner { lifecycle }
        binding.ansChoice1.setOnClickListener { view -> onClickAnswer(view) }
        binding.ansChoice2.setOnClickListener { view -> onClickAnswer(view) }
        binding.ansChoice3.setOnClickListener { view -> onClickAnswer(view) }
        binding.ansChoice4.setOnClickListener { view -> onClickAnswer(view) }
        binding.disable.setOnClickListener { _ -> finishAndRemoveTask() }
        binding.disableSound.setOnClickListener { _ -> disableSound() }
        triviaViewModel.triviaDataValidatorUtil.alarmValidator.observe(this, Observer {})
        adsViewModel


        val id = intent?.extras?.get("id") as Long?
        val hour: Int? = intent?.extras?.get("hour") as Int?
        val minute: Int? = intent?.extras?.get("minute") as Int?
        val day: Int? = intent?.extras?.get("day") as Int?
        val singleDay: Boolean = intent?.extras?.get("singleAlarm") == "true"
        val snooze = intent?.extras?.get("snooze") as Boolean?
        binding.snooze.setOnClickListener { _ ->
            processAlarmSnooze(hour, minute)
            finishAndRemoveTask()
        }
        //Move this onto viewmodel
        // Improve this
        if (snooze == false) {
            val diu = alarmViewModel.dataIntegrityUtil
            if (!diu.getState(diu.vars.ST_INITIAL_INTEGRITY_ALARM)) {
                //exec these in one coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    coroutineScope {
                        launch {
                            addAlarmViewModel.processAlarmRenewal(
                                hour,
                                minute,
                                day,
                                singleDay,
                                id
                            )
                        }
                    }
                }
            }
        }
        alarmViewModel.triviaDataMap.observe(this, Observer { data ->
            println("invoked")
            if (data != null) {
                println(data)
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        alarmViewModel.setDataAndRetrieveAnswersArray(data, alarmViewModel)
                    }
                    initAlarmSpeak()
                    var alarmUri: Uri =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    if (alarmViewModel.soundPref == "none") return@launch
                    else {
                        alarmUri = Uri.parse(alarmViewModel.soundPref)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        mp = MediaPlayer().apply {
                            setAudioStreamType(AudioManager.STREAM_ALARM)
                            setDataSource(this@AlarmActivity, alarmUri)
                            prepare()
                            start()
                        }
                    }
                }

            }
        })
    }

    private fun processAlarmSnooze(hour: Int?, minute: Int?) {
        val time = System.currentTimeMillis() + 300000
        val requestCode = (time / 1000000).toInt()
        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent.putExtra("snooze", true)
        alarmIntent.putExtra("hour", hour)
        alarmIntent.putExtra("minute", minute)
        alarmIntent.putExtra("requestCode", requestCode)
        val alarmPendingIntent: PendingIntent = PendingIntent.getActivity(
            this, requestCode, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            time,
            alarmPendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disableSound()
    }

    private fun disableSound() {
        if (this::mp.isInitialized) mp.release()
    }

    lateinit var mp:MediaPlayer
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