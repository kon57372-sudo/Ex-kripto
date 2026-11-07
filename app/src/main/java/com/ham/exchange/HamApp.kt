package com.ham.exchange

import android.app.Application
import androidx.room.Room
import com.ham.exchange.data.db.HamDatabase

class HamApp : Application() {
    val db by lazy { Room.databaseBuilder(this, HamDatabase::class.java, "ham.db").fallbackToDestructiveMigration().build() }
    val prefs by lazy { getSharedPreferences("ham", MODE_PRIVATE) }
}
