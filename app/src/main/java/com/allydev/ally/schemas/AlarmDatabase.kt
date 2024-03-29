package com.allydev.ally.schemas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AlarmDatabase? = null

        @Volatile
        lateinit var alarmDao: AlarmDao

        fun getAlarmDao(context:Context): AlarmDao{
            return if (this::alarmDao.isInitialized) alarmDao else getInstance(context).alarmDao().also { alarmDao = it }
        }

        fun getInstance(context: Context): AlarmDatabase {
            return instance ?: buildDatabase(context).also { instance = it }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AlarmDatabase {
            return Room.databaseBuilder(context, AlarmDatabase::class.java, "alarm-db")
//                .addMigrations(AlarmMigrations.MIGRATION_1_2)\
                .build()
        }
    }
}