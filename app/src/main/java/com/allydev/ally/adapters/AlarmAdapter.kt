package com.allydev.ally.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.allydev.ally.R
import com.allydev.ally.schemas.Alarm
import com.allydev.ally.viewmodels.AlarmHolderViewModel


class AlarmAdapter() : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {
    private var alarms = emptyList<Alarm>()

    class AlarmViewHolder(val binding: com.allydev.ally.databinding.RecyclerviewAlarmRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        return AlarmViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recyclerview_alarm_row,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.itemView.tag = alarm
        holder.binding.viewModel = AlarmHolderViewModel(alarm)
    }

    internal fun setAlarms(alarms: List<Alarm>) {
        this.alarms = alarms
        notifyDataSetChanged()
    }

    fun getId(context: Context, day: String): Int {
        return context.resources.getIdentifier(day, "id", context.packageName)
    }
}