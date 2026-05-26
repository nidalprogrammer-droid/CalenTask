package com.example

import android.app.Application
import androidx.room.Room
import com.example.model.AppDatabase
import com.example.repository.TaskRepository

class CalenTaskApp : Application() {
    val database by lazy { 
        Room.databaseBuilder(this, AppDatabase::class.java, "calentask_db")
            .fallbackToDestructiveMigration()
            .build() 
    }
    val repository by lazy { TaskRepository(database.taskDao()) }
}
