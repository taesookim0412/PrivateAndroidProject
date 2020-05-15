package com.allydev.ally

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.allydev.ally.utils.IntroUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    val triviaViewModel by viewModels<TriviaViewModel>()
    lateinit var introUtil: IntroUtil
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


        val navController = findNavController(R.id.mainFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)



        TriviaDatabase.getTriviaDao(application)
        val triviaViewModel = ViewModelProvider(this).get(TriviaViewModel::class.java)
        /*triviaViewModel.repository.api.switchToken_NewBuild("d5f9566082f19b4f8cb7ade00ba07212000e1c09bec57f870b0f1eef7d49ab4e")*/

        //Put appropriate questions


        triviaViewModel.validateQuestions_GT_50()
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
        if (triviaViewModel.categoryEntities.value?.size == 0){
            triviaViewModel.validateAPICategories()
        }

        //Map the fetched boolean
        var refreshed = false
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
        })




        //Then check if we selected categories
    }

    private fun trailWithDifficultyDialog() {
        if ((introUtil.STATE_Intro == true || introUtil.difficultyPref == -1) && introUtil.choice_Selected == true) {
            displayDifficultyDialog()
        }
    }

    lateinit var difficultyDialog: AlertDialog
    private fun displayDifficultyDialog(){
        val singleItems = arrayOf("Random", "Easy", "Medium", "Hard")
        var checkedItem = if (introUtil.difficultyPref == -1) 1 else introUtil.difficultyPref

        difficultyDialog =  MaterialAlertDialogBuilder(this)
            .setTitle("Difficulty")
            .setPositiveButton("OK") { dialog, which ->
                introUtil.setDifficulty(checkedItem)
                introUtil.STATE_Intro = false
                onChangeSettings()
                // Respond to positive button press
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                // Respond to item chosen
                checkedItem = which
            }
            .show()
    }

    private fun displayIntroDialog() {

    }

    lateinit var categoryDialog: AlertDialog

    private fun displayCategoryDialog() {
        if (introUtil.choice_Selected == false) {
            println("Opening new dialog")
            val categoryData:Array<TriviaCategoriesEntity> = introUtil.categoriesData.value?: emptyArray()
            //Allow for "RANDOM" Selection at index 0. Thus i+=1
            val singleItems =
                categoryData.foldIndexed(Array<String>(categoryData.size+1) { "" }) { i, a: Array<String>, data ->
                    if (i == 0) a[0] = "Random"
                    a[i+1] = data.name ?: ""
                    return@foldIndexed a
                }
            var checkedItem = 0
            println(introUtil.categoryPref)
            if (introUtil.categoryPref != -2L && introUtil.categoryPref != 0L) {
                for (i in 0..categoryData.size - 1) {
                    if (categoryData[i].id == introUtil.choice_Category) checkedItem = i + 1

                }
            }

            categoryDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Choose A Trivia Category")
                .setPositiveButton("OK") { dialog, which ->
                    // Respond to positive button press
                    introUtil.setCategory()
                    onChangeSettings()
                    introUtil.choice_Selected = true
                    trailWithDifficultyDialog()
                }
                // Single-choice items (initialized with checked item)
                .setSingleChoiceItems(singleItems, checkedItem ?: 0) { dialog, which ->
                    introUtil.choice_Category = if (which == 0) 0 else categoryData[which-1].id
                    // Respond to item chosen
                }
                .setOnCancelListener(DialogInterface.OnCancelListener {
                    println("Here")
                    introUtil.choice_Selected = true
                })
                .show()


        }

    }

    fun onChangeSettings(){
        if (introUtil.categoryPref != -2L && introUtil.difficultyPref != -1){
            //Wipe && Revalidate
            triviaViewModel.deleteAllTriviaAndRecreate()
        }
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
                displayDifficultyDialog()
                true
            }
            R.id.action_settings_category -> {
                introUtil.choice_Selected = false
                introUtil.choice_Category = introUtil.categoryPref
                displayCategoryDialog()
                /*NavHostFragment.findNavController(mainFragment).navigate(R.id.categoryFragment)*/
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClickSettings(item: MenuItem) {

    }
}
