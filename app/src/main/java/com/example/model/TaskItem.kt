package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskType {
    TASK, EXAM, ESSAY, EVENT
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val timestamp: Long,
    val type: TaskType = TaskType.TASK,
    val hasAlert: Boolean = false,
    val alertOffsetMinutes: Int = 15,
    val isCompleted: Boolean = false
)
