package com.allydev.ally.fragments.dialogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.allydev.ally.R

class SleepGeniusAbout: DialogFragment() {
    companion object{
        fun newInstance(): SleepGeniusAbout{
            val f = SleepGeniusAbout()
            return f
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_sleep_genius_about, container, false)
        return root
    }

}