package com.allydev.ally

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.allydev.ally.api.TriviaViewModel
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.trivia.categories.TriviaCategoriesEntity
import com.allydev.ally.utils.TriviaViewModel.DataIntegrityUtil
import com.allydev.ally.utils.TriviaViewModel.IntroUtil
import com.allydev.ally.utils.TriviaViewModel.TriviaDataValidatorUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
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
    }

    override fun onPause() {
        super.onPause()
        if (this::categoryDialog.isInitialized) categoryDialog.dismiss()
        if (this::difficultyDialog.isInitialized) difficultyDialog.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmIntent = Intent(this, AlarmActivity::class.java)
        //startActivity(alarmIntent)


        setContentView(R.layout.activity_main)
        AlarmDatabase.getAlarmDao(applicationContext)
        introUtil = triviaViewModel.introUtil
        dataIntegrityUtil = triviaViewModel.dataIntegrityUtil
        triviaDataValidatorUtil = triviaViewModel.triviaDataValidatorUtil


        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.mainFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
        dataGrab()
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
            //Delay these launches
            R.id.action_settings_category -> {
                openDialogQuery(0)
                true
            }
            R.id.action_settings_difficulty -> {
                openDialogQuery(1)
                true
            }
            R.id.action_settings_reset -> {
                openDialogQuery(2)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    lateinit var triviaDataValidatorUtil: TriviaDataValidatorUtil

    fun dataGrab() {
        dataIntegrityUtil.setDialogPending()
        triviaDataValidatorUtil.initialValidator_SwitchInitial.observe(
            this,
            Observer prefObs@{ data:Pair<Boolean, Boolean> ->
                if (!dataIntegrityUtil.getState(dataIntegrityUtil.vars.ST_INITIAL_INTEGRITY_DIALOG)) return@prefObs
                CoroutineScope(Dispatchers.IO).launch {
                    with(data) {
                        if (first) displayCategoryDialog()
                        else if (second) displayDifficultyDialog()
                    }
                }
            })
    }


    //triviaDataValidatorUtil.runValidators().observe(this, Observer { res ->
    //            if (res != true) return@Observer
    //            introUtil.pref_Configs.observe(this, Observer prefObs@{ data ->
    //                if (!dataIntegrityUtil.getState(dataIntegrityUtil.vars.ST_INITIAL_INTEGRITY_DIALOG)) return@prefObs
    //                CoroutineScope(Dispatchers.IO).launch {
    //                    data.also {
    //                        if (it.first == -2L || it.second == -1)
    //                            if (it.first == -2L) displayCategoryDialog()
    //                            else if (it.second == -1) displayDifficultyDialogQuery()
    //                    }
    //                }
    //
    //            })
    //        })

    /*LEGACY
    private fun trailWithDifficultyDialog() {
        println("trailing")
        if ((introUtil.STATE_Intro == true || introUtil.difficultyPref == -1) && introUtil.choice_Selected == true) {
            displayDifficultyDialogQuery()
        }
    }*/

    lateinit var difficultyDialog: AlertDialog

    private fun displayDifficultyDialog(
        categoryCounts: IntArray = triviaViewModel.getQuestionCtsByCat(
            introUtil.categoryPref
        )
    ) {
        if (categoryCounts.equals(intArrayOf(0))) return
        val singleItems = arrayOf("Random", "Easy", "Medium", "Hard")
        singleItems.forEachIndexed { i, a ->
            val sb = StringBuilder(a)
            var ct = categoryCounts[i]
            singleItems[i] = sb.append(" (").append(ct.toString()).append(")").toString()
        }
        var checkedItem = introUtil.difficultyPref

        Looper.prepare()
        difficultyDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Difficulty")
            .setPositiveButton("OK") { dialog, which ->
                dataIntegrityUtil.setDialogPending()
                introUtil.setDifficulty(checkedItem, categoryCounts[checkedItem])
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

    private fun displayResetAllDialog() {
        Looper.prepare()
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Trivia Data?")
            .setMessage("This will delete all downloaded Trivia entries and also re-download more. Be sure to have an internet connection available!")
            .setNegativeButton("CANCEL") { dialog, which ->
                            }
            .setPositiveButton("ACCEPT") { dialog, which ->
                triviaViewModel.deleteAllTrivia()
            }
            .show()
        Looper.loop()
    }

    lateinit var categoryDialog: AlertDialog

    suspend fun displayCategoryDialog() {
        val categoryData: Array<TriviaCategoriesEntity> = triviaViewModel.findAllCategories()
        println("Category data: " + Arrays.toString(categoryData))
        //Allow for "RANDOM" Selection at index 0. Thus i+=1
        //Array of strings

        //0th element
        val singleItems =
            categoryData.foldIndexed(Array<String>(categoryData.size + 1) { "" }) { i, a: Array<String>, data ->
                if (i == 0) a[0] = "Random (${introUtil.total_num_of_verified_questions})"
                a[i + 1] =
                    "${data.name} (${triviaViewModel.questionCategoryCountPairs[data.id]?.second})"
                return@foldIndexed a
            }
        var checkedItem = 0
        if (introUtil.categoryPref != 0L) {
            for (i in 0..categoryData.size - 1) {
                if (categoryData[i].id == introUtil.categoryPref) checkedItem = i + 1

            }
        }
        var categoryId = 0L
        var categoryString = ""
        Looper.prepare()
        categoryDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Choose A Trivia Category")
            .setPositiveButton("OK") { dialog, which ->
                // Respond to positive button press
                dataIntegrityUtil.setDialogPending()
                introUtil.setCategory(categoryId, categoryString)
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                categoryString = if (which == 0) "" else categoryData[which-1].name?:""
                categoryId = if (which == 0) 0 else categoryData[which - 1].id
                // Respond to item chosen
            }
            .setOnCancelListener(DialogInterface.OnCancelListener {
                dataIntegrityUtil.setDialogPending()
            })
            .show()
        Looper.loop()

    }



    //0 -> Cat Dialog
    //1 -> Diff Dialog
    //2 -> reset
    fun openDialogQuery(choice: Int) {
        dataIntegrityUtil.setDialogPending()
            triviaDataValidatorUtil.initialValidator_SwitchInitial.observe(
            this,
            Observer prefObs@{ data:Pair<Boolean, Boolean> ->
                println("Called in dialog query")
                if (!dataIntegrityUtil.getState(dataIntegrityUtil.vars.ST_INITIAL_INTEGRITY_DIALOG)) return@prefObs
                CoroutineScope(Dispatchers.IO).launch {
                    if (choice == 0) displayCategoryDialog()
                    else if (choice == 1) displayDifficultyDialog()
                    else if (choice == 2) displayResetAllDialog()
                }

            })
    }

}
