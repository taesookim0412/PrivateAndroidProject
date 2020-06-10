package com.allydev.ally.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allydev.ally.R
import com.allydev.ally.adapters.AlarmAdapter
import com.allydev.ally.viewmodels.AddAlarmViewModel
import com.allydev.ally.schemas.AlarmDataViewModel
import kotlinx.android.synthetic.main.fragment_all_alarms.*

class AllAlarms : Fragment() {

    private val alarmViewModel by activityViewModels<AlarmDataViewModel>()
    private val addAlarmViewModel by activityViewModels<AddAlarmViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_all_alarms, container, false)
        // Inflate the layout for this fragment
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.show()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(context)
        val viewAdapter = AlarmAdapter(requireContext().applicationContext)
        /*val recyclerView: RecyclerView?  =*/
            alarms_view.apply{
            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter

        }
        alarmViewModel.allAlarmsSorted?.observe(viewLifecycleOwner, Observer { alarms ->
            alarms?.let{ viewAdapter.setAlarms(it) }
            Log.d("alarms", alarms.toString())
        })
        addAlarm.setOnClickListener{ tView -> addAlarmNav(tView)}

    }

    fun addAlarmNav(view:View){
        addAlarmViewModel.hardReset()
        view.findNavController().navigate(R.id.action_allAlarms_to_addAlarm)
    }



}
