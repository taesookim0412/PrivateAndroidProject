package com.allydev.ally.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allydev.ally.R
import com.allydev.ally.adapters.AlarmAdapter
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.schemas.AlarmViewModel
import kotlinx.android.synthetic.main.fragment_all_alarms.*

class AllAlarms : Fragment() {

//    private lateinit var alarmDao: AlarmDao
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var addAlarmViewModel: AddAlarmViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_all_alarms, container, false)
        // Inflate the layout for this fragment
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(context)
        val viewAdapter = AlarmAdapter()
        /*val recyclerView: RecyclerView?  =*/
            alarms_view.apply{
            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter

        }
        alarmViewModel = ViewModelProviders.of(activity!!).get(AlarmViewModel::class.java)
        alarmViewModel.allAlarmsSorted?.observe(this, androidx.lifecycle.Observer { alarms ->
            alarms?.let{ viewAdapter.setAlarms(it) }
            Log.d("alarms", alarms.toString())
        })
        addAlarm.setOnClickListener{ tView -> addAlarmNav(tView)}

    }

    fun addAlarmNav(view:View){
        addAlarmViewModel = ViewModelProviders.of(activity!!).get(AddAlarmViewModel::class.java)
        addAlarmViewModel.hardReset()
        view.findNavController().navigate(R.id.addAlarm)
    }



}
