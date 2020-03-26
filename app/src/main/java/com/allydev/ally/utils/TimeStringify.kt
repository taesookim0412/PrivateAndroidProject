package com.allydev.ally.utils

class TimeStringify {
    companion object {
        fun getHour(hour:Int): String{
            if (hour == 0){
                return "12"
            }
            else if (hour == 12){
                return "12"
            }
            else if (hour > 12){
                return (hour - 12).toString()
            }
            else{
                return hour.toString()
            }
        }
        fun getMinute(minute:Int):String{
            if (minute < 10){
                return "0$minute"
            }
            else{
                return minute.toString()
            }
        }
        fun getAmpm(hour:Int): String{
            if (hour == 12){
                return "PM"
            }
            else if (hour > 12){
                return "PM"
            }
            else {
                return "AM"
            }
        }
    }
}