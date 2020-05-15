package com.allydev.ally.schemas

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "hour") val hour: Int?,
    @ColumnInfo(name = "min") val min: Int?,
    @ColumnInfo(name = "sun") val sun: Boolean?,
    @ColumnInfo(name = "mon") val mon: Boolean?,
    @ColumnInfo(name = "tue") val tue: Boolean?,
    @ColumnInfo(name = "wed") val wed: Boolean?,
    @ColumnInfo(name = "thurs") val thurs: Boolean?,
    @ColumnInfo(name = "fri") val fri: Boolean?,
    @ColumnInfo(name = "sat") val sat: Boolean?,
    @ColumnInfo(name = "daysSize") val daysElems: Int?,
    @ColumnInfo(name = "requestId") val requestId: Int?,
    @ColumnInfo(name = "singleAlarm") val singleAlarm: Boolean?

) {
    constructor(
        hour: Int?,
        min: Int?,
        days: Array<Boolean>,
        daysElems: Int?,
        requestId: Int?,
        singleAlarm: Boolean?

    ) : this(null, hour, min, days[0], days[1], days[2], days[3], days[4], days[5], days[6], daysElems, requestId, singleAlarm)
}