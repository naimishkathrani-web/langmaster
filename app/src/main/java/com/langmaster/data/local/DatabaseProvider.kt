package com.langmaster.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: run {
                android.util.Log.d("DatabaseProvider", "Building AppDatabase...")
                val startTime = System.currentTimeMillis()
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "langmaster_v2.db"
                ).fallbackToDestructiveMigration().build()
                android.util.Log.d("DatabaseProvider", "AppDatabase built in ${System.currentTimeMillis() - startTime}ms")
                db.also { instance = it }
            }
        }
    }
}
