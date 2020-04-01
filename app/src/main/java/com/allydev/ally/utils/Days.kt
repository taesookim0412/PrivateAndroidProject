package com.allydev.ally.utils

import com.allydev.ally.R
import com.allydev.ally.schemas.Alarm

class Days{
    companion object{
     val mon: Day =
        Day(2, "mon", R.id.mon)
    val tue: Day =
        Day(3, "tue", R.id.tue)
    val wed: Day =
        Day(4, "wed", R.id.wed)
    val thurs: Day =
        Day(5, "thurs", R.id.thurs)
    val fri: Day =
        Day(6, "fri", R.id.fri)
    val sat: Day =
        Day(7, "sat", R.id.sat)
    val sun: Day =
        Day(1, "sun", R.id.sun)
    val days: Array<Day> = arrayOf(mon, tue, wed, thurs, fri, sat, sun)

    fun strToDay(str: String): Day {
        if (str === "mon") return mon
        else if (str === "tue") return tue
        else if (str === "wed") return wed
        else if (str === "thurs") return thurs
        else if (str === "fri") return fri
        else if (str === "sat") return sat
        else if (str === "sun") return sun
        return mon
    }
    fun calIntToDay(calDate: Int): Day {
        if (calDate == 1) return sun
        else if (calDate == 2) return mon
        else if (calDate == 3) return tue
        else if (calDate == 4) return wed
        else if (calDate == 5) return thurs
        else if (calDate == 6) return fri
        else if (calDate == 7) return sat
        return mon
    }

        public fun createDaysBoolArr(newDaysSet: Set<Day>?): Array<Boolean>{
            val daysBoolArr = Array<Boolean>(7) { false }

            for (day: Day in newDaysSet.orEmpty()) {
                daysBoolArr[day.num - 1] = true
            }
            return daysBoolArr
        }
        public fun createDaysSet(it: Alarm): MutableSet<Day>{
            val newDaysSet:MutableSet<Day> = HashSet<Day>()
            if (it.sun == true) newDaysSet.add(Days.sun)
            if (it.mon == true) newDaysSet.add(Days.mon)
            if (it.tue == true) newDaysSet.add(Days.tue)
            if (it.wed == true) newDaysSet.add(Days.wed)
            if (it.thurs == true) newDaysSet.add(Days.thurs)
            if (it.fri == true) newDaysSet.add(Days.fri)
            if (it.sat == true) newDaysSet.add(Days.sat)
            return newDaysSet
        }

    }
}

data class Day(val num:Int, val name:String, val id:Int)