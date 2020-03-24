package com.allydev.ally.fragments.dialogfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.allydev.ally.R
import com.allydev.ally.viewmodels.TimeViewModel

class SleepGenius : DialogFragment() {
    private lateinit var hourStr:String
    private lateinit var minuteStr:String
    private lateinit var ampmStr:String
    private lateinit var timeViewModel: TimeViewModel
    companion object{
        fun newInstance(): SleepGenius{
            val f = SleepGenius()
            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: com.allydev.ally.databinding.FragmentSleepGeniusBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sleep_genius, container, false)
        timeViewModel = ViewModelProviders.of(activity!!).get(TimeViewModel::class.java)
        binding.timeViewModel = timeViewModel
        val root = binding.root
        val wakeUpAt = binding.wakeUpAt
        wakeUpAt.setOnClickListener{view -> onClickWakeUpAt(view)}
        return root
    }

    fun onClickWakeUpAt(view:View){
        Log.d("asd", "asd")
    }
}