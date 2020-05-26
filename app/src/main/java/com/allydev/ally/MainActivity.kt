package com.allydev.ally

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.api.TriviaViewModel
import com.allydev.ally.schemas.trivia.TriviaDatabase
import com.allydev.ally.schemas.trivia.categories.TriviaCategoriesEntity
import com.allydev.ally.utils.TriviaViewModel.DataIntegrityUtil
import com.allydev.ally.utils.TriviaViewModel.IntroUtil
import com.allydev.ally.utils.TriviaViewModel.TriviaDataValidatorUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    val triviaViewModel by viewModels<TriviaViewModel>()
    lateinit var introUtil: IntroUtil
    lateinit var dataIntegrityUtil: DataIntegrityUtil
    override fun onResume() {
        super.onResume()

        /*if (!addAlarmViewModel.isTimeHooked()) {

            registerReceiver(addAlarmViewModel.timeChangeReceiver, minuteTickIntent)
            addAlarmViewModel.intentHooked.value = true
        }*/
    }

    override fun onPause() {
        super.onPause()
        if (this::categoryDialog.isInitialized) categoryDialog.dismiss()
        if (this::difficultyDialog.isInitialized) difficultyDialog.dismiss()
    }

    private val minuteTickIntent = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*setSupportActionBar(toolbar)*/
        AlarmDatabase.getAlarmDao(applicationContext)
        introUtil = triviaViewModel.introUtil
        dataIntegrityUtil = triviaViewModel.dataIntegrityUtil
        triviaDataValidatorUtil  = triviaViewModel.triviaDataValidatorUtil


        val navController = findNavController(R.id.mainFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)



        /*TriviaDatabase.getTriviaDao(application)*/
        /*triviaViewModel.repository.api.switchToken_NewBuild("d5f9566082f19b4f8cb7ade00ba07212000e1c09bec57f870b0f1eef7d49ab4e")*/

        //Put appropriate questions


//        triviaViewModel.allTrivia?.observe(this, Observer{
//            Log.d("it size: ", it.size.toString())
//        })

        /*triviaViewModel.test()*/

        /*TimeZoneService().scheduleJob(applicationContext)
        var jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        var jobs = jobScheduler.allPendingJobs
        println(jobs.toString())*/


        //First check if we can fetch with our internet
        //Initialize a validation

        dataGrab()

        /*triviaViewModel.validateQuestions_Half()
        if (triviaViewModel.categoryEntities.value?.size == 0){
            triviaViewModel.validateAPICategories()
        }

        triviaViewModel.validateAPICategoryCts()*/

        //Map the fetched boolean

        /////////////////LEGACY
        /*var refreshed = false
        triviaViewModel.fetch_Attempted.observe(this, Observer { res ->
            if (res == false) return@Observer
            introUtil.categoriesData = triviaViewModel.categoryEntities
            if (introUtil.categoriesData.value?.size == 0) {
                //TODO: no network -  Add an option to play music, put that here
                println("No network")
            } else if (refreshed == false) {
                //No Preference: Intro Dialog
                if (introUtil.categoryPref == -2L) {
                    introUtil.choice_Selected = false
                    introUtil.STATE_Intro = true
                    displayCategoryDialog()
                }
                //else if has a pref but dialog pre-opened (screen flip)
                else if (introUtil.choice_Selected == false) displayCategoryDialog()
                refreshed = true
            }
        })*/




        //Then check if we selected categories
    }

    lateinit var triviaDataValidatorUtil: TriviaDataValidatorUtil

    private fun dataGrab() {
        if (dataIntegrityUtil.ST_INITIAL == false) return
        if (!dataIntegrityUtil.initialIntegrity()) return
        triviaDataValidatorUtil.runValidators().observe(this, Observer{res->
            if (res == true){
                //Network
                introUtil.setPreferences.observe(this, Observer{ data ->
                    CoroutineScope(Dispatchers.IO).launch{
                        data.also{
                            if (it.first == -2L) displayCategoryDialog()
                            else if (it.second == -1) displayDifficultyDialogQuery()
                        }
                    }

                })


                /*triviaDataValidatorUtil.initialPreferences().subscribeOn(Schedulers.newThread())
                    .subscribeWith(object: DisposableObserver<Boolean>(){
                        override fun onComplete() {
                        }

                        override fun onNext(settingsExistant: Boolean?) {
                            if (settingsExistant==null)return
                            if (!settingsExistant) {
                                introUtil.ST_INTRO = true
                            }

                        }

                        override fun onError(e: Throwable?) {
                        }

                    })*/
            }

        })

    }

    /*LEGACY
    private fun trailWithDifficultyDialog() {
        println("trailing")
        if ((introUtil.STATE_Intro == true || introUtil.difficultyPref == -1) && introUtil.choice_Selected == true) {
            displayDifficultyDialogQuery()
        }
    }*/

    lateinit var difficultyDialog: AlertDialog
    suspend fun displayDifficultyDialogQuery() {
        triviaViewModel.getQuestionCtPairs()
        val data = triviaViewModel.getQuestionCtsByCat(introUtil.categoryPref.value!!)
        displayDifficultyDialog(data)
    }

    private fun displayDifficultyDialog(categoryCounts: IntArray = triviaViewModel.getQuestionCtsByCat(introUtil.categoryPref.value!!)) {
        if (categoryCounts.equals(intArrayOf(0))) return
        val singleItems = arrayOf("Random", "Easy", "Medium", "Hard")
        val catChoice = introUtil.choice_Category
        singleItems.forEachIndexed { i, a ->
            val sb = StringBuilder(a)
            var ct = categoryCounts[i]
            singleItems[i] = sb.append(" (").append(ct.toString()).append(")").toString()
        }
        var checkedItem = if (introUtil.difficultyPref.value == -1) 1 else introUtil.difficultyPref.value!!

        Looper.prepare()
        difficultyDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Difficulty")
            .setPositiveButton("OK") { dialog, which ->
                introUtil.setDifficulty(checkedItem, categoryCounts[checkedItem])
                introUtil.ST_INTRO = false
                // Respond to positive button press
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                // Respond to item chosen
                checkedItem = which
            }
            .show()
        Looper.loop()
    }

    private fun displayIntroDialog() {

    }

    lateinit var categoryDialog: AlertDialog

    suspend fun displayCategoryDialog() {
        val categoryData: Array<TriviaCategoriesEntity> = triviaViewModel.triviaCategoryRepository.findAll()
        println("Category data: " + Arrays.toString(categoryData))
        //Allow for "RANDOM" Selection at index 0. Thus i+=1
        //Array of strings
        val singleItems =
            categoryData.foldIndexed(Array<String>(categoryData.size + 1) { "" }) { i, a: Array<String>, data ->
                if (i == 0) a[0] = "Random"
                a[i + 1] = data.name ?: ""
                return@foldIndexed a
            }
        var checkedItem = 0
        if (introUtil.categoryPref.value != -2L && introUtil.categoryPref.value != 0L) {
            for (i in 0..categoryData.size - 1) {
                if (categoryData[i].id == introUtil.choice_Category) checkedItem = i + 1

            }
        }
        Looper.prepare()
        categoryDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Choose A Trivia Category")
            .setPositiveButton("OK") { dialog, which ->
                // Respond to positive button press
                introUtil.setCategory()
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                introUtil.choice_CategoryString = singleItems[which]
                introUtil.choice_Category = if (which == 0) 0 else categoryData[which - 1].id
                // Respond to item chosen
            }
            .setOnCancelListener(DialogInterface.OnCancelListener {
                println("Here")
            })
            .show()
        Looper.loop()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings_difficulty -> {
                CoroutineScope(Dispatchers.IO).launch{displayDifficultyDialogQuery()}
                true
            }
            R.id.action_settings_category -> {
                CoroutineScope(Dispatchers.IO).launch{ displayCategoryDialog()}
                /*NavHostFragment.findNavController(mainFragment).navigate(R.id.categoryFragment)*/
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClickSettings(item: MenuItem) {

    }
}
