package com.allydev.ally

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.allydev.ally.api.TriviaViewModel
import com.allydev.ally.databinding.ActivityMainBinding
import com.allydev.ally.schemas.AlarmDatabase
import com.allydev.ally.schemas.trivia.categories.TriviaCategoriesEntity
import com.allydev.ally.utils.triviaviewmodel.DataIntegrityUtil
import com.allydev.ally.utils.triviaviewmodel.IntroUtil
import com.allydev.ally.utils.triviaviewmodel.TriviaDataValidatorUtil
import com.allydev.ally.viewmodels.AdViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    val triviaViewModel by viewModels<TriviaViewModel>()
    val adViewModel by viewModels<AdViewModel>()
    lateinit var introUtil: IntroUtil
    lateinit var dataIntegrityUtil: DataIntegrityUtil
    lateinit var triviaDataValidatorUtil: TriviaDataValidatorUtil

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
//        val alarmIntent = Intent(this, AlarmActivity::class.java)
//        startActivity(alarmIntent)
        AlarmDatabase.getAlarmDao(applicationContext)
        introUtil = triviaViewModel.introUtil
        dataIntegrityUtil = triviaViewModel.dataIntegrityUtil
        triviaDataValidatorUtil = triviaViewModel.triviaDataValidatorUtil

        adViewModel
        val adRequest = AdRequest.Builder().build()

        val db = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = dataIntegrityUtil
            lifecycleOwner = this@MainActivity
            setSupportActionBar(toolbar)
            errorImage.setOnClickListener{showError()}
        }

        db.bannerAd.loadAd(adRequest)


        //val navController = findNavController(R.id.mainFragment)
        //val appBarConfiguration = AppBarConfiguration(navController.graph)
        //findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, appBarConfiguration)

        dataGrab()


    }

    private fun displaySoundDialog() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(introUtil.soundPreference))
        startActivityForResult(intent, 0)
        dataIntegrityUtil.setDialogPending()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        dataIntegrityUtil.setDialogPending()
        if (resultCode == Activity.RESULT_OK && requestCode == 0){
            val uri:Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val uriData = uri?.toString() ?: "none"
            triviaViewModel.introUtil.setSoundPref(uriData)
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
            //Delay these launches
            R.id.action_settings_category -> {
                openDialogQuery(0)
                true
            }
            R.id.action_settings_difficulty -> {
                openDialogQuery(1)
                true
            }
            R.id.action_settings_ringtone -> {
                openDialogQuery(2)
                true
            }
            R.id.action_settings_stats -> {
                openDialogQuery(3)
                true
            }
            R.id.action_settings_reset -> {
                openDialogQuery(4)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showError(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Network Error")
            .setMessage("Network error! Trivia data will not be available unless it is downloaded! Press retry while connected to the internet.")
            .setNeutralButton("OK") { dialog, which ->
            }
            .setPositiveButton("RETRY") { dialog, which ->
                triviaDataValidatorUtil.runValidators().observe(this, Observer{_-> })
            }
            .show()
    }

    fun dataGrab() {
        dataIntegrityUtil.setDialogPending()
        triviaDataValidatorUtil.initialValidator_SwitchInitial.observe(
            this,
            Observer prefObs@{ data:Triple<Boolean, Boolean, Boolean> ->
                if (!dataIntegrityUtil.getState(dataIntegrityUtil.vars.ST_INITIAL_INTEGRITY_DIALOG)) return@prefObs
                if (dataIntegrityUtil.data_error.value == View.VISIBLE){
                    showError()
                    return@prefObs
                }
                CoroutineScope(Dispatchers.IO).launch {
                    with(data) {
                        if (first) displayCategoryDialog()
                        else if (second) displayDifficultyDialog()
                        else if (third) displaySoundDialog()

                    }
                }
            })
    }

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
            .setOnCancelListener { dataIntegrityUtil.setDialogPending() }
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
                a[i + 1] =
                    "${data.name} (${triviaViewModel.questionCategoryCountPairs[data.id]?.second})"
                return@foldIndexed a
            }
        singleItems[0] = "Random (${introUtil.total_num_of_verified_questions})"
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
            .setOnCancelListener{ dataIntegrityUtil.setDialogPending() }
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
            Observer prefObs@{ _ ->
                if (!dataIntegrityUtil.getState(dataIntegrityUtil.vars.ST_INITIAL_INTEGRITY_DIALOG)) return@prefObs
                val cancellable = (choice == 0 || choice == 1 || choice == 4)
                if (dataIntegrityUtil.data_error.value == View.VISIBLE && cancellable){
                    Toast.makeText(this, "Not connected to the internet!", Toast.LENGTH_SHORT).show()
                    triviaDataValidatorUtil.runValidators().observe(this, Observer {})
                    return@prefObs
                }
                CoroutineScope(Dispatchers.IO).launch {
                    when (choice){
                        0 -> displayCategoryDialog()
                        1 -> displayDifficultyDialog()
                        2 -> displaySoundDialog()
                        3 -> displayStatsDialog()
                        4 -> displayResetAllDialog()
                    }
                }

            })
    }

    private fun displayStatsDialog() {
        Looper.prepare()
        MaterialAlertDialogBuilder(this)
            .setTitle("Device Trivia Stats")
            .setMessage("Questions available ${triviaViewModel.triviaSize}")
            .setPositiveButton("RETURN") { dialog, which ->
            }
            .show()
        Looper.loop()
    }


}