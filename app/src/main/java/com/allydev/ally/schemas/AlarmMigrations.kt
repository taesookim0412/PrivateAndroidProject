package com.allydev.ally.schemas

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AlarmMigrations {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE `alarms` (`id` INTEGER, `hour` INTEGER, `min` INTEGER, `days` TEXT, "
//                        + "PRIMARY KEY(`id`))")
            }
        }
    }
}