package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "SCIENTIFIC", "TVM", "CASH_FLOW", "BREAK_EVEN", "LOAN", "MARGIN", "STATISTICS"
    val title: String,
    val expression: String,
    val result: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
