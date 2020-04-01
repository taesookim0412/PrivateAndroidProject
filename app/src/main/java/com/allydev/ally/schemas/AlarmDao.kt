package com.allydev.ally.schemas

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert
    fun createAlarm(alarm: Alarm)

    @Update
    fun updateAlarm(vararg alarms: Alarm)

    @Delete
    fun deleteAlarm(vararg alarms: Alarm)

    @Query("SELECT * from alarms")
    fun findAll(): List<Alarm>

    @Query("SELECT * from alarms ORDER BY hour ASC, min ASC")
    fun findAllSorted(): LiveData<List<Alarm>>

    @Query("SELECT * from alarms WHERE hour = :pHour AND min = :pMinute")
    fun findByHourMinute(pHour: Int?, pMinute:Int?): List<Alarm>
}