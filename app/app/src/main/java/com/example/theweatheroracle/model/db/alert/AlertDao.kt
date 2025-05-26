package com.example.theweatheroracle.model.db.alert

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.theweatheroracle.model.alert.Alert

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: Alert)

    @Query("SELECT * FROM alerts")
    suspend fun getAllAlerts(): List<Alert>

    @Delete
    suspend fun delete(alert: Alert)
}